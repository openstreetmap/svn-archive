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
	QPixmap* tiles;
	bool dataDisplayed;
	EarthPoint topLeft, bottomRight;
	int nRows, nCols, tileSize;

	bool doNeedMoreData();

public:
	LandsatManager(MainWindow *p) { widget=p; 
			dataDisplayed=false; }
	LandsatManager(MainWindow*,int,int,int);
	~LandsatManager() { delete[] tiles; }

	void grab(double=1.0);
	void forceGrab(double=1.0);
	bool needMoreData() { return dataDisplayed && doNeedMoreData(); }
	void toggleDisplay();
	void draw(QPainter&);
	void grabAll() { if (dataDisplayed) grabTiles(0,0,nCols,nRows); }
	void forceGrabAll() { dataDisplayed=true; grabTiles(0,0,nCols,nRows); }
	QPixmap doGrab(double w,double s,double e,double n,int width,int height);
	void drawTiles(QPainter& p);
	void left();
	void right();
	void up();
	void down();
	void resize(int w,int h);
	void grabTiles(int x1,int y1,int x2,int y2);
	int getTileSize(){ return tileSize; }
};

}

#endif
