package org.openstreetmap.josm.plugins.contourmerge.util;

import java.text.MessageFormat;

public class Assert {
	public static void checkArgNotNull(Object arg, String name) throws IllegalArgumentException {
		if (arg == null){
			throw new IllegalArgumentException(
					MessageFormat.format("argument ''{0}'' must not be null", name) // don't translate, it's a technical message
			);
		}
	}
	
	public static void checkArg(boolean cond, String msg, Object... values){
		if (!cond){
			throw new IllegalArgumentException(
					MessageFormat.format(msg, values)
			);
		}
	}
	
	public static void assertTrue(boolean cond, String msg, Object... values) throws AssertionError {
		if (!cond){
			throw new AssertionError(
					MessageFormat.format(msg, values)
			);
		}
	}
}
