package edu.uniquindio.stayhub_v2.exception;

public class AccommodationAlreadyBookedException extends RuntimeException {
    public AccommodationAlreadyBookedException(String message) {
        super(message);
    }
}
