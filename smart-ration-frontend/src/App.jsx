import { Navigate, Route, Routes } from "react-router-dom";
import AppNavbar from "./components/AppNavbar";
import ProtectedRoute from "./components/ProtectedRoute";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import AdminInsightsPage from "./pages/AdminInsightsPage";
import DepotOperationsPage from "./pages/DepotOperationsPage";
import RegisterPage from "./pages/RegisterPage";

function App() {
  return (
    <div className="app-shell">
      <AppNavbar />
      <main className="container py-4">
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/operations"
            element={
              <ProtectedRoute roles={["ROLE_DEALER"]}>
                <DepotOperationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/insights"
            element={
              <ProtectedRoute roles={["ROLE_ADMIN"]}>
                <AdminInsightsPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </div>
  );
}

export default App;
