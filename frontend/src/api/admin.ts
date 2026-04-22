import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from './client';
import type { Restaurant, MenuItem, CuisineTag } from '../types';

// ── Restaurants ───────────────────────────────────────────────────────────────

interface CreateRestaurantRequest {
  name: string;
  description: string;
  rating: number;
  deliveryTime: number;
  cuisineTagIds?: string[];
}

export function useCreateRestaurant() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateRestaurantRequest) =>
      apiClient.post<Restaurant>('/api/restaurants', data).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['restaurants'] }),
  });
}

export function useUpdateRestaurant() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...data }: CreateRestaurantRequest & { id: string }) =>
      apiClient.put<Restaurant>(`/api/restaurants/${id}`, data).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['restaurants'] }),
  });
}

export function useDeleteRestaurant() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.delete(`/api/restaurants/${id}`).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['restaurants'] }),
  });
}

// ── Menu Items ────────────────────────────────────────────────────────────────

interface CreateMenuItemRequest {
  name: string;
  description: string;
  price: number;
}

export function useCreateMenuItem(restaurantId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateMenuItemRequest) =>
      apiClient.post<MenuItem>(`/api/restaurants/${restaurantId}/menu`, data).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['menu', restaurantId] }),
  });
}

export function useUpdateMenuItem(restaurantId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...data }: CreateMenuItemRequest & { id: string }) =>
      apiClient
        .put<MenuItem>(`/api/restaurants/${restaurantId}/menu/${id}`, data)
        .then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['menu', restaurantId] }),
  });
}

export function useDeleteMenuItem(restaurantId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.delete(`/api/restaurants/${restaurantId}/menu/${id}`).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['menu', restaurantId] }),
  });
}

// ── Cuisine Tags ──────────────────────────────────────────────────────────────

export function useCreateCuisineTag() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string }) =>
      apiClient.post<CuisineTag>('/api/restaurants/tags', data).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['cuisineTags'] }),
  });
}

export function useDeleteCuisineTag() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.delete(`/api/restaurants/tags/${id}`).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['cuisineTags'] }),
  });
}
