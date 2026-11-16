import { request } from './client';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types';

export const authApi = {
  register: async (payload: RegisterRequest): Promise<AuthResponse> => {
    const res = await request<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return res.data;
  },

  login: async (payload: LoginRequest): Promise<AuthResponse> => {
    const res = await request<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return res.data;
  },
};
