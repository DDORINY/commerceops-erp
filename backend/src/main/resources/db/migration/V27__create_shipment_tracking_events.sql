CREATE TABLE IF NOT EXISTS shipment_tracking_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    description VARCHAR(500) NOT NULL,
    event_at DATETIME(6) NOT NULL,
    raw_payload TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_shipment_tracking_events_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
    INDEX idx_shipment_tracking_events_shipment_id (shipment_id),
    INDEX idx_shipment_tracking_events_event_at (event_at),
    INDEX idx_shipment_tracking_events_status (status)
);
