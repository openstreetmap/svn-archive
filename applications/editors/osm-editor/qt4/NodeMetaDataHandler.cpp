#include "NodeMetaDataHandler.h"
#include <qstringlist.h>

#include <iostream>
using std::cout;
using std::cerr;
using std::endl;

namespace OpenStreetMap
{

// Tests an incoming set of matadata against the required criteria
bool NodeMetaData::testmatch(const NodeMetaData& indata)
{ 
	// 090706 if the key is class and the value is the specified value
	// that also counts as a match
	return (key==indata.key && value==indata.value) ||
			(key=="class" && value==indata.value);
}

NodeMetaDataHandler::NodeMetaDataHandler()
{
	nData["pub"] = NodeMetaData("amenity","pub");
	//nData["church"] = NodeMetaData("amenity","church");
	nData["church"] = NodeMetaData("amenity","place_of_worship");
	nData["viewpoint"] = NodeMetaData ("tourism","viewpoint");
	nData["hill"] = NodeMetaData("natural","peak");
	nData["farm"] = NodeMetaData("residence","farm");
	nData["hamlet"] = NodeMetaData("place","hamlet");
	nData["village"] = NodeMetaData("place","village");
	nData["small town"] = NodeMetaData("place","small town");
	nData["large town"] = NodeMetaData("place","town");
	nData["city"] = NodeMetaData("place","city");
	nData["railway station"] = NodeMetaData("railway","station");
	nData["car park"] = NodeMetaData("amenity","parking");
	nData["mast"] = NodeMetaData("man_made","mast");
	nData["point of interest"] = NodeMetaData("leisure","point_of_interest");
	nData["suburb"] = NodeMetaData("place","suburb");
	nData["waypoint"] = NodeMetaData("waypoint","waypoint");
	nData["campsite"] = NodeMetaData("tourism","camp_site");
	nData["restaurant"] = NodeMetaData("amenity","restaurant");
	nData["tea shop"] = NodeMetaData("amenity","tea shop");
	nData["bridge"] = NodeMetaData("highway","bridge");
	nData["barn"] = NodeMetaData("man_made","barn");
	nData["country park"] = NodeMetaData("leisure","country_park");

	// Put area stuff in here too. This is a thoroughly nasty fudge; plan is to
	// rename from NodeMetaData to something else. Well, the plan is actually
	// to completely revise the mapping of high-level types to Map Features
	// tags. Oh, and consign class to the wastebin of history, too. :-)
	nData["wood"] = NodeMetaData("landuse","wood");
	nData["heath"] = NodeMetaData("natural","heath");
	nData["lake"] = NodeMetaData("natural","water");
	nData["park"] = NodeMetaData("leisure","park");
}

// Returns the metadata matching a node type
NodeMetaData NodeMetaDataHandler::getMetaData(const QString& type) 
{
	std::map<QString,NodeMetaData>::iterator i = nData.find(type);
	if (i==nData.end())
		return NodeMetaData();
	return i->second;
}

// Returns the node type matching node meta data
QString NodeMetaDataHandler::getNodeType(const QString& k,const QString &v) 
{
	NodeMetaData nd(k,v);

	for(std::map<QString,NodeMetaData>::iterator i=nData.begin();
				   	i!=nData.end(); i++)
	{
		if (i->second.testmatch(nd))
			return i->first;
	}
	return "node";
}

bool NodeMetaDataHandler::keyExists(const QString& k)
{
	for(std::map<QString,NodeMetaData>::iterator i=nData.begin();
		i!=nData.end(); i++)
	{
		if (i->second.key == k)
			return true; 
	}
	return false;
}

}
