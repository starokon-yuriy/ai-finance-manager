describe('Finance Manager - Income Tab', () => {
  beforeEach(() => {
    cy.mockApiCalls();
    cy.visit('/');
    cy.waitForAppLoad();
  });

  describe('Page Load and Initial State', () => {
    it('should display the app header and title', () => {
      cy.get('.app-header').should('be.visible');
      cy.contains('Finance Manager').should('be.visible');
      cy.get('.logo-icon').should('contain', '💰');
    });

    it('should display all navigation tabs', () => {
      cy.get('.nav-tabs').within(() => {
        cy.contains('button', 'Income').should('be.visible');
        cy.contains('button', 'Expense').should('be.visible');
        cy.contains('button', 'Balance').should('be.visible');
      });
    });

    it('should display Income tab as active by default', () => {
      cy.contains('button', /^Income$/).should('have.class', 'active');
      cy.contains('h2', 'Income').should('be.visible');
      cy.contains('+ Add Income').should('be.visible');
    });

    it('should load categories and transactions on mount', () => {
      cy.wait('@getIncomeCategories');
      cy.wait('@getExpenseCategories');
      cy.wait('@getIncomeTransactions');
      cy.wait('@getExpenseTransactions');
    });
  });

  describe('Income Transactions Display', () => {
    it('should display income transactions grouped by category', () => {
      cy.wait('@getIncomeTransactions');

      cy.get('.category-group').should('exist');
      cy.contains('Salary').should('be.visible');
      cy.contains('$5000.00').should('be.visible');
      cy.contains('Monthly salary').should('be.visible');
    });

    it('should display transaction details in table format', () => {
      cy.wait('@getIncomeTransactions');

      cy.get('.transactions-table').should('exist');
      cy.get('.transactions-table thead').within(() => {
        cy.contains('Date').should('be.visible');
        cy.contains('Comment').should('be.visible');
        cy.contains('Amount').should('be.visible');
      });

      cy.get('.transactions-table tbody tr').should('have.length.at.least', 1);
    });

    it('should calculate and display total income', () => {
      cy.wait('@getIncomeTransactions');

      cy.get('.total-row').should('be.visible');
      cy.get('.total-row').within(() => {
        cy.contains('Total:').should('be.visible');
        cy.get('.total-amount').should('contain', '$5000.00');
      });
    });

    it('should display empty state when no transactions exist', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=INCOMES*', {
        statusCode: 200,
        body: { categorySummaries: [] }
      }).as('emptyIncomeTransactions');

      cy.reload();
      cy.wait('@emptyIncomeTransactions');

      cy.contains('No income transactions found.').should('be.visible');
    });

    it('should display loading state while fetching data', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=INCOMES*', (req) => {
        req.reply({
          delay: 1000,
          statusCode: 200,
          body: {
            categorySummaries: [
              {
                category: { idCategory: 1, description: 'Salary', type: 'INCOMES' },
                categoryTotal: 5000,
                transactions: [
                  {
                    idTransaction: 1,
                    amount: 5000,
                    transactionDate: '2026-02-15',
                    comment: 'Monthly salary'
                  }
                ]
              }
            ]
          }
        });
      }).as('slowIncomeTransactions');

      cy.reload();
      cy.contains('Loading...').should('be.visible');
      cy.wait('@slowIncomeTransactions');
    });
  });

  describe('Date Filtering', () => {
    it('should display date filter inputs', () => {
      cy.get('.filters-row').should('exist');
      cy.contains('label', 'From:').should('be.visible');
      cy.contains('label', 'To:').should('be.visible');
      cy.get('input[type="date"]').should('have.length.at.least', 2);
    });

    it('should apply date filter when Apply button is clicked', () => {
      cy.wait('@getIncomeTransactions');

      // Change the date filter
      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-01');
        cy.get('input[type="date"]').eq(1).clear().type('2026-02-28');
        cy.contains('button', 'Apply').click();
      });

      // Wait for the new request after Apply
      cy.wait('@getIncomeTransactions');
    });
  });

  describe('Add Income Transaction', () => {
    it('should open add transaction modal when Add Income button is clicked', () => {
      cy.contains('+ Add Income').click();

      cy.get('.modal').should('be.visible');
      cy.get('.modal-header').within(() => {
        cy.contains('Add Income').should('be.visible');
        cy.get('.close-button').should('be.visible');
      });
    });

    it('should display all form fields in the modal', () => {
      cy.contains('+ Add Income').click();

      cy.get('.modal').within(() => {
        cy.contains('label', 'Category').should('be.visible');
        cy.get('select').should('be.visible');

        cy.contains('label', 'Amount').should('be.visible');
        cy.get('input[type="number"]').should('be.visible');

        cy.contains('label', 'Date').should('be.visible');
        cy.get('input[type="date"]').should('be.visible');

        cy.contains('label', 'Comment').should('be.visible');
        cy.get('textarea').should('be.visible');
      });
    });

    it('should display only income categories in the dropdown', () => {
      cy.wait('@getIncomeCategories');
      cy.contains('+ Add Income').click();

      cy.get('.modal select option').should('have.length', 3); // Including "Select a category"
      cy.get('.modal select').within(() => {
        cy.contains('Salary').should('exist');
        cy.contains('Freelance').should('exist');
        cy.contains('Food').should('not.exist');
      });
    });

    it('should close modal when close button is clicked', () => {
      cy.contains('+ Add Income').click();
      cy.get('.modal').should('be.visible');

      cy.get('.close-button').click();
      cy.get('.modal').should('not.exist');
    });

    it('should close modal when Cancel button is clicked', () => {
      cy.contains('+ Add Income').click();
      cy.get('.modal').should('be.visible');

      cy.contains('button', 'Cancel').click();
      cy.get('.modal').should('not.exist');
    });

    it('should close modal when clicking outside of it', () => {
      cy.contains('+ Add Income').click();
      cy.get('.modal').should('be.visible');

      cy.get('.modal-overlay').click({ force: true });
      cy.get('.modal').should('not.exist');
    });

    it('should successfully create a new income transaction', () => {
      cy.contains('+ Add Income').click();

      cy.fillTransactionForm({
        category: '1',
        amount: '1000',
        date: '2026-02-15',
        comment: 'Test income'
      });

      cy.contains('button', /^Add$/).click();

      cy.wait('@createTransaction').its('request.body').should('deep.include', {
        amount: 1000,
        categoryId: 1,
        comment: 'Test income'
      });

      // Modal should close after successful submission
      cy.get('.modal').should('not.exist');

      // Should refresh the transactions list
      cy.wait('@getIncomeTransactions');
    });

    it('should require all mandatory fields', () => {
      cy.contains('+ Add Income').click();

      cy.contains('button', /^Add$/).click();

      // Form should not submit without required fields
      cy.get('.modal').should('be.visible');
    });

    it('should validate amount field', () => {
      cy.contains('+ Add Income').click();

      cy.get('input[type="number"]').should('have.attr', 'step', '0.01');
      cy.get('input[type="number"]').should('have.attr', 'required');
    });
  });

  describe('Error Handling', () => {
    it('should handle API error when fetching transactions', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=INCOMES*', {
        statusCode: 500,
        body: { message: 'Internal Server Error' }
      }).as('failedIncomeTransactions');

      cy.reload();
      cy.wait('@failedIncomeTransactions');

      // Should display empty state on error
      cy.contains('No income transactions found.').should('be.visible');
    });

    it('should display alert when transaction creation fails', () => {
      cy.intercept('POST', '/api/v1/finance/transactions', {
        statusCode: 400,
        body: { message: 'Validation error' }
      }).as('failedCreateTransaction');

      cy.contains('+ Add Income').click();

      cy.fillTransactionForm({
        category: '1',
        amount: '1000',
        date: '2026-02-15',
        comment: 'Test'
      });

      // Stub the alert
      cy.window().then((win) => {
        cy.stub(win, 'alert').as('alertStub');
      });

      cy.contains('button', /^Add$/).click();
      cy.wait('@failedCreateTransaction');

      cy.get('@alertStub').should('have.been.calledWith',
        'Failed to add transaction. Please try again.'
      );
    });
  });

  describe('Responsive Behavior', () => {
    it('should be visible on mobile viewport', () => {
      cy.viewport('iphone-x');

      cy.get('.app-header').should('be.visible');
      cy.get('.nav-tabs').should('be.visible');
      cy.contains('+ Add Income').should('be.visible');
    });

    it('should be visible on tablet viewport', () => {
      cy.viewport('ipad-2');

      cy.get('.app-header').should('be.visible');
      cy.get('.transactions-container').should('be.visible');
    });
  });
});

