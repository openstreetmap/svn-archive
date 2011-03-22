package relcontext;

import java.beans.PropertyChangeEvent;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.Shortcut;

import java.util.*;
import javax.swing.*;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import relcontext.actions.*;

/**
 * The new, advanced relation editing panel.
 * 
 * @author Zverik
 */
public class RelContextDialog extends ToggleDialog implements EditLayerChangeListener, ChosenRelationListener, SelectionChangedListener {
    private JList relationsList;
    private final DefaultListModel relationsData;
    private ChosenRelation chosenRelation;
    private JPanel topLine;

    public RelContextDialog() {
        super(tr("Advanced Relation Editor"), "icon_relcontext",
                tr("Opens advanced relation/multipolygon editor panel"),
                Shortcut.registerShortcut("view:relcontext", tr("Toggle: {0}", tr("Open Relation Editor")),
                KeyEvent.VK_R, Shortcut.GROUP_LAYER, Shortcut.SHIFT_DEFAULT), 150);

        JPanel rcPanel = new JPanel(new BorderLayout());

        chosenRelation = new ChosenRelation();
        chosenRelation.addChosenRelationListener(this);
        MapView.addEditLayerChangeListener(chosenRelation);

        relationsData = new DefaultListModel();
        relationsList = new JList(relationsData);
        relationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationsList.setCellRenderer(new OsmPrimitivRenderer() {
            @Override
            protected String getComponentToolTipText( OsmPrimitive value ) {
                return null;
            }
        });
        relationsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent e ) {
                if( Main.main.getEditLayer() == null ) {
                    return;
                }
                chosenRelation.set((Relation)relationsList.getSelectedValue());
                relationsList.clearSelection();
            }
        });
        rcPanel.add(new JScrollPane(relationsList), BorderLayout.CENTER);

        // [±][X] relation U [AZ][Down][Edit]
        topLine = new JPanel(new BorderLayout());
        JPanel topLeftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftButtons.add(new JButton(new AddRemoveMemberAction(chosenRelation)));
        topLeftButtons.add(new JButton(new ClearChosenRelationAction(chosenRelation)));
        topLine.add(topLeftButtons, BorderLayout.WEST);
        topLine.add(new ChosenRelationComponent(chosenRelation), BorderLayout.CENTER);
        JPanel topRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final Action downloadChosenRelationAction = new DownloadChosenRelationAction(chosenRelation);
        final JButton downloadButton = new JButton(downloadChosenRelationAction);
        topRightButtons.add(downloadButton);
        topRightButtons.add(new JButton(new EditChosenRelationAction(chosenRelation)));
        topLine.add(topRightButtons, BorderLayout.EAST);
        rcPanel.add(topLine, BorderLayout.NORTH);

        downloadChosenRelationAction.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent evt ) {
                downloadButton.setVisible(downloadChosenRelationAction.isEnabled());
            }
        });
        downloadButton.setVisible(false);
        topLine.setVisible(false);

        // [+][Multi] [X]Adm [X]Tags [X]1
        JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomLine.add(new JButton(new CreateRelationAction(chosenRelation)));
        bottomLine.add(new JButton(new CreateMultipolygonAction(chosenRelation)));
        rcPanel.add(bottomLine, BorderLayout.SOUTH);

        add(rcPanel, BorderLayout.CENTER);
    }

    @Override
    public void hideNotify() {
        SelectionEventManager.getInstance().removeSelectionListener(this);
        MapView.removeEditLayerChangeListener(this);
        DatasetEventManager.getInstance().removeDatasetListener(chosenRelation);
    }

    @Override
    public void showNotify() {
        SelectionEventManager.getInstance().addSelectionListener(this, FireMode.IN_EDT_CONSOLIDATED);
        MapView.addEditLayerChangeListener(this);
        DatasetEventManager.getInstance().addDatasetListener(chosenRelation, FireMode.IN_EDT);
    }

    public ChosenRelation getChosenRelation() {
        return chosenRelation;
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        if( topLine != null )
            topLine.setVisible(newRelation != null);
        // ?
    }

    public void selectionChanged( Collection<? extends OsmPrimitive> newSelection ) {
        if( !isVisible() || relationsData == null )
            return;
        // repopulate relations table
        relationsData.clear();
        if( newSelection == null )
            return;
        Set<Relation> rels = new HashSet<Relation>();
        for( OsmPrimitive element : newSelection ) {
            for( OsmPrimitive ref : element.getReferrers() ) {
                if( ref instanceof Relation && !ref.isIncomplete() && !ref.isDeleted() ) {
                    rels.add((Relation) ref);
                }
            }
        }
        for( Relation rel : rels )
            relationsData.addElement(rel);
    }

    private void updateSelection() {
        if (Main.main.getCurrentDataSet() == null) {
            selectionChanged(Collections.<OsmPrimitive>emptyList());
        } else {
            selectionChanged(Main.main.getCurrentDataSet().getSelected());
        }
    }

    public void editLayerChanged( OsmDataLayer oldLayer, OsmDataLayer newLayer ) {
        updateSelection();
    }
}
