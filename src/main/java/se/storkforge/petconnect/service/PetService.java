package se.storkforge.petconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;
import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    private static final Logger logger = LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Autowired
    public PetService(PetRepository petRepository,
                      FileStorageService fileStorageService,
                      UserService userService) {
        this.petRepository = petRepository;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<Pet> getAllPets(Pageable pageable, PetFilter filter) {
        logger.info("Retrieving all pets with pagination, page: {}, size: {}, filter: {}",
                pageable.getPageNumber(), pageable.getPageSize(), filter);

        Specification<Pet> spec = buildSpecificationFromFilter(filter);
        return petRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Pet> getPetsByFilter(PetFilter filter) {
        logger.info("Retrieving pets by filter: {}", filter);
        Specification<Pet> spec = buildSpecificationFromFilter(filter);
        return petRepository.findAll(spec);
    }

    private Specification<Pet> buildSpecificationFromFilter(PetFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return Specification.where(null);
        }

        Specification<Pet> spec = Specification.where(null);

        if (filter.getSpecies() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("species"), filter.getSpecies()));
        }

        if (filter.getAvailable() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("available"), filter.getAvailable()));
        }

        if (filter.getMinAge() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("age"), filter.getMinAge()));
        }

        if (filter.getMaxAge() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("age"), filter.getMaxAge()));
        }

        if (filter.getLocation() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("location")),
                            "%" + filter.getLocation().toLowerCase() + "%"));
        }

        if (filter.getNameContains() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")),
                            "%" + filter.getNameContains().toLowerCase() + "%"));
        }

        return spec;
    }

    @Transactional(readOnly = true)
    public Optional<Pet> getPetById(Long id) {
        logger.info("Retrieving pet by ID: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid pet ID: ID must be positive and non-null");
        }
        return petRepository.findById(id);
    }

    @Transactional
    public Pet createPet(PetInputDTO petInput, String currentUsername) {
        logger.info("Creating new pet for user: {}", currentUsername);

        validatePetInput(petInput);

        Pet pet = new Pet();
        pet.setName(petInput.name());
        pet.setSpecies(petInput.species());
        pet.setAvailable(petInput.available());
        pet.setAge(petInput.age());
        pet.setLocation(petInput.location());

        setPetOwner(pet, petInput.ownerId(), currentUsername);

        return petRepository.save(pet);
    }

    private void validatePetInput(PetInputDTO petInput) {
        if (petInput.name() == null || petInput.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be empty");
        }
        if (petInput.species() == null || petInput.species().trim().isEmpty()) {
            throw new IllegalArgumentException("Pet species cannot be empty");
        }
        if (petInput.age() < 0) {  // Removed null check since age is primitive int
            throw new IllegalArgumentException("Pet age cannot be negative");
        }
        if (petInput.location() != null && petInput.location().trim().isEmpty()) {
            throw new IllegalArgumentException("Pet location cannot be empty if provided");
        }
        // If you have a predefined list of allowed species
        // List<String> allowedSpecies = Arrays.asList("dog", "cat", "bird", "rabbit", "fish");
        // if (!allowedSpecies.contains(petInput.species().toLowerCase())) {
        //     throw new IllegalArgumentException("Invalid pet species: " + petInput.species());
        // }
    }

    private void setPetOwner(Pet pet, Long ownerId, String currentUsername) {
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

    @Transactional
    public Pet updatePet(Long id, PetUpdateInputDTO petUpdate, String currentUsername) {
        logger.info("Updating pet with ID: {} for user: {}", id, currentUsername);

        Pet existingPet = petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet with id " + id + " not found"));

        validateOwnership(existingPet, currentUsername);

        applyPetUpdates(existingPet, petUpdate, currentUsername);

        return petRepository.save(existingPet);
    }

    private void validateOwnership(Pet pet, String currentUsername) {
        if (pet.getOwner() == null || !pet.getOwner().getUsername().equals(currentUsername)) {
            throw new SecurityException("You can only modify your own pets");
        }
    }

    private void applyPetUpdates(Pet pet, PetUpdateInputDTO petUpdate, String currentUsername) {
        if (petUpdate.name() != null) {
            pet.setName(petUpdate.name());
        }
        if (petUpdate.species() != null) {
            pet.setSpecies(petUpdate.species());
        }
        if (petUpdate.available() != null) {
            pet.setAvailable(petUpdate.available());
        }
        if (petUpdate.age() != null) {
            pet.setAge(petUpdate.age());
        }
        if (petUpdate.location() != null) {
            pet.setLocation(petUpdate.location());
        }

        if (petUpdate.ownerId() != null) {
            updatePetOwner(pet, petUpdate.ownerId(), currentUsername);
        }
    }

    private void updatePetOwner(Pet pet, Long newOwnerId, String currentUsername) {
        Optional<User> newOwnerOptional = Optional.ofNullable(userService.getUserById(newOwnerId));
        User newOwner = newOwnerOptional.orElseThrow(() ->
                new IllegalArgumentException("New owner not found with ID: " + newOwnerId));

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

    @Transactional
    public void deletePet(Long id, String currentUsername) {
        logger.info("Deleting pet with ID: {} for user: {}", id, currentUsername);

        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet with id " + id + " not found"));

        validateOwnership(pet, currentUsername);

        if (pet.getOwner() != null) {
            pet.getOwner().getPets().remove(pet);
        }

        if (pet.getProfilePicturePath() != null) {
            fileStorageService.delete(pet.getProfilePicturePath());
        }

        petRepository.delete(pet);
    }

    @Transactional
    public void uploadProfilePicture(Long id, MultipartFile file) {
        logger.info("Uploading profile picture for pet with ID: {}", id);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet with id " + id + " not found"));

        if (pet.getProfilePicturePath() != null) {
            try {
                fileStorageService.delete(pet.getProfilePicturePath());
            } catch (RuntimeException e) {
                // Log error but continue with new upload
                logger.warn("Failed to delete old profile picture: {}", e.getMessage());
            }
        }

        String filename = fileStorageService.store(file);
        pet.setProfilePicturePath(filename);
        petRepository.save(pet);
    }

    @Transactional(readOnly = true)
    public Resource getProfilePicture(Long id) {
        logger.info("Retrieving profile picture for pet with ID: {}", id);

        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet with id " + id + " not found"));

        if (pet.getProfilePicturePath() == null) {
            throw new RuntimeException("Pet does not have a profile picture");
        }

        return fileStorageService.loadFile(pet.getProfilePicturePath());
    }
}