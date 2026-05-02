import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAdminOrders, useUpdateOrderStatus } from '../../api/admin';
import Pagination from '../../components/ui/Pagination';
import { useToast } from '../../components/ui/Toast';
import type { Order } from '../../types';

const PAGE_SIZE_OPTIONS = [5, 10, 25];

export default function AdminOrdersPage() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const { data, isLoading } = useAdminOrders({ page, size: pageSize });
  const updateStatus = useUpdateOrderStatus();
  const { showToast } = useToast();

  const handleStatusChange = async (orderId: string, newStatus: Order['status']) => {
    try {
      await updateStatus.mutateAsync({ id: orderId, status: newStatus });
      showToast('Order status updated successfully', 'success');
    } catch {
      showToast('Failed to update order status', 'error');
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <Link to="/admin" className="text-sm text-orange-500 hover:underline">← Admin</Link>
          <h1 className="text-2xl font-bold text-gray-800 mt-1">Orders</h1>
        </div>
      </div>

      {isLoading ? (
        <p className="text-gray-500">Loading…</p>
      ) : (
        <>
          <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Order ID</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Restaurant</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Total</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Date</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data?.content.map((order) => (
                  <tr key={order.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-800 font-mono text-xs">{order.id}</td>
                    <td className="px-4 py-3 text-gray-800">{order.restaurantName}</td>
                    <td className="px-4 py-3 text-gray-800">${order.total.toFixed(2)}</td>
                    <td className="px-4 py-3 text-gray-500">
                      {new Date(order.createdAt).toLocaleString()}
                    </td>
                    <td className="px-4 py-3">
                      <select
                        value={order.status}
                        onChange={(e) => handleStatusChange(order.id, e.target.value as Order['status'])}
                        className="text-xs font-medium px-2 py-1 rounded-md border border-gray-200 bg-white"
                        disabled={updateStatus.isPending}
                      >
                        <option value="PENDING">PENDING</option>
                        <option value="CONFIRMED">CONFIRMED</option>
                        <option value="PREPARING">PREPARING</option>
                        <option value="DELIVERED">DELIVERED</option>
                        <option value="CANCELLED">CANCELLED</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {(!data?.content || data.content.length === 0) && (
              <div className="p-8 text-center text-gray-500">No orders found.</div>
            )}
          </div>
          <Pagination
            currentPage={page}
            totalPages={data?.totalPages ?? 1}
            totalElements={data?.totalElements}
            pageSize={pageSize}
            pageSizeOptions={PAGE_SIZE_OPTIONS}
            onPageChange={setPage}
            onPageSizeChange={(s) => { setPageSize(s); setPage(0); }}
          />
        </>
      )}
    </div>
  );
}
