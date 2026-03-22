import axiosInstance from './axiosInstance';

export const userApi = {
  getCurrentUser: async () => {
    const response = await axiosInstance.get('/users/me');
    return response.data;
  },

  updateProfile: async (data) => {
    const response = await axiosInstance.put('/users/update-profile', data);
    return response.data;
  },

  changePassword: async (currentPassword, newPassword) => {
    const response = await axiosInstance.post('/users/change-password', {
      currentPassword,
      newPassword,
    });
    return response.data;
  },

  getAllUsers: async () => {
    const response = await axiosInstance.get('/users/all');
    return response.data;
  },

  // Alias for getAllUsers for compatibility
  getUsers: async () => {
    const response = await axiosInstance.get('/users/all');
    return response.data;
  },

  getPendingUsers: async () => {
    const response = await axiosInstance.get('/users/pending');
    return response.data;
  },

  activateUser: async (userId, role) => {
    const response = await axiosInstance.post(`/users/${userId}/activate?role=${role}`);
    return response.data;
  },

  updateUserRole: async (userId, role) => {
    const response = await axiosInstance.put(`/users/${userId}/role`, { role });
    return response.data;
  },

  toggleUserStatus: async (userId) => {
    const response = await axiosInstance.put(`/users/${userId}/toggle-status`);
    return response.data;
  },

  getApprovers: async () => {
    const response = await axiosInstance.get('/users/approvers');
    return response.data;
  },
};

export default userApi;
