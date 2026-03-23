import { createContext, useContext, useMemo, useState } from "react";
import { logoutUser } from "../services/authService";
import {
  clearAuthStorage,
  getAccessToken,
  getStoredUser,
  getRefreshToken,
  setAccessToken,
  setRefreshToken,
  setStoredUser
} from "../utils/authStorage";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser());
  const [accessToken, setAccessTokenState] = useState(getAccessToken());
  const [refreshToken, setRefreshTokenState] = useState(getRefreshToken());

  const login = (authData) => {
    const userData = {
      id: authData.userId,
      name: authData.name,
      rationCardNumber: authData.rationCardNumber,
      role: authData.role
    };

    setUser(userData);
    setAccessTokenState(authData.accessToken);
    setRefreshTokenState(authData.refreshToken);
    setStoredUser(userData);
    setAccessToken(authData.accessToken);
    setRefreshToken(authData.refreshToken);
  };

  const logout = async () => {
    const currentRefreshToken = getRefreshToken();

    if (currentRefreshToken) {
      try {
        await logoutUser(currentRefreshToken);
      } catch (error) {
      }
    }

    setUser(null);
    setAccessTokenState(null);
    setRefreshTokenState(null);
    clearAuthStorage();
  };

  const updateAccessToken = (newAccessToken) => {
    setAccessTokenState(newAccessToken);
    setAccessToken(newAccessToken);
  };

  const value = useMemo(
    () => ({
      user,
      accessToken,
      refreshToken,
      login,
      logout,
      updateAccessToken,
      isAuthenticated: Boolean(user && accessToken),
      isAdmin: user?.role === "ROLE_ADMIN"
    }),
    [user, accessToken, refreshToken]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
