#include "Parser.h"
#include "Client.h"
#include "Node.h"
#include <iostream>
#include <fstream>
#include <sstream>

using std::cout;
using std::cerr;
using std::endl;

void dotest(OSM::Components *comp1);

int main(int argc,char* argv[])
{
	if(argc != 2)
	{
		cerr<<"Usage: test InOsmFile > out.osm" << endl;
		exit(1);
	}
	
	std::ifstream in(argv[1]);
	OSM::Components *comp1 = OSM::Parser::parse(in);
	in.close();

	if (comp1 == NULL) {
		cerr << "Error occurred while parsing: " << argv[1] << endl;
		return 1;
	}
	
        comp1->toXML(std::cout);

	delete comp1;

	return 0;
}

void dotest(OSM::Components *comp1)
{
	comp1->rewindNodes();
	comp1->rewindSegments();
	comp1->rewindWays();

	while(comp1->hasMoreNodes())
	{
		OSM::Node *n = comp1->nextNode();
		cout << "Node id: " << n->id << " lat: " << n->getLat()
				<<" lon: " << n->getLon() << endl << "tags:" << endl;

		std::vector<std::string> keys = n->getTags();

		for(int count=0; count<keys.size(); count++)
		{
			cout  << "Key: " << keys[count] << " Value: " << 
				n->tags[keys[count]] << endl;
		}
	}
	while(comp1->hasMoreWays())
	{
		OSM::Way *w = comp1->nextWay();
		cout << "Way id: " << w->id << " tags:" << endl;

		std::vector<std::string> keys = w->getTags();

		for(int count=0; count<keys.size(); count++)
		{
			cout  << "Key: " << keys[count] << " Value: " << 
				w->tags[keys[count]] << endl;
		}
	}
}
