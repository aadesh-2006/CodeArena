import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { ProfilePage } from '../pages/ProfilePage';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { Header } from '../components/Header';

describe('Frontend Authentication and User Management', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders login form and handles successful login', async () => {
    const user = userEvent.setup();

    vi.stubGlobal(
      'fetch',
      vi.fn().mockImplementation((url: string) => {
        if (url === '/api/auth/login') {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Login successful',
                data: {
                  token: 'mock-jwt-token-xyz',
                  type: 'Bearer',
                  expiresInMs: 86400000,
                  user: {
                    id: 1,
                    username: 'testuser',
                    email: 'test@codearena.dev',
                    displayName: 'Test Dev',
                    role: 'USER',
                    isActive: true,
                    createdAt: '2026-11-16T12:00:00Z',
                    updatedAt: '2026-11-16T12:00:00Z',
                  },
                },
                timestamp: '2026-11-16T12:00:00Z',
              }),
          });
        }
        return Promise.reject(new Error('Unknown endpoint'));
      })
    );

    render(
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={<div>Dashboard Home</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.getByRole('heading', { name: /Sign In to CodeArena/i })).toBeInTheDocument();

    await user.type(screen.getByLabelText(/Username or Email/i), 'testuser');
    await user.type(screen.getByLabelText(/^Password$/i), 'Password123!');
    await user.click(screen.getByRole('button', { name: /Sign In/i }));

    await waitFor(() => {
      expect(localStorage.getItem('codearena_token')).toBe('mock-jwt-token-xyz');
      expect(screen.getByText('Dashboard Home')).toBeInTheDocument();
    });
  });

  it('displays error alert on invalid credentials', async () => {
    const user = userEvent.setup();

    vi.stubGlobal(
      'fetch',
      vi.fn().mockImplementation((url: string) => {
        if (url === '/api/auth/login') {
          return Promise.resolve({
            ok: false,
            status: 401,
            json: () =>
              Promise.resolve({
                success: false,
                message: 'Invalid username/email or password',
                data: null,
                timestamp: '2026-11-16T12:00:00Z',
              }),
          });
        }
        return Promise.reject(new Error('Unknown endpoint'));
      })
    );

    render(
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText(/Username or Email/i), 'wronguser');
    await user.type(screen.getByLabelText(/^Password$/i), 'WrongPass123!');
    await user.click(screen.getByRole('button', { name: /Sign In/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(/Invalid username\/email or password/i);
    });
  });

  it('validates client-side constraints on registration form', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/register']}>
        <AuthProvider>
          <RegisterPage />
        </AuthProvider>
      </MemoryRouter>
    );

    // Enter short/invalid username
    await user.type(screen.getByLabelText(/Username/i), 'ab');
    await user.type(screen.getByLabelText(/Email Address/i), 'invalid-email');
    await user.type(screen.getByLabelText(/^Password$/i), 'simple');
    await user.click(screen.getByRole('button', { name: /Register & Start Coding/i }));

    expect(screen.getByRole('alert')).toHaveTextContent(/Username must be 3-30 characters long/i);
  });

  it('handles successful registration and redirect', async () => {
    const user = userEvent.setup();

    vi.stubGlobal(
      'fetch',
      vi.fn().mockImplementation((url: string) => {
        if (url === '/api/auth/register') {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'User registered successfully',
                data: {
                  token: 'reg-token-12345',
                  type: 'Bearer',
                  expiresInMs: 86400000,
                  user: {
                    id: 2,
                    username: 'newcoder',
                    email: 'new@codearena.dev',
                    displayName: null,
                    role: 'USER',
                    isActive: true,
                    createdAt: '2026-11-16T12:00:00Z',
                    updatedAt: '2026-11-16T12:00:00Z',
                  },
                },
                timestamp: '2026-11-16T12:00:00Z',
              }),
          });
        }
        return Promise.reject(new Error('Unknown endpoint'));
      })
    );

    render(
      <MemoryRouter initialEntries={['/register']}>
        <AuthProvider>
          <Routes>
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/" element={<div>Welcome to CodeArena</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText(/Username/i), 'newcoder');
    await user.type(screen.getByLabelText(/Email Address/i), 'new@codearena.dev');
    await user.type(screen.getByLabelText(/^Password$/i), 'SecretPass123!');
    await user.click(screen.getByRole('button', { name: /Register & Start Coding/i }));

    await waitFor(() => {
      expect(localStorage.getItem('codearena_token')).toBe('reg-token-12345');
      expect(screen.getByText('Welcome to CodeArena')).toBeInTheDocument();
    });
  });

  it('protects profile route and redirects unauthenticated users', async () => {
    render(
      <MemoryRouter initialEntries={['/profile']}>
        <AuthProvider>
          <Routes>
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <div>Secret Profile Content</div>
                </ProtectedRoute>
              }
            />
            <Route path="/login" element={<div>Login Page Redirected</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Login Page Redirected')).toBeInTheDocument();
      expect(screen.queryByText('Secret Profile Content')).not.toBeInTheDocument();
    });
  });

  it('renders user details on profile page and allows updating display name', async () => {
    localStorage.setItem('codearena_token', 'valid-stored-token');
    const user = userEvent.setup();

    const mockProfile = {
      id: 5,
      username: 'dev_hero',
      email: 'hero@codearena.dev',
      displayName: 'Code Hero',
      role: 'USER',
      isActive: true,
      createdAt: '2026-01-15T00:00:00Z',
      updatedAt: '2026-01-15T00:00:00Z',
    };

    vi.stubGlobal(
      'fetch',
      vi.fn().mockImplementation((url: string, opts?: RequestInit) => {
        if (url === '/api/users/me' && opts?.method === 'PUT') {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'Profile updated successfully',
                data: {
                  ...mockProfile,
                  displayName: 'Hero Supreme',
                },
                timestamp: '2026-11-16T12:00:00Z',
              }),
          });
        }
        if (url === '/api/users/me') {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'User profile retrieved',
                data: mockProfile,
                timestamp: '2026-11-16T12:00:00Z',
              }),
          });
        }
        return Promise.reject(new Error('Unknown endpoint'));
      })
    );

    render(
      <MemoryRouter initialEntries={['/profile']}>
        <AuthProvider>
          <Routes>
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Code Hero')).toBeInTheDocument();
      expect(screen.getAllByText('@dev_hero').length).toBeGreaterThan(0);
      expect(screen.getByText('hero@codearena.dev')).toBeInTheDocument();
    });

    // Click edit profile
    await user.click(screen.getByRole('button', { name: /Edit Profile/i }));

    const displayNameInput = screen.getByLabelText(/Display Name/i);
    await user.clear(displayNameInput);
    await user.type(displayNameInput, 'Hero Supreme');

    await user.click(screen.getByRole('button', { name: /Save Changes/i }));

    await waitFor(() => {
      expect(screen.getByText(/Profile updated successfully/i)).toBeInTheDocument();
      expect(screen.getByText('Hero Supreme')).toBeInTheDocument();
    });
  });

  it('updates header to reflect authenticated state and handles logout', async () => {
    localStorage.setItem('codearena_token', 'valid-token');
    const user = userEvent.setup();

    vi.stubGlobal(
      'fetch',
      vi.fn().mockImplementation((url: string) => {
        if (url === '/api/users/me') {
          return Promise.resolve({
            ok: true,
            json: () =>
              Promise.resolve({
                success: true,
                message: 'User profile retrieved',
                data: {
                  id: 10,
                  username: 'alex',
                  email: 'alex@codearena.dev',
                  displayName: 'Alex',
                  role: 'USER',
                  isActive: true,
                  createdAt: '2026-11-16T12:00:00Z',
                  updatedAt: '2026-11-16T12:00:00Z',
                },
                timestamp: '2026-11-16T12:00:00Z',
              }),
          });
        }
        return Promise.reject(new Error('Unknown endpoint'));
      })
    );

    render(
      <MemoryRouter initialEntries={['/']}>
        <AuthProvider>
          <Header />
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Alex')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Sign Out/i })).toBeInTheDocument();
    });

    // Logout
    await user.click(screen.getByRole('button', { name: /Sign Out/i }));

    await waitFor(() => {
      expect(localStorage.getItem('codearena_token')).toBeNull();
      expect(screen.getByRole('link', { name: /Sign In/i })).toBeInTheDocument();
      expect(screen.getByRole('link', { name: /Register/i })).toBeInTheDocument();
    });
  });
});
