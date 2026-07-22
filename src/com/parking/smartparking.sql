DROP DATABASE if exists smart_parking;
CREATE DATABASE IF NOT EXISTS smart_parking;
USE smart_parking;

DROP TABLE IF EXISTS parking_spots;

CREATE TABLE parking_spots (
    spot_id INT PRIMARY KEY,
    vehicle_number VARCHAR(20) DEFAULT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    allocated_time TIMESTAMP NULL DEFAULT NULL,
    hourly_rate DECIMAL(6, 2) NOT NULL -- New Column added
);

-- Insert test slots with explicit hourly rates
INSERT INTO parking_spots (spot_id, vehicle_number, vehicle_type, is_available, allocated_time, hourly_rate) VALUES 
(101, NULL, 'CAR', TRUE, NULL, 100.00),
(102, NULL, 'CAR', TRUE, NULL, 100.00),
(201, NULL, 'BIKE', TRUE, NULL, 50.00),
(202, NULL, 'BIKE', TRUE, NULL, 50.00);

SELECT * FROM parking_spots;
