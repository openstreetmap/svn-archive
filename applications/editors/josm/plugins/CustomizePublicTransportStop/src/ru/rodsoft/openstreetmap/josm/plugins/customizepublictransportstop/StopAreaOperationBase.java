package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

/**
 * @author Rodion Scherbakov
 * Base class of operation of customizing of stop area
 */
public abstract class StopAreaOperationBase implements IStopAreaCustomizer 
{

	/**
	 * Current dataset of Josm
	 */
	private DataSet _CurrentDataSet;
	
	/**
	 * Constructor of operation of customizing of stop area
	 * @param currentDataSet Current data set of JOSM
	 */
	public StopAreaOperationBase(DataSet currentDataSet) 
	{
		_CurrentDataSet = currentDataSet;
	}

	/**
	 * Get current dataset of Josm
	 * Used for comparability wit previous version
	 * @return Current dataset of Josm
	 */
	protected DataSet getCurrentDataSet()
	{
		return _CurrentDataSet;
	}
	
	/**
	 * Perform operation of customizing of stop area
	 * @param stopArea Stop area object
	 * @return stopArea Resulting stop area object
	 */
	@Override
	public abstract StopArea performCustomizing(StopArea stopArea);

	/**
	 * Get tag value of JOSM object
	 * @param member JOSM object
	 * @param tagName Tag name
	 * @return Tag value
	 */
	public static String getTagValue(OsmPrimitive member, String tagName)
	{
		return member.getKeys().get(tagName);
	}
	
	/**
	 * Comparing of value of tag for josm object
	 * @param osmObject JOSM object
	 * @param tagName Tag name
	 * @param tagValue Tag value
	 * @return true, if tag exists and equals tagValue
	 */
	public static Boolean compareTag (OsmPrimitive osmObject, String tagName, String tagValue)
	{
		String value = osmObject.getKeys().get(tagName);
		if(value != null)
			return value.equals(tagValue);
		return false;
	}
	
	/**
	 * Assign tag value to JOSM object
	 * @param commands Command list
	 * @param target Target OSM object
	 * @param tag Tag name
	 * @param tagValue Tag value
	 * @return Resulting command list
	 */
	public static List<Command> assignTag(List<Command> commands, OsmPrimitive target, String tag, String tagValue)
	{
		if(commands == null)
			commands = new ArrayList<Command>();
		commands.add(new ChangePropertyCommand(target, tag, tagValue));
		return commands;
	}
	
	/**
	 * Clear tag value of JOSM object
	 * @param commands Command list
	 * @param target Target OSM object
	 * @param tag Tag name
	 * @return Resulting command list
	 */
	public static List<Command> clearTag(List<Command> commands, OsmPrimitive target, String tag)
	{
		return assignTag(commands, target, tag, null);
	}
	
    /**
     * Calculation of center of platform, if platform is way
     * @param platform Platform primitive
     * @return Coordinates of center of platform
     */
    public static LatLon getCenterOfWay(OsmPrimitive platform)
    {
		if(platform instanceof Way)
		{ 
			//p = mapView.getPoint((Node) stopArea.selectedObject);
			Double sumLat = 0.0;
			Double sumLon = 0.0;
			Integer countNode = 0;
			for(Node node : ((Way) platform).getNodes())
			{
				LatLon coord = node.getCoor();
				sumLat += coord.getX();
				sumLon += coord.getY();
				countNode++;
			}
			return new LatLon(sumLon / countNode, sumLat / countNode);		
		}
		return null;
    }


}
