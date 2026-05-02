import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useMenuItems } from '../../api/menu';
import { useRestaurant } from '../../api/restaurants';
import { useCreateMenuItem, useUpdateMenuItem, useDeleteMenuItem } from '../../api/admin';
import Modal from '../../components/ui/Modal';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import MenuItemForm from '../../components/forms/MenuItemForm';
import Pagination from '../../components/ui/Pagination';
import { useToast } from '../../components/ui/Toast';
import type { MenuItem } from '../../types';
import type { MenuItemFormData } from '../../lib/validators';

export default function AdminMenuItemsPage() {
  const { id: restaurantId } = useParams<{ id: string }>();
  const [page, setPage] = useState(0);
  const [showCreate, setShowCreate] = useState(false);
  const [editTarget, setEditTarget] = useState<MenuItem | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<MenuItem | null>(null);

  const { data: restaurant } = useRestaurant(restaurantId!);
  const [pageSize, setPageSize] = useState(10);
  const { data: menuData, isLoading } = useMenuItems(restaurantId!, { page, size: pageSize, sort: 'price,asc' });
  const createItem = useCreateMenuItem(restaurantId!);
  const updateItem = useUpdateMenuItem(restaurantId!);
  const deleteItem = useDeleteMenuItem(restaurantId!);
  const { showToast } = useToast();

  async function handleCreate(formData: MenuItemFormData) {
    try {
      await createItem.mutateAsync(formData);
      showToast('Menu item created!', 'success');
      setShowCreate(false);
    } catch {
      showToast('Failed to create menu item.', 'error');
    }
  }

  async function handleUpdate(formData: MenuItemFormData) {
    if (!editTarget) return;
    try {
      await updateItem.mutateAsync({ id: editTarget.id, ...formData });
      showToast('Menu item updated!', 'success');
      setEditTarget(null);
    } catch {
      showToast('Failed to update menu item.', 'error');
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteItem.mutateAsync(deleteTarget.id);
      showToast('Menu item deleted.', 'success');
      setDeleteTarget(null);
    } catch {
      showToast('Failed to delete menu item.', 'error');
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <Link to="/admin/restaurants" className="text-sm text-orange-500 hover:underline">← Restaurants</Link>
          <h1 className="text-2xl font-bold text-gray-800 mt-1">{restaurant?.name ?? 'Restaurant'} — Menu</h1>
        </div>
        <button onClick={() => setShowCreate(true)} className="px-4 py-2 bg-orange-500 text-white text-sm font-medium rounded-lg hover:bg-orange-600 transition-colors">+ Add Item</button>
      </div>

      {isLoading ? <p className="text-gray-500">Loading…</p> : (
        <>
          <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th className="text-left px-4 py-3 font-medium text-gray-600">Name</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600 hidden sm:table-cell">Price</th>
                  <th className="text-right px-4 py-3 font-medium text-gray-600">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {menuData?.content.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-800">
                      {item.name}
                      <p className="text-xs text-gray-400 font-normal">{item.description}</p>
                    </td>
                    <td className="px-4 py-3 text-gray-600 hidden sm:table-cell">${item.price.toFixed(2)}</td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button onClick={() => setEditTarget(item)} className="text-xs text-orange-500 hover:underline">Edit</button>
                        <button onClick={() => setDeleteTarget(item)} className="text-xs text-red-500 hover:underline">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination
            currentPage={page}
            totalPages={menuData?.totalPages ?? 1}
            totalElements={menuData?.totalElements}
            pageSize={pageSize}
            pageSizeOptions={[5, 10, 25]}
            onPageChange={setPage}
            onPageSizeChange={(s) => { setPageSize(s); setPage(0); }}
          />
        </>
      )}

      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Add Menu Item">
        <MenuItemForm onSubmit={handleCreate} onCancel={() => setShowCreate(false)} isLoading={createItem.isPending} />
      </Modal>
      <Modal isOpen={!!editTarget} onClose={() => setEditTarget(null)} title="Edit Menu Item">
        {editTarget && <MenuItemForm defaultValues={editTarget} onSubmit={handleUpdate} onCancel={() => setEditTarget(null)} isLoading={updateItem.isPending} />}
      </Modal>
      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Delete Menu Item" message={`Delete "${deleteTarget?.name}"?`} isLoading={deleteItem.isPending} />
    </div>
  );
}
