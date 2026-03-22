import axiosInstance from './axiosInstance';

export const productApi = {
  getProducts: async (params = {}) => {
    const response = await axiosInstance.get('/products', { params });
    return response.data;
  },

  getActiveProducts: async () => {
    const response = await axiosInstance.get('/products/active');
    return response.data;
  },

  getProductById: async (id) => {
    const response = await axiosInstance.get(`/products/${id}`);
    return response.data;
  },

  searchProducts: async (searchParams) => {
    const response = await axiosInstance.post('/products/search', searchParams);
    return response.data;
  },

  createProduct: async (data) => {
    const response = await axiosInstance.post('/products', data);
    return response.data;
  },

  activateProduct: async (id) => {
    const response = await axiosInstance.post(`/products/${id}/activate`);
    return response.data;
  },

  getVersionHistory: async (productName) => {
    const response = await axiosInstance.get(`/products/history/${productName}`);
    return response.data;
  },

  // Get product versions by product ID (uses report endpoint)
  getProductVersions: async (productId) => {
    const response = await axiosInstance.get(`/reports/products/${productId}/versions`);
    return response.data;
  },

  // Get ECOs related to a product
  getProductEcos: async (productId) => {
    const response = await axiosInstance.post('/ecos/search', { productId });
    return response.data;
  },
};

export default productApi;
