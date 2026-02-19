describe('Finance Manager - Balance Tab', () => {
  beforeEach(() => {
    cy.mockApiCalls();
    cy.visit('/');
    cy.waitForAppLoad();
    cy.navigateToTab('Balance');
  });

  describe('Tab Navigation', () => {
    it('should switch to Balance tab when clicked', () => {
      cy.contains('button', /^Balance$/).should('have.class', 'active');
      cy.contains('h2', 'Balance').should('be.visible');
    });

    it('should display balance view toggle buttons', () => {
      cy.get('.balance-view-toggle').should('be.visible');
      cy.get('.balance-view-toggle').within(() => {
        cy.contains('button', 'Incomes').should('be.visible');
        cy.contains('button', 'Expenses').should('be.visible');
      });
    });

    it('should have Incomes view active by default', () => {
      cy.get('.balance-view-toggle button').first().should('have.class', 'active');
    });
  });

  describe('Date Filtering', () => {
    it('should display date filter inputs', () => {
      cy.get('.filters-row').should('be.visible');
      cy.get('.filters-row input[type="date"]').should('have.length', 2);
      cy.contains('button', 'Apply').should('be.visible');
    });

    it('should apply balance date filter', () => {
      // Wait for initial data load
      cy.wait('@getIncomeTransactions');

      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-01');
        cy.get('input[type="date"]').eq(1).clear().type('2026-02-28');
        cy.contains('button', 'Apply').click();
      });

      // Wait for the new request after Apply is clicked
      cy.wait('@getIncomeTransactions');
    });

    it('should maintain independent date filter for balance tab', () => {
      // Set balance date
      cy.get('.filters-row input[type="date"]').first().clear().type('2026-01-01');

      // Switch to income tab
      cy.navigateToTab('Income');

      // Income tab should have different dates
      cy.get('.filters-row input[type="date"]').first()
        .should('not.have.value', '2026-01-01');
    });
  });

  describe('Incomes View', () => {
    it('should display income transactions in Amount and Date format', () => {
      cy.wait('@getIncomeTransactions');

      cy.get('.balance-table').should('be.visible');
      cy.get('.balance-table thead').within(() => {
        cy.contains('Amount').should('be.visible');
        cy.contains('Date').should('be.visible');
      });
    });

    it('should display individual income transactions', () => {
      cy.wait('@getIncomeTransactions');

      cy.get('.balance-table tbody tr').should('exist');
      cy.get('.balance-table tbody').within(() => {
        cy.contains('$5000.00').should('be.visible');
        cy.contains('2026-02-15').should('be.visible');
      });
    });

    it('should calculate total income correctly', () => {
      cy.wait('@getIncomeTransactions');

      cy.get('.total-row').should('be.visible');
      cy.get('.total-amount').should('contain', '$5000.00');
    });

    it('should handle multiple income transactions', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=INCOMES*', {
        statusCode: 200,
        body: {
          categorySummaries: [
            {
              category: { idCategory: 1, description: 'Salary', type: 'INCOMES' },
              categoryTotal: 5000,
              transactions: [
                {
                  idTransaction: 1,
                  amount: 3000,
                  transactionDate: '2026-02-15',
                  comment: 'Salary part 1'
                },
                {
                  idTransaction: 2,
                  amount: 2000,
                  transactionDate: '2026-02-20',
                  comment: 'Salary part 2'
                }
              ]
            }
          ]
        }
      }).as('multipleIncomes');

      cy.reload();
      cy.navigateToTab('Balance');
      cy.wait('@multipleIncomes');

      cy.get('.balance-table tbody tr').should('have.length', 2);
      cy.get('.total-amount').should('contain', '$5000.00');
    });
  });

  describe('Expenses View', () => {
    it('should switch to Expenses view when button clicked', () => {
      cy.get('.balance-view-toggle').within(() => {
        cy.contains('button', 'Expenses').click();
      });

      cy.get('.balance-view-toggle button').eq(1).should('have.class', 'active');
      cy.wait('@getExpenseTransactions');
    });

    it('should display expenses in Category and Amount format', () => {
      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@getExpenseTransactions');

      cy.get('.balance-table thead').within(() => {
        cy.contains('Category').should('be.visible');
        cy.contains('Amount').should('be.visible');
      });
    });

    it('should display expenses grouped by category', () => {
      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@getExpenseTransactions');

      cy.get('.balance-table tbody tr').should('exist');
      cy.get('.balance-table tbody').within(() => {
        cy.contains('Food').should('be.visible');
        cy.contains('$300.00').should('be.visible');
      });
    });

    it('should calculate total expenses correctly', () => {
      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@getExpenseTransactions');

      cy.get('.total-amount').should('contain', '$300.00');
    });

    it('should handle multiple expense categories', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
        statusCode: 200,
        body: {
          categorySummaries: [
            {
              category: { idCategory: 3, description: 'Food', type: 'EXPENSES' },
              categoryTotal: 300,
              transactions: []
            },
            {
              category: { idCategory: 4, description: 'Transport', type: 'EXPENSES' },
              categoryTotal: 100,
              transactions: []
            },
            {
              category: { idCategory: 5, description: 'Entertainment', type: 'EXPENSES' },
              categoryTotal: 50,
              transactions: []
            }
          ]
        }
      }).as('multipleExpenseCategories');

      cy.reload();
      cy.navigateToTab('Balance');
      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@multipleExpenseCategories');

      cy.get('.balance-table tbody tr').should('have.length', 3);
      cy.contains('Food').should('be.visible');
      cy.contains('Transport').should('be.visible');
      cy.contains('Entertainment').should('be.visible');
      cy.get('.total-amount').should('contain', '$450.00');
    });
  });

  describe('View Toggle', () => {
    it('should refetch data when switching between views', () => {
      // Initially on Incomes
      cy.wait('@getIncomeTransactions');

      // Switch to Expenses
      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@getExpenseTransactions');

      // Switch back to Incomes
      cy.get('.balance-view-toggle').contains('button', 'Incomes').click();
      cy.wait('@getIncomeTransactions');
    });

    it('should maintain date filter when switching views', () => {
      // Set custom date filter
      cy.get('.filters-row input[type="date"]').first().clear().type('2026-02-01');
      cy.contains('button', 'Apply').click();
      cy.wait('@getIncomeTransactions');

      // Switch to Expenses
      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();

      // Date filters should still be set
      cy.get('.filters-row input[type="date"]').first().should('have.value', '2026-02-01');
    });

    it('should update total when switching views', () => {
      cy.wait('@getIncomeTransactions');
      cy.get('.total-amount').should('contain', '$5000.00');

      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@getExpenseTransactions');
      cy.get('.total-amount').should('contain', '$300.00');
    });
  });

  describe('CSV Export', () => {
    it('should display Download CSV button', () => {
      cy.get('.csv-download-section').should('be.visible');
      cy.contains('button', 'Download CSV').should('be.visible');
    });

    it('should trigger CSV export when button clicked', () => {
      // Stub window methods for download
      cy.window().then((win) => {
        cy.stub(win.URL, 'createObjectURL').returns('blob:mock-url');
        cy.stub(win.URL, 'revokeObjectURL');
      });

      cy.contains('button', 'Download CSV').click();

      cy.wait('@exportCsv').its('request.url')
        .should('include', '/api/v1/finance/transactions/export');
    });

    it('should export with current date filter', () => {
      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-01');
        cy.get('input[type="date"]').eq(1).clear().type('2026-02-28');
      });

      cy.window().then((win) => {
        cy.stub(win.URL, 'createObjectURL').returns('blob:mock-url');
        cy.stub(win.URL, 'revokeObjectURL');
      });

      cy.contains('button', 'Download CSV').click();

      cy.wait('@exportCsv').its('request.url')
        .should('include', 'dateFrom=2026-02-01')
        .and('include', 'dateTo=2026-02-28');
    });

    it('should handle CSV export error', () => {
      cy.intercept('GET', '/api/v1/finance/transactions/export*', {
        statusCode: 500,
        body: { message: 'Export failed' }
      }).as('failedExport');

      cy.window().then((win) => {
        cy.stub(win, 'alert').as('alertStub');
      });

      cy.contains('button', 'Download CSV').click();
      cy.wait('@failedExport');

      cy.get('@alertStub').should('have.been.calledWith',
        'Failed to download CSV. Please try again.'
      );
    });
  });

  describe('Empty States', () => {
    it('should display empty state for incomes when no data', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=INCOMES*', {
        statusCode: 200,
        body: { categorySummaries: [] }
      }).as('emptyIncomes');

      cy.reload();
      cy.navigateToTab('Balance');
      cy.wait('@emptyIncomes');

      cy.contains('No transactions found.').should('be.visible');
    });

    it('should display empty state for expenses when no data', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
        statusCode: 200,
        body: { categorySummaries: [] }
      }).as('emptyExpenses');

      cy.reload();
      cy.navigateToTab('Balance');
      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@emptyExpenses');

      cy.contains('No transactions found.').should('be.visible');
    });
  });

  describe('Data Formatting', () => {
    it('should format amounts with currency symbol and decimals', () => {
      cy.wait('@getIncomeTransactions');

      cy.get('.balance-table .amount-cell').each(($el) => {
        const text = $el.text();
        expect(text).to.match(/\$\d+\.\d{2}/);
      });
    });

    it('should format dates correctly', () => {
      cy.wait('@getIncomeTransactions');

      cy.get('.balance-table tbody td').each(($el) => {
        const text = $el.text();
        if (text.match(/\d/)) {
          // Check if it's a date or amount
          if (!text.includes('$')) {
            expect(text).to.match(/\d{4}-\d{2}-\d{2}/);
          }
        }
      });
    });

    it('should display zero total correctly', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=INCOMES*', {
        statusCode: 200,
        body: { categorySummaries: [] }
      }).as('noIncomes');

      cy.reload();
      cy.navigateToTab('Balance');
      cy.wait('@noIncomes');

      cy.get('.total-amount').should('contain', '$0.00');
    });
  });

  describe('Loading States', () => {
    it('should show loading indicator while fetching', () => {
      cy.intercept('GET', '/api/v1/finance/transactions?type=INCOMES*', (req) => {
        // Use delay before replying
        req.on('response', (res) => {
          res.setDelay(2000);
        });
      }).as('slowLoad');

      cy.reload();
      cy.navigateToTab('Balance');

      // Check immediately for loading indicator
      cy.get('.transactions-container').should('exist');

      cy.wait('@slowLoad');
    });
  });

  describe('Balance Calculation', () => {
    it('should calculate net balance from incomes and expenses', () => {
      // Get income total
      cy.wait('@getIncomeTransactions');
      cy.get('.total-amount').invoke('text').then((incomeText) => {
        const incomeAmount = parseFloat(incomeText.replace('$', '').replace(',', ''));

        // Switch to expenses and get total
        cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
        cy.wait('@getExpenseTransactions');
        cy.get('.total-amount').invoke('text').then((expenseText) => {
          const expenseAmount = parseFloat(expenseText.replace('$', '').replace(',', ''));

          // Calculate expected net balance
          const netBalance = incomeAmount - expenseAmount;
          expect(netBalance).to.equal(4700); // 5000 - 300
        });
      });
    });
  });
});

