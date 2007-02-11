#ifndef AREAELEMSTYLE_H
#define AREAELEMSTYLE_H

#include "ElemStyle.h"
#include <string>

namespace OSM
{

class AreaElemStyle : public ElemStyle
{
private:
	std::string colour;

public:
	AreaElemStyle (std::string colour, int minZoom)
	{	
		this->colour = colour;
		this->minZoom = minZoom;
	}

	std::string getColour()
	{
		return colour;
	}

	std::string getFeatureClass()
	{
		return "area";
	}

};

}
#endif
