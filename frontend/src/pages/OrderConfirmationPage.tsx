import { Link, useParams } from 'react-router-dom';
import { useOrder } from '../api/orders';

export default function OrderConfirmationPage() {
  const { id } = useParams<{ id: string }>();
  const { data: order, isLoading } = useOrder(id!);

  return (
    <div className="max-w-md mx-auto text-center py-16">
      <div className="text-6xl mb-4">🎉</div>
      <h1 className="text-2xl font-bold text-gray-800">Order Placed!</h1>
      {isLoading ? (
        <p className="text-gray-500 mt-2">Loading order details…</p>
      ) : order ? (
        <>
          <p className="text-gray-500 mt-2">
            Order <span className="font-mono font-medium text-gray-700">#{order.id}</span>
          </p>
          <p className="mt-1 text-sm text-gray-500">
            Status: <span className="font-medium text-orange-500">{order.status}</span>
          </p>
        </>
      ) : (
        <p className="text-gray-500 mt-2">Order ID: <span className="font-mono">{id}</span></p>
      )}
      <div className="flex flex-col gap-3 mt-8">
        <Link to="/orders" className="px-6 py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors">
          View Order History
        </Link>
        <Link to="/restaurants" className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:border-orange-400 hover:text-orange-500 transition-colors">
          Continue Browsing
        </Link>
      </div>
    </div>
  );
}
