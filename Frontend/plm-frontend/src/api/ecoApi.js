import axiosInstance from './axiosInstance';

export const ecoApi = {
  getAllEcos: async (params = {}) => {
    const response = await axiosInstance.get('/ecos', { params });
    return response.data;
  },

  getEcos: async (params = {}) => {
    const response = await axiosInstance.get('/ecos/my', { params });
    return response.data;
  },

  getEcoById: async (id) => {
    const response = await axiosInstance.get(`/ecos/${id}`);
    return response.data;
  },

  getMyEcos: async (params = {}) => {
    const response = await axiosInstance.get('/ecos/my', { params });
    return response.data;
  },

  getPendingApprovals: async (params = {}) => {
    const response = await axiosInstance.get('/ecos/pending-approvals', { params });
    return response.data;
  },

  searchEcos: async (searchParams) => {
    const response = await axiosInstance.post('/ecos/search', searchParams);
    return response.data;
  },

  createEco: async (data) => {
    const response = await axiosInstance.post('/ecos', data);
    return response.data;
  },

  updateEco: async (id, data) => {
    const response = await axiosInstance.put(`/ecos/${id}`, data);
    return response.data;
  },

  submitEco: async (id) => {
    const response = await axiosInstance.post(`/ecos/${id}/submit`);
    return response.data;
  },

  approveEco: async (id, comments) => {
    const response = await axiosInstance.post(`/ecos/${id}/approve`, {
      decision: 'APPROVED',
      comments: comments || '',
    });
    return response.data;
  },

  rejectEco: async (id, comments) => {
    const response = await axiosInstance.post(`/ecos/${id}/approve`, {
      decision: 'REJECTED',
      comments: comments || '',
    });
    return response.data;
  },

  addBomChange: async (id, changeData) => {
    const response = await axiosInstance.post(`/ecos/${id}/bom-changes`, changeData);
    return response.data;
  },

  addProductChange: async (id, changeData) => {
    const response = await axiosInstance.post(`/ecos/${id}/product-changes`, changeData);
    return response.data;
  },

  removeChange: async (ecoId, changeId) => {
    const response = await axiosInstance.delete(`/ecos/${ecoId}/changes/${changeId}`);
    return response.data;
  },

  addBomOperationChange: async (id, changeData) => {
    const response = await axiosInstance.post(`/ecos/${id}/bom-operation-changes`, changeData);
    return response.data;
  },

  removeBomOperationChange: async (ecoId, changeId) => {
    const response = await axiosInstance.delete(`/ecos/${ecoId}/bom-operation-changes/${changeId}`);
    return response.data;
  },

  addAttachment: async (id, file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await axiosInstance.post(`/ecos/${id}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  removeAttachment: async (ecoId, attachmentId) => {
    const response = await axiosInstance.delete(`/ecos/${ecoId}/attachments/${attachmentId}`);
    return response.data;
  },

  getComparison: async (id) => {
    const response = await axiosInstance.get(`/ecos/${id}/comparison`);
    return response.data;
  },

  getStages: async (productId) => {
    const params = productId ? { productId } : {};
    const response = await axiosInstance.get('/ecos/stages', { params });
    return response.data;
  },
};

export default ecoApi;
