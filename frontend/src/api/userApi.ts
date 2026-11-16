import { request } from './client';
import { User, UpdateProfileRequest } from '../types';

export const userApi = {
  getProfile: async (): Promise<User> => {
    const res = await request<User>('/api/users/me', {
      method: 'GET',
    });
    return res.data;
  },

  updateProfile: async (payload: UpdateProfileRequest): Promise<User> => {
    const res = await request<User>('/api/users/me', {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
    return res.data;
  },
};
