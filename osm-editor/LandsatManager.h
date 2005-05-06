#ifndef LANDSATMANAGER_H
#define LANDSATMANAGER_H

#include "Map.h"
#include <qpixmap.h>
#include "functions.h"
#include <qpainter.h>
#include <qwidget.h>

namespace OpenStreetMap
{

class MainWindow;

class LandsatManager
{
private:
	MainWindow *widget;
	QPixmap pixmap;
	bool dataLoaded, dataDisplayed;
	LatLon topLeft, bottomRight;

public:
	LandsatManager(MainWindow *p) { widget=p; 
			dataLoaded=false; dataDisplayed=false; }
	void grab();
	void forceGrab();
	bool needMoreData();
	void turnOff() { dataLoaded=false; }
	void toggleDisplay() { dataDisplayed=!dataDisplayed; }	
	void draw(QPainter&);
};

}

#endif
