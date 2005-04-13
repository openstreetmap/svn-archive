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


#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include "functions.h"
#include "GPSDevice.h"
#include "Polygon.h"
#include "Segment.h"
#include "Components.h"
#include "Map.h"
#include <map>
#include <vector>

#include <qmainwindow.h>
#include <qfont.h>
#include <qcolor.h>
#include <qpixmap.h>
#include <qpen.h>
#include <qevent.h>
#include <qcombobox.h>
#include <qtoolbutton.h>

using std::vector;

// Mouse action modes
enum { ACTION_TRACK, ACTION_DELETE, ACTION_WAYPOINT, ACTION_POLYGON };

namespace OpenStreetMap 
{


struct ImgLabelData 
{
	QFont font;
	QColor colour;

	ImgLabelData(const char* f, int s,QColor c) :font(f,s){ colour=c; }
};

struct SegData
{
	QString segtype;
	QPen pen;

	SegData(){}
	SegData(const QString &st, const QPen& p) { segtype=st; pen=p; }
};

struct PolyData
{
	QString polytype;
	QColor colour;

	PolyData(){}
	PolyData(const QString& pt,	const QColor& c) { polytype=pt; colour=c; }
};

class WaypointRep
{
private:
	QPixmap image;	
	ImgLabelData * labelData; // pointer so we can use NULL for no label

public:
	WaypointRep(const QString& imagefile) : image(imagefile)
		{  labelData=NULL;  }
	WaypointRep(const QString& imagefile,const char* f,int s,
					QColor c) :
			image(imagefile)
	    { labelData=new ImgLabelData(f,s,c);  }
	WaypointRep(const char* f,int s,QColor c)
		{  labelData=new ImgLabelData(f,s,c);  }
	~WaypointRep() 
		{ if(labelData)delete labelData; }
	void draw(QPainter&,int x,int y,const QString& label=""); 
	const QPixmap& getImage(){ return image; }
};

class MainWindow : public QMainWindow
{

Q_OBJECT

private:
	//GridRef topleft;
	//double scale;
	Map map;
	
	// key data
	Components * components;
	vector<Polygon*> polygons;

	// on-screen data representations 
	std::map<QString,WaypointRep*> waypointReps; 
	std::map<QString,QPen> segpens;
	vector<PolyData> polydata;

	// currently selected trackpoints
	int selectedTrackpoint, selectedTrackpoint2;

	// current mouse action mode
	int actionMode;

	// other stuff
	std::vector<ScreenPos> curPolygonPts;
	QString curSegType; 
	int curPolygonType; 
	double polygonRes;
	Polygon * curPolygon;
	bool trackpoints;
	QString curFilename; 
	bool mouseDown;

	QToolButton* modeButtons[4];

	QComboBox * modes;
	void drawTrackpoint(QPainter&,const QPen&,int,int);
	void drawTrackpoint(QPainter&,const QPen&,int,int,int,int);
	void saveFile(const QString&);

	void initSegmentSelection(int,int);
	void initPolygon();
	void endSegmentSelection(int,int);
	void endPolygon(int,int);

	int findNearestTrackpoint(int,int,int);

public:
	MainWindow (double=51.0,double=-1.0,double=100,
			 		double=3.2,double=3.2);
	~MainWindow();
	Components * doOpen(const QString&);

	void paintEvent(QPaintEvent*);
	void mousePressEvent(QMouseEvent*);
	void mouseReleaseEvent(QMouseEvent*);
	void mouseMoveEvent(QMouseEvent*);
	void keyPressEvent(QKeyEvent*);
	void drawTrack(QPainter&);
	void drawWaypoints(QPainter&);
	void drawWaypoint(QPainter&,const Waypoint&);
	void drawPolygons(QPainter&);
	void drawPolygon(QPainter&,Polygon*);
	void rescale(double);
	void editWaypoint(int,int,int);

public slots:
	void open();
	void save();
	void saveAs();
	void readGPS();
	void renameFeature();
	void toggleWaypoints();
	void undo();
	void changePolygonRes();
	void setMode(int);
	void setSegType(const QString&);
	void grabTracks();
};

}
#endif // MAINWINDOW_H
