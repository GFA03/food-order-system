import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { User } from '../types';

interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
}

interface AuthContextValue extends AuthState {
  login: (token: string, user: User) => void;
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
    const token = localStorage.getItem('token');
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

  useEffect(() => {
    // Keep localStorage in sync
    if (state.token) {
      localStorage.setItem('token', state.token);
    } else {
      localStorage.removeItem('token');
    }
  }, [state.token]);

  function login(token: string, user: User) {
    localStorage.setItem('token', token);
    setState({ token, user, isAuthenticated: true });
  }

  function logout() {
    localStorage.removeItem('token');
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
