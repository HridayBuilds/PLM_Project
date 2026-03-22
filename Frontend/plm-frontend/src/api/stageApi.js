import axiosInstance from './axiosInstance';

export const stageApi = {
  getStages: async (productId) => {
    const params = productId ? { productId } : {};
    const response = await axiosInstance.get('/ecos/stages', { params });
    return response.data;
  },

  createStage: async (data) => {
    const response = await axiosInstance.post('/admin/stages', data);
    return response.data;
  },

  updateStage: async (id, data) => {
    const response = await axiosInstance.put(`/admin/stages/${id}`, data);
    return response.data;
  },

  deleteStage: async (id) => {
    const response = await axiosInstance.delete(`/admin/stages/${id}`);
    return response.data;
  },

  reorderStages: async (stageIds) => {
    const response = await axiosInstance.post('/admin/stages/reorder', stageIds);
    return response.data;
  },

  getApprovalRules: async () => {
    const response = await axiosInstance.get('/admin/approval-rules');
    return response.data;
  },

  getApprovalRulesByStage: async (stageId) => {
    const response = await axiosInstance.get(`/admin/approval-rules/stage/${stageId}`);
    return response.data;
  },

  createApprovalRule: async (data) => {
    const response = await axiosInstance.post('/admin/approval-rules', data);
    return response.data;
  },

  deleteApprovalRule: async (id) => {
    const response = await axiosInstance.delete(`/admin/approval-rules/${id}`);
    return response.data;
  },
};

export default stageApi;
