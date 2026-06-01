import axios from 'axios';

const api = axios.create({
baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1',  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (data) => api.post('/auth/reset-password', data),
};

export const eventsAPI = {
  list: (params) => api.get('/events', { params }),
  getById: (id) => api.get(`/events/${id}`),
  search: (q, params) => api.get('/events/search', { params: { q, ...params } }),
  create: (data) => api.post('/events', data),
  update: (id, data) => api.put(`/events/${id}`, data),
  delete: (id) => api.delete(`/events/${id}`),
  myEvents: (params) => api.get('/events/my-events', { params }),
};

export const registrationsAPI = {
  register: (eventId) => api.post(`/registrations/events/${eventId}`),
  cancel: (id) => api.delete(`/registrations/${id}`),
  myRegistrations: (params) => api.get('/registrations/my', { params }),
  getParticipants: (eventId, params) => api.get(`/registrations/events/${eventId}/participants`, { params }),
  checkIn: (token) => api.post(`/registrations/check-in?token=${token}`),
};

export default api;