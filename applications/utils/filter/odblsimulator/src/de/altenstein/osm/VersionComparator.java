package de.altenstein.osm;

import java.util.ArrayList;
import java.util.HashMap;

public class VersionComparator {
	
	int outputType = 0;	
	OsmXmlWriter writer;
	OsmNode finalNode;
	
	/**
	 * Constructor. Will set the member variables.
	 * @param writer OsmXmlWriter object pointing onto the output file.
	 * @param outputType
	 */
	public VersionComparator(OsmXmlWriter writer, int outputType){
		this.writer = writer;
		this.outputType = outputType;
	}
	
	/**
	 * Compares all nodes saved in nodeList.
	 * Analyzes the changes between them varying between outputType. 
	 * Returns the calculated licenseStatus of that node. 
	 * @param nodeList holds OsmNode objects of the same id
	 * @return 0, 1 or 2 giving information about license status. 3 if missing versions were encountered.
	 */
	public int compareNodes(ArrayList<OsmNode> nodeList){
		finalNode = null;
		
		// detect missing way versions (e.g. due to limitations while creating history planet file extract)
		if (Integer.parseInt(nodeList.get(nodeList.size() - 1).attMap.get("version")) != nodeList.size()){
			finalNode = nodeList.get(0);
			return 3;
		}
		
		// compare nodes in different manners depending on outputType
		switch (outputType){
		case 1:
			// output is MOST CURRENT VERSION of node + licenseStatus.
			// 0 -> if ALL versions AGREED
			// 1 -> if version 1 did AGREE (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE
			finalNode = nodeList.get(nodeList.size() - 1);
			
			// do not output those nodes which have been deleted (last version's visible-attribute is false)
			if (finalNode.attMap.containsKey("visible")){
				if (finalNode.attMap.get("visible").equals("false")){
					return 3;
				}
			}
			
			int agreeCount = 0;
			for (int i = 0; i < nodeList.size(); i++){
				if (nodeList.get(i).agreed){
					agreeCount++;
				} else {
					break;
				}
			}
			
			if (agreeCount > 0){
				if (agreeCount == nodeList.size()){
					// all versions of node being agreed
					finalNode.licenseStatus = LicenseConstants.NODE_AGREED;
					writer.writeNode(finalNode);
					return 0;
				} else {
					// not all, but at least the first version of this node agreed 
					finalNode.licenseStatus = LicenseConstants.NODE_SOME_AGREED;
					writer.writeNode(finalNode);
					return 1;
				}
			} else {
				// the first version of node did not agree
				finalNode.licenseStatus = LicenseConstants.NODE_ALL_DISAGREED;
				writer.writeNode(finalNode);
				return 2;
			}
		
		case 2:
			// output is LAST AGREED VERSION of node + licenseStatus. Equivalent to implementation planned by OSM.
			// licenseStatus tag contains:
			// 0 -> if ALL versions AGREED
			// 1 -> if version 1 did AGREE (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE
			int nodeListSize = nodeList.size();
			
			agreeCount = 0;
			for (int i = 0; i < nodeList.size(); i++){
				if (nodeList.get(i).agreed){
					agreeCount++;
					finalNode = nodeList.get(i);
				} else {
					break;
				}
			}
			
			// do not output those nodes which have been deleted (last version's visible-attribute is false)
			if (agreeCount > 0){
				if (finalNode.attMap.containsKey("visible")){
					if (finalNode.attMap.get("visible").equals("false")){
						return 3;
					}
				}
			}
			
			if (agreeCount == 0){
				// no single node agreed
				return 2;
			} else if (agreeCount < nodeListSize){
				// at least version 1 agreed
				finalNode.licenseStatus = LicenseConstants.NODE_SOME_AGREED;
				writer.writeNode(finalNode);
				return 1;
			} else {
				// all versions agreed
				finalNode.licenseStatus = LicenseConstants.NODE_AGREED;
				writer.writeNode(finalNode);
				return 0;
			}
		case 3:
			// output is MOST CURRENT VERSION of node + licenseStatus. It is checked if an edit is believed to be trivial by VersionChangeHandler.isTrivialNodeEdit(). 
			// licenseStatus tag contains:
			// 0 -> if version 1 AGREED and ALL versions PASSED isTrivialNodeEdit()
			// 1 -> if version 1 AGREED but NOT ALL subsequent versions PASSED isTrivialNodeEdit() (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE
			VersionChangeHandler changeHandler = new VersionChangeHandler(0);
			finalNode = nodeList.get(nodeList.size() - 1);
			nodeListSize = nodeList.size();
			
			// do not output those nodes which have been deleted (last version's visible-attribute is false)
			if (finalNode.attMap.containsKey("visible")){
				if (finalNode.attMap.get("visible").equals("false")){
					return 3;
				}
			}
			
			agreeCount = 0;
			if (nodeList.get(0).agreed){
				agreeCount++;
				for (int i = 0; i < nodeList.size() - 1; i++){
					if (nodeList.get(i+1).agreed){
						agreeCount++;
					} else {
						boolean isTrivialEdit = changeHandler.isTrivialNodeEdit(nodeList.get(i), nodeList.get(i+1));
						if (isTrivialEdit){
							agreeCount++;
						} else {
							break;
						}
					}
				}
			}			
			
			if (agreeCount == 0){
				// no single node agreed
				finalNode.licenseStatus = LicenseConstants.NODE_ALL_DISAGREED;
				writer.writeNode(finalNode);
				return 2;
			} else if (agreeCount < nodeListSize){
				// at least version 1 agreed
				finalNode.licenseStatus = LicenseConstants.NODE_SOME_AGREED;
				writer.writeNode(finalNode);
				return 1;
			} else {
				// all versions agreed
				finalNode.licenseStatus = LicenseConstants.NODE_AGREED;
				writer.writeNode(finalNode);
				return 0;
			}
		
		case 4:
			// output is LAST PASSED VERSION of node + licenseStatus.
			// It is checked whether an edit is believed to be trivial by using VersionChangeHandler.isTrivialNodeEdit()
			// 0 -> if version 1 AGREED and ALL versions PASSED isTrivialNodeEdit()
			// 1 -> if version 1 AGREED but NOT ALL subsequent versions PASSED isTrivialNodeEdit() (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE
			changeHandler = new VersionChangeHandler(0);
			finalNode = null;
			nodeListSize = nodeList.size();			
			
			agreeCount = 0;
			if (nodeList.get(0).agreed){
				agreeCount++;
				finalNode = nodeList.get(0);
				for (int i = 0; i < nodeListSize - 1; i++){
					if (nodeList.get(i+1).agreed){
						agreeCount++;
					} else {
						boolean isTrivialEdit = changeHandler.isTrivialNodeEdit(nodeList.get(i), nodeList.get(i+1));
						if (isTrivialEdit){
							agreeCount++;
							finalNode = nodeList.get(i+1);
						} else {
							break;
						}
					}
				}
			}
			
			// do not output those nodes which have been deleted (last version's visible-attribute is false)
			if (agreeCount > 0){
				if (finalNode.attMap.containsKey("visible")){
					if (finalNode.attMap.get("visible").equals("false")){
						return 3;
					}
				}
			}
			
			if (agreeCount == 0){
				// no single node agreed, so no node version will be in output
				return 2;
			} else if (agreeCount < nodeListSize){
				// at least version 1 agreed
				finalNode.licenseStatus = LicenseConstants.NODE_SOME_AGREED;
				writer.writeNode(finalNode);
				return 1;
			} else {
				// all versions agreed
				finalNode.licenseStatus = LicenseConstants.NODE_AGREED;
				writer.writeNode(finalNode);
				return 0;
			}
		
		case 5:
			// output will be an ARTIFICIAL node + licenseStatus. Only changes from users who agreed will be in output.
			// 0 -> if ALL versions AGREED
			// 1 -> if version 1 did AGREE (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE 
			agreeCount = 0;
			changeHandler = new VersionChangeHandler();
			if (nodeList.get(0).agreed){
				finalNode = nodeList.get(0);
				agreeCount++;
				for (int i = 0; i < nodeList.size() - 1; i++){
					if (nodeList.get(i+1).agreed){
						agreeCount++;
					}
					finalNode = changeHandler.writeNodeChanges(nodeList.get(i), nodeList.get(i+1), finalNode);
				}
				
				// do not output those nodes which have been deleted (last version's visible-attribute is false)
				if (finalNode.attMap.containsKey("visible")){
					if (finalNode.attMap.get("visible").equals("false")){
						return 3;
					}
				}
				
				if (agreeCount == nodeList.size()){
					finalNode.licenseStatus = LicenseConstants.NODE_AGREED;
					writer.writeNode(finalNode);
					return 0;
				} else {
					finalNode.licenseStatus = LicenseConstants.NODE_SOME_AGREED;
					writer.writeNode(finalNode);
					return 1;
				}
			} else {
				finalNode = nodeList.get(0);
				return 2;
			}
		}
		
		return 0; // formerly "return licenseStatus"
	}
	
	/**
	 * Compares all versions contained in wayList.
	 * Analyzes the changes between them varying between outputType. 
	 * Returns the calculated licenseStatus of that way.
	 * @param wayList
	 * @param nodeLicenseStatus (will be used to calculate way statistics and to remove nodes from way's node references (for the case they are not agreed)
	 * @param nodePositions (will only be used if outputType == 4 to calculate the insert index of node references)
	 * @return 0, 1 or 2 giving information about license status. 3 if missing versions were encountered.
	 */
	public int compareWays(ArrayList<OsmWay> wayList, HashMap<Integer,Integer> nodeLicenseStatus, HashMap<Integer,Double[]> nodePositions){
		
		// detect missing way versions (e.g. due to limitations while creating history planet file extract)
		if (Integer.parseInt(wayList.get(wayList.size() - 1).attMap.get("version")) != wayList.size()){
			return 3;
		}
		
		OsmWay finalWay = null;
		
		switch (outputType){		
		case 1:
			// output is MOST CURRENT way + licenseStatus. 
			// license status will say whether there is an agreed version of that way and if that version has all nodes agreed
			// output is MOST CURRENT WAY.
			// licenseStatus tag contains:
			// 0 -> if ALL versions AGREED
			// 1 -> if version 1 did AGREE (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE
			// nodeLicenseStatus tag contains:
			// 0 -> if ALL nodes of last agreed way AGREED
			// 1 -> if SOME nodes of way version 1 AGREED
			// 2 -> if NO node of way version 1 AGREED
			// 3 -> if ALL nodes of ALL ways AGREED (licenseStatus==0)
			int wayListSize = wayList.size();
			finalWay = wayList.get(wayListSize - 1);
			
			// do not output those ways which have been deleted (last version's visible-attribute is false)
			if (finalWay.attMap.containsKey("visible")){
				if (finalWay.attMap.get("visible").equals("false")){
					return 3;
				}
			}
			
			// see how many versions of way agreed
			int agreeCount = 0;
			int nodeAgreeCount = 0;
			
			ArrayList<Integer> nodeStatusList = new ArrayList<Integer>();
						
			while (agreeCount < wayList.size() && wayList.get(agreeCount).agreed){
				OsmWay currentWay = wayList.get(agreeCount);
				
				int agreedNodes = 0;
				
				// iterate over this way's nodes to see if they all have licenseStatus < 2
				for (int i = 0; i < currentWay.nodeList.size(); i++){
					if (nodeLicenseStatus.get(currentWay.nodeList.get(i)) < 2){
						// node will stay in way's node-list
						agreedNodes++;
					}
				}
				
				if (agreedNodes < 2){
					// then less than 2 nodes agreed
					nodeStatusList.add(2);
				} else if (agreedNodes < currentWay.nodeList.size()){
					// then some referenced nodes agreed
					nodeStatusList.add(1);
				} else {
					// then all referenced nodes agreed
					nodeStatusList.add(0);
				}				
				
				if (agreedNodes == 0){
					nodeAgreeCount++;
				}
				agreeCount++;
			}
			
			if (agreeCount == 0){
				// v1 disagreed
				finalWay.licenseStatus = LicenseConstants.WAY_ALL_DISAGREED;
				writer.writeWay(finalWay);
				return 2;
			} else if (agreeCount < wayList.size()){
				// v1, but not all versions agreed
				finalWay.licenseStatus = LicenseConstants.WAY_SOME_AGREED;
				if (nodeStatusList.get(agreeCount - 1) == 0){
					// all nodes of last agreed version agreed
					finalWay.nodeLicenseStatus = LicenseConstants.NODE_AGREED;
				} else {
					// some nodes of last agreed version agreed
					if (nodeStatusList.get(agreeCount - 1) == 1){
						finalWay.nodeLicenseStatus = LicenseConstants.NODE_SOME_AGREED;
					} else {
						// less than 2 nodes of last agreed version agreed
						finalWay.nodeLicenseStatus = LicenseConstants.NODE_ALL_DISAGREED;
					}
				}
				writer.writeWay(finalWay);
				return 1;
			} else {
				// all versions agreed
				finalWay.licenseStatus = LicenseConstants.WAY_AGREED;
				if (nodeStatusList.get(agreeCount - 1) == 0){
					// all nodes of most current version agreed
					finalWay.nodeLicenseStatus = LicenseConstants.NODE_AGREED;
				} else {
					// not all nodes of most current version agreed
					if (nodeStatusList.get(agreeCount - 1) == 1){
						finalWay.nodeLicenseStatus = LicenseConstants.NODE_SOME_AGREED;
					} else {
						// less than 2 nodes most current version agreed
						finalWay.nodeLicenseStatus = LicenseConstants.NODE_ALL_DISAGREED;
					}
				}
				writer.writeWay(finalWay);
				return 0;
			}		
		case 2:
			// output is LAST AGREED VERSION of way + licenseStatus. Equivalent to implementation planned by OSM.
			// licenseStatus tag contains:
			// 0 -> if ALL versions AGREED
			// 1 -> if version 1 did AGREE (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE
			wayListSize = wayList.size();
			agreeCount = 0;
			for (int i = 0; i < wayListSize; i++){
				if (wayList.get(i).agreed){
					agreeCount++;
					finalWay = wayList.get(i);
				} else {
					break;
				}
			}
			
			// do not output those ways which have been deleted (last version's visible-attribute is false)
			if (agreeCount > 0){
				if (finalWay.attMap.containsKey("visible")){
					if (finalWay.attMap.get("visible").equals("false")){
						return 3;
					}
				}
			}
			
			if (agreeCount == 0){
				return 2;
			} else {
				finalWay = removeNonagreedNodeRefs(finalWay, nodeLicenseStatus);
				if (agreeCount < wayListSize){
					finalWay.licenseStatus = LicenseConstants.WAY_SOME_AGREED;
					if (finalWay.nodeList.size() > 1){
						writer.writeWay(finalWay);
					}
					return 1;
				} else {
					finalWay.licenseStatus = LicenseConstants.WAY_AGREED;
					if (finalWay.nodeList.size() > 1){
						writer.writeWay(finalWay);
					}
					return 0;
				}
			}
		case 3:
			// output MOST CURRENT VERSION of way + licenseStatus. It is checked if an edit is believed to be trivial by VersionChangeHandler.isTrivialNodeEdit(). 
			// licenseStatus tag contains:
			// 0 -> if version 1 AGREED and ALL versions PASSED isTrivialWayEdit()
			// 1 -> if version 1 AGREED but NOT ALL subsequent versions PASSED isTrivialWayEdit() (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE
			VersionChangeHandler changeHandler = new VersionChangeHandler(1);
			finalWay = wayList.get(wayList.size() - 1);
			wayListSize = wayList.size();
			
			// do not output those ways which have been deleted (last version's visible-attribute is false)
			if (finalWay.attMap.containsKey("visible")){
				if (finalWay.attMap.get("visible").equals("false")){
					return 3;
				}
			}		
			
			//System.out.println("finalWay: " + finalWay.getAttValue("version") + " , uid=" + finalWay.getAttValue("uid"));
			agreeCount = 0;
			if (wayList.get(0).agreed){
				agreeCount++;
				for (int i = 0; i < wayListSize - 1; i++){
					if (wayList.get(i+1).agreed){
						agreeCount++;
					} else {
						boolean isTrivialEdit = changeHandler.isTrivialWayEdit(wayList.get(i), wayList.get(i+1));
						if (isTrivialEdit){
							agreeCount++;
						} else {
							break;
						}
					}
				}
			}
			
			if (agreeCount == 0){
				finalWay.licenseStatus = LicenseConstants.WAY_ALL_DISAGREED;
				writer.writeWay(finalWay);
				return 2;
			} else if (agreeCount < wayListSize){
				finalWay.licenseStatus = LicenseConstants.WAY_SOME_AGREED;
				writer.writeWay(finalWay);
				return 1;
			} else {
				finalWay.licenseStatus = LicenseConstants.WAY_AGREED;
				writer.writeWay(finalWay);
				return 0;
			}
		
		case 4:
			// output is LAST PASSED VERSION of way + licenseStatus.
			// It is checked if an edit is believed to be trivial by using VersionChangeHandler.isTrivialWayEdit()
			// non-agreed/passed nodes are deleted from node list. way is only included if consisting of at least two nodes
			// 0 -> if version 1 AGREED and ALL versions PASSED isTrivialNodeEdit()
			// 1 -> if version 1 AGREED but NOT ALL subsequent versions PASSED isTrivialNodeEdit() (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE
			changeHandler = new VersionChangeHandler(1);
			finalWay = null;
			wayListSize = wayList.size();			
			
			agreeCount = 0;
			if (wayList.get(0).agreed){
				agreeCount++;
				finalWay = wayList.get(0);
				for (int i = 0; i < wayListSize - 1; i++){
					if (wayList.get(i+1).agreed){
						agreeCount++;
					} else {
						boolean isTrivialEdit = changeHandler.isTrivialWayEdit(wayList.get(i), wayList.get(i+1));
						if (isTrivialEdit){
							agreeCount++;
							finalWay = wayList.get(i+1);
						} else {
							break;
						}
					}
				}
			}
			
			// do not output those nodes which have been deleted (last version's visible-attribute is false)
			if (agreeCount > 0){
				if (finalWay.attMap.containsKey("visible")){
					if (finalWay.attMap.get("visible").equals("false")){
						return 3;
					}
				}
			}
			
			if (agreeCount == 0){
				// no single way agreed, so no way version will be in output
				return 2;
			} else {
				finalWay = removeNonagreedNodeRefs(finalWay, nodeLicenseStatus);
				if (agreeCount < wayListSize){
					finalWay.licenseStatus = LicenseConstants.WAY_SOME_AGREED;
					if (finalWay.nodeList.size() > 1){
						writer.writeWay(finalWay);
					}
					return 1;
				} else {
					finalWay.licenseStatus = LicenseConstants.WAY_AGREED;
					if (finalWay.nodeList.size() > 1){
						writer.writeWay(finalWay);
					}
					return 0;
				}
			}
		case 5:
			// output will be an ARTIFICIAL way + licenseStatus. Only changes from users who agreed will be in output.
			// 0 -> if ALL versions AGREED
			// 1 -> if version 1 did AGREE (and maybe subsequent versions)
			// 2 -> if version 1 did NOT AGREE 
			wayListSize = wayList.size();
			agreeCount = 0;
			
			changeHandler = new VersionChangeHandler();
			if (wayList.get(0).agreed){
				finalWay = wayList.get(0);
				agreeCount++;
				
				for (int i = 0; i < wayList.size() - 1; i++){
					if (wayList.get(i+1).agreed){
						agreeCount++;
					}
					finalWay = changeHandler.writeWayChanges(wayList.get(i), wayList.get(i+1), finalWay, nodePositions);
				}
				
				// do not output those ways which have been deleted (last version's visible-attribute is false)
				if (finalWay.attMap.containsKey("visible")){
					if (finalWay.attMap.get("visible").equals("false")){
						return 3;
					}
				}
								
				if (agreeCount < wayListSize){
					finalWay.licenseStatus = LicenseConstants.WAY_SOME_AGREED;
					finalWay = removeNonagreedNodeRefs(finalWay, nodeLicenseStatus);
					if (finalWay.nodeList.size() > 1){
						writer.writeWay(finalWay);
					}
					return 1;
				} else {
					finalWay.licenseStatus = LicenseConstants.WAY_AGREED;
					finalWay = removeNonagreedNodeRefs(finalWay, nodeLicenseStatus);
					if (finalWay.nodeList.size() > 1){
						writer.writeWay(finalWay);
					}
					return 0;
				}
			} else {
				return 2;
			}
		}
		return 0;
	}
	
	/**
	 * Runs through the way's nodeList and remove's all node references where licenseStatus has been calculated to be == 2
	 * @param way
	 * @param nodeLicenseStatus
	 * @return
	 */
	private OsmWay removeNonagreedNodeRefs(OsmWay way, HashMap<Integer,Integer> nodeLicenseStatus){
		for (int i = 0; i < way.nodeList.size(); i++){
			if (nodeLicenseStatus.get(way.nodeList.get(i)) == 2){
				way.nodeList.remove(i);
				i--;
			}
		}		
		return way;
	}
}
