package de.altenstein.osm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class HistoryParser {
	
	// define member variables
	
	XMLStreamReader reader;
	VersionComparator comp;
	OsmXmlWriter writer;
	// nodeList will hold all nodes of the same id. 
	// Once a new id is encountered, nodeList will be emptied to hold nodes with the new id.
	// Same for waysList.
	ArrayList<OsmNode> nodeList = new ArrayList<OsmNode>();
	ArrayList<OsmWay> wayList = new ArrayList<OsmWay>();
	AgreeList list;
	
	// HashMap nodeLicenseStatus contains all node IDs as keys and their license status as defined by LicenseConstants
	// nodePositions contains all IDs as keys and a Double[] array containing their lat and lon values (will only be filled if outputType == 4)
	HashMap<Integer,Integer> nodeLicenseStatus = new HashMap<Integer,Integer>();
	HashMap<Integer,Double[]> nodePositions = new HashMap<Integer,Double[]>();
	
	int outputType;
	
	// statistics counters
	int totalNodeCount = 0;
	int[] nodeLicenseCount = new int[4];
	int totalNodeVersionCount = 0;
	int totalWayCount = 0;
	int[] wayLicenseCount = new int[4];	
	int totalWayVersionCount = 0;
	/**
	 * Constructor for HistoryParser.
	 * Creates a XMLStreamReader object pointing onto file given by filename.
	 * AgreeList should include all id's which agreed to license change.
	 * @param inputFilename
	 * @param agreeListFilename
	 * @param outputFilename
	 * @param outputType
	 */
	public HistoryParser(String inputFilename, String agreeListFilename, String outputFilename, int outputType){
		writer = new OsmXmlWriter(outputFilename);
		comp = new VersionComparator(writer, outputType);
		this.outputType = outputType;
		try{
			File file = new File(inputFilename);
			FileInputStream inputStream = new FileInputStream(file);
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			reader = inputFactory.createXMLStreamReader(inputStream);
			list = new AgreeList(agreeListFilename);
			System.out.println(list.getStatistics());
		} catch (FileNotFoundException e){
			System.out.println("OSM History Planet File was not found: " + inputFilename);
			e.printStackTrace();
		} catch (XMLStreamException e){
			System.out.println("Error while parsing OSM file. Please check if OSM History Planet File is well-formatted.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses all nodes of the given document.
	 * After collecting all nodes of one id, they will be compared to each other by calling compareNodes()
	 * After all nodes are parsed, reader will be pointing onto the first way element in document
	 */
	public void parseNodes(){
		String lastID = "0";
		try {
			while (reader.hasNext()){
				int type = reader.next();
				
				switch (type){				
				case XMLStreamConstants.START_ELEMENT:
					String localName = reader.getLocalName();
					if (localName == "node"){
						String id = reader.getAttributeValue(null,"id");
						
						if (!lastID.equals(id)){
							// if true, a node with a new id is present, so nodes with previous id will be compared by calling compareNodes()
							if (nodeList.size() > 0){
								compareNodes();
							}
							
							// nodeList cleared as nodes have been compared and nodeList is prepared to hold nodes with a different id
							lastID = id;
							nodeList.clear();
						}
						
						// new OsmNode object will be created and added to nodeList
						OsmNode node = new OsmNode(reader, list);
						nodeList.add(node);
						
					} else if (localName == "way"){
						// if true, all nodes have been processed 
						// last nodes will be compared and function parseNodes() will return
						compareNodes();
						System.out.println("##### All nodes have been parsed.");
						return;
					}
					break;
					
				case XMLStreamConstants.END_DOCUMENT:
					// if document does only contain nodes (no ways/relations) END_DOCUMENT means that all nodes have been parsed
					compareNodes();
					System.out.println("##### End of document");
					finalStatistics();
					break;
				
				default:
					break;
				}
			}
					
		} catch (XMLStreamException e) {
			System.err.println("Error while parsing nodes. Document well-formatted?");
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses all ways of the given document.
	 * After collecting all ways of one id, they will be compared to each other by calling compareWays()
	 * After all ways are parsed, reader will be pointing onto the first relation element in document or end of document is reached
	 */
	public void parseWays(){
		String lastID = "0";
		boolean first = true;
		try {
			while (reader.hasNext()){
				int type;
				if (first && reader.getLocalName() == "way"){
					first = false;
					type = reader.getEventType();
				} else {
					type = reader.next();
				}
				switch (type){				
				case XMLStreamConstants.START_ELEMENT:
					String localName = reader.getLocalName();
					if (localName == "way"){
						String id = reader.getAttributeValue(null,"id");
						
						if (!lastID.equals(id)){
							// if true, a way with a new id is present, so ways with previous id will be compared by calling compareWays()
							if (wayList.size() > 0){
								compareWays();
							}							
							// wayList cleared as ways have been compared and wayList is prepared to hold ways with a different id
							lastID = id;
							wayList.clear();
						}
						
						// new OsmWay object will be created and added to wayList
						OsmWay way = new OsmWay(reader, list);
						wayList.add(way);
						
					} else if (localName == "relation"){
						// if true, all ways have been processed 
						// last ways will be compared and function parseWays() will return
						compareWays();
						System.out.println("##### All ways have been parsed.");
						finalStatistics();
						return;
					}
					break;
					
				case XMLStreamConstants.END_DOCUMENT:
					// if document does not contain relations, END_DOCUMENT means that all ways have been parsed
					compareWays();
					System.out.println("##### End of document");
					break;
				
				default:
					break;
				}
			}
					
		} catch (XMLStreamException e) {
			System.err.println("Error while parsing ways. Document well-formatted?");
			e.printStackTrace();
		}
		finalStatistics();
	}
	
	/**
	 * Causes writer object to close the document and the corresponding file stream.
	 * Then outputs statistics about parsed/calculated license status.
	 */
	private void finalStatistics(){
		writer.closeDoc();
		// output count statistics
		System.out.println("____________________\nParsing, calculating and writing output completed." +
				"\nAnalysis was done using outputType " + outputType +
				
				"\nTotal node count: " + totalNodeCount +
				"\nAverage versions per node: " + (double)totalNodeVersionCount/totalNodeCount +
				"\nNodes with license status 0: " + nodeLicenseCount[0] + " (" + (double)nodeLicenseCount[0]/totalNodeCount*100 + " %)" +
				"\nNodes with license status 1: " + nodeLicenseCount[1] + " (" + (double)nodeLicenseCount[1]/totalNodeCount*100 + " %)" +
				"\nNodes with license status 2: " + nodeLicenseCount[2] + " (" + (double)nodeLicenseCount[2]/totalNodeCount*100 + " %)" +
				"\nNodes excluded from analysis: " + nodeLicenseCount[3] + " (" + (double)nodeLicenseCount[3]/totalNodeCount*100 + " %)" +
				"\nTotal way count: " + totalWayCount +
				"\nAverage versions per way: " + (double)totalWayVersionCount/totalWayCount +
				"\nWays with license status 0: " + wayLicenseCount[0] + " (" + (double)wayLicenseCount[0]/totalWayCount*100 + " %)" +
				"\nWays with license status 1: " + wayLicenseCount[1] + " (" + (double)wayLicenseCount[1]/totalWayCount*100 + " %)" +
				"\nWays with license status 2: " + wayLicenseCount[2] + " (" + (double)wayLicenseCount[2]/totalWayCount*100 + " %)" +
				"\nWays excluded from analysis: " + wayLicenseCount[3] + " (" + (double)wayLicenseCount[3]/totalWayCount*100 + " %)");
	}
	
	/**
	 * comp.compareNodes() lets VersionComparator compare the nodes included in nodeList.
	 * Adds the returned node's calculated licenseStatus together with his id to HashMap<Integer,Integer> nodeLicenseStatus.
	 * If outputType = 5 adds the node's position to HashMap<Integer,Double[]> nodePositions. 
	 * Additionally increments statistics counters.
	 */
	private void compareNodes(){
		int licenseStatus = comp.compareNodes(nodeList);
		int id = Integer.parseInt(nodeList.get(0).getAttValue("id"));
		if (licenseStatus <= 2){
			nodeLicenseStatus.put(id,licenseStatus);
		} else {
			nodeLicenseStatus.put(id, 2);
		}
		if (outputType == 5){
			nodePositions.put(id, new Double[]{Double.parseDouble(comp.finalNode.attMap.get("lat")),Double.parseDouble(comp.finalNode.attMap.get("lon"))});
		}
		// count statistics
		totalNodeCount++;
		totalNodeVersionCount += nodeList.size();
		nodeLicenseCount[licenseStatus]++;
	}
	
	/**
	 * comp.compareWays() lets VersionComparator compare the ways included in wayList.
	 * Additionally increments statistics counters.
	 */
	private void compareWays(){
		int licenseStatus = comp.compareWays(wayList, nodeLicenseStatus, nodePositions);
		// count statistics
		totalWayCount++;
		totalWayVersionCount += wayList.size();
		wayLicenseCount[licenseStatus]++;
	}
}