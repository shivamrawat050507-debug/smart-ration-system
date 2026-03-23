import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row } from "react-bootstrap";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { loginUser } from "../services/authService";
import { isEmpty } from "../utils/validators";

function LoginPage() {
  const [formData, setFormData] = useState({
    rationCardNumber: "",
    password: ""
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (sessionStorage.getItem("smartRationSessionExpired") === "true") {
      setError("Your session expired. Please log in again.");
      sessionStorage.removeItem("smartRationSessionExpired");
    }
  }, []);

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  const validateForm = () => {
    if (isEmpty(formData.rationCardNumber) || isEmpty(formData.password)) {
      setError("Both fields are required.");
      return false;
    }
    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);
      const response = await loginUser(formData);
      login(response);
      navigate(location.state?.from?.pathname || "/dashboard");
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Row className="justify-content-center align-items-center min-vh-75">
      <Col md={6} lg={5}>
        <Card className="border-0 shadow-lg auth-card">
          <Card.Body className="p-4 p-md-5">
            <div className="mb-4 text-center">
              <h2 className="fw-bold">Login</h2>
              <p className="text-muted mb-0">Access your ration dashboard and orders.</p>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Ration Card Number</Form.Label>
                <Form.Control
                  type="text"
                  name="rationCardNumber"
                  value={formData.rationCardNumber}
                  onChange={handleChange}
                  placeholder="Enter ration card number"
                />
              </Form.Group>

              <Form.Group className="mb-4">
                <Form.Label>Password</Form.Label>
                <Form.Control
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Enter password"
                />
              </Form.Group>

              <Button type="submit" className="w-100" disabled={loading}>
                {loading ? "Logging in..." : "Login"}
              </Button>
            </Form>

            <p className="text-center mt-4 mb-0">
              New user? <Link to="/register">Create an account</Link>
            </p>
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
}

export default LoginPage;
