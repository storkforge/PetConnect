package se.storkforge.petconnect.service;

public class PetFilter {
    private String species;
    private Boolean available;
    private Integer minAge;
    private Integer maxAge;
    private String location;
    private String nameContains;

    // Constructors
    public PetFilter() {}

    public PetFilter(String species, Boolean available) {
        this.species = species;
        this.available = available;
    }

    // Getters and Setters
    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNameContains() {
        return nameContains;
    }

    public void setNameContains(String nameContains) {
        this.nameContains = nameContains;
    }

    // Utility method to check if filter is empty
    public boolean isEmpty() {
        return species == null && available == null && minAge == null &&
                maxAge == null && location == null && nameContains == null;
    }
}