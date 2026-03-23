import { useEffect, useState } from "react";
import { Alert, Badge, Card, Col, Row, Spinner, Table } from "react-bootstrap";
import { useAuth } from "../context/AuthContext";
import { fetchDashboardOverview } from "../services/dashboardService";

function DashboardPage() {
  const { user } = useAuth();
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadOverview = async () => {
      try {
        const data = await fetchDashboardOverview(user.id);
        setOverview(data);
      } catch (apiError) {
        setError(apiError.response?.data?.message || "Unable to load the smart ration dashboard.");
      } finally {
        setLoading(false);
      }
    };

    if (user?.id) {
      loadOverview();
    }
  }, [user]);

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" />
      </div>
    );
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>;
  }

  const severityClass = {
    high: "danger",
    medium: "warning",
    low: "info"
  };
  const isBeneficiary = overview.profile.role === "ROLE_USER";

  return (
    <div className="d-grid gap-4">
      <section className="dashboard-hero p-4 p-md-5">
        <div className="d-flex flex-column flex-lg-row justify-content-between gap-3">
          <div>
            <p className="eyebrow mb-2">State, City & Depot-Aware Operations</p>
            <h1 className="fw-bold mb-3">{overview.profile.name}</h1>
            <p className="lead-text mb-3">
              {isBeneficiary
                ? "Your profile only shows personal entitlement, issued quantity, and pending balance."
                : "Centralized controls unify ration policy, depot stock, and anti-fraud verification in one platform."}
            </p>
            <div className="d-flex flex-wrap gap-2">
              <Badge bg="light" text="dark" className="hero-badge">
                {overview.profile.stateName}
              </Badge>
              <Badge bg="light" text="dark" className="hero-badge">
                {overview.profile.cityName}
              </Badge>
              <Badge bg="light" text="dark" className="hero-badge">
                {overview.profile.depotCode}
              </Badge>
            </div>
          </div>
          <Card className="glass-card hero-side-card">
            <Card.Body>
              <p className="text-uppercase small text-muted mb-2">Assigned collection point</p>
              <h4 className="fw-bold mb-1">{overview.profile.depotName}</h4>
              <p className="text-muted mb-2">{overview.profile.rationCardNumber}</p>
              <strong>{overview.profile.beneficiaryStatus}</strong>
            </Card.Body>
          </Card>
        </div>
      </section>

      <Row className="g-4">
        {overview.metrics.map((metric) => (
          <Col md={6} xl={3} key={metric.label}>
            <Card className="border-0 shadow-sm metric-card h-100">
              <Card.Body>
                <p className="text-muted mb-2">{metric.label}</p>
                <h3 className="fw-bold mb-2">{metric.value}</h3>
                <p className="small text-muted mb-0">{metric.helper}</p>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>

      <Row className="g-4">
        <Col lg={7}>
          <Card className="border-0 shadow-sm h-100">
            <Card.Body>
              <div className="section-title-row">
                <div>
                  <h5 className="fw-bold mb-1">Monthly Entitlement</h5>
                  <p className="text-muted mb-0">
                    {overview.entitlement.entitlementMonth} • {overview.entitlement.rationCategory}
                  </p>
                </div>
                <Badge bg="success">
                  {overview.entitlement.familyMembers} members
                </Badge>
              </div>
              <Table responsive hover className="align-middle mt-3 mb-3">
                <thead>
                  <tr>
                    <th>Commodity</th>
                    <th>Entitled</th>
                    <th>Issued</th>
                    <th>Pending</th>
                    {!isBeneficiary ? <th>Depot Stock</th> : null}
                  </tr>
                </thead>
                <tbody>
                  {overview.entitlement.items.map((item) => (
                    <tr key={item.commodity}>
                      <td>{item.commodity}</td>
                      <td>{item.entitlement} {item.unit}</td>
                      <td>{item.issued} {item.unit}</td>
                      <td>{item.pending} {item.unit}</td>
                      {!isBeneficiary ? <td>{item.depotAvailable} {item.unit}</td> : null}
                    </tr>
                  ))}
                </tbody>
              </Table>
              <p className="text-muted mb-0">{overview.entitlement.policyNote}</p>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={5}>
          <Card className="border-0 shadow-sm h-100">
            <Card.Body>
              {isBeneficiary ? (
                <>
                  <h5 className="fw-bold mb-1">Personal Details</h5>
                  <p className="text-muted">Depot stock is hidden from beneficiaries.</p>
                  <div className="d-grid gap-3">
                    <div className="feature-pill">
                      <div className="small text-muted mb-1">Name</div>
                      <strong>{overview.profile.name}</strong>
                    </div>
                    <div className="feature-pill">
                      <div className="small text-muted mb-1">Ration Card Number</div>
                      <strong>{overview.profile.rationCardNumber}</strong>
                    </div>
                    <div className="feature-pill">
                      <div className="small text-muted mb-1">Assigned Depot</div>
                      <strong>{overview.profile.depotName}</strong>
                    </div>
                    <div className="feature-pill">
                      <div className="small text-muted mb-1">Status</div>
                      <strong>{overview.profile.beneficiaryStatus}</strong>
                    </div>
                  </div>
                </>
              ) : (
                <>
                  <h5 className="fw-bold mb-1">Fraud Prevention Controls</h5>
                  <p className="text-muted">Rule-based protection built into every distribution step.</p>
                  <div className="d-grid gap-3">
                    {overview.fraudControls.map((control) => (
                      <div key={control} className="feature-pill">
                        {control}
                      </div>
                    ))}
                  </div>
                </>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row className="g-4">
        <Col lg={isBeneficiary ? 12 : 6}>
          {isBeneficiary ? (
            <Card className="border-0 shadow-sm h-100">
              <Card.Body>
                <h5 className="fw-bold mb-1">Issued Summary</h5>
                <p className="text-muted">Only issued and pending quantities are shown in the beneficiary profile.</p>
                <div className="d-grid gap-3">
                  {overview.entitlement.items.map((item) => (
                    <div key={item.commodity} className="stock-row">
                      <div>
                        <div className="fw-semibold">{item.commodity}</div>
                        <div className="small text-muted">
                          Issued {item.issued} {item.unit} • Pending {item.pending} {item.unit}
                        </div>
                      </div>
                      <Badge bg={item.pending > 0 ? "warning" : "success"}>
                        {item.issued} {item.unit}
                      </Badge>
                    </div>
                  ))}
                </div>
              </Card.Body>
            </Card>
          ) : (
            <Card className="border-0 shadow-sm h-100">
              <Card.Body>
                <h5 className="fw-bold mb-1">Depot Stock Snapshot</h5>
                <p className="text-muted">
                  {overview.depotSnapshot.depotName} • {overview.depotSnapshot.cityName}
                </p>
                <div className="d-grid gap-3">
                  {overview.depotSnapshot.stockItems.map((item) => (
                    <div key={item.commodity} className="stock-row">
                      <div>
                        <div className="fw-semibold">{item.commodity}</div>
                        <div className="small text-muted">
                          Available {item.available} kg • Demand {item.monthlyDemand} kg
                        </div>
                      </div>
                      <Badge bg={item.status === "Critical" ? "danger" : item.status === "Watch" ? "warning" : "success"}>
                        {item.status}
                      </Badge>
                    </div>
                  ))}
                </div>
                <div className="info-box mt-3">
                  <strong>Partial distribution:</strong> {overview.depotSnapshot.partialDistributionRule}
                </div>
                <div className="info-box mt-3">
                  <strong>Unclaimed expiry:</strong> {overview.depotSnapshot.unclaimedRule}
                </div>
              </Card.Body>
            </Card>
          )}
        </Col>

        <Col lg={isBeneficiary ? 12 : 6}>
          <Card className="border-0 shadow-sm h-100">
            <Card.Body>
              <h5 className="fw-bold mb-1">{isBeneficiary ? "Profile Alerts" : "Operational Alerts"}</h5>
              <p className="text-muted">
                {isBeneficiary
                  ? "Only personal entitlement alerts are visible in the beneficiary profile."
                  : "These alerts show how central tracking surfaces risks early."}
              </p>
              <div className="d-grid gap-3">
                {overview.alerts.map((alert) => (
                  <Alert key={alert.title} variant={severityClass[alert.severity] || "secondary"} className="mb-0">
                    <div className="fw-semibold">{alert.title}</div>
                    <div>{alert.detail}</div>
                  </Alert>
                ))}
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default DashboardPage;
