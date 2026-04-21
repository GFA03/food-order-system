import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useOrders } from '../api/orders';
import Pagination from '../components/ui/Pagination';
import { Skeleton } from '../components/ui/Skeleton';
import type { Order } from '../types';

const STATUS_COLORS: Record<Order['status'], string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PREPARING: 'bg-purple-100 text-purple-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

function OrderRowSkeleton() {
  return (
    <div className="flex items-center gap-4 p-4 bg-white rounded-xl border border-gray-100">
      <div className="flex-1 space-y-2">
        <Skeleton className="h-4 w-1/3" />
        <Skeleton className="h-3 w-1/2" />
      </div>
      <Skeleton className="h-6 w-20 rounded-full" />
      <Skeleton className="h-4 w-16" />
    </div>
  );
}

export default function OrderHistoryPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError } = useOrders({ page, size: 10 });

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Order History</h1>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => <OrderRowSkeleton key={i} />)}
        </div>
      ) : isError ? (
        <p className="text-red-500 text-center py-8">Failed to load orders.</p>
      ) : data?.content.length === 0 ? (
        <div className="text-center py-16">
          <p className="text-5xl mb-4">📋</p>
          <p className="text-gray-500">No orders yet. Place your first order!</p>
          <Link to="/restaurants" className="mt-4 inline-block text-orange-500 hover:underline">Browse Restaurants</Link>
        </div>
      ) : (
        <>
          <div className="space-y-3">
            {data?.content.map((order) => (
              <Link key={order.id} to={`/orders/${order.id}`} className="flex items-center gap-4 p-4 bg-white rounded-xl border border-gray-100 hover:shadow-sm hover:border-orange-200 transition-all">
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-gray-800">{order.restaurantName}</p>
                  <p className="text-sm text-gray-500 mt-0.5">{new Date(order.createdAt).toLocaleDateString()} · {order.items.length} item{order.items.length !== 1 ? 's' : ''}</p>
                </div>
                <span className={`px-2.5 py-1 text-xs font-medium rounded-full ${STATUS_COLORS[order.status]}`}>{order.status}</span>
                <p className="font-semibold text-gray-800 shrink-0">${order.total.toFixed(2)}</p>
                <span className="text-gray-400 text-sm">→</span>
              </Link>
            ))}
          </div>
          <Pagination currentPage={page} totalPages={data?.totalPages ?? 1} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}
