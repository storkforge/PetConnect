package se.storkforge.petconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an email notification to a specified recipient.
     * @param to - recipient's email address.
     * @param subject - subject of the email.
     * @param text - content/body of the email.
     */
    public void sendMeetUpNotification(String to, String subject, String text) {
        if (to == null || to.isEmpty())
            throw new IllegalArgumentException("Recipient cannot be empty");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email notification", e);
        }

    }
}
