/*
 ClinicStaff.java

 Frontend model for ClinicStaff

 Author: Matthew Barron (230398863)

 Date: 11th August 2026
*/

package za.ac.cput.model.domain;

public class ClinicStaff extends User {
    private String staffRole; // NURSE, ADMIN
    private String department;

    public ClinicStaff() {}

    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
