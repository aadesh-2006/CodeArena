import React from 'react';
import { ShieldCheck, GitBranch } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="border-t border-gray-800 bg-[#0b0f19] py-8 mt-auto text-sm text-gray-500">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center space-x-4">
          <span className="font-semibold text-gray-400">CodeArena Engineering Platform</span>
          <span className="text-gray-700">|</span>
          <span>DSA Practice Track</span>
        </div>

        <div className="flex items-center space-x-6 text-xs text-gray-400">
          <div className="flex items-center space-x-1.5">
            <GitBranch className="w-3.5 h-3.5 text-gray-500" />
            <span>Milestone 1 — Architecture Foundation</span>
          </div>
          <div className="flex items-center space-x-1.5">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
            <span>Sandboxed Boundary Architecture</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
