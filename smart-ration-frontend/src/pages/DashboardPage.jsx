import { useEffect, useState } from "react";
import { Alert, Card, Col, Row, Spinner } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { fetchUserDetails } from "../services/authService";
import { fetchUserOrders } from "../services/orderService";

function DashboardPage() {
  const { user } = useAuth();
  const { totalItems } = useCart();
  const [profile, setProfile] = useState(null);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        const [userProfile, bookingData] = await Promise.all([
          fetchUserDetails(user.id),
          fetchUserOrders(user.id)
        ]);
        setProfile(userProfile);
        setOrders(bookingData);
      } catch (apiError) {
        setError(apiError.response?.data?.message || "Unable to load dashboard data.");
      } finally {
        setLoading(false);
      }
    };

    if (user?.id) {
      loadDashboardData();
    }
  }, [user]);

  const quickLinks = [
    {
      title: "Browse Products",
      text: "View available ration stock and add items to your cart.",
      to: "/products"
    },
    {
      title: "Place Order",
      text: "Review selected items and complete your ration request.",
      to: "/order"
    }
  ];

  const activeOrders = orders.filter((order) => order.status === "PENDING_PAYMENT").length;
  const latestOrders = orders.slice(0, 5);

  return (
    <div>
      <section className="dashboard-hero p-4 p-md-5 mb-4">
        <h1 className="fw-bold">Welcome back, {profile?.name || user?.name}</h1>
        <p className="mb-0 text-secondary">
          Manage your ration requests, view stock, and place your order smoothly.
        </p>
      </section>

      <Row className="g-4 mb-4">
        <Col md={4}>
          <Card className="border-0 shadow-sm stat-card">
            <Card.Body>
              <p className="text-muted mb-2">User ID</p>
              <h3 className="fw-bold mb-0">{user?.id}</h3>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="border-0 shadow-sm stat-card">
            <Card.Body>
              <p className="text-muted mb-2">Ration Card</p>
              <h3 className="fw-bold mb-0">{profile?.rationCardNumber || user?.rationCardNumber}</h3>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="border-0 shadow-sm stat-card">
            <Card.Body>
              <p className="text-muted mb-2">Pending Orders</p>
              <h3 className="fw-bold mb-0">{activeOrders}</h3>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row className="g-4 mb-4">
        {quickLinks.map((item) => (
          <Col md={6} key={item.title}>
            <Card className="border-0 shadow-sm h-100">
              <Card.Body>
                <Card.Title className="fw-bold">{item.title}</Card.Title>
                <Card.Text className="text-muted">{item.text}</Card.Text>
                <Link to={item.to} className="btn btn-primary">
                  Open
                </Link>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>

      <Row className="g-4">
        <Col lg={5}>
          <Card className="border-0 shadow-sm h-100">
            <Card.Body>
              <h5 className="fw-bold">Quick Summary</h5>
              <p className="text-muted mb-3">Live details from your ration account.</p>
              <ul className="list-group list-group-flush">
                <li className="list-group-item px-0 d-flex justify-content-between">
                  <span>Registered phone</span>
                  <strong>{profile?.phone || "Loading..."}</strong>
                </li>
                <li className="list-group-item px-0 d-flex justify-content-between">
                  <span>Items in cart</span>
                  <strong>{totalItems}</strong>
                </li>
                <li className="list-group-item px-0 d-flex justify-content-between">
                  <span>Total orders</span>
                  <strong>{orders.length}</strong>
                </li>
                <li className="list-group-item px-0 d-flex justify-content-between">
                  <span>Active orders</span>
                  <strong>{activeOrders}</strong>
                </li>
              </ul>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={7}>
          <Card className="border-0 shadow-sm h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h5 className="fw-bold mb-0">Recent Orders</h5>
                <Link to="/order" className="btn btn-outline-primary btn-sm">
                  Manage Orders
                </Link>
              </div>

              {loading ? (
                <div className="text-center py-4">
                  <Spinner animation="border" variant="primary" />
                </div>
              ) : error ? (
                <Alert variant="danger" className="mb-0">
                  {error}
                </Alert>
              ) : latestOrders.length === 0 ? (
                <Alert variant="info" className="mb-0">
                  No orders found yet. Place your first order from the order page.
                </Alert>
              ) : (
                <div className="list-group list-group-flush">
                  {latestOrders.map((order) => (
                    <div
                      className="list-group-item px-0 d-flex justify-content-between align-items-start"
                      key={order.id}
                    >
                      <div>
                        <div className="fw-semibold">Order #{order.id}</div>
                        <div className="text-muted small">
                          {order.deliveryDate} at {order.deliveryTime}
                        </div>
                      </div>
                      <span className="badge bg-primary">{order.status}</span>
                    </div>
                  ))}
                </div>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default DashboardPage;
