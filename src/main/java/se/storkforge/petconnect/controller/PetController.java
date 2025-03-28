package se.storkforge.petconnect.controller;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;
import se.storkforge.petconnect.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<List<Pet>> getAllPets() {
        List<Pet> pets = petService.getAllPets();
        return new ResponseEntity<>(pets, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {
        Optional<Pet> optionalPet = petService.getPetById(id);
        return optionalPet.map(pet -> new ResponseEntity<>(pet, HttpStatus.OK))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) {
        Pet createdPet = petService.createPet(pet);
        return new ResponseEntity<>(createdPet, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(@PathVariable Long id, @RequestBody Pet updatedPet) {
        try {
            Pet updated = petService.updatePet(id, updatedPet);
            return ResponseEntity.ok(updated);
        } catch (PetNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        try {
            petService.deletePet(id);
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