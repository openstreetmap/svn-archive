// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.io.DownloadPrimitivesWithReferrersTask;

public class DownloadReferrersThread extends Thread {

    private Node node;

    public DownloadReferrersThread(Node node) {
        super();
        this.node = node;

    }

    @Override
    public void run() {

        synchronized (this) {

            Collection<Node> allNodes = node.getDataSet().getNodes();
            List<PrimitiveId> nodesToBeDownloaded = new ArrayList<>();
            for (Node currNode : allNodes) {
                if (currNode.hasTag("public_transport", "stop_position") || currNode.hasTag("highway", "bus_stop")
                        || currNode.hasTag("public_transport", "platform") || currNode.hasTag("highway", "platform")
                        || currNode.hasTag("railway", "platform")) {
                    nodesToBeDownloaded.add(currNode);
                }
            }

            DownloadPrimitivesWithReferrersTask task = new DownloadPrimitivesWithReferrersTask(false, nodesToBeDownloaded, false, true,
                    null, null);
            Thread t = new Thread(task);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }

}
