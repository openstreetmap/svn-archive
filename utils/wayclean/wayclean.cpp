#include "Parser.h"
#include "Client.h"
#include "Node.h"
#include "Way.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <cstdlib>
#include <vector>

using std::cout;
using std::cerr;
using std::endl;


int main(int argc,char* argv[])
{
	OSM::Components *compIn = OSM::Parser::parse(std::cin);
	OSM::Components compOut;

	if (!compIn)
	{
		cerr << OSM::Parser::getError();
		exit(1);
	}


	compIn->rewindWays();
	while(compIn->hasMoreWays())
	{
		OSM::Way *w = compIn->nextWay();
		cerr << w->id << endl;
		if(w)
		{
			std::vector<int> nodes = compIn->orderWay(w->id);

			if(nodes.size())
			{
				OSM::Way *way = new OSM::Way;
				way->tags = w->tags;
				compOut.addWay(way);
				for(int i=0; i<nodes.size()-1; i++)
				{
					int segid=compOut.addSegment
					(new OSM::Segment(nodes[i],nodes[i+1]));
					way->addSegment(segid);
				}
			}
		}
	}

	compIn->rewindNodes();
	while(compIn->hasMoreNodes())
	{
		OSM::Node *n = new OSM::Node(*(compIn->nextNode()));
		compOut.addNode(n);
	}

	compOut.toXML(std::cout);

	delete compIn;

	return 0;

}
