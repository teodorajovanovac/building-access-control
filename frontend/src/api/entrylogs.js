import apiClient from './client';

export const getTodayEntryLogs = (buildingId) =>
  apiClient.get('/api/query/entrylogs/today', { params: { buildingId } }).then((r) => r.data);

export const getCurrentlyPresent = (buildingId) =>
  apiClient.get('/api/query/entrylogs/present', { params: { buildingId } }).then((r) => r.data);

export const searchEntryLogs = (params) =>
  apiClient.get('/api/query/entrylogs/search', { params }).then((r) => r.data);

export const getMyApartmentEntryLogs = () =>
  apiClient.get('/api/resident/entrylogs').then((r) => r.data);

export const exportEntryLogs = (params) =>
  apiClient.get('/api/query/entrylogs/export', { params, responseType: 'blob' }).then((r) => r.data);
