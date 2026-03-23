import { useState } from "react";
import { Alert, Button, Card, Col, Form, Row } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../services/authService";
import { hasMinLength, isEmpty, isValidPhone } from "../utils/validators";

function RegisterPage() {
  const [formData, setFormData] = useState({
    name: "",
    rationCardNumber: "",
    phone: "",
    password: ""
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  const validateForm = () => {
    if (Object.values(formData).some((value) => isEmpty(value))) {
      setError("All fields are required.");
      return false;
    }

    if (!isValidPhone(formData.phone)) {
      setError("Phone number must be 10 digits.");
      return false;
    }

    if (!hasMinLength(formData.password, 6)) {
      setError("Password must be at least 6 characters.");
      return false;
    }

    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);
      await registerUser(formData);
      setSuccess("Registration successful. Redirecting to login page...");
      setTimeout(() => navigate("/login"), 1500);
    } catch (apiError) {
      if (typeof apiError.response?.data === "object") {
        const firstMessage = Object.values(apiError.response.data)[0];
        setError(firstMessage || "Registration failed.");
      } else {
        setError(apiError.response?.data?.message || "Registration failed.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Row className="justify-content-center align-items-center min-vh-75">
      <Col md={7} lg={6}>
        <Card className="border-0 shadow-lg auth-card">
          <Card.Body className="p-4 p-md-5">
            <div className="mb-4 text-center">
              <h2 className="fw-bold">Register</h2>
              <p className="text-muted mb-0">Create your ration account to order supplies online.</p>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}
            {success && <Alert variant="success">{success}</Alert>}

            <Form onSubmit={handleSubmit}>
              <Row>
                <Col md={6}>
                  <Form.Group className="mb-3">
                    <Form.Label>Full Name</Form.Label>
                    <Form.Control
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleChange}
                      placeholder="Enter your name"
                    />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group className="mb-3">
                    <Form.Label>Phone Number</Form.Label>
                    <Form.Control
                      type="text"
                      name="phone"
                      value={formData.phone}
                      onChange={handleChange}
                      placeholder="10-digit mobile number"
                    />
                  </Form.Group>
                </Col>
              </Row>

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
                  placeholder="Create a password"
                />
              </Form.Group>

              <Button type="submit" className="w-100" disabled={loading}>
                {loading ? "Registering..." : "Register"}
              </Button>
            </Form>

            <p className="text-center mt-4 mb-0">
              Already have an account? <Link to="/login">Login here</Link>
            </p>
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
}

export default RegisterPage;
