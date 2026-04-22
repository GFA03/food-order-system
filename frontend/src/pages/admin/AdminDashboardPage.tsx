import { Link } from 'react-router-dom';

const sections = [
  { title: 'Restaurants', description: 'Create, edit, and delete restaurants', href: '/admin/restaurants', icon: '🍽️' },
  { title: 'Cuisine Tags', description: 'Manage cuisine category tags', href: '/admin/cuisine-tags', icon: '🏷️' },
];

export default function AdminDashboardPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-2">Admin Dashboard</h1>
      <p className="text-gray-500 mb-8">Manage the OmniEats platform</p>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 max-w-2xl">
        {sections.map((s) => (
          <Link key={s.href} to={s.href} className="flex items-start gap-4 p-5 bg-white rounded-xl border border-gray-100 hover:shadow-md hover:border-orange-200 transition-all">
            <span className="text-3xl">{s.icon}</span>
            <div>
              <h2 className="font-semibold text-gray-800">{s.title}</h2>
              <p className="text-sm text-gray-500 mt-0.5">{s.description}</p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
