import apiClient from './client';

const BASE = '/api/admin/users';

export const getUsers = () => apiClient.get(BASE).then((r) => r.data);
export const getUser = (id) => apiClient.get(`${BASE}/${id}`).then((r) => r.data);
export const createUser = (data) => apiClient.post(BASE, data).then((r) => r.data);
export const updateUser = (id, data) => apiClient.put(`${BASE}/${id}`, data).then((r) => r.data);
export const deleteUser = (id) => apiClient.delete(`${BASE}/${id}`);

export const getMyProfile = () => apiClient.get('/api/resident/me').then((r) => r.data);
