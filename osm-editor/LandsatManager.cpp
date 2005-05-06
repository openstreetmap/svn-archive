#include "LandsatManager.h"
#include "landsat.h"
#include "MainWindow.h"

#include <iostream>
using namespace std;

namespace OpenStreetMap
{

void LandsatManager::grab()
{
	if(dataLoaded)
		forceGrab();
}

void LandsatManager::forceGrab()
{
	int w=widget->width(), h=widget->height();
	topLeft = widget->getMap().getLatLon (ScreenPos(-w,-h));
	bottomRight = widget->getMap().getLatLon (ScreenPos(w*2,h*2));
	LS_LOAD_DATA *landsatData = grab_landsat
			(topLeft.lon,bottomRight.lat,bottomRight.lon,topLeft.lat,w*3,h*3);
	pixmap.loadFromData((const uchar*)landsatData->data,landsatData->nbytes);
	free(landsatData->data);
	free(landsatData);
	dataLoaded=true;
	dataDisplayed=true;
}

bool LandsatManager::needMoreData()
{
	if(dataLoaded)
	{
		ScreenPos topLeftPos=widget->getMap().getScreenPos
				(topLeft.lat,topLeft.lon),
			  bottomRightPos = widget->getMap().getScreenPos
					  (bottomRight.lat,bottomRight.lon);

		return( topLeftPos.x>=0 || topLeftPos.y>=0 || 
			bottomRightPos.x<=widget->width() || 
			bottomRightPos.y<=widget->height() );
	}
	return false;
}

void LandsatManager::draw(QPainter& p)
{
	if(dataDisplayed && dataLoaded)
	{
		ScreenPos topLeftPos=
				widget->getMap().getScreenPos(topLeft.lat,topLeft.lon);

		p.drawPixmap(0,0,pixmap,-topLeftPos.x,-topLeftPos.y,
						widget->width(),widget->height());
	}
}

}
