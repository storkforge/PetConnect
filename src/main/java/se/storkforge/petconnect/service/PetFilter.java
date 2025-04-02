package se.storkforge.petconnect.service;

/**
 * Filter criteria for searching pets.
 * Used in conjunction with PetSpecification to filter pets based on various attributes.
 */
public class PetFilter {
    private String species;
    private Boolean available;
    private Integer minAge;
    private Integer maxAge;
    private String location;
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
        this.species = species;
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
        this.location = location;
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
        this.nameContains = nameContains;
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
}