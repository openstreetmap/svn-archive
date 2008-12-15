#include "Parser.h"
#include "Client.h"
#include "Node.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include "FeatureClassification.h"
#include "FeaturesParser.h"

using std::cout;
using std::cerr;
using std::endl;

void dotest(OSM::Components *comp1, OSM::FeatureClassification*);

int main(int argc,char* argv[])
{
	if(argc<3)
	{
		cerr<<"Usage: test InOsmFile FeaturesFile" << endl;
		exit(1);
	}
	
	std::ifstream in(argv[1]);
	cerr<<"parsing osm"<<endl;
	OSM::Components *comp1 = OSM::Parser::parse(in);
	in.close();
	cerr<<"done"<<endl;
	cerr<<"parsing rulesfile"<<endl;
	std::ifstream in2(argv[2]);
	OSM::FeatureClassification *classification=OSM::FeaturesParser::parse(in2);
	cerr<<"done"<<endl;
	in2.close();
	if(comp1) 
		cerr<<"comp1 exists"<<endl;
	if(classification) 
		cerr<<"classification exists"<<endl;
	if(comp1&&classification)
	{
		dotest(comp1,classification);
		delete classification;
		delete comp1;
	}

	return 0;

}

void dotest(OSM::Components *comp1, OSM::FeatureClassification* classification)
{
	comp1->rewindNodes();
	comp1->rewindWays();

	while(comp1->hasMoreWays())
	{
		OSM::Way *w = comp1->nextWay();
		cout << endl << "Way id: " << w->id() << " tags:" << endl;

		std::vector<std::string> keys = w->getTags();

		for(int count=0; count<keys.size(); count++)
		{
			cout  << "Key: " << keys[count] << " Value: " << 
				w->tags()[keys[count]] << endl;
		}
		cout << endl << "Info from featureClassification:" << endl;
		cout << "Feature class: " << classification->getFeatureClass(w)
									 <<endl<<endl;
	}
}
