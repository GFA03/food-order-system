import { useMutation } from '@tanstack/react-query';
import apiClient from './client';
import type { AuthResponse } from '../types';

interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

interface RegisterResponse {
  id: string;
  email: string;
  name: string;
}

export function useLogin() {
  return useMutation({
    mutationFn: (data: LoginRequest) =>
      apiClient.post<AuthResponse>('/api/auth/login', data).then((r) => r.data),
  });
}

export function useRegister() {
  return useMutation({
    mutationFn: (data: RegisterRequest) =>
      apiClient.post<RegisterResponse>('/api/auth/register', data).then((r) => r.data),
  });
}
