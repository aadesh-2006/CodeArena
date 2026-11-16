import React from 'react';
import { CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';
import { HealthStatus } from '../types';

interface StatusCardProps {
  status: HealthStatus | null;
  loading: boolean;
  error: string | null;
  onRefresh: () => void;
}

export const StatusCard: React.FC<StatusCardProps> = ({ status, loading, error, onRefresh }) => {
  return (
    <div className="bg-[#111827] border border-gray-800 rounded-xl p-6 shadow-xl">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-white tracking-tight flex items-center space-x-2">
          <span>Backend System Status</span>
        </h2>
        <button
          onClick={onRefresh}
          disabled={loading}
          className="flex items-center space-x-1.5 text-xs text-blue-400 hover:text-blue-300 bg-blue-500/10 hover:bg-blue-500/20 px-2.5 py-1.5 rounded border border-blue-500/20 transition disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Check Health</span>
        </button>
      </div>

      {loading && !status && (
        <div className="text-sm text-gray-400 py-4 flex items-center space-x-2">
          <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <span>Connecting to backend at /api/health...</span>
        </div>
      )}

      {error && (
        <div className="bg-red-500/10 border border-red-500/30 rounded-lg p-4 flex items-start space-x-3 text-sm text-red-300">
          <AlertCircle className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
          <div>
            <div className="font-semibold text-red-200">Backend Disconnected</div>
            <div className="text-xs text-red-400 mt-1">{error}</div>
            <div className="text-xs text-gray-400 mt-2">
              Start the backend via <code className="text-gray-300 bg-gray-900 px-1.5 py-0.5 rounded">mvn spring-boot:run</code> or Docker Compose.
            </div>
          </div>
        </div>
      )}

      {status && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-gray-900/60 border border-gray-800/80 rounded-lg p-3">
            <span className="text-xs text-gray-400 block mb-1">Service Status</span>
            <div className="flex items-center space-x-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
              <span className="text-sm font-semibold text-emerald-400">{status.status}</span>
            </div>
          </div>

          <div className="bg-gray-900/60 border border-gray-800/80 rounded-lg p-3">
            <span className="text-xs text-gray-400 block mb-1">Service Name</span>
            <span className="text-sm font-mono text-gray-200">{status.service}</span>
          </div>

          <div className="bg-gray-900/60 border border-gray-800/80 rounded-lg p-3">
            <span className="text-xs text-gray-400 block mb-1">Version</span>
            <span className="text-sm font-mono text-blue-400">{status.version}</span>
          </div>
        </div>
      )}
    </div>
  );
};
