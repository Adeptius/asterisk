package ua.adeptius.amocrm.exceptions;


public class AmoWrongLoginOrApiKeyExeption extends AmoException {

    public AmoWrongLoginOrApiKeyExeption() {
    }

    public AmoWrongLoginOrApiKeyExeption(String message) {
        super(message);
    }
}
