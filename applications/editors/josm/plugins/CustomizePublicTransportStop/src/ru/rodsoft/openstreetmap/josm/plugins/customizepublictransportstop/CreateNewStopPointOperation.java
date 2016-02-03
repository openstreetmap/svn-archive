package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.tools.Geometry;

import ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop.OSMTags;

/**
 * 
 * @author Rodion Scherbakov
 * Operation of creation of new stop point
 */
public class CreateNewStopPointOperation extends StopAreaOperationBase 
{

	/**
	 * Constructor of operation object
	 * @param currentDataSet Current Josm data set
	 */
	public CreateNewStopPointOperation(DataSet currentDataSet)
	{
		super(currentDataSet);
	}
	
    /**
     * The *result* does not depend on the current map selection state,
     * neither does the result *order*.
     * It solely depends on the distance to point p.
     *
     * This code is coped from JOSM code
     * 
     * @return a sorted map with the keys representing the distance of
     *      their associated nodes to point p.
     */
    private Map<Double, List<Node>> getNearestNodesImpl(Point p) {
        TreeMap<Double, List<Node>> nearestMap = new TreeMap<>();
        DataSet ds = getCurrentDataSet();

        if (ds != null) {
            double dist, snapDistanceSq = 200;
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, 200))) {
                if ((dist = Main.map.mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq)
                {
                    List<Node> nlist;
                    if (nearestMap.containsKey(dist)) {
                        nlist = nearestMap.get(dist);
                    } else {
                        nlist = new LinkedList<>();
                        nearestMap.put(dist, nlist);
                    }
                    nlist.add(n);
                }
            }
        }

        return nearestMap;
    }

    /**
     * Selection of area for search of roads
     * @param p Current point
     * @param snapDistance Distance for search
     * @return Area
     */
    private BBox getBBox(Point p, int snapDistance) {
        return new BBox(Main.map.mapView.getLatLon(p.x - snapDistance, p.y - snapDistance),
        		Main.map.mapView.getLatLon(p.x + snapDistance, p.y + snapDistance));
    }

    /**
     * Search of nearest points on ways 
     * @param platformCoord Platform coordinates 
     * @param stopArea Stop area object
     * @return Dictionary of founded points and distances from platform
     */
    public AbstractMap.SimpleEntry<Double, Node> getNearestNode(LatLon platformCoord, StopArea stopArea)
    {
    	Point p = Main.map.mapView.getPoint(platformCoord);
    	Map<Double, List<Node>> dist_nodes = getNearestNodesImpl(p);
    	Double[] distances = dist_nodes.keySet().toArray(new Double[0]);
    	distances = sort(distances);
    	Integer distanceIndex = -1;
    	Node nearestNode = null;
    	while(++distanceIndex < distances.length && nearestNode == null)
    	{
    		List<Node> nodes = dist_nodes.get(distances[distanceIndex]);
    		for(Node node : nodes)
    		{
    			for(Way way : getCurrentDataSet().getWays())
    			{
    				if(way.getNodes().contains(node) && testWay(way, stopArea))
    				{
    					nearestNode = node;
    					break;
    				}
    			}
    			if(nearestNode != null)
    				break;
    		}
    	}
    	if(nearestNode == null)
    		return null;
    	return new AbstractMap.SimpleEntry<Double, Node> (distances[--distanceIndex], nearestNode);
    }

    /**
     * Sorting of founded points by distance
     * @param distances Array of distances
     * @return Sorted array of distances
     */
    public Double[] sort(Double[] distances)
    {
    	for(Integer i = 0; i < distances.length - 1; i++)
    	for(Integer j = i + 1; j < distances.length; j++)
    	{
    		if(distances[i]>distances[j])
    		{
    			Double d = distances[i];
    			distances[i] = distances[j];
    			distances[j] = d;
    		}
    	}
    	return distances;
    }
    
    /**
     * Selection of ways for stop position by type of way and type of stop
     * @param way The way
     * @param stopArea Stop area
     * @return true, if way can contain stop position
     */
    public Boolean testWay(Way way, StopArea stopArea)
    {
    	if(stopArea.isTrainStation || stopArea.isTrainStop)
    	{
    		if(OSMTags.RAIL_TAG_VALUE.equals(way.getKeys().get(OSMTags.RAILWAY_TAG)) && OSMTags.MAIN_TAG_VALUE.equals(way.getKeys().get(OSMTags.USAGE_TAG)))
    			return true;
    		return false;
    	}

    	if(stopArea.isTram)
    	{
    		if(OSMTags.TRAM_TAG_VALUE.equals(way.getKeys().get(OSMTags.RAILWAY_TAG)))
    			return true;
    		return false;
    	}
    	
    	String[] highwayValues = {OSMTags.TRUNK_TAG_VALUE, OSMTags.PRIMARY_TAG_VALUE, OSMTags.SECONDARY_TAG_VALUE, OSMTags.TERTIARY_TAG_VALUE,
    			OSMTags.UNCLASSIFIED_TAG_VALUE, OSMTags.RESIDENTIAL_TAG_VALUE, OSMTags.SERVICE_TAG_VALUE,
    			OSMTags.BUS_GUIDEWAY_TAG_VALUE, OSMTags.ROAD_TAG_VALUE, OSMTags.TRUNK_LINK_TAG_VALUE, 
    			OSMTags.PRIMARY_LINK_TAG_VALUE, OSMTags.SECONDARY_LINK_TAG_VALUE, OSMTags.TERTIARY_LINK_TAG_VALUE };
    	if(stopArea.isBus || stopArea.isTrolleybus || stopArea.isShareTaxi)
    	{
    		String highway = way.getKeys().get(OSMTags.HIGHWAY_TAG);
    		if(highway != null)
    		for(Integer i = 0; i < highwayValues.length; i++)
    		{
    			if(highwayValues[i].equals(highway))
    				return true;
    		}
    	}
    	return false;
    }
    
    /**
     * The *result* does not depend on the current map selection state,
     * neither does the result *order*.
     * It solely depends on the distance to point p.
     *
     * This code is coped from JOSM code
     * 
     * @return a sorted map with the keys representing the perpendicular
     *      distance of their associated way segments to point p.
     */
    private Map<Double, List<WaySegment>> getNearestWaySegmentsImpl(Point p) {
        Map<Double, List<WaySegment>> nearestMap = new TreeMap<>();
        DataSet ds = getCurrentDataSet();

        if (ds != null) {
            double snapDistanceSq = Main.pref.getInteger("mappaint.segment.snap-distance", 200);
            snapDistanceSq *= snapDistanceSq;

            for (Way w : ds.searchWays(getBBox(p, Main.pref.getInteger("mappaint.segment.snap-distance", 200)))) {
                Node lastN = null;
                int i = -2;
                for (Node n : w.getNodes()) {
                    i++;
                    if (n.isDeleted() || n.isIncomplete()) { //FIXME: This shouldn't happen, raise exception?
                        continue;
                    }
                    if (lastN == null) {
                        lastN = n;
                        continue;
                    }

                    Point2D A = Main.map.mapView.getPoint2D(lastN);
                    Point2D B = Main.map.mapView.getPoint2D(n);
                    double c = A.distanceSq(B);
                    double a = p.distanceSq(B);
                    double b = p.distanceSq(A);

                    /* perpendicular distance squared
                     * loose some precision to account for possible deviations in the calculation above
                     * e.g. if identical (A and B) come about reversed in another way, values may differ
                     * -- zero out least significant 32 dual digits of mantissa..
                     */
                    double perDistSq = Double.longBitsToDouble(
                            Double.doubleToLongBits( a - (a - b + c) * (a - b + c) / 4 / c )
                            >> 32 << 32); // resolution in numbers with large exponent not needed here..

                    if (perDistSq < snapDistanceSq && a < c + snapDistanceSq && b < c + snapDistanceSq) {
                        List<WaySegment> wslist;
                        if (nearestMap.containsKey(perDistSq)) {
                            wslist = nearestMap.get(perDistSq);
                        } else {
                            wslist = new LinkedList<>();
                            nearestMap.put(perDistSq, wslist);
                        }
                        wslist.add(new WaySegment(w, i));
                    }

                    lastN = n;
                }
            }
        }

        return nearestMap;
    }
    
    /**
     * Selection of nearest way for stop position
     * @param platformCoord Platform coordinates
     * @param stopArea Stop area
     * @return Nearest way segment
     */
    protected NearestWaySegment getNearestWaySegment(LatLon platformCoord, StopArea stopArea)
    {
    	
    	Point p = Main.map.mapView.getPoint(platformCoord);
    	Map<Double, List<WaySegment>> dist_waySegments = getNearestWaySegmentsImpl(p);
    	for(Map.Entry<Double, List<WaySegment>> entry : dist_waySegments.entrySet())
    	{
    		for(WaySegment waySegment : entry.getValue())
    		{
    			if(testWay(waySegment.way, stopArea))
    			{
    				Node n = waySegment.getFirstNode();
    				Node lastN = waySegment.getSecondNode();
    		
            		EastNorth newPosition = Geometry.closestPointToSegment(n.getEastNorth(),
              			 lastN.getEastNorth(), Projections.project(platformCoord));
            		LatLon newNodePosition = Projections.inverseProject(newPosition);
                	Point2D lastN2D = Main.map.mapView.getPoint2D(lastN);
                	Point2D n2D = Main.map.mapView.getPoint2D(n);
            		Point2D newNodePosition2D = Main.map.mapView.getPoint2D(newNodePosition);
            		Double distCurrenNodes =lastN2D.distance(n2D); 
            		if((newNodePosition2D.distance(lastN2D) < distCurrenNodes) && (newNodePosition2D.distance(n2D) < distCurrenNodes))
            		{
        				return new NearestWaySegment(entry.getKey(), waySegment, new Node(newNodePosition));
            		}
    			}
    		}
    	}
    	return null;
    }

    /**
     * Creation of stop position node on nearest way
     * @param newStopNode New stop position node
     * @param waySegment Way segment including stop position node
     * @return Stop position node
     */
    protected Node createNodeOnWay(Node newStopNode, WaySegment waySegment)
    {
    	Main.main.undoRedo.add(new AddCommand(newStopNode));
    	List<Node> wayNodes = waySegment.way.getNodes();
    	wayNodes.add(waySegment.lowerIndex + 1, newStopNode); 
    	Way newWay = new Way(waySegment.way);
    	newWay.setNodes(wayNodes);
    	Main.main.undoRedo.add(new ChangeCommand(waySegment.way, newWay));
    	return newStopNode;
    }

    /**
     * Creation of stop position
     * @param stopArea Stop Area
     */
    @Override
    public StopArea performCustomizing(StopArea stopArea)
    {
		LatLon platformCoord = null;
		if(stopArea.selectedObject instanceof Node)
		{ 
			platformCoord = ((Node) stopArea.selectedObject).getCoor();
		}
		else
			platformCoord = getCenterOfWay(stopArea.selectedObject);
		if(platformCoord == null)
			return stopArea;
		AbstractMap.SimpleEntry<Double, Node> nearestNode = getNearestNode(platformCoord, stopArea);
		NearestWaySegment nearestWaySegment = getNearestWaySegment(platformCoord, stopArea);
		Node newStopPointNode = null;
		if(nearestNode != null && nearestWaySegment != null)
		{
			if(nearestWaySegment.distanceSq < nearestNode.getKey())
			{
				newStopPointNode = createNodeOnWay(nearestWaySegment.newNode, nearestWaySegment.waySegment);
			}
			else
			{
				newStopPointNode = nearestNode.getValue();
			}
		}
		else
			if(nearestNode != null && nearestWaySegment == null)
			{
				newStopPointNode = nearestNode.getValue();
			}
			else
				if(nearestNode == null && nearestWaySegment != null)
				{
					newStopPointNode = createNodeOnWay(nearestWaySegment.newNode, nearestWaySegment.waySegment);
				}
		if(newStopPointNode != null)
		{
			stopArea.stopPoints.add(newStopPointNode);
		}
		return stopArea;
	}
    
}
