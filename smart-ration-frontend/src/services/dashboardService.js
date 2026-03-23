import api from "./api";

export const fetchDashboardOverview = async (userId) => {
  const response = await api.get(`/dashboard/overview/${userId}`);
  return response.data;
};

export const distributeRation = async (userId) => {
  const response = await api.post(`/dashboard/distribute/${userId}`);
  return response.data;
};

export const scanDealerCard = async (userId, payload) => {
  const response = await api.post(`/dashboard/dealer/scan/${userId}`, payload);
  return response.data;
};

export const distributeDealerRation = async (userId, payload) => {
  const response = await api.post(`/dashboard/dealer/distribute/${userId}`, payload);
  return response.data;
};

export const allocateStockToDepot = async (payload) => {
  const response = await api.post("/dashboard/admin/allocate-stock", payload);
  return response.data;
};

export const updateRationRule = async (payload) => {
  const response = await api.post("/dashboard/admin/update-ration-rule", payload);
  return response.data;
};

export const addCity = async (payload) => {
  const response = await api.post("/dashboard/admin/add-city", payload);
  return response.data;
};

export const addDepot = async (payload) => {
  const response = await api.post("/dashboard/admin/add-depot", payload);
  return response.data;
};
