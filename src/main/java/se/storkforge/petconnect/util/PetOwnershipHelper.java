package se.storkforge.petconnect.util;

import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.UserService;
import org.springframework.stereotype.Component; // Lägg till denna import

@Component
public class PetOwnershipHelper {

    public void setPetOwner(Pet pet, Long ownerId, String currentUsername, UserService userService) {
        if (ownerId != null) {
            User owner;
            try {
                owner = userService.getUserById(ownerId);
            } catch (Exception e) {
                throw new SecurityException("Invalid owner reference");
            }
            if (!owner.getUsername().equals(currentUsername)) {
                throw new SecurityException("You can only create pets for yourself");
            }

            pet.setOwner(owner);
            owner.addPet(pet);
        }
    }

    public void updatePetOwner(Pet pet, Long newOwnerId, String currentUsername, UserService userService) {
        User newOwner = userService.getUserById(newOwnerId);

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