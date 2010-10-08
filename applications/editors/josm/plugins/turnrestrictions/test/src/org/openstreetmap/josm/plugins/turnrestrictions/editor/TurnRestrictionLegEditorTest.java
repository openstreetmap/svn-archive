package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListProvider;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListTransferHandler;

/**
 * Simple test application to test functionality and layout of the 
 * {@see TurnRestrictionLegEditor}
 */
public class TurnRestrictionLegEditorTest extends JFrame {
    
    private JTextArea taTest;
    private TurnRestrictionLegEditor editor;
    private TurnRestrictionEditorModel model;
    private JList lstObjects;
    private DefaultListModel listModel;
    private DataSet dataSet;
    
    
    protected JPanel buildLegEditorPanel() {
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
        JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.0;       
        pnl.add(new JLabel("From"), gc);
        
        gc.weightx = 1.0;
        gc.gridx = 1;
        model = new TurnRestrictionEditorModel(layer, controler);
        dataSet = new DataSet();
        model.populate(new Relation());
        pnl.add(editor = new TurnRestrictionLegEditor(model, TurnRestrictionLegRole.FROM), gc);
        
        return pnl;
    }
    
    protected JPanel buildObjectListPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        listModel = new DefaultListModel();
        pnl.add(new JScrollPane(lstObjects = new JList(listModel)), BorderLayout.CENTER);
        lstObjects.setCellRenderer(new OsmPrimitivRenderer());      
        
        PrimitiveIdListProvider provider = new PrimitiveIdListProvider() {          
            public List<PrimitiveId> getSelectedPrimitiveIds() {
                List<PrimitiveId> ret = new ArrayList<PrimitiveId>();
                int [] sel = lstObjects.getSelectedIndices();
                for (int i: sel){
                    ret.add(((OsmPrimitive)lstObjects.getModel().getElementAt(i)).getPrimitiveId());
                }
                return ret;
            }
        };
        
        lstObjects.setTransferHandler(new PrimitiveIdListTransferHandler(provider));
        lstObjects.setDragEnabled(true);
        return pnl;
    }
    
    protected void build() {
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;    
        gc.insets = new Insets(20, 0, 20, 0);
        gc.weightx = 1.0;       
        gc.weighty = 0.0;
        add(buildLegEditorPanel(), gc);
        
        gc.gridy = 1;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        add(buildObjectListPanel(), gc);
        setSize(600,600);   
    }
    
    protected void initForTest1() {
        Way w = new Way(1);
        w.put("name", "way-1");
        
        editor.getModel().setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, w);
    }
    
    protected void initForTest2() {
        Way w = new Way(1);
        w.put("name", "way-1");     
        dataSet.addPrimitive(w);
        editor.getModel().setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, w);
        
        Node n = new Node(new LatLon(1,1));
        n.setOsmId(1, 1);
        n.put("name", "node.1");
        dataSet.addPrimitive(n);
        listModel.addElement(n);
        
        w = new Way();
        w.setOsmId(2,1);
        w.put("name", "way.1");
        dataSet.addPrimitive(w);
        listModel.addElement(w);
        
        Relation r = new Relation();
        r.setOsmId(3,1);
        r.put("name", "relation.1");
        dataSet.addPrimitive(r);
        listModel.addElement(r);
    }

    public TurnRestrictionLegEditorTest(){
        build();
        initForTest2();
    }
    
    static public void main(String args[]) {
        new TurnRestrictionLegEditorTest().setVisible(true);
    }
}
