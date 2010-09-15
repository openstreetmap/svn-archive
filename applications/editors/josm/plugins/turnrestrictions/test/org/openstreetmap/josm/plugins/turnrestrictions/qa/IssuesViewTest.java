package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionEditorModel;

/**
 * Simple test application for layout and functionality of the issues view.
 */
public class IssuesViewTest extends JFrame {
    private IssuesModel model;
    
    protected void build() {
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        // mock a controler 
        NavigationControler controler = new NavigationControler() {
            public void gotoAdvancedEditor() {
            }

            public void gotoBasicEditor() {
            }

            public void gotoBasicEditor(BasicEditorFokusTargets focusTarget) {
            }           
        };
        OsmDataLayer layer = new OsmDataLayer(new DataSet(), "test", null);
        TurnRestrictionEditorModel editorModel = new TurnRestrictionEditorModel(layer, controler);
        model = new IssuesModel(editorModel);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        JScrollPane pane = new JScrollPane(new IssuesView(model));
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        c.add(pane, gc);
        
        List<Issue> issues = new ArrayList<Issue>();
        issues.add(new RequiredTagMissingError(model, "type", "restriction"));
        issues.add(new MissingRestrictionTypeError(model));
        model.populate(issues);
    }
    
    public IssuesViewTest() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,600);
        build();
    }
    
    public static void main(String args[]) {
        new IssuesViewTest().setVisible(true);
    }
}
