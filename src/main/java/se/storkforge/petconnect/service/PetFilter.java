package se.storkforge.petconnect.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * Filter criteria for searching pets.
 * Used in conjunction with PetSpecification to filter pets based on various attributes.
 */
public class PetFilter {

    @Size(max = 50)
    private String species;

    private Boolean available;

    @Min(0)
    private Integer minAge;

    @Min(0)
    private Integer maxAge;

    @Size(max = 100)
    private String location;

    @Size(max = 100)
    private String nameContains;

    /**
     * Default constructor.
     */
    public PetFilter() {}

    /**
     * Constructor with species and availability.
     *
     * @param species   The species of the pet.
     * @param available The availability of the pet.
     */
    public PetFilter(String species, Boolean available) {
        this.species = species;
        this.available = available;
    }

    /**
     * Gets the species of the pet.
     *
     * @return The species of the pet.
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Sets the species of the pet.
     *
     * @param species The species to set.
     */
    public void setSpecies(String species) {
        this.species = (species != null && !species.trim().isEmpty()) ? species.trim() : null;
    }

    /**
     * Gets the availability of the pet.
     *
     * @return The availability of the pet.
     */
    public Boolean getAvailable() {
        return available;
    }

    /**
     * Sets the availability of the pet.
     *
     * @param available The availability to set.
     */
    public void setAvailable(Boolean available) {
        this.available = available;
    }

    /**
     * Gets the minimum age of the pet.
     *
     * @return The minimum age of the pet.
     */
    public Integer getMinAge() {
        return minAge;
    }

    /**
     * Sets the minimum age of the pet.
     *
     * @param minAge The minimum age to set.
     */
    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    /**
     * Gets the maximum age of the pet.
     *
     * @return The maximum age of the pet.
     */
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * Sets the maximum age of the pet.
     *
     * @param maxAge The maximum age to set.
     */
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Gets the location of the pet.
     *
     * @return The location of the pet.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the pet.
     *
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = (location != null && !location.trim().isEmpty()) ? location.trim() : null;
    }

    /**
     * Gets the nameContains filter.
     *
     * @return The nameContains filter.
     */
    public String getNameContains() {
        return nameContains;
    }

    /**
     * Sets the nameContains filter.
     *
     * @param nameContains The nameContains filter to set.
     */
    public void setNameContains(String nameContains) {
        this.nameContains = (nameContains != null && !nameContains.trim().isEmpty()) ? nameContains.trim() : null;
    }

    /**
     * Validates the filter criteria.
     *
     * @throws IllegalArgumentException if minAge is greater than maxAge.
     */
    public void validate() {
        if (minAge != null && maxAge != null && minAge > maxAge) {
            throw new IllegalArgumentException("Minimum age cannot be greater than maximum age");
        }
    }

    /**
     * Checks if the filter is empty.
     *
     * @return true if the filter is empty, false otherwise.
     */
    public boolean isEmpty() {
        return species == null && available == null && minAge == null &&
                maxAge == null && location == null && nameContains == null;
    }

    /**
     * Returns a string representation of the PetFilter object.
     *
     * @return A string representation of the PetFilter object.
     */
    @Override
    public String toString() {
        return "PetFilter{" +
                "species='" + species + '\'' +
                ", available=" + available +
                ", minAge=" + minAge +
                ", maxAge=" + maxAge +
                ", location='" + location + '\'' +
                ", nameContains='" + nameContains + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PetFilter petFilter = (PetFilter) o;
        return Objects.equals(species, petFilter.species) &&
                Objects.equals(available, petFilter.available) &&
                Objects.equals(minAge, petFilter.minAge) &&
                Objects.equals(maxAge, petFilter.maxAge) &&
                Objects.equals(location, petFilter.location) &&
                Objects.equals(nameContains, petFilter.nameContains);
    }

    @Override
    public int hashCode() {
        return Objects.hash(species, available, minAge, maxAge, location, nameContains);
    }
}