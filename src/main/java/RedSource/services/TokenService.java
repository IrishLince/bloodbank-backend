package RedSource.services;

import RedSource.entities.Token;
import RedSource.entities.User;
import RedSource.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
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

        List<Token> accessTokensToRevoke = validUserTokens.stream()
            .filter(token -> "ACCESS".equals(token.getTokenType()))
            .peek(token -> {
                token.setRevoked(true);
                token.setExpired(true);
            })
            .toList();

        if (!accessTokensToRevoke.isEmpty()) {
            tokenRepository.saveAll(validUserTokens);
        }
    }

    public boolean isTokenValid(String token) {
        return findByToken(token)
            .map(t -> !t.isExpired() && !t.isRevoked())
            .orElse(false);
    }
    
    public Token save(Token token) {
        return tokenRepository.save(token);
    }
    
    public List<Token> findAllValidTokens() {
        return tokenRepository.findAll().stream()
            .filter(token -> !token.isExpired() && !token.isRevoked())
            .toList();
    }
    
    public long countAllTokens() {
        return tokenRepository.count();
    }
    
    public void deleteAllTokens() {
        tokenRepository.deleteAll();
    }
    
    public void cleanupOldTokensWithoutUUID() {
        List<Token> allTokens = tokenRepository.findAll();
        
        for (Token token : allTokens) {
            if (token.getToken().length() < 200) { // Old tokens without UUID are shorter
                tokenRepository.delete(token);
            }
        }
    }
}
