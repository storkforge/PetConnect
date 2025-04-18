package se.storkforge.petconnect.controller;

import jakarta.validation.constraints.Min;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.service.PetFilter;
import se.storkforge.petconnect.service.PetService;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public ResponseEntity<Page<Pet>> getAllPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) @Min(0) Integer minAge,
            @RequestParam(required = false) @Min(0) Integer maxAge,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String nameContains) {

        Pageable pageable = PageRequest.of(page, size);
        PetFilter filter = new PetFilter();
        filter.setSpecies(species);
        filter.setAvailable(available);
        filter.setMinAge(minAge);
        filter.setMaxAge(maxAge);
        filter.setLocation(location);
        filter.setNameContains(nameContains);

        Page<Pet> pets = petService.getAllPets(pageable, filter);
        return new ResponseEntity<>(pets, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {
        try {
            Optional<Pet> optionalPet = petService.getPetById(id);
            return optionalPet.map(pet -> new ResponseEntity<>(pet, HttpStatus.OK))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Pet> createPet(
            @Valid @RequestBody PetInputDTO petInput,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(petService.createPet(petInput, username));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetUpdateInputDTO petUpdate,
            Authentication authentication) {
        try {
            Pet updated = petService.updatePet(id, petUpdate, authentication.getName());
            return ResponseEntity.ok(updated);
        } catch (PetNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            petService.deletePet(id, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (PetNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}/PFP")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            petService.uploadProfilePicture(id, file);
            return ResponseEntity.ok("Profile picture uploaded successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (PetNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload picture: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}/PFP")
    public ResponseEntity<Resource> getProfilePicture(
            @PathVariable Long id) {
        Resource resource = petService.getProfilePicture(id);
        MediaType mediaType = determineMediaType(resource.getFilename());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
    private MediaType determineMediaType(String filename) {
        if (filename == null) {
            return MediaType.IMAGE_JPEG;
        }
        if (filename.toLowerCase().endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (filename.toLowerCase().endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.IMAGE_JPEG;
    }

    @DeleteMapping("/{id}/PFP")
    public ResponseEntity<Void> deleteProfilePicture (@PathVariable Long id) {
        try {
            petService.deleteProfilePicture(id);
            return ResponseEntity.noContent().build();
        } catch (PetNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<String> handlePetNotFound(PetNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}