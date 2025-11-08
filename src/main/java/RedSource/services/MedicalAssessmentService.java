package RedSource.services;

import RedSource.entities.MedicalAssessment;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.MedicalAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MedicalAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(MedicalAssessmentService.class);
    public static final String MEDICAL_ASSESSMENTS = "Medical Assessments";
    public static final String MEDICAL_ASSESSMENT = "Medical Assessment";

    private final MedicalAssessmentRepository medicalAssessmentRepository;

    public List<MedicalAssessment> getAll() {
        try {
            List<MedicalAssessment> assessments = medicalAssessmentRepository.findAll();
            log.info(MessageUtils.retrieveSuccess(MEDICAL_ASSESSMENTS));
            return assessments;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(MEDICAL_ASSESSMENTS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public MedicalAssessment getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return medicalAssessmentRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(MEDICAL_ASSESSMENT);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public MedicalAssessment save(MedicalAssessment assessment) {
        try {
            Date now = new Date();
            assessment.setCreatedAt(now);
            assessment.setUpdatedAt(now);
            assessment.setAssessmentDate(now);
            MedicalAssessment savedAssessment = medicalAssessmentRepository.save(assessment);
            log.info(MessageUtils.saveSuccess(MEDICAL_ASSESSMENT));
            return savedAssessment;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError(MEDICAL_ASSESSMENT);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public MedicalAssessment update(String id, MedicalAssessment assessment) {
        try {
            MedicalAssessment existingAssessment = getById(id);
            if (existingAssessment == null) {
                throw new ServiceException("Medical Assessment not found");
            }
            assessment.setId(id);
            assessment.setCreatedAt(existingAssessment.getCreatedAt());
            assessment.setUpdatedAt(new Date());
            MedicalAssessment updatedAssessment = medicalAssessmentRepository.save(assessment);
            log.info(MessageUtils.updateSuccess(MEDICAL_ASSESSMENT));
            return updatedAssessment;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError(MEDICAL_ASSESSMENT);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            MedicalAssessment assessment = getById(id);
            if (assessment == null) {
                throw new ServiceException("Medical Assessment not found");
            }
            medicalAssessmentRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess(MEDICAL_ASSESSMENT));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError(MEDICAL_ASSESSMENT);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
}

