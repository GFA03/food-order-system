import { Navigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import RegisterForm from '../components/forms/RegisterForm';

export default function RegisterPage() {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to="/restaurants" replace />;
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-md bg-white p-8 rounded-xl shadow-sm border border-gray-100">
        <div className="text-center mb-6">
          <span className="text-4xl">🍽️</span>
          <h1 className="mt-2 text-2xl font-bold text-gray-800">Create an account</h1>
          <p className="text-gray-500 text-sm mt-1">Join OmniEats today</p>
        </div>
        <RegisterForm />
        <p className="mt-4 text-center text-sm text-gray-500">
          Already have an account?{' '}
          <Link to="/login" className="text-orange-500 hover:underline font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
