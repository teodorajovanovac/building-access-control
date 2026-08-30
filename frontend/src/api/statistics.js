import apiClient from './client';

export const getStatistics = (buildingId, from, to) =>
  apiClient.get('/api/statistics', { params: { buildingId, from, to } }).then((r) => r.data);
