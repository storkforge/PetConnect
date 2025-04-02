package se.storkforge.petconnect.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import se.storkforge.petconnect.entity.Pet;
import java.util.ArrayList;
import java.util.List;

public class PetSpecification {

    private static final String SPECIES = "species";
    private static final String AVAILABLE = "available";
    private static final String AGE = "age";
    private static final String LOCATION = "location";
    private static final String NAME = "name";

    /**
     * Creates a JPA Specification for filtering Pet entities.
     *
     * @param filter the criteria to filter pets by
     * @return a Specification that can be used with JpaSpecificationExecutor
     */
    public static Specification<Pet> filterPets(PetFilter filter) {
        if (filter == null) {
            return Specification.where(null);
        }

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSpecies() != null) {
                predicates.add(criteriaBuilder.equal(root.get(SPECIES), filter.getSpecies()));
            }

            if (filter.getAvailable() != null) {
                predicates.add(criteriaBuilder.equal(root.get(AVAILABLE), filter.getAvailable()));
            }

            if (filter.getMinAge() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(AGE), filter.getMinAge()));
            }

            if (filter.getMaxAge() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(AGE), filter.getMaxAge()));
            }

            if (filter.getLocation() != null) {
                predicates.add(criteriaBuilder.equal(root.get(LOCATION), filter.getLocation()));
            }

            if (filter.getNameContains() != null) {
                String nameContains = filter.getNameContains().trim();
                if (!nameContains.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(NAME)),
                            "%" + nameContains.toLowerCase() + "%"));
                }
            }

            return predicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}