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
#include "TileManager.h"
#include "MapWidget.h"
#include <cmath>

namespace OpenStreetMap
{

bool Tile::contains(Tile *t)
{
	return !(
	(t->lon<lon || t->lon>=lon+tileSizeLL || t->lat<lat||t->lat>=lat+tileSizeLL)
		    ||
	(t->lon+t->tileSizeLL<=lon || t->lon+t->tileSizeLL>lon+tileSizeLL
			|| t->lat+t->tileSizeLL<=lat|| t->lat+t->tileSizeLL>lat+tileSizeLL)
		   );
}

TileManager::TileManager(MapWidget *p, int ts,
							const QString& server, 
							const QString& tileURL) : lshttp(server)
{
	widget=p; dataDisplayed = false; tileSizePx = ts;
	constURL = tileURL;
}

TileManager::~TileManager()
{
	for (int count=0; count<newtiles.size(); count++)
		delete newtiles[count];
}

Tile * TileManager::addTile(int lat,int lon,int tileSizeLL, int scale)
{
	cerr << "    TileManager::addTile()" << endl;
	Tile *t = new Tile(lat,lon,tileSizeLL);
	newtiles.push_back(t);
	return t;
}

Tile * PixTileManager::addTile(int lat,int lon,int tileSizeLL, int scale)
{
	cerr << "    PixTileManager::addTile()" << endl;
	PixTile *t = new PixTile(lat,lon,tileSizeLL,scale);
	newtiles.push_back(t);
	return t;
}

// this should be called every time the screen state changes
void TileManager::grab()
{
	if(!dataDisplayed) return;

	// get the current scale
	double pixelsperll = widget->getMap().getScale();

	// To avoid floating-point issues we convert latitude and longitude to
	// integers by multiplying by 1000000
	//
	// tileSizePx is the predetermined (and constant) tile size
	//
	// The code loops through the currently displayed dimensions (in
	// lat/lon*1000000) and works out whether a tile needs to be grabbed.
	// To do this we need the latitude/longitude step - how many latitude/
	// longitude units each tile covers.
	// This can be obtained from the scale and the tile size as follows.
	int tileSizeLL = ( ((double)tileSizePx) / pixelsperll) * 1000000; 

	EarthPoint bottomLeft = widget->getMap().getBottomLeft(),
				topRight = widget->getMap().getTopRight();

	int lonmin = ( floor((bottomLeft.x*1000000) / tileSizeLL)) * tileSizeLL,
	    lonmax = ( ceil((topRight.x*1000000) / tileSizeLL)) * tileSizeLL,
	    latmin = ( floor((bottomLeft.y*1000000) / tileSizeLL)) * tileSizeLL,
	    latmax = ( ceil((topRight.y*1000000) / tileSizeLL)) * tileSizeLL;



	// Loop through tiles in view - if any don't exist yet, grab them
	for(int loncount=lonmin; loncount<lonmax; loncount+=tileSizeLL)
	{
		for(int latcount=latmin; latcount<latmax; latcount+=tileSizeLL)
		{
			cout << "Trying " << loncount << "," << latcount << endl;
			if(!tileExists(latcount,loncount,tileSizeLL,pixelsperll))
			{
				cerr << "  Tile doesn't exist so grabbing it" << endl;
				Tile *t = addTile  (latcount,loncount,tileSizeLL,pixelsperll);
				cerr << "addTile done" << endl;
				double a = ((double)loncount) / 1000000;
				double b = ((double)latcount) / 1000000;
				double c = ((double)tileSizeLL)/1000000;
				cerr << "calling getURL" << endl;
				QString url = getURL(a,b,c);
				cerr << "done. calling scheduleRequest" << endl;
				scheduleRequest(url,t);

			}
		}
	}
}

void TileManager::scheduleRequest(const QString& url, void* data)
{
	cerr << "  Scheduling request: " << url.toAscii().constData() << endl;
	lshttp.scheduleCommand("GET",url,this,
					SLOT(newDataReceived(const QByteArray&,void*)),data,
					SLOT(handleNetCommError(const QString&)), widget);
}

QString TileManager::getURL(double blLon, double blLat, 
								double tileSizeLL)
{
	QString str;
	str.sprintf("%s&bbox=%lf,%lf,%lf,%lf",
						constURL.toAscii().constData(),
					   	blLon, blLat, blLon+tileSizeLL,
								blLat+tileSizeLL,tileSizePx, tileSizePx);
	return str;
}

QString PixTileManager::getURL(double blLon, double blLat, 
								double tileSizeLL)
{
	QString str;
	str.sprintf("%s&bbox=%lf,%lf,%lf,%lf&width=%d&height=%d",
						constURL.toAscii().constData(), 
							blLon, blLat, blLon+tileSizeLL,
								blLat+tileSizeLL,tileSizePx, tileSizePx);
	return str;
}

bool TileManager::tileExists(int lat,int lon, int tileSizeLL,int scale) 
{
	bool exists=false;
	Tile testTile(lat,lon,tileSizeLL);
	for(int count=0; count<newtiles.size(); count++)
	{
		if (newtiles[count]->contains(&testTile))
		{
			return true;
		}
	}
	return exists;
}

// Overridden tileExists() for PixTileManager needed as for pixmap tile 
// sources, scale needs to be taken into account
bool PixTileManager::tileExists(int lat,int lon, int tileSizeLL,int scale)
{
	bool exists = false;
	PixTile t1(lat,lon,tileSizeLL,scale);
	for(int count=0; count<newtiles.size(); count++)
	{
		PixTile *t2 = (PixTile*)(newtiles[count]);
		if (t2->contains(&t1) && t2->scale == t1.scale)
		{
			return true;
		}
	}
	return exists;
}

void PixTileManager::drawTilesNew(QPainter& p)
{
	if(dataDisplayed)
	{
		int pixelsperll = widget->getMap().getScale();
		for(int count=0; count<newtiles.size(); count++)
		{
			double a = ((double)newtiles[count]->lon) / 1000000;
			double b = ((double)newtiles[count]->lat) / 1000000;
			ScreenPos pos = widget->getMap().getScreenPos (a,b);
			PixTile *tile = (PixTile*)(newtiles[count]);
			if(tile->scale == pixelsperll)
				tile->draw(p,pos.x,pos.y-tileSizePx);
		}
	}
}

bool TileManager::toggleDisplay()
{
	dataDisplayed = !dataDisplayed;
	if(dataDisplayed)
	{
		//grabTiles(0,0,nCols,nRows);
		grab();
	}
	return dataDisplayed;
}

// slots can't be virtual methods? so immediately call the real virtual method
void TileManager::newDataReceived(const QByteArray& response,void *t)
{
	handleNewDataReceived(response,t);
}

void PixTileManager::handleNewDataReceived(const QByteArray& response, void *t)
{
	PixTile *tile = (PixTile *)t;
	tile->pixmap.loadFromData(response);
	tile->hasData = true;
	widget->update();
}

void PixTileManager::clearRequests()
{
	lshttp.clearRequests();
	PixTile *t; 
	bool cont = (newtiles.size()>0);
	while(cont) 
	{
		t = (PixTile*)(newtiles[newtiles.size()-1]);
		if(t->hasData==false)
		{
			delete t; 
			newtiles.pop_back();
			cont = (newtiles.size()>0);
		}
		else
		{
			cont=false;
		}
	}	
}


void OSMTileManager::handleNewDataReceived(const QByteArray& response, void *t)
{
	// just load the OSM data in. t is basically ignored.
	widget->loadComponents(response,t);
	widget->update();

}

}
