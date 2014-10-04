package nanolog;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;

import nanolog.NanoLogLayer.NanoLogLayerListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;

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
        logPanel = new JList<String>(listModel);
        createLayout(logPanel, true, null);
    }

    public void updateMarkers() {
        List<NanoLogEntry> entries = new ArrayList<NanoLogEntry>();
        for( NanoLogLayer l : Main.map.mapView.getLayersOfType(NanoLogLayer.class) ) {
            entries.addAll(l.getEntries());
        }
        listModel.setEntries(entries);
    }

    @Override
    public void activeLayerChange( Layer oldLayer, Layer newLayer ) {
        // todo
    }

    @Override
    public void layerAdded( Layer newLayer ) {
        if( newLayer instanceof NanoLogLayer )
            ((NanoLogLayer)newLayer).addListener(this);
        updateMarkers();
    }

    @Override
    public void layerRemoved( Layer oldLayer ) {
        updateMarkers();
    }

    @Override
    public void markersUpdated( NanoLogLayer layer ) {
        updateMarkers();
    }

    @Override
    public void markerActivated( NanoLogLayer layer, NanoLogEntry entry ) {
        int idx = entry == null ? -1 : listModel.find(entry);
        if( idx >= 0 ) {
            logPanel.setSelectedIndex(idx);
            Rectangle rect = logPanel.getCellBounds(Math.max(0, idx-2), Math.min(idx+4, listModel.getSize()));
            logPanel.scrollRectToVisible(rect);
        }
    }
    
    private class LogListModel extends AbstractListModel<String> {
        private List<NanoLogEntry> entries;
        private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

        public int getSize() {
            return entries.size();
        }

        public String getElementAt( int index ) {
            return TIME_FORMAT.format(entries.get(index).getTime()) + " " + entries.get(index).getMessage();
        }

        public void setEntries( List<NanoLogEntry> entries ) {
            this.entries = entries;
            fireContentsChanged(this, 0, entries.size());
        }

        public int find( NanoLogEntry entry ) {
            return entries.indexOf(entry);
        }
    }
}
