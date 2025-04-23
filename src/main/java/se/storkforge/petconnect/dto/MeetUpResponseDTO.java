package se.storkforge.petconnect.dto;

import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class MeetUpResponseDTO {
    private Long id;
    private Double longitude;
    private Double latitude;
    private String status;
    private List<ParticipantDTO> participants;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantDTO> participants) {
        this.participants = participants;
    }

    public static class ParticipantDTO {
        private Long id;
        private String email;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public MeetUpResponseDTO() {}

    public MeetUpResponseDTO(MeetUp meetUp) {
        this.id = meetUp.getId();
        if (meetUp.getLocation() != null) {
            this.longitude = meetUp.getLocation().getPosition().getLon();
            this.latitude = meetUp.getLocation().getPosition().getLat();
        }
        this.status = meetUp.getStatus();
        this.participants = meetUp.getParticipants().stream()
                .map(user -> {
                    ParticipantDTO dto = new ParticipantDTO();
                    dto.setId(user.getId());
                    dto.setEmail(user.getEmail());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}