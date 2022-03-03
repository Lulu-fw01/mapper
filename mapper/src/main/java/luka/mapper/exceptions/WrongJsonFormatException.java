package luka.mapper.exceptions;

public class WrongJsonFormatException extends RuntimeException {

    public WrongJsonFormatException(String message) {
        super(message);
    }
}
