package nanolog;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.JumpToMarkerActions;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

/**
 * NanoLog layer: a set of points that can be georeferenced.
 *
 * @author zverik
 */
public class NanoLogLayer extends Layer implements JumpToMarkerActions.JumpToMarkerLayer {

    private List<NanoLogEntry> log;
    private int selectedEntry;
    private final Set<NanoLogLayerListener> listeners = new HashSet<>();
    private NLLMouseAdapter mouseListener;

    public NanoLogLayer(List<NanoLogEntry> entries) {
        super(tr("NanoLog"));
        log = new ArrayList<>(entries);
        selectedEntry = -1;
        mouseListener = new NLLMouseAdapter();
    }

    public void setupListeners() {
        MainApplication.getMap().mapView.addMouseListener(mouseListener);
        MainApplication.getMap().mapView.addMouseMotionListener(mouseListener);
    }

    @Override
    public synchronized void destroy() {
        MainApplication.getMap().mapView.removeMouseListener(mouseListener);
        MainApplication.getMap().mapView.removeMouseMotionListener(mouseListener);
        super.destroy();
    }

    public NanoLogLayer(File file) throws IOException {
        this(readNanoLog(file));
    }

    public void addListener(NanoLogLayerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NanoLogLayerListener listener) {
        listeners.remove(listener);
    }

    protected void fireMarkersChanged() {
        for (NanoLogLayerListener listener : listeners) {
            listener.markersUpdated(this);
        }
    }

    protected void fireMarkerSelected() {
        for (NanoLogLayerListener listener : listeners) {
            listener.markerActivated(this, selectedEntry < 0 ? null : log.get(selectedEntry));
        }
    }

    public List<NanoLogEntry> getEntries() {
        return Collections.unmodifiableList(log);
    }

    public static List<NanoLogEntry> readNanoLog(File file) throws IOException {
        final Pattern NANOLOG_LINE = Pattern.compile("(.+?)\\t(.+?)(?:\\s*\\{\\{(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)(?:,\\s*(\\d+))?\\}\\})?");
        final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SS");
        List<NanoLogEntry> result = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
            while (r.ready()) {
                String line = r.readLine();
                if (line != null) {
                    Matcher m = NANOLOG_LINE.matcher(line);
                    if (m.matches()) {
                        String time = m.group(1);
                        String message = m.group(2);
                        String lat = m.group(3);
                        String lon = m.group(4);
                        String dir = m.group(5);
                        Date timeDate = null;
                        try {
                            timeDate = fmt.parse(time);
                        } catch (ParseException e) {
                            Logging.warn(e);
                        }
                        if (message == null || message.length() == 0 || timeDate == null)
                            continue;
                        LatLon pos = null;
                        Integer direction = null;
                        if (lat != null && lon != null) {
                            try {
                                pos = new LatLon(Double.parseDouble(lat), Double.parseDouble(lon));
                                direction = Integer.valueOf(dir);
                            } catch (NumberFormatException e) {
                                Logging.trace(e);
                            }
                        }
                        result.add(new NanoLogEntry(timeDate, message, pos, direction));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds box) {
        // todo
        for (int i = 0; i < log.size(); i++) {
            NanoLogEntry entry = log.get(i);
            int radius = 4;
            if (entry.getPos() != null) {
                Point p = mv.getPoint(entry.getPos());
                g.setColor(selectedEntry == i ? Color.red : Color.yellow);
                g.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
            }
        }
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("nanolog.png");
    }

    @Override
    public String getToolTipText() {
        return tr("NanoLog of {0} entries", log.size());
    }

    @Override
    public void mergeFrom(Layer from) {
        // todo
    }

    @Override
    public boolean isMergable(Layer other) {
        return other instanceof NanoLogLayer;
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        for (NanoLogEntry entry : log) {
            v.visit(entry.getPos());
        }
    }

    @Override
    public Object getInfoComponent() {
        StringBuilder b = new StringBuilder();
        int cnt = 0;
        for (NanoLogEntry e : log) {
            if (e.getPos() != null)
                cnt++;
        }
        b.append(tr("NanoLog of {0} lines, {1} of them with coordinates.", log.size(), cnt));
        return b.toString();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[] {
            LayerListDialog.getInstance().createShowHideLayerAction(),
            LayerListDialog.getInstance().createDeleteLayerAction(),
            new RenameLayerAction(null, this),
            SeparatorLayerAction.INSTANCE,
            new CorrelateEntries(true),
            new CorrelateEntries(false),
            new SaveLayer(),
            SeparatorLayerAction.INSTANCE,
            new LayerListPopup.InfoAction(this)
        };
    }

    @Override
    public void jumpToNextMarker() {
        selectedEntry++;
        if (selectedEntry < 0)
            selectedEntry = 0;
        else if (selectedEntry >= log.size())
            selectedEntry = log.size() - 1;
        invalidate();
    }

    @Override
    public void jumpToPreviousMarker() {
        selectedEntry--;
        if (selectedEntry < 0)
            selectedEntry = 0;
        else if (selectedEntry >= log.size())
            selectedEntry = log.size() - 1;
        invalidate();
    }

    protected void setSelected(int i) {
        int newSelected = i >= 0 && i < log.size() ? i : -1;
        if (newSelected != selectedEntry) {
            selectedEntry = newSelected;
            fireMarkerSelected();
            invalidate();
        }
    }

    public void setSelected(NanoLogEntry entry) {
        if (entry == null)
            setSelected(-1);
        else {
            for (int i = 0; i < log.size(); i++) {
                if (entry.equals(log.get(i))) {
                    setSelected(i);
                    break;
                }
            }
        }
    }

    private class NLLMouseAdapter extends MouseAdapter {
        private int dragging;

        public int nearestEntry(MouseEvent e) {
            LatLon ll = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
            int radius = 8;
            if (ll != null) {
                LatLon lld = MainApplication.getMap().mapView.getLatLon(e.getX() + radius, e.getY() + radius);
                double distance = Math.max(lld.lat() - ll.lat(), lld.lon() - ll.lon());
                boolean selectedIsSelected = false;
                int newSelected = -1;
                for (int i = 0; i < log.size(); i++) {
                    if (log.get(i).getPos() != null && log.get(i).getPos().distance(ll) < distance) {
                        newSelected = i;
                        if (i == selectedEntry)
                            selectedIsSelected = true;
                    }
                }
                if (newSelected >= 0)
                    return selectedIsSelected ? selectedEntry : newSelected;
            }
            return -1;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int nearest = nearestEntry(e);
            if (nearest > 0)
                setSelected(nearest);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            doDrag(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragging > 0) {
                dragging = 0;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int nearest = nearestEntry(e);
            if (nearest > 0 && MainApplication.getLayerManager().getActiveLayer() == NanoLogLayer.this) {
                dragging = nearest;
                doDrag(e);
            }
        }

        private void doDrag(MouseEvent e) {
            if (dragging > 0)
                dragTo(dragging, e.getX(), e.getY());
        }
    }

    protected void dragTo(int entry, int x, int y) {
        GpxLayer gpx = GPXChooser.topLayer();
        if (gpx == null)
            return;
        EastNorth eastNorth = MainApplication.getMap().mapView.getEastNorth(x, y);
        double tolerance = eastNorth.distance(MainApplication.getMap().mapView.getEastNorth(x + 300, y));
        WayPoint wp = gpx.data.nearestPointOnTrack(eastNorth, tolerance);
        if (wp == null)
            return;
        long newTime = Correlator.getGpxDate(gpx.data, wp.getCoor());
        if (newTime <= 0)
            return;
        Correlator.revertPos(log);
        Correlator.correlate(log, gpx.data, log.get(entry).getTime().getTime() - newTime);
        MainApplication.getMap().mapView.repaint();
    }

    private class CorrelateEntries extends JosmAction {
        private boolean toZero;

        CorrelateEntries(boolean toZero) {
            super(toZero ? tr("Correlate with GPX...") : tr("Put on GPX..."), "nanolog/correlate",
                    tr("Correlate entries with GPS trace"), null, false);
            this.toZero = toZero;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // 1. Select GPX trace or display message to load one
            // (better yet, disable when no GPX traces)
            GpxLayer layer = GPXChooser.chooseLayer();
            // 2. Correlate by default, sticking by date
            // (if does not match, shift so hours-minutes stay)
            if (layer != null) {
                long offset = toZero ? 0 : Correlator.crudeMatch(log, layer.data);
                Correlator.revertPos(log);
                Correlator.correlate(log, layer.data, offset);
                fireMarkersChanged();
                MainApplication.getMap().mapView.repaint();
            }
            // 3. Show non-modal (?) window with a slider and a text input
            // (todo: better slider, like in blender)
        }
    }

    private static class SaveLayer extends JosmAction {

        SaveLayer() {
            super(tr("Save layer..."), "nanolog/save", tr("Save NanoLog layer"), null, false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // todo
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), "Sorry, no saving yet", "NanoLog", JOptionPane.ERROR_MESSAGE);
        }
    }

    public interface NanoLogLayerListener {
        void markersUpdated(NanoLogLayer layer);

        void markerActivated(NanoLogLayer layer, NanoLogEntry entry);
    }
}
