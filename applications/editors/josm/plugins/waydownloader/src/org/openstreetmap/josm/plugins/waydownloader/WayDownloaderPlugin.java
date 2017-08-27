// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.waydownloader;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.MergeNodesAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Plugin class for the Way Downloader plugin
 *
 * @author Harry Wood
 */
public class WayDownloaderPlugin extends Plugin {

    private Way priorConnectedWay = null;
    private Node selectedNode = null;

    /** Plugin constructor called at JOSM startup */
    public WayDownloaderPlugin(PluginInformation info) {
        super(info);
        //add WayDownloadAction to tools menu
        MainMenu.add(MainApplication.getMenu().moreToolsMenu, new WayDownloadAction());
    }

    private class WayDownloadAction extends JosmAction implements Runnable {

        /** Set up the action (text appearing on the menu, keyboard shortcut etc */
        public WayDownloadAction() {
            super( tr("Way Download") ,
                    "way-download",
                    tr("Download map data on the end of selected way"),
                    Shortcut.registerShortcut("waydownloader:waydownload", tr("Way Download"), KeyEvent.VK_W, Shortcut.CTRL_SHIFT),
                    true);
        }

        /** Called when the WayDownloadAction action is triggered (e.g. user clicked the menu option) */
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode = null;
            DataSet ds = MainApplication.getLayerManager().getEditDataSet();
            Collection<Node> selection = ds.getSelectedNodes();
            if (selection.isEmpty()) {
                Collection<Way> selWays = ds.getSelectedWays();
                if (!workFromWaySelection(selWays)) {
                    showWarningMessage(tr("<html>Neither a node nor a way with an endpoint outside of the<br>current download areas is selected.<br>Select a node on the start or end of a way or an entire way first.</html>"));
                    return;
                }
                selection = ds.getSelectedNodes();
            }

            if ( selection.isEmpty() || selection.size()>1 || ! (selection.iterator().next() instanceof Node)) {
                showWarningMessage(tr("<html>Could not find a unique node to start downloading from.</html>"));
                return;
            }

            selectedNode = (Node) selection.iterator().next();
            MainApplication.getMap().mapView.zoomTo(selectedNode.getEastNorth());

            //Before downloading. Figure a few things out.
            //Find connected way
            List<Way> connectedWays = findConnectedWays(selectedNode);
            if (connectedWays.isEmpty()) {
                showWarningMessage(
                        tr("<html>There are no ways connected to node ''{0}''. Aborting.</html>",
                        selectedNode.getDisplayName(DefaultNameFormatter.getInstance()))
                );
                return;
            }
            priorConnectedWay = connectedWays.get(0);

            //Download a little rectangle around the selected node
            double latbuffer = Main.pref.getDouble("waydownloader.latbuffer", 0.00001);
            double lonbuffer = Main.pref.getDouble("waydownloader.latbuffer", 0.00002);
            DownloadOsmTask downloadTask = new DownloadOsmTask();
            final PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor();
            LatLon ll = selectedNode.getCoor();
            final Future<?> future = downloadTask.download(
                    false /* no new layer */,
                    new Bounds(
                            ll.lat()- latbuffer,
                            ll.lon()- lonbuffer,
                            ll.lat()+ latbuffer,
                            ll.lon()+ lonbuffer
                    ),
                    monitor
            );
            // schedule closing of the progress monitor after the download
            // job has finished
            MainApplication.worker.submit(
                    () -> {
					    try {
					        future.get();
					    } catch(Exception e1) {
					        Logging.error(e1);
					        return;
					    }
					    monitor.close();
					}
            );
            //The download is scheduled to be executed.
            //Now schedule the run() method (below) to be executed once that's completed.
            MainApplication.worker.execute(this);
        }

        /**
         * Logic to excute after the download has happened
         */
        @Override
        public void run() {
            //Find ways connected to the node after the download
            List<Way> connectedWays = findConnectedWays(selectedNode);

            if (connectedWays.isEmpty()) {
                String msg = tr("Way downloader data inconsistency. Prior connected way ''{0}'' wasn''t discovered after download",
                                priorConnectedWay.getDisplayName(DefaultNameFormatter.getInstance())
                        );
                showErrorMessage(msg);
                return;
            }

            if (connectedWays.size()==1) {
                //Just one way connecting still to the node . Presumably the one which was there before
                //Check if it's just a duplicate node

                Node dupeNode = findDuplicateNode(selectedNode);
                if (dupeNode!=null) {
                    String msg = tr("<html>There aren''t further connected ways to download.<br>"
                            + "A potential duplicate node of the currently selected node was found, though.<br><br>"
                            + "The currently selected node is ''{0}''<br>"
                            + "The potential duplicate node is ''{1}''<br>"
                            + "Merge the duplicate node onto the currently selected node and continue way downloading?"
                            + "</html>",
                            selectedNode.getDisplayName(DefaultNameFormatter.getInstance()),
                            dupeNode.getDisplayName(DefaultNameFormatter.getInstance())
                    );

                    int ret = JOptionPane.showConfirmDialog(
                            Main.parent,
                            msg,
                            tr("Merge duplicate node?"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (ret != JOptionPane.YES_OPTION)
                        return;
                    Command cmd = MergeNodesAction.mergeNodes(
                    		MainApplication.getLayerManager().getEditLayer(),
                            Collections.singletonList(dupeNode),
                            selectedNode
                    );
                    if (cmd != null) {
                    	MainApplication.undoRedo.add(cmd);
                        MainApplication.getLayerManager().getEditLayer().data.setSelected(selectedNode);
                    }
                    connectedWays = findConnectedWays(selectedNode);
                } else {
                    showInfoMessage(tr("<html>No more connected ways to download.</html>"));
                    return;
                }
                return;
            }

            if (connectedWays.size()>2) {
                //Three or more ways meeting at this node. Means we have a junction.
                String msg = tr(
                        "Node ''{0}'' is a junction with more than 2 connected ways.",
                        selectedNode.getDisplayName(DefaultNameFormatter.getInstance())
                );
                showWarningMessage(msg);
                return;
            }

            if (connectedWays.size()==2) {
                //Two connected ways (The "normal" way downloading case)
                //Figure out which of the two is new.
                Way wayA = connectedWays.get(0);
                Way wayB = connectedWays.get(1);
                Way nextWay = wayA;
                if (priorConnectedWay.equals(wayA)) nextWay = wayB;

                Node nextNode = findOtherEnd(nextWay, selectedNode);

                //Select the next node
                MainApplication.getLayerManager().getEditDataSet().setSelected(nextNode);
                MainApplication.getMap().mapView.zoomTo(nextNode.getEastNorth());
            }
        }

        @Override
        protected void updateEnabledState() {
            setEnabled(getLayerManager().getEditLayer() != null);
        }

        @Override
        protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
            // do nothing
        }
    }

    /**
     * Check whether there is a potentially duplicate node for <code>referenceNode</code>.
     *
     * @param referenceNode the reference node
     * @return the potential duplicate node. null, if no duplicate found.
     */
    private Node findDuplicateNode(Node referenceNode) {
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        List<Node> candidates = ds.searchNodes(new Bounds(referenceNode.getCoor(), 0.0003, 0.0005).toBBox());
        for (Node candidate: candidates) {
            if (!candidate.equals(referenceNode)
                    && !candidate.isIncomplete()
                    && candidate.getCoor().equals(referenceNode.getCoor()))
                return candidate;
        }
        return null;
    }

    /** Given the node on one end of the way, return the node on the other end */
    private Node findOtherEnd(Way way, Node firstEnd) {
        Node otherEnd = way.firstNode();
        if (otherEnd.equals(firstEnd)) otherEnd = way.lastNode();
        return otherEnd;
    }

    /**
     * Replies the list of ways <code>referenceNode</code> is either the first or the
     * last node in.
     *
     * @param referenceNode the reference node
     * @return the list of ways. May be empty, but null.
     */
    private List<Way> findConnectedWays(Node referenceNode) {
        List<Way> referers = OsmPrimitive.getFilteredList(referenceNode.getReferrers(), Way.class);
        ArrayList<Way> connectedWays = new ArrayList<>(referers.size());

        //loop through referers
        for (Way way: referers) {
            if (way.getNodesCount() >= 2 && way.isFirstLastNode(referenceNode)) {
                    connectedWays.add(way);
            }
        }
        return connectedWays;
    }

    /**
     * given a selected way, select a node on the end of the way which is not in a downloaded area
     * return true if this worked
     */
    private boolean workFromWaySelection(Collection<? extends OsmPrimitive> selection) {
        if (selection.size() != 1)
            return false;
        Way selectedWay = (Way) selection.iterator().next();
        selectedNode = selectedWay.firstNode();

        if (isDownloaded(selectedNode)) {
            selectedNode = selectedWay.lastNode();

            if (isDownloaded(selectedNode)) return false;
        }
        MainApplication.getLayerManager().getEditDataSet().setSelected(selectedNode);
        return true;
    }

    private boolean isDownloaded(Node node) {
        for (DataSource datasource : MainApplication.getLayerManager().getEditDataSet().getDataSources()) {
            Bounds bounds = datasource.bounds;
            if (bounds != null && bounds.contains(node.getCoor())) return true;
        }
        return false;
    }

    private static void showWarningMessage(final String msg) {
        if (msg != null) {
            Logging.warn(msg.replace("<html>", "").replace("</html>", ""));
            GuiHelper.runInEDT(new Runnable() {
                @Override
                public void run() {
                    new Notification(msg)
                    .setIcon(JOptionPane.WARNING_MESSAGE)
                    .show();
                }
            });
        }
    }

    private static void showErrorMessage(final String msg) {
        if (msg != null) {
            Logging.error(msg.replace("<html>", "").replace("</html>", ""));
            GuiHelper.runInEDT(new Runnable() {
                @Override
                public void run() {
                    new Notification(msg)
                    .setIcon(JOptionPane.ERROR_MESSAGE)
                    .show();
                }
            });
        }
    }

    private static void showInfoMessage(final String msg) {
        if (msg != null) {
            Logging.info(msg.replace("<html>", "").replace("</html>", ""));
            GuiHelper.runInEDT(new Runnable() {
                @Override
                public void run() {
                    new Notification(msg)
                    .setIcon(JOptionPane.INFORMATION_MESSAGE)
                    .setDuration(Notification.TIME_SHORT)
                    .show();
                }
            });
        }
    }
}
