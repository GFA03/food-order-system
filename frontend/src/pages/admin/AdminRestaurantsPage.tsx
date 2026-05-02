import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useRestaurants } from '../../api/restaurants';
import { useCreateRestaurant, useUpdateRestaurant, useDeleteRestaurant } from '../../api/admin';
import Modal from '../../components/ui/Modal';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import RestaurantForm from '../../components/forms/RestaurantForm';
import Pagination from '../../components/ui/Pagination';
import { useToast } from '../../components/ui/Toast';
import type { Restaurant } from '../../types';
import type { RestaurantFormData } from '../../lib/validators';

const PAGE_SIZE_OPTIONS = [5, 10, 25];

export default function AdminRestaurantsPage() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [showCreate, setShowCreate] = useState(false);
  const [editTarget, setEditTarget] = useState<Restaurant | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Restaurant | null>(null);

  const { data, isLoading } = useRestaurants({ page, size: pageSize, sort: 'rating,desc' });
  const createRestaurant = useCreateRestaurant();
  const updateRestaurant = useUpdateRestaurant();
  const deleteRestaurant = useDeleteRestaurant();
  const { showToast } = useToast();

  async function handleCreate(formData: RestaurantFormData) {
    try {
      await createRestaurant.mutateAsync(formData);
      showToast('Restaurant created!', 'success');
      setShowCreate(false);
    } catch {
      showToast('Failed to create restaurant.', 'error');
    }
  }

  async function handleUpdate(formData: RestaurantFormData) {
    if (!editTarget) return;
    try {
      await updateRestaurant.mutateAsync({ id: editTarget.id, ...formData });
      showToast('Restaurant updated!', 'success');
      setEditTarget(null);
    } catch {
      showToast('Failed to update restaurant.', 'error');
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteRestaurant.mutateAsync(deleteTarget.id);
      showToast('Restaurant deleted.', 'success');
      setDeleteTarget(null);
    } catch {
      showToast('Failed to delete restaurant.', 'error');
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <Link to="/admin" className="text-sm text-orange-500 hover:underline">← Admin</Link>
          <h1 className="text-2xl font-bold text-gray-800 mt-1">Restaurants</h1>
        </div>
        <button onClick={() => setShowCreate(true)} className="px-4 py-2 bg-orange-500 text-white text-sm font-medium rounded-lg hover:bg-orange-600 transition-colors">
          + Add Restaurant
        </button>
      </div>

      {isLoading ? <p className="text-gray-500">Loading…</p> : (
        <>
          <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Name</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600 hidden sm:table-cell">Rating</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600 hidden md:table-cell">Delivery</th>
                  <th className="text-right px-4 py-3 font-medium text-gray-600">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data?.content.map((r) => (
                  <tr key={r.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-800">
                      {r.name}
                      <p className="text-xs text-gray-400 font-normal">{r.description}</p>
                    </td>
                    <td className="px-4 py-3 text-gray-600 hidden sm:table-cell">⭐ {r.rating}</td>
                    <td className="px-4 py-3 text-gray-600 hidden md:table-cell">{r.deliveryTime} min</td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Link to={`/admin/restaurants/${r.id}/menu`} className="text-xs text-blue-500 hover:underline">Menu</Link>
                        <button onClick={() => setEditTarget(r)} className="text-xs text-orange-500 hover:underline">Edit</button>
                        <button onClick={() => setDeleteTarget(r)} className="text-xs text-red-500 hover:underline">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination
            currentPage={page}
            totalPages={data?.totalPages ?? 1}
            totalElements={data?.totalElements}
            pageSize={pageSize}
            pageSizeOptions={PAGE_SIZE_OPTIONS}
            onPageChange={setPage}
            onPageSizeChange={(s) => { setPageSize(s); setPage(0); }}
          />
        </>
      )}

      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Add Restaurant">
        <RestaurantForm onSubmit={handleCreate} onCancel={() => setShowCreate(false)} isLoading={createRestaurant.isPending} />
      </Modal>

      <Modal isOpen={!!editTarget} onClose={() => setEditTarget(null)} title="Edit Restaurant">
        {editTarget && <RestaurantForm defaultValues={editTarget} onSubmit={handleUpdate} onCancel={() => setEditTarget(null)} isLoading={updateRestaurant.isPending} />}
      </Modal>

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Delete Restaurant"
        message={`Are you sure you want to delete "${deleteTarget?.name}"? This action cannot be undone.`}
        isLoading={deleteRestaurant.isPending}
      />
    </div>
  );
}
