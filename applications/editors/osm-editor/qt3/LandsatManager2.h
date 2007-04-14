/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
#ifndef LANDSATMANAGER_H
#define LANDSATMANAGER_H

#include "Map.h"
#include <qpixmap.h>
#include "functions.h"
#include <qpainter.h>
#include <qwidget.h>
#include <qcstring.h>

#include "HTTPHandler.h"

#include <vector>
using std::vector;

namespace OpenStreetMap
{

class MainWindow2;

class Tile
{
public:
 	int lat, lon;	
	QPixmap pixmap;
	bool hasData;

	Tile(int lt, int ln, int w, int h) 
		{ lat=lt; lon=ln; pixmap=QPixmap(w,h); hasData=false; }

	void draw(QPainter& p, int x, int y)
	{
		if(hasData)
			p.drawPixmap(x,y,pixmap);
	}

	QString getURL(double llstep)
	{
		QString url;

		double a = ((double)lon) / 1000000;
		double b = ((double)lat) / 1000000;

		url.sprintf("/wms.cgi?request=GetMap&width=%d&height=%d&layers=global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg&bbox=%lf,%lf,%lf,%lf", 
		pixmap.width(),pixmap.height(),a,b,a+(llstep/1000000),
							b+(llstep/1000000));

		return url;
	}
};

class LandsatManager2 : public QObject
{
Q_OBJECT

private:
	MainWindow2 *widget;
	QPixmap pixmap;
	QPixmap* tiles;
	bool dataDisplayed;
	EarthPoint topLeft, bottomRight;
	int nRows, nCols, tileSize;
	HTTPHandler lshttp;

	bool doNeedMoreData();

	vector<Tile*> newtiles;

public:
	LandsatManager2(MainWindow2 *p): lshttp("onearth.jpl.nasa.gov") { widget=p; 
			dataDisplayed=false; }
	LandsatManager2(MainWindow2*,int,int,int);
	~LandsatManager2() { delete[] tiles; }

	bool needMoreData() { return dataDisplayed && doNeedMoreData(); }
	bool toggleDisplay();
	void draw(QPainter&);
	//void grabAll() { if (dataDisplayed) grabTiles(0,0,nCols,nRows); }
	void grabAll() { if (dataDisplayed) grabTilesNew(); }
//	void forceGrabAll() { dataDisplayed=true; grabTiles(0,0,nCols,nRows); }
	void forceGrabAll() { dataDisplayed=true; grabTilesNew(); }
	//QPixmap doGrab(double w,double s,double e,double n,int width,int height);
	void drawTiles(QPainter& p);
	void left();
	void right();
	void up();
	void down();
	void resize(int w,int h);
	void grabTiles(int x1,int y1,int x2,int y2);
	int getTileSize(){ return tileSize; }
	void drawTilesNew(QPainter& p);
	void grabTilesNew();
	bool tileExists(int,int);
	void clearTiles();

public slots:
	void dataReceived(const QByteArray& response,void *dim);
	void newDataReceived(const QByteArray& response,void *t);

};

}

#endif
