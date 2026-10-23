/*
 ResetPasswordRequest.java

 Reset Password Request DTO

 Author: Joshua Reid Adams (230317693)

 Date: 18th August 2026
*/

package za.ac.cput.model.auth;

public class ResetPasswordRequest {
    private String email;
    private String resetSessionToken;
    private String newPassword;

    public ResetPasswordRequest() {}
    public ResetPasswordRequest(String email, String resetSessionToken, String newPassword) {
        this.email = email;
        this.resetSessionToken = resetSessionToken;
        this.newPassword = newPassword;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getResetSessionToken() { return resetSessionToken; }
    public void setResetSessionToken(String resetSessionToken) { this.resetSessionToken = resetSessionToken; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
