import axiosInstance from './axiosInstance';

export const authApi = {
  login: async (loginId, password) => {
    const response = await axiosInstance.post('/auth/login', {
      loginId,
      password
    });
    return response.data;
  },

  signup: async (data) => {
    const response = await axiosInstance.post('/auth/signup', {
      loginId: data.loginId,
      email: data.email,
      password: data.password,
      confirmPassword: data.confirmPassword || data.password,
      firstName: data.firstName || '',
      lastName: data.lastName || '',
    });
    return response.data;
  },

  verifyEmail: async (token) => {
    const response = await axiosInstance.get(`/auth/verify?token=${token}`);
    return response.data;
  },

  resendVerification: async (email) => {
    const response = await axiosInstance.post('/auth/resend-verification', { email });
    return response.data;
  },

  forgotPassword: async (email) => {
    const response = await axiosInstance.post('/auth/forgot-password', { email });
    return response.data;
  },

  resetPassword: async (token, newPassword) => {
    const response = await axiosInstance.post('/auth/reset-password', {
      token,
      newPassword
    });
    return response.data;
  },

  checkLoginIdAvailability: async (loginId) => {
    const response = await axiosInstance.get(`/auth/check-login-id?loginId=${loginId}`);
    return response.data;
  },

  checkEmailAvailability: async (email) => {
    const response = await axiosInstance.get(`/auth/check-email?email=${email}`);
    return response.data;
  },

  changePassword: async (currentPassword, newPassword, confirmNewPassword) => {
    const response = await axiosInstance.post('/auth/change-password', {
      currentPassword,
      newPassword,
      confirmNewPassword
    });
    return response.data;
  },

  updateProfile: async (data) => {
    const response = await axiosInstance.put('/auth/profile', data);
    return response.data;
  },
};

export default authApi;
