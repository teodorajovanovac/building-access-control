import axios from 'axios';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('bac_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const isLoginCall = error.config && error.config.url && error.config.url.includes('/api/auth/login');
      if (!isLoginCall) {
        localStorage.removeItem('bac_token');
        localStorage.removeItem('bac_user');
        if (window.location.pathname !== '/login') {
          window.location.assign('/login');
        }
      }
    }
    return Promise.reject(error);
  },
);

/** Izvlači čitljivu poruku greške iz backend-ovog ApiError oblika (vidi GlobalExceptionHandler). */
export function extractErrorMessage(error) {
  if (error?.response?.data) {
    const data = error.response.data;
    if (data.details && data.details.length > 0) {
      return `${data.message}: ${data.details.join('; ')}`;
    }
    if (data.message) return data.message;
  }
  if (error?.message) return error.message;
  return 'Došlo je do neočekivane greške.';
}

export default apiClient;
