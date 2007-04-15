package org.openstreetmap.josm.plugins.validator;

/** The error severity */
public enum Severity {
	/** Error messages */
	ERROR("Errors", "error.gif"),
	/** Warning messages */ 
	WARNING("Warnings", "warning.gif"), 
	/** Other messages */ 
	OTHER("Other", "other.gif"); 
	
	/** Description of the severity code */
	private final String message;
	
	/** Associated icon */
	private final String icon;

	/**
	 * Constructor
	 * 
	 * @param message Description
	 * @param icon Associated icon
	 */
    Severity(String message, String icon) 
    {
        this.message = message;
		this.icon = icon;
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
    
    
}