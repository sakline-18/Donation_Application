package org.example.donation_application.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Donation {
    private int id;
    private int donorId;
    private int studentId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime donationDate;
    private String donorName;
    private String studentName;

    public Donation() {}

    public Donation(int donorId, int studentId, BigDecimal amount, String description) {
        this.donorId = donorId;
        this.studentId = studentId;
        this.amount = amount;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDateTime donationDate) { this.donationDate = donationDate; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
