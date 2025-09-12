package RedSource.entities.utils;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

import java.util.stream.Collectors;

public class MessageUtils {

    // Login messages
    public static final String LOGIN_SUCCESS = "Successfully logged in.";
    public static final String LOGIN_ERROR = "Login attempt failed.";
    public static final String LOGIN_FAILED = "Invalid email or password.";
    public static final String LOGIN_EXCEPTION = "Error during login: {}";

    // Donor messages
    public static final String DONOR_REGISTRATION_SUCCESS = "Donor registration successful.";
    public static final String DONOR_REGISTRATION_FAILURE = "Failed to register donor.";
    public static final String DONOR_PROFILE_UPDATE_SUCCESS = "Donor profile successfully updated.";
    public static final String DONOR_PROFILE_UPDATE_FAILURE = "Failed to update donor profile.";
    public static final String DONOR_NOT_FOUND = "Donor not found.";
    public static final String APPOINTMENT_SUCCESS = "Appointment successfully scheduled.";
    public static final String APPOINTMENT_FAILURE = "Failed to schedule appointment.";

    // Bloodbank Admin messages
    public static final String INVENTORY_UPDATE_SUCCESS = "Blood inventory successfully updated.";
    public static final String INVENTORY_UPDATE_FAILURE = "Failed to update blood inventory.";
    public static final String INVENTORY_LOW_ALERT = "Blood inventory is low for type %s.";
    public static final String ADMIN_ACCESS_DENIED = "Access denied. Admin privileges required.";

    // Hospital messages
    public static final String HOSPITAL_REQUEST_SUCCESS = "Blood request successfully processed.";
    public static final String HOSPITAL_REQUEST_FAILURE = "Failed to process blood request.";
    public static final String HOSPITAL_NOT_FOUND = "Hospital record not found.";
    public static final String HOSPITAL_ACCESS_DENIED = "Access denied. Hospital privileges required.";

    // CRUD operations messages
    public static final String RETRIEVE_SUCCESS = "Successfully retrieved %s.";
    public static final String RETRIEVE_ERROR = "Failed to retrieve %s.";
    public static final String RETRIEVE_EMPTY = "No %s found.";
    public static final String SAVE_SUCCESS = "Successfully saved %s.";
    public static final String SAVE_ERROR = "Failed to save %s.";
    public static final String UPDATE_SUCCESS = "Successfully updated %s.";
    public static final String UPDATE_ERROR = "Failed to update %s.";
    public static final String DELETE_SUCCESS = "Successfully deleted %s.";
    public static final String DELETE_ERROR = "Failed to delete %s.";

    // General validation and existence messages
    public static final String NOT_FOUND = "%s not found.";
    public static final String ALREADY_EXISTS = "%s already exists.";
    public static final String INVALID_REQUEST = "Invalid request for %s.";
    public static final String NO_PERMISSION = "You do not have permission to access %s.";

    // Additional validation messages
    public static final String MISSING_NAME = "Name is required and cannot be blank.";
    public static final String MISSING_EMAIL = "Email is required and cannot be blank.";
    public static final String MISSING_PASSWORD = "Password is required and must be at least 6 characters long.";
    public static final String MISSING_CONTACT_INFORMATION = "Contact information is required and cannot be blank.";
    public static final String MISSING_BIRTHDATE = "Birthdate is required.";

    // Dynamic methods to format messages
    public static String retrieveSuccess(String value) {
        return String.format(RETRIEVE_SUCCESS, value);
    }

    public static String retrieveError(String value) {
        return String.format(RETRIEVE_ERROR, value);
    }

    public static String retrieveEmpty(String value) {
        return String.format(RETRIEVE_EMPTY, value);
    }

    public static String saveSuccess(String value) {
        return String.format(SAVE_SUCCESS, value);
    }

    public static String saveError(String value) {
        return String.format(SAVE_ERROR, value);
    }

    public static String updateSuccess(String value) {
        return String.format(UPDATE_SUCCESS, value);
    }

    public static String updateError(String value) {
        return String.format(UPDATE_ERROR, value);
    }

    public static String deleteSuccess(String value) {
        return String.format(DELETE_SUCCESS, value);
    }

    public static String deleteError(String value) {
        return String.format(DELETE_ERROR, value);
    }

    public static String invalidRequest(String value) {
        return String.format(INVALID_REQUEST, value);
    }

    public static String notFound(String value) {
        return String.format(NOT_FOUND, value);
    }

    public static String alreadyExists(String value) {
        return String.format(ALREADY_EXISTS, value);
    }

    public static String inventoryLowAlert(String bloodType) {
        return String.format(INVENTORY_LOW_ALERT, bloodType);
    }

    public static String validationErrors(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
    }

}
