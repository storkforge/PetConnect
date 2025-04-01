package se.storkforge.petconnect.controller;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;
import se.storkforge.petconnect.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Optional;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;
    private final PetRepository petRepository;

    @Autowired
    public PetController(PetService petService,
                         PetRepository petRepository) {
        this.petService = petService;
        this.petRepository = petRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Pet>> getAllPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pet> pets = petService.getAllPets(pageable);
        return new ResponseEntity<>(pets, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {
        try {
            Optional<Pet> optionalPet = petService.getPetById(id);
            return optionalPet.map(pet -> new ResponseEntity<>(pet, HttpStatus.OK)).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody PetInputDTO petInput, Authentication authentication) {
        Pet createdPet = petService.createPet(petInput);
        return new ResponseEntity<>(createdPet, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(@PathVariable Long id, @RequestBody PetUpdateInputDTO petUpdate, Authentication authentication) {
        try {
            Pet updated = petService.updatePet(id, petUpdate, authentication.getName());
            return ResponseEntity.ok(updated);
        } catch (PetNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id, Authentication authentication) {
        try {
            petService.deletePet(id, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (PetNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/PFP")
    public ResponseEntity<String> uploadPetProfilePicture(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        petService.uploadProfilePicture(id, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/PFP")
    public ResponseEntity<Resource> getPetProfilePicture(
            @PathVariable Long id) {
        Resource resource = petService.getProfilePicture(id);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }
}