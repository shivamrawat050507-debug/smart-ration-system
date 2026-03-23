import api from "./api";

export const payForOrder = async (orderId) => {
  const response = await api.post(`/payments/${orderId}`);
  return response.data;
};
