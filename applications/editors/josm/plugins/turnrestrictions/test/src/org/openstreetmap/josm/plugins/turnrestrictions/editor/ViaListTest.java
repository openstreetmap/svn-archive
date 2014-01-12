package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JList;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Simple test application to test the via list editor
 *
 */
public class ViaListTest  extends JFrame{
    
    private ViaList lstVias;
    private TurnRestrictionEditorModel model;
    private JList lstJOSMSelection;
    
    
    protected void build() {
        DataSet ds = new DataSet();
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
        gc.insets = new Insets(5,5,5,20);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 0.5;
        gc.weighty = 1.0;
        
        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
        c.add(lstVias = new ViaList(new ViaListModel(model, selectionModel), selectionModel), gc);
        
        gc.gridx = 1;
        c.add(lstJOSMSelection = new JList(), gc);
        
        setSize(600,600);       
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    protected void initTest1() {
        DataSet ds = new DataSet();
        Relation r = new Relation();
        Node n;
        for (int i = 1; i<10; i++){
            n = new Node(new LatLon(i,i));  
            n.put("name", "node." + i);
            ds.addPrimitive(n);
            r.addMember(new RelationMember("via",n));
        }       
        model.populate(r);
    }
    
    public ViaListTest() {
        build();        
        initTest1();
    }
    
    static public void main(String args[]) {
        new ViaListTest().setVisible(true);
    }
}
