import apiClient from './client';

export const getSecurityMe = () => apiClient.get('/api/security/me').then((r) => r.data);

export const scanCode = (code) => apiClient.post('/api/security/access/scan', { code }).then((r) => r.data);

export const searchPeople = (query) =>
  apiClient.get('/api/security/people/search', { params: { query } }).then((r) => r.data);

export const processManual = (type, id) =>
  apiClient.post(`/api/security/access/manual/${type}/${id}`).then((r) => r.data);

export const denyManually = (data) =>
  apiClient.post('/api/security/access/deny', data).then((r) => r.data);
