import api from "./api";

export const createOrder = async (payload) => {
  const response = await api.post("/orders", payload);
  return response.data;
};

export const fetchUserOrders = async (userId) => {
  const response = await api.get(`/orders/user/${userId}`);
  return response.data;
};

export const fetchAllOrders = async () => {
  const response = await api.get("/admin/orders");
  return response.data;
};
