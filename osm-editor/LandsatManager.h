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
	bool dataDisplayed;
	EarthPoint topLeft, bottomRight;

	bool doNeedMoreData();

public:
	LandsatManager(MainWindow *p) { widget=p; 
			dataDisplayed=false; }
	void grab();
	void forceGrab();
	bool needMoreData() { return dataDisplayed && doNeedMoreData(); }
	void toggleDisplay();
	void draw(QPainter&);
};

}

#endif
