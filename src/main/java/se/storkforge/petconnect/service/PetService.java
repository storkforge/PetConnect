package se.storkforge.petconnect.service;

import se.storkforge.petconnect.entity.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.repository.PetRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    private final PetRepository petRepository;

    @Autowired
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    public Optional<Pet> getPetById(Long id) {
        return petRepository.findById(id);
    }

    public Pet createPet(Pet pet) {
        return petRepository.save(pet);
    }

    public Pet updatePet(Long id, Pet updatedPet) {
        if (petRepository.existsById(id)) {
            updatedPet.setId(id);
            return petRepository.save(updatedPet);
        }
        return null;
    }

    public void deletePet(Long id) {
        try {
            petRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Pet with id " + id + " not found", e);
        }
    }
}