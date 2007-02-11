#ifndef ELEMSTYLE_H
#define ELEMSTYLE_H

#include <string>

namespace OSM
{

class ElemStyle
{
	// zoom range to display the feature
protected:
	int minZoom;

public:
	int getMinZoom()
	{
		return minZoom;
	}

	virtual std::string getFeatureClass() = 0;
};


}

#endif
