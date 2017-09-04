package ua.adeptius.amocrm.exceptions;


public class AmoWrongLoginOrApiKeyException extends AmoException {

    public AmoWrongLoginOrApiKeyException() {
    }

    public AmoWrongLoginOrApiKeyException(String message) {
        super(message);
    }
}
