package reverter;

/**
 * Exception thrown if a changeset created by a modeartor redaction account is being reverted. 
 */
@SuppressWarnings("serial")
public class RevertRedactedChangesetException extends Exception {

    public RevertRedactedChangesetException(String message) {
        super(message);
    }

    public RevertRedactedChangesetException(Throwable cause) {
        super(cause);
    }

    public RevertRedactedChangesetException(String message, Throwable cause) {
        super(message, cause);
    }
}
