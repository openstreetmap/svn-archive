package org.openstreetmap.josm.plugins.validator;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Validation error
 * @author frsantos
 */
public class TestError
{
	/** Severity */
	private Severity severity;
	/** The error message */
	private String message;
	/** The affected primitives */
	private List<? extends OsmPrimitive> primitives;
	
	/**
	 * Constructor
	 */
	public TestError()
	{
	}

	/**
	 * Constructor
	 * @param severity The severity of this error
	 * @param message The error message
	 * @param primitives The affected primitives
	 */
	public TestError(Severity severity, String message, List<? extends OsmPrimitive> primitives)
	{
		this.severity = severity;
		this.message = message;
		this.primitives = primitives;
	}
	
	/**
	 * Constructor
	 * @param severity The severity of this error
	 * @param message The error message
	 * @param primitive The affected primitive
	 */
	public TestError(Severity severity, String message, OsmPrimitive primitive)
	{
		this.severity = severity;
		this.message = message;
		
		List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
		primitives.add(primitive);
		
		this.primitives = primitives;
	}
	
	/**
	 * Gets the error message
	 * @return the error message
	 */
	public String getMessage() 
	{
		return message;
	}
	
	/**
	 * Sets the error message
	 * @param message The error message
	 */
	public void setMessage(String message) 
	{
		this.message = message;
	}
	
	/**
	 * Gets the list of primitives affected by this error 
	 * @return the list of primitives affected by this error
	 */
	public List<? extends OsmPrimitive> getPrimitives() 
	{
		return primitives;
	}

	/**
	 * Sets the list of primitives affected by this error 
	 * @param primitives the list of primitives affected by this error
	 */

	public void setPrimitives(List<OsmPrimitive> primitives) 
	{
		this.primitives = primitives;
	}

	/**
	 * Gets the severity of this error
	 * @return the severity of this error
	 */
	public Severity getSeverity() 
	{
		return severity;
	}

	/**
	 * Sets the severity of this error
	 * @param severity the severity of this error
	 */
	public void setSeverity(Severity severity) 
	{
		this.severity = severity;
	}
}
