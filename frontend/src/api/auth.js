import apiClient from './client';

export function login(email, password) {
  return apiClient.post('/api/auth/login', { email, password }).then((r) => r.data);
}

export function register({ firstName, lastName, email, password, apartmentId }) {
  return apiClient
    .post('/api/auth/register', { firstName, lastName, email, password, apartmentId })
    .then((r) => r.data);
}
