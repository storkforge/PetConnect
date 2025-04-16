package se.storkforge.petconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.storkforge.petconnect.entity.MeetUp;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetUpRepository extends JpaRepository<MeetUp, Long> {

    // a combined spatial and time query (recommended for most use cases)
    @Query(value = """
        SELECT * FROM meet_up
        WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius)
        AND date_time BETWEEN :start AND :end
        """, nativeQuery = true)
    List<MeetUp> findMeetUpsNearAndWithinTime(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radius") double radiusInMeters,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    List<MeetUp> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = """
        SELECT * FROM meet_up 
        WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius)
        """, nativeQuery = true)
    List<MeetUp> findMeetUpsNear(@Param("longitude") double longitude,
                                 @Param("latitude") double latitude,
                                 @Param("radius") double radiusInMeters);
}
