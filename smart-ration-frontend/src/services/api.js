import axios from "axios";
import { clearAuthStorage, getAccessToken, getRefreshToken, setAccessToken } from "../utils/authStorage";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json"
  }
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      getRefreshToken() &&
      !originalRequest.url?.includes("/auth/login") &&
      !originalRequest.url?.includes("/auth/refresh")
    ) {
      originalRequest._retry = true;

      try {
        const tokenResponse = await axios.post("http://localhost:8080/api/auth/refresh", {
          refreshToken: getRefreshToken()
        });
        const newAccessToken = tokenResponse.data.accessToken;
        setAccessToken(newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        clearAuthStorage();
        sessionStorage.setItem("smartRationSessionExpired", "true");
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default api;
