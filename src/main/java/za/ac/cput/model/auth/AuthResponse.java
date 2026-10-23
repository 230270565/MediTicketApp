/*
 AuthResponse.java

 Auth Response DTO

 Author: Joshua Reid Adams (230317693)

 Date: 11th August 2026
*/

package za.ac.cput.model.auth;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    protected AuthResponse() {
        // Required for JSON serialization
    }

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
