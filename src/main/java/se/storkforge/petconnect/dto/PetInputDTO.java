package se.storkforge.petconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class PetInputDTO {

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Species is required")
        private String species;

        private boolean available;

        @Min(value = 0, message = "Age cannot be negative")
        private int age;

        private Long ownerId;

        private String location;

        // Empty constructor (needed for form binding)
        public PetInputDTO() {
        }

        // All-args constructor
        public PetInputDTO(String name, String species, boolean available, int age, Long ownerId, String location) {
                this.name = name;
                this.species = species;
                this.available = available;
                this.age = age;
                this.ownerId = ownerId;
                this.location = location;
        }

        // Getters and setters

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

        public boolean isAvailable() {
                return available;
        }

        public void setAvailable(boolean available) {
                this.available = available;
        }

        public int getAge() {
                return age;
        }

        public void setAge(int age) {
                this.age = age;
        }

        public Long getOwnerId() {
                return ownerId;
        }

        public void setOwnerId(Long ownerId) {
                this.ownerId = ownerId;
        }

        public String getLocation() {
                return location;
        }

        public void setLocation(String location) {
                this.location = location;
        }
}
