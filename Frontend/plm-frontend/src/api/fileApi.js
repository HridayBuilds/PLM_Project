import axiosInstance from './axiosInstance';

export const fileApi = {
  // Upload a file
  uploadFile: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await axiosInstance.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  // Get file info
  getFileInfo: async (id) => {
    const response = await axiosInstance.get(`/files/${id}`);
    return response.data;
  },

  // Delete a file
  deleteFile: async (id) => {
    const response = await axiosInstance.delete(`/files/${id}`);
    return response.data;
  },

  // Get my files
  getMyFiles: async (params) => {
    const response = await axiosInstance.get('/files/my', { params });
    return response.data;
  }
};
