package se.storkforge.petconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.storkforge.petconnect.service.SmsService;

@RestController
@RequestMapping("/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * Endpoint to send an SMS message.
     * Accepts phone number and message content via request parameters.
     * @param to - recipient's phone number.
     * @param content - message text (default is "Don't forget the meet-up!").
     * @return HTTP response indicating the success or failure of the operation.
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendSms(
            @RequestParam String to,
            @RequestParam(defaultValue = "Don't forget the meet-up!") String content) {
        try {
            smsService.sendSms(to, content);
            return ResponseEntity.ok("SMS sent to " + to);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send SMS: " + e.getMessage());
        }
    }
}
