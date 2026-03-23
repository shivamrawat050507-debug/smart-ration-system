import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import {
  createProduct,
  deleteProduct,
  fetchAdminProducts,
  updateProduct
} from "../services/productService";

const initialForm = {
  name: "",
  description: "",
  stockQuantity: "",
  unit: "kg"
};

function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [formData, setFormData] = useState(initialForm);
  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadProducts = async () => {
    try {
      const data = await fetchAdminProducts();
      setProducts(data);
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Unable to load products.");
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");

    const payload = { ...formData, stockQuantity: Number(formData.stockQuantity) };

    try {
      if (editingId) {
        await updateProduct(editingId, payload);
        setMessage("Product updated successfully.");
      } else {
        await createProduct(payload);
        setMessage("Product created successfully.");
      }
      setFormData(initialForm);
      setEditingId(null);
      loadProducts();
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Unable to save product.");
    }
  };

  const handleEdit = (product) => {
    setEditingId(product.id);
    setFormData({
      name: product.name,
      description: product.description,
      stockQuantity: product.stockQuantity,
      unit: product.unit
    });
  };

  const handleDelete = async (productId) => {
    try {
      await deleteProduct(productId);
      setMessage("Product deleted successfully.");
      loadProducts();
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Unable to delete product.");
    }
  };

  return (
    <Row className="g-4">
      <Col lg={5}>
        <Card className="border-0 shadow-sm">
          <Card.Body>
            <h3 className="fw-bold">{editingId ? "Edit Product" : "Add Product"}</h3>
            {message && <Alert variant="success">{message}</Alert>}
            {error && <Alert variant="danger">{error}</Alert>}
            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Name</Form.Label>
                <Form.Control name="name" value={formData.name} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Description</Form.Label>
                <Form.Control name="description" value={formData.description} onChange={handleChange} />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Stock Quantity</Form.Label>
                <Form.Control
                  type="number"
                  name="stockQuantity"
                  value={formData.stockQuantity}
                  onChange={handleChange}
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Unit</Form.Label>
                <Form.Control name="unit" value={formData.unit} onChange={handleChange} />
              </Form.Group>
              <Button type="submit">{editingId ? "Update Product" : "Create Product"}</Button>
            </Form>
          </Card.Body>
        </Card>
      </Col>

      <Col lg={7}>
        <Card className="border-0 shadow-sm">
          <Card.Body>
            <h3 className="fw-bold">Product Management</h3>
            <Table responsive hover>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Stock</th>
                  <th>Unit</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td>{product.name}</td>
                    <td>{product.stockQuantity}</td>
                    <td>{product.unit}</td>
                    <td className="d-flex gap-2">
                      <Button size="sm" variant="outline-primary" onClick={() => handleEdit(product)}>
                        Edit
                      </Button>
                      <Button size="sm" variant="outline-danger" onClick={() => handleDelete(product.id)}>
                        Delete
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
}

export default AdminProductsPage;
