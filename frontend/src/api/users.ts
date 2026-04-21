import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from './client';
import type { UserWithProfile } from '../types';

interface UpdateProfileRequest {
  name?: string;
  deliveryAddress?: string;
  latitude?: number;
  longitude?: number;
  dietaryPreferences?: string[];
}

export function useProfile() {
  return useQuery({
    queryKey: ['profile'],
    queryFn: () =>
      apiClient.get<UserWithProfile>('/api/users/me').then((r) => r.data),
  });
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateProfileRequest) =>
      apiClient.put<UserWithProfile>('/api/users/me', data).then((r) => r.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['profile'] }),
  });
}
