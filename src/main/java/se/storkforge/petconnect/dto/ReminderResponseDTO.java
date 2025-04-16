package se.storkforge.petconnect.dto;

import java.time.LocalDateTime;

public class ReminderResponseDTO {

    private Long id;
    private String title;
    private String petName; // Hämta från Pet-entiteten
    private String type;
    private LocalDateTime scheduledDate;
    private String notes;

    public ReminderResponseDTO() {
    }

    public ReminderResponseDTO(Long id, String title, String petName, String type, LocalDateTime scheduledDate, String notes) {
        this.id = id;
        this.title = title;
        this.petName = petName;
        this.type = type;
        this.scheduledDate = scheduledDate;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}