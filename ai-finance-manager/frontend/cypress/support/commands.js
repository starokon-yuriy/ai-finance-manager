// ***********************************************
// Custom commands for Cypress tests
// ***********************************************

/**
 * Custom command to select an element by data-testid attribute
 * Usage: cy.getByTestId('element-id')
 */
Cypress.Commands.add('getByTestId', (testId) => {
  return cy.get(`[data-testid="${testId}"]`);
});

/**
 * Custom command to wait for the app to be fully loaded
 */
Cypress.Commands.add('waitForAppLoad', () => {
  cy.get('.app-header').should('be.visible');
  cy.get('.nav-tabs').should('be.visible');
});

/**
 * Custom command to intercept API calls
 */
Cypress.Commands.add('mockApiCalls', () => {
  // Mock categories endpoint
  cy.intercept('GET', '/api/v1/finance/categories?type=INCOMES', {
    statusCode: 200,
    body: [
      { idCategory: 1, description: 'Salary', type: 'INCOMES' },
      { idCategory: 2, description: 'Freelance', type: 'INCOMES' }
    ]
  }).as('getIncomeCategories');

  cy.intercept('GET', '/api/v1/finance/categories?type=EXPENSES', {
    statusCode: 200,
    body: [
      { idCategory: 3, description: 'Food', type: 'EXPENSES' },
      { idCategory: 4, description: 'Transport', type: 'EXPENSES' }
    ]
  }).as('getExpenseCategories');

  // Mock transactions endpoint for income
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
              amount: 5000,
              transactionDate: '2026-02-15',
              comment: 'Monthly salary'
            }
          ]
        }
      ]
    }
  }).as('getIncomeTransactions');

  // Mock transactions endpoint for expenses
  cy.intercept('GET', '/api/v1/finance/transactions?type=EXPENSES*', {
    statusCode: 200,
    body: {
      categorySummaries: [
        {
          category: { idCategory: 3, description: 'Food', type: 'EXPENSES' },
          categoryTotal: 300,
          transactions: [
            {
              idTransaction: 2,
              amount: 150,
              transactionDate: '2026-02-10',
              comment: 'Groceries'
            },
            {
              idTransaction: 3,
              amount: 150,
              transactionDate: '2026-02-12',
              comment: 'Restaurant'
            }
          ]
        }
      ]
    }
  }).as('getExpenseTransactions');

  // Mock create transaction endpoint
  cy.intercept('POST', '/api/v1/finance/transactions', {
    statusCode: 201,
    body: {
      idTransaction: 999,
      amount: 1000,
      transactionDate: '2026-02-15',
      comment: 'Test transaction'
    }
  }).as('createTransaction');

  // Mock CSV export endpoint
  cy.intercept('GET', '/api/v1/finance/transactions/export*', {
    statusCode: 200,
    body: 'Date,Category,Amount,Comment\n2026-02-15,Salary,5000,Monthly salary'
  }).as('exportCsv');
});

/**
 * Custom command to fill transaction form
 */
Cypress.Commands.add('fillTransactionForm', (data) => {
  if (data.category) {
    cy.get('select').first().select(data.category);
  }
  if (data.amount) {
    cy.get('input[type="number"]').clear().type(data.amount);
  }
  if (data.date) {
    cy.get('input[type="date"]').eq(2).clear().type(data.date);
  }
  if (data.comment) {
    cy.get('textarea').clear().type(data.comment);
  }
});

/**
 * Custom command to navigate to a specific tab
 */
Cypress.Commands.add('navigateToTab', (tabName) => {
  cy.contains('button', new RegExp(`^${tabName}$`, 'i')).click();
});

