import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { menuItemSchema, type MenuItemFormData } from '../../lib/validators';
import type { MenuItem } from '../../types';

interface MenuItemFormProps {
  defaultValues?: Partial<MenuItem>;
  onSubmit: (data: MenuItemFormData) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

export default function MenuItemForm({ defaultValues, onSubmit, onCancel, isLoading }: MenuItemFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<MenuItemFormData>({
    resolver: zodResolver(menuItemSchema),
    defaultValues: defaultValues
      ? { name: defaultValues.name, description: defaultValues.description, price: defaultValues.price }
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
        <textarea {...register('description')} rows={2} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400 resize-none" />
        {errors.description && <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>}
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Price ($)</label>
        <input type="number" step="0.01" min="0.01" {...register('price', { valueAsNumber: true })} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400" />
        {errors.price && <p className="mt-1 text-sm text-red-600">{errors.price.message}</p>}
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
