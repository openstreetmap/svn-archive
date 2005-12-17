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

namespace OpenStreetMap
{

class MainWindow2;

class LandsatManager2
{
private:
	MainWindow2 *widget;
	QPixmap pixmap;
	QPixmap* tiles;
	bool dataDisplayed;
	EarthPoint topLeft, bottomRight;
	int nRows, nCols, tileSize;

	bool doNeedMoreData();

public:
	LandsatManager2(MainWindow2 *p) { widget=p; 
			dataDisplayed=false; }
	LandsatManager2(MainWindow2*,int,int,int);
	~LandsatManager2() { delete[] tiles; }

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
