package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import javax.swing.JFrame;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
/**
 * Simple application to test functionality and layout of the turn restriction editor.
 *
 */
public class TurnRestrictionEditorTest extends JFrame {
    
    public TurnRestrictionEditorTest() {
        setSize(10,10);
        TurnRestrictionEditor editor = new TurnRestrictionEditor(this, new OsmDataLayer(new DataSet(), "test", null));
        editor.setSize(600,600);
        editor.setVisible(true);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    static public void main(String args[]) {
        new TurnRestrictionEditorTest().setVisible(true);
    }
}
