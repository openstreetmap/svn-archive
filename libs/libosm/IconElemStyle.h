#ifndef ICONELEMSTYLE_H
#define ICONELEMSTYLE_H

#include "ElemStyle.h"
#include <string>

namespace OSM
{

class IconElemStyle : public ElemStyle
{
private:
	std::string icon;
	bool annotate;

public:
	IconElemStyle (std::string icon, bool annotate, int minZoom)
	{
		this->icon=icon;
		this->annotate=annotate;
		this->minZoom = minZoom;
	}	
	
	std::string getIcon()
	{
		return icon;
	}

	bool doAnnotate()
	{
		return annotate;
	}

	std::string getFeatureClass()
	{
		return "point";
	}
};

}

#endif
