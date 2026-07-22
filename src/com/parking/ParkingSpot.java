package com.parking;

import java.sql.Timestamp;

/**
 * Data model representing a parking spot in the system.
 * This is a standard POJO (Plain Old Java Object) that maps to a database row.
 */
public class ParkingSpot {
    
    // Unique ID for the parking spot
    private int spotId;
    
    // License plate of the parked vehicle (null if the spot is empty)
    private String vehicleNumber;
    
    // Type of vehicle allowed in this spot (e.g., CAR or BIKE)
    private String vehicleType;
    
    // Status flag: true if the spot is empty, false if occupied
    private boolean isAvailable;
    
    // The exact date and time the vehicle parked
    private Timestamp allocatedTime;
    
    // Price charged per hour for using this spot
    private double hourlyRate;

    /**
     * Default constructor (required for various frameworks and object serialization).
     */
    public ParkingSpot() {}

    /**
     * Overloaded constructor to quickly create a complete ParkingSpot object.
     */
    public ParkingSpot(int spotId, String vehicleNumber, String vehicleType, boolean isAvailable, Timestamp allocatedTime, double hourlyRate) {
        this.spotId = spotId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.isAvailable = isAvailable;
        this.allocatedTime = allocatedTime;
        this.hourlyRate = hourlyRate;
    }

    // --- Getters and Setters ---

    public int getSpotId() { return spotId; }
    public void setSpotId(int spotId) { this.spotId = spotId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public Timestamp getAllocatedTime() { return allocatedTime; }
    public void setAllocatedTime(Timestamp allocatedTime) { this.allocatedTime = allocatedTime; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    /**
     * Converts the parking spot details into a clean, aligned tabular string format.
     * This makes it easy to print the data directly onto the console dashboard.
     */
    @Override
    public String toString() {
        return String.format("| Spot ID: %-4d | Type: %-5s | Available: %-5b | Rate/Hr: Rs.%-6.2f | Vehicle: %-15s | Time: %-21s |", 
                spotId, 
                vehicleType, 
                isAvailable, 
                hourlyRate, 
                (vehicleNumber == null ? "N/A" : vehicleNumber), 
                (allocatedTime == null ? "N/A" : allocatedTime.toString()));
    }
}