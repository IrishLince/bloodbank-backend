package RedSource.entities.DTO.auth;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String id;
    private String name;
    private String email;
    private String profilePhotoUrl;
    private List<String> roles;

    public JwtResponse(String accessToken, String refreshToken, String id, String name, String email, String profilePhotoUrl, List<String> roles) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePhotoUrl = profilePhotoUrl;
        this.roles = roles;
    }
}