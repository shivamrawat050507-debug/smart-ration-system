import { useEffect, useState } from "react";
import { Alert, Card, Table } from "react-bootstrap";
import { fetchAllOrders } from "../services/orderService";

function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadOrders = async () => {
      try {
        const data = await fetchAllOrders();
        setOrders(data);
      } catch (apiError) {
        setError(apiError.response?.data?.message || "Unable to load orders.");
      }
    };

    loadOrders();
  }, []);

  return (
    <Card className="border-0 shadow-sm">
      <Card.Body>
        <h3 className="fw-bold">Order Management</h3>
        {error && <Alert variant="danger">{error}</Alert>}
        <Table responsive hover>
          <thead>
            <tr>
              <th>Order ID</th>
              <th>User</th>
              <th>Delivery</th>
              <th>Status</th>
              <th>Payment</th>
              <th>Total Items</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>{order.id}</td>
                <td>{order.userName}</td>
                <td>
                  {order.deliveryDate} {order.deliveryTime}
                </td>
                <td>{order.status}</td>
                <td>{order.paymentStatus}</td>
                <td>{order.totalItems}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card.Body>
    </Card>
  );
}

export default AdminOrdersPage;
