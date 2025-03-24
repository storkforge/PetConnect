package se.storkforge.petconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.exeption.PetNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    private static final Logger logger = LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;

    @Autowired
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public List<Pet> getAllPets() {
        logger.info("Retrieving all pets");
        return petRepository.findAll();
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
}