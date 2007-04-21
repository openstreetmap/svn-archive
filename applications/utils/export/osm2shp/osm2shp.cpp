#include <libshp/shapefil.h>
#include "Node.h"
#include "Components.h"
#include "Parser.h"
#include "Way.h"
#include <vector>
#include <fstream>
#include <iostream>

using std::cerr;
using std::endl;


int main (int argc, char* argv[])
{
	if(argc<3)
	{
		cerr << "Usage: osm2shp OSMfile nodeSHPfile waySHPfile" << endl;
		exit(1);
	}

	std::ifstream in (argv[1]);

	if(in.good())
	{
		OSM::Components *comp = OSM::Parser::parse(in);
		in.close();
		if(comp)
		{
			comp->makeShp(argv[2],argv[3]);
			delete comp;
		}
		else
		{
			cerr << OSM::Parser::getError() << endl;
			exit(1);
		}
	}

	return 0;
}

