import React, { useState, useEffect } from 'react';
import './App.css';
import apiService from './services/apiService';

function App() {
  const [activeTab, setActiveTab] = useState('income');
  const [categories, setCategories] = useState([]);
  const [incomeTransactions, setIncomeTransactions] = useState([]);
  const [expenseTransactions, setExpenseTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [balanceView, setBalanceView] = useState('incomes'); // 'incomes' or 'expenses'

  const [newTransaction, setNewTransaction] = useState({
    amount: '',
    transactionDate: new Date().toISOString().split('T')[0],
    transactionType: 'INCOMES',
    categoryId: '',
    comment: ''
  });

  const [incomeDateFilter, setIncomeDateFilter] = useState({
    dateFrom: new Date(new Date().setDate(1)).toISOString().split('T')[0],
    dateTo: new Date().toISOString().split('T')[0]
  });

  const [expenseDateFilter, setExpenseDateFilter] = useState({
    dateFrom: new Date(new Date().setDate(1)).toISOString().split('T')[0],
    dateTo: new Date().toISOString().split('T')[0]
  });

  const [balanceDateFilter, setBalanceDateFilter] = useState({
    dateFrom: new Date(new Date().setDate(1)).toISOString().split('T')[0],
    dateTo: new Date().toISOString().split('T')[0]
  });

  useEffect(() => {
    fetchCategories();
    fetchIncomeTransactions();
    fetchExpenseTransactions();
  }, []);


  const fetchCategories = async () => {
    try {
      const [incomeCategories, expenseCategories] = await Promise.all([
        apiService.getCategories('INCOMES'),
        apiService.getCategories('EXPENSES')
      ]);
      setCategories([...incomeCategories, ...expenseCategories]);
    } catch (error) {
      console.error('Error fetching categories:', error);
    }
  };

  const fetchIncomeTransactions = async () => {
    try {
      setLoading(true);
      const response = await apiService.getTransactions(
        'INCOMES',
        incomeDateFilter.dateFrom,
        incomeDateFilter.dateTo
      );
      setIncomeTransactions(response.categorySummaries || []);
    } catch (error) {
      console.error('Error fetching income transactions:', error);
      setIncomeTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchExpenseTransactions = async () => {
    try {
      setLoading(true);
      const response = await apiService.getTransactions(
        'EXPENSES',
        expenseDateFilter.dateFrom,
        expenseDateFilter.dateTo
      );
      setExpenseTransactions(response.categorySummaries || []);
    } catch (error) {
      console.error('Error fetching expense transactions:', error);
      setExpenseTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchBalanceTransactions = async () => {
    try {
      setLoading(true);
      if (balanceView === 'incomes') {
        const response = await apiService.getTransactions(
          'INCOMES',
          balanceDateFilter.dateFrom,
          balanceDateFilter.dateTo
        );
        setIncomeTransactions(response.categorySummaries || []);
      } else {
        const response = await apiService.getTransactions(
          'EXPENSES',
          balanceDateFilter.dateFrom,
          balanceDateFilter.dateTo
        );
        setExpenseTransactions(response.categorySummaries || []);
      }
    } catch (error) {
      console.error('Error fetching balance transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddTransaction = async (e) => {
    e.preventDefault();
    try {
      await apiService.createTransaction({
        amount: parseFloat(newTransaction.amount),
        transactionDate: newTransaction.transactionDate,
        categoryId: parseInt(newTransaction.categoryId),
        comment: newTransaction.comment
      });

      setShowAddModal(false);
      setNewTransaction({
        amount: '',
        transactionDate: new Date().toISOString().split('T')[0],
        transactionType: activeTab === 'income' ? 'INCOMES' : 'EXPENSES',
        categoryId: '',
        comment: ''
      });

      if (activeTab === 'income') {
        fetchIncomeTransactions();
      } else if (activeTab === 'expense') {
        fetchExpenseTransactions();
      } else {
        fetchBalanceTransactions();
      }
    } catch (error) {
      console.error('Error adding transaction:', error);
      alert('Failed to add transaction. Please try again.');
    }
  };

  const handleDownloadCSV = async () => {
    try {
      const csvContent = await apiService.exportTransactionsToCsv(
        balanceDateFilter.dateFrom,
        balanceDateFilter.dateTo
      );

      // Create blob and download
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `transactions_${balanceDateFilter.dateFrom}_${balanceDateFilter.dateTo}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading CSV:', error);
      alert('Failed to download CSV. Please try again.');
    }
  };

  const calculateTotal = (transactions) => {
    return transactions.reduce((sum, summary) => sum + (summary.categoryTotal || 0), 0);
  };

  const openAddModal = (type) => {
    setNewTransaction({
      amount: '',
      transactionDate: new Date().toISOString().split('T')[0],
      transactionType: type,
      categoryId: '',
      comment: ''
    });
    setShowAddModal(true);
  };

  return (
    <div className="App">
      {/* Header */}
      <header className="app-header">
        <div className="header-content">
          <div className="logo">
            <span className="logo-icon">💰</span>
            <h1>Finance Manager</h1>
          </div>
        </div>
      </header>

      {/* Navigation Tabs */}
      <nav className="nav-tabs">
        <button
          className={activeTab === 'income' ? 'active' : ''}
          onClick={() => setActiveTab('income')}
        >
          Income
        </button>
        <button
          className={activeTab === 'expense' ? 'active' : ''}
          onClick={() => setActiveTab('expense')}
        >
          Expense
        </button>
        <button
          className={activeTab === 'balance' ? 'active' : ''}
          onClick={() => setActiveTab('balance')}
        >
          Balance
        </button>
      </nav>

      {/* Main Content */}
      <main className="main-content">
        {/* Tab 1: Income */}
        {activeTab === 'income' && (
          <div className="tab-content">
            <div className="tab-header">
              <h2>Income</h2>
              <button className="add-button-small" onClick={() => openAddModal('INCOMES')}>
                + Add Income
              </button>
            </div>

            <div className="filters-row">
              <div className="filter-group">
                <label>From:</label>
                <input
                  type="date"
                  value={incomeDateFilter.dateFrom}
                  onChange={(e) => setIncomeDateFilter({...incomeDateFilter, dateFrom: e.target.value})}
                />
              </div>
              <div className="filter-group">
                <label>To:</label>
                <input
                  type="date"
                  value={incomeDateFilter.dateTo}
                  onChange={(e) => setIncomeDateFilter({...incomeDateFilter, dateTo: e.target.value})}
                />
              </div>
              <button className="apply-button" onClick={fetchIncomeTransactions}>
                Apply
              </button>
            </div>

            <div className="transactions-container">
              {loading ? (
                <div className="loading">Loading...</div>
              ) : incomeTransactions.length === 0 ? (
                <div className="empty-state">No income transactions found.</div>
              ) : (
                <div className="category-list">
                  {incomeTransactions.map((summary, index) => (
                    <div key={index} className="category-group">
                      <div className="category-group-header">
                        <h3>{summary.category.description}</h3>
                        <span className="category-total">${summary.categoryTotal?.toFixed(2) || '0.00'}</span>
                      </div>
                      <table className="transactions-table">
                        <thead>
                          <tr>
                            <th>Date</th>
                            <th>Comment</th>
                            <th>Amount</th>
                          </tr>
                        </thead>
                        <tbody>
                          {summary.transactions.map((transaction) => (
                            <tr key={transaction.idTransaction}>
                              <td>{transaction.transactionDate}</td>
                              <td>{transaction.comment || '-'}</td>
                              <td className="amount-cell">${transaction.amount.toFixed(2)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ))}
                </div>
              )}

              <div className="total-row">
                <span className="total-label">Total:</span>
                <span className="total-amount">${calculateTotal(incomeTransactions).toFixed(2)}</span>
              </div>
            </div>
          </div>
        )}

        {/* Tab 2: Expense */}
        {activeTab === 'expense' && (
          <div className="tab-content">
            <div className="tab-header">
              <h2>Expense</h2>
              <button className="add-button-small" onClick={() => openAddModal('EXPENSES')}>
                + Add Expense
              </button>
            </div>

            <div className="filters-row">
              <div className="filter-group">
                <label>From:</label>
                <input
                  type="date"
                  value={expenseDateFilter.dateFrom}
                  onChange={(e) => setExpenseDateFilter({...expenseDateFilter, dateFrom: e.target.value})}
                />
              </div>
              <div className="filter-group">
                <label>To:</label>
                <input
                  type="date"
                  value={expenseDateFilter.dateTo}
                  onChange={(e) => setExpenseDateFilter({...expenseDateFilter, dateTo: e.target.value})}
                />
              </div>
              <button className="apply-button" onClick={fetchExpenseTransactions}>
                Apply
              </button>
            </div>

            <div className="transactions-container">
              {loading ? (
                <div className="loading">Loading...</div>
              ) : expenseTransactions.length === 0 ? (
                <div className="empty-state">No expense transactions found.</div>
              ) : (
                <div className="category-list">
                  {expenseTransactions.map((summary, index) => (
                    <div key={index} className="category-group">
                      <div className="category-group-header">
                        <h3>{summary.category.description}</h3>
                        <span className="category-total">${summary.categoryTotal?.toFixed(2) || '0.00'}</span>
                      </div>
                      <table className="transactions-table">
                        <thead>
                          <tr>
                            <th>Date</th>
                            <th>Comment</th>
                            <th>Amount</th>
                          </tr>
                        </thead>
                        <tbody>
                          {summary.transactions.map((transaction) => (
                            <tr key={transaction.idTransaction}>
                              <td>{transaction.transactionDate}</td>
                              <td>{transaction.comment || '-'}</td>
                              <td className="amount-cell">${transaction.amount.toFixed(2)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ))}
                </div>
              )}

              <div className="total-row">
                <span className="total-label">Total:</span>
                <span className="total-amount">${calculateTotal(expenseTransactions).toFixed(2)}</span>
              </div>
            </div>
          </div>
        )}

        {/* Tab 3: Balance */}
        {activeTab === 'balance' && (
          <div className="tab-content">
            <div className="tab-header">
              <h2>Balance</h2>
            </div>

            <div className="filters-row">
              <div className="filter-group">
                <label>From:</label>
                <input
                  type="date"
                  value={balanceDateFilter.dateFrom}
                  onChange={(e) => setBalanceDateFilter({...balanceDateFilter, dateFrom: e.target.value})}
                />
              </div>
              <div className="filter-group">
                <label>To:</label>
                <input
                  type="date"
                  value={balanceDateFilter.dateTo}
                  onChange={(e) => setBalanceDateFilter({...balanceDateFilter, dateTo: e.target.value})}
                />
              </div>
              <button className="apply-button" onClick={fetchBalanceTransactions}>
                Apply
              </button>
            </div>

            <div className="balance-view-toggle">
              <button
                className={balanceView === 'incomes' ? 'active' : ''}
                onClick={() => {
                  setBalanceView('incomes');
                  setTimeout(() => fetchBalanceTransactions(), 0);
                }}
              >
                Incomes
              </button>
              <button
                className={balanceView === 'expenses' ? 'active' : ''}
                onClick={() => {
                  setBalanceView('expenses');
                  setTimeout(() => fetchBalanceTransactions(), 0);
                }}
              >
                Expenses
              </button>
            </div>

            <div className="transactions-container">
              {loading ? (
                <div className="loading">Loading...</div>
              ) : (balanceView === 'incomes' ? incomeTransactions : expenseTransactions).length === 0 ? (
                <div className="empty-state">No transactions found.</div>
              ) : (
                <div className="balance-table-container">
                  {balanceView === 'incomes' ? (
                    // Incomes Table: Amount and Date columns (individual transactions)
                    <table className="balance-table">
                      <thead>
                        <tr>
                          <th>Amount</th>
                          <th>Date</th>
                        </tr>
                      </thead>
                      <tbody>
                        {incomeTransactions.flatMap(summary =>
                          summary.transactions.map((transaction) => (
                            <tr key={transaction.idTransaction}>
                              <td className="amount-cell">${transaction.amount.toFixed(2)}</td>
                              <td>{transaction.transactionDate}</td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  ) : (
                    // Expenses Table: Category and Amount columns (grouped by category)
                    <table className="balance-table">
                      <thead>
                        <tr>
                          <th>Category</th>
                          <th>Amount</th>
                        </tr>
                      </thead>
                      <tbody>
                        {expenseTransactions.map((summary, index) => (
                          <tr key={index}>
                            <td>{summary.category.description}</td>
                            <td className="amount-cell">${summary.categoryTotal?.toFixed(2) || '0.00'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              )}

              <div className="total-row">
                <span className="total-label">Total:</span>
                <span className="total-amount">
                  ${calculateTotal(balanceView === 'incomes' ? incomeTransactions : expenseTransactions).toFixed(2)}
                </span>
              </div>

              <div className="csv-download-section">
                <button className="csv-button" onClick={handleDownloadCSV}>
                  Download CSV
                </button>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Add Transaction Modal */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Add {newTransaction.transactionType === 'INCOMES' ? 'Income' : 'Expense'}</h2>
              <button className="close-button" onClick={() => setShowAddModal(false)}>×</button>
            </div>
            <form onSubmit={handleAddTransaction}>
              <div className="form-group">
                <label>Category</label>
                <select
                  value={newTransaction.categoryId}
                  onChange={(e) => setNewTransaction({...newTransaction, categoryId: e.target.value})}
                  required
                >
                  <option value="">Select a category</option>
                  {categories
                    .filter(cat => cat.type === newTransaction.transactionType)
                    .map(cat => (
                      <option key={cat.idCategory} value={cat.idCategory}>
                        {cat.description}
                      </option>
                    ))}
                </select>
              </div>
              <div className="form-group">
                <label>Amount</label>
                <input
                  type="number"
                  step="0.01"
                  value={newTransaction.amount}
                  onChange={(e) => setNewTransaction({...newTransaction, amount: e.target.value})}
                  placeholder="0.00"
                  required
                />
              </div>
              <div className="form-group">
                <label>Date</label>
                <input
                  type="date"
                  value={newTransaction.transactionDate}
                  onChange={(e) => setNewTransaction({...newTransaction, transactionDate: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>Comment</label>
                <textarea
                  value={newTransaction.comment}
                  onChange={(e) => setNewTransaction({...newTransaction, comment: e.target.value})}
                  placeholder="Add a note..."
                  rows="3"
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-button" onClick={() => setShowAddModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="submit-button">
                  Add
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;

