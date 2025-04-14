package se.storkforge.petconnect.dto;

import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MeetUpRequestDTO {
    private double latitude;
    private double longitude;
    private LocalDateTime dateTime;
    private List<Long> participantIds;
    private String status;

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
