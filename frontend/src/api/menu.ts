import { useQuery } from '@tanstack/react-query';
import apiClient from './client';
import type { MenuItem, PaginatedResponse } from '../types';

interface MenuParams {
  page: number;
  size: number;
  sort: string;
}

export function useMenuItems(restaurantId: string, params: MenuParams) {
  return useQuery({
    queryKey: ['menu', restaurantId, params],
    queryFn: () =>
      apiClient
        .get<PaginatedResponse<MenuItem>>(`/api/restaurants/${restaurantId}/menu`, { params })
        .then((r) => r.data),
    enabled: !!restaurantId,
  });
}
