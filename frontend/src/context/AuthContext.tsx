import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { User, UserWithProfile } from '../types';
import apiClient from '../api/client';
import { clearToken, getToken, setToken } from '../lib/tokenStorage';

interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
}

interface AuthContextValue extends AuthState {
  login: (token: string, user: User, persistent: boolean) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

interface TokenPayload {
  sub?: string;
  id?: string;
  email?: string;
  name?: string;
  roles?: string[];
}

function decodeTokenPayload(token: string): TokenPayload {
  try {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload)) as TokenPayload;
  } catch {
    return {};
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>(() => {
    const token = getToken();
    if (token) {
      const payload = decodeTokenPayload(token);
      const user: User = {
        id: payload.sub ?? payload.id ?? '',
        email: payload.email ?? '',
        name: payload.name ?? '',
        roles: payload.roles ?? [],
      };
      return { token, user, isAuthenticated: true };
    }
    return { token: null, user: null, isAuthenticated: false };
  });

  // Overwrite the optimistic boot-time user with the authoritative server response.
  // The axios response interceptor handles 401 (clears token + redirects to /login).
  useEffect(() => {
    if (!state.token) return;
    apiClient.get<UserWithProfile>('/api/users/me').then((r) => {
      const { id, email, name, roles } = r.data;
      setState((prev) => ({ ...prev, user: { id, email, name, roles } }));
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state.token]);

  function login(token: string, user: User, persistent: boolean) {
    setToken(token, persistent);
    setState({ token, user, isAuthenticated: true });
  }

  function logout() {
    clearToken();
    setState({ token: null, user: null, isAuthenticated: false });
  }

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
