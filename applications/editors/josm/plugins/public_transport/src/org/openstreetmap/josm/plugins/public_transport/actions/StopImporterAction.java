// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.osm.visitor.PrimitiveVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.io.GpxReader;
import org.openstreetmap.josm.plugins.public_transport.commands.SettingsStoptypeCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.TrackStoplistAddCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.TrackStoplistDeleteCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.TrackStoplistDetachCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.TrackStoplistRelocateCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.TrackStoplistSortCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.TrackSuggestStopsCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.WaypointsDetachCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.WaypointsDisableCommand;
import org.openstreetmap.josm.plugins.public_transport.commands.WaypointsEnableCommand;
import org.openstreetmap.josm.plugins.public_transport.dialogs.StopImporterDialog;
import org.openstreetmap.josm.plugins.public_transport.models.WaypointTableModel;
import org.openstreetmap.josm.plugins.public_transport.refs.TrackReference;
import org.openstreetmap.josm.spi.preferences.Config;
import org.xml.sax.SAXException;

/**
 * Create Stops from GPX
 */
public class StopImporterAction extends JosmAction {
    private static StopImporterDialog dialog = null;

    private static DefaultListModel<TrackReference> tracksListModel = null;

    private static GpxData data = null;

    private static TrackReference currentTrack = null;

    private static WaypointTableModel waypointTM = null;

    public boolean inEvent = false;

    /**
     * Constructs a new {@code StopImporterAction}.
     */
    public StopImporterAction() {
        super(tr("Create Stops from GPX ..."), null, tr("Create Stops from a GPX file"), null,
                false);
        putValue("toolbar", "publictransport/stopimporter");
        MainApplication.getToolbar().register(this);
    }

    public WaypointTableModel getWaypointTableModel() {
        return waypointTM;
    }

    public StopImporterDialog getDialog() {
        return dialog;
    }

    public DefaultListModel<TrackReference> getTracksListModel() {
        if (tracksListModel == null)
            tracksListModel = new DefaultListModel<>();
        return tracksListModel;
    }

    public TrackReference getCurrentTrack() {
        return currentTrack;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (dialog == null)
            dialog = new StopImporterDialog(this);

        dialog.setVisible(true);

        if (tr("Create Stops from GPX ...").equals(event.getActionCommand())) {
            String curDir = Config.getPref().get("lastDirectory");
            if (curDir.equals("")) {
                curDir = ".";
            }
            JFileChooser fc = new JFileChooser(new File(curDir));
            fc.setDialogTitle(tr("Select GPX file"));
            fc.setMultiSelectionEnabled(false);

            int answer = fc.showOpenDialog(MainApplication.getMainFrame());
            if (answer != JFileChooser.APPROVE_OPTION)
                return;

            if (!fc.getCurrentDirectory().getAbsolutePath().equals(curDir))
                Config.getPref().put("lastDirectory", fc.getCurrentDirectory().getAbsolutePath());

            importData(fc.getSelectedFile());

            refreshData();
        } else if ("stopImporter.settingsGPSTimeStart".equals(event.getActionCommand())) {
            if (!inEvent && dialog.gpsTimeStartValid() && currentTrack != null)
                UndoRedoHandler.getInstance().add(new TrackStoplistRelocateCommand(this));
        } else if ("stopImporter.settingsStopwatchStart".equals(event.getActionCommand())) {
            if (!inEvent && dialog.stopwatchStartValid() && currentTrack != null)
                UndoRedoHandler.getInstance().add(new TrackStoplistRelocateCommand(this));
        } else if ("stopImporter.settingsTimeWindow".equals(event.getActionCommand())) {
            if (currentTrack != null)
                currentTrack.timeWindow = dialog.getTimeWindow();
        } else if ("stopImporter.settingsThreshold".equals(event.getActionCommand())) {
            if (currentTrack != null)
                currentTrack.threshold = dialog.getThreshold();
        } else if ("stopImporter.settingsSuggestStops".equals(event.getActionCommand()))
            UndoRedoHandler.getInstance().add(new TrackSuggestStopsCommand(this));
        else if ("stopImporter.stoplistFind".equals(event.getActionCommand()))
            findNodesInTable(dialog.getStoplistTable(), currentTrack.stoplistTM.getNodes());
        else if ("stopImporter.stoplistShow".equals(event.getActionCommand()))
            showNodesFromTable(dialog.getStoplistTable(), currentTrack.stoplistTM.getNodes());
        else if ("stopImporter.stoplistMark".equals(event.getActionCommand()))
            markNodesFromTable(dialog.getStoplistTable(), currentTrack.stoplistTM.getNodes());
        else if ("stopImporter.stoplistDetach".equals(event.getActionCommand())) {
            UndoRedoHandler.getInstance().add(new TrackStoplistDetachCommand(this));
            dialog.getStoplistTable().clearSelection();
        } else if ("stopImporter.stoplistAdd".equals(event.getActionCommand()))
            UndoRedoHandler.getInstance().add(new TrackStoplistAddCommand(this));
        else if ("stopImporter.stoplistDelete".equals(event.getActionCommand()))
            UndoRedoHandler.getInstance().add(new TrackStoplistDeleteCommand(this));
        else if ("stopImporter.stoplistSort".equals(event.getActionCommand()))
            UndoRedoHandler.getInstance().add(new TrackStoplistSortCommand(this));
        else if ("stopImporter.waypointsFind".equals(event.getActionCommand()))
            findNodesInTable(dialog.getWaypointsTable(), waypointTM.nodes);
        else if ("stopImporter.waypointsShow".equals(event.getActionCommand()))
            showNodesFromTable(dialog.getWaypointsTable(), waypointTM.nodes);
        else if ("stopImporter.waypointsMark".equals(event.getActionCommand()))
            markNodesFromTable(dialog.getWaypointsTable(), waypointTM.nodes);
        else if ("stopImporter.waypointsDetach".equals(event.getActionCommand())) {
            UndoRedoHandler.getInstance().add(new WaypointsDetachCommand(this));
            dialog.getWaypointsTable().clearSelection();
        } else if ("stopImporter.waypointsAdd".equals(event.getActionCommand()))
            UndoRedoHandler.getInstance().add(new WaypointsEnableCommand(this));
        else if ("stopImporter.waypointsDelete".equals(event.getActionCommand()))
            UndoRedoHandler.getInstance().add(new WaypointsDisableCommand(this));
        else if ("stopImporter.settingsStoptype".equals(event.getActionCommand()))
            UndoRedoHandler.getInstance().add(new SettingsStoptypeCommand(this));
    }

    private void importData(final File file) {
        try {
            InputStream is;
            if (file.getName().endsWith(".gpx.gz"))
                is = new GZIPInputStream(new FileInputStream(file));
            else
                is = new FileInputStream(file);
            // Workaround for SAX BOM bug
            // https://bugs.openjdk.java.net/browse/JDK-6206835
            if (!((is.read() == 0xef) && (is.read() == 0xbb) && (is.read() == 0xbf))) {
                is.close();
                if (file.getName().endsWith(".gpx.gz"))
                    is = new GZIPInputStream(new FileInputStream(file));
                else
                    is = new FileInputStream(file);
            }
            final GpxReader r = new GpxReader(is);
            final boolean parsedProperly = r.parse(true);
            data = r.getGpxData();

            if (!parsedProperly) {
                JOptionPane.showMessageDialog(null,
                        tr("Error occurred while parsing gpx file {0}. Only a part of the file will be available.",
                                file.getName()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, tr("File \"{0}\" does not exist", file.getName()));
        } catch (SAXException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, tr("Parsing file \"{0}\" failed", file.getName()));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, tr("IOException \"{0}\" occurred", e.toString()));
        }
    }

    private void refreshData() {
        tracksListModel.clear();
        if (data != null) {
            Vector<TrackReference> trackRefs = new Vector<>();
            Iterator<GpxTrack> trackIter = data.tracks.iterator();
            while (trackIter.hasNext()) {
                GpxTrack track = trackIter.next();
                trackRefs.add(new TrackReference(track, this));
            }

            Collections.sort(trackRefs);

            Iterator<TrackReference> iter = trackRefs.iterator();
            while (iter.hasNext()) {
                tracksListModel.addElement(iter.next());
            }

            waypointTM = new WaypointTableModel(this);
            Iterator<WayPoint> waypointIter = data.waypoints.iterator();
            while (waypointIter.hasNext()) {
                WayPoint waypoint = waypointIter.next();
                waypointTM.addRow(waypoint);
            }
            dialog.setWaypointsTableModel(waypointTM);
        } else {
            JOptionPane.showMessageDialog(null,
                    tr("The GPX file contained no tracks or waypoints."), tr("No data found"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void tracksSelectionChanged(int selectedPos) {
        if (selectedPos >= 0) {
            currentTrack = tracksListModel.elementAt(selectedPos);
            dialog.setTrackValid(true);

            // Prepare Settings
            dialog.setSettings(currentTrack.gpsSyncTime, currentTrack.stopwatchStart,
                    currentTrack.timeWindow, currentTrack.threshold);

            // Prepare Stoplist
            dialog.setStoplistTableModel(tracksListModel.elementAt(selectedPos).stoplistTM);
        } else {
            currentTrack = null;
            dialog.setTrackValid(false);
        }
    }

    public Node createNode(LatLon latLon, String name) {
        return createNode(latLon, dialog.getStoptype(), name);
    }

    public static Node createNode(LatLon latLon, String type, String name) {
        Node node = new Node(latLon);
        setTagsWrtType(node, type);
        node.put("name", name);
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        if (ds == null) {
            JOptionPane.showMessageDialog(null,
                    tr("There exists no dataset."
                            + " Try to download data from the server or open an OSM file."),
                    tr("No data found"), JOptionPane.ERROR_MESSAGE);

            return null;
        }
        ds.addPrimitive(node);
        return node;
    }

    /**
     * sets the tags of the node according to the type
     * @param node node
     * @param type type
     */
    public static void setTagsWrtType(Node node, String type) {
        node.remove("highway");
        node.remove("railway");
        if ("bus".equals(type))
            node.put("highway", "bus_stop");
        else if ("tram".equals(type))
            node.put("railway", "tram_stop");
        else if ("light_rail".equals(type))
            node.put("railway", "station");
        else if ("subway".equals(type))
            node.put("railway", "station");
        else if ("rail".equals(type))
            node.put("railway", "station");
    }

    /**
     * returns a collection of all selected lines or a collection of all lines otherwise
     * @param table table
     * @return all selected lines or a collection of all lines otherwise
     */
    public static Vector<Integer> getConsideredLines(JTable table) {
        int[] selectedLines = table.getSelectedRows();
        Vector<Integer> consideredLines = new Vector<>();
        if (selectedLines.length > 0) {
            for (int i = 0; i < selectedLines.length; ++i) {
                consideredLines.add(selectedLines[i]);
            }
        } else {
            for (int i = 0; i < table.getRowCount(); ++i) {
                consideredLines.add(Integer.valueOf(i));
            }
        }
        return consideredLines;
    }

    /**
     * marks the table items whose nodes are marked on the map
     * @param table table
     * @param nodes nodes
     */
    public static void findNodesInTable(JTable table, Vector<Node> nodes) {
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        if (ds == null)
            return;

        table.clearSelection();

        for (int i = 0; i < table.getRowCount(); ++i) {
            if (nodes.elementAt(i) != null && ds.isSelected(nodes.elementAt(i)))
                table.addRowSelectionInterval(i, i);
        }
    }

    /**
     * shows the nodes that correspond to the marked lines in the table. If no lines are marked in the table, show all nodes from the vector
     * @param table table
     * @param nodes nodes
     */
    public static void showNodesFromTable(JTable table, Vector<Node> nodes) {
        BoundingXYVisitor box = new BoundingXYVisitor();
        Vector<Integer> consideredLines = getConsideredLines(table);
        for (int i = 0; i < consideredLines.size(); ++i) {
            int j = consideredLines.elementAt(i);
            if (nodes.elementAt(j) != null)
                nodes.elementAt(j).accept((PrimitiveVisitor) box);
        }
        if (box.getBounds() == null)
            return;
        box.enlargeBoundingBox();
        MainApplication.getMap().mapView.zoomTo(box);
    }

    /**
     * marks the nodes that correspond to the marked lines in the table. If no lines are marked in the table, mark all nodes from the vector
     * @param table table
     * @param nodes nodes
     */
    public static void markNodesFromTable(JTable table, Vector<Node> nodes) {
        OsmPrimitive[] osmp = {null};
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        ds.setSelected(osmp);
        Vector<Integer> consideredLines = getConsideredLines(table);
        for (int i = 0; i < consideredLines.size(); ++i) {
            int j = consideredLines.elementAt(i);
            if (nodes.elementAt(j) != null)
                ds.addSelected(nodes.elementAt(j));
        }
    }

    public static String timeOf(double t) {
        t -= Math.floor(t / 24 / 60 / 60) * 24 * 60 * 60;

        int hour = (int) Math.floor(t / 60 / 60);
        t -= Math.floor(t / 60 / 60) * 60 * 60;
        int minute = (int) Math.floor(t / 60);
        t -= Math.floor(t / 60) * 60;
        double second = t;

        Format format = new DecimalFormat("00");
        Format formatS = new DecimalFormat("00.###");
        return (format.format(hour) + ":" + format.format(minute) + ":" + formatS.format(second));
    }

    public Action getFocusWaypointNameAction() {
        return new FocusWaypointNameAction();
    }

    public Action getFocusWaypointShelterAction(String shelter) {
        return new FocusWaypointShelterAction(shelter);
    }

    public Action getFocusWaypointDeleteAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = dialog.getWaypointsTable();
                int row = table.getEditingRow();
                if (row < 0)
                    return;
                table.clearSelection();
                table.addRowSelectionInterval(row, row);
                UndoRedoHandler.getInstance().add(new WaypointsDisableCommand(StopImporterAction.this));
            }
        };
    }

    public Action getFocusTrackStoplistNameAction() {
        return new FocusTrackStoplistNameAction();
    }

    public Action getFocusTrackStoplistShelterAction(String shelter) {
        return new FocusTrackStoplistShelterAction(shelter);
    }

    public Action getFocusStoplistDeleteAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = dialog.getStoplistTable();
                int row = table.getEditingRow();
                if (row < 0)
                    return;
                table.clearSelection();
                table.addRowSelectionInterval(row, row);
                UndoRedoHandler.getInstance().add(new TrackStoplistDeleteCommand(StopImporterAction.this));
            }
        };
    }

    private static class FocusWaypointNameAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table = dialog.getWaypointsTable();
            showNodesFromTable(table, waypointTM.nodes);
            markNodesFromTable(table, waypointTM.nodes);
            int row = table.getEditingRow();
            if (row < 0)
                row = 0;
            waypointTM.inEvent = true;
            if (table.getCellEditor() != null) {
                if (!table.getCellEditor().stopCellEditing())
                    table.getCellEditor().cancelCellEditing();
            }
            table.editCellAt(row, 1);
            table.getCellEditor().getTableCellEditorComponent(table, "", true, row, 1);
            waypointTM.inEvent = false;
        }
    }

    private static class FocusWaypointShelterAction extends AbstractAction {
        private String defaultShelter = null;

        FocusWaypointShelterAction(String defaultShelter) {
            this.defaultShelter = defaultShelter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table = dialog.getWaypointsTable();
            showNodesFromTable(table, waypointTM.nodes);
            markNodesFromTable(table, waypointTM.nodes);
            int row = table.getEditingRow();
            if (row < 0)
                row = 0;
            waypointTM.inEvent = true;
            if (table.getCellEditor() != null) {
                if (!table.getCellEditor().stopCellEditing())
                    table.getCellEditor().cancelCellEditing();
            }
            table.editCellAt(row, 2);
            waypointTM.inEvent = false;
            table.getCellEditor().getTableCellEditorComponent(table, defaultShelter, true, row, 2);
        }
    }

    private static class FocusTrackStoplistNameAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table = dialog.getStoplistTable();
            showNodesFromTable(table, currentTrack.stoplistTM.getNodes());
            markNodesFromTable(table, currentTrack.stoplistTM.getNodes());
            int row = table.getEditingRow();
            if (row < 0)
                row = 0;
            currentTrack.inEvent = true;
            if (table.getCellEditor() != null) {
                if (!table.getCellEditor().stopCellEditing())
                    table.getCellEditor().cancelCellEditing();
            }
            table.editCellAt(row, 1);
            table.getCellEditor().getTableCellEditorComponent(table, "", true, row, 1);
            currentTrack.inEvent = false;
        }
    }

    private static class FocusTrackStoplistShelterAction extends AbstractAction {
        private String defaultShelter = null;

        FocusTrackStoplistShelterAction(String defaultShelter) {
            this.defaultShelter = defaultShelter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table = dialog.getStoplistTable();
            showNodesFromTable(table, currentTrack.stoplistTM.getNodes());
            markNodesFromTable(table, currentTrack.stoplistTM.getNodes());
            int row = table.getEditingRow();
            if (row < 0)
                row = 0;
            currentTrack.inEvent = true;
            if (table.getCellEditor() != null) {
                if (!table.getCellEditor().stopCellEditing())
                    table.getCellEditor().cancelCellEditing();
            }
            table.editCellAt(row, 2);
            currentTrack.inEvent = false;
            table.getCellEditor().getTableCellEditorComponent(table, defaultShelter, true, row, 2);
        }
    }
}
