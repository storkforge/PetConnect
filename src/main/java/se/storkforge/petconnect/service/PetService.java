package se.storkforge.petconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;

import java.util.Optional;

@Service
public class PetService {

    private static final Logger logger = LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Autowired
    public PetService(PetRepository petRepository, FileStorageService storageService, UserService userService) {
        this.petRepository = petRepository;
        this.fileStorageService = storageService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<Pet> getAllPets(Pageable pageable) {
        logger.info("Retrieving all pets with pagination, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return petRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Pet> getPetById(Long id) {
        logger.info("Retrieving pet by ID: {}", id);
        return petRepository.findById(id);
    }

    @Transactional
    public Pet createPet(PetInputDTO petInput) {
        Pet pet = new Pet();
        pet.setName(petInput.name());
        pet.setSpecies(petInput.species());
        pet.setAvailable(petInput.available());
        pet.setAge(petInput.age());
        pet.setLocation(petInput.location());

        if (petInput.ownerId() != null) {
            User owner = userService.getUserById(petInput.ownerId());
            pet.setOwner(owner);
            owner.addPet(pet);
        }

        return petRepository.save(pet);
    }

    @Transactional
    public Pet updatePet(Long id, PetUpdateInputDTO petUpdate, String currentUsername) {
        Pet existingPet = petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet with id " + id + " not found"));

        // Check if current user is the owner
        if (existingPet.getOwner() != null &&
                !existingPet.getOwner().getUsername().equals(currentUsername)) {
            throw new SecurityException("You can only update your own pets");
        }

        if (petUpdate.name() != null) existingPet.setName(petUpdate.name());
        if (petUpdate.species() != null) existingPet.setSpecies(petUpdate.species());
        if (petUpdate.available() != null) existingPet.setAvailable(petUpdate.available());
        if (petUpdate.age() != null) existingPet.setAge(petUpdate.age());
        if (petUpdate.location() != null) existingPet.setLocation(petUpdate.location());

        if (petUpdate.ownerId() != null) {
            User newOwner = userService.getUserById(petUpdate.ownerId());
            if (existingPet.getOwner() != null) {
                existingPet.getOwner().removePet(existingPet);
            }
            existingPet.setOwner(newOwner);
            newOwner.addPet(existingPet);
        }

        return petRepository.save(existingPet);
    }

    @Transactional
    public void deletePet(Long id, String currentUsername) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException("Pet with id " + id + " not found"));

        // Check ownership
        if (pet.getOwner() == null || !pet.getOwner().getUsername().equals(currentUsername)) {
            throw new SecurityException("You can only delete your own pets");
        }

        // Clear the bidirectional relationship
        if (pet.getOwner() != null) {
            pet.getOwner().getPets().remove(pet); // Remove from owner's collection
        }

        // Delete the pet (owner reference will be automatically removed by JPA)
        petRepository.delete(pet);
    }

    public void uploadProfilePicture(Long id, MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        logger.info("Uploading profile picture to pet with ID: {}", id);
        Optional<Pet> pet = petRepository.findById(id);
        if (pet.isEmpty()) {
            logger.error("Pet not found with ID: {}", id);
            throw new PetNotFoundException("Pet with id " + id + " not found");
        }

        if (pet.get().getProfilePicturePath() != null) {
            fileStorageService.delete(pet.get().getProfilePicturePath());
        }

        String filename = fileStorageService.store(file);
        pet.get().setProfilePicturePath(filename);
        petRepository.save(pet.get());
    }

    @Transactional(readOnly = true)
    public Resource getProfilePicture(Long id) {
        Optional<Pet> pet = petRepository.findById(id);
        if (pet.isEmpty()) {
            logger.error("Pet not found with ID: {}", id);
            throw new PetNotFoundException("Pet with id " + id + " not found");
        }
        String filename = pet.get().getProfilePicturePath();
        if (filename == null) {
            logger.error("Profile picture path is null for pet ID: {}", id);
            throw new RuntimeException("Pet does not have a profile picture");
        }
        return fileStorageService.loadFile(filename);
    }
}