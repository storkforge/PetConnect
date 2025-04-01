package se.storkforge.petconnect.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.service.PetService;

@Controller
public class PetGraphQLController {

    private final PetService petService;

    public PetGraphQLController(PetService petService) {
        this.petService = petService;
    }

    @QueryMapping
    public Page<Pet> getAllPets(@Argument int page, @Argument int size) {
        return petService.getAllPets(PageRequest.of(page, size));
    }

    @QueryMapping
    public Pet getPetById(@Argument Long id) {
        return petService.getPetById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with id: " + id));
    }

    @MutationMapping
    public Pet createPet(@Argument("pet") PetInputDTO petInput) {
        Pet pet = new Pet();
        pet.setName(petInput.name());
        pet.setSpecies(petInput.species());
        pet.setAvailable(petInput.available());
        pet.setAge(petInput.age());
        pet.setOwner(petInput.owner());
        pet.setLocation(petInput.location());
        return petService.createPet(pet);
    }
    @MutationMapping
    public Pet updatePet(@Argument Long id, @Argument("pet") PetUpdateInputDTO petInput) {
        Pet existingPet = petService.getPetById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet not found with id: " + id));

         updateNonNullFields(existingPet, petInput);

        return petService.updatePet(id, existingPet);
    }
private void updateNonNullFields(Pet existingPet, PetUpdateInputDTO petInput) {
     if (petInput.name() != null) {
         existingPet.setName(petInput.name());
     }
     if (petInput.species() != null) {
         existingPet.setSpecies(petInput.species());
     }
    if (petInput.available() != null) {
         existingPet.setAvailable(petInput.available());
     }
     if (petInput.age() != null) {
         existingPet.setAge(petInput.age());
     }
    if (petInput.owner() != null) {
         existingPet.setOwner(petInput.owner());
     }
     if (petInput.location() != null) {
         existingPet.setLocation(petInput.location());
     }
 }
    @MutationMapping
    public Boolean deletePet(@Argument Long id) {
          // Check if pet exists first
            petService.getPetById(id)
                    .orElseThrow(() -> new PetNotFoundException("Pet not found with id: " + id));

                petService.deletePet(id);
        return true;
    }
}