import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.MergeNodesAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSource;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
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
    public WayDownloaderPlugin() {
        //add WayDownloadAction to tools menu
        MainMenu.add(Main.main.menu.toolsMenu, new WayDownloadAction());
    }

    private class WayDownloadAction extends JosmAction implements Runnable {

        /** Set up the action (text appearing on the menu, keyboard shortcut etc */
        public WayDownloadAction() {

            super( "Way Download" ,
                    "way-download",
                    "Download map data on the end of selected way",
                    Shortcut.registerShortcut("waydownloader:waydownload", "Way Download", KeyEvent.VK_W, Shortcut.GROUP_MENU, Shortcut.SHIFT_DEFAULT),
                    true);
        }

        /** Called when the WayDownloadAction action is triggered (e.g. user clicked the menu option) */
        public void actionPerformed(ActionEvent e) {

            System.out.println("Way Download");

            String errMsg = null;

            selectedNode = null;
            Collection<OsmPrimitive> selection = Main.ds.getSelectedNodes();

            if (selection.size()==0) {
                selection = Main.ds.getSelectedWays();
                if (!workFromWaySelection(selection)) {
                    errMsg = tr("Select a starting node on the end of a way");
                }
                selection = Main.ds.getSelectedNodes();
            }

            if ( selection.size()==0 || selection.size()>1 ) {
                errMsg = tr("Select a starting node on the end of a way");
            } else {
                OsmPrimitive p = selection.iterator().next();



                if (!(p instanceof Node)) {
                    errMsg = tr("Select a starting node on the end of a way");
                } else {
                    selectedNode = (Node) p;


                    Main.map.mapView.zoomTo(selectedNode.getEastNorth());

                    //Before downloading. Figure a few things out.
                    //Find connected way
                    ArrayList<Way> connectedWays = findConnectedWays();

                    if (connectedWays.size()==0) {
                        errMsg = tr("Select a starting node on the end of a way");
                    } else {
                        priorConnectedWay =(Way) connectedWays.get(0);

                        //Download a little rectangle around the selected node
                        double latbuffer=0.0003; //TODO make this an option
                        double lonbuffer=0.0005;
                        DownloadOsmTask downloadTask = new DownloadOsmTask();
                        downloadTask.download( null,
                                               selectedNode.getCoor().lat()-latbuffer,
                                               selectedNode.getCoor().lon()-lonbuffer,
                                               selectedNode.getCoor().lat()+latbuffer,
                                               selectedNode.getCoor().lon()+lonbuffer);

                        //The download is scheduled to be executed.
                        //Now schedule the run() method (below) to be executed once that's completed.
                        Main.worker.execute(this);
                    }
                }
            }

            if(errMsg != null)
                JOptionPane.showMessageDialog(Main.parent, errMsg);
        }

        /**
         * Logic to excute after the download has happened
         */
        public void run() {
            //Find ways connected to the node after the download
            ArrayList<Way> connectedWays = findConnectedWays();

            String errMsg = null;
            if (connectedWays.size()==0) {
                throw new RuntimeException("Way downloader data inconsistency. priorConnectedWay (" +
                        priorConnectedWay.toString() + ") wasn't discovered after download");

            } else if (connectedWays.size()==1) {
                //Just one way connecting the node still. Presumably the one which was there before

                //Check if it's just a duplicate node
                Node dupeNode = duplicateNode();
                if (dupeNode!=null) {

                    if (JOptionPane.showConfirmDialog(null, "Merge duplicate node?")==JOptionPane.YES_OPTION) {
                        LinkedList<Node> dupeNodes = new LinkedList<Node>();
                        dupeNodes.add(dupeNode);
                        MergeNodesAction.mergeNodes(dupeNodes, selectedNode);

                        connectedWays = findConnectedWays(); //Carry on
                    }


                } else {
                    errMsg = tr("Reached the end of the line");
                }

            }

            if (connectedWays.size()>2) {
                //Three or more ways meeting at this node. Means we have a junction.
                errMsg = tr("Reached a junction");

            } else if (connectedWays.size()==2) {
                //Two connected ways (The "normal" way downloading case)
                //Figure out which of the two is new.
                System.out.println("connectedWays.toString()=" + connectedWays.toString());
                Way wayA = (Way) connectedWays.get(0);
                Way wayB = (Way) connectedWays.get(1);
                Way nextWay = wayA;
                if (priorConnectedWay.equals(wayA)) nextWay = wayB;

                Node nextNode = findOtherEnd(nextWay, selectedNode);

                //Select the next node
                Main.ds.setSelected(nextNode);

                Main.map.mapView.zoomTo(nextNode.getEastNorth());
            }
            if(errMsg != null)
                JOptionPane.showMessageDialog(Main.parent, errMsg);
        }
    }

    /** See if there's another node at the same coordinates. If so return it. Otherwise null */
    private Node duplicateNode() {
        Iterator nodesIter = Main.ds.nodes.iterator();
        while (nodesIter.hasNext()) {
            Node onNode = (Node) nodesIter.next();
            if (!onNode.equals(this.selectedNode)
                    && onNode.getCoor().lat()==selectedNode.getCoor().lat()
                    && onNode.getCoor().lon()==selectedNode.getCoor().lon()) {
                return onNode;
            }
        }
        return null;
    }

    /** Given the the node on one end of the way, return the node on the other end */
    private Node findOtherEnd(Way way, Node firstEnd) {
        Node otherEnd = way.nodes.get(0);
        if (otherEnd.equals(firstEnd)) otherEnd = way.nodes.get(way.nodes.size()-1);
        return otherEnd;
    }

    /** find set of ways which have an end on the selectedNode */
    private ArrayList<Way> findConnectedWays() {
        ArrayList<Way> connectedWays = new ArrayList<Way>();

        //loop through every way
        Iterator waysIter = Main.ds.ways.iterator();
        while (waysIter.hasNext()) {
            Way onWay = (Way) waysIter.next();


            Object[] nodes = onWay.nodes.toArray();
            if (nodes.length<2) {
                //Should never happen should it? TODO: investigate. For the moment ignore these
                System.err.println("WayDownloader plugin encountered a way with " + nodes.length + " nodes :" + onWay.toString());
            } else {
                Node firstNode = (Node) nodes[0];
                Node lastNode = (Node) nodes[nodes.length-1];

                if (firstNode.equals(selectedNode) || lastNode.equals(selectedNode)) {
                    //Found it
                    connectedWays.add(onWay);
                }
            }
        }
        return connectedWays;
    }

    /**
     * given a selected way, select a node on the end of the way which is not in a downloaded area
     * return true if this worked
     */
    private boolean workFromWaySelection(Collection<OsmPrimitive> selection) {

        if (selection.size()>1) {
            //more than one way selected
            return false;
        } else {
            Way selectedWay = (Way) selection.toArray()[0];
            selectedNode = (Node) selectedWay.nodes.get(0);

            if (isDownloaded(selectedNode)) {
                selectedNode = findOtherEnd(selectedWay, selectedNode);

                if (isDownloaded(selectedNode)) return false;
            }
        }
        Main.ds.setSelected(selectedNode);
        return true;
    }

    private boolean isDownloaded(Node node) {
        Iterator downloadedAreasIter = Main.ds.dataSources.iterator();
        while (downloadedAreasIter.hasNext()) {
            DataSource datasource = (DataSource) downloadedAreasIter.next();
            Bounds bounds = datasource.bounds;

            if (node.getCoor().lat()>bounds.min.lat() &&
                node.getCoor().lat()<bounds.max.lat() &&
                node.getCoor().lon()>bounds.min.lon() &&
                node.getCoor().lon()<bounds.max.lon()) {
                return true;
            }
        }
        return false;
    }
}
