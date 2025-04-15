package se.storkforge.petconnect.service;

public interface SmsSender {
    void send(String to, String from, String message);
}
