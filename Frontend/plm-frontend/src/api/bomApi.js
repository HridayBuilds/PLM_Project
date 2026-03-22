import axiosInstance from './axiosInstance';

export const bomApi = {
  getBoms: async (params = {}) => {
    const response = await axiosInstance.get('/boms', { params });
    return response.data;
  },

  getActiveBoms: async () => {
    const response = await axiosInstance.get('/boms/active');
    return response.data;
  },

  getBomById: async (id) => {
    const response = await axiosInstance.get(`/boms/${id}`);
    return response.data;
  },

  getBomsByProduct: async (productId) => {
    const response = await axiosInstance.get(`/boms/product/${productId}`);
    return response.data;
  },

  searchBoms: async (searchParams) => {
    const response = await axiosInstance.post('/boms/search', searchParams);
    return response.data;
  },

  createBom: async (data) => {
    const response = await axiosInstance.post('/boms', data);
    return response.data;
  },

  updateBom: async (id, data) => {
    const response = await axiosInstance.put(`/boms/${id}`, data);
    return response.data;
  },

  activateBom: async (id) => {
    const response = await axiosInstance.post(`/boms/${id}/activate`);
    return response.data;
  },
};

export default bomApi;
