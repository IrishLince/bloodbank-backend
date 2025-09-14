package RedSource.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import RedSource.services.TokenService;
import RedSource.entities.Token;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${redsource.app.jwtSecret:defaultSecretKeyWhichShouldBeChangedInProduction}")
    private String jwtSecret;

    @Value("${redsource.app.jwtExpirationMs:3600000}")
    private int jwtExpirationMs;
    
    @Value("${redsource.app.refreshTokenExpirationMs:604800000}")
    private int refreshTokenExpirationMs;
    
    @Autowired
    private TokenService tokenService;

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }
    
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateRefreshTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .setId(java.util.UUID.randomUUID().toString()) // Add unique ID to prevent duplicates
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String generateRefreshTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + refreshTokenExpirationMs))
                .setId(java.util.UUID.randomUUID().toString()) // Add unique ID to prevent duplicates
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            // Only verify the JWT signature and expiration
            // Database validation is handled separately in AuthTokenFilter
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // Logging handled by the AuthEntryPointJwt
        } catch (ExpiredJwtException e) {
            // Mark token as expired in database
            tokenService.findByToken(authToken).ifPresent(token -> {
                token.setExpired(true);
                tokenService.save(token);
            });
        }
        return false;
    }
    
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}