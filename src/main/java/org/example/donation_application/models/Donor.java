package org.example.donation_application.models;

public class Donor extends User {
    private String organization;
    private byte[] donorPicture;

    public Donor() {}

    public Donor(String name, String email, String password, String phone, String organization) {
        super(name, email, password, phone);
        this.organization = organization;
    }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public byte[] getDonorPicture() { return donorPicture; }
    public void setDonorPicture(byte[] donorPicture) { this.donorPicture = donorPicture; }
}
