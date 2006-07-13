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
#include "LandsatManager2.h"
#include "MainWindow2.h"

#include <iostream>
using namespace std;

#include <cmath>

namespace OpenStreetMap
{



LandsatManager2::LandsatManager2(MainWindow2 *p, int w, int h, int ts) :
						lshttp("onearth.jpl.nasa.gov")
{
	widget=p; dataDisplayed = false; tileSize = ts;
	nRows = ((h-1)/tileSize) + 1; nCols = ((w-1)/tileSize) + 1;
	tiles = new QPixmap[nRows*nCols];	
	
	for(int count=0; count<nRows*nCols; count++)
		tiles[count].resize(tileSize,tileSize);
}


/*
QPixmap LandsatManager2::doGrab(double w,double s,double e,double n,
							int width, int height)
{
	int *dimensions = new int[4];
	QPixmap pixmap(tileSize,tileSize);
	CURL_LOAD_DATA *landsatData = grab_landsat (w,s,e,n,width,height);
	pixmap.loadFromData((const uchar*)landsatData->data,landsatData->nbytes);
	free(landsatData->data);
	free(landsatData);
	return pixmap;
}
*/

	
bool LandsatManager2::doNeedMoreData()
{
	ScreenPos topLeftPos=widget->getMap().getScreenPos
				(topLeft.x,topLeft.y),
			  bottomRightPos = widget->getMap().getScreenPos
					  (bottomRight.x,bottomRight.y);

	return( topLeftPos.x>=0 || topLeftPos.y>=0 || 
			bottomRightPos.x<=widget->width() || 
			bottomRightPos.y<=widget->height() );
}


void LandsatManager2::draw(QPainter& p)
{
	if(dataDisplayed) 
	{
		ScreenPos topLeftPos=
				widget->getMap().getScreenPos(topLeft.x,topLeft.y);

		p.drawPixmap(0,0,pixmap,-topLeftPos.x,-topLeftPos.y,
						widget->width(),widget->height());
	}
}

void LandsatManager2::drawTiles(QPainter& p)
{
	if(dataDisplayed)
	{
		for(int row=0; row<nRows; row++)
		{
			for(int col=0; col<nCols; col++)
			{
				p.drawPixmap( col*tileSize, row*tileSize , 
								tiles[row*nCols+col], 0, 0, 
							tileSize, tileSize );
			}
		}
	}
}

void LandsatManager2::drawTilesNew(QPainter& p)
{
	if(dataDisplayed)
	{
		for(int count=0; count<newtiles.size(); count++)
		{
			//cerr << count << " ";
			double a = ((double)newtiles[count]->lon) / 1000000;
			double b = ((double)newtiles[count]->lat) / 1000000;
			//cerr << " lon=" <<a << " lat=" <<b;
			ScreenPos pos = widget->getMap().getScreenPos (a,b);
			//cerr << "(" << pos.x << "," << pos.y << ") ";
			//if(newtiles[count]->hasData)
			//	cerr << "*HAS DATA* ";
			newtiles[count]->draw(p,pos.x,pos.y-400);
			//cerr << endl;
		}
	}
}

bool LandsatManager2::toggleDisplay()
{
	/*
	if(doNeedMoreData()&&!dataDisplayed) 
		forceGrab ();
	else
		dataDisplayed=!dataDisplayed; 
	*/
	dataDisplayed = !dataDisplayed;
	if(dataDisplayed)
	{
		//grabTiles(0,0,nCols,nRows);
		grabTilesNew();
	}
	return dataDisplayed;
}

void LandsatManager2::left()
{
	if(dataDisplayed)
	{
		for(int row=0; row<nRows; row++)
		{
			for(int col=nCols-1; col>0; col--)
			{
				tiles[row*nCols + col] = tiles[row*nCols + (col-1)];
			}
		}
		

		// grab using lat/lon
		//grabTiles(0,0,1,nRows);
		grabTilesNew();
	}
}

void LandsatManager2::right()
{
	if(dataDisplayed)
	{
		for(int row=0; row<nRows; row++)
		{
			for(int col=0; col<nCols-1; col++)
			{
				tiles[row*nCols + col] = tiles[row*nCols + (col+1)];
			}
		}

		// grab using lat/lon
		//grabTiles(nCols-1,0,nCols,nRows);
		grabTilesNew();
	}
}

void LandsatManager2::up()
{
	if(dataDisplayed)
	{
		for(int row=nRows-1; row>0; row--)
		{
			for(int col=0; col<nCols; col++)
			{
				tiles[row*nCols + col] = tiles[(row-1)*nCols + col];
			}
		}

		// grab using lat/lon
		//grabTiles(0,0,nCols,1);
		grabTilesNew();
	}
}

void LandsatManager2::down()
{
	if(dataDisplayed)
	{
		for(int row=0; row<nRows-1; row++)
		{
			for(int col=0; col<nCols; col++)
			{
				tiles[row*nCols + col] = tiles[(row+1)*nCols + col];
			}
		}

		// grab using lat/lon
		//grabTiles(0,nRows-1,nCols,nRows);
		grabTilesNew();
	}
}

void LandsatManager2::resize(int w,int h)
{
	nRows = ((h-1)/tileSize) + 1,
	nCols = ((w-1)/tileSize) + 1;


	delete[] tiles;
	tiles = new QPixmap[nRows*nCols];

	for(int row=0; row<nRows; row++)
	{
		for(int col=0; col<nCols; col++)
		{
			tiles[row*nCols + col].resize(tileSize,tileSize); 
		}

	}

	if(dataDisplayed)
		grabAll();

}

void LandsatManager2::grabTiles(int x1,int y1,int x2,int y2)
{
	EarthPoint bottomLeft = widget->getMap().getEarthPoint
			(x1*tileSize, y2*tileSize),
				topRight = widget->getMap().getEarthPoint
			(x2*tileSize, y1*tileSize);


	int *dimensions = new int[4];
	dimensions[0] = x1;
	dimensions[1] = y1;
	dimensions[2] = x2;
	dimensions[3] = y2;

	QString url;

	url.sprintf("/wms.cgi?request=GetMap&width=%d&height=%d&layers=global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg&bbox=%lf,%lf,%lf,%lf", (x2-x1)*tileSize, (y2-y1)*tileSize, bottomLeft.x,bottomLeft.y,topRight.x,topRight.y);

	cerr << "Landsat URL: " << url << endl;

	lshttp.scheduleCommand("GET",url,this,
						SLOT(dataReceived(const QByteArray&,void*)),
						dimensions,
						SLOT(handleNetCommError(const QString&)), widget);

}

void LandsatManager2::dataReceived(const QByteArray& response,void *dim)
{
	cerr << "dataReceived()"  << endl;

	int *dimensions = (int *)dim;
	int x1 = dimensions[0], y1 = dimensions[1], x2 = dimensions[2], 
		y2 = dimensions[3];

	QPixmap pixmap(tileSize,tileSize);
	pixmap.loadFromData(response);

	for(int row=y1; row<=y2-1; row++)
	{
		for(int col=x1; col<=x2-1; col++)
		{
			copyBlt (&tiles[row*nCols+col],0,0,
					&pixmap,(col-x1)*tileSize,(row-y1)*tileSize , 
					tileSize, tileSize );
		}
	}

	delete[] dimensions;
}


// this should be called every time the screen state changes
void LandsatManager2::grabTilesNew()
{
	// want a constant dimension of 400x400
	double pixelsperll = widget->getMap().getScale();

	// grab 400x400 pixel tiles
	int llstep = (400 / pixelsperll) * 1000000; 

	EarthPoint bottomLeft = widget->getMap().getBottomLeft(),
				topRight = widget->getMap().getTopRight();

	cerr << "bottomLeft: lon=" << bottomLeft.x << " lat=" <<bottomLeft.y
			<< endl;
	cerr << "topRight: lon=" << topRight.x << " lat=" <<topRight.y
			<< endl;
	int lonmin = ( floor((bottomLeft.x*1000000) / llstep)) * llstep,
	    lonmax = ( ceil((topRight.x*1000000) / llstep)) * llstep,
	    latmin = ( floor((bottomLeft.y*1000000) / llstep)) * llstep,
	    latmax = ( ceil((topRight.y*1000000) / llstep)) * llstep;


	cerr << "lonmin:" << lonmin << endl;
	cerr << "lonmax:" << lonmax << endl;
	cerr << "latmin:" << latmin << endl;
	cerr << "latmax:" << latmax << endl;

	// Erase old tiles no longer in view
	/* no don't do this - it's be better to cache them
	vector<Tile*>::iterator j;

	for(vector<Tile*>::iterator i=newtiles.begin(); i!=newtiles.end(); i++)
	{
		if((*i)->lat<lonmin||(*i)->lon>=lonmax||(*i)->lat<latmin||
						(*i)->lat>=latmax)
		{
			delete *i;
			newtiles.erase(i);
			i--;
		}
	}
	*/

	// Loop through tiles in view - if any don't exist yet, grab them
	for(int loncount=lonmin; loncount<lonmax; loncount+=llstep)
	{
		for(int latcount=latmin; latcount<latmax; latcount+=llstep)
		{
			if(!tileExists(latcount,loncount))
			{
				int tilewidth = 400; 
				int tileheight =  400; 
				Tile *tile = new Tile (latcount,loncount,tilewidth,tileheight);
				newtiles.push_back(tile);
				QString url = tile->getURL(llstep);
				cerr<<"Tile URL = " << url << endl;
				lshttp.scheduleCommand("GET",url,this,
						SLOT(newDataReceived(const QByteArray&,void*)),tile,
						SLOT(handleNetCommError(const QString&)), widget);
			}
		}
	}
}

void LandsatManager2::clearTiles()
{
	// 090706 clear any pending requests - otherwise there will be an
	// almighty crash :-)
	lshttp.clearRequests();

	vector<Tile*>::iterator i = newtiles.begin();
	while(i!=newtiles.end())
	{
        delete *i;
		newtiles.erase(i);
	}
}

void LandsatManager2::newDataReceived(const QByteArray& response,void *t)
{

	Tile *tile = (Tile *)t;
	cerr << "newDataReceived()"  << endl;
	cerr << "lat=" << tile->lat << " lon=" << tile->lon << endl;
	tile->pixmap.loadFromData(response);
	tile->hasData = true;
	widget->update();
}

bool LandsatManager2::tileExists(int lat,int lon)
{
	for(int count=0; count<newtiles.size(); count++)
	{
		if(newtiles[count]->lon==lon && newtiles[count]->lat==lat)
			return true;
	}
	return false;
}

}
