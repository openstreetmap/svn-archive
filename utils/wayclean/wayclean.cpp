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

	if (!compIn)
	{
		cerr << OSM::Parser::getError();
		exit(1);
	}

	OSM::Components *compOut = compIn->cleanWays();	
	if(compOut)
	{
		compOut->toXML(std::cout);
		delete compOut;
	}

	delete compIn;

	return 0;
}
