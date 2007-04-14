#ifndef PERMISSIONS_H
#define PERMISSIONS_H

#include <map>
#include <qstring.h>

namespace OpenStreetMap
{

class RouteMetaData
{
	public:
	QString foot, 
		 horse, 
		 bike,
		 car;

	QString routeClass; 
	QString railway;

	bool doneBicycle, doneMotorcar, doneHighway;

	RouteMetaData() {foot=horse=bike=car="no"; routeClass="unknown"; 
						railway="";doneBicycle=doneMotorcar=doneHighway=false; }
	RouteMetaData(QString f, QString b, QString h, QString c, QString cl,
					QString r="")
		{ foot=f; horse=h; bike=b; car=c; routeClass = cl; railway=r;
	      doneBicycle=doneMotorcar=doneHighway=false;	}
	bool testmatch(const RouteMetaData& indata);
	bool testmatch(const QString& requiredCriteria, const QString& indata);
	RouteMetaData preferred();
	QString preferred(const QString& property);
	void parseKV(const QString&, const QString&);
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
