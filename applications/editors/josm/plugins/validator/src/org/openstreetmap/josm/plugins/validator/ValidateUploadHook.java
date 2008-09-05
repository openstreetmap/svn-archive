package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.util.*;

import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.UploadAction.UploadHook;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.validator.util.AgregatePrimitivesVisitor;
import org.openstreetmap.josm.tools.GBC;

/**
 * The action that does the validate thing.
 * <p>
 * This action iterates through all active tests and give them the data, so that
 * each one can test it.
 * 
 * @author frsantos
 */
public class ValidateUploadHook implements UploadHook
{
	/** Serializable ID */
    private static final long serialVersionUID = -2304521273582574603L;

    /**
     * Validate the modified data before uploading
     */
    public boolean checkUpload(Collection<OsmPrimitive> add, Collection<OsmPrimitive> update, Collection<OsmPrimitive> delete)
    {
        Collection<Test> tests = OSMValidatorPlugin.getEnabledTests(true);
        if( tests.isEmpty() )
            return true;
        
        AgregatePrimitivesVisitor v = new AgregatePrimitivesVisitor();
        v.visit(add);
        Collection<OsmPrimitive> selection = v.visit(update);

        List<TestError> errors = new ArrayList<TestError>(30);
        for(Test test : tests) 
        {
            test.setBeforeUpload(true);
            test.setPartialSelection(true);
            test.startTest();
            test.visit(selection);
            test.endTest();
            errors.addAll( test.getErrors() );
        }
        tests = null;
        
        return displayErrorScreen(errors);
    }
    
    /**
     * Displays a screen where the actions that would be taken are displayed and
     * give the user the possibility to cancel the upload.
     * @param errors The errors displayed in the screen
     * @return <code>true</code>, if the upload should continue. <code>false</code>
     *          if the user requested cancel.
     */
    private boolean displayErrorScreen(List<TestError> errors) 
    {
        if( errors == null || errors.isEmpty() ) 
        {
            return true;
        }

        JPanel p = new JPanel(new GridBagLayout());
        ErrorTreePanel errorPanel = new ErrorTreePanel(errors);
        errorPanel.expandAll();
        p.add(new JScrollPane(errorPanel), GBC.eol());

        return JOptionPane.showConfirmDialog(Main.parent, p, tr("Data with errors. Upload anyway?"),
                                             JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }    
}
