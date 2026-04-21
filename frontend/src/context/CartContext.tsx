import { createContext, useContext, useState, type ReactNode } from 'react';
import type { MenuItem, CartItem } from '../types';

interface CartState {
  restaurantId: string | null;
  restaurantName: string | null;
  items: CartItem[];
}

interface CartContextValue extends CartState {
  addItem: (menuItem: MenuItem, quantity: number, restaurantId: string, restaurantName: string) => void;
  removeItem: (menuItemId: string) => void;
  updateQuantity: (menuItemId: string, quantity: number) => void;
  clearCart: () => void;
  totalItems: number;
  totalPrice: number;
}

const CartContext = createContext<CartContextValue | null>(null);

const EMPTY_CART: CartState = { restaurantId: null, restaurantName: null, items: [] };

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<CartState>(EMPTY_CART);

  function addItem(
    menuItem: MenuItem,
    quantity: number,
    restaurantId: string,
    restaurantName: string
  ) {
    setCart((prev) => {
      // Same restaurant — merge
      if (prev.restaurantId === restaurantId || prev.restaurantId === null) {
        const existing = prev.items.find((i) => i.menuItem.id === menuItem.id);
        const items = existing
          ? prev.items.map((i) =>
              i.menuItem.id === menuItem.id
                ? { ...i, quantity: i.quantity + quantity }
                : i
            )
          : [...prev.items, { menuItem, quantity }];
        return { restaurantId, restaurantName, items };
      }
      // Different restaurant — caller must confirm first (handled in UI)
      return prev;
    });
  }

  function removeItem(menuItemId: string) {
    setCart((prev) => ({
      ...prev,
      items: prev.items.filter((i) => i.menuItem.id !== menuItemId),
    }));
  }

  function updateQuantity(menuItemId: string, quantity: number) {
    if (quantity <= 0) {
      removeItem(menuItemId);
      return;
    }
    setCart((prev) => ({
      ...prev,
      items: prev.items.map((i) =>
        i.menuItem.id === menuItemId ? { ...i, quantity } : i
      ),
    }));
  }

  function clearCart() {
    setCart(EMPTY_CART);
  }

  const totalItems = cart.items.reduce((sum, i) => sum + i.quantity, 0);
  const totalPrice = cart.items.reduce((sum, i) => sum + i.menuItem.price * i.quantity, 0);

  return (
    <CartContext.Provider
      value={{ ...cart, addItem, removeItem, updateQuantity, clearCart, totalItems, totalPrice }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used within CartProvider');
  return ctx;
}
