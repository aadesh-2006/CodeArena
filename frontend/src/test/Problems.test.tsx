import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { Header } from '../components/Header';
import { ProblemCataloguePage } from '../pages/ProblemCataloguePage';
import { ProblemDetailPage } from '../pages/ProblemDetailPage';

const mockTopics = [
  { id: 1, name: 'Arrays', slug: 'arrays', description: 'Array problems' },
  { id: 2, name: 'Dynamic Programming', slug: 'dp', description: 'DP algorithms' },
];

const mockTags = [
  { id: 1, name: 'Hash Table', slug: 'hash-table' },
  { id: 2, name: 'Two Pointers', slug: 'two-pointers' },
];

const mockProblemSummary1 = {
  id: 101,
  slug: 'two-sum',
  title: 'Two Sum',
  difficulty: 'EASY',
  problemType: 'ALGORITHMIC',
  topics: [{ id: 1, name: 'Arrays', slug: 'arrays' }],
  tags: [{ id: 1, name: 'Hash Table', slug: 'hash-table' }],
  createdAt: '2026-11-17T00:00:00Z',
};

const mockProblemSummary2 = {
  id: 102,
  slug: 'course-schedule',
  title: 'Course Schedule',
  difficulty: 'MEDIUM',
  problemType: 'ALGORITHMIC',
  topics: [{ id: 2, name: 'Dynamic Programming', slug: 'dp' }],
  tags: [],
  createdAt: '2026-11-17T00:01:00Z',
};

const mockDetailTwoSum = {
  slug: 'two-sum',
  title: 'Two Sum',
  statement: 'Given an array of integers nums and an integer target, return indices of the two numbers that add up to target.',
  difficulty: 'EASY',
  problemType: 'ALGORITHMIC',
  constraintsText: '2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9',
  inputFormat: 'Array nums and integer target',
  outputFormat: 'Array of two integers [i, j]',
  timeLimitMs: 2000,
  memoryLimitMb: 256,
  editorial: 'Use a hash map to map value to index in linear time O(n).',
  topics: [{ id: 1, name: 'Arrays', slug: 'arrays', description: 'Array problems' }],
  tags: [{ id: 1, name: 'Hash Table', slug: 'hash-table' }],
  examples: [
    {
      id: 1,
      inputText: 'nums = [2,7,11,15], target = 9',
      outputText: '[0,1]',
      explanation: 'Because nums[0] + nums[1] == 9, we return [0, 1].',
      orderIndex: 1,
    },
  ],
  hints: [
    {
      id: 1,
      hintText: 'Try using a hash map to achieve O(n) runtime.',
      orderIndex: 1,
    },
  ],
  createdAt: '2026-11-17T00:00:00Z',
  updatedAt: '2026-11-17T00:00:00Z',
};

const mockDetailNoEditorial = {
  slug: 'course-schedule',
  title: 'Course Schedule',
  statement: 'Determine if you can finish all courses.',
  difficulty: 'MEDIUM',
  problemType: 'ALGORITHMIC',
  constraintsText: '1 <= numCourses <= 2000',
  inputFormat: 'int numCourses, int[][] prerequisites',
  outputFormat: 'boolean',
  timeLimitMs: 2000,
  memoryLimitMb: 256,
  editorial: null,
  topics: [{ id: 2, name: 'Dynamic Programming', slug: 'dp', description: 'DP' }],
  tags: [],
  examples: [
    {
      id: 2,
      inputText: 'numCourses = 2, prerequisites = [[1,0]]',
      outputText: 'true',
      explanation: null,
      orderIndex: 1,
    },
  ],
  hints: [],
  createdAt: '2026-11-17T00:01:00Z',
  updatedAt: '2026-11-17T00:01:00Z',
};

describe('Public Problem Catalogue and Detail Frontend (Stage D)', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  const setupMockFetch = (customHandler?: (url: string) => Promise<unknown> | null) => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockImplementation((url: string) => {
        if (customHandler) {
          const custom = customHandler(url);
          if (custom) return custom;
        }

        if (url.startsWith('/api/topics')) {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Success',
                data: mockTopics,
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }

        if (url.startsWith('/api/tags')) {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Success',
                data: mockTags,
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }

        if (url.startsWith('/api/users/me') || url.startsWith('/api/users/profile')) {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Success',
                data: {
                  id: 1,
                  username: 'alexdev',
                  email: 'alex@codearena.dev',
                  displayName: 'Alex',
                  role: 'USER',
                  isActive: true,
                  createdAt: '2026-11-17T00:00:00Z',
                  updatedAt: '2026-11-17T00:00:00Z',
                },
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }

        if (url.startsWith('/api/problems/two-sum')) {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Success',
                data: mockDetailTwoSum,
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }

        if (url.startsWith('/api/problems/course-schedule')) {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Success',
                data: mockDetailNoEditorial,
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }

        if (url.startsWith('/api/problems/non-existent')) {
          return Promise.resolve({
            ok: false,
            status: 404,
            json: () =>
              Promise.resolve({
                success: false,
                message: 'Problem not found: non-existent',
                data: null,
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }

        if (url.startsWith('/api/problems')) {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Success',
                data: {
                  content: [mockProblemSummary1, mockProblemSummary2],
                  page: 0,
                  size: 20,
                  totalElements: 2,
                  totalPages: 1,
                  hasNext: false,
                  hasPrevious: false,
                },
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }

        return Promise.reject(new Error(`Unhandled request: ${url}`));
      })
    );
  };

  describe('Problem Catalogue Page (/problems)', () => {
    it('renders catalogue layout, toolbar, and problem rows', async () => {
      setupMockFetch();

      render(
        <MemoryRouter initialEntries={['/problems']}>
          <ProblemCataloguePage />
        </MemoryRouter>
      );

      // Skeleton state is displayed while loading
      expect(screen.getByTestId('catalogue-skeleton')).toBeInTheDocument();

      // Problems rendered after API returns
      await waitFor(() => {
        expect(screen.getByText('Two Sum')).toBeInTheDocument();
        expect(screen.getByText('Course Schedule')).toBeInTheDocument();
        expect(screen.getByTestId('difficulty-easy')).toBeInTheDocument();
        expect(screen.getByTestId('difficulty-medium')).toBeInTheDocument();
      });

      expect(screen.getByText('Total Available')).toBeInTheDocument();
      expect(screen.getByText('2')).toBeInTheDocument();
    });

    it('triggers search query parameter update on typing', async () => {
      const user = userEvent.setup();
      setupMockFetch();

      render(
        <MemoryRouter initialEntries={['/problems']}>
          <ProblemCataloguePage />
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Two Sum')).toBeInTheDocument();
      });

      const searchInput = screen.getByRole('searchbox');
      await user.type(searchInput, 'array');

      await waitFor(() => {
        expect(fetch).toHaveBeenCalledWith(
          expect.stringContaining('search=array'),
          expect.any(Object)
        );
      });
    });

    it('filters problems by difficulty when difficulty button is clicked', async () => {
      const user = userEvent.setup();
      setupMockFetch();

      render(
        <MemoryRouter initialEntries={['/problems']}>
          <ProblemCataloguePage />
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Two Sum')).toBeInTheDocument();
      });

      const easyBtn = screen.getByRole('button', { name: /^Easy$/i });
      await user.click(easyBtn);

      await waitFor(() => {
        expect(fetch).toHaveBeenCalledWith(
          expect.stringContaining('difficulty=EASY'),
          expect.any(Object)
        );
      });
    });

    it('filters problems by topic and tag via select dropdowns', async () => {
      const user = userEvent.setup();
      setupMockFetch();

      render(
        <MemoryRouter initialEntries={['/problems']}>
          <ProblemCataloguePage />
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Two Sum')).toBeInTheDocument();
      });

      const topicSelect = screen.getByLabelText('Filter by Topic');
      await user.selectOptions(topicSelect, 'arrays');

      await waitFor(() => {
        expect(fetch).toHaveBeenCalledWith(
          expect.stringContaining('topic=arrays'),
          expect.any(Object)
        );
      });

      const tagSelect = screen.getByLabelText('Filter by Tag');
      await user.selectOptions(tagSelect, 'hash-table');

      await waitFor(() => {
        expect(fetch).toHaveBeenCalledWith(
          expect.stringContaining('tag=hash-table'),
          expect.any(Object)
        );
      });
    });

    it('displays empty state when API returns zero results', async () => {
      setupMockFetch((url) => {
        if (url.includes('search=unknown')) {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Success',
                data: {
                  content: [],
                  page: 0,
                  size: 20,
                  totalElements: 0,
                  totalPages: 0,
                  hasNext: false,
                  hasPrevious: false,
                },
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }
        return null;
      });

      render(
        <MemoryRouter initialEntries={['/problems?search=unknown']}>
          <ProblemCataloguePage />
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByTestId('catalogue-empty')).toBeInTheDocument();
        expect(screen.getByText('No problems found')).toBeInTheDocument();
      });
    });

    it('displays error alert on API failure', async () => {
      setupMockFetch((url) => {
        if (url.startsWith('/api/problems')) {
          return Promise.resolve({
            ok: false,
            status: 500,
            json: () =>
              Promise.resolve({
                success: false,
                message: 'Internal server error while fetching problems',
                data: null,
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }
        return null;
      });

      render(
        <MemoryRouter initialEntries={['/problems']}>
          <ProblemCataloguePage />
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByRole('alert')).toBeInTheDocument();
        expect(screen.getByText(/Internal server error while fetching problems/i)).toBeInTheDocument();
      });
    });

    it('renders pagination controls and updates page parameter', async () => {
      const user = userEvent.setup();
      setupMockFetch((url) => {
        if (url.startsWith('/api/problems')) {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Success',
                data: {
                  content: [mockProblemSummary1],
                  page: 0,
                  size: 1,
                  totalElements: 2,
                  totalPages: 2,
                  hasNext: true,
                  hasPrevious: false,
                },
                timestamp: '2026-11-17T00:00:00Z',
              }),
          });
        }
        return null;
      });

      render(
        <MemoryRouter initialEntries={['/problems']}>
          <ProblemCataloguePage />
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Page 1 of 2')).toBeInTheDocument();
      });

      const nextBtn = screen.getByLabelText('Next Page');
      expect(nextBtn).toBeEnabled();
      await user.click(nextBtn);

      await waitFor(() => {
        expect(fetch).toHaveBeenCalledWith(
          expect.stringContaining('page=1'),
          expect.any(Object)
        );
      });
    });
  });

  describe('Problem Detail Page (/problems/:slug)', () => {
    it('renders problem statement, constraints, formats, limits, and examples', async () => {
      setupMockFetch();

      render(
        <MemoryRouter initialEntries={['/problems/two-sum']}>
          <Routes>
            <Route path="/problems/:slug" element={<ProblemDetailPage />} />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByTestId('detail-skeleton')).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.getByRole('heading', { level: 1, name: 'Two Sum' })).toBeInTheDocument();
        expect(screen.getByText(/Given an array of integers nums and an integer target/i)).toBeInTheDocument();
      });

      expect(screen.getByText(/2000 ms/i)).toBeInTheDocument();
      expect(screen.getByText(/256 MB/i)).toBeInTheDocument();
      expect(screen.getByText(/2 <= nums.length <= 10\^4/i)).toBeInTheDocument();
      expect(screen.getByText(/Array nums and integer target/i)).toBeInTheDocument();
      expect(screen.getByText(/Array of two integers \[i, j\]/i)).toBeInTheDocument();

      // Example rendering
      expect(screen.getByTestId('problem-example-1')).toBeInTheDocument();
      expect(screen.getByText('nums = [2,7,11,15], target = 9')).toBeInTheDocument();
      expect(screen.getByText('[0,1]')).toBeInTheDocument();
      expect(screen.getByText(/Because nums\[0\] \+ nums\[1\] == 9/i)).toBeInTheDocument();
    });

    it('toggles hints on click', async () => {
      const user = userEvent.setup();
      setupMockFetch();

      render(
        <MemoryRouter initialEntries={['/problems/two-sum']}>
          <Routes>
            <Route path="/problems/:slug" element={<ProblemDetailPage />} />
          </Routes>
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByRole('heading', { level: 1, name: 'Two Sum' })).toBeInTheDocument();
      });

      // Hint is initially hidden
      expect(screen.queryByTestId('hint-content-1')).not.toBeInTheDocument();

      // Click to reveal hint
      const hintButton = screen.getByRole('button', { name: /Hint 1/i });
      await user.click(hintButton);

      expect(screen.getByTestId('hint-content-1')).toBeInTheDocument();
      expect(screen.getByText(/Try using a hash map to achieve O\(n\) runtime\./i)).toBeInTheDocument();
    });

    it('shows editorial when problem.editorial is present and hides when null', async () => {
      const user = userEvent.setup();
      setupMockFetch();

      // 1. With Editorial (two-sum)
      const { unmount } = render(
        <MemoryRouter initialEntries={['/problems/two-sum']}>
          <Routes>
            <Route path="/problems/:slug" element={<ProblemDetailPage />} />
          </Routes>
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByTestId('editorial-section')).toBeInTheDocument();
      });

      const editorialToggle = screen.getByRole('button', { name: /Official Editorial & Approach/i });
      await user.click(editorialToggle);

      expect(screen.getByTestId('editorial-content')).toBeInTheDocument();
      expect(screen.getByText(/Use a hash map to map value to index in linear time O\(n\)\./i)).toBeInTheDocument();

      unmount();

      // 2. Without Editorial (course-schedule)
      render(
        <MemoryRouter initialEntries={['/problems/course-schedule']}>
          <Routes>
            <Route path="/problems/:slug" element={<ProblemDetailPage />} />
          </Routes>
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByRole('heading', { level: 1, name: 'Course Schedule' })).toBeInTheDocument();
      });

      expect(screen.queryByTestId('editorial-section')).not.toBeInTheDocument();
    });

    it('displays 404 state when problem does not exist or is unpublished', async () => {
      setupMockFetch();

      render(
        <MemoryRouter initialEntries={['/problems/non-existent']}>
          <Routes>
            <Route path="/problems/:slug" element={<ProblemDetailPage />} />
          </Routes>
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByTestId('problem-not-found')).toBeInTheDocument();
        expect(screen.getByText('Problem Not Found')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: /Browse Problem Catalogue/i })).toBeInTheDocument();
      });
    });
  });

  describe('Integration & Navigation', () => {
    it('allows anonymous users to navigate from catalogue to problem detail', async () => {
      const user = userEvent.setup();
      setupMockFetch();

      render(
        <MemoryRouter initialEntries={['/problems']}>
          <Routes>
            <Route path="/problems" element={<ProblemCataloguePage />} />
            <Route path="/problems/:slug" element={<ProblemDetailPage />} />
          </Routes>
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Two Sum')).toBeInTheDocument();
      });

      const solveLink = screen.getAllByRole('link', { name: /Solve/i })[0];
      await user.click(solveLink);

      await waitFor(() => {
        expect(screen.getByRole('heading', { level: 1, name: 'Two Sum' })).toBeInTheDocument();
      });
    });

    it('preserves authenticated user status in header when browsing problems', async () => {
      setupMockFetch();
      localStorage.setItem('codearena_token', 'test-jwt');

      render(
        <MemoryRouter initialEntries={['/problems']}>
          <AuthProvider>
            <Header />
            <ProblemCataloguePage />
          </AuthProvider>
        </MemoryRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Two Sum')).toBeInTheDocument();
        expect(screen.queryByRole('link', { name: /Sign In/i })).not.toBeInTheDocument();
        expect(screen.getByText('Alex')).toBeInTheDocument();
      });
    });
  });
});
