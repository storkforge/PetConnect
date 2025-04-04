package se.storkforge.petconnect.util;

import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class PetOwnershipHelper {

    public void setPetOwner(Pet pet, Long ownerId, String currentUsername, UserService userService) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID must be positive and non-null");
        }
        User owner;
        try {
            owner = userService.getUserById(ownerId);
            if (owner == null) {
                throw new SecurityException("Owner not found with ID: " + ownerId);
            }
        } catch (Exception e) {
            throw new SecurityException("Invalid owner reference: " + e.getMessage(), e);
        }

        // Ensure current user can only set ownership to themselves
        if (!owner.getUsername().equals(currentUsername)) {
            throw new SecurityException("You can only set ownership to yourself");
        }

        pet.setOwner(owner);
        owner.addPet(pet);
    }

    public void updatePetOwner(Pet pet, Long newOwnerId, String currentUsername, UserService userService) {
        if (newOwnerId == null || newOwnerId <= 0) {
            throw new IllegalArgumentException("New owner ID must be positive and non-null");
        }
        User newOwner;
        try {
            newOwner = userService.getUserById(newOwnerId);
            if (newOwner == null) {
                throw new SecurityException("New owner not found with ID: " + newOwnerId);
            }
        } catch (Exception e) {
            throw new SecurityException("Invalid new owner reference: " + e.getMessage(), e);
        }
        // Ensure current user can only transfer to themselves
        if (!newOwner.getUsername().equals(currentUsername)) {
            throw new SecurityException("You can only transfer ownership to yourself");
        }
        if (pet.getOwner() != null) {
            pet.getOwner().getPets().remove(pet);
        }
        pet.setOwner(newOwner);
        newOwner.addPet(pet);
    }
}