package se.storkforge.petconnect.util;

import se.storkforge.petconnect.entity.Pet;

public class OwnershipValidator {

    public static void validateOwnership(Pet pet, String username) {
        System.out.println("OwnershipValidator.validateOwnership() called with: pet=" + pet + ", username=" + username); // Lägg till denna rad

        if (pet == null) {
            System.out.println("Ownership validation failed: pet is null");
        } else if (pet.getOwner() == null) {
            System.out.println("Ownership validation failed: pet.getOwner() is null");
        } else {
            System.out.println("pet.getOwner().getUsername() = " + pet.getOwner().getUsername());
            if (!pet.getOwner().getUsername().equals(username)) {
                System.out.println("Ownership validation failed: usernames do not match");
            }
        }

        if (pet == null || pet.getOwner() == null || !pet.getOwner().getUsername().equals(username)) {
            System.out.println("Ownership validation failed: pet=" + pet + ", username=" + username);
            throw new SecurityException("You do not have permission to perform this action");
        }
    }
}