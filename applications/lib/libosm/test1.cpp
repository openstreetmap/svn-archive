#include "Parser.h"
#include "Client.h"
#include "Node.h"
#include <iostream>
#include <fstream>
#include <sstream>

using namespace std;

void dotest(OSM::Components *comp1);

int main(int argc, char* argv[])
{
	if (argc < 4)
	{
		cerr << "Usage: test InOsmFile OsmUsername OsmPassword" << endl;
		exit(1);
	}

	ifstream in(argv[1]);
	OSM::Components *comp1 = OSM::Parser::parse(in);
	in.close();

	OSM::Client client("http://www.openstreetmap.org/api/0.5");
	client.setLoginDetails(argv[2], argv[3]);

	string osmData = client.grabOSM("map", -0.75, 51.02, -0.7, 51.07);

	istringstream sstream;
	sstream.str(osmData);

	OSM::Components *comp2 = OSM::Parser::parse(sstream);
	cout << "Testing components from API call:" << endl;
	dotest(comp2);
	cout << "Testing components from local file:" << endl;
	dotest(comp1);

	delete comp2;
	delete comp1;

	return 0;

}

void dotest(OSM::Components *comp1)
{
	comp1->rewindNodes();
	comp1->rewindWays();

	while (comp1->hasMoreNodes())
	{
		OSM::Node *n = comp1->nextNode();
		cout << "Node id: " << n->id() << " lat: " << n->getLat() << " lon: "
				<< n->getLon() << endl << "tags:" << endl;

		map<string, string>::const_iterator iter = n->tags().begin();
		for (; iter != n->tags().end(); ++iter)
		{
			cout << "Key: " << iter->first << " Value: " << iter->second
					<< endl;
		}
	}
	while (comp1->hasMoreWays())
	{
		OSM::Way *w = comp1->nextWay();
		cout << "Way id: " << w->id() << " tags:" << endl;

		map<string, string>::const_iterator iter = w->tags().begin();
		for (; iter != w->tags().end(); ++iter)
		{
			cout << "Key: " << iter->first << " Value: " << iter->second
					<< endl;
		}
	}
}
