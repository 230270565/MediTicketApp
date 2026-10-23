/*
 ChangePasswordRequest.java

 Change Password Request DTO

 Author: Joshua Reid Adams (230317693)

 Date: 11th August 2026
*/

package za.ac.cput.model.auth;

public class ChangePasswordRequest {

    private String email;
    private String oldPassword;
    private String newPassword;

    public ChangePasswordRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
