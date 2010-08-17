package org.openstreetmap.josm.plugins.remotecontrol;

/**
 * Contains a preference name to control permission for the operation
 * implemented by the RequestHandler, and an error message to be displayed
 * if not permitted.
 */
public class PermissionPref {
	String pref;
	String message;
	public PermissionPref(String pref, String message)
	{
		this.pref = pref;
		this.message = message;
	}
}