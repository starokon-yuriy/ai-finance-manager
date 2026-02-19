import { render, screen, fireEvent, waitFor, within, cleanup, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';
import apiService from './services/apiService';

// Mock the apiService
jest.mock('./services/apiService');

describe('App Component', () => {
  // Mock data
  const mockCategories = [
    { idCategory: 1, description: 'Salary', type: 'INCOMES' },
    { idCategory: 2, description: 'Freelance', type: 'INCOMES' },
    { idCategory: 3, description: 'Food', type: 'EXPENSES' },
    { idCategory: 4, description: 'Transport', type: 'EXPENSES' }
  ];

  const mockIncomeTransactions = {
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
  };

  const mockExpenseTransactions = {
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
  };

  beforeEach(() => {
    // Reset all mocks before each test
    jest.clearAllMocks();

    // Setup default mock implementations
    apiService.getCategories.mockImplementation((type) => {
      if (type === 'INCOMES') {
        return Promise.resolve(mockCategories.filter(c => c.type === 'INCOMES'));
      }
      return Promise.resolve(mockCategories.filter(c => c.type === 'EXPENSES'));
    });

    apiService.getTransactions.mockImplementation((type) => {
      if (type === 'INCOMES') {
        return Promise.resolve(mockIncomeTransactions);
      }
      return Promise.resolve(mockExpenseTransactions);
    });

    apiService.createTransaction.mockResolvedValue({});
    apiService.exportTransactionsToCsv.mockResolvedValue('CSV Content');
  });

  afterEach(() => {
    // Cleanup DOM after each test
    cleanup();
    // Clear all mocks
    jest.clearAllMocks();
  });

  describe('Rendering', () => {
    test('renders Finance Manager header', async () => {
      render(<App />);
      await waitFor(() => {
        expect(screen.getByText('Finance Manager')).toBeInTheDocument();
      });
    });

    test('renders all navigation tabs', async () => {
      render(<App />);

      // Wait for API calls to complete so component is fully rendered
      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalled();
      });

      // Now check for buttons - use getAllByRole since buttons match the regex
      const buttons = screen.getAllByRole('button');
      const buttonTexts = buttons.map(b => b.textContent.toLowerCase());

      expect(buttonTexts).toContain('income');
      expect(buttonTexts).toContain('expense');
      expect(buttonTexts).toContain('balance');
    });

    test('renders Income tab by default', async () => {
      render(<App />);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalled();
      });

      expect(screen.getAllByText('Income').length).toBeGreaterThan(0); // tab and heading
      expect(screen.getByText('+ Add Income')).toBeInTheDocument();
    });
  });

  describe('Tab Navigation', () => {
    test('switches to Expense tab when clicked', async () => {
      render(<App />);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalled();
      });

      const buttons = screen.getAllByRole('button');
      const expenseTab = buttons.find(b => b.textContent.toLowerCase().includes('expense'));
      fireEvent.click(expenseTab);

      await waitFor(() => {
        expect(screen.getByText('+ Add Expense')).toBeInTheDocument();
      });
    });

    test('switches to Balance tab when clicked', async () => {
      render(<App />);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalled();
      });

      const buttons = screen.getAllByRole('button');
      const balanceTab = buttons.find(b => b.textContent.toLowerCase() === 'balance');
      fireEvent.click(balanceTab);

      await waitFor(() => {
        expect(screen.getByText('Download CSV')).toBeInTheDocument();
      });
    });

    test('applies active class to selected tab', async () => {
      render(<App />);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalled();
      });

      const buttons = screen.getAllByRole('button');
      const incomeTab = buttons.find(b => b.textContent.toLowerCase() === 'income');
      expect(incomeTab).toHaveClass('active');

      const expenseTab = buttons.find(b => b.textContent.toLowerCase() === 'expense');
      fireEvent.click(expenseTab);

      await waitFor(() => {
        expect(expenseTab).toHaveClass('active');
      });
    });
  });

  describe('Data Fetching', () => {
    test('fetches categories on mount', async () => {
      render(<App />);

      await waitFor(() => {
        expect(apiService.getCategories).toHaveBeenCalledWith('INCOMES');
        expect(apiService.getCategories).toHaveBeenCalledWith('EXPENSES');
      });
    });

    test('fetches income transactions on mount', async () => {
      render(<App />);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalledWith(
          'INCOMES',
          expect.any(String),
          expect.any(String)
        );
      });
    });

    test('fetches expense transactions on mount', async () => {
      render(<App />);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalledWith(
          'EXPENSES',
          expect.any(String),
          expect.any(String)
        );
      });
    });

    test('displays loading state while fetching', async () => {
      apiService.getTransactions.mockImplementation(() =>
        new Promise(resolve => setTimeout(() => resolve(mockIncomeTransactions), 100))
      );

      render(<App />);

      expect(screen.getByText('Loading...')).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      });
    });

    test('displays error message when no data found', async () => {
      apiService.getTransactions.mockResolvedValue({ categorySummaries: [] });

      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('No income transactions found.')).toBeInTheDocument();
      });
    });
  });

  describe('Income Tab Functionality', () => {
    test('displays income transactions', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Salary')).toBeInTheDocument();
      });

      expect(screen.getAllByText('$5000.00').length).toBeGreaterThan(0);
      expect(screen.getByText('Monthly salary')).toBeInTheDocument();
    });

    test('calculates and displays total income', async () => {
      render(<App />);

      await waitFor(() => {
        const totals = screen.getAllByText('$5000.00');
        expect(totals.length).toBeGreaterThan(0);
      });
    });

    test('applies date filter when Apply button clicked', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Salary')).toBeInTheDocument();
      });

      const dateInputs = screen.getAllByDisplayValue(/2026-\d{2}-\d{2}/);
      const applyButtons = screen.getAllByText('Apply');

      fireEvent.change(dateInputs[0], { target: { value: '2026-02-01' } });
      fireEvent.click(applyButtons[0]);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenLastCalledWith(
          'INCOMES',
          '2026-02-01',
          expect.any(String)
        );
      });
    });
  });

  describe('Expense Tab Functionality', () => {
    test('displays expense transactions', async () => {
      render(<App />);

      const expenseTab = screen.getByRole('button', { name: /^expense$/i });
      fireEvent.click(expenseTab);

      await waitFor(() => {
        expect(screen.getByText('Food')).toBeInTheDocument();
        expect(screen.getByText('Groceries')).toBeInTheDocument();
        expect(screen.getByText('Restaurant')).toBeInTheDocument();
      });
    });

    test('calculates and displays total expenses', async () => {
      render(<App />);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalled();
      });

      const buttons = screen.getAllByRole('button');
      const expenseTab = buttons.find(b => b.textContent.toLowerCase() === 'expense');
      fireEvent.click(expenseTab);

      await waitFor(() => {
        expect(screen.getAllByText('$300.00').length).toBeGreaterThan(0);
      });
    });
  });

  describe('Add Transaction Modal', () => {
    test('opens modal when Add Income button clicked', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('+ Add Income')).toBeInTheDocument();
      });

      const addButton = screen.getByText('+ Add Income');
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(screen.getByRole('combobox')).toBeInTheDocument(); // select element
      });

      // Check that form elements are present
      expect(screen.getByRole('combobox')).toBeInTheDocument();
      expect(screen.getByPlaceholderText('0.00')).toBeInTheDocument(); // amount input
      expect(screen.getByPlaceholderText('Add a note...')).toBeInTheDocument(); // textarea
      expect(screen.getByText(/^Add$/)).toBeInTheDocument(); // submit button
      expect(screen.getByText('Cancel')).toBeInTheDocument();
    });

    test('closes modal when close button clicked', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('+ Add Income')).toBeInTheDocument();
      });

      const addButton = screen.getByText('+ Add Income');
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(screen.getByText('Add Income')).toBeInTheDocument();
      });

      const closeButton = screen.getByText('×');
      fireEvent.click(closeButton);

      await waitFor(() => {
        expect(screen.queryByText('Add Income')).not.toBeInTheDocument();
      });
    });

    test('closes modal when Cancel button clicked', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('+ Add Income')).toBeInTheDocument();
      });

      const addButton = screen.getByText('+ Add Income');
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(screen.getByText('Add Income')).toBeInTheDocument();
      });

      const cancelButton = screen.getByText('Cancel');
      fireEvent.click(cancelButton);

      await waitFor(() => {
        expect(screen.queryByText('Add Income')).not.toBeInTheDocument();
      });
    });

    test('submits new income transaction', async () => {
      const user = userEvent.setup();
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('+ Add Income')).toBeInTheDocument();
      });

      const addButton = screen.getByText('+ Add Income');
      await user.click(addButton);

      await waitFor(() => {
        expect(screen.getByRole('combobox')).toBeInTheDocument();
      });

      // Fill in the form
      const categorySelect = screen.getByRole('combobox');
      const amountInput = screen.getByPlaceholderText('0.00');
      const commentInput = screen.getByPlaceholderText('Add a note...');

      await user.selectOptions(categorySelect, '1');
      await user.type(amountInput, '1000');
      await user.type(commentInput, 'Test income');

      const submitButton = screen.getByRole('button', { name: /^add$/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(apiService.createTransaction).toHaveBeenCalledWith({
          amount: 1000,
          transactionDate: expect.any(String),
          categoryId: 1,
          comment: 'Test income'
        });
      });

      // Wait for modal to close
      await waitFor(() => {
        expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
      });
    });

    test('displays correct categories for income transaction', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('+ Add Income')).toBeInTheDocument();
      });

      const addButton = screen.getByText('+ Add Income');
      fireEvent.click(addButton);

      await waitFor(() => {
        const categorySelect = screen.getByRole('combobox');
        expect(categorySelect).toBeInTheDocument();
      });

      const categorySelect = screen.getByRole('combobox');

      // Check that only income categories are shown
      const options = within(categorySelect).getAllByRole('option');
      const categoryOptions = options.filter(opt => opt.value !== '');

      expect(categoryOptions).toHaveLength(2); // Salary and Freelance
    });
  });

  describe('Balance Tab Functionality', () => {
    test('displays balance view toggle buttons', async () => {
      render(<App />);

      const balanceTab = screen.getByRole('button', { name: /^balance$/i });
      fireEvent.click(balanceTab);

      await waitFor(() => {
        expect(screen.getByText('Incomes')).toBeInTheDocument();
        expect(screen.getByText('Expenses')).toBeInTheDocument();
      });
    });

    test('toggles between incomes and expenses view', async () => {
      render(<App />);

      const balanceTab = screen.getByRole('button', { name: /^balance$/i });
      fireEvent.click(balanceTab);

      await waitFor(() => {
        expect(screen.getByText('Incomes')).toBeInTheDocument();
      });

      // Find the Expenses toggle button within the balance view
      const expensesToggle = screen.getAllByText('Expenses').find(
        el => el.tagName === 'BUTTON'
      );
      fireEvent.click(expensesToggle);

      await waitFor(() => {
        expect(apiService.getTransactions).toHaveBeenCalledWith(
          'EXPENSES',
          expect.any(String),
          expect.any(String)
        );
      });
    });

    test('downloads CSV when Download CSV button clicked', async () => {
      render(<App />);

      const balanceTab = screen.getByRole('button', { name: /^balance$/i });
      fireEvent.click(balanceTab);

      await waitFor(() => {
        expect(screen.getByText('Download CSV')).toBeInTheDocument();
      });

      // Mock URL.createObjectURL and related methods
      const mockCreateObjectURL = jest.fn(() => 'blob:mock-url');
      const mockRevokeObjectURL = jest.fn();
      const originalCreateObjectURL = global.URL.createObjectURL;
      const originalRevokeObjectURL = global.URL.revokeObjectURL;

      global.URL.createObjectURL = mockCreateObjectURL;
      global.URL.revokeObjectURL = mockRevokeObjectURL;

      const mockLink = {
        click: jest.fn(),
        href: '',
        download: '',
        setAttribute: jest.fn()
      };
      const originalCreateElement = document.createElement;
      const createElementSpy = jest.spyOn(document, 'createElement').mockImplementation((tag) => {
        if (tag === 'a') {
          return mockLink;
        }
        return originalCreateElement.call(document, tag);
      });

      const appendChildSpy = jest.spyOn(document.body, 'appendChild').mockImplementation(() => {});
      const removeChildSpy = jest.spyOn(document.body, 'removeChild').mockImplementation(() => {});

      const downloadButton = screen.getByText('Download CSV');
      fireEvent.click(downloadButton);

      await waitFor(() => {
        expect(apiService.exportTransactionsToCsv).toHaveBeenCalled();
        expect(mockLink.click).toHaveBeenCalled();
      });

      // Restore mocks
      global.URL.createObjectURL = originalCreateObjectURL;
      global.URL.revokeObjectURL = originalRevokeObjectURL;
      createElementSpy.mockRestore();
      appendChildSpy.mockRestore();
      removeChildSpy.mockRestore();
    });
  });

  describe('Error Handling', () => {
    test('handles API error when fetching transactions', async () => {
      const consoleError = jest.spyOn(console, 'error').mockImplementation(() => {});
      apiService.getTransactions.mockRejectedValue(new Error('API Error'));

      render(<App />);

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalled();
      });

      consoleError.mockRestore();
    });

    test('shows alert when transaction creation fails', async () => {
      const alertMock = jest.spyOn(window, 'alert').mockImplementation(() => {});
      apiService.createTransaction.mockRejectedValue(new Error('Failed to create'));

      const user = userEvent.setup();
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('+ Add Income')).toBeInTheDocument();
      });

      const addButton = screen.getByText('+ Add Income');
      await user.click(addButton);

      await waitFor(() => {
        expect(screen.getByRole('combobox')).toBeInTheDocument();
      });

      // Fill and submit form
      const categorySelect = screen.getByRole('combobox');
      const amountInput = screen.getByPlaceholderText('0.00');

      await user.selectOptions(categorySelect, '1');
      await user.type(amountInput, '1000');

      const submitButton = screen.getByRole('button', { name: /^add$/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(alertMock).toHaveBeenCalledWith('Failed to add transaction. Please try again.');
      });

      alertMock.mockRestore();
    });
  });
});

