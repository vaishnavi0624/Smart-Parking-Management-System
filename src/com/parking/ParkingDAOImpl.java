package com.parking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ParkingDAO interface.
 * Handles all database operations (CRUD) for the parking system.
 */
public class ParkingDAOImpl implements ParkingDAO {

    /**
     * Checks if a vehicle with the given registration number is already parked in the system.
     * 
     * @param conn Open database connection.
     * @param vehicleNumber The vehicle license plate to check.
     * @return true if the vehicle is already parked, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    private boolean isVehicleAlreadyParked(Connection conn, String vehicleNumber) throws SQLException {
        if (vehicleNumber == null) return false;
        
        // Count active parking spots holding this vehicle number (case-insensitive)
        String query = "SELECT COUNT(*) FROM parking_spots WHERE UPPER(vehicle_number) = ? AND is_available = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, vehicleNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Verifies if a specific Spot ID exists in the database.
     * 
     * @param conn Open database connection.
     * @param spotId The ID of the spot to verify.
     * @return true if the spot exists, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    private boolean doesSpotExist(Connection conn, int spotId) throws SQLException {
        String query = "SELECT COUNT(*) FROM parking_spots WHERE spot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, spotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Finds an available spot and parks the vehicle.
     * 
     * @param vehicleNumber The vehicle's license plate.
     * @param vehicleType The type of vehicle (CAR or BIKE).
     * @return true if successfully parked, false if validation fails or parking lot is full.
     */
    @Override
    public boolean parkVehicle(String vehicleNumber, String vehicleType) {
        // Reject null or empty values
        if (vehicleNumber == null || vehicleType == null || vehicleNumber.trim().isEmpty()) {
            System.out.println("❌ DAO Validation Error: Vehicle parameters cannot be null or empty.");
            return false;
        }

        String normalizedType = vehicleType.trim().toUpperCase();

        // Enforce supported vehicle types
        if (!normalizedType.equals("CAR") && !normalizedType.equals("BIKE")) {
            System.out.println("❌ DAO Validation Error: System only supports 'CAR' or 'BIKE'. Cannot park: " + normalizedType);
            return false;
        }

        String findSpotQuery = "SELECT spot_id FROM parking_spots WHERE UPPER(vehicle_type) = ? AND is_available = TRUE LIMIT 1";
        String allocateSpotQuery = "UPDATE parking_spots SET vehicle_number = ?, is_available = FALSE, allocated_time = NOW() WHERE spot_id = ?";
        
        // Auto-closes database connection when done here we check duplication of user input
        try (Connection conn = DBConnection.getConnection()) {
            // Prevent duplicate parking entries for the same vehicle
            if (isVehicleAlreadyParked(conn, vehicleNumber)) {
                System.out.println("❌ Validation Error: Vehicle " + vehicleNumber.toUpperCase() + " is already parked inside!");
                return false;
            }

            // Step 1: Find the first available spot for this vehicle type
            int availableSpotId = -1;
            try (PreparedStatement psFind = conn.prepareStatement(findSpotQuery)) {
                psFind.setString(1, normalizedType);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next()) availableSpotId = rs.getInt("spot_id");
                }
            }

            // Stop if parking lot is completely full for this type
            if (availableSpotId == -1) {
                System.out.println("❌ Error: No available slots for vehicle type: " + normalizedType);
                return false;
            }

            // Step 2: Assign the vehicle to the found spot
            try (PreparedStatement psAllocate = conn.prepareStatement(allocateSpotQuery)) {
                psAllocate.setString(1, vehicleNumber.trim().toUpperCase());
                psAllocate.setInt(2, availableSpotId);
                
                int rowsUpdated = psAllocate.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("✅ Success: Vehicle parked successfully at Spot ID: " + availableSpotId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error during parking sequence execution: " + e.getMessage());
        }
        return false;
    }

    /**
     * Unparks a vehicle, calculates the total bill based on elapsed hours, and frees up the spot.
     * 
     * @param spotId The ID of the spot to free.
     * @return true if successfully unparked, false otherwise.
     */
    @Override
    public boolean unparkVehicle(int spotId) {
        if (spotId <= 0) {
            System.out.println("❌ Validation Error: Invalid Spot ID.");
            return false;
        }

        // Fetch spot details and calculate total hours spent using SQL
        String detailsQuery = "SELECT vehicle_number, vehicle_type, hourly_rate, allocated_time, " +
                              "TIMESTAMPDIFF(HOUR, allocated_time, NOW()) AS hours_spent " +
                              "FROM parking_spots WHERE spot_id = ?";
                              
        // Clear out the spot columns to make it available again
        String unparkQuery = "UPDATE parking_spots SET vehicle_number = NULL, is_available = TRUE, allocated_time = NULL WHERE spot_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            // Make sure the spot actually exists
            if (!doesSpotExist(conn, spotId)) {
                System.out.println("❌ Validation Error: Spot ID " + spotId + " does not exist.");
                return false;
            }

            String vehicleNumber = "";
            String vehicleType = "";
            double hourlyRate = 0.0;
            long hoursSpent = 0;

            // Step 1: Read current spot details
            try (PreparedStatement psDetails = conn.prepareStatement(detailsQuery)) {
                psDetails.setInt(1, spotId);
                try (ResultSet rs = psDetails.executeQuery()) {
                    if (rs.next()) {
                        // Check if the spot is already empty
                        if (rs.getString("vehicle_number") == null) {
                            System.out.println("❌ Validation Error: Spot ID " + spotId + " is already vacant.");
                            return false;
                        }
                        
                        vehicleNumber = rs.getString("vehicle_number");
                        vehicleType = rs.getString("vehicle_type");
                        hourlyRate = rs.getDouble("hourly_rate");
                        
                        // Handle potential missing allocation timestamps safely
                        Timestamp allocatedTime = rs.getTimestamp("allocated_time");
                        if (allocatedTime == null) {
                            System.out.println("⚠️ Database Warning: allocated_time was NULL. Falling back to base 1-hour charge.");
                            hoursSpent = 1;
                        } else {
                            hoursSpent = rs.getLong("hours_spent");
                            // Default to 1 hour if parked for less than 60 minutes
                            if (rs.wasNull() || hoursSpent < 1) {
                                hoursSpent = 1; 
                            }
                        }
                    }
                }
            }

            // Step 2: Calculate total cost
            double totalCost = hoursSpent * hourlyRate;

            // Step 3: Clear database records for this spot
            try (PreparedStatement psUnpark = conn.prepareStatement(unparkQuery)) {
                psUnpark.setInt(1, spotId);
                int rowsUpdated = psUnpark.executeUpdate();
                
                if (rowsUpdated > 0) {
                    // Print final invoice to console
                    System.out.println("\n=====================================");
                    System.out.println("        PARKING RECEIPT & BILL        ");
                    System.out.println("=====================================");
                    System.out.println(" Spot Released   : " + spotId);
                    System.out.println(" Vehicle Number : " + vehicleNumber);
                    System.out.println(" Vehicle Type   : " + vehicleType);
                    System.out.println(" Duration       : " + hoursSpent + " Hour(s)");
                    System.out.println(" Dynamic Rate   : Rs. " + hourlyRate + " /hr");
                    System.out.println("-------------------------------------");
                    System.out.println(" TOTAL PAYABLE  : Rs. " + totalCost);
                    System.out.println("=====================================\n");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error during unparking sequence execution: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all parking spots from the database.
     * 
     * @return A list of all ParkingSpot objects.
     */
    @Override
    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> spots = new ArrayList<>();
        String query = "SELECT * FROM parking_spots";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            // Loop through database records and convert them to Java objects
            while (rs.next()) {
                spots.add(new ParkingSpot(
                        rs.getInt("spot_id"),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        rs.getBoolean("is_available"),
                        rs.getTimestamp("allocated_time"),
                        rs.getDouble("hourly_rate")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error fetching system layout records: " + e.getMessage());
        }
        return spots;
    }

    /**
     * Filters and retrieves only available parking spots matching a specific vehicle type.
     * 
     * @param vehicleType The type of vehicle (CAR or BIKE).
     * @return A list of available ParkingSpot objects for that type.
     */
    @Override
    public List<ParkingSpot> getAvailableSpotsByVehicleType(String vehicleType) {
        List<ParkingSpot> spots = new ArrayList<>();
        if (vehicleType == null || vehicleType.trim().isEmpty()) return spots;

        String normalizedType = vehicleType.trim().toUpperCase();
        
        // Validate search type before hitting the database
        if (!normalizedType.equals("CAR") && !normalizedType.equals("BIKE")) {
            System.out.println("❌ DAO Validation Error: Invalid type search query.");
            return spots;
        }

        String query = "SELECT * FROM parking_spots WHERE UPPER(vehicle_type) = ? AND is_available = TRUE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, normalizedType);
            try (ResultSet rs = ps.executeQuery()) {
                // Populate the list with matching free spaces
                while (rs.next()) {
                    spots.add(new ParkingSpot(
                            rs.getInt("spot_id"),
                            rs.getString("vehicle_number"),
                            rs.getString("vehicle_type"),
                            rs.getBoolean("is_available"),
                            rs.getTimestamp("allocated_time"),
                            rs.getDouble("hourly_rate")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error fetching vehicle status layout: " + e.getMessage());
        }
        return spots;
    }
}