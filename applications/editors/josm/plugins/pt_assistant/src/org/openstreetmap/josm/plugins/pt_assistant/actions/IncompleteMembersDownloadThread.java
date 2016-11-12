// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.io.DownloadPrimitivesWithReferrersTask;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Thread that downloads incomplete relation members while pausing the rest of testing
 * @author darya
 *
 */
public class IncompleteMembersDownloadThread extends Thread {

    /**
     * Default constructor
     */
    public IncompleteMembersDownloadThread() {
        super();

    }

    @Override
    public void run() {

        try {
            synchronized (this) {

                ArrayList<PrimitiveId> list = new ArrayList<>();

                // if there are selected routes, try adding them first:
                for (Relation currentSelectedRelation : Main.getLayerManager().getEditDataSet()
                        .getSelectedRelations()) {
                    if (RouteUtils.isTwoDirectionRoute(currentSelectedRelation)) {
                        list.add(currentSelectedRelation);
                    }
                }

                if (list.isEmpty()) {
                    // add all route relations that are of public_transport
                    // version 2:
                    Collection<Relation> allRelations = Main.getLayerManager().getEditDataSet().getRelations();
                    for (Relation currentRelation : allRelations) {
                        if (RouteUtils.isTwoDirectionRoute(currentRelation)) {
                            list.add(currentRelation);
                        }
                    }
                }

                // add all stop_positions:
                Collection<Node> allNodes = Main.getLayerManager().getEditDataSet().getNodes();
                for (Node currentNode : allNodes) {
                    if (currentNode.hasTag("public_transport", "stop_position")) {
                        List<OsmPrimitive> referrers = currentNode.getReferrers();
                        boolean parentWayExists = false;
                        for (OsmPrimitive referrer : referrers) {
                            if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
                                parentWayExists = true;
                                break;
                            }
                        }
                        if (!parentWayExists) {
                            list.add(currentNode);

                        }

                    }
                }

                DownloadPrimitivesWithReferrersTask task = new DownloadPrimitivesWithReferrersTask(false, list, false,
                        true, null, null);
                Thread t = new Thread(task);
                t.start();
                t.join();

            }

        } catch (InterruptedException e) {
            // do nothing in case the download was interrupted
            Main.trace(e);
        }

    }
}
