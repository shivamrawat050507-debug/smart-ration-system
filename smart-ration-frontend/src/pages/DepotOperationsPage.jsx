import { useEffect, useRef, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, ListGroup, Modal, Row, Spinner, Table } from "react-bootstrap";
import { useAuth } from "../context/AuthContext";
import {
  distributeDealerRation,
  distributeRation,
  fetchDashboardOverview,
  scanDealerCard
} from "../services/dashboardService";

const DEMO_QR_CODE = "QR-GRG-D014-RC4421";

function DepotOperationsPage() {
  const { user } = useAuth();
  const videoRef = useRef(null);
  const streamRef = useRef(null);
  const frameRef = useRef(null);
  const detectorRef = useRef(null);
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [distributionResult, setDistributionResult] = useState(null);
  const [distributionLoading, setDistributionLoading] = useState(false);
  const [scanLoading, setScanLoading] = useState(false);
  const [scanCode, setScanCode] = useState("");
  const [scanResult, setScanResult] = useState(null);
  const [cameraOpen, setCameraOpen] = useState(false);
  const [cameraStarting, setCameraStarting] = useState(false);
  const [cameraError, setCameraError] = useState("");

  useEffect(() => () => {
    stopCamera();
  }, []);

  useEffect(() => {
    const loadOverview = async () => {
      try {
        setOverview(await fetchDashboardOverview(user.id));
      } catch (apiError) {
        setError(apiError.response?.data?.message || "Unable to load depot operations.");
      } finally {
        setLoading(false);
      }
    };

    if (user?.id) {
      loadOverview();
    }
  }, [user]);

  const refreshOverview = async () => {
    const refreshedOverview = await fetchDashboardOverview(user.id);
    setOverview(refreshedOverview);
  };

  const handleBeneficiaryDistribute = async () => {
    try {
      setDistributionLoading(true);
      setError("");
      const result = await distributeRation(user.id);
      setDistributionResult(result);
      await refreshOverview();
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Unable to distribute ration.");
    } finally {
      setDistributionLoading(false);
    }
  };

  const runScan = async (qrCodeValue) => {
    try {
      setScanLoading(true);
      setError("");
      setDistributionResult(null);
      const normalizedQr = qrCodeValue.trim();
      setScanCode(normalizedQr);
      const result = await scanDealerCard(user.id, { qrCodeValue: normalizedQr });
      setScanResult(result);
    } catch (apiError) {
      setScanResult(null);
      setError(apiError.response?.data?.message || "Unable to scan this QR code.");
    } finally {
      setScanLoading(false);
    }
  };

  const handleDealerScan = async (event) => {
    event.preventDefault();
    if (!scanCode.trim()) {
      setError("Scan a QR code before continuing.");
      return;
    }
    await runScan(scanCode);
  };

  const stopCamera = () => {
    if (frameRef.current) {
      cancelAnimationFrame(frameRef.current);
      frameRef.current = null;
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }
    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }
  };

  const closeCameraModal = () => {
    stopCamera();
    setCameraOpen(false);
    setCameraStarting(false);
    setCameraError("");
  };

  const scanVideoFrame = async () => {
    if (!videoRef.current || !detectorRef.current || scanLoading) {
      frameRef.current = requestAnimationFrame(scanVideoFrame);
      return;
    }

    try {
      const barcodes = await detectorRef.current.detect(videoRef.current);
      const qrMatch = barcodes.find((entry) => entry.rawValue);
      if (qrMatch?.rawValue) {
        closeCameraModal();
        await runScan(qrMatch.rawValue);
        return;
      }
    } catch (scannerError) {
      setCameraError("Camera opened, but QR detection failed on this browser.");
      closeCameraModal();
      return;
    }

    frameRef.current = requestAnimationFrame(scanVideoFrame);
  };

  const openCameraScanner = async () => {
    setCameraError("");
    setError("");

    const Detector = window.BarcodeDetector;
    if (!Detector) {
      setCameraError("This browser does not support in-browser QR scanning. Use the QR text field instead.");
      return;
    }

    try {
      const supportedFormats = await Detector.getSupportedFormats();
      if (!supportedFormats.includes("qr_code")) {
        setCameraError("This browser does not support QR detection. Use the QR text field instead.");
        return;
      }

      setCameraOpen(true);
      setCameraStarting(true);
      detectorRef.current = new Detector({ formats: ["qr_code"] });

      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: { ideal: "environment" }
        },
        audio: false
      });

      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        await videoRef.current.play();
      }

      setCameraStarting(false);
      frameRef.current = requestAnimationFrame(scanVideoFrame);
    } catch (scannerError) {
      stopCamera();
      setCameraOpen(false);
      setCameraStarting(false);
      setCameraError("Unable to access the device camera. Check camera permission and try again.");
    }
  };

  const handleDealerDistribute = async () => {
    try {
      setDistributionLoading(true);
      setError("");
      const result = await distributeDealerRation(user.id, { qrCodeValue: scanCode });
      setDistributionResult(result);
      await refreshOverview();
      await runScan(scanCode);
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Unable to distribute ration.");
    } finally {
      setDistributionLoading(false);
    }
  };

  const handleDownloadReceipt = () => {
    const receipt = distributionResult?.receipt || scanResult?.receiptPreview;
    const beneficiaryName = scanResult?.beneficiaryName || overview?.profile?.name;
    const cardNumber = scanResult?.cardNumber || overview?.profile?.rationCardNumber;
    const items = distributionResult?.items || [];

    if (!receipt) {
      return;
    }

    const receiptWindow = window.open("", "_blank", "width=900,height=700");
    if (!receiptWindow) {
      return;
    }

    const itemsMarkup = items
      .map(
        (item) => `
          <tr>
            <td>${item.commodity}</td>
            <td>${item.given} ${item.unit}</td>
            <td>${item.pending} ${item.unit}</td>
          </tr>
        `
      )
      .join("");

    receiptWindow.document.write(`
      <html>
        <head>
          <title>${receipt.receiptNumber}</title>
          <style>
            body { font-family: Arial, sans-serif; padding: 32px; color: #1f2a37; }
            h1 { margin-bottom: 8px; }
            table { width: 100%; border-collapse: collapse; margin-top: 16px; }
            th, td { border: 1px solid #d1d5db; padding: 10px; text-align: left; }
            .meta { margin: 16px 0; }
          </style>
        </head>
        <body>
          <h1>Digital Ration Receipt</h1>
          <div class="meta"><strong>Name:</strong> ${beneficiaryName || "N/A"}</div>
          <div class="meta"><strong>Card Number:</strong> ${cardNumber || "N/A"}</div>
          <div class="meta"><strong>Depot:</strong> ${receipt.collectionDepot}</div>
          <div class="meta"><strong>Date & Time:</strong> ${receipt.issueDate}</div>
          ${items.length > 0 ? `
            <table>
              <thead>
                <tr>
                  <th>Commodity</th>
                  <th>Quantity Given</th>
                  <th>Pending</th>
                </tr>
              </thead>
              <tbody>${itemsMarkup}</tbody>
            </table>
          ` : ""}
          <p style="margin-top: 20px;">${receipt.note}</p>
        </body>
      </html>
    `);
    receiptWindow.document.close();
    receiptWindow.focus();
    receiptWindow.print();
  };

  if (loading) {
    return <Spinner animation="border" />;
  }

  if (error && !overview) {
    return <Alert variant="danger">{error}</Alert>;
  }

  if (user?.role === "ROLE_DEALER") {
    return (
      <div className="dealer-screen-shell">
        <div className="dealer-screen-card">
          <div className="dealer-screen-header">
            <p className="dealer-kicker mb-2">Dealer Distribution Console</p>
            <h2 className="fw-bold mb-2">{overview.profile.depotName}</h2>
            <p className="text-muted mb-0">
              Scan the beneficiary QR and follow the instruction exactly as shown.
            </p>
          </div>

          <Row className="g-3 mb-3">
            {overview.metrics.map((metric) => (
              <Col xs={6} key={metric.label}>
                <Card className="border-0 shadow-sm h-100">
                  <Card.Body>
                    <div className="small text-muted mb-1">{metric.label}</div>
                    <div className="fw-bold fs-4">{metric.value}</div>
                    <div className="small text-muted">{metric.helper}</div>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>

          {error ? <Alert variant="danger" className="mb-3">{error}</Alert> : null}
          {distributionResult ? (
            <Alert variant={distributionResult.distributionMode === "PARTIAL" ? "warning" : "success"} className="mb-3">
              <div className="fw-semibold">Distribution Successful</div>
              <div>{distributionResult.message}</div>
            </Alert>
          ) : null}

          <Card className="border-0 shadow-sm dealer-scan-panel">
            <Card.Body>
              <Form onSubmit={handleDealerScan} className="d-grid gap-3">
                <Button
                  type="button"
                  className="dealer-scan-button"
                  onClick={openCameraScanner}
                  disabled={scanLoading || cameraStarting}
                >
                  {cameraStarting ? "Opening Camera..." : "Scan QR"}
                </Button>
                <Form.Group>
                  <Form.Label className="small text-muted">QR value for scanner demo</Form.Label>
                  <Form.Control
                    size="lg"
                    value={scanCode}
                    onChange={(event) => setScanCode(event.target.value)}
                    placeholder="Scanner fills this automatically in production"
                  />
                </Form.Group>
                <div className="d-grid gap-2">
                  <Button type="submit" variant="outline-dark" disabled={scanLoading}>
                    Fetch Ration Instruction
                  </Button>
                  <Button type="button" variant="link" className="p-0 text-start dealer-demo-link" onClick={() => runScan(DEMO_QR_CODE)}>
                    Use demo QR: {DEMO_QR_CODE}
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>

          {scanResult ? (
            <div className="d-grid gap-3 mt-3">
              <Card className="border-0 shadow-sm dealer-person-card">
                <Card.Body>
                  <div className="dealer-person-grid">
                    <div>
                      <p className="small text-muted mb-1">Beneficiary</p>
                      <h3 className="fw-bold mb-0">{scanResult.beneficiaryName}</h3>
                    </div>
                    <div>
                      <p className="small text-muted mb-1">Members</p>
                      <strong>{scanResult.familyMembers}</strong>
                    </div>
                    <div>
                      <p className="small text-muted mb-1">Category</p>
                      <strong>{scanResult.rationCategory}</strong>
                    </div>
                  </div>
                  <div className="small text-muted mt-3">
                    Card {scanResult.cardNumber} • Depot {scanResult.depotCode}
                  </div>
                </Card.Body>
              </Card>

              <section className={`dealer-instruction-card tone-${scanResult.statusTone}`}>
                <p className="dealer-instruction-label mb-2">Ration Instruction</p>
                <div className="dealer-instruction-headline">{scanResult.headline}</div>
                {scanResult.supportingText ? (
                  <div className="dealer-instruction-support">{scanResult.supportingText}</div>
                ) : null}
                {scanResult.reason ? (
                  <div className="dealer-reason-text">Reason: {scanResult.reason}</div>
                ) : null}
                {scanResult.warningMessage ? (
                  <Alert variant="warning" className="mt-3 mb-0">
                    {scanResult.warningMessage}
                  </Alert>
                ) : null}
              </section>

              {scanResult.instructions.length > 0 ? (
                <Card className="border-0 shadow-sm">
                  <Card.Body>
                    <h5 className="fw-bold mb-3">Commodity Instructions</h5>
                    <div className="d-grid gap-2">
                      {scanResult.instructions.map((item) => (
                        <div key={item.commodity} className="dealer-instruction-row">
                          <div>
                            <strong>{item.commodity}</strong>
                            <div className="small text-muted">{item.primaryLabel}</div>
                          </div>
                          <Badge bg={item.pendingQuantity > 0 ? "warning" : "success"}>
                            {item.giveNowQuantity} {item.unit}
                          </Badge>
                        </div>
                      ))}
                    </div>
                    {scanResult.pendingMessage ? (
                      <Alert variant="warning" className="mt-3 mb-0">
                        {scanResult.pendingMessage}
                      </Alert>
                    ) : null}
                  </Card.Body>
                </Card>
              ) : null}

              <div className="d-grid gap-2">
                <Button
                  size="lg"
                  className="dealer-distribute-button"
                  disabled={!scanResult.canDistribute || distributionLoading}
                  onClick={handleDealerDistribute}
                >
                  {distributionLoading ? "Distributing..." : "Distribute Ration"}
                </Button>
                <Button
                  size="lg"
                  variant="outline-dark"
                  onClick={handleDownloadReceipt}
                  disabled={!distributionResult?.receipt}
                >
                  View Receipt / Download PDF
                </Button>
              </div>
            </div>
          ) : (
            <Card className="border-0 shadow-sm mt-3">
              <Card.Body>
                <div className="dealer-empty-state">
                  <div className="dealer-empty-title">Ready for the next beneficiary</div>
                  <p className="text-muted mb-0">
                    Tap <strong>Scan QR</strong> and the system will tell the dealer exactly what to give.
                  </p>
                </div>
              </Card.Body>
            </Card>
          )}
        </div>

        <Modal show={cameraOpen} onHide={closeCameraModal} centered>
          <Modal.Header closeButton>
            <Modal.Title>Scan Beneficiary QR</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <div className="dealer-camera-frame">
              <video ref={videoRef} className="dealer-camera-video" playsInline muted />
            </div>
            <p className="small text-muted mt-3 mb-0">
              Hold the QR code inside the frame. The scan will trigger automatically.
            </p>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="outline-dark" onClick={closeCameraModal}>
              Close
            </Button>
          </Modal.Footer>
        </Modal>

        {cameraError ? (
          <Alert variant="warning" className="mt-3 mb-0">
            {cameraError}
          </Alert>
        ) : null}
      </div>
    );
  }

  return (
    <Row className="g-4">
      <Col lg={5}>
        <Card className="border-0 shadow-sm h-100">
          <Card.Body>
            <h4 className="fw-bold">Distribution Workflow</h4>
            <p className="text-muted">From QR scan to receipt generation.</p>
            <ListGroup variant="flush">
              {overview.workflowSteps.map((step, index) => (
                <ListGroup.Item key={step} className="px-0">
                  <div className="fw-semibold mb-1">Step {index + 1}</div>
                  <div className="text-muted">{step}</div>
                </ListGroup.Item>
              ))}
            </ListGroup>
          </Card.Body>
        </Card>
      </Col>
      <Col lg={7}>
        <Card className="border-0 shadow-sm mb-4">
          <Card.Body>
            <h4 className="fw-bold">Recent Distribution Trail</h4>
            <p className="text-muted">Central logs keep dealer actions, fraud blocks, and partial issues visible.</p>
            <Table responsive hover className="align-middle mb-0">
              <thead>
                <tr>
                  <th>Transaction</th>
                  <th>Status</th>
                  <th>Mode</th>
                  <th>Summary</th>
                </tr>
              </thead>
              <tbody>
                {overview.recentTransactions.map((txn) => (
                  <tr key={txn.transactionId}>
                    <td>
                      <div className="fw-semibold">{txn.transactionId}</div>
                      <div className="small text-muted">{txn.timestamp}</div>
                    </td>
                    <td>{txn.status}</td>
                    <td>{txn.mode}</td>
                    <td>{txn.summary}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
        <Card className="border-0 shadow-sm">
          <Card.Body>
            <h4 className="fw-bold">Receipt Preview</h4>
            <p className="text-muted">Digital evidence generated after every successful issue.</p>
            {overview.profile.role === "ROLE_USER" ? (
              <div className="mb-3">
                <Alert variant={overview.distributionStatus.partialDistributionExpected ? "warning" : "info"} className="mb-3">
                  <div className="fw-semibold">{overview.distributionStatus.warningMessage}</div>
                  <div>{overview.distributionStatus.pendingMessage}</div>
                </Alert>
                <Button
                  className="w-100 mb-3 distribute-button"
                  disabled={!overview.distributionStatus.eligible || distributionLoading}
                  onClick={handleBeneficiaryDistribute}
                >
                  {distributionLoading ? "Distributing..." : "Distribute Ration"}
                </Button>
              </div>
            ) : null}
            <div className="receipt-card">
              <div className="receipt-grid">
                <div>
                  <span className="receipt-label">Receipt No.</span>
                  <strong>{overview.receiptPreview.receiptNumber}</strong>
                </div>
                <div>
                  <span className="receipt-label">Issued On</span>
                  <strong>{overview.receiptPreview.issueDate}</strong>
                </div>
                <div>
                  <span className="receipt-label">QR Reference</span>
                  <strong>{overview.receiptPreview.qrReference}</strong>
                </div>
                <div>
                  <span className="receipt-label">Collection Depot</span>
                  <strong>{overview.receiptPreview.collectionDepot}</strong>
                </div>
              </div>
              <p className="mb-0 mt-3">{overview.receiptPreview.note}</p>
            </div>
            {distributionResult ? (
              <Alert variant={distributionResult.distributionMode === "PARTIAL" ? "warning" : "success"} className="mt-3 mb-0">
                <div className="fw-semibold">{distributionResult.message}</div>
                <div>{distributionResult.smsStatus}</div>
              </Alert>
            ) : null}
          </Card.Body>
        </Card>
      </Col>
    </Row>
  );
}

export default DepotOperationsPage;
