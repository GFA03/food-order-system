import { useRouteError, isRouteErrorResponse, Link } from 'react-router-dom';

export default function ErrorPage() {
  const error = useRouteError();

  const message = isRouteErrorResponse(error)
    ? error.statusText
    : error instanceof Error
    ? error.message
    : 'An unexpected error occurred.';

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 px-4">
      <h1 className="text-6xl font-bold text-red-500">500</h1>
      <h2 className="mt-4 text-2xl font-semibold text-gray-800">Something went wrong</h2>
      <p className="mt-2 text-gray-500 max-w-md text-center">{message}</p>
      <Link
        to="/restaurants"
        className="mt-6 px-6 py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors"
      >
        Back to Restaurants
      </Link>
    </div>
  );
}
