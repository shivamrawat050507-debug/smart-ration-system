import { useEffect, useState } from "react";
import { Alert, Col, Row, Spinner } from "react-bootstrap";
import ProductCard from "../components/ProductCard";
import { useCart } from "../context/CartContext";
import { fetchProducts } from "../services/productService";

function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState("");
  const { addToCart } = useCart();

  useEffect(() => {
    const loadProducts = async () => {
      try {
        const data = await fetchProducts();
        setProducts(data);
      } catch (apiError) {
        setError(apiError.response?.data?.message || "Unable to fetch products.");
      } finally {
        setLoading(false);
      }
    };

    loadProducts();
  }, []);

  const handleAddToCart = (product) => {
    addToCart(product);
    setSuccess(`${product.name} added to cart.`);
    window.setTimeout(() => setSuccess(""), 1500);
  };

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" variant="primary" />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-4">
        <h2 className="fw-bold">Ration Products</h2>
        <p className="text-muted mb-0">Current stock fetched from the backend inventory API.</p>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <Row className="g-4">
        {products.map((product) => (
          <Col md={6} lg={4} key={product.id}>
            <ProductCard product={product} onAddToCart={handleAddToCart} />
          </Col>
        ))}
      </Row>
    </div>
  );
}

export default ProductsPage;
