import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';

export default function CartPage() {
  const { items, restaurantName, totalItems, totalPrice, removeItem, updateQuantity } = useCart();

  if (totalItems === 0) {
    return (
      <div className="text-center py-16">
        <p className="text-5xl mb-4">🛒</p>
        <h1 className="text-2xl font-bold text-gray-800">Your cart is empty</h1>
        <p className="text-gray-500 mt-2">Add some items from a restaurant to get started.</p>
        <Link
          to="/restaurants"
          className="mt-6 inline-block px-6 py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors"
        >
          Browse Restaurants
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">Your Cart</h1>
      {restaurantName && (
        <p className="text-sm text-gray-500 mb-6">From: <span className="font-medium text-gray-700">{restaurantName}</span></p>
      )}

      <div className="space-y-3 mb-6">
        {items.map(({ menuItem, quantity }) => (
          <div key={menuItem.id} className="flex items-center gap-4 bg-white p-4 rounded-xl border border-gray-100">
            <div className="flex-1 min-w-0">
              <p className="font-medium text-gray-800">{menuItem.name}</p>
              <p className="text-sm text-orange-500">${menuItem.price.toFixed(2)} each</p>
            </div>
            <div className="flex items-center gap-2">
              <button onClick={() => updateQuantity(menuItem.id, quantity - 1)} className="w-7 h-7 flex items-center justify-center rounded-full border border-gray-300 hover:border-orange-400 hover:text-orange-500 transition-colors" aria-label="Decrease quantity">−</button>
              <span className="w-6 text-center text-sm font-medium">{quantity}</span>
              <button onClick={() => updateQuantity(menuItem.id, quantity + 1)} className="w-7 h-7 flex items-center justify-center rounded-full border border-gray-300 hover:border-orange-400 hover:text-orange-500 transition-colors" aria-label="Increase quantity">+</button>
            </div>
            <p className="w-16 text-right font-semibold text-gray-800">${(menuItem.price * quantity).toFixed(2)}</p>
            <button onClick={() => removeItem(menuItem.id)} className="text-gray-400 hover:text-red-500 transition-colors" aria-label={`Remove ${menuItem.name}`}>✕</button>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-xl border border-gray-100 p-4 mb-6">
        <div className="flex justify-between text-sm text-gray-600 mb-2">
          <span>Items ({totalItems})</span>
          <span>${totalPrice.toFixed(2)}</span>
        </div>
        <div className="flex justify-between font-bold text-gray-800 text-lg border-t border-gray-100 pt-2">
          <span>Total</span>
          <span>${totalPrice.toFixed(2)}</span>
        </div>
      </div>

      <Link to="/checkout" className="block w-full text-center py-3 bg-orange-500 text-white font-semibold rounded-xl hover:bg-orange-600 transition-colors">
        Proceed to Checkout
      </Link>
    </div>
  );
}
