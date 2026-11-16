import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { User, LoginRequest, RegisterRequest, UpdateProfileRequest } from '../types';
import { authApi } from '../api/authApi';
import { userApi } from '../api/userApi';
import { getStoredToken, setStoredToken, clearStoredToken } from '../api/client';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
  updateProfile: (data: UpdateProfileRequest) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(getStoredToken());
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const refreshProfile = useCallback(async () => {
    try {
      const profile = await userApi.getProfile();
      setUser(profile);
    } catch {
      clearStoredToken();
      setToken(null);
      setUser(null);
    }
  }, []);

  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = getStoredToken();
      if (storedToken) {
        setToken(storedToken);
        await refreshProfile();
      }
      setIsLoading(false);
    };

    initializeAuth();

    // Listen for 401 unauthorized events from API client
    const handleUnauthorized = () => {
      clearStoredToken();
      setToken(null);
      setUser(null);
    };

    // Listen for storage events (e.g. user logged out or changed account in another tab)
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'codearena_token') {
        if (!e.newValue) {
          setToken(null);
          setUser(null);
        } else {
          setToken(e.newValue);
          refreshProfile();
        }
      }
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    window.addEventListener('storage', handleStorageChange);

    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
      window.removeEventListener('storage', handleStorageChange);
    };
  }, [refreshProfile]);

  const login = async (credentials: LoginRequest): Promise<void> => {
    const authData = await authApi.login(credentials);
    setStoredToken(authData.token);
    setToken(authData.token);
    setUser(authData.user);
  };

  const register = async (data: RegisterRequest): Promise<void> => {
    const authData = await authApi.register(data);
    setStoredToken(authData.token);
    setToken(authData.token);
    setUser(authData.user);
  };

  const logout = (): void => {
    clearStoredToken();
    setToken(null);
    setUser(null);
  };

  const updateProfile = async (data: UpdateProfileRequest): Promise<void> => {
    const updated = await userApi.updateProfile(data);
    setUser(updated);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user && !!token,
        isLoading,
        login,
        register,
        logout,
        refreshProfile,
        updateProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
