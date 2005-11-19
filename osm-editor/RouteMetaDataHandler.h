#ifndef PERMISSIONS_H
#define PERMISSIONS_H

#include <map>
#include <qstring.h>

namespace OpenStreetMap
{

struct RouteMetaData
{
	QString foot, 
		 horse, 
		 bike,
		 car;

	QString routeClass; 

	RouteMetaData() {foot=horse=bike=car="no"; routeClass="unknown";}
	RouteMetaData(QString f, QString b, QString h, QString c, QString cl )
		{ foot=f; horse=h; bike=b; car=c; routeClass = cl; }
	bool operator==(const RouteMetaData& md2)
		{ return foot==md2.foot && horse==md2.horse && bike==md2.bike 
				&& car==md2.car && routeClass==md2.routeClass; }
};

// Class for obtaining the permissions on a particular 
class RouteMetaDataHandler
{
private:
	std::map<QString,RouteMetaData> rData;

public:
	RouteMetaDataHandler();
	RouteMetaData getMetaData(const QString& type);
	QString getRouteType(const RouteMetaData &rData);
};

}
#endif
