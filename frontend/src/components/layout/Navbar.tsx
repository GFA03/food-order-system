import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const { totalItems } = useCart();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/restaurants" className="flex items-center gap-2">
            <span className="text-2xl">🍽️</span>
            <span className="text-xl font-bold text-orange-500">OmniEats</span>
          </Link>

          {/* Nav links */}
          {isAuthenticated && (
            <div className="hidden md:flex items-center gap-6">
              <NavLink
                to="/restaurants"
                className={({ isActive }) =>
                  `text-sm font-medium transition-colors ${isActive ? 'text-orange-500' : 'text-gray-600 hover:text-orange-500'}`
                }
              >
                Restaurants
              </NavLink>
              <NavLink
                to="/orders"
                className={({ isActive }) =>
                  `text-sm font-medium transition-colors ${isActive ? 'text-orange-500' : 'text-gray-600 hover:text-orange-500'}`
                }
              >
                Orders
              </NavLink>
              <NavLink
                to="/ai-search"
                className={({ isActive }) =>
                  `text-sm font-medium transition-colors ${isActive ? 'text-orange-500' : 'text-gray-600 hover:text-orange-500'}`
                }
              >
                AI Search
              </NavLink>
              <NavLink
                to="/profile"
                className={({ isActive }) =>
                  `text-sm font-medium transition-colors ${isActive ? 'text-orange-500' : 'text-gray-600 hover:text-orange-500'}`
                }
              >
                Profile
              </NavLink>
              {user?.roles.includes('ADMIN') && (
                <NavLink
                  to="/admin"
                  className={({ isActive }) =>
                    `text-sm font-medium transition-colors ${isActive ? 'text-orange-500' : 'text-gray-600 hover:text-orange-500'}`
                  }
                >
                  Admin
                </NavLink>
              )}
            </div>
          )}

          {/* Right side */}
          <div className="flex items-center gap-4">
            {isAuthenticated ? (
              <>
                <NavLink
                  to="/cart"
                  className={({ isActive }) =>
                    `relative text-xl transition-colors ${isActive ? 'text-orange-500' : 'text-gray-600 hover:text-orange-500'}`
                  }
                  aria-label={`Cart, ${totalItems} items`}
                >
                  🛒
                  {totalItems > 0 && (
                    <span className="absolute -top-2 -right-2 bg-orange-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                      {totalItems > 9 ? '9+' : totalItems}
                    </span>
                  )}
                </NavLink>
                <span className="text-sm text-gray-600 hidden sm:block">{user?.name}</span>
                <button
                  onClick={handleLogout}
                  className="text-sm font-medium text-gray-600 hover:text-red-500 transition-colors"
                >
                  Logout
                </button>
              </>
            ) : (
              <Link
                to="/login"
                className="text-sm font-medium text-white bg-orange-500 hover:bg-orange-600 px-4 py-2 rounded-lg transition-colors"
              >
                Login
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
