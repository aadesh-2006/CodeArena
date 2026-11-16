import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { User as UserIcon, Mail, Shield, Calendar, Edit3, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';
import { ApiError } from '../api/client';

export const ProfilePage: React.FC = () => {
  const { user, updateProfile } = useAuth();

  const [username, setUsername] = useState(user?.username || '');
  const [displayName, setDisplayName] = useState(user?.displayName || '');
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  if (!user) {
    return null;
  }

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSuccessMsg(null);
    setErrorMsg(null);
    setIsSaving(true);

    try {
      await updateProfile({
        username: username.trim(),
        displayName: displayName.trim() || undefined,
      });
      setSuccessMsg('Profile updated successfully.');
      setIsEditing(false);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setErrorMsg(err.message);
      } else if (err instanceof Error) {
        setErrorMsg(err.message);
      } else {
        setErrorMsg('Failed to update profile.');
      }
    } finally {
      setIsSaving(false);
    }
  };

  const formattedDate = user.createdAt
    ? new Date(user.createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      })
    : 'N/A';

  return (
    <div className="max-w-3xl mx-auto my-8 space-y-6">
      <div className="bg-[#111827] border border-gray-800 rounded-2xl p-6 sm:p-8 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-gray-800 gap-4">
          <div className="flex items-center space-x-4">
            <div className="w-16 h-16 rounded-2xl bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400 font-bold text-2xl">
              {(user.displayName || user.username).charAt(0).toUpperCase()}
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h2 className="text-2xl font-bold text-white">{user.displayName || user.username}</h2>
                <span className="text-xs px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20 font-medium">
                  {user.role}
                </span>
              </div>
              <p className="text-sm text-gray-400">{`@${user.username}`}</p>
            </div>
          </div>

          <button
            type="button"
            onClick={() => {
              setIsEditing(!isEditing);
              setUsername(user.username);
              setDisplayName(user.displayName || '');
              setErrorMsg(null);
              setSuccessMsg(null);
            }}
            className="inline-flex items-center space-x-2 px-4 py-2 rounded-lg border border-gray-700 bg-gray-800/80 hover:bg-gray-800 text-sm font-medium text-gray-200 transition"
          >
            <Edit3 className="w-4 h-4 text-gray-400" />
            <span>{isEditing ? 'Cancel Editing' : 'Edit Profile'}</span>
          </button>
        </div>

        {successMsg && (
          <div className="mt-6 p-4 rounded-lg bg-emerald-900/20 border border-emerald-800/40 text-emerald-300 text-sm flex items-center space-x-3">
            <CheckCircle className="w-5 h-5 flex-shrink-0 text-emerald-400" />
            <span>{successMsg}</span>
          </div>
        )}

        {errorMsg && (
          <div className="mt-6 p-4 rounded-lg bg-red-900/20 border border-red-800/40 text-red-300 text-sm flex items-center space-x-3">
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-400" />
            <span>{errorMsg}</span>
          </div>
        )}

        {isEditing ? (
          <form onSubmit={handleUpdate} className="mt-6 space-y-4">
            <div>
              <label htmlFor="edit-username" className="block text-xs font-semibold uppercase tracking-wider text-gray-300 mb-1.5">
                Username
              </label>
              <input
                id="edit-username"
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-3.5 py-2 bg-gray-900 border border-gray-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label htmlFor="edit-displayName" className="block text-xs font-semibold uppercase tracking-wider text-gray-300 mb-1.5">
                Display Name (Optional)
              </label>
              <input
                id="edit-displayName"
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="Full Name or Nickname"
                className="w-full px-3.5 py-2 bg-gray-900 border border-gray-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="pt-2 flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => setIsEditing(false)}
                className="px-4 py-2 rounded-lg border border-gray-700 text-sm text-gray-300 hover:bg-gray-800"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSaving}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-500 disabled:bg-blue-800 text-white text-sm font-medium rounded-lg flex items-center space-x-2"
              >
                {isSaving ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Saving...</span>
                  </>
                ) : (
                  <span>Save Changes</span>
                )}
              </button>
            </div>
          </form>
        ) : (
          <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="p-4 rounded-xl bg-gray-900/60 border border-gray-800 flex items-start space-x-3">
              <UserIcon className="w-5 h-5 text-blue-400 mt-0.5" />
              <div>
                <span className="text-xs text-gray-400 block font-medium">Username</span>
                <span className="text-sm font-semibold text-white">{`@${user.username}`}</span>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-gray-900/60 border border-gray-800 flex items-start space-x-3">
              <Mail className="w-5 h-5 text-indigo-400 mt-0.5" />
              <div>
                <span className="text-xs text-gray-400 block font-medium">Email Address</span>
                <span className="text-sm font-semibold text-white">{user.email}</span>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-gray-900/60 border border-gray-800 flex items-start space-x-3">
              <Shield className="w-5 h-5 text-emerald-400 mt-0.5" />
              <div>
                <span className="text-xs text-gray-400 block font-medium">Account Role</span>
                <span className="text-sm font-semibold text-white">{user.role}</span>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-gray-900/60 border border-gray-800 flex items-start space-x-3">
              <Calendar className="w-5 h-5 text-amber-400 mt-0.5" />
              <div>
                <span className="text-xs text-gray-400 block font-medium">Member Since</span>
                <span className="text-sm font-semibold text-white">{formattedDate}</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
