import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useProfile, useUpdateProfile } from '../api/users';
import { profileSchema, type ProfileFormData } from '../lib/validators';
import { useToast } from '../components/ui/Toast';
import { Skeleton } from '../components/ui/Skeleton';

export default function ProfilePage() {
  const [isEditing, setIsEditing] = useState(false);
  const { data: profile, isLoading, isError } = useProfile();
  const updateProfile = useUpdateProfile();
  const { showToast } = useToast();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
  });

  function startEditing() {
    if (profile) {
      reset({
        name: profile.name,
        deliveryAddress: profile.profile?.deliveryAddress ?? '',
        dietaryPreferences: profile.profile?.dietaryPreferences ?? [],
      });
    }
    setIsEditing(true);
  }

  async function onSubmit(data: ProfileFormData) {
    try {
      await updateProfile.mutateAsync({
        name: data.name,
        deliveryAddress: data.deliveryAddress,
        dietaryPreferences: data.dietaryPreferences,
      });
      showToast('Profile updated successfully!', 'success');
      setIsEditing(false);
    } catch {
      showToast('Failed to update profile. Please try again.', 'error');
    }
  }

  if (isLoading) {
    return (
      <div className="max-w-xl mx-auto space-y-4">
        <Skeleton className="h-8 w-1/3" />
        <Skeleton className="h-4 w-1/2" />
        <Skeleton className="h-4 w-2/3" />
        <Skeleton className="h-4 w-1/2" />
      </div>
    );
  }

  if (isError || !profile) {
    return <p className="text-red-500 text-center py-8">Failed to load profile.</p>;
  }

  return (
    <div className="max-w-xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Profile</h1>
        {!isEditing && (
          <button onClick={startEditing} className="px-4 py-2 text-sm font-medium text-orange-500 border border-orange-300 rounded-lg hover:bg-orange-50 transition-colors">
            Edit
          </button>
        )}
      </div>

      {isEditing ? (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 bg-white rounded-xl border border-gray-100 p-6">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
            <input id="name" type="text" {...register('name')} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400" />
            {errors.name && <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>}
          </div>
          <div>
            <label htmlFor="deliveryAddress" className="block text-sm font-medium text-gray-700 mb-1">Delivery Address</label>
            <input id="deliveryAddress" type="text" {...register('deliveryAddress')} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-400" />
            {errors.deliveryAddress && <p className="mt-1 text-sm text-red-600">{errors.deliveryAddress.message}</p>}
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setIsEditing(false)} className="flex-1 py-2 border border-gray-300 text-gray-700 rounded-lg hover:border-gray-400 transition-colors">Cancel</button>
            <button type="submit" disabled={isSubmitting || updateProfile.isPending} className="flex-1 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:opacity-50 transition-colors">
              {updateProfile.isPending ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </form>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 p-6 space-y-4">
          <div>
            <p className="text-xs font-medium text-gray-400 uppercase tracking-wide">Name</p>
            <p className="mt-1 text-gray-800">{profile.name}</p>
          </div>
          <div>
            <p className="text-xs font-medium text-gray-400 uppercase tracking-wide">Email</p>
            <p className="mt-1 text-gray-800">{profile.email}</p>
          </div>
          <div>
            <p className="text-xs font-medium text-gray-400 uppercase tracking-wide">Roles</p>
            <div className="flex gap-1 mt-1">
              {profile.roles.map((role) => (
                <span key={role} className="px-2 py-0.5 bg-orange-50 text-orange-600 text-xs font-medium rounded-full">{role}</span>
              ))}
            </div>
          </div>
          {profile.profile && (
            <>
              <div>
                <p className="text-xs font-medium text-gray-400 uppercase tracking-wide">Delivery Address</p>
                <p className="mt-1 text-gray-800">{profile.profile.deliveryAddress || '—'}</p>
              </div>
              {profile.profile.dietaryPreferences?.length > 0 && (
                <div>
                  <p className="text-xs font-medium text-gray-400 uppercase tracking-wide">Dietary Preferences</p>
                  <div className="flex flex-wrap gap-1 mt-1">
                    {profile.profile.dietaryPreferences.map((pref) => (
                      <span key={pref} className="px-2 py-0.5 bg-green-50 text-green-700 text-xs font-medium rounded-full">{pref}</span>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}
