#include "Polygon.h"
#include <iostream>
using std::endl;

namespace OpenStreetMap
{

void Polygon::toGPX(std::ostream& outfile)
{
	outfile << "<extensions>" << endl;
	outfile << "<polygon>" << endl << "<type>"<<
			type<<"</type>"<<endl;

	for(vector<EarthPoint>::iterator i=points.begin(); i!=points.end(); i++)
	{
		outfile << "<polypt lat=\"" << i->y << "\" lon=\"" 
				<< i->x << "\" />" << endl;
	}

	outfile << "</polygon>" << endl << "</extensions>" << endl;
}

}
