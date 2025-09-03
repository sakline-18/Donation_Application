package org.example.donation_application.models;

import java.math.BigDecimal;

public class Student extends User {
    public enum Stream {
        SCIENCE("Science"),
        COMMERCE("Commerce"),
        ARTS("Arts");

        private final String value;

        Stream(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Stream fromString(String text) {
            for (Stream stream : Stream.values()) {
                if (stream.value.equalsIgnoreCase(text)) {
                    return stream;
                }
            }
            throw new IllegalArgumentException("No enum constant for: " + text);
        }
    }

    private String address;
    private String school;
    private Stream stream;
    private String reasonForRegistration;
    private byte[] studentPicture;
    private boolean isPending;
    private Integer needAmount;

    public Student() {
    }

    public Student(String name, String email, String password, String phone,
                   String address, String school, Stream stream, String reasonForRegistration) {
        super(name, email, password, phone);
        this.address = address;
        this.school = school;
        this.stream = stream;
        this.reasonForRegistration = reasonForRegistration;
        this.isPending = true;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public Stream getStream() {
        return stream;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }

    public String getReasonForRegistration() {
        return reasonForRegistration;
    }

    public void setReasonForRegistration(String reasonForRegistration) {
        this.reasonForRegistration = reasonForRegistration;
    }

    public byte[] getStudentPicture() {
        return studentPicture;
    }

    public void setStudentPicture(byte[] studentPicture) {
        this.studentPicture = studentPicture;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public Integer getNeedAmount() {
        return needAmount;
    }

    public void setNeedAmount(Integer needAmount) {
        this.needAmount = needAmount;
    }
}
