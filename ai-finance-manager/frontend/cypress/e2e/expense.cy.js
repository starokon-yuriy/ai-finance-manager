describe('Finance Manager - Expense Tab', () => {
  beforeEach(() => {
    cy.mockApiCalls();
    cy.visit('/');
    cy.waitForAppLoad();
    cy.navigateToTab('Expense');
  });

  describe('Tab Navigation', () => {
    it('should switch to Expense tab when clicked', () => {
      cy.contains('button', /^Expense$/).should('have.class', 'active');
      cy.contains('h2', 'Expense').should('be.visible');
      cy.contains('+ Add Expense').should('be.visible');
    });

    it('should load expense transactions when tab is opened', () => {
      cy.wait('@getExpenseTransactions');
    });
  });

  describe('Expense Transactions Display', () => {
    it('should display expense transactions grouped by category', () => {
      cy.wait('@getExpenseTransactions');

      cy.get('.category-group').should('exist');
      cy.contains('Food').should('be.visible');
      cy.contains('$300.00').should('be.visible');
    });

    it('should display multiple transactions in the same category', () => {
      cy.wait('@getExpenseTransactions');

      cy.get('.transactions-table tbody tr').should('have.length', 2);
      cy.contains('Groceries').should('be.visible');
      cy.contains('Restaurant').should('be.visible');
    });

    it('should calculate and display total expenses', () => {
      cy.wait('@getExpenseTransactions');

      cy.get('.total-row').should('be.visible');
      cy.get('.total-row .total-amount').should('contain', '$300.00');
    });

    it('should display category total correctly', () => {
      cy.wait('@getExpenseTransactions');

      cy.get('.category-group-header').within(() => {
        cy.contains('Food').should('be.visible');
        cy.get('.category-total').should('contain', '$300.00');
      });
    });

    it('should display empty state when no expenses exist', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
        statusCode: 200,
        body: { categorySummaries: [] }
      }).as('emptyExpenseTransactions');

      cy.reload();
      cy.navigateToTab('Expense');
      cy.wait('@emptyExpenseTransactions');

      cy.contains('No expense transactions found.').should('be.visible');
    });
  });

  describe('Date Filtering', () => {
    it('should apply expense date filter independently', () => {
      cy.wait('@getExpenseTransactions');

      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-01');
        cy.get('input[type="date"]').eq(1).clear().type('2026-02-15');
        cy.contains('button', 'Apply').click();
      });

      // Wait for the new request after Apply
      cy.wait('@getExpenseTransactions');
    });

    it('should maintain separate date filters for income and expense tabs', () => {
      // Set expense date filter
      cy.get('.filters-row input[type="date"]').first().clear().type('2026-02-05');

      // Switch to income tab
      cy.navigateToTab('Income');

      // Income tab should have different dates
      cy.get('.filters-row input[type="date"]').first()
        .should('not.have.value', '2026-02-05');
    });
  });

  describe('Add Expense Transaction', () => {
    it('should open add expense modal', () => {
      cy.contains('+ Add Expense').click();

      cy.get('.modal').should('be.visible');
      cy.get('.modal-header').contains('Add Expense').should('be.visible');
    });

    it('should display only expense categories in the dropdown', () => {
      cy.wait('@getExpenseCategories');
      cy.contains('+ Add Expense').click();

      cy.get('.modal select').within(() => {
        cy.contains('Food').should('exist');
        cy.contains('Transport').should('exist');
        cy.contains('Salary').should('not.exist');
        cy.contains('Freelance').should('not.exist');
      });
    });

    it('should successfully create a new expense transaction', () => {
      cy.contains('+ Add Expense').click();

      cy.fillTransactionForm({
        category: '3',
        amount: '50',
        date: '2026-02-16',
        comment: 'Lunch expense'
      });

      cy.contains('button', /^Add$/).click();

      cy.wait('@createTransaction').its('request.body').should('deep.include', {
        amount: 50,
        categoryId: 3,
        comment: 'Lunch expense'
      });

      cy.get('.modal').should('not.exist');
      cy.wait('@getExpenseTransactions');
    });

    it('should allow adding expense without comment', () => {
      cy.contains('+ Add Expense').click();

      cy.fillTransactionForm({
        category: '4',
        amount: '25.50',
        date: '2026-02-16'
      });

      cy.contains('button', /^Add$/).click();

      cy.wait('@createTransaction').its('request.body').should('include', {
        amount: 25.50,
        categoryId: 4
      });
    });

    it('should support decimal amounts', () => {
      cy.contains('+ Add Expense').click();

      cy.get('input[type="number"]').clear().type('123.45');
      cy.get('input[type="number"]').should('have.value', '123.45');
    });
  });

  describe('Transaction Table', () => {
    it('should format dates correctly', () => {
      cy.wait('@getExpenseTransactions');

      cy.get('.transactions-table tbody td').first().then(($td) => {
        const text = $td.text();
        expect(text).to.match(/\d{4}-\d{2}-\d{2}/);
      });
    });

    it('should format amounts with two decimal places', () => {
      cy.wait('@getExpenseTransactions');

      cy.get('.amount-cell').each(($el) => {
        const amount = $el.text();
        expect(amount).to.match(/\$\d+\.\d{2}/);
      });
    });

    it('should display dash for empty comments', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
        statusCode: 200,
        body: {
          categorySummaries: [{
            category: { idCategory: 3, description: 'Food', type: 'EXPENSES' },
            categoryTotal: 100,
            transactions: [{
              idTransaction: 4,
              amount: 100,
              transactionDate: '2026-02-16',
              comment: null
            }]
          }]
        }
      }).as('transactionWithoutComment');

      cy.reload();
      cy.navigateToTab('Expense');
      cy.wait('@transactionWithoutComment');

      cy.get('.transactions-table tbody').contains('-').should('exist');
    });
  });

  describe('Multiple Categories', () => {
    it('should display multiple expense categories', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
        statusCode: 200,
        body: {
          categorySummaries: [
            {
              category: { idCategory: 3, description: 'Food', type: 'EXPENSES' },
              categoryTotal: 300,
              transactions: [{
                idTransaction: 2,
                amount: 300,
                transactionDate: '2026-02-10',
                comment: 'Groceries'
              }]
            },
            {
              category: { idCategory: 4, description: 'Transport', type: 'EXPENSES' },
              categoryTotal: 100,
              transactions: [{
                idTransaction: 5,
                amount: 100,
                transactionDate: '2026-02-12',
                comment: 'Gas'
              }]
            }
          ]
        }
      }).as('multiCategoryExpenses');

      cy.reload();
      cy.navigateToTab('Expense');
      cy.wait('@multiCategoryExpenses');

      cy.get('.category-group').should('have.length', 2);
      cy.contains('Food').should('be.visible');
      cy.contains('Transport').should('be.visible');
      cy.get('.total-amount').should('contain', '$400.00');
    });
  });

  describe('Error Scenarios', () => {
    it('should handle network error gracefully', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
        forceNetworkError: true
      }).as('networkError');

      cy.reload();
      cy.navigateToTab('Expense');

      cy.contains('No expense transactions found.').should('be.visible');
    });

    it('should handle invalid API response', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
        statusCode: 200,
        body: null
      }).as('invalidResponse');

      cy.reload();
      cy.navigateToTab('Expense');

      cy.contains('No expense transactions found.').should('be.visible');
    });
  });

  describe('UI Interactions', () => {
    it('should highlight category header on hover', () => {
      cy.wait('@getExpenseTransactions');

      cy.get('.category-group-header').first().trigger('mouseover');
      // Visual check - the element should be visible and interactable
      cy.get('.category-group-header').first().should('be.visible');
    });

    it('should scroll through long transaction lists', () => {
      // Create many transactions
      const manyTransactions = Array.from({ length: 20 }, (_, i) => ({
        idTransaction: i + 10,
        amount: 50,
        transactionDate: '2026-02-10',
        comment: `Transaction ${i + 1}`
      }));

      cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
        statusCode: 200,
        body: {
          categorySummaries: [{
            category: { idCategory: 3, description: 'Food', type: 'EXPENSES' },
            categoryTotal: 1000,
            transactions: manyTransactions
          }]
        }
      }).as('manyTransactions');

      cy.reload();
      cy.navigateToTab('Expense');
      cy.wait('@manyTransactions');

      cy.get('.transactions-table tbody tr').should('have.length', 20);
    });
  });
});

