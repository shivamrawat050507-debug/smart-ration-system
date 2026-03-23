import { Container, Nav, Navbar, Badge, Button } from "react-bootstrap";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

function AppNavbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const { totalItems } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <Navbar bg="white" expand="lg" className="border-bottom shadow-sm">
      <Container>
        <Navbar.Brand as={Link} to={isAuthenticated ? "/dashboard" : "/login"} className="fw-bold text-primary">
          Smart Ration System
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="main-navbar" />
        <Navbar.Collapse id="main-navbar">
          <Nav className="ms-auto align-items-lg-center gap-lg-2">
            {isAuthenticated ? (
              <>
                <Nav.Link as={NavLink} to="/dashboard">
                  Dashboard
                </Nav.Link>
                <Nav.Link as={NavLink} to="/products">
                  Products
                </Nav.Link>
                <Nav.Link as={NavLink} to="/order">
                  Order <Badge bg="primary">{totalItems}</Badge>
                </Nav.Link>
                {isAdmin && (
                  <>
                    <Nav.Link as={NavLink} to="/admin/products">
                      Admin Products
                    </Nav.Link>
                    <Nav.Link as={NavLink} to="/admin/orders">
                      Admin Orders
                    </Nav.Link>
                  </>
                )}
                <span className="navbar-text text-muted me-2">
                  Welcome, {user?.name}
                </span>
                <Button variant="outline-primary" size="sm" onClick={handleLogout}>
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Nav.Link as={NavLink} to="/login">
                  Login
                </Nav.Link>
                <Nav.Link as={NavLink} to="/register">
                  Register
                </Nav.Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default AppNavbar;
