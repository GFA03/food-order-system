export interface User {
  id: string;
  email: string;
  name: string;
  roles: string[];
}

export interface UserProfile {
  deliveryAddress: string;
  latitude: number;
  longitude: number;
  dietaryPreferences: string[];
}

export interface UserWithProfile extends User {
  profile: UserProfile;
}

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  rating: number;
  deliveryTime: number; // minutes
  cuisineTags: CuisineTag[];
}

export interface MenuItem {
  id: string;
  name: string;
  description: string;
  price: number;
  restaurantId: string;
}

export interface CuisineTag {
  id: string;
  name: string;
}

export interface Order {
  id: string;
  status: 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'DELIVERED' | 'CANCELLED';
  createdAt: string;
  total: number;
  restaurantName: string;
  items: OrderItem[];
}

export interface OrderItem {
  id: string;
  menuItemName: string;
  quantity: number;
  price: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // current page (0-indexed)
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface AiSuggestion {
  type: 'restaurant' | 'menuItem';
  id: string;
  name: string;
  description: string;
  reason: string;
}

export interface CartItem {
  menuItem: MenuItem;
  quantity: number;
}

export interface CreateOrderRequest {
  restaurantId: string;
  items: { menuItemId: string; quantity: number }[];
}
