import { Button, Card } from "react-bootstrap";

function ProductCard({ product, onAddToCart }) {
  return (
    <Card className="h-100 product-card border-0 shadow-sm">
      <Card.Body className="d-flex flex-column">
        <div className="product-badge">{product.name}</div>
        <Card.Title className="mt-3">{product.name}</Card.Title>
        <Card.Text className="text-muted">{product.description}</Card.Text>
        <div className="mt-auto">
          <p className="fw-semibold mb-3">
            Available Stock: {product.quantity} {product.unit}
          </p>
          <Button variant="primary" className="w-100" onClick={() => onAddToCart(product)}>
            Add To Cart
          </Button>
        </div>
      </Card.Body>
    </Card>
  );
}

export default ProductCard;
