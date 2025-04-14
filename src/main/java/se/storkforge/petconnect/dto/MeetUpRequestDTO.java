package se.storkforge.petconnect.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MeetUpRequestDTO {
    private double latitude;
    private double longitude;
    private LocalDateTime dateTime;
    private List<Long> participantIds;

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
}
