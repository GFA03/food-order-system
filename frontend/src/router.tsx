import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import PageLayout from './components/layout/PageLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import RestaurantsPage from './pages/RestaurantsPage';
import RestaurantDetailPage from './pages/RestaurantDetailPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import OrderConfirmationPage from './pages/OrderConfirmationPage';
import OrderHistoryPage from './pages/OrderHistoryPage';
import OrderDetailPage from './pages/OrderDetailPage';
import ProfilePage from './pages/ProfilePage';
import AiSearchPage from './pages/AiSearchPage';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminRestaurantsPage from './pages/admin/AdminRestaurantsPage';
import AdminMenuItemsPage from './pages/admin/AdminMenuItemsPage';
import AdminCuisineTagsPage from './pages/admin/AdminCuisineTagsPage';
import AdminOrdersPage from './pages/admin/AdminOrdersPage';
import NotFoundPage from './pages/NotFoundPage';
import ErrorPage from './pages/ErrorPage';

function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}

function AdminRoute() {
  const { isAuthenticated, user } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!user?.roles.includes('ADMIN')) return <Navigate to="/restaurants" replace />;
  return <Outlet />;
}

const router = createBrowserRouter([
  // Public routes
  { path: '/login', element: <LoginPage />, errorElement: <ErrorPage /> },
  { path: '/register', element: <RegisterPage />, errorElement: <ErrorPage /> },

  // Protected app routes
  {
    path: '/',
    element: <PageLayout />,
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <Navigate to="/restaurants" replace /> },

      // USER routes
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'restaurants', element: <RestaurantsPage /> },
          { path: 'restaurants/:id', element: <RestaurantDetailPage /> },
          { path: 'cart', element: <CartPage /> },
          { path: 'checkout', element: <CheckoutPage /> },
          { path: 'orders/:id/confirmation', element: <OrderConfirmationPage /> },
          { path: 'orders', element: <OrderHistoryPage /> },
          { path: 'orders/:id', element: <OrderDetailPage /> },
          { path: 'profile', element: <ProfilePage /> },
          { path: 'ai-search', element: <AiSearchPage /> },
        ],
      },

      // ADMIN routes
      {
        element: <AdminRoute />,
        children: [
          { path: 'admin', element: <AdminDashboardPage /> },
          { path: 'admin/restaurants', element: <AdminRestaurantsPage /> },
          { path: 'admin/restaurants/:id/menu', element: <AdminMenuItemsPage /> },
          { path: 'admin/cuisine-tags', element: <AdminCuisineTagsPage /> },
          { path: 'admin/orders', element: <AdminOrdersPage /> },
        ],
      },
    ],
  },

  // 404
  { path: '*', element: <NotFoundPage /> },
]);

export default router;
