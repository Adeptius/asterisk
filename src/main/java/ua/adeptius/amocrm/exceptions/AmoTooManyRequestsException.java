package ua.adeptius.amocrm.exceptions;


public class AmoTooManyRequestsException extends AmoException {

    public AmoTooManyRequestsException() {
    }

    public AmoTooManyRequestsException(String message) {
        super(message);
    }
}
