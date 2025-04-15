package se.storkforge.petconnect.dto;

import jakarta.validation.constraints.*;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MeetUpRequestDTO {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @NotNull(message = "Date and time is required")
    @FutureOrPresent(message = "Date and time must be in the future or now")
    private LocalDateTime dateTime;

    @NotNull(message = "Participant list cannot be null")
    @Size(min = 1, message = "At least one participant must be selected")
    private List<@NotNull(message = "Participant ID cannot be null") Long> participantIds;

    @NotBlank(message = "Status must not be blank")
    private String status;

    // Getters and Setters

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public MeetUpRequestDTO() {
    }

    public static MeetUpRequestDTO fromEntity(MeetUp meetUp) {
        MeetUpRequestDTO dto = new MeetUpRequestDTO();
        dto.setLatitude(meetUp.getLocation().getPosition().getLat());
        dto.setLongitude(meetUp.getLocation().getPosition().getLon());
        dto.setDateTime(meetUp.getDateTime());
        dto.setStatus(meetUp.getStatus());

        if (meetUp.getParticipants() != null) {
            List<Long> ids = meetUp.getParticipants()
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            dto.setParticipantIds(ids);
        }
        return dto;
    }
}
