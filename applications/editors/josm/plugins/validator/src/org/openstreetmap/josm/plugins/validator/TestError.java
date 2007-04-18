package org.openstreetmap.josm.plugins.validator;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.command.Command;
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
	private List<OsmPrimitive> primitives;
	/** The tester that raised this error */
	private Test tester;
	/** Internal code used by testers to classify errors */
	private int internalCode;
	
	/**
	 * Constructor
	 */
	public TestError()
	{
	}

	/**
	 * Constructor
	 * @param tester The tester
	 * @param severity The severity of this error
	 * @param message The error message
	 * @param primitives The affected primitives
	 */
	public TestError(Test tester, Severity severity, String message, List<OsmPrimitive> primitives)
	{
		this.tester = tester;
		this.severity = severity;
		this.message = message;
		this.primitives = primitives;
	}
	
	/**
	 * Constructor
	 * @param tester The tester
	 * @param severity The severity of this error
	 * @param message The error message
	 * @param primitive The affected primitive
	 */
	public TestError(Test tester, Severity severity, String message, OsmPrimitive primitive)
	{
		this.tester = tester;
		this.severity = severity;
		this.message = message;
		
		List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
		primitives.add(primitive);
		
		this.primitives = primitives;
	}
	
	/**
	 * Constructor
	 * @param tester The tester
	 * @param severity The severity of this error
	 * @param message The error message
	 * @param primitive The affected primitive
	 * @param internalCode The internal code
	 */
	public TestError(Test tester, Severity severity, String message, OsmPrimitive primitive, int internalCode)
	{
		this(tester, severity, message, primitive);
		this.internalCode = internalCode;
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
	public List<OsmPrimitive> getPrimitives() 
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

	/**
	 * Gets the tester that raised this error 
	 * @return the tester that raised this error
	 */
	public Test getTester() 
	{
		return tester;
	}

	/**
	 * Gets the internal code
	 * @return the internal code
	 */
	public int getInternalCode() 
	{
		return internalCode;
	}

	
	/**
	 * Sets the internal code
	 * @param internalCode The internal code
	 */
	public void setInternalCode(int internalCode) 
	{
		this.internalCode = internalCode;
	}
	
	/**
	 * Returns true if the error can be fixed automatically
	 * 
	 * @return true if the error can be fixed
	 */
	public boolean isFixable()
	{
		return tester != null && tester.isFixable(this);
	}
	
	/**
	 * Fixes the error with the appropiate command
	 * 
	 * @return The command to fix the error
	 */
	public Command getFix()
	{
		if( tester == null )
			return null;
		
		return tester.fixError(this);
	}	
}
