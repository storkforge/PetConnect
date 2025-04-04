package se.storkforge.petconnect.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.storkforge.petconnect.entity.Pet;

public class OwnershipValidator {

    private static final Logger logger = LoggerFactory.getLogger(OwnershipValidator.class);

    public static void validateOwnership(Pet pet, String username) {
        logger.debug("validateOwnership() called with: pet={}, username={}", pet, username);

        boolean validationFailed = false;

        if (pet == null) {
            logger.warn("Ownership validation failed: pet is null");
            validationFailed = true;
        } else if (pet.getOwner() == null) {
            logger.warn("Ownership validation failed: pet.getOwner() is null");
            validationFailed = true;
        } else {
            logger.debug("pet.getOwner().getUsername() = {}", pet.getOwner().getUsername());
            if (!pet.getOwner().getUsername().equals(username)) {
                logger.warn("Ownership validation failed: usernames do not match");
                validationFailed = true;
            }
        }

        if (validationFailed) {
            logger.error("Ownership validation failed: pet={}, username={}", pet, username);
            throw new SecurityException("You do not have permission to perform this action");
        }
    }
}