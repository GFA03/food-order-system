import { createBrowserRouter } from 'react-router-dom';
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
import NotFoundPage from './pages/NotFoundPage';
import ErrorPage from './pages/ErrorPage';

const router = createBrowserRouter([
  // Public routes (no layout)
  { path: '/login', element: <LoginPage />, errorElement: <ErrorPage /> },
  { path: '/register', element: <RegisterPage />, errorElement: <ErrorPage /> },

  // App routes (with layout)
  {
    path: '/',
    element: <PageLayout />,
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <RestaurantsPage /> },
      { path: 'restaurants', element: <RestaurantsPage /> },
      { path: 'restaurants/:id', element: <RestaurantDetailPage /> },
      { path: 'cart', element: <CartPage /> },
      { path: 'checkout', element: <CheckoutPage /> },
      { path: 'orders/:id/confirmation', element: <OrderConfirmationPage /> },
      { path: 'orders', element: <OrderHistoryPage /> },
      { path: 'orders/:id', element: <OrderDetailPage /> },
      { path: 'profile', element: <ProfilePage /> },
      { path: 'ai-search', element: <AiSearchPage /> },
      { path: 'admin', element: <AdminDashboardPage /> },
      { path: 'admin/restaurants', element: <AdminRestaurantsPage /> },
      { path: 'admin/restaurants/:id/menu', element: <AdminMenuItemsPage /> },
      { path: 'admin/cuisine-tags', element: <AdminCuisineTagsPage /> },
    ],
  },

  // 404
  { path: '*', element: <NotFoundPage /> },
]);

export default router;
