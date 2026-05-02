import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useRestaurant } from '../api/restaurants';
import { useMenuItems } from '../api/menu';
import { Skeleton, RestaurantCardSkeleton } from '../components/ui/Skeleton';
import Pagination from '../components/ui/Pagination';
import Modal from '../components/ui/Modal';
import { useCart } from '../context/CartContext';
import type { MenuItem } from '../types';

interface MenuItemCardProps {
  item: MenuItem;
  onAddToCart: (item: MenuItem, quantity: number) => void;
}

function MenuItemCard({ item, onAddToCart }: MenuItemCardProps) {
  const [qty, setQty] = useState(1);

  return (
    <div className="flex items-start justify-between gap-4 p-4 bg-white rounded-xl border border-gray-100">
      <div className="flex-1 min-w-0">
        <h3 className="font-semibold text-gray-800">{item.name}</h3>
        <p className="text-sm text-gray-500 mt-0.5 line-clamp-2">{item.description}</p>
        <p className="text-orange-500 font-bold mt-1">${item.price.toFixed(2)}</p>
      </div>
      <div className="flex flex-col items-end gap-2 shrink-0">
        <div className="flex items-center gap-2">
          <button onClick={() => setQty((q) => Math.max(1, q - 1))} className="w-7 h-7 flex items-center justify-center rounded-full border border-gray-300 text-gray-600 hover:border-orange-400 hover:text-orange-500 transition-colors" aria-label="Decrease quantity">−</button>
          <span className="w-6 text-center text-sm font-medium">{qty}</span>
          <button onClick={() => setQty((q) => q + 1)} className="w-7 h-7 flex items-center justify-center rounded-full border border-gray-300 text-gray-600 hover:border-orange-400 hover:text-orange-500 transition-colors" aria-label="Increase quantity">+</button>
        </div>
        <button onClick={() => onAddToCart(item, qty)} className="px-3 py-1.5 bg-orange-500 text-white text-sm font-medium rounded-lg hover:bg-orange-600 transition-colors">
          Add to Cart
        </button>
      </div>
    </div>
  );
}

export default function RestaurantDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(8);
  const [sort, setSort] = useState('price,asc');
  const [pendingItem, setPendingItem] = useState<{ item: MenuItem; qty: number } | null>(null);

  const { data: restaurant, isLoading: loadingRestaurant, isError: errorRestaurant } = useRestaurant(id!);
  const { data: menuData, isLoading: loadingMenu } = useMenuItems(id!, { page, size: pageSize, sort });
  const { addItem, restaurantId: cartRestaurantId, clearCart } = useCart();

  function handleAddToCart(item: MenuItem, quantity: number) {
    if (cartRestaurantId && cartRestaurantId !== id) {
      setPendingItem({ item, qty: quantity });
    } else {
      addItem(item, quantity, id!, restaurant?.name ?? '');
    }
  }

  function confirmClearAndAdd() {
    if (!pendingItem) return;
    clearCart();
    addItem(pendingItem.item, pendingItem.qty, id!, restaurant?.name ?? '');
    setPendingItem(null);
  }

  if (errorRestaurant) {
    return (
      <div className="text-center py-12">
        <p className="text-red-500">Restaurant not found.</p>
        <Link to="/restaurants" className="mt-4 inline-block text-orange-500 hover:underline">← Back to Restaurants</Link>
      </div>
    );
  }

  return (
    <div>
      <Link to="/restaurants" className="text-sm text-orange-500 hover:underline mb-4 inline-block">← Back to Restaurants</Link>

      {loadingRestaurant ? (
        <div className="space-y-3 mb-8">
          <Skeleton className="h-8 w-1/2" />
          <Skeleton className="h-4 w-3/4" />
          <Skeleton className="h-4 w-1/3" />
        </div>
      ) : restaurant ? (
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-800">{restaurant.name}</h1>
          <p className="text-gray-500 mt-2">{restaurant.description}</p>
          <div className="flex items-center gap-6 mt-3 text-sm text-gray-600">
            <span className="flex items-center gap-1"><span aria-hidden="true">⭐</span><span className="font-medium">{restaurant.rating.toFixed(1)}</span></span>
            <span className="flex items-center gap-1"><span aria-hidden="true">🕐</span><span>{restaurant.deliveryTime} min delivery</span></span>
          </div>
          {restaurant.cuisineTags.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-3">
              {restaurant.cuisineTags.map((tag) => (
                <span key={tag.id} className="px-2 py-0.5 bg-orange-50 text-orange-600 text-xs font-medium rounded-full">{tag.name}</span>
              ))}
            </div>
          )}
        </div>
      ) : null}

      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-bold text-gray-800">Menu</h2>
        <select value={sort} onChange={(e) => { setSort(e.target.value); setPage(0); }} className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" aria-label="Sort menu items">
          <option value="price,asc">Price: Low → High</option>
          <option value="price,desc">Price: High → Low</option>
        </select>
      </div>

      {loadingMenu ? (
        <div className="space-y-3">{Array.from({ length: 4 }).map((_, i) => <RestaurantCardSkeleton key={i} />)}</div>
      ) : menuData?.content.length === 0 ? (
        <p className="text-gray-500 text-center py-8">No menu items available.</p>
      ) : (
        <>
          <div className="space-y-3">
            {menuData?.content.map((item) => <MenuItemCard key={item.id} item={item} onAddToCart={handleAddToCart} />)}
          </div>
          <Pagination
            currentPage={page}
            totalPages={menuData?.totalPages ?? 1}
            totalElements={menuData?.totalElements}
            pageSize={pageSize}
            pageSizeOptions={[4, 8, 16]}
            onPageChange={setPage}
            onPageSizeChange={(s) => { setPageSize(s); setPage(0); }}
          />
        </>
      )}

      <Modal isOpen={!!pendingItem} onClose={() => setPendingItem(null)} title="Start a new cart?">
        <p className="text-gray-600 text-sm mb-6">Your cart contains items from a different restaurant. Adding this item will clear your current cart.</p>
        <div className="flex gap-3">
          <button onClick={() => setPendingItem(null)} className="flex-1 py-2 border border-gray-300 text-gray-700 rounded-lg hover:border-gray-400 transition-colors">Keep current cart</button>
          <button onClick={confirmClearAndAdd} className="flex-1 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors">Start new cart</button>
        </div>
      </Modal>
    </div>
  );
}
