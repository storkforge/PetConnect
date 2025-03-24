package se.storkforge.petconnect.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Species is required")
    @Size(max = 50, message = "Species cannot exceed 50 characters")
    private String species;

    private boolean available; // Corrected field name
    private int age;
    private String owner;
    private String location;

    public Pet() {}

    public Pet(String name, String species, boolean available, int age, String owner, String location) {
        this.name = name;
        this.species = species;
        this.available = available; // Corrected field name
        this.age = age;
        this.owner = owner;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public boolean isAvailable() { // Corrected getter
        return available;
    }

    public void setAvailable(boolean available) { // Corrected setter
        this.available = available;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", species='" + species + '\'' +
                ", available=" + (available ? "Yes" : "No") + // Corrected field name
                ", age=" + age +
                ", owner='" + owner + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return id != null && id.equals(pet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}