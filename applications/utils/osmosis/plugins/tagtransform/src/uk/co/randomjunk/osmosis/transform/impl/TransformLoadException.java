// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.impl;

public class TransformLoadException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TransformLoadException(String message, Exception cause) {
		super(message, cause);
	}

}
