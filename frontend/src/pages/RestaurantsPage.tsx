import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useRestaurants, useCuisineTags } from '../api/restaurants';
import { RestaurantCardSkeleton } from '../components/ui/Skeleton';
import Pagination from '../components/ui/Pagination';
import type { Restaurant } from '../types';

const SORT_OPTIONS = [
  { label: 'Rating (High → Low)', value: 'rating,desc' },
  { label: 'Rating (Low → High)', value: 'rating,asc' },
  { label: 'Delivery Time (Fast → Slow)', value: 'deliveryTime,asc' },
  { label: 'Delivery Time (Slow → Fast)', value: 'deliveryTime,desc' },
];

function RestaurantCard({ restaurant }: { restaurant: Restaurant }) {
  return (
    <Link
      to={`/restaurants/${restaurant.id}`}
      className="block bg-white rounded-xl border border-gray-100 p-5 hover:shadow-md hover:border-orange-200 transition-all"
    >
      <h3 className="text-lg font-semibold text-gray-800">{restaurant.name}</h3>
      <p className="text-sm text-gray-500 mt-1 line-clamp-2">{restaurant.description}</p>
      <div className="flex items-center gap-4 mt-3 text-sm text-gray-600">
        <span className="flex items-center gap-1">
          <span aria-hidden="true">⭐</span>
          <span>{restaurant.rating.toFixed(1)}</span>
        </span>
        <span className="flex items-center gap-1">
          <span aria-hidden="true">🕐</span>
          <span>{restaurant.deliveryTime} min</span>
        </span>
      </div>
      {restaurant.cuisineTags.length > 0 && (
        <div className="flex flex-wrap gap-1 mt-3">
          {restaurant.cuisineTags.map((tag) => (
            <span
              key={tag.id}
              className="px-2 py-0.5 bg-orange-50 text-orange-600 text-xs font-medium rounded-full"
            >
              {tag.name}
            </span>
          ))}
        </div>
      )}
    </Link>
  );
}

export default function RestaurantsPage() {
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState('rating,desc');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);

  const { data, isLoading, isError } = useRestaurants({
    page,
    size: 6,
    sort,
    tags: selectedTags,
  });

  const { data: tags } = useCuisineTags();

  function toggleTag(tagId: string) {
    setSelectedTags((prev) =>
      prev.includes(tagId) ? prev.filter((t) => t !== tagId) : [...prev, tagId]
    );
    setPage(0);
  }

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Restaurants</h1>

        {/* Sort */}
        <select
          value={sort}
          onChange={(e) => { setSort(e.target.value); setPage(0); }}
          className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400"
          aria-label="Sort restaurants"
        >
          {SORT_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>

      {/* Tag filters */}
      {tags && tags.length > 0 && (
        <div className="flex flex-wrap gap-2 mb-6" role="group" aria-label="Filter by cuisine">
          {tags.map((tag) => (
            <button
              key={tag.id}
              onClick={() => toggleTag(tag.id)}
              aria-pressed={selectedTags.includes(tag.id)}
              className={`px-3 py-1.5 text-sm font-medium rounded-full border transition-colors ${
                selectedTags.includes(tag.id)
                  ? 'bg-orange-500 text-white border-orange-500'
                  : 'bg-white text-gray-600 border-gray-300 hover:border-orange-400 hover:text-orange-500'
              }`}
            >
              {tag.name}
            </button>
          ))}
          {selectedTags.length > 0 && (
            <button
              onClick={() => { setSelectedTags([]); setPage(0); }}
              className="px-3 py-1.5 text-sm font-medium text-red-500 hover:underline"
            >
              Clear filters
            </button>
          )}
        </div>
      )}

      {/* Results */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <RestaurantCardSkeleton key={i} />
          ))}
        </div>
      ) : isError ? (
        <div className="text-center py-12 text-red-500">
          Failed to load restaurants. Please try again.
        </div>
      ) : data?.content.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          No restaurants found matching your filters.
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {data?.content.map((restaurant) => (
              <RestaurantCard key={restaurant.id} restaurant={restaurant} />
            ))}
          </div>
          <Pagination
            currentPage={page}
            totalPages={data?.totalPages ?? 1}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  );
}
