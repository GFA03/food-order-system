import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useCuisineTags } from '../../api/restaurants';
import { useCreateCuisineTag, useDeleteCuisineTag } from '../../api/admin';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { useToast } from '../../components/ui/Toast';
import type { CuisineTag } from '../../types';

export default function AdminCuisineTagsPage() {
  const [newTagName, setNewTagName] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<CuisineTag | null>(null);

  const { data: tags, isLoading } = useCuisineTags();
  const createTag = useCreateCuisineTag();
  const deleteTag = useDeleteCuisineTag();
  const { showToast } = useToast();

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!newTagName.trim()) return;
    try {
      await createTag.mutateAsync({ name: newTagName.trim() });
      showToast('Tag created!', 'success');
      setNewTagName('');
    } catch {
      showToast('Failed to create tag.', 'error');
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteTag.mutateAsync(deleteTarget.id);
      showToast('Tag deleted.', 'success');
      setDeleteTarget(null);
    } catch {
      showToast('Failed to delete tag.', 'error');
    }
  }

  return (
    <div className="max-w-xl">
      <Link to="/admin" className="text-sm text-orange-500 hover:underline">← Admin</Link>
      <h1 className="text-2xl font-bold text-gray-800 mt-1 mb-6">Cuisine Tags</h1>

      <form onSubmit={handleCreate} className="flex gap-3 mb-6">
        <input
          type="text"
          value={newTagName}
          onChange={(e) => setNewTagName(e.target.value)}
          placeholder="New tag name…"
          className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400"
        />
        <button type="submit" disabled={createTag.isPending || !newTagName.trim()} className="px-4 py-2 bg-orange-500 text-white text-sm font-medium rounded-lg hover:bg-orange-600 disabled:opacity-50 transition-colors">
          {createTag.isPending ? 'Adding…' : 'Add Tag'}
        </button>
      </form>

      {isLoading ? <p className="text-gray-500">Loading…</p> : tags?.length === 0 ? (
        <p className="text-gray-500">No tags yet.</p>
      ) : (
        <div className="space-y-2">
          {tags?.map((tag) => (
            <div key={tag.id} className="flex items-center justify-between px-4 py-3 bg-white rounded-xl border border-gray-100">
              <span className="font-medium text-gray-800">{tag.name}</span>
              <button onClick={() => setDeleteTarget(tag)} className="text-sm text-red-500 hover:underline">Delete</button>
            </div>
          ))}
        </div>
      )}

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Delete Tag" message={`Delete tag "${deleteTarget?.name}"?`} isLoading={deleteTag.isPending} />
    </div>
  );
}
