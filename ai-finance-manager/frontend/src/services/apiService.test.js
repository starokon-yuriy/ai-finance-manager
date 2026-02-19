import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import apiService from './apiService';

describe('API Service', () => {
  let mock;

  beforeEach(() => {
    // Create a new instance of axios-mock-adapter
    mock = new MockAdapter(axios);
  });

  afterEach(() => {
    // Reset the mock adapter after each test
    mock.reset();
  });

  afterAll(() => {
    // Restore the original axios adapter
    mock.restore();
  });

  describe('checkConnection', () => {
    test('returns success when connection is successful', async () => {
      const mockData = [
        { idCategory: 1, description: 'Food', type: 'EXPENSES' }
      ];

      mock.onGet('/api/v1/finance/categories').reply(200, mockData);

      const result = await apiService.checkConnection();

      expect(result.success).toBe(true);
      expect(result.data).toEqual(mockData);
    });

    test('returns error when connection fails', async () => {
      mock.onGet('/api/v1/finance/categories').networkError();

      const result = await apiService.checkConnection();

      expect(result.success).toBe(false);
      expect(result.error).toBeDefined();
    });
  });

  describe('getTransactions', () => {
    test('fetches income transactions with correct parameters', async () => {
      const mockData = {
        categorySummaries: [
          {
            category: { idCategory: 1, description: 'Salary', type: 'INCOMES' },
            categoryTotal: 5000,
            transactions: []
          }
        ]
      };

      mock.onGet('/api/v1/finance/transactions', {
        params: { type: 'INCOMES', dateFrom: '2026-02-01', dateTo: '2026-02-28' }
      }).reply(200, mockData);

      const result = await apiService.getTransactions('INCOMES', '2026-02-01', '2026-02-28');

      expect(result).toEqual(mockData);
      expect(mock.history.get.length).toBe(1);
      expect(mock.history.get[0].params).toEqual({
        type: 'INCOMES',
        dateFrom: '2026-02-01',
        dateTo: '2026-02-28'
      });
    });

    test('fetches expense transactions with correct parameters', async () => {
      const mockData = {
        categorySummaries: [
          {
            category: { idCategory: 3, description: 'Food', type: 'EXPENSES' },
            categoryTotal: 300,
            transactions: []
          }
        ]
      };

      mock.onGet('/api/v1/finance/transactions').reply(200, mockData);

      const result = await apiService.getTransactions('EXPENSES', '2026-02-01', '2026-02-28');

      expect(result).toEqual(mockData);
    });

    test('throws error when request fails', async () => {
      mock.onGet('/api/v1/finance/transactions').reply(500, {
        message: 'Internal Server Error'
      });

      await expect(
        apiService.getTransactions('INCOMES', '2026-02-01', '2026-02-28')
      ).rejects.toThrow();
    });
  });

  describe('getCategories', () => {
    test('fetches income categories', async () => {
      const mockData = [
        { idCategory: 1, description: 'Salary', type: 'INCOMES' },
        { idCategory: 2, description: 'Freelance', type: 'INCOMES' }
      ];

      mock.onGet('/api/v1/finance/categories', {
        params: { type: 'INCOMES' }
      }).reply(200, mockData);

      const result = await apiService.getCategories('INCOMES');

      expect(result).toEqual(mockData);
      expect(mock.history.get.length).toBe(1);
      expect(mock.history.get[0].params).toEqual({ type: 'INCOMES' });
    });

    test('fetches expense categories', async () => {
      const mockData = [
        { idCategory: 3, description: 'Food', type: 'EXPENSES' },
        { idCategory: 4, description: 'Transport', type: 'EXPENSES' }
      ];

      mock.onGet('/api/v1/finance/categories').reply(200, mockData);

      const result = await apiService.getCategories('EXPENSES');

      expect(result).toEqual(mockData);
    });

    test('throws error when request fails', async () => {
      mock.onGet('/api/v1/finance/categories').reply(404);

      await expect(
        apiService.getCategories('INCOMES')
      ).rejects.toThrow();
    });
  });

  describe('createTransaction', () => {
    test('creates a new transaction successfully', async () => {
      const newTransaction = {
        amount: 1000,
        transactionDate: '2026-02-15',
        categoryId: 1,
        comment: 'Test transaction'
      };

      const mockResponse = {
        idTransaction: 123,
        ...newTransaction
      };

      mock.onPost('/api/v1/finance/transactions').reply(201, mockResponse);

      const result = await apiService.createTransaction(newTransaction);

      expect(result).toEqual(mockResponse);
      expect(mock.history.post.length).toBe(1);
      expect(JSON.parse(mock.history.post[0].data)).toEqual(newTransaction);
    });

    test('throws error when creation fails with validation error', async () => {
      const newTransaction = {
        amount: -100, // Invalid amount
        transactionDate: '2026-02-15',
        categoryId: 1,
        comment: 'Test transaction'
      };

      mock.onPost('/api/v1/finance/transactions').reply(400, {
        message: 'Validation error'
      });

      await expect(
        apiService.createTransaction(newTransaction)
      ).rejects.toThrow();
    });

    test('sends correct request body', async () => {
      const newTransaction = {
        amount: 500.50,
        transactionDate: '2026-02-15',
        categoryId: 2,
        comment: 'Partial payment'
      };

      mock.onPost('/api/v1/finance/transactions').reply(201, { ...newTransaction, idTransaction: 456 });

      await apiService.createTransaction(newTransaction);

      expect(mock.history.post.length).toBe(1);
      const sentData = JSON.parse(mock.history.post[0].data);
      expect(sentData.amount).toBe(500.50);
      expect(sentData.categoryId).toBe(2);
    });
  });

  describe('exportTransactionsToCsv', () => {
    test('exports transactions to CSV successfully', async () => {
      const mockCsvData = 'Date,Category,Amount,Comment\n2026-02-15,Salary,5000,Monthly salary';

      mock.onGet('/api/v1/finance/transactions/export', {
        params: { dateFrom: '2026-02-01', dateTo: '2026-02-28' }
      }).reply(200, mockCsvData);

      const result = await apiService.exportTransactionsToCsv('2026-02-01', '2026-02-28');

      expect(result).toBe(mockCsvData);
      expect(mock.history.get.length).toBe(1);
      expect(mock.history.get[0].params).toEqual({
        dateFrom: '2026-02-01',
        dateTo: '2026-02-28'
      });
    });

    test('throws error when export fails', async () => {
      mock.onGet('/api/v1/finance/transactions/export').reply(500);

      await expect(
        apiService.exportTransactionsToCsv('2026-02-01', '2026-02-28')
      ).rejects.toThrow();
    });

    test('handles empty date range', async () => {
      const mockCsvData = 'Date,Category,Amount,Comment';

      mock.onGet('/api/v1/finance/transactions/export').reply(200, mockCsvData);

      const result = await apiService.exportTransactionsToCsv('2026-02-01', '2026-02-01');

      expect(result).toBe(mockCsvData);
    });
  });

  describe('Error Handling', () => {
    test('handles network errors', async () => {
      mock.onGet('/api/v1/finance/transactions').networkError();

      await expect(
        apiService.getTransactions('INCOMES', '2026-02-01', '2026-02-28')
      ).rejects.toThrow();
    });

    test('handles timeout errors', async () => {
      mock.onGet('/api/v1/finance/transactions').timeout();

      await expect(
        apiService.getTransactions('INCOMES', '2026-02-01', '2026-02-28')
      ).rejects.toThrow();
    });

    test('handles 401 unauthorized errors', async () => {
      mock.onGet('/api/v1/finance/categories').reply(401, {
        message: 'Unauthorized'
      });

      await expect(
        apiService.getCategories('INCOMES')
      ).rejects.toThrow();
    });

    test('handles 404 not found errors', async () => {
      mock.onGet('/api/v1/finance/transactions').reply(404, {
        message: 'Not found'
      });

      await expect(
        apiService.getTransactions('INCOMES', '2026-02-01', '2026-02-28')
      ).rejects.toThrow();
    });
  });
});

