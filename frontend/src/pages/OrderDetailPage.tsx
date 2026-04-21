import { useParams, Link } from 'react-router-dom';
import { useOrder } from '../api/orders';
import { Skeleton } from '../components/ui/Skeleton';
import type { Order } from '../types';

const STATUS_COLORS: Record<Order['status'], string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PREPARING: 'bg-purple-100 text-purple-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: order, isLoading, isError } = useOrder(id!);

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto space-y-4">
        <Skeleton className="h-8 w-1/3" />
        <Skeleton className="h-4 w-1/2" />
        <div className="space-y-3 mt-6">
          {Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-12 w-full" />)}
        </div>
      </div>
    );
  }

  if (isError || !order) {
    return (
      <div className="text-center py-12">
        <p className="text-red-500">Order not found.</p>
        <Link to="/orders" className="mt-4 inline-block text-orange-500 hover:underline">← Back to Orders</Link>
      </div>
    );
  }

  const subtotal = order.items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  return (
    <div className="max-w-2xl mx-auto">
      <Link to="/orders" className="text-sm text-orange-500 hover:underline mb-4 inline-block">← Back to Orders</Link>

      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">{order.restaurantName}</h1>
          <p className="text-sm text-gray-500 mt-1">{new Date(order.createdAt).toLocaleString()}</p>
        </div>
        <span className={`px-3 py-1 text-sm font-medium rounded-full ${STATUS_COLORS[order.status]}`}>{order.status}</span>
      </div>

      <div className="bg-white rounded-xl border border-gray-100 overflow-hidden mb-4">
        <div className="px-5 py-3 border-b border-gray-100 bg-gray-50">
          <h2 className="font-semibold text-gray-700 text-sm">Items</h2>
        </div>
        <div className="divide-y divide-gray-100">
          {order.items.map((item) => (
            <div key={item.id} className="flex items-center justify-between px-5 py-3">
              <div>
                <p className="font-medium text-gray-800">{item.menuItemName}</p>
                <p className="text-sm text-gray-500">× {item.quantity}</p>
              </div>
              <div className="text-right">
                <p className="font-medium text-gray-800">${(item.price * item.quantity).toFixed(2)}</p>
                <p className="text-xs text-gray-400">${item.price.toFixed(2)} each</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-100 p-5">
        <div className="flex justify-between text-sm text-gray-600 mb-2">
          <span>Subtotal</span>
          <span>${subtotal.toFixed(2)}</span>
        </div>
        <div className="flex justify-between font-bold text-gray-800 text-lg border-t border-gray-100 pt-2">
          <span>Total</span>
          <span>${order.total.toFixed(2)}</span>
        </div>
      </div>
    </div>
  );
}
