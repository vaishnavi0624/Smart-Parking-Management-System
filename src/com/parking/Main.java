package com.parking;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Main class of the Smart Parking Management System.
 * It displays the menu, takes user input, validates it,
 * and calls the required database operations.
 *
 * @author Easha Kadganve
 * @author Vaishnavi Jadhav
 * @version 1.0.0
 * @since 2026-07-14
 */
public class Main {

    /** Scanner object to read user input from the console. */
    private static final Scanner scanner = new Scanner(System.in);

    /** DAO object used to perform database operations. */
    private static final ParkingDAO parkingDAO = new ParkingDAOImpl();

    /**
     * Strict regex for Indian vehicle registration plates.
     * Examples allowed: MH-12-AB-1234, MH12AB1234, MH 12 AB 1234, KA-01-M-9999
     */
    private static final Pattern VEHICLE_PLATE_PATTERN =
            Pattern.compile("^[A-Z]{2}[-\\s]?[0-9]{2}[-\\s]?[A-Z]{1,3}[-\\s]?[0-9]{4}$", Pattern.CASE_INSENSITIVE);

    /**
     * Main method where the program starts.
     * Displays the menu and performs the selected operation.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("=== Welcome to Smart Parking Management System ===");

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Park a Vehicle");
            System.out.println("2. Unpark a Vehicle (Free a Spot & Bill)");
            System.out.println("3. View Dashboard (All Spots)");
            System.out.println("4. Check Available Spots by Vehicle Type");
            System.out.println("5. Exit");
            System.out.print("Select an option (1-5): ");

            int choice = readIntegerInput();

            switch (choice) {
                case 1:
                    handlePark();
                    break;
                case 2:
                    handleUnpark();
                    break;
                case 3:
                    displayAllSpots();
                    break;
                case 4:
                    displayAvailableByType();
                    break;
                case 5:
                    System.out.println("Thank you for using Smart Parking Management System. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("❌ Invalid choice! Please enter a number between 1 and 5.");
            }
        }
    }

    /**
     * Takes vehicle details from the user,
     * validates them, and parks the vehicle.
     */
    private static void handlePark() {
        System.out.print("Enter Vehicle Type (CAR / BIKE): ");
        String vehicleType = scanner.nextLine().trim().toUpperCase();

        if (!vehicleType.equals("CAR") && !vehicleType.equals("BIKE")) {
            System.out.println("❌ Validation Error: System only supports 'CAR' or 'BIKE'.");
            return;
        }

        System.out.print("Enter Vehicle Registration Number (e.g., MH-12-AB-1234): ");
        String vehicleNumber = scanner.nextLine().trim();

        if (vehicleNumber.isEmpty()) {
            System.out.println("❌ Validation Error: Registration number cannot be empty.");
            return;
        }

        if (!VEHICLE_PLATE_PATTERN.matcher(vehicleNumber).matches()) {
            System.out.println("❌ Validation Error: Invalid Registration format! Use 4 to 15 alphanumeric characters (hyphens/spaces allowed).");
            return;
        }

        // Call DAO method to park the vehicle
        parkingDAO.parkVehicle(vehicleNumber, vehicleType);
    }

    /**
     * Takes the spot ID from the user
     * and unparks the vehicle.
     */
    private static void handleUnpark() {
        System.out.print("Enter Spot ID to clear: ");
        int spotId = readIntegerInput();

        if (spotId <= 0) {
            System.out.println("❌ Validation Error: Spot ID must be a positive integer.");
            return;
        }

        // Call DAO method to unpark the vehicle
        parkingDAO.unparkVehicle(spotId);
    }

    /**
     * Displays all parking spots available in the database.
     */
    private static void displayAllSpots() {
        List<ParkingSpot> spots = parkingDAO.getAllSpots();

        if (spots.isEmpty()) {
            System.out.println("⚠️ Database is currently empty. Please configure some spots in MySQL first.");
            return;
        }

        String border = "=========================================================================================================================";

        System.out.println("\n" + border);
        System.out.println("                                            PARKING SYSTEM DASHBOARD                                                     ");
        System.out.println(border);

        // Print all parking spot details
        spots.forEach(System.out::println);

        System.out.println(border);
    }

    /**
     * Displays all available parking spots
     * for the selected vehicle type.
     */
    private static void displayAvailableByType() {
        System.out.print("Enter Vehicle Type to query (CAR / BIKE): ");
        String type = scanner.nextLine().trim().toUpperCase();

        if (!type.equals("CAR") && !type.equals("BIKE")) {
            System.out.println("❌ Validation Error: Vehicle Type must be CAR or BIKE.");
            return;
        }

        List<ParkingSpot> availableSpots = parkingDAO.getAvailableSpotsByVehicleType(type);

        if (availableSpots.isEmpty()) {
            System.out.println("❌ Sorry, no available spots left for " + type);
        } else {
            System.out.println("\n--- Available " + type + " Slots ---");

            // Display available spot IDs
            availableSpots.forEach(spot -> System.out.println("📍 Spot ID: " + spot.getSpotId()));
        }
    }

    /**
     * Reads an integer input from the user.
     * Returns -1 if the input is not a valid number.
     *
     * @return User's integer input or -1 if invalid
     */
    private static int readIntegerInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}