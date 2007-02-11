#include "Parser.h"
#include "Client.h"
#include "Node.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include "ElemStyles.h"
#include "LineElemStyle.h"
#include "AreaElemStyle.h"
#include "IconElemStyle.h"
#include "RulesParser.h"

using std::cout;
using std::cerr;
using std::endl;

void dotest(OSM::Components *comp1, OSM::ElemStyles*);

int main(int argc,char* argv[])
{
	if(argc<3)
	{
		cerr<<"Usage: test InOsmFile RulesFile" << endl;
		exit(1);
	}
	
	std::ifstream in(argv[1]);
	cerr<<"parsing osm"<<endl;
	OSM::Components *comp1 = OSM::Parser::parse(in);
	in.close();
	cerr<<"done"<<endl;
	cerr<<"parsig ruelsfiel"<<endl;
	std::ifstream in2(argv[2]);
	OSM::ElemStyles *elemStyles = OSM::RulesParser::parse(in2);
	cerr<<"done"<<endl;
	in2.close();
	if(comp1) 
		cerr<<"comp1 exists"<<endl;
	if(elemStyles) 
		cerr<<"elemStyles exists"<<endl;
	if(comp1&&elemStyles)
	{
		dotest(comp1,elemStyles);
		delete elemStyles;
		delete comp1;
	}

	return 0;

}

void dotest(OSM::Components *comp1, OSM::ElemStyles* elemStyles)
{
	comp1->rewindNodes();
	comp1->rewindSegments();
	comp1->rewindWays();

	while(comp1->hasMoreNodes())
	{
		OSM::Node *n = comp1->nextNode();
		cout << endl << "Node id: " << n->id << " lat: " << n->getLat()
				<<" lon: " << n->getLon() << endl << "tags:" << endl;

		std::vector<std::string> keys = n->getTags();

		for(int count=0; count<keys.size(); count++)
		{
			cout  << "Key: " << keys[count] << " Value: " << 
				n->tags[keys[count]] << endl;
		}

		cout << endl << "Info from elemstyles:" << endl;
		OSM::ElemStyle * style = elemStyles->getStyle(n);
		if(style)
		{
			if(style->getFeatureClass()=="point")
			{
				OSM::IconElemStyle *i = (OSM::IconElemStyle*) style;
				cout << "Icon: " << i->getIcon()  << endl;
			}
		}
	}
	while(comp1->hasMoreWays())
	{
		OSM::Way *w = comp1->nextWay();
		cout << endl << "Way id: " << w->id << " tags:" << endl;

		std::vector<std::string> keys = w->getTags();

		for(int count=0; count<keys.size(); count++)
		{
			cout  << "Key: " << keys[count] << " Value: " << 
				w->tags[keys[count]] << endl;
		}
		cout << endl << "Info from elemstyles:" << endl;
		OSM::ElemStyle * style = elemStyles->getStyle(w);
		cout << "Feature class: " << elemStyles->getFeatureClass(w);
		if(style)
		{
			if(style->getFeatureClass()=="polyline")
			{
				OSM::LineElemStyle *l = (OSM::LineElemStyle*) style;
				cout << "Width: " << l->getWidth()  << endl;
				cout << "Colour: " << l->getColour()  << endl;
			}
			else if(style->getFeatureClass()=="area")
			{
				OSM::AreaElemStyle *a = (OSM::AreaElemStyle*) style;
				cout << "Colour: " << a->getColour()  << endl;
			}
		}
	}
}
