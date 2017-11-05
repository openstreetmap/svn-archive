package nanolog;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;

import nanolog.NanoLogLayer.NanoLogLayerListener;

/**
 * NanoLog Panel. Displays the selected log item, along with surrounding 30-50 lines.
 *
 * @author zverik
 */
public class NanoLogPanel extends ToggleDialog implements LayerChangeListener, NanoLogLayerListener {
    private JList<String> logPanel;
    private LogListModel listModel;

    public NanoLogPanel() {
        super(tr("NanoLog"), "nanolog", tr("Open NanoLog panel"), null, 150, false);

        listModel = new LogListModel();
        logPanel = new JList<>(listModel);
        createLayout(logPanel, true, null);
    }

    public void updateMarkers() {
        List<NanoLogEntry> entries = new ArrayList<>();
        for (NanoLogLayer l : MainApplication.getLayerManager().getLayersOfType(NanoLogLayer.class)) {
            entries.addAll(l.getEntries());
        }
        listModel.setEntries(entries);
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
        Layer newLayer = e.getAddedLayer();
        if (newLayer instanceof NanoLogLayer)
            ((NanoLogLayer) newLayer).addListener(this);
        updateMarkers();
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        updateMarkers();
    }

    @Override
    public void markersUpdated(NanoLogLayer layer) {
        updateMarkers();
    }

    @Override
    public void markerActivated(NanoLogLayer layer, NanoLogEntry entry) {
        int idx = entry == null ? -1 : listModel.find(entry);
        if (idx >= 0) {
            logPanel.setSelectedIndex(idx);
            Rectangle rect = logPanel.getCellBounds(Math.max(0, idx-2), Math.min(idx+4, listModel.getSize()));
            logPanel.scrollRectToVisible(rect);
        }
    }

    private static class LogListModel extends AbstractListModel<String> {
        private List<NanoLogEntry> entries;
        private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

        @Override
        public int getSize() {
            return entries.size();
        }

        @Override
        public String getElementAt(int index) {
            return TIME_FORMAT.format(entries.get(index).getTime()) + " " + entries.get(index).getMessage();
        }

        public void setEntries(List<NanoLogEntry> entries) {
            this.entries = entries;
            fireContentsChanged(this, 0, entries.size());
        }

        public int find(NanoLogEntry entry) {
            return entries.indexOf(entry);
        }
    }
}
