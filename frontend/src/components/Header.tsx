import React from 'react';
import { Terminal, Code2 } from 'lucide-react';

interface HeaderProps {
  activeTab?: string;
  onNavigate?: (tab: string) => void;
}

export const Header: React.FC<HeaderProps> = ({ activeTab = 'problems', onNavigate }) => {
  const navItems = [
    { id: 'problems', label: 'Problems' },
    { id: 'explore', label: 'Explore' },
    { id: 'submissions', label: 'Submissions' },
    { id: 'dashboard', label: 'Dashboard' },
  ];

  return (
    <header className="border-b border-gray-800 bg-[#0e1424]/90 backdrop-blur sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-8">
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => onNavigate?.('problems')}>
            <div className="w-9 h-9 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400">
              <Code2 className="w-5 h-5" />
            </div>
            <div className="flex items-baseline space-x-1">
              <span className="text-xl font-bold tracking-tight text-white">CodeArena</span>
              <span className="text-xs font-semibold px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
                v0.1.0
              </span>
            </div>
          </div>

          <nav className="hidden md:flex space-x-1" aria-label="Main Navigation">
            {navItems.map((item) => (
              <button
                key={item.id}
                onClick={() => onNavigate?.(item.id)}
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

        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2 text-xs text-gray-400 bg-gray-900 border border-gray-800 px-3 py-1.5 rounded-full">
            <span className="inline-block w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span>API Gateway Active</span>
          </div>
          <button
            onClick={() => onNavigate?.('profile')}
            className="flex items-center space-x-2 text-sm text-gray-300 hover:text-white bg-gray-800/80 hover:bg-gray-800 px-3.5 py-1.5 rounded-lg border border-gray-700/60 transition"
          >
            <Terminal className="w-4 h-4 text-blue-400" />
            <span>Developer</span>
          </button>
        </div>
      </div>
    </header>
  );
};
