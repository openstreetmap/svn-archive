// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * A runtime exception indicating that the end of the store has been reached while reading.
 * 
 * @author Brett Henderson
 */
public class EndOfStoreException extends OsmosisRuntimeException {

	private static final long serialVersionUID = 1L;
	
	
	/**
     * Constructs a new exception with <code>null</code> as its detail message.
     */
    public EndOfStoreException() {
		super();
	}
    
	
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message.
     */
    public EndOfStoreException(String message) {
		super(message);
	}
    
	
    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * 
     * @param cause the cause.
     */
    public EndOfStoreException(Throwable cause) {
		super(cause);
	}
    
	
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public EndOfStoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
