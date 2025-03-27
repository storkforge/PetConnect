package se.storkforge.petconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.storkforge.petconnect.service.MailService;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @GetMapping("/send")
    public ResponseEntity<String> sendMail(@RequestParam String to) {
        mailService.sendMeetUpNotification(to, "MeetUp Reminder", "Don't forget the meet-up!");
        return ResponseEntity.ok("Mail sent to " + to);
    }
}
