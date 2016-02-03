package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;

/**
 * 
 * @author Rodion Scherbakov
 * Operation of constructing of stop area object from selected JOSM object
 */
public class CreateStopAreaFromSelectedObjectOperation extends
		StopAreaOperationBase 
{

	/**
	 * Constructor of operation object
	 * @param currentDataSet Current Josm data set
	 */
	public CreateStopAreaFromSelectedObjectOperation(DataSet currentDataSet) 
	{
		super(currentDataSet);
	}

	/**
	 * Parsing of josm object tags and customizing of stop area object
	 * @param member Josm object
	 */
	public void parseTags(StopArea stopArea, OsmPrimitive member)
	{
		if(stopArea.name == null)
			stopArea.name = getTagValue(member, OSMTags.NAME_TAG);
		if(stopArea.nameEn == null)
			stopArea.nameEn = getTagValue(member, OSMTags.NAME_EN_TAG);
		if(stopArea.operator == null)
			stopArea.operator = getTagValue(member, OSMTags.OPERATOR_TAG);
		if(stopArea.network == null)
			stopArea.network = getTagValue(member, OSMTags.NETWORK_TAG);
		if(stopArea.service == null)
			stopArea.service = getTagValue(member, OSMTags.SERVICE_TAG);
		if(compareTag(member, OSMTags.BUS_TAG, OSMTags.YES_TAG_VALUE))
			stopArea.isBus = true;
		if(compareTag(member, OSMTags.TROLLEYBUS_TAG, OSMTags.YES_TAG_VALUE))
			stopArea.isTrolleybus = true;
		if(compareTag(member, OSMTags.SHARE_TAXI_TAG, OSMTags.YES_TAG_VALUE))
			stopArea.isShareTaxi = true;
		if(!(stopArea.isBus || stopArea.isShareTaxi || stopArea.isTrolleybus) && compareTag(member, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE))
			stopArea.isBus = true;
		if(compareTag(member, OSMTags.TRAM_TAG, OSMTags.YES_TAG_VALUE) || compareTag(member, OSMTags.RAILWAY_TAG, OSMTags.TRAM_STOP_TAG_VALUE))
			stopArea.isTram = true;
		if(compareTag(member, OSMTags.RAILWAY_TAG, OSMTags.HALT_TAG_VALUE))
			stopArea.isTrainStop = true;
		if(compareTag(member, OSMTags.RAILWAY_TAG, OSMTags.STATION_TAG_VALUE))
			stopArea.isTrainStation = true;
		if(compareTag(member, OSMTags.AMENITY_TAG, OSMTags.BUS_STATION_TAG_VALUE))
		{
			stopArea.isBusStation = true;
		}
		if(member == stopArea.selectedObject)
		{
			if(compareTag(member, OSMTags.BENCH_TAG, OSMTags.YES_TAG_VALUE))
				stopArea.isBench = true;
			if(compareTag(member, OSMTags.SHELTER_TAG, OSMTags.YES_TAG_VALUE))
				stopArea.isShelter = true;
			if(compareTag(member, OSMTags.COVERED_TAG, OSMTags.YES_TAG_VALUE))
				stopArea.isCovered = true;
			if(compareTag(member, OSMTags.AREA_TAG, OSMTags.YES_TAG_VALUE))
				stopArea.isArea = true;
		}
	}
	
	/**
	 * Test, are transport types assigned to platforms
	 * @param platform Platform object
	 * @return true, if transport types assigned to this platforms
	 */
	public boolean testIsTransportTypeAssigned(OsmPrimitive platform)
	{
		String[] transportTypes = new String[] { OSMTags.BUS_TAG, OSMTags.TROLLEYBUS_TAG, OSMTags.SHARE_TAXI_TAG, OSMTags.TRAM_TAG, OSMTags.TRAIN_TAG };
		for(String transportType : transportTypes)
		{
			if(compareTag(platform, transportType, OSMTags.YES_TAG_VALUE))
				return true;			
		}
		return false;
	}
	
	/**
	 * Setting of stop area from selected josm object
	 * @param selectedObject Selected josm object
	 */
	public void fromSelectedObject(StopArea stopArea)
	{
		Collection<OsmPrimitive> selectedObjects = new ArrayList<OsmPrimitive>();
		selectedObjects.add(stopArea.selectedObject);
		for(Relation rel : OsmPrimitive.getParentRelations(selectedObjects))
		{
			if(compareTag(rel, OSMTags.TYPE_TAG, OSMTags.PUBLIC_TRANSPORT_TAG) && compareTag(rel, OSMTags.PUBLIC_TRANSPORT_TAG, OSMTags.STOP_AREA_TAG_VALUE))
			{
				stopArea.stopAreaRelation = rel;
			}
			if(stopArea.stopAreaRelation != null)
				break;
		}
		
		if(stopArea.stopAreaRelation != null)
		{
			parseTags(stopArea, stopArea.stopAreaRelation);
			parseTags(stopArea, stopArea.selectedObject);
			for(RelationMember member : stopArea.stopAreaRelation.getMembers())
			{				
				if(member.getMember() instanceof Node && compareTag(member.getMember(), OSMTags.PUBLIC_TRANSPORT_TAG, OSMTags.STOP_POSITION_TAG_VALUE))
				{
					stopArea.isStopPointExists = true;
					stopArea.stopPoints.add(member.getNode());
				}
				else
					if(compareTag(member.getMember(), OSMTags.PUBLIC_TRANSPORT_TAG, OSMTags.PLATFORM_TAG_VALUE))
					{
						stopArea.platforms.add(member.getMember());
					}
					else
					{
						stopArea.otherMembers.add(member.getMember());
					}
				parseTags(stopArea, member.getMember());
			}
			if(!stopArea.platforms.contains(stopArea.selectedObject))
			{
				parseTags(stopArea, stopArea.selectedObject);
				stopArea.platforms.add(stopArea.selectedObject);
			}
		}
		else
		{
			parseTags(stopArea, stopArea.selectedObject);
			stopArea.platforms.add(stopArea.selectedObject);
		}
		for(OsmPrimitive platform : stopArea.platforms)
		{
			if(testIsTransportTypeAssigned(platform))
			{
				stopArea.isAssignTransportType = true;
				break;
			}
		}
		if(!(stopArea.isBus || stopArea.isTrolleybus || stopArea.isShareTaxi) && stopArea.selectedObject != null && (compareTag(stopArea.selectedObject, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE) || stopArea.isBusStation))
		{
			stopArea.isBus = true;
		}		
	}

	/**
	 * Construct stop area object from selected JOSM object
	 * @param stopArea Original stop area object
	 * @return Stop area objects with settings of selected JOSM object and included it stop area relation
	 */
	@Override
	public StopArea performCustomizing(StopArea stopArea) 
	{
		if(getCurrentDataSet() == null)
			return null;
		OsmPrimitive selectedObject = getCurrentDataSet().getSelectedNodesAndWays().iterator().next();
		if(selectedObject == null)
			return null;
		if(stopArea == null)
			stopArea = new StopArea(selectedObject);
		fromSelectedObject(stopArea);
		return stopArea;
	}

}
