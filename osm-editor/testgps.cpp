#include <iostream>
#include <cstdlib>
#include "GPSDevice.h"

using namespace std;
using namespace FreeMapper;

int main()
{
	GPSDevice device ("Garmin", "/dev/ttyS0");
	Track* t = device.getTrack();

	for(int count=0; count<t->size(); count++)
	{
		cout << "timestamp "<< (*t)[count].timestamp
			 << " " << (*t)[count].lat << " " << (*t)[count].lon << endl;
	}

	vector<Waypoint>* w = device.getWaypoints();
	for(int count=0; count<w->size(); count++)
	{
		cout << "type "<< (*w)[count].type
			 << " " << (*w)[count].lat << " " << (*w)[count].lon << 
				" " << (*w)[count].name << endl;
	}

	delete w;
	delete t;

	return 0;
}
