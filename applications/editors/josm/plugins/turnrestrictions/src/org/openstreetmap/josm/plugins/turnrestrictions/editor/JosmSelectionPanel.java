package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListProvider;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListTransferHandler;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * This panels displays the objects currently selected in the current
 * JOSM edit layer.
 *
 */
public class JosmSelectionPanel extends JPanel {
    /**  the list view */
    private JList lstSelection;
    /** the model managing the selection */
    private JosmSelectionListModel model;
    
    private CopyAction actCopy;
    private TransferHandler transferHandler;
    
    /**
     * builds the UI for the panel 
     */
    protected void build(OsmDataLayer layer) {
        setLayout(new BorderLayout());
        lstSelection = new JList(model);
        lstSelection.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstSelection.setSelectionModel(model.getListSelectionModel());
        lstSelection.setCellRenderer(new OsmPrimitivRenderer());
        lstSelection.setTransferHandler(transferHandler = new JosmSelectionTransferHandler(model));
        lstSelection.setDragEnabled(true);
        
        add(new JScrollPane(lstSelection), BorderLayout.CENTER);
        add(new JLabel(tr("Selection")), BorderLayout.NORTH);
        
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));        
        actCopy = new CopyAction();
        lstSelection.addMouseListener(new PopupLauncher());
    }
    
    /**
     * Creates the JOSM selection panel for the selection in an OSM data layer
     * 
     * @param layer the data layer. Must not be null.
     * @exception IllegalArgumentException thrown if {@code layer} is null
     */
    public JosmSelectionPanel(OsmDataLayer layer, JosmSelectionListModel model) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        this.model = model;
        build(layer); 
    }
    
    /**
     * wires the UI as listener to global event sources 
     */
    public void wireListeners() {
        MapView.addEditLayerChangeListener(model);
        DatasetEventManager.getInstance().addDatasetListener(model, FireMode.IN_EDT);
        SelectionEventManager.getInstance().addSelectionListener(model, FireMode.IN_EDT_CONSOLIDATED);
    }
    
    /**
     * removes the UI as listener to global event sources 
     */
    public void unwireListeners() {
        MapView.removeEditLayerChangeListener(model);
        DatasetEventManager.getInstance().removeDatasetListener(model);
        SelectionEventManager.getInstance().removeSelectionListener(model);     
    }
    
    class PopupLauncher extends PopupMenuLauncher {
        @Override
        public void launch(MouseEvent evt) {
            new PopupMenu().show(lstSelection, evt.getX(), evt.getY());
        }       
    }
    
    class PopupMenu extends JPopupMenu {
        public PopupMenu() {
            JMenuItem item = add(actCopy);
            item.setTransferHandler(transferHandler);
            actCopy.setEnabled(!model.getSelected().isEmpty());
        }
    }

    class CopyAction extends AbstractAction {
        private Action delegate;
        
        public CopyAction(){
            putValue(NAME, tr("Copy"));
            putValue(SHORT_DESCRIPTION, tr("Copy to the clipboard"));
            putValue(SMALL_ICON, ImageProvider.get("copy"));
            putValue(ACCELERATOR_KEY, Shortcut.getCopyKeyStroke());
            delegate = lstSelection.getActionMap().get("copy");
        }

        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);
        }
    }
    
    static private class JosmSelectionTransferHandler extends PrimitiveIdListTransferHandler {
        public JosmSelectionTransferHandler(PrimitiveIdListProvider provider) {
            super(provider);
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            // the JOSM selection list is read-only. Don't allow to drop or paste
            // data on it
            return false;
        }
    }
}
