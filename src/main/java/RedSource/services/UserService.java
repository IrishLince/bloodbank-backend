package RedSource.services;

import RedSource.entities.User;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public static final String DONOR = "Donor";
    public static final String BLOODBANK = "BloodBank";
    public static final String HOSPITAL = "Hospital";
    public static final String USER = "User";
    public static final String USERS = "Users";
    public static final String ROLE = "Role";

    private final UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Retrieve all users without any filter
    public List<User> getAll() {
        try {
            List<User> users = userRepository.findAll();
            log.info(MessageUtils.retrieveSuccess(USERS));
            return users;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(USERS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Retrieve users by specific filter (e.g., role)
    public List<User> getAllByFilter(String role) {
        try {
            if (role != null && !role.trim().isEmpty()) {
                List<User> users = userRepository.findAllByRole(role);
                log.info(MessageUtils.retrieveSuccess(role));
                return users;
            } else {
                log.warn(MessageUtils.invalidRequest(ROLE));
                return List.of();
            }
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(role != null ? role : "Filtered Users");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Retrieve a user by ID
    public User getUserById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return userRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(USER);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Save a new user
    public User save(User user) {
        try {
            // Encode password if it's not already encoded
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            User savedUser = userRepository.save(user);
            log.info(MessageUtils.saveSuccess(user.getRole().toString()));
            return savedUser;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError(user.getRole().toString());
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Update an existing user
    public User update(String id, User user) {
        try {
            User existingUser = getUserById(id);
            if (existingUser == null) {
                throw new ServiceException("User not found", new RuntimeException("User not found"));
            }
            
            // CRITICAL: Preserve existing password if no new password is provided
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                // Keep the existing password - don't change it
                user.setPassword(existingUser.getPassword());
            } else {
                // Only encode new password if it's provided and not already encoded
                if (!user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            }
            
            // CRITICAL: Preserve existing email to prevent JWT token invalidation
            // Email changes would break authentication since JWT tokens contain email as subject
            user.setEmail(existingUser.getEmail());
            
            // Preserve creation date from existing user
            user.setCreatedAt(existingUser.getCreatedAt());
            
            // CRITICAL: Preserve reward system data - these fields should NEVER be modified via profile updates
            // Reward points are only modified by the RewardPointsManagementService
            // Always use existing values to prevent accidental overwrites
            user.setRewardPoints(existingUser.getRewardPoints());
            user.setTotalDonations(existingUser.getTotalDonations());
            user.setDonorTier(existingUser.getDonorTier());
            
            // Preserve account status if not provided
            if (user.getAccountStatus() == null || user.getAccountStatus().trim().isEmpty()) {
                user.setAccountStatus(existingUser.getAccountStatus());
            }
            
            user.setId(id);
            user.setUpdatedAt(new Date());
            User updatedUser = userRepository.save(user);
            log.info(MessageUtils.updateSuccess(user.getRole().toString()));
            return updatedUser;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError(user.getRole().toString());
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Delete a user by ID with dynamic role logging
    public void delete(String id, String role) {
        try {
            User user = getUserById(id);
            if (user == null) {
                throw new ServiceException("User not found", new RuntimeException("User not found"));
            }
            userRepository.delete(user);
            log.info(MessageUtils.deleteSuccess(role != null ? role : USER));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError(role != null ? role : USER);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
}
