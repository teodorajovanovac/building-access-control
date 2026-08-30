import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { login as loginApi } from '../api/auth';

const AuthContext = createContext(null);

function loadStoredUser() {
  const raw = localStorage.getItem('bac_user');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadStoredUser);
  const [token, setToken] = useState(() => localStorage.getItem('bac_token'));

  const login = useCallback(async (email, password) => {
    const data = await loginApi(email, password);
    const loggedInUser = {
      id: data.userId,
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      role: data.role,
    };
    localStorage.setItem('bac_token', data.token);
    localStorage.setItem('bac_user', JSON.stringify(loggedInUser));
    setToken(data.token);
    setUser(loggedInUser);
    return loggedInUser;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('bac_token');
    localStorage.removeItem('bac_user');
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token && user),
      login,
      logout,
    }),
    [user, token, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth mora biti korišćen unutar AuthProvider-a');
  return ctx;
}
