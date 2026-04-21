import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from './client';
import type { Order, PaginatedResponse, CreateOrderRequest } from '../types';

export function useCreateOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (order: CreateOrderRequest) =>
      apiClient.post<Order>('/api/orders', order).then((r) => r.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['orders'] }),
  });
}

export function useOrders(params: { page: number; size: number }) {
  return useQuery({
    queryKey: ['orders', params],
    queryFn: () =>
      apiClient
        .get<PaginatedResponse<Order>>('/api/orders', { params })
        .then((r) => r.data),
  });
}

export function useOrder(orderId: string) {
  return useQuery({
    queryKey: ['order', orderId],
    queryFn: () =>
      apiClient.get<Order>(`/api/orders/${orderId}`).then((r) => r.data),
    enabled: !!orderId,
  });
}
