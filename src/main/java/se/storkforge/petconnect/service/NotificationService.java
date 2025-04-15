package se.storkforge.petconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.User;

@Service
public class NotificationService {

    private final MailService mailService;
    private final SmsService smsService;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(MailService mailService, SmsService smsService) {
        this.mailService = mailService;
        this.smsService = smsService;
    }

    public void notifyUser(User user, String subject, String message) {
        Throwable emailFailure = null;

        try {
            mailService.sendMeetUpNotification(user.getEmail(), subject, message);
        } catch (Throwable e) {
            logger.warn("Failed to send email to {}.", user.getEmail(), e);
            emailFailure = e;
        }

        try {
            smsService.sendSms(user.getPhoneNumber(), message);
        } catch (Throwable e) {
            logger.warn("Failed to send SMS to {}.", user.getPhoneNumber(), e);
            if (emailFailure == null) throw e;
        }

        if (emailFailure != null) {
            throw new RuntimeException("Failed to send email notification", emailFailure);
        }
    }
}
