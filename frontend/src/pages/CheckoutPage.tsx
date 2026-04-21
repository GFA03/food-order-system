import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useCreateOrder } from '../api/orders';

export default function CheckoutPage() {
  const navigate = useNavigate();
  const { items, restaurantId, restaurantName, totalPrice, clearCart } = useCart();
  const createOrder = useCreateOrder();

  if (items.length === 0) {
    return (
      <div className="text-center py-16">
        <p className="text-gray-500">Your cart is empty.</p>
        <Link to="/restaurants" className="mt-4 inline-block text-orange-500 hover:underline">
          Browse Restaurants
        </Link>
      </div>
    );
  }

  async function handlePlaceOrder() {
    if (!restaurantId) return;
    try {
      const order = await createOrder.mutateAsync({
        restaurantId,
        items: items.map((i) => ({ menuItemId: i.menuItem.id, quantity: i.quantity })),
      });
      clearCart();
      navigate(`/orders/${order.id}/confirmation`);
    } catch {
      // error shown via createOrder.isError
    }
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Checkout</h1>

      <div className="bg-white rounded-xl border border-gray-100 p-5 mb-6">
        <h2 className="font-semibold text-gray-700 mb-3">Order Summary</h2>
        {restaurantName && (
          <p className="text-sm text-gray-500 mb-3">From: <span className="font-medium text-gray-700">{restaurantName}</span></p>
        )}
        <div className="space-y-2">
          {items.map(({ menuItem, quantity }) => (
            <div key={menuItem.id} className="flex justify-between text-sm text-gray-700">
              <span>{menuItem.name} × {quantity}</span>
              <span>${(menuItem.price * quantity).toFixed(2)}</span>
            </div>
          ))}
        </div>
        <div className="flex justify-between font-bold text-gray-800 border-t border-gray-100 mt-3 pt-3">
          <span>Total</span>
          <span>${totalPrice.toFixed(2)}</span>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-100 p-5 mb-6">
        <h2 className="font-semibold text-gray-700 mb-2">Delivery Address</h2>
        <p className="text-sm text-gray-500">Strada Academiei 14, Bucuresti (from your profile)</p>
      </div>

      {createOrder.isError && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-lg mb-4">
          <p className="text-sm text-red-700">Failed to place order. Please try again.</p>
        </div>
      )}

      <button
        onClick={handlePlaceOrder}
        disabled={createOrder.isPending}
        className="w-full py-3 bg-orange-500 text-white font-semibold rounded-xl hover:bg-orange-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        {createOrder.isPending ? 'Placing Order…' : 'Place Order'}
      </button>
    </div>
  );
}
