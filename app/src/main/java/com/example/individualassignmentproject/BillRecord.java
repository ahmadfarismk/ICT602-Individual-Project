package com.example.individualassignmentproject;

/**
 * Data model class representing a single electricity bill record.
 * Designed to be compatible with Firebase Realtime Database serialization.
 */
public class BillRecord {

    private String id;          // Firebase push key
    private String month;       // e.g., "January"
    private int units;          // kWh used (1-1000)
    private int rebate;         // Rebate percentage (0-5)
    private double totalCharges; // Before rebate (RM)
    private double finalCost;   // After rebate (RM)

    /**
     * Default no-arg constructor required by Firebase Realtime Database.
     */
    public BillRecord() {
    }

    public BillRecord(String month, int units, int rebate, double totalCharges, double finalCost) {
        this.month = month;
        this.units = units;
        this.rebate = rebate;
        this.totalCharges = totalCharges;
        this.finalCost = finalCost;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public int getRebate() {
        return rebate;
    }

    public void setRebate(int rebate) {
        this.rebate = rebate;
    }

    public double getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(double totalCharges) {
        this.totalCharges = totalCharges;
    }

    public double getFinalCost() {
        return finalCost;
    }

    public void setFinalCost(double finalCost) {
        this.finalCost = finalCost;
    }
}
