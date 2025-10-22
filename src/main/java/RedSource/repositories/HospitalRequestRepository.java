package RedSource.repositories;

import RedSource.entities.HospitalRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRequestRepository extends MongoRepository<HospitalRequest, String> {

    List<HospitalRequest> findAllByBloodBankId(String bloodBankId);

    List<HospitalRequest> findByBloodBankId(String bloodBankId);

    Optional<HospitalRequest> findById(String id);

    List<HospitalRequest> findAllByStatus(String status);

    List<HospitalRequest> findByHospitalId(String hospitalId);
}
