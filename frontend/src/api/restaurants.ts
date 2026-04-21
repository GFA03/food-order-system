import { useQuery } from '@tanstack/react-query';
import apiClient from './client';
import type { Restaurant, CuisineTag, PaginatedResponse } from '../types';

interface RestaurantsParams {
  page: number;
  size: number;
  sort: string;
  tags?: string[];
}

export function useRestaurants(params: RestaurantsParams) {
  return useQuery({
    queryKey: ['restaurants', params],
    queryFn: () =>
      apiClient
        .get<PaginatedResponse<Restaurant>>('/api/restaurants', { params })
        .then((r) => r.data),
  });
}

export function useCuisineTags() {
  return useQuery({
    queryKey: ['cuisineTags'],
    queryFn: () =>
      apiClient.get<CuisineTag[]>('/api/restaurants/tags').then((r) => r.data),
  });
}

export function useRestaurant(id: string) {
  return useQuery({
    queryKey: ['restaurant', id],
    queryFn: () =>
      apiClient.get<Restaurant>(`/api/restaurants/${id}`).then((r) => r.data),
    enabled: !!id,
  });
}
