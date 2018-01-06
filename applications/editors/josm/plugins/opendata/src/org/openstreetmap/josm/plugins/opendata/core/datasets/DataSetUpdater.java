// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SimplifyWayAction;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.command.SplitWayCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.io.Capabilities;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public abstract class DataSetUpdater {

    public static final void updateDataSet(DataSet dataSet, AbstractDataSetHandler handler, File associatedFile) {
        if (dataSet != null) {
            if (handler != null) {
                if (associatedFile != null) {
                    handler.setAssociatedFile(associatedFile);
                    long lastmodified = associatedFile.lastModified();
                    if (lastmodified > 0) {
                        handler.setSourceDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date(lastmodified)));
                    }
                }
                if (!Main.pref.getBoolean(OdConstants.PREF_RAWDATA)) {
                    handler.updateDataSet(dataSet);
                }
                handler.checkDataSetSource(dataSet);
                handler.checkNames(dataSet);
            }
            // Simplify ways geometries
            for (Way w : dataSet.getWays()) {
                SequenceCommand command = SimplifyWayAction.simplifyWay(w, 0.25);
                if (command != null) {
                    command.executeCommand();
                }
            }
            // Split ways exceeding 90% of the API limit (currently 2000 nodes)
            Capabilities capabilities = OsmApi.getOsmApi().getCapabilities();
            long maxwaynodes = capabilities != null ? capabilities.getMaxWayNodes() : 2000L;
            int max = (int) (0.9 * maxwaynodes);
            for (Way w : dataSet.getWays().stream()
                    .filter(w -> w.getNodesCount() > max)
                    .collect(Collectors.toList())) {
                List<Node> atNodes = new ArrayList<>();
                if (w.isClosed()) {
                    atNodes.add(w.getNode(0));
                }
                double n = Math.ceil(w.getNodesCount() / (double) max);
                for (int i = 1; i < n; i++) {
                    atNodes.add(w.getNode((int) ((i / n) * w.getNodesCount())));
                }
                SplitWayCommand res = SplitWayCommand.split(w, atNodes, Collections.emptyList());
                if (res != null) {
                    res.executeCommand();
                }
            }
            // Replace multipolygons with single untagged member by their sole member
            for (Relation r : dataSet.getRelations()) {
                if (r.isMultipolygon() && r.getMembersCount() == 1) {
                    OsmPrimitive outer = r.getMember(0).getMember();
                    if (!outer.isTagged()) {
                        r.remove("type");
                        r.removeMember(0);
                        outer.setKeys(r.getKeys());
                        dataSet.removePrimitive(r);
                    }
                }
            }
        }
    }
}
