package org.openstreetmap.josm.plugins.validator;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.validator.util.AgregatePrimitivesVisitor;

/**
 * The action that does the validate thing.
 * <p>
 * This action iterates through all active tests and give them the data, so that
 * each one can test it.
 * 
 * @author frsantos
 */
public class ValidateAction extends JosmAction 
{
	/** Serializable ID */
    private static final long serialVersionUID = -2304521273582574603L;

    /**
	 * Constructor
	 */
	public ValidateAction()
	{
		super("Validation", "validator", "Performs the data validation", KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_MASK, true);
	}

	public void actionPerformed(ActionEvent ev)
	{
		OSMValidatorPlugin plugin = OSMValidatorPlugin.getPlugin();
		plugin.errors = new ArrayList<TestError>();
		plugin.validationDialog.setVisible(true);
		
		Collection<Test> tests = OSMValidatorPlugin.getTests(true);
		if( tests.isEmpty() )
			return;
		
		Collection<OsmPrimitive> selection = Main.ds.getSelected();
		if( selection.isEmpty() )
			selection = Main.ds.allNonDeletedPrimitives();
		else
		{
			AgregatePrimitivesVisitor v = new AgregatePrimitivesVisitor();
			selection = v.visit(selection);
		}

		for(Test test : tests) 
        {
		    test.startTest();
		    test.visit(selection);
			test.endTest();
			plugin.errors.addAll( test.getErrors() );
		}
		tests = null;
		
		plugin.validationDialog.refresh();
	}
}
