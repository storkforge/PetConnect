package se.storkforge.petconnect.entity;

import jakarta.persistence.*;
import java.io.Serializable; // Lägg till denna import

@Entity
@Table(name = "roles")
public class Role implements Serializable { // Lägg till "implements Serializable"

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}