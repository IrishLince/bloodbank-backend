package RedSource.repositories;

import RedSource.entities.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    
    @Query("{'user_id': ?0, 'revoked': false, 'expired': false}")
    List<Token> findAllValidTokensByUser(String userId);
    
    @Query("{'token': ?0}")
    Optional<Token> findByToken(String token);
}
