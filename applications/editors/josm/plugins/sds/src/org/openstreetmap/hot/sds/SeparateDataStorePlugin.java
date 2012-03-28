// License: GPL. See LICENSE file for details.
package org.openstreetmap.hot.sds;

import java.util.ArrayList;
import java.util.HashMap;

import org.openstreetmap.josm.Main;
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

/**
 *
 * Plugin that loads extra data from HOT separate data store.
 *
 * @author Frederik Ramm <frederik.ramm@geofabrik.de>
 */
public class SeparateDataStorePlugin extends Plugin 
{

	public HashMap<Long, IPrimitive> originalNodes = new HashMap<Long, IPrimitive>();
	public HashMap<Long, IPrimitive> originalWays = new HashMap<Long, IPrimitive>();
	public HashMap<Long, IPrimitive> originalRelations = new HashMap<Long, IPrimitive>();
	
	public ArrayList<QueueItem> uploadQueue = new ArrayList<QueueItem>();
	
	private PrimitiveVisitor learnVisitor = new PrimitiveVisitor() {
		public void visit(INode i) { originalNodes.put(i.getId(), i); }
		public void visit(IWay i) { originalWays.put(i.getId(), i); }
		public void visit(IRelation i) { originalRelations.put(i.getId(), i); }
	};
	
	class QueueItem {
		public IPrimitive primitive;
		public HashMap<String,String> tags;
		public boolean sdsOnly;
		public boolean processed;
		public QueueItem(IPrimitive p, HashMap<String,String> t, boolean s) {
			primitive = p;
			tags = t;
			sdsOnly = s;
			processed = false;
		}
	}
	
    /**
     * Creates the plugin
     */
    public SeparateDataStorePlugin(PluginInformation info) 
    {
        super(info);
    	System.out.println("initializing SDS plugin");
    	
    	// this lets us see what JOSM load from the server, and augment it with our data:
        OsmReader.registerPostprocessor(new ReadPostprocessor(this));
        
        // this makes sure that our data is never written to the OSM server on a low level;
        OsmWriterFactory.theFactory = new SdsOsmWriterFactory(this);
        
        // this lets us see what JOSM is planning to upload, and prepare our own uploads
        // accordingly
        UploadAction.registerUploadHook(new DetermineSdsModificationsUploadHook(this));
        
        // this lets us perform our own uploads after JOSM has succeeded:
        OsmServerWriter.registerPostprocessor(new WritePostprocessor(this));

        // add menu
        new SdsMenu(this);
    }

	public String getIgnorePrefix() {
        return Main.pref.get("sds-server.tag-prefix", "hot:");
	}
	
	public IPrimitive getOriginalPrimitive(IPrimitive other) {
		switch (other.getType()) {
		case NODE: return originalNodes.get(other.getId());
		case WAY: return originalWays.get(other.getId());
		case RELATION: return originalRelations.get(other.getId());
		}
		return null;	
	}
	
	protected void enqueueForUpload(IPrimitive prim, HashMap<String, String> tags, boolean onlySds) {
		uploadQueue.add(new QueueItem(prim, tags, onlySds));
	}
	
	/** 
	 * Stores the given primitive in the plugin's cache in order to
	 * determine changes later.
	 * @param prim
	 */
	protected void learn(IPrimitive prim) {
		if (prim instanceof OsmPrimitive) {
			((OsmPrimitive)prim).save().visit(learnVisitor);
		} else {
			prim.visit(learnVisitor);
		}
	}
	
	/**
	 * removes all elements from the upload queue that have the processed flag set.
	 */
	protected void clearQueue() {
		ArrayList<QueueItem> newQueue = new ArrayList<QueueItem>();
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

    public PreferenceSetting getPreferenceSetting() {
        return new SdsPluginPreferences();
    }

}

