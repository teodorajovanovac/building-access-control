import apiClient from './client';

const RESIDENT_BASE = '/api/resident/gatepasses';
const ADMIN_BASE = '/api/admin/gatepasses';

export const getMyGatePasses = (page = 0, size = 10, sort) =>
  apiClient.get(RESIDENT_BASE, { params: { page, size, sort } }).then((r) => r.data);

export const getMyGatePass = (id) => apiClient.get(`${RESIDENT_BASE}/${id}`).then((r) => r.data);

export const getGatePassHistory = (id) =>
  apiClient.get(`${RESIDENT_BASE}/${id}/history`).then((r) => r.data);

export const createGatePass = (data) => apiClient.post(RESIDENT_BASE, data).then((r) => r.data);

export const updateGatePass = (id, data) =>
  apiClient.put(`${RESIDENT_BASE}/${id}`, data).then((r) => r.data);

export const cancelGatePass = (id) => apiClient.delete(`${RESIDENT_BASE}/${id}`);

export const searchGatePasses = (params) =>
  apiClient.get(`${ADMIN_BASE}/search`, { params }).then((r) => r.data);

export const getPublicGatePass = (code) =>
  apiClient.get(`/api/public/gatepass/${encodeURIComponent(code)}`).then((r) => r.data);
