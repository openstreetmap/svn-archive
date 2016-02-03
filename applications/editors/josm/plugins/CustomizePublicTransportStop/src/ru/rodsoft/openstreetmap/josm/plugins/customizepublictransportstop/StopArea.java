package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

import java.util.ArrayList;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

/**
 * 
 * @author Rodion Scherbakov
 * Stop area settings
 */
public class StopArea {

	/**
	 * Name of stop area
	 */
	public String name = null;
	/**
	 * English name of stop area
	 */
	public String nameEn = null;
	/**
	 * Operator of stop area
	 */
	public String operator = null;
	/**
	 * Network name
	 */
	public String network = null;
	/**
	 * Level of network including this stop area
	 */
	public String service = null;
	/**
	 * Flag of bus stop area
	 */
	public Boolean isBus = false;
	/**
	 * Flag of trolleybus stop area
	 */
	public Boolean isTrolleybus = false;
	/**
	 * Flag of share taxi stop area
	 */
	public Boolean isShareTaxi = false;
	/**
	 * Flag of bus station stop area
	 */
	public Boolean isBusStation = false;
	/**
	 * Flag of tram stop area
	 */
	public Boolean isTram = false;
	/**
	 * Flag of railway stop area
	 */
	public Boolean isTrainStop = false;
	/**
	 * Flag of railway station
	 */
	public Boolean isTrainStation = false;
	/**
	 * Flag of bench on selected platform
	 */
	public Boolean isAssignTransportType = false;
	/**
	 * Flag of bench near platform
	 */
	public Boolean isBench = false;
	/**
	 * Flag of covered platform 
	 */
	public Boolean isCovered = false;
	/**
	 * Flag of shelter on selected platform
	 */
	public Boolean isShelter = false;
	/**
	 * Relation of stop area
	 */
	public Relation stopAreaRelation = null;
	/**
	 * Flag of existing of stop position
	 */
	public Boolean isStopPointExists = false;
	/**
	 * Flag of area platform
	 */
	public Boolean isArea = false;
	/**
	 * Separate node of bus stop or bus station
	 */
	public Node separateBusStopNode = null;
	
	/**
	 * List of nodes of stop positions
	 */
	public final ArrayList<Node> stopPoints = new ArrayList<Node>();
	/**
	 * List of josm objects of platforms
	 */
	public final ArrayList<OsmPrimitive> platforms = new ArrayList<OsmPrimitive>();
	/**
	 * List of non stop positions or platform stop area members
	 */
	public final ArrayList<OsmPrimitive> otherMembers = new ArrayList<OsmPrimitive>();
	
	/**
	 * Selected josm objects. Must be a platform
	 */
	public OsmPrimitive selectedObject = null;

	
	/**
	 * Constructor of stop area object
	 */
	public StopArea()
	{ }
	
	/**
	 * Constructor of stop area object from selected object
	 * @param selectedObject Selected object
	 */
	public StopArea(OsmPrimitive selectedObject)
	{
		this.selectedObject = selectedObject;
	}
	
	/**
	 * Get selected in editor node
	 * @return Selected node or null
	 */
	public Node getSelectedNode()
	{
		if(selectedObject instanceof Node)
			return (Node)selectedObject;
		return null;
	}
	
	/**
	 * Get selected way
	 * @return Selected way or null
	 */
	public Way getSelectedWay()
	{
		if(selectedObject instanceof Way)
			return (Way)selectedObject;
		return null;
	}
	
}