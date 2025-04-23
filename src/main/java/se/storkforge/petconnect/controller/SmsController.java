package se.storkforge.petconnect.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import se.storkforge.petconnect.service.SmsService;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Pattern;

@RestController
@RequestMapping("/sms")
@Validated
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendSms(
            @RequestParam String to,
            @RequestParam(defaultValue = "Don't forget the meet-up!") String content) {

        if (to == null || to.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number cannot be empty");
        }

        if (!to.matches("^\\+?[0-9]{10,15}$")) {
            return ResponseEntity.badRequest().body("Invalid phone number format");
        }

        smsService.sendSms(to, content);
        return ResponseEntity.ok("SMS sent to " + to);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleValidationExceptions(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getConstraintViolations().iterator().next().getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getParameterName() + " parameter is missing");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to send SMS: " + ex.getMessage());
    }
}