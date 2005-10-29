#include "LandsatManager.h"
#include "curlstuff.h"
#include "MainWindow.h"

#include <iostream>
using namespace std;

namespace OpenStreetMap
{


LandsatManager::LandsatManager(MainWindow *p, int w, int h, int ts)
{
	widget=p; dataDisplayed = false; tileSize = ts;
	nRows = ((h-1)/tileSize) + 1; nCols = ((w-1)/tileSize) + 1;
	cerr<<"nRows = "<<nRows << " nCols="<<nCols<<endl;
	tiles = new QPixmap[nRows*nCols];	
	
	for(int count=0; count<nRows*nCols; count++)
		tiles[count].resize(tileSize,tileSize);
}

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

QPixmap LandsatManager::doGrab(double w,double s,double e,double n,
							int width, int height)
{
	QPixmap pixmap(tileSize,tileSize);
	CURL_LOAD_DATA *landsatData = grab_landsat (w,s,e,n,width,height);
	pixmap.loadFromData((const uchar*)landsatData->data,landsatData->nbytes);
	free(landsatData->data);
	free(landsatData);
	return pixmap;
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

void LandsatManager::drawTiles(QPainter& p)
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


void LandsatManager::toggleDisplay()
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
		grabTiles(0,0,nCols,nRows);
	}
}

void LandsatManager::left()
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
		grabTiles(0,0,1,nRows);
	}
}

void LandsatManager::right()
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
		grabTiles(nCols-1,0,nCols,nRows);
	}
}

void LandsatManager::up()
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
		grabTiles(0,0,nCols,1);
	}
}

void LandsatManager::down()
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
		grabTiles(0,nRows-1,nCols,nRows);
	}
}

void LandsatManager::resize(int w,int h)
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

void LandsatManager::grabTiles(int x1,int y1,int x2,int y2)
{
	EarthPoint bottomLeft = widget->getMap().getEarthPoint
			(x1*tileSize, y2*tileSize),
				topRight = widget->getMap().getEarthPoint
			(x2*tileSize, y1*tileSize);


	QPixmap tile = doGrab(bottomLeft.x,bottomLeft.y,topRight.x,topRight.y,
							(x2-x1)*tileSize,(y2-y1)*tileSize);

	for(int row=y1; row<=y2-1; row++)
	{
		for(int col=x1; col<=x2-1; col++)
		{
			copyBlt (&tiles[row*nCols+col],0,0,
					&tile,(col-x1)*tileSize,(row-y1)*tileSize , 
					tileSize, tileSize );
		}
	}
}

}
