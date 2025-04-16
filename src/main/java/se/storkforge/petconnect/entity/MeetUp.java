package se.storkforge.petconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class MeetUp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "geometry(Point, 4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @NotNull(message = "Location must not be null")
    private Point<G2D> location;

    @Future(message = "Date and time must be in the future")
    private LocalDateTime dateTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_meetup",
            joinColumns = @JoinColumn(name = "meetup_id"),              // Refers to MeetUp's ID
            inverseJoinColumns = @JoinColumn(name = "user_id")          // Refers to User's ID
    )
    private Set<User> participants = new HashSet<>();

    @NotNull(message = "Status is required")
    @Pattern(regexp = "PLANNED|CONFIRMED|CANCELED", message = "Status must be on of: PLANNED, CONFIRMED, CANCELED")
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Point<G2D> getLocation() {
        return location;
    }

    public void setLocation(Point<G2D> location) {
        this.location = location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MeetUp{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", dateTime=" + dateTime +
                ", participants=" + participants +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        MeetUp meetUp = (MeetUp) o;
        return getId() != null && Objects.equals(getId(), meetUp.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
