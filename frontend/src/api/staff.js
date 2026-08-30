import apiClient from './client';

const BASE = '/api/admin/staff';

export const getStaffByBuilding = (buildingId) =>
  apiClient.get(BASE, { params: { buildingId } }).then((r) => r.data);
export const getStaff = (id) => apiClient.get(`${BASE}/${id}`).then((r) => r.data);
export const createStaff = (data) => apiClient.post(BASE, data).then((r) => r.data);
export const updateStaff = (id, data) => apiClient.put(`${BASE}/${id}`, data).then((r) => r.data);
export const deleteStaff = (id) => apiClient.delete(`${BASE}/${id}`);
