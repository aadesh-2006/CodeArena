import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import App from '../App';

describe('CodeArena Frontend Application Shell', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders application branding and header navigation', async () => {
    vi.stubGlobal('fetch', vi.fn().mockImplementation(() =>
      Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            success: true,
            message: 'Service is healthy',
            data: {
              status: 'UP',
              service: 'codearena-backend',
              version: '0.1.0-SNAPSHOT',
              timestamp: '2026-11-16T19:00:00Z',
            },
            timestamp: '2026-11-16T19:00:00Z',
          }),
      })
    ));

    render(<App />);

    expect(screen.getByText('CodeArena')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Problems' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Explore' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Submissions' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Dashboard' })).toBeInTheDocument();
    expect(screen.getByText(/Engineered for DSA Mastery & Technical Rigor/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('codearena-backend')).toBeInTheDocument();
    });
  });

  it('renders backend health status when API returns successfully', async () => {
    vi.stubGlobal('fetch', vi.fn().mockImplementation(() =>
      Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            success: true,
            message: 'Service is healthy',
            data: {
              status: 'UP',
              service: 'codearena-backend',
              version: '0.1.0-SNAPSHOT',
              timestamp: '2026-11-16T19:00:00Z',
            },
            timestamp: '2026-11-16T19:00:00Z',
          }),
      })
    ));

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('codearena-backend')).toBeInTheDocument();
      expect(screen.getByText('0.1.0-SNAPSHOT')).toBeInTheDocument();
    });
  });

  it('displays graceful error card when backend health endpoint is unreachable', async () => {
    vi.stubGlobal('fetch', vi.fn().mockImplementation(() =>
      Promise.reject(new Error('Network error'))
    ));

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Backend Disconnected')).toBeInTheDocument();
      expect(screen.getByText('Network error')).toBeInTheDocument();
    });
  });
});
