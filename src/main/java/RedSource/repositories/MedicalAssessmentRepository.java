package RedSource.repositories;

import RedSource.entities.MedicalAssessment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalAssessmentRepository extends MongoRepository<MedicalAssessment, String> {
    List<MedicalAssessment> findByUserId(String userId);
}


