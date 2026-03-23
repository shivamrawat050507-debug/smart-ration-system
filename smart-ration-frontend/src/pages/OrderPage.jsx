import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Table } from "react-bootstrap";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { createOrder, fetchUserOrders } from "../services/orderService";
import { payForOrder } from "../services/paymentService";

function OrderPage() {
  const { cartItems, updateQuantity, removeFromCart, clearCart } = useCart();
  const { user } = useAuth();
  const [deliveryDate, setDeliveryDate] = useState("");
  const [deliveryTime, setDeliveryTime] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [userOrders, setUserOrders] = useState([]);

  useEffect(() => {
    const loadOrders = async () => {
      try {
        const data = await fetchUserOrders(user.id);
        setUserOrders(data);
      } catch (error) {
        setUserOrders([]);
      }
    };

    if (user?.id) {
      loadOrders();
    }
  }, [user]);

  const handlePlaceOrder = async () => {
    setErrorMessage("");
    setSuccessMessage("");

    if (cartItems.length === 0) {
      setErrorMessage("Add at least one product to the cart before placing an order.");
      return;
    }

    if (!deliveryDate || !deliveryTime) {
      setErrorMessage("Please select a delivery date and delivery time.");
      return;
    }

    try {
      setLoading(true);
      const order = await createOrder({
        userId: user.id,
        deliveryDate,
        deliveryTime: `${deliveryTime}:00`,
        items: cartItems.map((item) => ({
          productId: item.id,
          quantity: item.selectedQuantity
        }))
      });

      setSuccessMessage(
        `Order placed successfully for ${order.deliveryDate} at ${order.deliveryTime}. Order ID: ${order.id}.`
      );
      setUserOrders((previous) => [order, ...previous]);
      clearCart();
      setDeliveryDate("");
      setDeliveryTime("");
    } catch (error) {
      if (typeof error.response?.data === "object" && !Array.isArray(error.response.data)) {
        const firstMessage = Object.values(error.response.data)[0];
        setErrorMessage(firstMessage || "Unable to place order.");
      } else {
        setErrorMessage(error.response?.data?.message || "Unable to place order.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handlePayNow = async (orderId) => {
    try {
      const payment = await payForOrder(orderId);
      setSuccessMessage(payment.message);
      setUserOrders((previous) =>
        previous.map((order) =>
          order.id === orderId
            ? { ...order, paymentStatus: payment.paymentStatus, status: payment.orderStatus }
            : order
        )
      );
    } catch (error) {
      setErrorMessage(error.response?.data?.message || "Unable to process payment.");
    }
  };

  return (
    <Row className="g-4">
      <Col lg={8}>
        <Card className="border-0 shadow-sm">
          <Card.Body>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h2 className="fw-bold mb-0">Order Page</h2>
              {cartItems.length > 0 && (
                <Button variant="outline-danger" size="sm" onClick={clearCart}>
                  Clear Cart
                </Button>
              )}
            </div>

            {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}
            {successMessage && <Alert variant="success">{successMessage}</Alert>}

            {cartItems.length === 0 ? (
              <Alert variant="info" className="mb-0">
                Your cart is empty. Add products from the products page to continue.
              </Alert>
            ) : (
              <Table responsive hover>
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Available</th>
                    <th>Quantity</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {cartItems.map((item) => (
                    <tr key={item.id}>
                      <td>{item.name}</td>
                      <td>
                        {item.quantity} {item.unit}
                      </td>
                      <td style={{ maxWidth: "140px" }}>
                        <Form.Control
                          type="number"
                          min="1"
                          max={item.quantity}
                          value={item.selectedQuantity}
                          onChange={(event) =>
                            updateQuantity(
                              item.id,
                              Math.min(Number(event.target.value), item.quantity)
                            )
                          }
                        />
                      </td>
                      <td>
                        <Button variant="outline-secondary" size="sm" onClick={() => removeFromCart(item.id)}>
                          Remove
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Card.Body>
        </Card>
      </Col>

      <Col lg={4}>
        <Card className="border-0 shadow-sm">
          <Card.Body>
            <h4 className="fw-bold">Order Summary</h4>
            <Form.Group className="mb-3">
              <Form.Label>Choose Date</Form.Label>
              <Form.Control
                type="date"
                value={deliveryDate}
                min={new Date().toISOString().split("T")[0]}
                onChange={(event) => setDeliveryDate(event.target.value)}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Choose Time Slot</Form.Label>
              <Form.Control
                type="time"
                value={deliveryTime}
                onChange={(event) => setDeliveryTime(event.target.value)}
              />
            </Form.Group>

            <p className="text-muted">
              Total items selected:{" "}
              <span className="fw-semibold">
                {cartItems.reduce((total, item) => total + item.selectedQuantity, 0)}
              </span>
            </p>

            <ul className="list-group list-group-flush mb-4">
              {cartItems.map((item) => (
                <li className="list-group-item px-0 d-flex justify-content-between" key={item.id}>
                  <span>{item.name}</span>
                  <span>
                    {item.selectedQuantity} {item.unit}
                  </span>
                </li>
              ))}
            </ul>

            <Button
              className="w-100"
              variant="primary"
              onClick={handlePlaceOrder}
              disabled={cartItems.length === 0 || loading}
            >
              {loading ? "Placing Order..." : "Place Order"}
            </Button>
          </Card.Body>
        </Card>

        <Card className="border-0 shadow-sm mt-4">
          <Card.Body>
            <h5 className="fw-bold">My Orders</h5>
            {userOrders.length === 0 ? (
              <p className="text-muted mb-0">No orders found yet.</p>
            ) : (
              <ul className="list-group list-group-flush">
                {userOrders.map((order) => (
                  <li
                    className="list-group-item px-0 d-flex justify-content-between align-items-start"
                    key={order.id}
                  >
                    <div>
                      <div className="fw-semibold">Order #{order.id}</div>
                      <div className="text-muted small">
                        {order.deliveryDate} at {order.deliveryTime}
                      </div>
                      <div className="text-muted small">
                        {order.items.map((item) => `${item.productName} (${item.quantity} ${item.unit})`).join(", ")}
                      </div>
                    </div>
                    <span className="badge bg-primary">
                      {order.status} / {order.paymentStatus}
                    </span>
                    {order.paymentStatus === "PENDING" && (
                      <Button size="sm" className="ms-2" onClick={() => handlePayNow(order.id)}>
                        Pay Now
                      </Button>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
}

export default OrderPage;
