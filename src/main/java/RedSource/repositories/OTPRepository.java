package RedSource.repositories;

import RedSource.entities.OTP;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPRepository extends MongoRepository<OTP, String> {
    Optional<OTP> findByPhoneNumberAndEmailAndVerifiedFalse(String phoneNumber, String email);
    Optional<OTP> findByPhoneNumberAndOtpCodeAndEmailAndVerifiedFalse(String phoneNumber, String otpCode, String email);
    Optional<OTP> findByPhoneNumberAndEmailAndVerifiedTrue(String phoneNumber, String email);
    void deleteByPhoneNumberAndEmail(String phoneNumber, String email);
} 