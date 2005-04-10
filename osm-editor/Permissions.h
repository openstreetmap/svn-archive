#ifndef PERMISSIONS_H
#define PERMISSIONS_H

#include <map>
#include <qstring.h>

namespace OpenStreetMap
{

struct PermData
{
	bool foot, 
		 horse, 
		 bike,
		 rail;

	int cars; 

	PermData() {}
	PermData(bool f, bool b, bool h, int c, bool r )
		{ foot=f; horse=h; bike=b; cars=c; rail = r;}
};

// Class for obtaining the permissions on a particular 
class Permissions
{
private:
	std::map<QString,PermData> permData;

public:
	Permissions();
	bool accessibleToFoot(const QString& type) throw(QString);
	bool accessibleToHorse(const QString& type) throw(QString);
	bool accessibleToBike(const QString& type) throw(QString);
	int accessibleToCars(const QString& type) throw(QString);
	bool isRailway(const QString& type) throw(QString);
};

}
#endif
