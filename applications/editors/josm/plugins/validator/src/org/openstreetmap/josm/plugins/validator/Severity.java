package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;

import org.openstreetmap.josm.data.Preferences;

/** The error severity */
public enum Severity {
	/** Error messages */
	ERROR(tr("Errors"), "error.gif",       Preferences.getPreferencesColor(marktr("validation error"), Color.RED)),
	/** Warning messages */ 
	WARNING(tr("Warnings"), "warning.gif", Preferences.getPreferencesColor(marktr("validation warning"), Color.YELLOW)), 
	/** Other messages */ 
	OTHER(tr("Other"), "other.gif",        Preferences.getPreferencesColor(marktr("validation other"), Color.CYAN)); 
	
	/** Description of the severity code */
	private final String message;
	
    /** Associated icon */
    private final String icon;

    /** Associated color */
    private final Color color;

	/**
	 * Constructor
	 * 
	 * @param message Description
	 * @param icon Associated icon
	 * @param color The color of this severity
	 */
    Severity(String message, String icon, Color color) 
    {
        this.message = message;
		this.icon = icon;
        this.color = color;
    }

	@Override
	public String toString() 
	{
		return message;
	}

	/** 
	 * Gets the associated icon
	 * @return the associated icon
	 */
	public String getIcon() 
	{
		return icon;
	}

    /**
     * Gets the associated color
     * @return The associated color
     */
    public Color getColor()
    {
        return color;
    }
    
    
}
