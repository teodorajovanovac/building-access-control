import apiClient from './client';

export const getTodayDenials = (buildingId) =>
  apiClient.get('/api/query/denials/today', { params: { buildingId } }).then((r) => r.data);

export const searchDenials = (params) =>
  apiClient.get('/api/query/denials/search', { params }).then((r) => r.data);

export const exportDenials = (params) =>
  apiClient.get('/api/query/denials/export', { params, responseType: 'blob' }).then((r) => r.data);
