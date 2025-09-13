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
        logger.info("Attempting to save ACCESS token for user: {}", user.getId());
        
        Token token = Token.builder()
                .userId(user.getId())
                .token(jwtToken)
                .tokenType("ACCESS")
                .revoked(false)
                .expired(false)
                .createdAt(new Date())
                .expiresAt(expiresAt)
                .build();
        
        try {
            Token savedToken = tokenRepository.save(token);
            logger.info("Successfully saved ACCESS token with ID: {} for user: {}", savedToken.getId(), user.getId());
            return savedToken;
        } catch (Exception e) {
            logger.error("Failed to save ACCESS token for user: {}. Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    public Token saveRefreshToken(User user, String refreshToken, Date expiresAt) {
        logger.info("Attempting to save REFRESH token for user: {}", user.getId());
        
        Token token = Token.builder()
                .userId(user.getId())
                .token(refreshToken)
                .tokenType("REFRESH")
                .revoked(false)
                .expired(false)
                .createdAt(new Date())
                .expiresAt(expiresAt)
                .build();
        
        try {
            Token savedToken = tokenRepository.save(token);
            logger.info("Successfully saved REFRESH token with ID: {} for user: {}", savedToken.getId(), user.getId());
            return savedToken;
        } catch (Exception e) {
            logger.error("Failed to save REFRESH token for user: {}. Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    public Optional<Token> findByToken(String token) {
        logger.debug("Searching for token in database: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
        Optional<Token> foundToken = tokenRepository.findByToken(token);
        if (foundToken.isPresent()) {
            logger.debug("Token found in database for user: {}", foundToken.get().getUserId());
        } else {
            logger.warn("Token not found in database. Token searched: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
        }
        return foundToken;
    }

    public List<Token> findAllValidTokensByUser(String userId) {
        logger.debug("Finding all valid tokens for user: {}", userId);
        List<Token> tokens = tokenRepository.findAllValidTokensByUser(userId);
        logger.debug("Found {} valid tokens for user: {}", tokens.size(), userId);
        return tokens;
    }

    public void revokeAllUserTokens(String userId) {
        logger.info("Revoking all tokens for user: {}", userId);
        List<Token> validUserTokens = tokenRepository.findAllValidTokensByUser(userId);
        if (validUserTokens.isEmpty()) {
            logger.info("No valid tokens found to revoke for user: {}", userId);
            return;
        }

        validUserTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });

        tokenRepository.saveAll(validUserTokens);
        logger.info("Successfully revoked {} tokens for user: {}", validUserTokens.size(), userId);
    }
    
    public void revokeAllUserAccessTokens(String userId) {
        logger.info("Revoking all ACCESS tokens for user: {}", userId);
        List<Token> validUserTokens = tokenRepository.findAllValidTokensByUser(userId);
        if (validUserTokens.isEmpty()) {
            logger.info("No valid tokens found to revoke for user: {}", userId);
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
            logger.info("Successfully revoked {} ACCESS tokens for user: {}", accessTokensToRevoke.size(), userId);
        } else {
            logger.info("No ACCESS tokens found to revoke for user: {}", userId);
        }
    }

    public boolean isTokenValid(String token) {
        logger.debug("Validating token");
        logger.debug("Token length: {}", token.length());
        logger.debug("Full token being searched: {}", token);
        
        // Get all tokens to compare
        List<Token> allTokens = tokenRepository.findAll();
        logger.debug("Total tokens in database: {}", allTokens.size());
        
        if (!allTokens.isEmpty()) {
            Token firstToken = allTokens.get(0);
            logger.debug("Sample stored token: {}", firstToken.getToken());
            logger.debug("Sample token length: {}", firstToken.getToken() != null ? firstToken.getToken().length() : "null");
            logger.debug("Tokens match: {}", token.equals(firstToken.getToken()));
        }
        
        Optional<Token> tokenOptional = findByToken(token);
        boolean isValid = tokenOptional.map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
        logger.debug("Token validation result: {}", isValid);
        return isValid;
    }
    
    public Token save(Token token) {
        return tokenRepository.save(token);
    }
}
