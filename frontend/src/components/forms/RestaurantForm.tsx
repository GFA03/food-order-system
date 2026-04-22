import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { restaurantSchema, type RestaurantFormData } from '../../lib/validators';
import type { Restaurant } from '../../types';

interface RestaurantFormProps {
  defaultValues?: Partial<Restaurant>;
  onSubmit: (data: RestaurantFormData) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

export default function RestaurantForm({ defaultValues, onSubmit, onCancel, isLoading }: RestaurantFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RestaurantFormData>({
    resolver: zodResolver(restaurantSchema),
    defaultValues: defaultValues
      ? {
          name: defaultValues.name,
          description: defaultValues.description,
          rating: defaultValues.rating,
          deliveryTime: defaultValues.deliveryTime,
        }
      : undefined,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
        <input {...register('name')} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400" />
        {errors.name && <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>}
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
        <textarea {...register('description')} rows={3} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400 resize-none" />
        {errors.description && <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>}
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Rating (0–5)</label>
          <input type="number" step="0.1" min="0" max="5" {...register('rating', { valueAsNumber: true })} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400" />
          {errors.rating && <p className="mt-1 text-sm text-red-600">{errors.rating.message}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Delivery Time (min)</label>
          <input type="number" min="1" {...register('deliveryTime', { valueAsNumber: true })} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400" />
          {errors.deliveryTime && <p className="mt-1 text-sm text-red-600">{errors.deliveryTime.message}</p>}
        </div>
      </div>
      <div className="flex gap-3 pt-2">
        <button type="button" onClick={onCancel} className="flex-1 py-2 border border-gray-300 text-gray-700 rounded-lg hover:border-gray-400 transition-colors">Cancel</button>
        <button type="submit" disabled={isLoading} className="flex-1 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:opacity-50 transition-colors">
          {isLoading ? 'Saving…' : 'Save'}
        </button>
      </div>
    </form>
  );
}
