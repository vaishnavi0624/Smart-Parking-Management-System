package com.parking;

import java.util.List;

/**
 * ParkingDAO interface defines the database operations
 * for the Smart Parking Management System.
 */
public interface ParkingDAO {

    /**
     * Parks a vehicle in an available parking spot.
     *
     * @param vehicleNumber Vehicle registration number
     * @param vehicleType Type of vehicle (CAR or BIKE)
     * @return true if the vehicle is parked successfully, otherwise false
     */
    boolean parkVehicle(String vehicleNumber, String vehicleType);

    /**
     * Removes a vehicle from a parking spot
     * and frees the parking space.
     *
     * @param spotId Parking spot ID
     * @return true if the vehicle is unparked successfully, otherwise false
     */
    boolean unparkVehicle(int spotId);

    /**
     * Retrieves all parking spots from the database.
     *
     * @return List of all parking spots
     */
    List<ParkingSpot> getAllSpots();

    /**
     * Retrieves all available parking spots
     * for the given vehicle type.
     *
     * @param vehicleType Type of vehicle (CAR or BIKE)
     * @return List of available parking spots
     */
    List<ParkingSpot> getAvailableSpotsByVehicleType(String vehicleType);
}