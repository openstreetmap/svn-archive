package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * This is a simple test application to test the functionality/layout of 
 * the {@see TurnRestrictionComboBox}
 * 
 */
public class TurnRestrictionComboBoxTest extends JFrame {
    
    private TurnRestrictionEditorModel model;
    private DataSet ds = new DataSet();
    
    protected void build() {
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
        
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        
        TurnRestrictionComboBox cb = new TurnRestrictionComboBox(
                new TurnRestrictionComboBoxModel(model)
        );
        add(cb, gc);        
    }
    
    public TurnRestrictionComboBoxTest() {
        build();
        setSize(600,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public static void main(String args[]) {
        new TurnRestrictionComboBoxTest().setVisible(true);
    }

}
