// License: GPL. See LICENSE file for details.
package org.openstreetmap.hot.sds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.osm.INode;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.IRelation;
import org.openstreetmap.josm.data.osm.IWay;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * This upload hook does the following things:
 * 
 * 1. Find out if there are any changes in the special tags that need to 
 *    be uploaded to a different server.
 * 2. Find out if any objects that did have special tags have now been 
 *    deleted, resulting in tag deletions on the special server.
 * 3. Find out if any objects carrying special tags have been newly created.
 * 4. Also, if it is determined that an object modification consists exclusively
 *    of special tags, then skip uploading that object, by removing it from 
 *    the apiDataSet.
 *    
 * This upload hook stores its findings with the SeparateDataStorePlugin, and
 * changes are sent to the SDS server only after the OSM upload has sucessfully
 * completed. The UploadSuccessHook is responsible for that.
 */
public class DetermineSdsModificationsUploadHook implements UploadHook
{
	private SeparateDataStorePlugin plugin;

	DetermineSdsModificationsUploadHook(SeparateDataStorePlugin plugin)	{
		this.plugin = plugin;
	}
	
    public boolean checkUpload(APIDataSet apiDataSet) {
    	
    	ArrayList<OsmPrimitive> droplist = new ArrayList<OsmPrimitive>();
    	
    	// check deleted primitives for special tags.
    	for (OsmPrimitive del : apiDataSet.getPrimitivesToDelete()) {
    		IPrimitive old = plugin.getOriginalPrimitive(del);
    		if (hasSpecialTags(old)) {
    			// request deletion of all tags for this object on special server.
    			plugin.enqueueForUpload(del, new HashMap<String, String>(), false);
    		}
    	}

    	// check modified primitives.
       	for (OsmPrimitive upd : apiDataSet.getPrimitivesToUpdate()) {
       		
       		HashSet<String> allKeys = new HashSet<String>();
       		boolean specialTags = false;
       		
       		// process tags of new object
       		for (String key : upd.keySet()) {
       			allKeys.add(key);
       			if (!specialTags && isSpecialKey(key)) specialTags = true;
       		}
       		
       		// process tags of old object
       		IPrimitive old = plugin.getOriginalPrimitive(upd);
       		for (String key : old.keySet()) {
       			allKeys.add(key);
      			if (!specialTags && isSpecialKey(key)) specialTags = true;
       		}

       		// if neither has special tags, done with this object.
       		if (!specialTags) continue;
       		
       		// special tags are involved. find out what, exactly, has changed.
       		boolean changeInSpecialTags = false;
       		boolean changeInOtherTags = false;
       		for (String key : allKeys) {
       			if (old.get(key) == null || upd.get(key) == null || !old.get(key).equals(upd.get(key))) {
       				if (isSpecialKey(key)) changeInSpecialTags = true; else changeInOtherTags = true;
       				if (changeInSpecialTags && changeInOtherTags) break;
       			}
       		}
       		
       		// change *only* in standard tags - done with this object.
       		if (!changeInSpecialTags) continue;
       		
       		// assemble new set of special tags. might turn out to be empty.
       		HashMap<String, String> newSpecialTags = new HashMap<String, String>();
       		for (String key : upd.keySet()) {
       			if (isSpecialKey(key)) newSpecialTags.put(key, upd.get(key));
       		}
       		
       		boolean uploadToOsm = changeInOtherTags;
       		
       		// not done yet: if no changes in standard tags, we need to find out if 
       		// there were changes in the other properties (node: lat/lon, way/relation:
       		// member list). If the answer is no, then the object must be removed from 
       		// JOSM's normal upload queue, else we would be uploading a non-edit.
       		if (!changeInOtherTags) {
       			switch(old.getType()) {
       			case NODE:
       				INode nold = (INode) old;
       				INode nupd = (INode) upd;
       				uploadToOsm = !(nold.getCoor().equals(nupd.getCoor()));
       				break;
       			case WAY:
       				IWay wold = (IWay) old;
       				IWay wupd = (IWay) upd;
       				if (wold.getNodesCount() != wupd.getNodesCount()) {
       					uploadToOsm = true;
       					break;
       				} 
       				for (int i = 0; i < wold.getNodesCount(); i++) {
       					if (wold.getNodeId(i) != wupd.getNodeId(i)) {
       						uploadToOsm = true;
       						break;
       					}
       				}
       				break;
       			case RELATION:
       				IRelation rold = (IRelation) old;
       				IRelation rupd = (IRelation) upd;
       				if (rold.getMembersCount()!= rupd.getMembersCount()) {
       					uploadToOsm = true;
       					break;
       				} 
       				for (int i = 0; i < rold.getMembersCount(); i++) {
       					if (rold.getMemberType(i) != rupd.getMemberType(i) ||
       						rold.getMemberId(i) != rupd.getMemberId(i)) {
       						uploadToOsm = true;
       						break;
       					}
       				}
       				break;
       			}
       		}
       		
       		// request that new set of special tags be uploaded
       		plugin.enqueueForUpload(upd, newSpecialTags, !uploadToOsm);
       		
       		// we cannot remove from getPrimitivesToUpdate, this would result in a 
       		// ConcurrentModificationException.
       		if (!uploadToOsm) droplist.add(upd);
       		
    	}
       	
    	apiDataSet.getPrimitivesToUpdate().removeAll(droplist);
		       	
       	// check added primitives. 
       	for (OsmPrimitive add : apiDataSet.getPrimitivesToAdd()) {
       		// assemble new set of special tags. might turn out to be empty.
       		HashMap<String, String> newSpecialTags = new HashMap<String, String>();
       		for (String key : add.keySet()) {
       			if (isSpecialKey(key)) newSpecialTags.put(key, add.get(key));
       		}
       		if (!newSpecialTags.isEmpty()) plugin.enqueueForUpload(add, newSpecialTags, false);
    	}
    	
       	// FIXME it is possible that the list of OSM edits is totally empty.
		return true;

    }
    
    boolean hasSpecialTags(IPrimitive p) {
    	for (String key : p.keySet()) {
    		if (isSpecialKey(key)) return true;
    	}    
    	return false;
    }
    
    boolean isSpecialKey(String key) {
    	return key.startsWith(plugin.getIgnorePrefix());
    }
}
