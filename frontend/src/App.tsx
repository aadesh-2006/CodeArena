import React, { useEffect, useState } from 'react';
import { Header } from './components/Header';
import { Footer } from './components/Footer';
import { StatusCard } from './components/StatusCard';
import { HealthStatus, ApiResponse } from './types';
import { BookOpen, Layers, Terminal, Cpu, Database } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('problems');
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch('/api/health');
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      const json: ApiResponse<HealthStatus> = await res.json();
      setHealth(json.data);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to connect to backend';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div className="min-h-screen flex flex-col bg-[#0b0f19] text-gray-100">
      <Header activeTab={activeTab} onNavigate={(tab) => setActiveTab(tab)} />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Hero Banner */}
        <section className="bg-gradient-to-r from-blue-900/20 via-gray-900/40 to-slate-900/20 border border-blue-900/30 rounded-2xl p-8 relative overflow-hidden">
          <div className="max-w-3xl space-y-4">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-blue-500/10 border border-blue-500/20 text-xs font-medium text-blue-400">
              <Terminal className="w-3.5 h-3.5" />
              <span>Release 1.0 Foundation — DSA Track</span>
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white">
              Engineered for DSA Mastery & Technical Rigor
            </h1>
            <p className="text-gray-400 text-base leading-relaxed">
              CodeArena is a modular online judge and practice platform. Solve canonical algorithmic problems,
              evaluate code through an isolated sandbox runner, and track deep performance metrics.
            </p>
          </div>
        </section>

        {/* Backend Status Card */}
        <StatusCard
          status={health}
          loading={loading}
          error={error}
          onRefresh={fetchHealth}
        />

        {/* Architecture Overview Cards */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-[#111827] border border-gray-800 rounded-xl p-6 space-y-3">
            <div className="w-10 h-10 rounded-lg bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
              <Cpu className="w-5 h-5" />
            </div>
            <h3 className="text-base font-semibold text-white">Modular Spring Boot Core</h3>
            <p className="text-sm text-gray-400 leading-normal">
              Java 21 LTS modular monolith running Spring Boot 3.4.x, clean separation of concerns, DTO boundaries, and OpenAPI docs.
            </p>
          </div>

          <div className="bg-[#111827] border border-gray-800 rounded-xl p-6 space-y-3">
            <div className="w-10 h-10 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
              <Database className="w-5 h-5" />
            </div>
            <h3 className="text-base font-semibold text-white">PostgreSQL & Flyway Migrations</h3>
            <p className="text-sm text-gray-400 leading-normal">
              Strict relational persistence, reproducible migrations via Flyway, and confidential hidden test case storage.
            </p>
          </div>

          <div className="bg-[#111827] border border-gray-800 rounded-xl p-6 space-y-3">
            <div className="w-10 h-10 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
              <Layers className="w-5 h-5" />
            </div>
            <h3 className="text-base font-semibold text-white">Isolated Execution Boundary</h3>
            <p className="text-sm text-gray-400 leading-normal">
              Clean adapter interface isolating untrusted user code execution from host services with strict limits and timeouts.
            </p>
          </div>
        </section>

        {/* Milestones Roadmap Preview */}
        <section className="bg-[#111827]/70 border border-gray-800 rounded-xl p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-base font-semibold text-white flex items-center space-x-2">
              <BookOpen className="w-4 h-4 text-blue-400" />
              <span>Milestone Implementation Plan</span>
            </h3>
            <span className="text-xs text-blue-400 font-mono">Milestone 1 Active</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
            <div className="p-3 rounded-lg bg-blue-500/10 border border-blue-500/30">
              <span className="text-blue-400 font-bold block mb-1">M1: Foundation</span>
              <span className="text-gray-300">Backend + Frontend + Schema + CI</span>
            </div>
            <div className="p-3 rounded-lg bg-gray-900 border border-gray-800">
              <span className="text-gray-400 font-bold block mb-1">M2: Auth & 25 Problems</span>
              <span className="text-gray-500">JWT + Admin Management + Seed Bank</span>
            </div>
            <div className="p-3 rounded-lg bg-gray-900 border border-gray-800">
              <span className="text-gray-400 font-bold block mb-1">M3-M5: Editor & Runner</span>
              <span className="text-gray-500">Monaco + Run/Submit Sandbox Verifier</span>
            </div>
            <div className="p-3 rounded-lg bg-gray-900 border border-gray-800">
              <span className="text-gray-400 font-bold block mb-1">M6-M7: Dashboard & Quality</span>
              <span className="text-gray-500">Analytics + History + Full Tests</span>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
};

export default App;
