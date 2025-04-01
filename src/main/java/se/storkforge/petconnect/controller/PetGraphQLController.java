package se.storkforge.petconnect.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
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
        return petService.createPet(petInput);
    }

    @MutationMapping
    public Pet updatePet(@Argument Long id, @Argument("pet") PetUpdateInputDTO petInput, Authentication authentication) {
        return petService.updatePet(id, petInput, authentication.getName());
    }

    @MutationMapping
    public Boolean deletePet(@Argument Long id, Authentication authentication) {
        petService.deletePet(id, authentication.getName());
        return true;
    }
}