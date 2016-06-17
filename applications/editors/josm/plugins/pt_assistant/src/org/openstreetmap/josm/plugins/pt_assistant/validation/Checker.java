package org.openstreetmap.josm.plugins.pt_assistant.validation;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;


/**
 * Represents tests and fixed of the PT_Assistant plugin 
 * 
 * @author darya
 *
 */
public abstract class Checker {
	
	// test which created this WayChecker:
	protected final Test test;

	// relation that is checked:
	protected Relation relation;

	// stores all found errors:
	protected ArrayList<TestError> errors = new ArrayList<>();
	
	protected Checker(Relation relation, Test test) {
		
		this.relation = relation;
		this.test = test;

	}
	
	/**
	 * Returns errors
	 */
	public List<TestError> getErrors() {

		return errors;
	}
	
	
	

}
