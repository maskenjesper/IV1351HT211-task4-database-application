package model;

public class RentException extends Exception {
    public RentException(String reason) {
        super(reason);
    }

    /**
     * Create a new instance thrown because of the specified reason and exception.
     *
     * @param reason    Why the exception was thrown.
     * @param rootCause The exception that caused this exception to be thrown.
     */
    public RentException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}
