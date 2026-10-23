/*
 Doctor.java

 Frontend model for Doctor

 Author: Jaden Clayton Abrahams (222206721)

 Date: 11th August 2026
*/

package za.ac.cput.model.domain;

public class Doctor extends User {
    private String specialty;
    private String licenseNumber;

    public Doctor() {}

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}
