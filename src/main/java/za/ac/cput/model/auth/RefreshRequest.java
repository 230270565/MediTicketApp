/*
 RefreshRequest.java

 Refresh Request DTO

 Author: Joshua Reid Adams (230317693)

 Date: 11th August 2026
*/

package za.ac.cput.model.auth;

public class RefreshRequest {

    private String refreshToken;

    protected RefreshRequest() {
        // Required for JSON deserialization
    }

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
