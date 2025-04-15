package se.storkforge.petconnect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserOverbookedException extends RuntimeException {
    public UserOverbookedException(String message) {
        super(message);
    }
}