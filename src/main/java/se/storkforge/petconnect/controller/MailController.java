package se.storkforge.petconnect.controller;

import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.storkforge.petconnect.service.MailService;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    /**
     * Endpoint to send an email notification.
     * Accepts recipient address, subject, and content via request parameters.
     * @param to - recipient's email address (validated).
     * @param subject - subject of the email (default is "MeetUp Reminder").
     * @param content - body of the email (default is "Don't forget the meet-up!").
     * @return HTTP response indicating the success or failure of the operation.
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendMail(
            @RequestParam @Email String to,
            @RequestParam(defaultValue = "MeetUp Reminder") String subject,
            @RequestParam(defaultValue = "Don't forget the meet-up!") String content) {
        try {
            mailService.sendMeetUpNotification(to, subject, content);
            return ResponseEntity.ok("Mail sent to " + to);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }


    }
}
