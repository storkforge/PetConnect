package se.storkforge.petconnect.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    @NotNull(message = "Participant list cannot be null")
    @Size(min = 1, message = "At least one participant must be selected")
    private List<@NotNull(message = "Participant ID cannot be null") Long> participantIds;

    @NotBlank(message = "Status must not be blank")
    private String status;

    // Getters and Setters
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static MeetUpRequestDTO fromEntity(MeetUp meetUp) {
        MeetUpRequestDTO dto = new MeetUpRequestDTO();
        dto.setLatitude(meetUp.getLocation().getPosition().getLat());
        dto.setLongitude(meetUp.getLocation().getPosition().getLon());
        dto.setDateTime(meetUp.getDateTime());
        dto.setStatus(meetUp.getStatus());
        dto.setParticipantIds(meetUp.getParticipants().stream()
                .map(User::getId)
                .collect(Collectors.toList()));
        return dto;
    }
}