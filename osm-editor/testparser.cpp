#include "GPXParser.h"

int main(int argc,char* argv[])
{
	FreeMapper::GPXParser parser;
	QFile file(argv[1]);
	QXmlInputSource source(&file);
	QXmlSimpleReader reader;
	reader.setContentHandler(&parser);
	reader.parse(source);
	FreeMapper::Components *c = parser.getComponents();	
	c->toGPX(argv[2]);
	delete c;

	return 0;
}
