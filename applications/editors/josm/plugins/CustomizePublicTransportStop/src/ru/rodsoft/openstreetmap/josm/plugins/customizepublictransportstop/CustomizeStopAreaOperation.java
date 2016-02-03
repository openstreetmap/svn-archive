package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

import java.util.ArrayList;
import java.util.List;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Operation of creation and customizing stop area relation and its members under user selection
 * @author Rodion Scherbakov
 */
public class CustomizeStopAreaOperation extends StopAreaOperationBase 
{

	/**
	 * Operation name in undo list
	 */
    private static final String TAG_ASSIGN_COMMAND_NAME = "Stop area tag assign";

	/**
	 * Constructor of operation object
	 * @param currentDataSet Current Josm data set
	 */
	public CustomizeStopAreaOperation(DataSet currentDataSet) 
	{
		super(currentDataSet);
	}

    /**
	 * Forming commands for josm for saving name and name:en attributes stop of area members and relation attributes
	 * @param target Stop area member or relation
	 * @param commands List of commands
	 * @param stopArea Stop area object
	 * @return Resulting list of commands
	 */
    public List<Command> nameTagAssign(OsmPrimitive target, List<Command> commands, StopArea stopArea)
    {
    	if(commands == null)
    		commands = new ArrayList<Command>();
    	
    	commands = assignTag(commands, target, OSMTags.NAME_TAG, "".equals(stopArea.name) ? null : stopArea.name);
    	commands = assignTag(commands, target, OSMTags.NAME_EN_TAG, "".equals(stopArea.nameEn) ? null : stopArea.nameEn);
    	return commands;
    }
	
    /**
     * Assign transport type tags to node
     * @param target Josm object for tag assigning 
     * @param commands Command list
	 * @param stopArea Stop area object
     * @param isStopPoint Flag of stop point
 	 * @return Resulting list of commands
    */
	protected List<Command> transportTypeTagAssign(OsmPrimitive target, List<Command> commands, StopArea stopArea, Boolean isStopPoint) 
	{
    	if(commands == null)
    		commands = new ArrayList<Command>();

    	if(isStopPoint)
		{
			if(stopArea.isTrainStop || stopArea.isTrainStation)
	    	{
				commands = clearTag(commands, target, OSMTags.BUS_TAG);
				commands = clearTag(commands, target, OSMTags.SHARE_TAXI_TAG);
				commands = clearTag(commands, target, OSMTags.TROLLEYBUS_TAG);
				commands = clearTag(commands, target, OSMTags.TRAM_TAG);
	    		commands = assignTag(commands, target, OSMTags.TRAIN_TAG, OSMTags.YES_TAG_VALUE);
	    	}
	    	else
	    	{
	    		commands = assignTag(commands, target, OSMTags.BUS_TAG, stopArea.isBus ? OSMTags.YES_TAG_VALUE : null);
	    		commands = assignTag(commands, target, OSMTags.SHARE_TAXI_TAG, stopArea.isShareTaxi ? OSMTags.YES_TAG_VALUE : null);
	    		commands = assignTag(commands, target, OSMTags.TROLLEYBUS_TAG, stopArea.isTrolleybus ? OSMTags.YES_TAG_VALUE : null);
	    		commands = assignTag(commands, target, OSMTags.TRAM_TAG, stopArea.isTram ? OSMTags.YES_TAG_VALUE : null);
	    		commands = assignTag(commands, target, OSMTags.TRAIN_TAG, stopArea.isTrainStation || stopArea.isTrainStop ? OSMTags.YES_TAG_VALUE : null);
	    	}
		}
		else
		{
			if(stopArea.isAssignTransportType)
	    	{
	    		commands = assignTag(commands, target, OSMTags.BUS_TAG, stopArea.isBus ? OSMTags.YES_TAG_VALUE : null);
	    		commands = assignTag(commands, target, OSMTags.SHARE_TAXI_TAG, stopArea.isShareTaxi ? OSMTags.YES_TAG_VALUE : null);
	    		commands = assignTag(commands, target, OSMTags.TROLLEYBUS_TAG, stopArea.isTrolleybus ? OSMTags.YES_TAG_VALUE : null);
	    		commands = assignTag(commands, target, OSMTags.TRAM_TAG, stopArea.isTram ? OSMTags.YES_TAG_VALUE : null);
	    		commands = assignTag(commands, target, OSMTags.TRAIN_TAG, stopArea.isTrainStation || stopArea.isTrainStop ? OSMTags.YES_TAG_VALUE : null);
	    	}
			else
			{
	    		commands = clearTag(commands, target, OSMTags.BUS_TAG);
	    		commands = clearTag(commands, target, OSMTags.SHARE_TAXI_TAG);
	    		commands = clearTag(commands, target, OSMTags.TROLLEYBUS_TAG);
	    		commands = clearTag(commands, target, OSMTags.TRAM_TAG);
	    		commands = clearTag(commands, target, OSMTags.TRAIN_TAG);    		
			}
		}
		return commands;
	}

	/**
	 * Forming commands for josm for saving general attributes of stop area members and relation 
	 * @param target Stop area member or relation
	 * @param commands List of commands
	 * @param stopArea Stop area object
     * @param isStopPoint Flag of stop point
	 * @return Resulting list of commands
	 */
    public List<Command> generalTagAssign(OsmPrimitive target, List<Command> commands, StopArea stopArea, Boolean isStopPoint)
    {
    	if(commands == null)
    		commands = new ArrayList<Command>();
    	
    	commands = nameTagAssign(target,commands, stopArea);
    	commands = assignTag(commands, target, OSMTags.NETWORK_TAG, "".equals(stopArea.network) ? null : stopArea.network);
    	commands = assignTag(commands, target, OSMTags.OPERATOR_TAG, "".equals(stopArea.operator) ? null : stopArea.operator);
    	commands = assignTag(commands, target, OSMTags.SERVICE_TAG, null == stopArea.service || OSMTags.CITY_NETWORK_TAG_VALUE.equals(stopArea.service) ? null : stopArea.service);
    	
    	commands = transportTypeTagAssign(target, commands, stopArea, isStopPoint);
    	return commands;
    }

    
    /**
     * Forming commands for josm for saving stop position attributes
     * @param target Stop position node
     * @param commands Original command list
	 * @param stopArea Stop area object
     * @param isFirst true, if target is first stop position in stop area
     * @return Resulting command list
     */
    public List<Command> stopPointTagAssign(OsmPrimitive target, List<Command> commands, StopArea stopArea, Boolean isFirst)
    {
    	if(commands == null)
    		commands = new ArrayList<Command>();
    	
    	commands = generalTagAssign(target, commands, stopArea, true);
    	if(isFirst)
    	{
    		if(stopArea.isTrainStop)
    		{
    			commands = assignTag(commands, target, OSMTags.RAILWAY_TAG, OSMTags.HALT_TAG_VALUE);
    		}
    		else
    			if(stopArea.isTrainStation)
    			{
    				commands = assignTag(commands, target, OSMTags.RAILWAY_TAG, OSMTags.STATION_TAG_VALUE);
    			}
    			else
    				if(stopArea.isTram)
    					commands = assignTag(commands, target, OSMTags.RAILWAY_TAG, OSMTags.TRAM_STOP_TAG_VALUE);
    				else
    					commands = clearTag(commands, target, OSMTags.RAILWAY_TAG);
    	}
    	else
    	{
    		commands = clearTag(commands, target, OSMTags.RAILWAY_TAG);
    	}
    	if(compareTag(target, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE))
    		commands = clearTag(commands, target, OSMTags.HIGHWAY_TAG);
    	if(compareTag(target, OSMTags.AMENITY_TAG, OSMTags.BUS_STATION_TAG_VALUE))
    		commands = clearTag(commands, target, OSMTags.AMENITY_TAG);
    	commands = assignTag(commands, target, OSMTags.PUBLIC_TRANSPORT_TAG, OSMTags.STOP_POSITION_TAG_VALUE);
    	return commands;		
    }
    
    /**
     * Forming commands for josm for saving platform attributes
     * @param target Platform node or way
     * @param commands Original command list
	 * @param stopArea Stop area object
     * @param isSelected true, if this platform is selected in editor
     * @param isFirst true, if this platform is first in stop area
     * @return Resulting command list
     */
    public List<Command> platformTagAssign(OsmPrimitive target, List<Command> commands, StopArea stopArea, Boolean isFirst)
    {
    	if(commands == null)
    		commands = new ArrayList<Command>();
    	
    	commands = generalTagAssign(target, commands, stopArea, false);
    	
    	if(compareTag(target, OSMTags.RAILWAY_TAG, OSMTags.HALT_TAG_VALUE) || compareTag(target, OSMTags.RAILWAY_TAG, OSMTags.STATION_TAG_VALUE))
    		commands = clearTag(commands, target, OSMTags.RAILWAY_TAG);
    	if(target instanceof Way && (stopArea.isTrainStop || stopArea.isTrainStation || stopArea.isTram))
    		commands = assignTag(commands, target, OSMTags.RAILWAY_TAG, OSMTags.PLATFORM_TAG_VALUE);
    	if(stopArea.isBus || stopArea.isShareTaxi || stopArea.isTrolleybus)
    	{
        	if(target instanceof Way)
        		commands = assignTag(commands, target, OSMTags.HIGHWAY_TAG, OSMTags.PLATFORM_TAG_VALUE);
        	else
        		if(isFirst && !stopArea.isBusStation)
        			commands = assignTag(commands, target, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE);
    	}
    	commands = assignTag(commands, target, OSMTags.PUBLIC_TRANSPORT_TAG, OSMTags.PLATFORM_TAG_VALUE);
        if(target == stopArea.selectedObject)
        {
        	commands = assignTag(commands, target, OSMTags.BENCH_TAG, stopArea.isBench ? OSMTags.YES_TAG_VALUE : null);
        	commands = assignTag(commands, target, OSMTags.SHELTER_TAG, stopArea.isShelter ? OSMTags.YES_TAG_VALUE : null);
        	commands = assignTag(commands, target, OSMTags.COVERED_TAG, stopArea.isCovered ? OSMTags.YES_TAG_VALUE : null);
        	commands = assignTag(commands, target, OSMTags.AREA_TAG, stopArea.isArea ? OSMTags.YES_TAG_VALUE : null);
        }
    	return commands;
    }
    
    /**
     * Forming commands for josm for saving attributes of non stop position or platform
     * @param target Member of stop area relation 
     * @param commands Original command list
	 * @param stopArea Stop area object
     * @return Resulting command list
     */
    public List<Command> otherMemberTagAssign(OsmPrimitive target, List<Command> commands, StopArea stopArea)
    {
    	if(commands == null)
    		commands = new ArrayList<Command>();
    	
    	commands = nameTagAssign(target, commands, stopArea);
    	commands = clearTag(commands, target, OSMTags.NETWORK_TAG);
    	commands = clearTag(commands, target, OSMTags.OPERATOR_TAG);
    	commands = clearTag(commands, target, OSMTags.SERVICE_TAG);
    	if(compareTag(target, OSMTags.RAILWAY_TAG, OSMTags.HALT_TAG_VALUE) || compareTag(target, OSMTags.RAILWAY_TAG, OSMTags.STATION_TAG_VALUE))
    		commands = clearTag(commands, target, OSMTags.RAILWAY_TAG);
    	return commands;
    } 	
    	
    /**
     * Forming commands for josm for saving stop area relation attributes
     * @param commands Original command list
	 * @param stopArea Stop area object
     * @return Resulting command list
     */
	private List<Command> createStopAreaRelation(List<Command> commands, StopArea stopArea) 
	{
    	if(commands == null)
    		commands = new ArrayList<Command>();

    	Relation newRelation = new Relation();
		for(Node node : stopArea.stopPoints)
		{
			newRelation.addMember(new RelationMember(OSMTags.STOP_ROLE, node));
		}
		for(OsmPrimitive platform : stopArea.platforms)
		{
			newRelation.addMember(new RelationMember(OSMTags.PLATFORM_ROLE, platform));
		}
		for(OsmPrimitive otherMember : stopArea.otherMembers)
		{
			newRelation.addMember(new RelationMember("", otherMember));
		}
		Main.main.undoRedo.add(new AddCommand(newRelation));
		commands = generalTagAssign(newRelation, commands, stopArea, false);
		commands = assignTag(commands, newRelation, OSMTags.TYPE_TAG, OSMTags.PUBLIC_TRANSPORT_TAG);
		commands = assignTag(commands, newRelation, OSMTags.PUBLIC_TRANSPORT_TAG, OSMTags.STOP_AREA_TAG_VALUE);
    	return commands;
	}

	/**
	 * Adding of new stop area members to relation
	 * @param commands Original command list
	 * @param targetRelation Stop area relation
	 * @param member Stop area relation member
	 * @param roleName Role name
	 * @return Resulting command list
	 */
	public static List<Command> addNewRelationMember(List<Command> commands, Relation targetRelation, OsmPrimitive member, String roleName)
	{
    	if(commands == null)
    		commands = new ArrayList<Command>();

    	for(RelationMember relationMember : targetRelation.getMembers())
		{
			if(relationMember.getMember() == member)
			{
				if(relationMember.getRole() == roleName)
				{
					return commands;
				}
				return commands;
			}
		}
		targetRelation.addMember(new RelationMember(roleName, member));
		commands.add(new ChangeCommand(targetRelation, targetRelation));
		return commands;	
	}
	
	/**
	 * Adding new stop area members to relation
	 * @param commands Original command list
	 * @param stopArea Stop area object
	 * @return Resulting command list
	 */
	private List<Command> addNewRelationMembers(List<Command> commands, StopArea stopArea) 
	{
    	if(commands == null)
    		commands = new ArrayList<Command>();

    	for(OsmPrimitive stopPoint : stopArea.stopPoints)
		{
			commands = addNewRelationMember(commands, stopArea.stopAreaRelation, stopPoint, OSMTags.STOP_ROLE);
		}
		for(OsmPrimitive platform : stopArea.platforms)
		{
			commands = addNewRelationMember(commands, stopArea.stopAreaRelation, platform, OSMTags.PLATFORM_ROLE);
		}
		for(OsmPrimitive otherMember : stopArea.otherMembers)
		{
			commands = addNewRelationMember(commands, stopArea.stopAreaRelation, otherMember, null);
		}
		return commands;
	}

	/**
	 * Testing, is josm object bus stop node or contains bus stop node, defined by tag and its value
	 * @param member Josm object
	 * @param tag Tag name
	 * @param tagValue Tag value
	 * @return true, if josm object is bus stop node or contains bus stop node
	 */
	private Node searchBusStop(OsmPrimitive member, String tag, String tagValue)
	{
		if(member instanceof Node)
		{			
			if(compareTag(member, tag, tagValue))
			{
				return (Node)member;
			}
		}
		else
		{
			Way memberWay = (Way) member;
			for(Node node : memberWay.getNodes())
			{
				if(compareTag(node, tag, tagValue))
				{
					return node;
				}					
			}
		}
		return null;
	}

	/**
	 * Testing, do stop area contains bus stop node, defined by tag and its value
	 * @param tag Tag name
	 * @param tagValue Tag value
	 * @return true, if stop area contains bus stop node
	 */
	public Node searchBusStop(StopArea stopArea, String tag, String tagValue) 
	{
		for(OsmPrimitive platform : stopArea.platforms)
		{
			Node busStopNode = searchBusStop(platform, tag, tagValue);
			if(busStopNode != null)
				return busStopNode;
		}
		for(OsmPrimitive otherMember : stopArea.otherMembers)
		{
			Node busStopNode = searchBusStop(otherMember, tag, tagValue);
			if(busStopNode != null)
				return busStopNode;
		}
		return null;
	}

	/**
	 * Testing, must stop area to have separate bus stop node
	 * @param stopArea Stop area object
	 * @param firstPlatform First platform of stop area JOSM object
	 * @return True, stop area must to have separate bus stop node
	 */
	public boolean needSeparateBusStop(StopArea stopArea, OsmPrimitive firstPlatform)
	{
		if(((stopArea.isBus || stopArea.isShareTaxi || stopArea.isTrolleybus) && (firstPlatform instanceof Way)))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Clear excess tags from JOSM object and its nodes
	 * @param commands Original command list
	 * @param target JOSM object
	 * @param tag Tag name
	 * @param tagValue Tag value
	 * @return Resulting command list
	 */
	private List<Command> clearExcessTags(List<Command> commands, OsmPrimitive target, String tag, String tagValue)
	{
    	if(commands == null)
    		commands = new ArrayList<Command>();

    	if(compareTag(target, tag, tagValue))
		{
			commands = clearTag(commands, target, tag);
		}
		if(target instanceof Way)
		{
			Way memberWay = (Way) target;
			for(Node node : memberWay.getNodes())
			{
				if(compareTag(node, tag, tagValue))
				{
					commands = clearTag(commands, target, tag);
				}					
			}
		}
		return commands;
	}

	/**
	 * Clear excess tags from JOSM object and its nodes
	 * @param commands Command list
	 * @param stopArea Stop area object
	 * @param tag Tag name
	 * @param tagValue Tag value
	 * @return Resulting command list
	 */
	public List<Command> clearExcessTags(List<Command> commands, StopArea stopArea, String tag, String tagValue)
	{
    	if(commands == null)
    		commands = new ArrayList<Command>();

    	for(OsmPrimitive stopPoint : stopArea.stopPoints)
		{
			clearExcessTags(commands, stopPoint, tag, tagValue);
		}
		for(OsmPrimitive platform : stopArea.platforms)
		{
			clearExcessTags(commands, platform, tag, tagValue);
		}
		for(OsmPrimitive otherMember : stopArea.otherMembers)
		{
			clearExcessTags(commands, otherMember, tag, tagValue);
		}
		return commands;
 	}

	
	/**
	 * Create separate bus stop node or assign bus stop tag to platform node
	 * @param commands Original command list
	 * @param stopArea Stop area object
	 * @param firstPlatform First platform in stop area relation
	 * @param tag Tag name
	 * @param tagValue Tag value
	 * @return Resulting command list
	 */
	protected List<Command> createSeparateBusStopNode(List<Command> commands, StopArea stopArea, OsmPrimitive firstPlatform, String tag, String tagValue)
	{
    	if(commands == null)
    		commands = new ArrayList<Command>();

    	LatLon centerOfPlatform = getCenterOfWay(firstPlatform);
		if(firstPlatform instanceof Way)
		{
			if(centerOfPlatform != null)
			{
				Node newNode =new Node();
				newNode.setCoor(centerOfPlatform);
		    	Main.main.undoRedo.add(new AddCommand(newNode));
		    	Main.main.undoRedo.add(new ChangePropertyCommand(newNode, tag, tagValue));
				commands = assignTag(commands, newNode, tag, tagValue);
				stopArea.otherMembers.add(newNode);
			}
		}
		else
		{
    		commands = assignTag(commands, firstPlatform, tag, tagValue);
		}
		return commands;
	}

	/**
	 * Forming commands for JOSM for saving stop area members and relation attributes
	 * @param stopArea Stop area object
	 * @return Resulting command list
	 */
	public List<Command> customize(StopArea stopArea)
	{
		try
		{
			List<Command> commands = new ArrayList<Command>();
			Node separateBusStopNode = searchBusStop(stopArea, OSMTags.AMENITY_TAG, OSMTags.BUS_STATION_TAG_VALUE);
			if(separateBusStopNode == null)
				separateBusStopNode = searchBusStop(stopArea, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE);
			if(stopArea.isBusStation)
			{
				commands = clearExcessTags(commands, stopArea, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE);
			}
			else
			{
				commands = clearExcessTags(commands, stopArea, OSMTags.AMENITY_TAG, OSMTags.BUS_STATION_TAG_VALUE);
			}
			if(!(stopArea.isBus || stopArea.isShareTaxi || stopArea.isTrolleybus))
			{
				commands = clearExcessTags(commands, stopArea, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE);
			}
			if(stopArea.stopPoints.size() == 0)
			{
				CreateNewStopPointOperation createNewStopPointOperation = new CreateNewStopPointOperation(getCurrentDataSet());
				createNewStopPointOperation.performCustomizing(stopArea);
			}
			Boolean isFirst = true;
			for(Node node : stopArea.stopPoints)
			{
				commands = stopPointTagAssign(node, commands, stopArea, isFirst);
				isFirst = false;
			}
			isFirst = true;
			OsmPrimitive firstPlatform = null;
			for(OsmPrimitive platform : stopArea.platforms)
			{
				commands = platformTagAssign(platform, commands, stopArea, isFirst);
				if(isFirst)
					firstPlatform = platform;
				isFirst = false;
			}
			if(needSeparateBusStop(stopArea, firstPlatform))
			{
				if(stopArea.isBusStation)
				{
					if(separateBusStopNode == null)
					{
						commands = createSeparateBusStopNode(commands, stopArea, firstPlatform, OSMTags.AMENITY_TAG, OSMTags.BUS_STATION_TAG_VALUE);
					}	
					else
					{
						commands = assignTag(commands, separateBusStopNode, OSMTags.AMENITY_TAG, OSMTags.BUS_STATION_TAG_VALUE);
					}
				}
				else
				{
					if(separateBusStopNode == null)
					{
						commands = createSeparateBusStopNode(commands, stopArea, firstPlatform, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE);
					}	
					else
					{
						commands = assignTag(commands, separateBusStopNode, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE);
					}
				}
			}
			else
			{
				if(stopArea.isBusStation)
				{
					commands = assignTag(commands, firstPlatform, OSMTags.AMENITY_TAG, OSMTags.BUS_STATION_TAG_VALUE);
				}
				else
					if(stopArea.isBus || stopArea.isTrolleybus || stopArea.isShareTaxi)
					{
						commands = assignTag(commands, firstPlatform, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE);
					}
			}
			for(OsmPrimitive otherMember : stopArea.otherMembers)
			{
				commands = otherMemberTagAssign(otherMember, commands, stopArea);
			}
			if(stopArea.stopAreaRelation == null)
			{
				if(stopArea.stopPoints.size() + stopArea.platforms.size() + stopArea.otherMembers.size() > 1)
				{
					commands = createStopAreaRelation(commands, stopArea);
				}
			}
			else
			{
				commands = generalTagAssign(stopArea.stopAreaRelation, commands, stopArea, false);
				commands = addNewRelationMembers(commands, stopArea);
			}
			return commands;
		}
		catch(Exception ex)
		{
			MessageBox.ok(ex.getMessage());
		}
		return null;
	}

	/**
	 * Construct and executing command list for customizing of stop area relation and its members
	 * @param stopArea Stop area object
	 * @return Stop area object
	 */
	@Override
	public StopArea performCustomizing(StopArea stopArea) 
	{
		List<Command> commands = customize(stopArea);
		if(commands != null && !commands.isEmpty())
			try
			{
				Main.main.undoRedo.add(new SequenceCommand(tr(TAG_ASSIGN_COMMAND_NAME), commands));
				return stopArea;
			}
			catch(Exception ex)
			{
			}
		return stopArea;
	}

}
