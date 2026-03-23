import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Form, Row, Spinner, Table } from "react-bootstrap";
import { useAuth } from "../context/AuthContext";
import {
  addCity,
  addDepot,
  allocateStockToDepot,
  fetchDashboardOverview,
  updateRationRule
} from "../services/dashboardService";

const initialForms = {
  stock: { depotCode: "", commodity: "Wheat", quantity: "" },
  rule: { stateCode: "HR", rationCategory: "BPL", commodityName: "Wheat", unit: "kg", quantityPerPerson: "" },
  city: { stateCode: "HR", cityCode: "", cityName: "", population: "" },
  depot: { cityCode: "", depotCode: "", depotName: "", address: "" }
};

function AdminInsightsPage() {
  const { user } = useAuth();
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [forms, setForms] = useState(initialForms);

  const loadOverview = async () => {
    try {
      setOverview(await fetchDashboardOverview(user.id));
      setError("");
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Unable to load admin insights.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.id) {
      loadOverview();
    }
  }, [user]);

  const handleChange = (section, field, value) => {
    setForms((current) => ({
      ...current,
      [section]: {
        ...current[section],
        [field]: value
      }
    }));
  };

  const runAction = async (action, payload, successMessage, resetSection) => {
    try {
      setError("");
      setSuccess("");
      await action(payload);
      setSuccess(successMessage);
      if (resetSection) {
        setForms((current) => ({ ...current, [resetSection]: initialForms[resetSection] }));
      }
      await loadOverview();
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Action failed.");
    }
  };

  if (loading) {
    return <Spinner animation="border" />;
  }

  if (!overview) {
    return <Alert variant="danger">Unable to load admin dashboard.</Alert>;
  }

  return (
    <div className="d-grid gap-4">
      {error ? <Alert variant="danger">{error}</Alert> : null}
      {success ? <Alert variant="success">{success}</Alert> : null}

      <Row className="g-4">
        <Col md={6} xl={3}>
          <Card className="border-0 shadow-sm metric-card h-100">
            <Card.Body>
              <p className="text-muted mb-2">Total Members</p>
              <h3 className="fw-bold">{overview.adminAnalytics.totalMembers.toLocaleString()}</h3>
              <p className="small text-muted mb-0">Real family-member count across active cities</p>
            </Card.Body>
          </Card>
        </Col>
        <Col md={6} xl={3}>
          <Card className="border-0 shadow-sm metric-card h-100">
            <Card.Body>
              <p className="text-muted mb-2">Total Wheat Required</p>
              <h3 className="fw-bold">{overview.adminAnalytics.totalWheatRequired.toLocaleString()} kg</h3>
              <p className="small text-muted mb-0">{overview.adminAnalytics.demandTrend}</p>
            </Card.Body>
          </Card>
        </Col>
        <Col md={6} xl={3}>
          <Card className="border-0 shadow-sm metric-card h-100">
            <Card.Body>
              <p className="text-muted mb-2">Total Stock Available</p>
              <h3 className="fw-bold">{overview.adminAnalytics.totalStockAvailable.toLocaleString()} kg</h3>
              <p className="small text-muted mb-0">Across all depot commodities</p>
            </Card.Body>
          </Card>
        </Col>
        <Col md={6} xl={3}>
          <Card className="border-0 shadow-sm metric-card h-100">
            <Card.Body>
              <p className="text-muted mb-2">Total Distributed</p>
              <h3 className="fw-bold">{overview.adminAnalytics.totalDistributed.toLocaleString()} kg</h3>
              <p className="small text-muted mb-0">{overview.adminAnalytics.stockUsageTrend}</p>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Card className="border-0 shadow-sm">
        <Card.Body>
          <h4 className="fw-bold">City-Wise Demand Calculation Panel</h4>
          <p className="text-muted">Calculated dynamically from family counts, ration rules, entitlement demand, and depot wheat stock.</p>
          <Table responsive hover className="align-middle mb-0">
            <thead>
              <tr>
                <th>City</th>
                <th>Total Population</th>
                <th>Wheat/Person</th>
                <th>Total Required</th>
                <th>Available Stock</th>
                <th>Shortage / Surplus</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {overview.cityDemandInsights.map((city) => (
                <tr key={city.cityName}>
                  <td>{city.cityName}</td>
                  <td>{city.totalPopulation.toLocaleString()}</td>
                  <td>{city.wheatPerPerson.toFixed(2)} kg</td>
                  <td>{city.totalRequiredStock.toLocaleString()} kg</td>
                  <td>{city.availableStock.toLocaleString()} kg</td>
                  <td className={city.shortageOrSurplus < 0 ? "text-danger fw-semibold" : "text-success fw-semibold"}>
                    {city.shortageOrSurplus < 0 ? "Shortage" : "Surplus"} {Math.abs(city.shortageOrSurplus).toLocaleString()} kg
                  </td>
                  <td>{city.status}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      <Card className="border-0 shadow-sm">
        <Card.Body>
          <h4 className="fw-bold">Depot-Level Stock Breakdown</h4>
          <p className="text-muted">Depot stock, distributed quantity, and remaining balance across monitored depots.</p>
          <Table responsive hover className="align-middle mb-0">
            <thead>
              <tr>
                <th>Depot</th>
                <th>City</th>
                <th>Total Stock</th>
                <th>Distributed Quantity</th>
                <th>Remaining Stock</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {overview.depotInsights.map((depot) => (
                <tr key={depot.depotCode}>
                  <td>{depot.depotCode} - {depot.depotName}</td>
                  <td>{depot.cityName}</td>
                  <td>{depot.totalStock.toLocaleString()} kg</td>
                  <td>{depot.distributedQuantity.toLocaleString()} kg</td>
                  <td>{depot.remainingStock.toLocaleString()} kg</td>
                  <td>{depot.status}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      <Row className="g-4">
        <Col lg={6}>
          <Card className="border-0 shadow-sm h-100">
            <Card.Body>
              <h5 className="fw-bold">Admin Action Controls</h5>
              <div className="d-grid gap-4 mt-3">
                <div className="feature-pill">
                  <div className="fw-semibold mb-2">Allocate Stock to Depot</div>
                  <Row className="g-2">
                    <Col md={4}>
                      <Form.Control placeholder="Depot Code" value={forms.stock.depotCode} onChange={(e) => handleChange("stock", "depotCode", e.target.value)} />
                    </Col>
                    <Col md={4}>
                      <Form.Control placeholder="Commodity" value={forms.stock.commodity} onChange={(e) => handleChange("stock", "commodity", e.target.value)} />
                    </Col>
                    <Col md={4}>
                      <Form.Control placeholder="Quantity" value={forms.stock.quantity} onChange={(e) => handleChange("stock", "quantity", e.target.value)} />
                    </Col>
                  </Row>
                  <Button className="mt-3" onClick={() => runAction(allocateStockToDepot, { ...forms.stock, quantity: Number(forms.stock.quantity) }, "Stock allocated successfully.", "stock")}>
                    Allocate Stock to Depot
                  </Button>
                </div>

                <div className="feature-pill">
                  <div className="fw-semibold mb-2">Update Ration Rules</div>
                  <Row className="g-2">
                    <Col md={3}>
                      <Form.Control value={forms.rule.stateCode} onChange={(e) => handleChange("rule", "stateCode", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control value={forms.rule.rationCategory} onChange={(e) => handleChange("rule", "rationCategory", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control value={forms.rule.commodityName} onChange={(e) => handleChange("rule", "commodityName", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control placeholder="Qty/person" value={forms.rule.quantityPerPerson} onChange={(e) => handleChange("rule", "quantityPerPerson", e.target.value)} />
                    </Col>
                  </Row>
                  <Button className="mt-3" onClick={() => runAction(updateRationRule, { ...forms.rule, quantityPerPerson: Number(forms.rule.quantityPerPerson) }, "Ration rule updated successfully.", "rule")}>
                    Update Ration Rules
                  </Button>
                </div>

                <div className="feature-pill">
                  <div className="fw-semibold mb-2">Add City</div>
                  <Row className="g-2">
                    <Col md={3}>
                      <Form.Control value={forms.city.stateCode} onChange={(e) => handleChange("city", "stateCode", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control placeholder="City Code" value={forms.city.cityCode} onChange={(e) => handleChange("city", "cityCode", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control placeholder="City Name" value={forms.city.cityName} onChange={(e) => handleChange("city", "cityName", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control placeholder="Population" value={forms.city.population} onChange={(e) => handleChange("city", "population", e.target.value)} />
                    </Col>
                  </Row>
                  <Button className="mt-3" onClick={() => runAction(addCity, { ...forms.city, population: Number(forms.city.population) }, "City added successfully.", "city")}>
                    Add City
                  </Button>
                </div>

                <div className="feature-pill">
                  <div className="fw-semibold mb-2">Add Depot</div>
                  <Row className="g-2">
                    <Col md={3}>
                      <Form.Control placeholder="City Code" value={forms.depot.cityCode} onChange={(e) => handleChange("depot", "cityCode", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control placeholder="Depot Code" value={forms.depot.depotCode} onChange={(e) => handleChange("depot", "depotCode", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control placeholder="Depot Name" value={forms.depot.depotName} onChange={(e) => handleChange("depot", "depotName", e.target.value)} />
                    </Col>
                    <Col md={3}>
                      <Form.Control placeholder="Address" value={forms.depot.address} onChange={(e) => handleChange("depot", "address", e.target.value)} />
                    </Col>
                  </Row>
                  <Button className="mt-3" onClick={() => runAction(addDepot, forms.depot, "Depot added successfully.", "depot")}>
                    Add Depot
                  </Button>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={6}>
          <Card className="border-0 shadow-sm h-100">
            <Card.Body>
              <h5 className="fw-bold">Demand & Fraud Monitoring</h5>
              <div className="info-box mb-3">
                Current Month Demand: {overview.adminAnalytics.currentMonthDemand.toLocaleString()} kg
              </div>
              <div className="info-box mb-3">
                Previous Month Demand: {overview.adminAnalytics.previousMonthDemand.toLocaleString()} kg
              </div>
              <div className="info-box mb-4">
                Stock Usage Trend: {overview.adminAnalytics.stockUsageTrend}
              </div>

              <h6 className="fw-bold">Detailed Fraud Monitoring</h6>
              <Table responsive className="align-middle mt-2">
                <tbody>
                  <tr>
                    <td>Total blocked attempts</td>
                    <td className="fw-semibold">{overview.fraudSummary.totalBlockedAttempts}</td>
                  </tr>
                  <tr>
                    <td>Wrong depot access</td>
                    <td className="fw-semibold">{overview.fraudSummary.wrongDepotAccess}</td>
                  </tr>
                  <tr>
                    <td>Duplicate claims</td>
                    <td className="fw-semibold">{overview.fraudSummary.duplicateClaims}</td>
                  </tr>
                </tbody>
              </Table>

              <h6 className="fw-bold mt-4">Smart System Insights</h6>
              <div className="d-grid gap-2 mt-2">
                {overview.smartInsights.map((insight) => (
                  <Alert key={insight} variant="warning" className="mb-0">
                    {insight}
                  </Alert>
                ))}
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Card className="border-0 shadow-sm">
        <Card.Body>
          <h4 className="fw-bold">System Activity Logs</h4>
          <Table responsive hover className="align-middle mb-0">
            <thead>
              <tr>
                <th>Time</th>
                <th>Type</th>
                <th>Severity</th>
                <th>Message</th>
              </tr>
            </thead>
            <tbody>
              {overview.activityLogs.map((log, index) => (
                <tr key={`${log.timestamp}-${index}`}>
                  <td>{log.timestamp}</td>
                  <td>{log.type}</td>
                  <td className="text-capitalize">{log.severity}</td>
                  <td>{log.message}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
    </div>
  );
}

export default AdminInsightsPage;
