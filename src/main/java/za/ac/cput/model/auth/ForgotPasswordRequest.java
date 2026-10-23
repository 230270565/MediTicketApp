/*
 ForgotPasswordRequest.java

 Forgot Password Request DTO

 Author: Joshua Reid Adams (230317693)

 Date: 18th August 2026
*/

package za.ac.cput.model.auth;

public class ForgotPasswordRequest {
    private String email;

    public ForgotPasswordRequest() {}
    public ForgotPasswordRequest(String email) { this.email = email; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
