package RedSource.repositories;

import RedSource.entities.UserAdmin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAdminRepository extends MongoRepository<UserAdmin, String> {
    Optional<UserAdmin> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    Optional<UserAdmin> findByUsername(String username);
    
    Boolean existsByUsername(String username);
}
