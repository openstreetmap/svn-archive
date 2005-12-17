#include "RouteMetaDataHandler.h"

#include <iostream>
using std::cout;
using std::cerr;
using std::endl;

namespace OpenStreetMap
{

// route classes (provisional):
// 1=unsurfaced path; 2=unsurfaced track; 3=surfaced (town) path; 
// 4=country lane; 5=residential road; 
// 6=unclassified town road; 7=secondary road; 8=primary road; 9=motorway;
// 16=railway.
//
// Permissions can be "yes", "no" or "unofficial"

RouteMetaDataHandler::RouteMetaDataHandler()
{
	rData["footpath"] = RouteMetaData ("yes","no", "no", "no",   "path");
	rData["permissive footpath"]=RouteMetaData ("unofficial", "no", "no", "no",
												  "path");
	rData["bridleway"] = RouteMetaData ("yes", "yes", "yes", "no",  "path"); 
	rData["permissive bridleway"] = RouteMetaData ("unofficial", "unofficial", 
												 "no", "no",  "path");
	rData["cycle path"] = RouteMetaData ("unofficial",  "unofficial", "no",
										"no", "path");
	rData["byway"] = RouteMetaData ("yes", "yes", "yes", "yes",  "unsurfaced");
	rData["minor road"] = RouteMetaData ("yes", "yes", "yes", "yes",  "minor");
	rData["residential road"] = RouteMetaData ("yes", "yes", "yes", "yes",  
												"residential");
	rData["B road"] = RouteMetaData ("yes", "yes", "yes", "yes",  "secondary");
	rData["A road"] = RouteMetaData ("yes", "yes", "yes", "yes",  "primary");
	rData["motorway"] = RouteMetaData ("no", "no", "no", "yes",  "motorway");
	rData["railway"] = RouteMetaData ("no", "no", "no", "no",  "railway");
	rData["new forest track"] = RouteMetaData ("yes", "no", "yes", "no",  
												"unsurfaced"); 
	rData["new forest cycle path"] = RouteMetaData ("yes", "yes", "yes", "no",  
												"unsurfaced"); 
}

RouteMetaData RouteMetaDataHandler::getMetaData(const QString& type) 
{
	std::map<QString,RouteMetaData>::iterator i = rData.find(type);
	if (i==rData.end())
		return RouteMetaData("no","no","no","no","");
	return i->second;
}

QString RouteMetaDataHandler::getRouteType(const RouteMetaData &rd) 
{
	for(std::map<QString,RouteMetaData>::iterator i=rData.begin();
		i!=rData.end(); i++)
	{
		if (i->second==rd)
			return i->first;
	}
	return "track";
}
}
