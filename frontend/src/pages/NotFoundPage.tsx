import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 px-4">
      <h1 className="text-9xl font-bold text-orange-500">404</h1>
      <h2 className="mt-4 text-2xl font-semibold text-gray-800">Page not found</h2>
      <p className="mt-2 text-gray-500">The page you're looking for doesn't exist or has been moved.</p>
      <Link
        to="/restaurants"
        className="mt-6 px-6 py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors"
      >
        Back to Restaurants
      </Link>
    </div>
  );
}
