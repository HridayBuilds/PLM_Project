import axios from 'axios';
import toast from 'react-hot-toast';
import useAuthStore from '../context/authStore';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request interceptor - add auth token
axiosInstance.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle errors
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const { response } = error;

    if (response) {
      const { status, data } = response;

      if (status === 401) {
        // Unauthorized - logout and redirect
        useAuthStore.getState().logout();
        window.location.href = '/login';
        toast.error('Session expired. Please login again.');
      } else if (status === 403) {
        toast.error('You do not have permission to perform this action.');
      } else if (status >= 400 && status < 500) {
        // Client errors - show message from API
        const message = data?.message || data?.error || 'An error occurred';
        toast.error(message);
      } else if (status >= 500) {
        // Server errors
        toast.error('Something went wrong. Please try again.');
      }
    } else {
      // Network error
      toast.error('Network error. Please check your connection.');
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
