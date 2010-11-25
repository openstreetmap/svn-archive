#include "Parser.h"
#include <fstream>
#include <iostream>

int main (int argc, char *argv[])
{
	if(argc<2)
	{
		cerr<<"Usage: "<<argv[0]<<" infile"<<endl;
		exit(1);
	}

	std::ifstream infile(argv[1]);
	if(!infile.good())
	{
		cerr<<"Error reading input file"<<endl;
		exit(1);
	}

	XML_Parser p = XML_ParserCreate(NULL);
	if (!p)
	{
		cerr<< "Error creating parser"<<endl;
		exit(1);
	}
	
	XML_SetElementHandler(p, Parser::startElement, Parser::endElement);

	// First parse to find the junction nodes
	if(!Parser::parse(p,infile))
	{
		cerr<<"Error in first pass:" << Parser::getError() <<endl;
		exit(1);
	}

	// Rewind the file
	// www.cs.hmc.edu/~geoff/classes/hmc.cs070.200109/notes/io.html
	infile.clear();
	infile.seekg(0,std::ios::beg);

	// Second parse to actually split the ways
	XML_ParserReset(p,NULL);
	XML_SetElementHandler(p, Parser::startElement, Parser::endElement);
	if(!Parser::parse(p,infile))
	{
		cerr<<"Error in second pass:" << Parser::getError() <<endl;
		exit(1);
	}


	return 0;
}
