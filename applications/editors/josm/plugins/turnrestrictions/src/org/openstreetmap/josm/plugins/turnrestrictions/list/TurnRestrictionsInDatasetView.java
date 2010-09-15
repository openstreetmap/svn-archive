package org.openstreetmap.josm.plugins.turnrestrictions.list;

import java.awt.BorderLayout;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;

/**
 * This is the view for the list of turn restrictions in the current data set.
 */
public class TurnRestrictionsInDatasetView extends AbstractTurnRestrictionsListView{    
    protected void build() {
        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
        model = new TurnRestrictionsInDatasetListModel(selectionModel);
        lstTurnRestrictions = new JList(model);
        lstTurnRestrictions.setSelectionModel(selectionModel);
        lstTurnRestrictions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstTurnRestrictions.setCellRenderer(new TurnRestrictionCellRenderer());
        
        setLayout(new BorderLayout());
        add(new JScrollPane(lstTurnRestrictions), BorderLayout.CENTER);
    }

    protected void registerAsListener() {
        MapView.addEditLayerChangeListener((EditLayerChangeListener)model);
        DatasetEventManager.getInstance().addDatasetListener((DataSetListener)model, FireMode.IN_EDT);
        if (Main.main.getEditLayer() != null) {
            model.setTurnRestrictions(Main.main.getEditLayer().data.getRelations());
        }
    }

    protected void unregisterAsListener() {
        MapView.removeEditLayerChangeListener((EditLayerChangeListener)model);
        DatasetEventManager.getInstance().removeDatasetListener((DataSetListener)model);
    }

    public TurnRestrictionsInDatasetView() {
        build();
    }
}
