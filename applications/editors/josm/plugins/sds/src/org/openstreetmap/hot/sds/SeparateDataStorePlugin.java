// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.data.osm.INode;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.IRelation;
import org.openstreetmap.josm.data.osm.IWay;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.PrimitiveVisitor;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.io.OsmServerWriter;
import org.openstreetmap.josm.io.OsmWriterFactory;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 *
 * Plugin that loads extra data from HOT separate data store.
 *
 * @author Frederik Ramm &lt;frederik.ramm@geofabrik.de&gt;
 */
public class SeparateDataStorePlugin extends Plugin {

    public final Map<Long, IPrimitive> originalNodes = new HashMap<>();
    public final Map<Long, IPrimitive> originalWays = new HashMap<>();
    public final Map<Long, IPrimitive> originalRelations = new HashMap<>();

    public List<QueueItem> uploadQueue = new ArrayList<>();

    private PrimitiveVisitor learnVisitor = new PrimitiveVisitor() {
        @Override
        public void visit(INode i) {
            originalNodes.put(i.getId(), i);
        }

        @Override
        public void visit(IWay i) {
            originalWays.put(i.getId(), i);
        }

        @Override
        public void visit(IRelation i) {
            originalRelations.put(i.getId(), i);
        }
    };

    static class QueueItem {
        public final IPrimitive primitive;
        public final Map<String, String> tags;
        public final boolean sdsOnly;
        public boolean processed;
        QueueItem(IPrimitive p, HashMap<String, String> t, boolean s) {
            primitive = p;
            tags = t;
            sdsOnly = s;
            processed = false;
        }
    }

    /**
     * Creates the plugin
     */
    public SeparateDataStorePlugin(PluginInformation info) {
        super(info);

        // this lets us see what JOSM load from the server, and augment it with our data
        OsmReader.registerPostprocessor(new ReadPostprocessor(this));

        // this makes sure that our data is never written to the OSM server on a low level
        OsmWriterFactory.setDefaultFactory(new SdsOsmWriterFactory(this));

        // this lets us see what JOSM is planning to upload, and prepare our own uploads accordingly
        UploadAction.registerUploadHook(new DetermineSdsModificationsUploadHook(this));

        // this lets us perform our own uploads after JOSM has succeeded
        OsmServerWriter.registerPostprocessor(new WritePostprocessor(this));

        // add menu
        new SdsMenu(this);
    }

    public String getIgnorePrefix() {
        return Config.getPref().get("sds-server.tag-prefix", "hot:");
    }

    public IPrimitive getOriginalPrimitive(IPrimitive other) {
        switch (other.getType()) {
        case NODE: return originalNodes.get(other.getId());
        case WAY: return originalWays.get(other.getId());
        case RELATION: return originalRelations.get(other.getId());
        default: throw new AssertionError("unexpected case: " + other.getType());
        }
    }

    protected void enqueueForUpload(IPrimitive prim, HashMap<String, String> tags, boolean onlySds) {
        uploadQueue.add(new QueueItem(prim, tags, onlySds));
    }

    /**
     * Stores the given primitive in the plugin's cache in order to
     * determine changes later.
     */
    protected void learn(IPrimitive prim) {
        if (prim instanceof OsmPrimitive) {
            ((OsmPrimitive) prim).save().accept(learnVisitor);
        } else {
            prim.accept(learnVisitor);
        }
    }

    /**
     * removes all elements from the upload queue that have the processed flag set.
     */
    protected void clearQueue() {
        ArrayList<QueueItem> newQueue = new ArrayList<>();
        for (QueueItem q : uploadQueue) {
            if (!q.processed) newQueue.add(q);
        }
        uploadQueue = newQueue;
    }

    /**
     * reset the processed flag for all elements of the queue.
     */
    protected void resetQueue() {
        for (QueueItem q : uploadQueue) {
            q.processed = false;
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new SdsPluginPreferences();
    }
}
