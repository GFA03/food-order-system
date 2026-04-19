import { Link, NavLink } from 'react-router-dom';

export default function Navbar() {
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
          </div>

          {/* Right side */}
          <div className="flex items-center gap-4">
            <NavLink
              to="/cart"
              className={({ isActive }) =>
                `relative text-sm font-medium transition-colors ${isActive ? 'text-orange-500' : 'text-gray-600 hover:text-orange-500'}`
              }
              aria-label="Cart"
            >
              🛒
            </NavLink>
            <Link
              to="/login"
              className="text-sm font-medium text-gray-600 hover:text-orange-500 transition-colors"
            >
              Login
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
}
