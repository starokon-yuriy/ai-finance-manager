import axios from 'axios';

// Base API URL - uses proxy configuration from package.json
const API_BASE_URL = '/api/v1/finance';

/**
 * API Service for communicating with Spring Boot backend
 */
const apiService = {
  /**
   * Test backend connection
   */
  checkConnection: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/categories`, {
        params: { type: 'EXPENSES' }
      });
      return { success: true, data: response.data };
    } catch (error) {
      return { success: false, error: error.message };
    }
  },

  /**
   * Get all transactions by type and date range
   */
  getTransactions: async (type, dateFrom, dateTo) => {
    const response = await axios.get(`${API_BASE_URL}/transactions`, {
      params: { type, dateFrom, dateTo }
    });
    return response.data;
  },

  /**
   * Get all categories by type
   */
  getCategories: async (type) => {
    const response = await axios.get(`${API_BASE_URL}/categories`, {
      params: { type }
    });
    return response.data;
  },

  /**
   * Create a new transaction
   */
  createTransaction: async (transaction) => {
    const response = await axios.post(`${API_BASE_URL}/transactions`, transaction);
    return response.data;
  },

  /**
   * Export transactions to CSV
   */
  exportTransactionsToCsv: async (dateFrom, dateTo) => {
    const response = await axios.get(`${API_BASE_URL}/transactions/export`, {
      params: { dateFrom, dateTo }
    });
    return response.data;
  },
};

export default apiService;

