import api from "./api";

const units = {
  rice: "kg",
  wheat: "kg",
  sugar: "kg"
};

const titles = {
  rice: "Rice",
  wheat: "Wheat",
  sugar: "Sugar"
};

export const fetchProducts = async () => {
  const response = await api.get("/products");
  return response.data.map((product) => ({
    id: product.id,
    name: product.name,
    unit: product.unit || units[product.name.toLowerCase()] || "kg",
    quantity: product.stockQuantity,
    description: product.description || `${product.name} available for public distribution.`
  }));
};

export const fetchAdminProducts = async () => {
  const response = await api.get("/admin/products");
  return response.data;
};

export const createProduct = async (payload) => {
  const response = await api.post("/admin/products", payload);
  return response.data;
};

export const updateProduct = async (productId, payload) => {
  const response = await api.put(`/admin/products/${productId}`, payload);
  return response.data;
};

export const deleteProduct = async (productId) => {
  await api.delete(`/admin/products/${productId}`);
};
