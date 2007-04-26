package org.openstreetmap.josm.plugins.validator;

import java.awt.Color;

import org.openstreetmap.josm.data.osm.visitor.SimplePaintVisitor;

/** The error severity */
public enum Severity {
	/** Error messages */
	ERROR("Errors", "error.gif",       SimplePaintVisitor.getPreferencesColor("validation error", Color.RED)),
	/** Warning messages */ 
	WARNING("Warnings", "warning.gif", SimplePaintVisitor.getPreferencesColor("validation warning", Color.YELLOW)), 
	/** Other messages */ 
	OTHER("Other", "other.gif",        SimplePaintVisitor.getPreferencesColor("validation other", Color.CYAN)); 
	
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