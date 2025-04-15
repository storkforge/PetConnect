package se.storkforge.petconnect.dto;

import jakarta.validation.constraints.*;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MeetUpRequestDTO {

    @Min(value = -90, message = "Latitude must be greater or equal to -90")
    @Max(value = 90, message = "Latitude must be less or equal to 90")
    private double latitude;

    @Min(value = -180, message = "Longitude must be greater or equal to -180")
    @Max(value = 180, message = "Longitude must be less or equal to 180")
    private double longitude;

    @NotNull(message = "Date and time must not be null")
    @Future(message = "Date and time must be in the future")
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
