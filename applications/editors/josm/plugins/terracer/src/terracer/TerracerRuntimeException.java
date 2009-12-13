/**
 * Terracer: A JOSM Plugin for terraced houses.
 *
 * Copyright 2009 CloudMade Ltd.
 *
 * Released under the GPLv2, see LICENSE file for details.
 */
package terracer;

/**
 * The Class TerracerRuntimeException indicates errors from the Terracer Plugin.
 * 
 * @author casualwalker
 */
public class TerracerRuntimeException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 857926026580277816L;

	/**
	 * Default constructor.
	 */
	public TerracerRuntimeException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TerracerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public TerracerRuntimeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public TerracerRuntimeException(Throwable cause) {
		super(cause);
	}

}
