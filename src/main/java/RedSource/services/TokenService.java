package RedSource.services;

import RedSource.entities.Token;
import RedSource.entities.User;
import RedSource.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public Token saveAccessToken(User user, String jwtToken, Date expiresAt) {
        Token token = Token.builder()
                .userId(user.getId())
                .token(jwtToken)
                .tokenType("ACCESS")
                .revoked(false)
                .expired(false)
                .createdAt(new Date())
                .expiresAt(expiresAt)
                .build();
        
        return tokenRepository.save(token);
    }
    
    public Token saveRefreshToken(User user, String refreshToken, Date expiresAt) {
        Token token = Token.builder()
                .userId(user.getId())
                .token(refreshToken)
                .tokenType("REFRESH")
                .revoked(false)
                .expired(false)
                .createdAt(new Date())
                .expiresAt(expiresAt)
                .build();
        
        return tokenRepository.save(token);
    }

    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public List<Token> findAllValidTokensByUser(String userId) {
        return tokenRepository.findAllValidTokensByUser(userId);
    }

    public void revokeAllUserTokens(String userId) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokensByUser(userId);
        if (validUserTokens.isEmpty()) {
            return;
        }

        validUserTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });

        tokenRepository.saveAll(validUserTokens);
    }
    
    public void revokeAllUserAccessTokens(String userId) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokensByUser(userId);
        if (validUserTokens.isEmpty()) {
            return;
        }

        validUserTokens.stream()
            .filter(token -> "ACCESS".equals(token.getTokenType()))
            .forEach(token -> {
                token.setRevoked(true);
                token.setExpired(true);
            });

        tokenRepository.saveAll(validUserTokens);
    }

    public boolean isTokenValid(String token) {
        Optional<Token> tokenOptional = findByToken(token);
        return tokenOptional.map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
    }
    
    public Token save(Token token) {
        return tokenRepository.save(token);
    }
}
