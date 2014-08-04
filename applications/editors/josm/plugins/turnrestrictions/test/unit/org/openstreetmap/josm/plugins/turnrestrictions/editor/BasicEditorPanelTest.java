package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Simple functional test for the layout / basic functionality of {@see BasicEditorPanel} 
 *   
 */
public class BasicEditorPanelTest extends JFrame {

    private TurnRestrictionEditorModel model;
    private DataSet ds;
    
    public BasicEditorPanelTest() {
        ds = new DataSet();
        OsmDataLayer layer =new OsmDataLayer(ds, "test",null);
        // mock a controler 
        NavigationControler controler = new NavigationControler() {
            public void gotoAdvancedEditor() {
            }

            public void gotoBasicEditor() {
            }

            public void gotoBasicEditor(BasicEditorFokusTargets focusTarget) {
            }           
        };
        model = new TurnRestrictionEditorModel(layer, controler);
        
        BasicEditorPanel panel = new BasicEditorPanel(model);
        
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(panel, BorderLayout.CENTER);      
        setSize(600,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    
    static public void main(String args[]) {
        new BasicEditorPanelTest().setVisible(true);
    }
}
