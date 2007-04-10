package org.openstreetmap.josm.plugins.validator;

import java.util.*;

import javax.swing.JPanel;

import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.visitor.Visitor;

/**
 * Parent class for all validation tests.
 * <p>
 * A test is a primitive visitor, so that it can access to all data to be
 * validated. These primitives are always visited in the same order: nodes
 * first, then segments, and ways last.
 * 
 * @author frsantos
 */
public class Test implements Visitor
{
	/** Name of the test */
	protected String name;
	
	/** Description of the test */
	protected String description;
	
	/** Whether this test is enabled. Used by peferences */
	protected boolean enabled;

	/** The list of errors */
	protected List<TestError> errors = new ArrayList<TestError>(30);

	/**
	 * Constructor
	 * @param name Name of the test
	 * @param description Description of the test
	 */
	public Test(String name, String description)
	{
		this.name = name;
		this.description = description;
	}
	
	/**
	 * Constructor
	 * @param name Name of the test
	 */
	public Test(String name)
	{
		this.name = name;
	}
	
	/**
	 * Initializes any global data used this tester.
	 * @param plugin The plugin
	 * @throws Exception When cannot initialize the test
	 */ 
	public static void initialize(@SuppressWarnings("unused") OSMValidatorPlugin plugin) throws Exception {}
	
	/**
	 * Notification of the start of the test. The tester can initialize the
	 * structures it may need for this test
	 */ 
	public void startTest() 
	{
		errors = new ArrayList<TestError>(30);
	}
	
	/**
	 * Gets the validation errors accumulated until this moment.
	 * @return The list of errors 
	 */
	public List<TestError> getErrors() 
	{
		return errors;
	}
	
	/**
	 * Notification of the end of the test. The tester may perform additional
	 * actions and destroy the used structures 
	 */
	public void endTest() {}

    /**
     * Visits all primitives to be tested. These primitives are always visited
     * in the same order: nodes first, then segments, and ways last.
     * 
     * @param selection The primitives to be tested
     */
    public void visit(Collection<OsmPrimitive> selection) 
    {
        for (OsmPrimitive p : selection)
        {
            p.visit(this);
        }
    }

    public void visit(Node n) {}

	public void visit(Segment s) {}

	public void visit(Way w) {}

	/**
	 * Allow the tester to manage its own preferences 
	 * @param testPanel The panel to add any preferences component
	 */
	public void addGui(@SuppressWarnings("unused") JPanel testPanel) 
	{
	}

	/**
	 * Called when the used submits the preferences
	 */
	public void ok() 
	{
	}
}
