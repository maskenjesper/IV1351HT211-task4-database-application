package model;

public class RentalException extends Exception {
    public RentalException(String reason) {
        super(reason);
    }

    /**
     * Create a new instance thrown because of the specified reason and exception.
     *
     * @param reason    Why the exception was thrown.
     * @param rootCause The exception that caused this exception to be thrown.
     */
    public RentalException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }


    public RentalException(String studentId, String instrumentId, String reason) {
        super(String.format("Student [%s] failed to rent instrument [%s] because of : %s",studentId,instrumentId,reason));
    }
}
