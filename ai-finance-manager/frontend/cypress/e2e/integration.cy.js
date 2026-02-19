describe('Finance Manager - Full Integration', () => {
  beforeEach(() => {
    cy.mockApiCalls();
    cy.visit('/');
    cy.waitForAppLoad();
  });

  describe('End-to-End User Flows', () => {
    it('should complete a full income management workflow', () => {
      // Step 1: View existing income transactions
      cy.contains('h2', 'Income').should('be.visible');
      cy.wait('@getIncomeTransactions');
      cy.contains('Salary').should('be.visible');

      // Step 2: Add a new income transaction
      cy.contains('+ Add Income').click();
      cy.fillTransactionForm({
        category: '2',
        amount: '500',
        date: '2026-02-17',
        comment: 'Freelance project payment'
      });
      cy.contains('button', /^Add$/).click();
      cy.wait('@createTransaction');

      // Step 3: Verify transaction was added (list refreshed)
      cy.wait('@getIncomeTransactions');
      cy.get('.modal').should('not.exist');

      // Step 4: Apply date filter
      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-01');
        cy.contains('button', 'Apply').click();
      });
      cy.wait('@getIncomeTransactions');
    });

    it('should complete a full expense management workflow', () => {
      // Navigate to Expense tab
      cy.navigateToTab('Expense');
      cy.wait('@getExpenseTransactions');

      // View existing expenses
      cy.contains('Food').should('be.visible');
      cy.contains('Groceries').should('be.visible');

      // Add new expense
      cy.contains('+ Add Expense').click();
      cy.fillTransactionForm({
        category: '4',
        amount: '30',
        date: '2026-02-17',
        comment: 'Taxi ride'
      });
      cy.contains('button', /^Add$/).click();
      cy.wait('@createTransaction');

      // Verify expense was added
      cy.wait('@getExpenseTransactions');
      cy.get('.modal').should('not.exist');
    });

    it('should navigate through all tabs and view balances', () => {
      // Start on Income tab
      cy.contains('button', /^Income$/).should('have.class', 'active');
      cy.wait('@getIncomeTransactions');
      cy.contains('Salary').should('be.visible');

      // Navigate to Expense tab
      cy.navigateToTab('Expense');
      cy.wait('@getExpenseTransactions');
      cy.contains('Food').should('be.visible');

      // Navigate to Balance tab
      cy.navigateToTab('Balance');
      cy.get('.balance-view-toggle').should('be.visible');

      // View income balance
      cy.wait('@getIncomeTransactions');
      cy.get('.balance-table').should('be.visible');
      cy.get('.total-amount').should('contain', '$5000.00');

      // Switch to expense balance
      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@getExpenseTransactions');
      cy.get('.total-amount').should('contain', '$300.00');

      // Download CSV
      cy.window().then((win) => {
        cy.stub(win.URL, 'createObjectURL').returns('blob:mock-url');
        cy.stub(win.URL, 'revokeObjectURL');
      });
      cy.contains('button', 'Download CSV').click();
      cy.wait('@exportCsv');
    });

    it('should handle multiple transaction additions in sequence', () => {
      // Add first income
      cy.contains('+ Add Income').click();
      cy.fillTransactionForm({
        category: '1',
        amount: '1000',
        date: '2026-02-17',
        comment: 'First income'
      });
      cy.contains('button', /^Add$/).click();
      cy.wait('@createTransaction');
      cy.wait('@getIncomeTransactions');

      // Add second income
      cy.contains('+ Add Income').click();
      cy.fillTransactionForm({
        category: '2',
        amount: '500',
        date: '2026-02-18',
        comment: 'Second income'
      });
      cy.contains('button', /^Add$/).click();
      cy.wait('@createTransaction');
      cy.wait('@getIncomeTransactions');

      // Switch to Expense and add expense
      cy.navigateToTab('Expense');
      cy.wait('@getExpenseTransactions');
      cy.contains('+ Add Expense').click();
      cy.fillTransactionForm({
        category: '3',
        amount: '50',
        date: '2026-02-17',
        comment: 'New expense'
      });
      cy.contains('button', /^Add$/).click();
      cy.wait('@createTransaction');
      cy.wait('@getExpenseTransactions');
    });

    it('should apply different date filters on each tab', () => {
      // Wait for initial load
      cy.wait('@getIncomeTransactions');

      // Set income date filter
      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-10');
        cy.contains('button', 'Apply').click();
      });

      // Wait for the new request with updated date
      cy.wait('@getIncomeTransactions');

      // Switch to Expense and set different filter
      cy.navigateToTab('Expense');
      cy.wait('@getExpenseTransactions');

      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-05');
        cy.contains('button', 'Apply').click();
      });
      cy.wait('@getExpenseTransactions');

      // Switch to Balance and set another filter
      cy.navigateToTab('Balance');
      cy.wait('@getIncomeTransactions');

      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-01');
        cy.contains('button', 'Apply').click();
      });
      cy.wait('@getIncomeTransactions');

      // Verify filters are maintained when switching back
      cy.navigateToTab('Income');
      cy.get('.filters-row input[type="date"]').first()
        .should('have.value', '2026-02-10');
    });
  });

  describe('Backend Integration', () => {
    it('should correctly call backend API for categories', () => {
      cy.wait('@getIncomeCategories').then((interception) => {
        expect(interception.request.url).to.include('/api/v1/finance/categories');
        expect(interception.request.url).to.include('type=INCOMES');
        expect(interception.response.statusCode).to.equal(200);
      });

      cy.wait('@getExpenseCategories').then((interception) => {
        expect(interception.request.url).to.include('/api/v1/finance/categories');
        expect(interception.request.url).to.include('type=EXPENSES');
        expect(interception.response.statusCode).to.equal(200);
      });
    });

    it('should correctly call backend API for transactions', () => {
      cy.wait('@getIncomeTransactions').then((interception) => {
        expect(interception.request.url).to.include('/api/v1/finance/transactions');
        expect(interception.request.url).to.include('type=INCOMES');
        expect(interception.request.url).to.include('dateFrom');
        expect(interception.request.url).to.include('dateTo');
        expect(interception.response.statusCode).to.equal(200);
      });
    });

    it('should send correct payload when creating transaction', () => {
      cy.contains('+ Add Income').click();
      cy.fillTransactionForm({
        category: '1',
        amount: '1500',
        date: '2026-02-20',
        comment: 'Bonus payment'
      });
      cy.contains('button', /^Add$/).click();

      cy.wait('@createTransaction').then((interception) => {
        expect(interception.request.method).to.equal('POST');
        expect(interception.request.url).to.include('/api/v1/finance/transactions');
        expect(interception.request.body).to.deep.include({
          amount: 1500,
          categoryId: 1,
          comment: 'Bonus payment'
        });
        expect(interception.request.body.transactionDate).to.exist;
        expect(interception.response.statusCode).to.equal(201);
      });
    });

    it('should call export API with correct parameters', () => {
      cy.navigateToTab('Balance');

      cy.get('.filters-row').within(() => {
        cy.get('input[type="date"]').first().clear().type('2026-02-01');
        cy.get('input[type="date"]').eq(1).clear().type('2026-02-28');
      });

      cy.window().then((win) => {
        cy.stub(win.URL, 'createObjectURL').returns('blob:mock-url');
        cy.stub(win.URL, 'revokeObjectURL');
      });

      cy.contains('button', 'Download CSV').click();

      cy.wait('@exportCsv').then((interception) => {
        expect(interception.request.url).to.include('/api/v1/finance/transactions/export');
        expect(interception.request.url).to.include('dateFrom=2026-02-01');
        expect(interception.request.url).to.include('dateTo=2026-02-28');
      });
    });
  });

  describe('Error Recovery', () => {
    it('should recover from failed transaction creation', () => {
      // First attempt fails
      cy.intercept('POST', '/api/v1/finance/transactions', {
        statusCode: 400,
        body: { message: 'Validation error' }
      }).as('failedCreate');

      cy.contains('+ Add Income').click();
      cy.fillTransactionForm({
        category: '1',
        amount: '100',
        date: '2026-02-17',
        comment: 'Test'
      });

      cy.window().then((win) => {
        cy.stub(win, 'alert').as('alertStub');
      });

      cy.contains('button', /^Add$/).click();
      cy.wait('@failedCreate');
      cy.get('@alertStub').should('have.been.called');

      // Modal should still be open for retry
      cy.get('.modal').should('be.visible');

      // Second attempt succeeds
      cy.intercept('POST', '/api/v1/finance/transactions', {
        statusCode: 201,
        body: { idTransaction: 999 }
      }).as('successCreate');

      cy.contains('button', /^Add$/).click();
      cy.wait('@successCreate');
      cy.get('.modal').should('not.exist');
    });

    it('should handle network errors and continue functioning', () => {
      // Clear the existing mock and set up error response
      cy.intercept('GET', '/api/v1/finance/transactions*type=EXPENSES*', {
        forceNetworkError: true
      }).as('networkError');

      // Reload to clear mocks and switch tab
      cy.reload();
      cy.waitForAppLoad();
      cy.navigateToTab('Expense');

      // App should handle the error gracefully and not crash
      // It will show empty state after error
      cy.get('.transactions-container').should('exist');

      // Should still be able to switch tabs
      cy.navigateToTab('Income');
      cy.contains('Salary', { timeout: 10000 }).should('be.visible');
    });

    it('should handle partial API failures gracefully', () => {
      // Categories fail but transactions succeed
      cy.intercept('GET', '/api/v1/finance/categories?type=INCOMES', {
        statusCode: 500
      }).as('failedCategories');

      cy.reload();
      cy.wait('@failedCategories');

      // App should still load transactions
      cy.wait('@getIncomeTransactions');
      cy.contains('Salary').should('be.visible');
    });
  });

  describe('Data Consistency', () => {
    it('should maintain data consistency across tab switches', () => {
      // Load income data
      cy.wait('@getIncomeTransactions');

      // Should show income total from mock data ($5000)
      cy.get('.total-amount').should('contain', '$5000.00');

      // Switch to expense
      cy.navigateToTab('Expense');
      cy.wait('@getExpenseTransactions');

      // Should show expense total from mock data ($300)
      cy.get('.total-amount').should('contain', '$300.00');

      // Switch to balance - should show same totals
      cy.navigateToTab('Balance');
      cy.wait('@getIncomeTransactions');
      cy.get('.total-amount').should('contain', '$5000.00');

      cy.get('.balance-view-toggle').contains('button', 'Expenses').click();
      cy.wait('@getExpenseTransactions');
      cy.get('.total-amount').should('contain', '$300.00');
    });

    it('should refresh data after adding transaction', () => {
      // Check initial count
      cy.wait('@getIncomeTransactions');
      cy.get('.transactions-table tbody tr').its('length').then((initialCount) => {

        // Add transaction
        cy.contains('+ Add Income').click();
        cy.fillTransactionForm({
          category: '1',
          amount: '100',
          date: '2026-02-17',
          comment: 'Test'
        });
        cy.contains('button', /^Add$/).click();
        cy.wait('@createTransaction');

        // Data should be refreshed
        cy.wait('@getIncomeTransactions');

        // Note: Since we're mocking, the count won't actually increase,
        // but we verify the refresh happened
        cy.get('.transactions-table').should('be.visible');
      });
    });
  });

  describe('User Experience', () => {
    it('should provide smooth navigation between tabs', () => {
      // Rapidly switch between tabs
      cy.navigateToTab('Expense');
      cy.navigateToTab('Income');
      cy.navigateToTab('Balance');
      cy.navigateToTab('Expense');

      // Should end up on correct tab
      cy.contains('button', /^Expense$/).should('have.class', 'active');
      cy.contains('h2', 'Expense').should('be.visible');
    });

    it('should maintain UI state during data loading', () => {
      // Navigate to expense tab and verify UI elements are present
      cy.navigateToTab('Expense');
      
      // Tab should be marked as active
      cy.contains('button', /^Expense$/).should('have.class', 'active');
      
      // Verify the main content header
      cy.contains('h2', 'Expense').should('be.visible');

      // Verify the transactions container exists
      cy.get('.transactions-container').should('exist');
      
      // Verify the Add Expense button is visible
      cy.contains('+ Add Expense').should('be.visible');
      
      // Wait for data to load
      cy.wait('@getExpenseTransactions');
      
      // Verify data is displayed after loading
      cy.contains('Food').should('be.visible');
    });

    it('should close modal on successful transaction creation', () => {
      cy.contains('+ Add Income').click();
      cy.get('.modal').should('be.visible');

      cy.fillTransactionForm({
        category: '1',
        amount: '100',
        date: '2026-02-17',
        comment: 'Test'
      });

      cy.contains('button', /^Add$/).click();
      cy.wait('@createTransaction');

      // Modal should automatically close
      cy.get('.modal').should('not.exist');
    });
  });

  describe('Accessibility', () => {
    it('should have proper button labels', () => {
      cy.get('button').each(($btn) => {
        // Check that button has text content (not empty)
        const text = $btn.text().trim();
        expect(text).to.not.be.empty;
      });
    });

    it('should have proper form labels', () => {
      cy.contains('+ Add Income').click();

      cy.get('label').should('have.length.at.least', 4);
      cy.contains('label', 'Category').should('exist');
      cy.contains('label', 'Amount').should('exist');
      cy.contains('label', 'Date').should('exist');
      cy.contains('label', 'Comment').should('exist');
    });

    it('should support keyboard navigation in modal', () => {
      cy.contains('+ Add Income').click();

      // Wait for modal to be visible
      cy.get('.modal').should('be.visible');

      // Verify form elements can receive focus
      cy.get('.modal select').first().focus();
      cy.focused().should('be.visible').and('have.prop', 'tagName', 'SELECT');

      // Move focus to next element (amount input)
      cy.get('.modal input[type="number"]').first().focus();
      cy.focused().should('have.attr', 'type', 'number');

      // Move focus to date input
      cy.get('.modal input[type="date"]').first().focus();
      cy.focused().should('have.attr', 'type', 'date');

      // Verify textarea can also receive focus
      cy.get('.modal textarea').first().focus();
      cy.focused().should('have.prop', 'tagName', 'TEXTAREA');
    });
  });
});

