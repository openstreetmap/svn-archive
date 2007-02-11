#ifndef LINEELEMSTYLE_H
#define LINEELEMSTYLE_H

#include "ElemStyle.h"
#include <string>
namespace OSM
{

class LineElemStyle : public  ElemStyle
{
protected:
	int width;
	std::string colour;

public:
	LineElemStyle (int width, std::string colour, int minZoom)
	{
		this->width = width;
		this->colour = colour;
		this->minZoom = minZoom;
	}

	int getWidth()
	{
		return width;
	}

	std::string getColour()
	{
		return colour;
	}

	std::string getFeatureClass()
	{
		return "polyline";
	}
};
}

#endif
