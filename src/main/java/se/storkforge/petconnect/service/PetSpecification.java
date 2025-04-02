package se.storkforge.petconnect.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import se.storkforge.petconnect.entity.Pet;
import java.util.ArrayList;
import java.util.List;

public class PetSpecification {

    /**
     * Creates a JPA Specification for filtering Pet entities.
     *
     * @param filter the criteria to filter pets by
     * @return a Specification that can be used with JpaSpecificationExecutor
     */
    public static Specification<Pet> filterPets(PetFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSpecies() != null) {
                predicates.add(criteriaBuilder.equal(root.get("species"), filter.getSpecies()));
            }

            if (filter.getAvailable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("available"), filter.getAvailable()));
            }

            if (filter.getMinAge() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("age"), filter.getMinAge()));
            }

            if (filter.getMaxAge() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("age"), filter.getMaxAge()));
            }

            if (filter.getLocation() != null) {
                predicates.add(criteriaBuilder.equal(root.get("location"), filter.getLocation()));
            }

            if (filter.getNameContains() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getNameContains().toLowerCase() + "%"));
            }
            return predicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}