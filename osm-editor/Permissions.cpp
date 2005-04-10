#include "Permissions.h"

#include <iostream>
using std::cout;
using std::cerr;
using std::endl;

namespace OpenStreetMap
{

Permissions::Permissions()
{
	permData["footpath"] = PermData (true, false, false, 0, false);
	permData["cycle path"] = PermData (true, true, false, 0, false);
	permData["bridleway"] = PermData (true, true, true, 0, false);
	permData["byway"] = PermData (true, true, true, 1, false);
	permData["minor road"] = PermData (true, true, true, 2, false);
	permData["B road"] = PermData (true, true, true, 3, false);
	permData["A road"] = PermData (true, true, true, 4, false);
	permData["motorway"] = PermData (false, false, false, 5, false);
	permData["railway"] = PermData (false, false, false, 0, true);
}

bool Permissions::accessibleToFoot(const QString& type) throw(QString)
{
	std::map<QString,PermData>::iterator i = permData.find(type);
	if (i==permData.end())
		throw QString("Unknown segment type: " + type); 
	return i->second.foot;
}

bool Permissions::accessibleToBike(const QString& type) throw(QString)
{
	std::map<QString,PermData>::iterator i = permData.find(type);
	if (i==permData.end())
		throw QString("Unknown segment type: " + type); 
	return i->second.bike;
}

bool Permissions::accessibleToHorse(const QString& type) throw(QString)
{
	std::map<QString,PermData>::iterator i = permData.find(type);
	if (i==permData.end())
		throw QString("Unknown segment type: " + type); 
	return i->second.horse;
}

int Permissions::accessibleToCars(const QString& type) throw(QString)
{
	std::map<QString,PermData>::iterator i = permData.find(type);
	if (i==permData.end())
		throw QString("Unknown segment type: " + type); 
	return i->second.cars;
}

bool Permissions::isRailway(const QString& type) throw(QString)
{
	std::map<QString,PermData>::iterator i = permData.find(type);
	if (i==permData.end())
		throw QString("Unknown segment type: " + type); 
	return i->second.rail;
}

}
