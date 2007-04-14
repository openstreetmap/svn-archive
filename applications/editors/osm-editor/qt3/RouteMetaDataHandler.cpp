#include "RouteMetaDataHandler.h"
#include <qstringlist.h>

#include <iostream>
using std::cout;
using std::cerr;
using std::endl;

namespace OpenStreetMap
{

// Tests an incoming set of matadata against the required criteria
bool RouteMetaData::testmatch(const RouteMetaData& indata)
{ 
	return testmatch(foot,indata.foot) && testmatch(horse,indata.horse)
				&& testmatch(bike,indata.bike) && testmatch(car,indata.car)
				&& testmatch(routeClass,indata.routeClass);
}

// Tests an incoming aspect of the metadata (foot, horse etc) against the 
// required criteria. 
// Note that allowed values are separated with a | 
// Note that a * means match anything
bool RouteMetaData::testmatch(const QString& requiredCriteria, 
								const QString& indata)
{
	if(requiredCriteria!="*")
	{
		QStringList allowed = QStringList::split("|",requiredCriteria);
		for(int count=0; count<allowed.size(); count++)
		{
			if(indata==allowed[count]) 
			{
				return true;
			}
		}
		return false;
	}
	return true;
}

// Returns the 'preferred' meta data i.e. the first of the list of allowed
// values e.g. footway rather than path. 
RouteMetaData RouteMetaData::preferred()
{
	return RouteMetaData(preferred(foot),preferred(bike),
							preferred(horse),preferred(car),
							preferred(routeClass) );
}

// Parse an OSM key/value pair
// 130506 reads new style keys such as 'bicycle' and 'highway'
void RouteMetaData::parseKV(const QString &key, const QString& value)
{
	//cerr << "Trying key " << key << " value " << value << endl;
	if(key == "foot")
		foot = value;
	else if(key == "horse")
		horse = value;
	// prevent conflicting 'bicycle' and 'bike' keys
	else if(key == "bike" || key == "bicycle")
	{
		if(key=="bicycle")
		{
			doneBicycle=true;
			bike = value;
		}
		else if(!doneBicycle)
		{
			bike=value;
		}
	}
	// same with 'motorcar' and 'car'
	else if(key == "car" || key == "motorcar")
	{
		if(key=="motorcar")
		{
			doneMotorcar=true;
			car = value;
		}
		else if (!doneMotorcar)
		{
			car = value;
		}
	}
	// same with 'class' and 'highway'
	else if(key == "class" || key == "highway")
	{
		if(key=="highway")
		{
			doneHighway=true;
			routeClass = value;
		}
		else if (!doneHighway)
		{
			routeClass=value;
		}
	}

	else if (key=="railway")
		railway = value;
}

// Returns the preferred value for a particular property (foot etc)
// This will be the first in the list.
QString RouteMetaData::preferred(const QString& property)
{
	QStringList allowed=QStringList::split("|",property);
	return allowed[0];
}

RouteMetaDataHandler::RouteMetaDataHandler()
{
	rData["footpath"] = RouteMetaData ("yes","no", "no", "no",   
							"footway|path");
	rData["path"] = RouteMetaData ("unknown","unknown", "unknown", 
						"no",   "footway|path");
	rData["permissive footpath"]=RouteMetaData ("permissive|unofficial", 
												"no", "no", "no",
												  "footway|path");
	rData["bridleway"] = RouteMetaData ("yes", "yes", "yes", "no",  
						"bridleway|path"); 
	rData["permissive bridleway"] = RouteMetaData ("permissive|unofficial", 
													"no",
													"permissive|unofficial", 
												 "no",  "bridleway|path");
	rData["cycle path"] = RouteMetaData ("permissive|unofficial",  
											"permissive|unofficial", "no",
										"no", "cycleway|path");
	rData["byway"] = RouteMetaData ("yes", "yes", "yes", "yes",  "unsurfaced");
	rData["minor road"] = RouteMetaData ("yes", "yes", "yes", "yes",  
								"unclassified|minor");
	rData["residential road"] = RouteMetaData ("yes", "yes", "yes", "yes",  
												"residential");
	rData["B road"] = RouteMetaData ("yes", "yes", "yes", "yes",  "secondary");
	rData["A road"] = RouteMetaData ("yes", "yes", "yes", "yes",  "primary");
	rData["motorway"] = RouteMetaData ("no", "no", "no", "yes",  "motorway");
	rData["railway"] = RouteMetaData ("no", "no", "no", "no",  "", "rail");
	rData["new forest track"] = RouteMetaData ("permissive|unofficial", 
												"no", "permissive|unofficial", 
												"no",  
												"unsurfaced"); 
	rData["new forest cycle path"] = RouteMetaData ("permissive|unofficial", 
												"permissive|unofficial", 
												"permissive|unofficial", "no",  
												"unsurfaced"); 
}

// Returns the metadata matching a type
// Note that the PREFERRED values will be returned (e.g. footway rather than
// path); this will be useful if we wish to write out the data.
RouteMetaData RouteMetaDataHandler::getMetaData(const QString& type) 
{
	std::map<QString,RouteMetaData>::iterator i = rData.find(type);
	if (i==rData.end())
		return RouteMetaData("no","no","no","no","");
	return i->second.preferred();
}


QString RouteMetaDataHandler::getRouteType(const RouteMetaData &rd) 
{
		/*
	cerr << "Trying RouteMetaData: foot=" << rd.foot << 
			" horse=" << rd.horse << " bike=" << rd.bike << " car=" << rd.car
			<< " routelcass=" << rd.routeClass <<endl;
			*/
	for(std::map<QString,RouteMetaData>::iterator i=rData.begin();
		i!=rData.end(); i++)
	{
		if (i->second.testmatch(rd))
			return i->first;
	}
	return "track";
}

}
