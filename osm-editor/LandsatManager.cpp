#include "LandsatManager.h"
#include "curlstuff.h"
#include "MainWindow.h"

#include <iostream>
using namespace std;

namespace OpenStreetMap
{

void LandsatManager::grab(double nScr)
{
	if(dataDisplayed)
		forceGrab(nScr);
}

void LandsatManager::forceGrab(double nScr)
{
	int w=widget->width(), h=widget->height();
	topLeft = widget->getMap().getEarthPoint (ScreenPos(-w*(nScr/2-0.5),
														-h*(nScr/2-0.5)));
	bottomRight = widget->getMap().getEarthPoint (ScreenPos(w*(nScr/2+0.5),
															h*(nScr/2+0.5)));
	CURL_LOAD_DATA *landsatData = grab_landsat
			(topLeft.x,bottomRight.y,bottomRight.x,topLeft.y,w*nScr,
			 h*nScr);
	pixmap.loadFromData((const uchar*)landsatData->data,landsatData->nbytes);
	free(landsatData->data);
	free(landsatData);
	dataDisplayed=true;
}


bool LandsatManager::doNeedMoreData()

{
	ScreenPos topLeftPos=widget->getMap().getScreenPos
				(topLeft.x,topLeft.y),
			  bottomRightPos = widget->getMap().getScreenPos
					  (bottomRight.x,bottomRight.y);

	return( topLeftPos.x>=0 || topLeftPos.y>=0 || 
			bottomRightPos.x<=widget->width() || 
			bottomRightPos.y<=widget->height() );
}


void LandsatManager::draw(QPainter& p)
{
	if(dataDisplayed) 
	{
		ScreenPos topLeftPos=
				widget->getMap().getScreenPos(topLeft.x,topLeft.y);

		p.drawPixmap(0,0,pixmap,-topLeftPos.x,-topLeftPos.y,
						widget->width(),widget->height());
	}
}

void LandsatManager::toggleDisplay()
{
	if(doNeedMoreData()&&!dataDisplayed) 
		forceGrab ();
	else
		dataDisplayed=!dataDisplayed; 
}

}
