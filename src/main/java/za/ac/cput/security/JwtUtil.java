/*
 JwtUtil.java

 Reads the role and expiry out of the JWT

 Author: Joshua Reid Adams (230317693)

 Date: 11th August 2026
*/

package za.ac.cput.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Reads the claims out of the token so the app knows who is logged in.
// It doesn't check the signature, the backend does that.
public class JwtUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static int extractUserId(String token) {
        JsonNode claims = decodePayload(token);
        return Integer.parseInt(claims.get("sub").asText());
    }

    public static String extractUserType(String token) {
        JsonNode claims = decodePayload(token);
        JsonNode node = claims.get("userType");
        return node != null ? node.asText() : null;
    }

    public static String extractStaffRole(String token) {
        JsonNode claims = decodePayload(token);
        JsonNode node = claims.get("staffRole");
        return node != null ? node.asText() : null;
    }

    private static JsonNode decodePayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Malformed JWT");
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return MAPPER.readTree(new String(decoded, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not decode JWT payload", e);
        }
    }
}
