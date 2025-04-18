package se.storkforge.petconnect.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class ReminderPreferences {
    private int remindHoursBefore = 24;
    private boolean emailReminder = true;
    private boolean smsReminder = true;


    public int getRemindHoursBefore() {
        return remindHoursBefore;
    }

    public void setRemindHoursBefore(int remindHoursBefore) {
        this.remindHoursBefore = remindHoursBefore;
    }

    public boolean isEmailReminder() {
        return emailReminder;
    }

    public void setEmailReminder(boolean emailReminder) {
        this.emailReminder = emailReminder;
    }

    public boolean isSmsReminder() {
        return smsReminder;
    }

    public void setSmsReminder(boolean smsReminder) {
        this.smsReminder = smsReminder;
    }
}
