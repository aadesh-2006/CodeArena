import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Clock,
  HardDrive,
  Lightbulb,
  FileText,
  ChevronDown,
  ChevronUp,
  AlertCircle,
  HelpCircle,
  Sparkles,
} from 'lucide-react';
import { problemApi } from '../api/problemApi';
import { PublicProblemDetail } from '../types';
import { DifficultyBadge } from '../components/DifficultyBadge';
import { FormattedContent } from '../components/FormattedContent';
import { ApiError } from '../api/client';

export const ProblemDetailPage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();

  const [problem, setProblem] = useState<PublicProblemDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isNotFound, setIsNotFound] = useState<boolean>(false);
  const [revealedHints, setRevealedHints] = useState<Record<number, boolean>>({});
  const [editorialOpen, setEditorialOpen] = useState<boolean>(false);

  useEffect(() => {
    let isMounted = true;
    if (!slug) {
      setIsNotFound(true);
      setLoading(false);
      return;
    }

    const fetchProblemDetail = async () => {
      setLoading(true);
      setError(null);
      setIsNotFound(false);
      try {
        const res = await problemApi.getProblemBySlug(slug);
        if (isMounted) {
          setProblem(res.data);
        }
      } catch (err: unknown) {
        if (isMounted) {
          if (err instanceof ApiError && err.status === 404) {
            setIsNotFound(true);
          } else {
            const msg = err instanceof Error ? err.message : 'Failed to load problem details.';
            setError(msg);
          }
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    fetchProblemDetail();
    return () => {
      isMounted = false;
    };
  }, [slug]);

  const toggleHint = (index: number) => {
    setRevealedHints((prev) => ({
      ...prev,
      [index]: !prev[index],
    }));
  };

  // 404 State
  if (isNotFound) {
    return (
      <div className="max-w-2xl mx-auto py-16 text-center space-y-4" data-testid="problem-not-found">
        <div className="w-16 h-16 rounded-full bg-gray-900 border border-gray-800 flex items-center justify-center text-gray-400 mx-auto">
          <HelpCircle className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-bold text-white">Problem Not Found</h1>
        <p className="text-sm text-gray-400 leading-relaxed max-w-md mx-auto">
          The requested problem does not exist, or it is currently unpublished or archived.
        </p>
        <div className="pt-4">
          <Link
            to="/problems"
            className="inline-flex items-center space-x-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold transition"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Browse Problem Catalogue</span>
          </Link>
        </div>
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="max-w-3xl mx-auto py-12 space-y-4">
        <div
          role="alert"
          className="p-5 rounded-xl bg-red-900/20 border border-red-500/30 flex items-start space-x-3 text-red-200"
        >
          <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
          <div className="flex-1 space-y-2">
            <h2 className="text-sm font-semibold">Error Loading Problem</h2>
            <p className="text-xs text-red-300">{error}</p>
          </div>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-white text-xs font-semibold transition"
          >
            Retry
          </button>
          <Link
            to="/problems"
            className="px-4 py-2 rounded-lg bg-transparent text-gray-400 hover:text-white text-xs font-medium transition"
          >
            Back to Problems
          </Link>
        </div>
      </div>
    );
  }

  // Loading Skeleton State
  if (loading || !problem) {
    return (
      <div className="max-w-4xl mx-auto space-y-6 animate-pulse" data-testid="detail-skeleton">
        <div className="h-4 bg-gray-800 rounded w-32"></div>
        <div className="space-y-3">
          <div className="h-8 bg-gray-800 rounded w-2/3"></div>
          <div className="flex items-center space-x-3">
            <div className="h-6 bg-gray-800 rounded w-16"></div>
            <div className="h-6 bg-gray-800 rounded w-24"></div>
          </div>
        </div>
        <div className="h-48 bg-gray-900 rounded-xl border border-gray-800"></div>
        <div className="h-32 bg-gray-900 rounded-xl border border-gray-800"></div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Breadcrumb Navigation */}
      <nav aria-label="Breadcrumb" className="flex items-center space-x-2 text-xs text-gray-400">
        <Link
          to="/problems"
          className="inline-flex items-center space-x-1.5 hover:text-blue-400 transition"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Problems</span>
        </Link>
        <span>/</span>
        <span className="text-gray-200 font-medium truncate max-w-xs">{problem.title}</span>
      </nav>

      {/* Problem Header */}
      <div className="border-b border-gray-800 pb-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            {problem.title}
          </h1>

          <div className="flex items-center space-x-2.5 flex-wrap">
            <DifficultyBadge difficulty={problem.difficulty} />
            <span className="px-2.5 py-1 rounded-md text-xs font-mono font-medium bg-gray-800 text-gray-300 border border-gray-700/60 uppercase">
              {problem.problemType.replace('_', ' ')}
            </span>
          </div>
        </div>

        {/* Limits & Metadata Bar */}
        <div className="flex flex-wrap items-center gap-4 text-xs text-gray-400">
          <div className="inline-flex items-center space-x-1.5 bg-gray-900 px-2.5 py-1 rounded-md border border-gray-800">
            <Clock className="w-3.5 h-3.5 text-blue-400" />
            <span>Time Limit:</span>
            <span className="font-mono text-gray-200 font-semibold">{problem.timeLimitMs} ms</span>
          </div>

          <div className="inline-flex items-center space-x-1.5 bg-gray-900 px-2.5 py-1 rounded-md border border-gray-800">
            <HardDrive className="w-3.5 h-3.5 text-emerald-400" />
            <span>Memory Limit:</span>
            <span className="font-mono text-gray-200 font-semibold">{problem.memoryLimitMb} MB</span>
          </div>

          {problem.topics && problem.topics.length > 0 && (
            <div className="flex items-center space-x-1.5 flex-wrap">
              {problem.topics.map((t) => (
                <span
                  key={t.id}
                  className="px-2 py-0.5 rounded text-xs bg-blue-500/10 text-blue-400 border border-blue-500/20"
                >
                  {t.name}
                </span>
              ))}
            </div>
          )}

          {problem.tags && problem.tags.length > 0 && (
            <div className="flex items-center space-x-1.5 flex-wrap">
              {problem.tags.map((tg) => (
                <span
                  key={tg.id}
                  className="px-2 py-0.5 rounded text-xs bg-gray-800 text-gray-400 border border-gray-700/60"
                >
                  #{tg.name}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Problem Statement Section */}
      <section aria-labelledby="statement-heading" className="space-y-3">
        <h2 id="statement-heading" className="text-base font-semibold text-white flex items-center space-x-2">
          <FileText className="w-4 h-4 text-blue-400" />
          <span>Description</span>
        </h2>
        <div className="bg-[#111827] border border-gray-800 rounded-xl p-5 text-gray-300 leading-relaxed text-sm shadow-sm">
          <FormattedContent content={problem.statement} />
        </div>
      </section>

      {/* Input & Output Format Specifications */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <section aria-labelledby="input-format-heading" className="space-y-2.5">
          <h2 id="input-format-heading" className="text-sm font-semibold text-gray-200 uppercase tracking-wider">
            Input Format
          </h2>
          <div className="bg-[#111827] border border-gray-800 rounded-xl p-4 text-xs font-mono text-gray-300">
            <FormattedContent content={problem.inputFormat} />
          </div>
        </section>

        <section aria-labelledby="output-format-heading" className="space-y-2.5">
          <h2 id="output-format-heading" className="text-sm font-semibold text-gray-200 uppercase tracking-wider">
            Output Format
          </h2>
          <div className="bg-[#111827] border border-gray-800 rounded-xl p-4 text-xs font-mono text-gray-300">
            <FormattedContent content={problem.outputFormat} />
          </div>
        </section>
      </div>

      {/* Constraints Section */}
      <section aria-labelledby="constraints-heading" className="space-y-3">
        <h2 id="constraints-heading" className="text-base font-semibold text-white">
          Constraints
        </h2>
        <div className="bg-[#090d16] border border-gray-800 rounded-xl p-4 text-xs font-mono text-gray-300 space-y-1">
          <FormattedContent content={problem.constraintsText} />
        </div>
      </section>

      {/* Examples Section */}
      {problem.examples && problem.examples.length > 0 && (
        <section aria-labelledby="examples-heading" className="space-y-4">
          <h2 id="examples-heading" className="text-base font-semibold text-white">
            Examples
          </h2>

          <div className="space-y-4">
            {problem.examples.map((example, idx) => (
              <div
                key={example.id || idx}
                data-testid={`problem-example-${idx + 1}`}
                className="bg-[#111827] border border-gray-800 rounded-xl p-5 space-y-3 shadow-sm"
              >
                <div className="text-xs font-semibold text-blue-400 uppercase tracking-wider">
                  Example {idx + 1}
                </div>

                <div className="space-y-2 text-xs">
                  <div>
                    <span className="text-gray-400 font-medium block mb-1">Input:</span>
                    <pre className="bg-[#090d16] border border-gray-800 p-2.5 rounded-lg font-mono text-gray-200 overflow-x-auto">
                      <code>{example.inputText}</code>
                    </pre>
                  </div>

                  <div>
                    <span className="text-gray-400 font-medium block mb-1">Output:</span>
                    <pre className="bg-[#090d16] border border-gray-800 p-2.5 rounded-lg font-mono text-emerald-400 overflow-x-auto">
                      <code>{example.outputText}</code>
                    </pre>
                  </div>

                  {example.explanation && (
                    <div className="pt-1 text-gray-300 text-xs">
                      <span className="text-gray-400 font-medium">Explanation: </span>
                      <span>{example.explanation}</span>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Hints Section */}
      {problem.hints && problem.hints.length > 0 && (
        <section aria-labelledby="hints-heading" className="space-y-3">
          <h2 id="hints-heading" className="text-base font-semibold text-white flex items-center space-x-2">
            <Lightbulb className="w-4 h-4 text-amber-400" />
            <span>Hints</span>
          </h2>

          <div className="space-y-2.5">
            {problem.hints.map((hint, idx) => {
              const isRevealed = Boolean(revealedHints[idx]);
              return (
                <div
                  key={hint.id || idx}
                  className="bg-[#111827] border border-gray-800 rounded-xl overflow-hidden shadow-sm"
                >
                  <button
                    type="button"
                    onClick={() => toggleHint(idx)}
                    aria-expanded={isRevealed}
                    className="w-full flex items-center justify-between p-4 text-left text-xs font-semibold text-gray-200 hover:bg-gray-800/40 transition"
                  >
                    <div className="flex items-center space-x-2">
                      <span className="text-amber-400">Hint {idx + 1}</span>
                      <span className="text-gray-400 font-normal">
                        {isRevealed ? '(click to hide)' : '(click to reveal)'}
                      </span>
                    </div>
                    {isRevealed ? (
                      <ChevronUp className="w-4 h-4 text-gray-400" />
                    ) : (
                      <ChevronDown className="w-4 h-4 text-gray-400" />
                    )}
                  </button>

                  {isRevealed && (
                    <div
                      data-testid={`hint-content-${idx + 1}`}
                      className="px-4 pb-4 pt-1 text-xs text-gray-300 border-t border-gray-800/80 leading-relaxed"
                    >
                      <FormattedContent content={hint.hintText} />
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </section>
      )}

      {/* Editorial Section (ONLY when editorial content is returned) */}
      {problem.editorial && problem.editorial.trim() && (
        <section
          aria-labelledby="editorial-heading"
          data-testid="editorial-section"
          className="bg-[#111827] border border-blue-900/40 rounded-xl overflow-hidden shadow-sm"
        >
          <button
            type="button"
            onClick={() => setEditorialOpen((prev) => !prev)}
            aria-expanded={editorialOpen}
            className="w-full flex items-center justify-between p-4 bg-gradient-to-r from-blue-900/20 to-transparent hover:bg-blue-900/30 text-left transition"
          >
            <div className="flex items-center space-x-2.5">
              <Sparkles className="w-4 h-4 text-blue-400" />
              <h2 id="editorial-heading" className="text-sm font-semibold text-white">
                Official Editorial & Approach
              </h2>
            </div>
            {editorialOpen ? (
              <ChevronUp className="w-4 h-4 text-blue-400" />
            ) : (
              <ChevronDown className="w-4 h-4 text-blue-400" />
            )}
          </button>

          {editorialOpen && (
            <div
              data-testid="editorial-content"
              className="p-5 border-t border-blue-900/30 bg-[#0d1322] text-sm text-gray-300 leading-relaxed"
            >
              <FormattedContent content={problem.editorial} />
            </div>
          )}
        </section>
      )}
    </div>
  );
};
