package org.openstreetmap.osmosis.core.store;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * A runtime exception indicating that the requested index value does not exist.
 * 
 * @author Brett Henderson
 */
public class NoSuchIndexElementException extends OsmosisRuntimeException {

	private static final long serialVersionUID = 1L;
	
	
	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     */
    public NoSuchIndexElementException() {
		super();
	}
    
	
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message.
     */
    public NoSuchIndexElementException(String message) {
		super(message);
	}
    
	
    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * 
     * @param cause the cause.
     */
    public NoSuchIndexElementException(Throwable cause) {
		super(cause);
	}
    
	
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public NoSuchIndexElementException(String message, Throwable cause) {
		super(message, cause);
	}
}
