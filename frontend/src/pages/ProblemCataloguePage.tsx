import React, { useEffect, useState, useTransition } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  Search,
  RotateCcw,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  Filter,
  ArrowUpDown,
  BookOpen,
  Tag as TagIcon,
  Folder,
} from 'lucide-react';
import { problemApi } from '../api/problemApi';
import {
  Difficulty,
  ProblemSummary,
  TagSummary,
  TopicSummary,
  PageResponse,
} from '../types';
import { DifficultyBadge } from '../components/DifficultyBadge';

export const ProblemCataloguePage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [, startTransition] = useTransition();

  // URL state extraction
  const searchParam = searchParams.get('search') || '';
  const rawDifficulty = searchParams.get('difficulty') || '';
  const difficultyParam: Difficulty | '' =
    rawDifficulty === 'EASY' || rawDifficulty === 'MEDIUM' || rawDifficulty === 'HARD'
      ? rawDifficulty
      : '';
  const topicParam = searchParams.get('topic') || '';
  const tagParam = searchParams.get('tag') || '';
  const sortByParam = searchParams.get('sortBy') || 'createdAt';
  const sortDirectionParam = searchParams.get('sortDirection') || 'DESC';
  const pageParam = parseInt(searchParams.get('page') || '0', 10);

  // Local state
  const [searchInput, setSearchInput] = useState<string>(searchParam);
  const [topics, setTopics] = useState<TopicSummary[]>([]);
  const [tags, setTags] = useState<TagSummary[]>([]);
  const [problemPage, setProblemPage] = useState<PageResponse<ProblemSummary> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Load available discovery metadata (topics & tags) once on mount
  useEffect(() => {
    let isMounted = true;
    const fetchMetadata = async () => {
      try {
        const [topicsRes, tagsRes] = await Promise.all([
          problemApi.getTopics(),
          problemApi.getTags(),
        ]);
        if (isMounted) {
          setTopics(topicsRes.data || []);
          setTags(tagsRes.data || []);
        }
      } catch (err: unknown) {
        // Metadata failure does not block problem listing
        console.warn('Failed to load discovery categories:', err);
      }
    };
    fetchMetadata();
    return () => {
      isMounted = false;
    };
  }, []);

  // Synchronize local search input if URL changes externally (e.g. back button)
  useEffect(() => {
    setSearchInput(searchParam);
  }, [searchParam]);

  // Debounced search input handler syncing with URL searchParam
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchInput !== searchParam) {
        updateQueryParam('search', searchInput.trim(), true);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [searchInput]);

  // Fetch problems on filter/pagination changes
  useEffect(() => {
    let isMounted = true;
    const fetchProblems = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await problemApi.getProblems({
          page: pageParam >= 0 ? pageParam : 0,
          size: 20,
          search: searchParam || undefined,
          difficulty: difficultyParam || undefined,
          topic: topicParam || undefined,
          tag: tagParam || undefined,
          sortBy: sortByParam as 'title' | 'difficulty' | 'createdAt',
          sortDirection: sortDirectionParam as 'ASC' | 'DESC',
        });
        if (isMounted) {
          setProblemPage(res.data);
        }
      } catch (err: unknown) {
        if (isMounted) {
          const msg = err instanceof Error ? err.message : 'Failed to load problems.';
          setError(msg);
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    fetchProblems();
    return () => {
      isMounted = false;
    };
  }, [
    searchParam,
    difficultyParam,
    topicParam,
    tagParam,
    sortByParam,
    sortDirectionParam,
    pageParam,
  ]);

  const updateQueryParam = (key: string, value: string, resetPage = false) => {
    startTransition(() => {
      const nextParams = new URLSearchParams(searchParams);
      if (value) {
        nextParams.set(key, value);
      } else {
        nextParams.delete(key);
      }
      if (resetPage) {
        nextParams.delete('page');
      }
      setSearchParams(nextParams);
    });
  };

  const handleDifficultyClick = (diff: Difficulty | '') => {
    updateQueryParam('difficulty', diff, true);
  };

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    const [sortBy, sortDirection] = value.split(':');
    startTransition(() => {
      const nextParams = new URLSearchParams(searchParams);
      nextParams.set('sortBy', sortBy);
      nextParams.set('sortDirection', sortDirection);
      nextParams.delete('page');
      setSearchParams(nextParams);
    });
  };

  const handleClearFilters = () => {
    setSearchInput('');
    setSearchParams(new URLSearchParams());
  };

  const handlePageChange = (newPage: number) => {
    if (newPage < 0 || (problemPage && newPage >= problemPage.totalPages)) {
      return;
    }
    updateQueryParam('page', newPage.toString(), false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const hasActiveFilters = Boolean(
    searchParam || difficultyParam || topicParam || tagParam
  );

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-gray-800 pb-5">
        <div>
          <div className="inline-flex items-center space-x-2 text-xs font-medium text-blue-400 mb-1.5">
            <BookOpen className="w-3.5 h-3.5" />
            <span>Problem Discovery</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Problems
          </h1>
          <p className="text-sm text-gray-400 mt-1">
            Curated algorithmic problems covering foundational and advanced Data Structures & Algorithms.
          </p>
        </div>

        {problemPage && (
          <div className="flex items-center space-x-2 text-xs text-gray-400 self-start sm:self-auto bg-gray-900/80 px-3 py-1.5 rounded-lg border border-gray-800">
            <span className="font-semibold text-gray-200">{problemPage.totalElements}</span>
            <span>Total Available</span>
          </div>
        )}
      </div>

      {/* Filter and Search Toolbar */}
      <div className="bg-[#111827] border border-gray-800 rounded-xl p-4 sm:p-5 space-y-4 shadow-sm">
        {/* Top Controls: Search and Sorters */}
        <div className="flex flex-col lg:flex-row gap-3 items-stretch lg:items-center justify-between">
          {/* Search Box */}
          <div className="relative flex-1 max-w-md">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <Search className="w-4 h-4" />
            </div>
            <input
              type="text"
              role="searchbox"
              aria-label="Search problems by title"
              placeholder="Search problems by title..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="w-full bg-[#0b0f19] text-gray-100 placeholder-gray-500 text-sm pl-9 pr-8 py-2 rounded-lg border border-gray-700/80 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 transition"
            />
            {searchInput && (
              <button
                type="button"
                onClick={() => setSearchInput('')}
                aria-label="Clear search"
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-200 text-xs"
              >
                ✕
              </button>
            )}
          </div>

          {/* Category Dropdowns & Sorter */}
          <div className="flex flex-wrap sm:flex-nowrap items-center gap-2.5">
            {/* Topic Filter */}
            <div className="relative min-w-[140px] flex-1 sm:flex-initial">
              <select
                aria-label="Filter by Topic"
                value={topicParam}
                onChange={(e) => updateQueryParam('topic', e.target.value, true)}
                className="w-full bg-[#0b0f19] text-gray-200 text-xs font-medium py-2 px-3 pr-8 rounded-lg border border-gray-700/80 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 appearance-none transition"
              >
                <option value="">All Topics</option>
                {topics.map((t) => (
                  <option key={t.id} value={t.slug}>
                    {t.name}
                  </option>
                ))}
              </select>
              <Folder className="w-3.5 h-3.5 text-gray-500 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>

            {/* Tag Filter */}
            <div className="relative min-w-[130px] flex-1 sm:flex-initial">
              <select
                aria-label="Filter by Tag"
                value={tagParam}
                onChange={(e) => updateQueryParam('tag', e.target.value, true)}
                className="w-full bg-[#0b0f19] text-gray-200 text-xs font-medium py-2 px-3 pr-8 rounded-lg border border-gray-700/80 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 appearance-none transition"
              >
                <option value="">All Tags</option>
                {tags.map((tg) => (
                  <option key={tg.id} value={tg.slug}>
                    {tg.name}
                  </option>
                ))}
              </select>
              <TagIcon className="w-3.5 h-3.5 text-gray-500 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>

            {/* Sort Dropdown */}
            <div className="relative min-w-[160px] flex-1 sm:flex-initial">
              <select
                aria-label="Sort problems"
                value={`${sortByParam}:${sortDirectionParam}`}
                onChange={handleSortChange}
                className="w-full bg-[#0b0f19] text-gray-200 text-xs font-medium py-2 px-3 pr-8 rounded-lg border border-gray-700/80 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 appearance-none transition"
              >
                <option value="createdAt:DESC">Newest First</option>
                <option value="createdAt:ASC">Oldest First</option>
                <option value="title:ASC">Title (A → Z)</option>
                <option value="title:DESC">Title (Z → A)</option>
                <option value="difficulty:ASC">Difficulty (Easy → Hard)</option>
                <option value="difficulty:DESC">Difficulty (Hard → Easy)</option>
              </select>
              <ArrowUpDown className="w-3.5 h-3.5 text-gray-500 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>
        </div>

        {/* Difficulty Pill Selection & Active Filters Row */}
        <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-gray-800/80">
          <div className="flex items-center space-x-1.5" role="group" aria-label="Filter by difficulty">
            <span className="text-xs text-gray-400 font-medium mr-1.5 hidden sm:inline">
              Difficulty:
            </span>
            <button
              type="button"
              onClick={() => handleDifficultyClick('')}
              className={`px-2.5 py-1 rounded-md text-xs font-medium transition ${
                difficultyParam === ''
                  ? 'bg-blue-600 text-white font-semibold'
                  : 'bg-gray-800/80 text-gray-300 hover:text-white hover:bg-gray-800'
              }`}
            >
              All
            </button>
            <button
              type="button"
              onClick={() => handleDifficultyClick('EASY')}
              className={`px-2.5 py-1 rounded-md text-xs font-medium transition ${
                difficultyParam === 'EASY'
                  ? 'bg-emerald-600 text-white font-semibold'
                  : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 hover:bg-emerald-500/20'
              }`}
            >
              Easy
            </button>
            <button
              type="button"
              onClick={() => handleDifficultyClick('MEDIUM')}
              className={`px-2.5 py-1 rounded-md text-xs font-medium transition ${
                difficultyParam === 'MEDIUM'
                  ? 'bg-amber-600 text-white font-semibold'
                  : 'bg-amber-500/10 text-amber-400 border border-amber-500/20 hover:bg-amber-500/20'
              }`}
            >
              Medium
            </button>
            <button
              type="button"
              onClick={() => handleDifficultyClick('HARD')}
              className={`px-2.5 py-1 rounded-md text-xs font-medium transition ${
                difficultyParam === 'HARD'
                  ? 'bg-rose-600 text-white font-semibold'
                  : 'bg-rose-500/10 text-rose-400 border border-rose-500/20 hover:bg-rose-500/20'
              }`}
            >
              Hard
            </button>
          </div>

          {hasActiveFilters && (
            <button
              type="button"
              onClick={handleClearFilters}
              className="inline-flex items-center space-x-1.5 text-xs text-blue-400 hover:text-blue-300 font-medium transition"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset Filters</span>
            </button>
          )}
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div
          role="alert"
          className="p-4 rounded-xl bg-red-900/20 border border-red-500/30 flex items-center justify-between text-sm text-red-300"
        >
          <div className="flex items-center space-x-3">
            <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0" />
            <span>{error}</span>
          </div>
          <button
            onClick={() => updateQueryParam('page', pageParam.toString(), false)}
            className="text-xs font-semibold px-3 py-1 rounded bg-red-500/20 hover:bg-red-500/30 text-red-200 transition"
          >
            Retry
          </button>
        </div>
      )}

      {/* Problem Table / Content View */}
      <div className="bg-[#111827] border border-gray-800 rounded-xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="divide-y divide-gray-800/80 p-6 space-y-4" data-testid="catalogue-skeleton">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="pt-4 first:pt-0 flex items-center justify-between animate-pulse">
                <div className="space-y-2 flex-1 max-w-md">
                  <div className="h-4 bg-gray-800 rounded w-3/4"></div>
                  <div className="h-3 bg-gray-900 rounded w-1/2"></div>
                </div>
                <div className="h-5 bg-gray-800 rounded w-16"></div>
              </div>
            ))}
          </div>
        ) : problemPage && problemPage.content.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-gray-300" aria-label="Problems Catalog">
              <thead className="bg-[#0b0f19] text-xs font-semibold uppercase tracking-wider text-gray-400 border-b border-gray-800">
                <tr>
                  <th scope="col" className="px-5 py-3.5">
                    Title
                  </th>
                  <th scope="col" className="px-5 py-3.5">
                    Difficulty
                  </th>
                  <th scope="col" className="px-5 py-3.5 hidden md:table-cell">
                    Topics
                  </th>
                  <th scope="col" className="px-5 py-3.5 hidden lg:table-cell">
                    Tags
                  </th>
                  <th scope="col" className="px-5 py-3.5 text-right">
                    Action
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800/80 font-normal">
                {problemPage.content.map((problem) => (
                  <tr
                    key={problem.id}
                    data-testid={`problem-row-${problem.slug}`}
                    className="hover:bg-gray-800/40 transition group"
                  >
                    <td className="px-5 py-4">
                      <Link
                        to={`/problems/${problem.slug}`}
                        className="font-semibold text-white group-hover:text-blue-400 transition flex items-center space-x-2"
                      >
                        <span>{problem.title}</span>
                      </Link>
                      <div className="flex items-center space-x-2 mt-1 text-xs text-gray-400 md:hidden">
                        {problem.topics.slice(0, 2).map((t) => (
                          <span key={t.id} className="text-gray-400">
                            #{t.name}
                          </span>
                        ))}
                      </div>
                    </td>

                    <td className="px-5 py-4 whitespace-nowrap">
                      <DifficultyBadge difficulty={problem.difficulty} />
                    </td>

                    <td className="px-5 py-4 hidden md:table-cell">
                      <div className="flex flex-wrap gap-1.5 max-w-xs">
                        {problem.topics && problem.topics.length > 0 ? (
                          problem.topics.map((t) => (
                            <span
                              key={t.id}
                              className="px-2 py-0.5 rounded text-xs bg-gray-800/90 text-gray-300 border border-gray-700/60"
                            >
                              {t.name}
                            </span>
                          ))
                        ) : (
                          <span className="text-xs text-gray-400">—</span>
                        )}
                      </div>
                    </td>

                    <td className="px-5 py-4 hidden lg:table-cell">
                      <div className="flex flex-wrap gap-1.5 max-w-xs">
                        {problem.tags && problem.tags.length > 0 ? (
                          problem.tags.map((tg) => (
                            <span
                              key={tg.id}
                              className="px-2 py-0.5 rounded text-xs bg-gray-900 text-gray-400 border border-gray-800"
                            >
                              {tg.name}
                            </span>
                          ))
                        ) : (
                          <span className="text-xs text-gray-400">—</span>
                        )}
                      </div>
                    </td>

                    <td className="px-5 py-4 text-right whitespace-nowrap">
                      <Link
                        to={`/problems/${problem.slug}`}
                        className="inline-flex items-center space-x-1 px-3 py-1.5 rounded-lg text-xs font-semibold bg-blue-600/10 hover:bg-blue-600 text-blue-400 hover:text-white border border-blue-500/30 hover:border-blue-600 transition"
                      >
                        <span>Solve</span>
                        <ChevronRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          /* Empty Search / Filter State */
          <div className="p-12 text-center space-y-3" data-testid="catalogue-empty">
            <div className="w-12 h-12 rounded-full bg-gray-800 flex items-center justify-center text-gray-400 mx-auto">
              <Filter className="w-6 h-6" />
            </div>
            <h3 className="text-base font-semibold text-white">No problems found</h3>
            <p className="text-sm text-gray-400 max-w-sm mx-auto">
              We couldn't find any published problems matching your current search or filter criteria.
            </p>
            {hasActiveFilters && (
              <div className="pt-2">
                <button
                  type="button"
                  onClick={handleClearFilters}
                  className="px-4 py-2 rounded-lg text-xs font-semibold bg-gray-800 hover:bg-gray-700 text-gray-200 transition"
                >
                  Clear all filters
                </button>
              </div>
            )}
          </div>
        )}

        {/* Pagination Bar */}
        {problemPage && problemPage.totalPages > 1 && (
          <div className="flex items-center justify-between px-5 py-3.5 bg-[#0b0f19] border-t border-gray-800 text-xs text-gray-400">
            <div>
              Showing{' '}
              <span className="font-semibold text-gray-200">
                {problemPage.page * problemPage.size + 1}
              </span>{' '}
              to{' '}
              <span className="font-semibold text-gray-200">
                {Math.min((problemPage.page + 1) * problemPage.size, problemPage.totalElements)}
              </span>{' '}
              of <span className="font-semibold text-gray-200">{problemPage.totalElements}</span> problems
            </div>

            <div className="flex items-center space-x-2">
              <button
                type="button"
                onClick={() => handlePageChange(problemPage.page - 1)}
                disabled={!problemPage.hasPrevious}
                aria-label="Previous Page"
                className="p-1.5 rounded-md border border-gray-800 bg-[#111827] text-gray-300 hover:text-white disabled:opacity-40 disabled:cursor-not-allowed transition"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>

              <span className="font-medium text-gray-300 px-2">
                Page {problemPage.page + 1} of {problemPage.totalPages}
              </span>

              <button
                type="button"
                onClick={() => handlePageChange(problemPage.page + 1)}
                disabled={!problemPage.hasNext}
                aria-label="Next Page"
                className="p-1.5 rounded-md border border-gray-800 bg-[#111827] text-gray-300 hover:text-white disabled:opacity-40 disabled:cursor-not-allowed transition"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
