package RedSource.repositories;

import RedSource.entities.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    List<Appointment> findAllByUserId(String userId);

    Optional<Appointment> findById(String id);

    List<Appointment> findAllByStatus(String status);

    List<Appointment> findByUserId(String userId);
    
    @Query("{ 'blood_bank_id': ?0 }")
    List<Appointment> findByBloodBankId(String bloodBankId);
}
