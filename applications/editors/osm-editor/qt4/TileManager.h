/*
    Copyright (C) 2006 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

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
#ifndef TILEMANAGER_H
#define TILEMANAGER_H

#include "Map.h"
#include <qpixmap.h>
#include "functions.h"
#include <qpainter.h>
#include <qwidget.h>
//#include <q3cstring.h>

#include "HTTPHandler.h"

#include <vector>
using std::vector;

namespace OpenStreetMap
{

class MapWidget;

class Tile
{
public:
 	int lat, lon,tileSizeLL; // note lat/lon = real lat/lon*1000000
	Tile(int lt,int ln,int ts)
			{ lat=lt; lon=ln; tileSizeLL = ts; }
	bool contains(Tile*);
};

class PixTile : public Tile
{
public:
	QPixmap pixmap;
	int scale;
	bool hasData;

	PixTile(int lt, int ln, int ts,int s, int w=400, int h=400) : Tile(lt,ln,ts)
		{ pixmap=QPixmap(w,h); hasData=false; scale=s; }

	void draw(QPainter& p, int x, int y)
	{
		if(hasData)
			p.drawPixmap(x,y,pixmap);
	}
};

class TileManager : public QObject
{
Q_OBJECT

protected:
	MapWidget *widget;
	bool dataDisplayed;
	int tileSizePx;
	HTTPHandler lshttp;
	vector<Tile*> newtiles;
	QString constURL;

public:
	TileManager(MapWidget *,int, const QString&, const QString&);
	~TileManager(); 
	
	virtual Tile *addTile(int,int,int,int);
	void forceGrab() { dataDisplayed=true; grab(); }
	virtual void grab();
	void scheduleRequest(const QString&,void*);
	virtual bool tileExists(int,int,int,int);
	bool toggleDisplay();
	virtual void handleNewDataReceived(const QByteArray& response,void *t) = 0;
	int getTileSize() { return tileSizePx; }
	virtual QString getURL(double blLon, double blLat, double tileSizeLL);
	bool isActive() { return dataDisplayed; }

public slots:
	void newDataReceived(const QByteArray& response,void *t);

};

class PixTileManager: public TileManager
{
public:
	PixTileManager(MapWidget* w,int ts, const QString& server,
					const QString& url) :
			TileManager(w,ts,server,url) { }

	Tile *addTile(int,int,int,int);
	void drawTilesNew(QPainter&);
	void handleNewDataReceived(const QByteArray&,void*);
	bool tileExists(int,int,int,int);
	QString getURL(double blLon, double blLat, double tileSizeLL);
	void clearRequests();
};

class OSMTileManager: public TileManager
{
private:
	QString username,password;
public:
	OSMTileManager(MapWidget* w,int ts, const QString& server,
					const QString& url) :
			TileManager(w,ts,server,url) { }
	void setAuthentication(const QString& u, const QString& p)
		{ username=u; password=p; }
	void handleNewDataReceived(const QByteArray&,void*);
	void grab() { lshttp.setAuthentication(username,password);
					TileManager::grab(); }
};

}
#endif
