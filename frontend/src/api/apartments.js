import apiClient from './client';

const BASE = '/api/admin/apartments';

export const getApartmentsByBuilding = (buildingId) =>
  apiClient.get(BASE, { params: { buildingId } }).then((r) => r.data);

/** Bez prijave — koristi se na formi registracije (SK2), pre nego što stanar ima token. */
export const getPublicApartments = (buildingId) =>
  apiClient.get(`/api/public/buildings/${buildingId}/apartments`).then((r) => r.data);
export const getApartment = (id) => apiClient.get(`${BASE}/${id}`).then((r) => r.data);
export const createApartment = (data) => apiClient.post(BASE, data).then((r) => r.data);
export const updateApartment = (id, data) => apiClient.put(`${BASE}/${id}`, data).then((r) => r.data);
export const deleteApartment = (id) => apiClient.delete(`${BASE}/${id}`);
