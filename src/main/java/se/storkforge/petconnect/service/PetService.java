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

import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;

import java.util.Optional;

@Service
public class PetService {

    private static final Logger logger = LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public PetService(PetRepository petRepository, FileStorageService storageService) {
        this.petRepository = petRepository;
        this.fileStorageService = storageService;
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
    public Pet createPet(Pet pet) {
        logger.info("Creating pet: {}", pet);
        Pet createdPet = petRepository.save(pet);
        logger.info("Pet created: {}", createdPet);
        return createdPet;
    }

    @Transactional
    public Pet updatePet(Long id, Pet updatedPet) {
        logger.info("Updating pet with ID: {}, updated pet: {}", id, updatedPet);
        if (!petRepository.existsById(id)) {
            logger.warn("Pet not found with ID: {}", id);
            throw new PetNotFoundException("Pet with id " + id + " not found");
        }
        updatedPet.setId(id);
        Pet savedPet = petRepository.save(updatedPet);
        logger.info("Pet updated: {}", savedPet);
        return savedPet;
    }

    @Transactional
    public void deletePet(Long id) {
        logger.info("Deleting pet with ID: {}", id);
        if (!petRepository.existsById(id)) {
            logger.error("Pet not found with ID: {}", id);
            throw new PetNotFoundException("Pet with id " + id + " not found");
        }
        petRepository.deleteById(id);
        logger.info("Pet deleted: {}", id);
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