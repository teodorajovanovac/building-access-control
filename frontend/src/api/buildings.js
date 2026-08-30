import apiClient from './client';

const BASE = '/api/admin/buildings';

export const getBuildings = () => apiClient.get(BASE).then((r) => r.data);

/** Bez prijave — koristi se na formi registracije (SK2), pre nego što stanar ima token. */
export const getPublicBuildings = () => apiClient.get('/api/public/buildings').then((r) => r.data);
export const getBuilding = (id) => apiClient.get(`${BASE}/${id}`).then((r) => r.data);
export const createBuilding = (data) => apiClient.post(BASE, data).then((r) => r.data);
export const updateBuilding = (id, data) => apiClient.put(`${BASE}/${id}`, data).then((r) => r.data);
export const deleteBuilding = (id) => apiClient.delete(`${BASE}/${id}`);
