package se.storkforge.petconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
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
import se.storkforge.petconnect.service.storageService.RestrictedFileStorageService;
import se.storkforge.petconnect.util.OwnershipValidator;
import se.storkforge.petconnect.util.PetOwnershipHelper;
import se.storkforge.petconnect.util.PetValidator;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    private static final Logger logger = LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;
    private final RestrictedFileStorageService storageService;
    private final UserService userService;
    private final PetOwnershipHelper petOwnershipHelper;
    private final OwnershipValidator ownershipValidator;

    public PetService(PetRepository petRepository,
                      RestrictedFileStorageService storageService,
                      UserService userService,
                      PetOwnershipHelper petOwnershipHelper,
                      OwnershipValidator ownershipValidator) {
        this.petRepository = petRepository;
        this.storageService = storageService;
        this.userService = userService;
        this.petOwnershipHelper = petOwnershipHelper;
        this.ownershipValidator = ownershipValidator;
    }

    @Transactional(readOnly = true)
    public Page<Pet> getAllPets(Pageable pageable, PetFilter filter) {
        logger.info("Retrieving all pets with pagination, page: {}, size: {}, filter: {}",
                pageable.getPageNumber(), pageable.getPageSize(), filter);

        Specification<Pet> spec = buildSpecificationFromFilter(filter);
        return petRepository.findAll(spec, pageable);
    }

    public List<Pet> getPetsByUser(Long userId) {
        return petRepository.findByOwnerId(userId);
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



    @Cacheable(value = "petCache", key = "#id")
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

        PetValidator.validatePetInput(petInput);

        Pet pet = new Pet();
        pet.setName(petInput.getName());
        pet.setSpecies(petInput.getSpecies());
        pet.setAvailable(petInput.isAvailable());
        pet.setAge(petInput.getAge());
        pet.setLocation(petInput.getLocation());

        // Set owner by username only
        User owner = userService.getUserByUsername(currentUsername)
                .orElseThrow(() -> new SecurityException("Invalid owner reference"));
        pet.setOwner(owner);

        return petRepository.save(pet);
    }


    private void applyPetUpdates(Pet pet, PetUpdateInputDTO petUpdate, String currentUsername) {
        PetValidator.validatePetUpdateInput(petUpdate);

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
            petOwnershipHelper.updatePetOwner(pet, petUpdate.ownerId(), currentUsername, userService);
        }
    }

    @CacheEvict(value = "petCache", key = "#id")
    @Transactional
    public Pet updatePet(Long id, PetUpdateInputDTO petUpdate, String currentUsername) {
        logger.info("Updating pet with ID: {} for user: {}", id, currentUsername);

        Pet existingPet = getOrElseThrow(id);

        ownershipValidator.validateOwnership(existingPet, currentUsername);

        applyPetUpdates(existingPet, petUpdate, currentUsername);

        return petRepository.save(existingPet);
    }

    @CacheEvict(value = "petCache", key = "#id")
    @Transactional
    public void deletePet(Long id, String currentUsername) {
        logger.info("Deleting pet with ID: {} for user: {}", id, currentUsername);

        Pet pet = getOrElseThrow(id);

        ownershipValidator.validateOwnership(pet, currentUsername);

        if (pet.getOwner() != null) {
            pet.getOwner().getPets().remove(pet);
        }

        if (pet.getProfilePicturePath() != null) {
            storageService.delete(pet.getProfilePicturePath());
        }

        petRepository.delete(pet);
    }

    @Transactional
    public void uploadProfilePicture(Long id, MultipartFile file) {
        String dir = "pets/"+ id +"/profilePictures";
        logger.info("Uploading profile picture for pet with ID: {}", id);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        Pet pet = getOrElseThrow(id);

        if (pet.getProfilePicturePath() != null) {
            try {
                storageService.delete(pet.getProfilePicturePath());
            } catch (RuntimeException e) {
                logger.warn("Failed to delete old profile picture: {}", e.getMessage());
            }
        }

        String filename = storageService.storeImage(file,dir);
        pet.setProfilePicturePath(filename);
        petRepository.save(pet);
    }

    @Transactional(readOnly = true)
    public Resource getProfilePicture(Long id) {
        logger.info("Retrieving profile picture for pet with ID: {}", id);
        Pet pet = getOrElseThrow(id);

        if (pet.getProfilePicturePath() == null) {
            throw new RuntimeException("Pet does not have a profile picture");
        }

        return storageService.loadFile(pet.getProfilePicturePath());
    }

    @Transactional
    public void deleteProfilePicture(Long id) {
        logger.info("Deleting profile picture for pet with ID: {}", id);
        Pet pet = getOrElseThrow(id);
        storageService.delete(pet.getProfilePicturePath());
    }

    private Pet getOrElseThrow(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet with id " + id + " not found"));
    }

    public List<Pet> getPetsByOwner(User owner) {
        return petRepository.findByOwner(owner);
    }

}