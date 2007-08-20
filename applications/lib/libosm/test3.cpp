#include "Parser.h"
#include "Client.h"
#include "Node.h"
#include <iostream>
#include <fstream>
#include <sstream>

using std::cout;
using std::cerr;
using std::endl;

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
