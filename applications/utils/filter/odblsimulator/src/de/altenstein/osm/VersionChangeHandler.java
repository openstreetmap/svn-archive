package de.altenstein.osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class VersionChangeHandler {
	
	HashMap<String,String> newTagsMap;
	HashMap<String,String> delTagsMap;
	HashMap<String,String[]> changedTagsMap;
	ArrayList<Integer> newNodes;
	ArrayList<Integer> delNodes;	
	
	TrivialEdits trivEd;
	
	/**
	 * Instances a VersionChangeHandler object (to be used with case 2 and 3).
	 * @param objectType Defines whether nodes or ways are to be compared. objectType == 0 for nodes, objectType == 1 for ways.
	 */
	public VersionChangeHandler(int objectType){
		switch (objectType){
		case 0:
			trivEd = new TrivialEdits("trivialNewTags.txt", "trivialDelTags.txt", "trivialValueChanges.txt", "trivialTagSwaps.txt");
			break;
		case 1:
			trivEd = new TrivialEdits("trivialNewTags.txt", "trivialDelTags.txt", "trivialValueChanges.txt", "trivialTagSwaps.txt");
		}
		
		// System.out.println(trivEd.toString());
	}
	
	/**
	 * Constructor without reference to external files. To be used with case 4 (synthetic nodes/ways).
	 */
	public VersionChangeHandler(){
		
	}
	
	/**
	 * Compares newNode to oldNode and in case that newNode agreed adds all changes between them to finalNode.
	 * Changes include added, deleted and changed tags.
	 * Furthermore, if newNode agreed, it sets finalNode's attMap to newNode's attMap.
	 * To be used with outputType == 5.
	 * @param oldNode
	 * @param newNode
	 * @param finalNode
	 * @return OsmNode finalNode 
	 */
	public OsmNode writeNodeChanges(OsmNode oldNode, OsmNode newNode, OsmNode finalNode){
		// setup of member variables
		// determine new, deleted and changed tags
		newTagsMap = getNewTags(oldNode, newNode);
		delTagsMap = getDeletedTags(oldNode, newNode);
		changedTagsMap = getChangedTags(oldNode, newNode);
		
		if (newNode.agreed){			
			// by applying newNode's attMap, position will be transferred as well
			finalNode.attMap = newNode.attMap;
			// add new tags
			Iterator<Entry<String,String>> newIt = newTagsMap.entrySet().iterator();
			while (newIt.hasNext()){
				Entry<String,String> entry = newIt.next();
				finalNode.tagMap.put(entry.getKey(), entry.getValue());
			}
			
			// remove del tags
			Iterator<Entry<String,String>> delIt = delTagsMap.entrySet().iterator();
			while (delIt.hasNext()){
				Entry<String,String> entry = delIt.next();
				finalNode.tagMap.remove(entry.getKey());
			}
			
			// update changed tags
			Iterator<Entry<String,String[]>> changedIt = changedTagsMap.entrySet().iterator();
			while (changedIt.hasNext()){
				Entry<String,String[]> entry = changedIt.next();
				if (finalNode.tagMap.containsKey(entry.getKey())){
					finalNode.tagMap.put(entry.getKey(), entry.getValue()[1]);
				}
			}			
		}		
		return finalNode;
	}
	
	/**
	 * Compares newWay to oldWay and in case that newNode agreed adds all changes between them to finalWay. 
	 * Changes include added, deleted, changed tags and added or deleted node references.
	 * Furthermore, if newWay agreed, it sets finalWay's attMap to newWay's attMap.
	 * To be used with outputType == 5.
	 * @param oldWay
	 * @param newWay
	 * @param finalWay
	 * @return OsmWay finalWay
	 */
	public OsmWay writeWayChanges(OsmWay oldWay, OsmWay newWay, OsmWay finalWay, HashMap<Integer,Double[]> nodePositions){
		// setup of member variables
		// determine new, deleted, changed tags, newNodes and delNodes
		newTagsMap = getNewTags(oldWay, newWay);
		delTagsMap = getDeletedTags(oldWay, newWay);
		changedTagsMap = getChangedTags(oldWay, newWay);
		newNodes = getAddedNodes(oldWay, newWay);
		delNodes = getDeletedNodes(oldWay, newWay);
		
		if (newWay.agreed){			
			finalWay.attMap = newWay.attMap;
			// add new tags
			Iterator<Entry<String,String>> newIt = newTagsMap.entrySet().iterator();
			while (newIt.hasNext()){
				Entry<String,String> entry = newIt.next();
				finalWay.tagMap.put(entry.getKey(), entry.getValue());
			}
			
			// remove deleted tags
			Iterator<Entry<String,String>> delIt = delTagsMap.entrySet().iterator();
			while (delIt.hasNext()){
				Entry<String,String> entry = delIt.next();
				finalWay.tagMap.remove(entry.getKey());
			}
			
			// update changed tags
			Iterator<Entry<String,String[]>> changedIt = changedTagsMap.entrySet().iterator();
			while (changedIt.hasNext()){
				Entry<String,String[]> entry = changedIt.next();
				if (finalWay.tagMap.containsKey(entry.getKey())){
					finalWay.tagMap.put(entry.getKey(), entry.getValue()[1]);
				}
			}
			
			// remove deleted node refs
			for (int i = 0; i < delNodes.size(); i++){
				//System.out.println("remove node ref: " + delNodes.get(i));
				finalWay.nodeList.remove(delNodes.get(i));
			}
			
			// add new node refs
			
			for (int i = 0; i < newNodes.size(); i++){
				int insertIndex = 0;
				
				double latNewNode = nodePositions.get(newNodes.get(i))[0];
				double lonNewNode = nodePositions.get(newNodes.get(i))[1];
				
				// determine at which index node should be added to node references
				if (finalWay.nodeList.size() > 1){
					double minDist = 9999;
					int minDistIndex = 0;
					for (int j = 0; j < finalWay.nodeList.size(); j++){
						double lat = nodePositions.get(finalWay.nodeList.get(j))[0];
						double lon = nodePositions.get(finalWay.nodeList.get(j))[1];
						double dist = Math.sqrt( (latNewNode - lat)*(latNewNode - lat) + (lonNewNode - lon)*(lonNewNode - lon) );
						if (dist < minDist){
							minDist = dist;
							minDistIndex = j;
						}
					}
					
					// determine if node has to be inserted before or after the calculated index
					if (minDistIndex == 0){
						double latFirst = nodePositions.get(finalWay.nodeList.get(0))[0];
						double lonFirst = nodePositions.get(finalWay.nodeList.get(0))[1];
						double latSecond = nodePositions.get(finalWay.nodeList.get(1))[0];
						double lonSecond = nodePositions.get(finalWay.nodeList.get(1))[1];
						double angle = getAngle(latFirst, lonFirst, latSecond, lonSecond, latNewNode, lonNewNode);
									
						if (angle > 90){
							insertIndex = 0;
						} else {
							insertIndex = 1;
						}
					} else if (minDistIndex + 1 == finalWay.nodeList.size()){
						int nodeListSize = finalWay.nodeList.size();
						double latLast = nodePositions.get(finalWay.nodeList.get(nodeListSize - 1))[0];
						double lonLast = nodePositions.get(finalWay.nodeList.get(nodeListSize - 1))[1];
						double latBeforeLast = nodePositions.get(finalWay.nodeList.get(nodeListSize - 2))[0];
						double lonBeforeLast = nodePositions.get(finalWay.nodeList.get(nodeListSize - 2))[1];
						double angle = getAngle(latLast, lonLast, latBeforeLast, lonBeforeLast, latNewNode, lonNewNode);
						
						if (angle < 90){
							insertIndex = nodeListSize - 1;
						} else {
							insertIndex = nodeListSize;
						}
					} else {
						double latBefore = nodePositions.get(finalWay.nodeList.get(minDistIndex - 1))[0];
						double lonBefore = nodePositions.get(finalWay.nodeList.get(minDistIndex - 1))[1];
						double distBefore = Math.sqrt( (latNewNode - latBefore)*(latNewNode - latBefore) + (lonNewNode - lonBefore)*(lonNewNode - lonBefore) );
						
						double latAfter = nodePositions.get(finalWay.nodeList.get(minDistIndex + 1))[0];
						double lonAfter = nodePositions.get(finalWay.nodeList.get(minDistIndex + 1))[1];
						double distAfter = Math.sqrt( (latNewNode - latAfter)*(latNewNode - latAfter) + (lonNewNode - lonAfter)*(lonNewNode - lonAfter) );
						
						if (distBefore < distAfter){
							insertIndex = minDistIndex;
						} else {
							insertIndex = minDistIndex + 1;
						}
					}
				}
				// add node ref at specified index
				//System.out.println("add node ref: " + newNodes.get(i));				
				finalWay.nodeList.add(insertIndex, newNodes.get(i));
			}
			
		}
		return finalWay;
	}
	
	/**
	 * Calculates the angle between two vectors. v1 is pointing from origin to A, v2 is pointing from origin to B.
	 * @param latOrigin
	 * @param lonOrigin
	 * @param latA
	 * @param lonA
	 * @param latB
	 * @param lonB
	 * @return double angle in deegrees (0 <= angle <= 180).
	 */
	private double getAngle(double latOrigin, double lonOrigin, double latA, double lonA, double latB, double lonB){
		double a1 = latA - latOrigin;
		double a2 = lonA - lonOrigin;
		double b1 = latB - latOrigin;
		double b2 = lonB - lonOrigin;
		
		double angle = Math.acos((a1*b1 + a2*b2)/(Math.sqrt(a1*a1 + a2*a2) * Math.sqrt(b1*b1 + b2*b2)));
		angle = angle*180/Math.PI;
		return angle;
	}
	
	/**
	 * Compares newNode to oldNode, analyzes which object properties have been edited and judges about if changes are trivial.
	 * To be used with outputType == 3 || 4.
	 * @param oldNode
	 * @param newNode
	 * @return true if changes are trivial, false if they are not trivial
	 */
	public boolean isTrivialNodeEdit(OsmNode oldNode, OsmNode newNode){
		// setup of member variables
				
		// determine new, deleted and changed tags
		newTagsMap = getNewTags(oldNode, newNode);
		delTagsMap = getDeletedTags(oldNode, newNode);
		changedTagsMap = getChangedTags(oldNode, newNode);
				
		// tolerance values
		double posTol = 1;
				
		//
		// check for various change events
		//
		
		// position
		boolean posChangeTrivial = true;
		if (!trivialPosChange(oldNode, newNode, posTol)){
			posChangeTrivial = false;
		}
		
		// new tags
		boolean newTagsTrivial = true;
		if (newTagsMap.size() > 0){
			// new tags compared to old node
			//System.out.println("new tags");
			newTagsTrivial = newTagsAreTrivial();
		}
		
		// deleted tags
		boolean delTagsTrivial = true;
		if (delTagsMap.size() > 0){
			// new tags compared to old node
			//System.out.println("deleted tags");
			delTagsTrivial = delTagsAreTrivial();
		}
		
		// changed tags
		boolean valueChangesTrivial = true;
		if (changedTagsMap.size() > 0){
			// new tags compared to old node
			//System.out.println("changed tags");
			valueChangesTrivial = valueChangeIsTrivial();
		}
		
		if (	newTagsTrivial && 
				delTagsTrivial && 
				valueChangesTrivial && 
				posChangeTrivial){
			return true;		
		} else {
			return false;
		}
	}
	
	/**
	 * Compares newWay to oldWay, analyzes which object properties have been edited and judges about if changes are trivial.
	 * To be used with outputType == 3 || 4.
	 * @param oldWay
	 * @param newWay
	 * @return true if changes are trivial, false if they are not trivial
	 */
	public boolean isTrivialWayEdit(OsmWay oldWay, OsmWay newWay){
		// determine new, deleted and changed tags
		newTagsMap = getNewTags(oldWay, newWay);
		delTagsMap = getDeletedTags(oldWay, newWay);
		changedTagsMap = getChangedTags(oldWay, newWay);
		newNodes = getAddedNodes(oldWay, newWay);
		delNodes = getDeletedNodes(oldWay, newWay);
		
		// new tags
		boolean newTagsTrivial = true;
		if (newTagsMap.size() > 0){
			// new tags compared to old way
			//System.out.println("new tags");
			newTagsTrivial = newTagsAreTrivial();
		}
		
		// deleted tags
		boolean delTagsTrivial = true;
		if (delTagsMap.size() > 0){
			// new tags compared to old way
			//System.out.println("deleted tags");
			delTagsTrivial = delTagsAreTrivial();
		}
		
		// changed tags
		boolean valueChangesTrivial = true;
		if (changedTagsMap.size() > 0){
			// new tags compared to old way
			//System.out.println("changed tags");
			valueChangesTrivial = valueChangeIsTrivial();
		}
		
		// added node references
		boolean addedNodeRefTrivial = true;
		if (newNodes.size() > 0){
			//System.out.println("added node ref\n   not trivial");
			addedNodeRefTrivial = false;
		}
		// deleted node references
		boolean deletedNodeRefTrivial = true;
		if (delNodes.size() > 0){
			//System.out.println("added node ref\n   not trivial");
			deletedNodeRefTrivial = false;
		}
		
		if (	newTagsTrivial && 
				delTagsTrivial && 
				valueChangesTrivial &&
				addedNodeRefTrivial &&
				deletedNodeRefTrivial){
			return true;		
		} else {
			return false;
		}
	}
	
	/**
	 * Checks whether position between node versions changed by more than the given tolerance.
	 * @param oldNode
	 * @param newNode
	 * @param tol Tolerance value in meters
	 * @return true if position change is below tolerance, false if position change is higher than tolerance
	 */
	private boolean trivialPosChange(OsmNode oldNode, OsmNode newNode, double tol){
		// check for position change
		if (	!oldNode.attMap.get("lat").equals(newNode.attMap.get("lat")) || 
				!oldNode.attMap.get("lon").equals(newNode.attMap.get("lon"))){
			//System.out.println("position change");		
			double oldLat = Double.parseDouble(oldNode.attMap.get("lat"));
			double oldLon = Double.parseDouble(oldNode.attMap.get("lon"));
			double newLat = Double.parseDouble(newNode.attMap.get("lat"));
			double newLon = Double.parseDouble(newNode.attMap.get("lon"));
			
			// calculate distance between both positions by using 'haversine' formula
			double r = 6371 * 1000;
			double toRadFactor =  Math.PI / 180;
			double dLat = (newLat - oldLat) * toRadFactor;
			double dLon = (newLon - oldLon) * toRadFactor;
			double a = 	Math.sin(dLat/2) * Math.sin(dLat/2) + 
						Math.cos(oldLat * toRadFactor) * Math.cos(newLat * toRadFactor) * 
						Math.sin(dLon/2) * Math.sin(dLon/2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			double dist = r * c;
			if (dist > tol){
				//System.out.println("   old: " + oldLat + " , " + oldLon + " _ new: " + newLat + " , " + newLon);
				//System.out.println("   "  + dist + " > " + tol + " meters");
				return false;
			} else {
				//System.out.println("   " + dist + " < " + tol + "meters");
			}
		}
		return true;
	}
	
	/**
	 * Compares all tags of the given OsmObject and returns those tags which have been added.
	 * OsmObject is an OsmNode or OsmWay.
	 * @param oldObject
	 * @param newObject
	 * @return HashMap holding all newly added tags (key value pairs)
	 */
	private HashMap<String,String> getNewTags(OsmObject oldObject, OsmObject newObject){
		// map will hold all new tags
		HashMap<String,String> newTagsMap = new HashMap<String,String>();
		
		Iterator<Entry<String, String>> it = newObject.getTagMap().entrySet().iterator();
		while (it.hasNext()){
			Entry<String, String> entry = it.next();
			
			if (!oldObject.hasTag(entry.getKey())){
				// new tag in this node version
				newTagsMap.put(entry.getKey(),entry.getValue());
			}
		}
		return newTagsMap;
	}
	
	/**
	 * Decides whether the newly added tags are trivial.
	 * To be used with outputType == 3 || 4. 
	 * @return true if all new tags are trivial, false if at least one new tag is copyright protected
	 */
	private boolean newTagsAreTrivial(){
		// ArrayList<String> trivialKeys = new ArrayList<String>();
		// trivialKeys.add("created_by");
		
		boolean newTagsTrivial = true;
		
		Iterator<Entry<String, String>> it = newTagsMap.entrySet().iterator();
		while (it.hasNext()){
			Entry<String, String> entry = it.next();
			//System.out.println("   " + entry.getKey() + "=" + entry.getValue());
			// breaks while-loop and returns false if trivialKeys-array doesn't contain key			
			if (!trivEd.containsNewKey(entry.getKey())){
				//System.out.println("      not trivial");
				newTagsTrivial = false;
				break;
			}
		}
		return newTagsTrivial;
	}
	
	/**
	 * Compares the tags of the given OsmObject and returns a HashMap containing all deleted tags.
	 * @param oldObject 
	 * @param newObject
	 * @return HashMap<String,String> containing tags which have been deleted in newObject compared to oldObject
	 */
	private HashMap<String,String> getDeletedTags(OsmObject oldObject, OsmObject newObject){
		// map will hold all deleted tags
		HashMap<String,String>delTagsMap = new HashMap<String,String>();
		
		Iterator<Entry<String, String>> it = oldObject.getTagMap().entrySet().iterator();
		while (it.hasNext()){
			Entry<String, String> entry = it.next();
			if (!newObject.hasTag(entry.getKey())){
				// tag has been deleted
				delTagsMap.put(entry.getKey(),entry.getValue());
			}
		}
		return delTagsMap;
	}
	
	/**
	 * Decides whether the deleted tags are trivial.
	 * To be used with outputType == 3 || 4.
	 * @return true if all new tags are trivial, false if at least one new tag is copyright protected
	 */
	private boolean delTagsAreTrivial(){
		// ArrayList<String> trivialKeys = new ArrayList<String>();
		// trivialKeys.add("created_by");
		
		boolean delTagsTrivial = true;
		
		Iterator<Entry<String, String>> it = delTagsMap.entrySet().iterator();
		while (it.hasNext()){
			Entry<String, String> entry = it.next();
			//System.out.println("   " + entry.getKey() + "=" + entry.getValue());
			// breaks while-loop and returns false if trivialKeys-array doesn't contain key			
			if (!trivEd.containsDelKey(entry.getKey())){
				//System.out.println("      not trivial");
				delTagsTrivial = false;
				break;
			}
		}
		return delTagsTrivial;
	}
	
	/**
	 * Compares all k/v-pairs and returns a HashMap containing all k/v-pairs which have changed
	 * @param oldObject
	 * @param newObject
	 * @return HashMap<String,String[]> containing all changed k/v-pairs. Key is the key, value is a String[] array where [0] is old value, [1] is new value
	 */
	private HashMap<String,String[]> getChangedTags(OsmObject oldObject, OsmObject newObject){
		// map will hold all deleted tags
		HashMap<String,String[]>changedTagsMap = new HashMap<String,String[]>();
		
		Iterator<Entry<String, String>> it = oldObject.getTagMap().entrySet().iterator();
		while (it.hasNext()){
			Entry<String, String> entry = it.next();
			if (newObject.hasTag(entry.getKey())){
				String key = entry.getKey();
				String oldValue = entry.getValue();
				String newValue = newObject.getTagValue(entry.getKey());
				if (!newValue.equals(oldValue)){
					// key has changed
					changedTagsMap.put(key,new String[]{oldValue,newValue});
				}
			}			
		}		
		return changedTagsMap;
	}
	
	/**
	 * Decides whether the value changes are trivial.
	 * To be used with outputType == 3 || 4.
	 * @return true if value changes are trivial, false if they are not trivial
	 */
	private boolean valueChangeIsTrivial(){
		boolean valueChangeTrivial = true;
		
		Iterator<Entry<String, String[]>> it = changedTagsMap.entrySet().iterator();
		while (it.hasNext()){
			Entry<String, String[]> entry = it.next();
			String key = entry.getKey();
			String v1 = entry.getValue()[0];
			String v2 = entry.getValue()[1];
			
			//System.out.println("   " + key + "=" + v1 + " --> " + key + "=" + v2);
				
			if (!trivEd.containsChangedValues(key, v1, v2)){
				//System.out.println("      not trivial");
				valueChangeTrivial = false;
				break;
			}
			
			/* Maybe detect change from str to straﬂe or so
			String value1 = entry.getValue()[0];
			String value2 = entry.getValue()[1];
			
			if (value1.contains("str") && !value1.contains("straﬂe") && value2.contains("straﬂe")){
				valueChangeTrivial = true;
				System.out.println("value change trivial");
			} else {
				valueChangeTrivial = false;
				System.out.println("value change not trivial");
				break;
			}
			*/
			
		}
		return valueChangeTrivial;
	}
	
	/**
	 * Compares newWay to oldWay and returns an ArrayList containing the id's of all newly added node references.
	 * @param oldWay
	 * @param newWay
	 * @return ArrayList<Integer> containing id's of new node references
	 */
	private ArrayList<Integer> getAddedNodes(OsmWay oldWay, OsmWay newWay){
		ArrayList<Integer> addNodes = new ArrayList<Integer>();
		for (int i = 0; i < newWay.nodeList.size(); i++){
			if (!oldWay.nodeList.contains(newWay.nodeList.get(i))){
				// new node reference
				addNodes.add(newWay.nodeList.get(i));
			}
		}
		return addNodes;
	}
	
	/**
	 * Compares newWay to oldWay and returns an ArrayList containing the id's of all deleted node references.
	 * @param oldWay
	 * @param newWay
	 * @return ArrayList<Integer> containing id's of deleted node references
	 */
	private ArrayList<Integer> getDeletedNodes(OsmWay oldWay, OsmWay newWay){
		ArrayList<Integer> delNodes = new ArrayList<Integer>();
		for (int i = 0; i < oldWay.nodeList.size(); i++){
			if (!newWay.nodeList.contains(oldWay.nodeList.get(i))){
				delNodes.add(oldWay.nodeList.get(i));
			}
		}
		return delNodes;
	}
	
}