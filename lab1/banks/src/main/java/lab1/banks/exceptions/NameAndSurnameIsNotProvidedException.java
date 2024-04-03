package lab1.banks.exceptions;

public final class NameAndSurnameIsNotProvidedException extends Exception {
    public NameAndSurnameIsNotProvidedException() {
        super();
    }

    public NameAndSurnameIsNotProvidedException(String message) {
        super(message);
    }
}
