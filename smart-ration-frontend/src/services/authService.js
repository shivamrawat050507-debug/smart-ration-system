import api from "./api";
import axios from "axios";

export const registerUser = async (payload) => {
  const response = await api.post("/auth/register", payload);
  return response.data;
};

export const loginUser = async (payload) => {
  const response = await api.post("/auth/login", payload);
  return response.data;
};

export const refreshAccessToken = async (refreshToken) => {
  const response = await axios.post("http://localhost:8080/api/auth/refresh", {
    refreshToken
  });
  return response.data;
};

export const logoutUser = async (refreshToken) => {
  const response = await axios.post("http://localhost:8080/api/auth/logout", {
    refreshToken
  });
  return response.data;
};

export const fetchUserDetails = async (userId) => {
  const response = await api.get(`/users/${userId}`);
  return response.data;
};
