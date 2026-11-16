import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Code2, LogOut, LogIn, UserPlus } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface HeaderProps {
  activeTab?: string;
  onNavigate?: (tab: string) => void;
}

export const Header: React.FC<HeaderProps> = ({ activeTab = 'problems', onNavigate }) => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();

  const navItems = [
    { id: 'problems', label: 'Problems', path: '/problems' },
    { id: 'explore', label: 'Explore', path: '/' },
    { id: 'submissions', label: 'Submissions', path: '/' },
    { id: 'dashboard', label: 'Dashboard', path: '/' },
  ];

  const handleNavClick = (id: string, path: string) => {
    if (onNavigate) {
      onNavigate(id);
    }
    navigate(path);
  };

  return (
    <header className="border-b border-gray-800 bg-[#0e1424]/90 backdrop-blur sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-8">
          <Link to="/" className="flex items-center space-x-3 cursor-pointer">
            <div className="w-9 h-9 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400">
              <Code2 className="w-5 h-5" />
            </div>
            <div className="flex items-baseline space-x-1">
              <span className="text-xl font-bold tracking-tight text-white">CodeArena</span>
              <span className="text-xs font-semibold px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
                v0.1.0
              </span>
            </div>
          </Link>

          <nav className="hidden md:flex space-x-1" aria-label="Main Navigation">
            {navItems.map((item) => (
              <button
                key={item.id}
                onClick={() => handleNavClick(item.id, item.path)}
                className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                  activeTab === item.id
                    ? 'bg-gray-800 text-white font-semibold'
                    : 'text-gray-400 hover:text-gray-200 hover:bg-gray-800/50'
                }`}
              >
                {item.label}
              </button>
            ))}
          </nav>
        </div>

        <div className="flex items-center space-x-3">
          <div className="hidden sm:flex items-center space-x-2 text-xs text-gray-400 bg-gray-900 border border-gray-800 px-3 py-1.5 rounded-full">
            <span className="inline-block w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span>API Gateway Active</span>
          </div>

          {isAuthenticated && user ? (
            <div className="flex items-center space-x-2">
              <Link
                to="/profile"
                className="flex items-center space-x-2 text-sm text-gray-200 hover:text-white bg-gray-800/80 hover:bg-gray-800 px-3 py-1.5 rounded-lg border border-gray-700/60 transition"
              >
                <div className="w-5 h-5 rounded-full bg-blue-600/30 text-blue-400 flex items-center justify-center text-xs font-semibold">
                  {(user.displayName || user.username).charAt(0).toUpperCase()}
                </div>
                <span className="font-medium max-w-[120px] truncate">{user.displayName || user.username}</span>
              </Link>
              <button
                onClick={logout}
                aria-label="Sign Out"
                title="Sign Out"
                className="p-1.5 text-gray-400 hover:text-red-400 hover:bg-gray-800/80 rounded-lg transition"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center space-x-2">
              <Link
                to="/login"
                className="inline-flex items-center space-x-1.5 text-sm text-gray-300 hover:text-white bg-gray-800/70 hover:bg-gray-800 px-3 py-1.5 rounded-lg border border-gray-700/60 transition"
              >
                <LogIn className="w-3.5 h-3.5 text-gray-400" />
                <span>Sign In</span>
              </Link>
              <Link
                to="/register"
                className="inline-flex items-center space-x-1.5 text-sm text-white bg-blue-600 hover:bg-blue-500 px-3 py-1.5 rounded-lg font-medium shadow-sm transition"
              >
                <UserPlus className="w-3.5 h-3.5" />
                <span>Register</span>
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
