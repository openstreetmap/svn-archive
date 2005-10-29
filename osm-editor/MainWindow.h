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
#include "Components.h"
#include "Map.h"
#include "LandsatManager.h"
#include "SRTMGeneral.h"
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
#include <qstatusbar.h>


using std::vector;

// Mouse action modes
// N_ACTIONS should always be the last
enum { ACTION_TRACK, ACTION_DELETE, ACTION_WAYPOINT, ACTION_POLYGON, ACTION_NAME_TRACK, ACTION_MOVE_WAYPOINT, ACTION_LINK, ACTION_NEW_SEG, N_ACTIONS };

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

class MainWindow : public QMainWindow, public DrawSurface
{

Q_OBJECT

private:
	//GridRef topleft;
	//double scale;
	Map map;
	
	// key data
	Components * components;
	Polygon *polygon;

	// on-screen data representations 
	std::map<QString,WaypointRep*> waypointReps; 
	std::map<QString,QPen> segpens;
	//vector<PolyData> polydata;
	std::map<QString,QPen> polydata;

	// currently selected trackpoints
	int selectedTrackpoint, selectedTrackpoint2;

	// selected segment
	TrackSeg *selectedSeg;

	// current mouse action mode
	int actionMode;

	// other stuff
	QString curPolygonType;
	std::vector<EarthPoint> curPolygonPts;
	QString curSegType; 
	double polygonRes;
	Polygon * curPolygon;
	bool trackpoints, contours;
	QString curFilename; 
	bool mouseDown;

	QToolButton* modeButtons[N_ACTIONS]; 

	QComboBox * modes;

	QPixmap landsatPixmap;


	void drawTrackpoint(QPainter&,const QBrush&,int,int,int);
	void drawTrackpoint(QPainter&,const QBrush&,int,int,int,int);
	void saveFile(const QString&,bool=false);

	void initSegmentSelection(int,int);
	void initPolygon();
	void endSegmentSelection(int,int);
	void endPolygon(int,int);

	int findNearestTrackpoint(int,int,int);

	int findNearestWaypoint(int,int,int);
	LandsatManager landsatManager;

	RetrievedTrackPoint pts[3];
	int nSelectedPoints;

	QPainter *curPainter;

	Waypoint savedWpt;
	bool wptSaved;

	double nameAngle;
	bool doingName;
	ScreenPos namePos, curNamePos;
	QString trackName;

	void doDrawTrack(QPainter&,bool);

public:
	MainWindow (double=51.0,double=-1.0,double=4000,
			 		double=640,double=480);
	~MainWindow();
	Components * doOpen(const QString&, bool=false);

	void open2(bool=false);
	void grabGPXFromNet(const QString& url);
	void postGPX(const QString& url);

	void paintEvent(QPaintEvent*);
	void mousePressEvent(QMouseEvent*);
	void mouseReleaseEvent(QMouseEvent*);
	void mouseMoveEvent(QMouseEvent*);
	void resizeEvent(QResizeEvent * ev);
	void keyPressEvent(QKeyEvent*);
	void drawTrack(QPainter&);
	void drawWaypoints(QPainter&);
	void drawWaypoint(QPainter&,const Waypoint&);
	void drawPolygons(QPainter&);
	void drawPolygon(QPainter&,Polygon*);
	void drawLandsat(QPainter&);
	void drawContours();
	void rescale(double);
	void editWaypoint(int,int,int);

	void updateWithLandsatCheck();
	Map getMap() { return map; }

	void drawContour(int,int,int,int,int,int,int);
	void drawAngleText(int,double,int,int,int,int,int,char*);
	void heightShading(int x1,int y1,int x2,int y2,int x3,int y3,
							int x4,int y4,int r,int g,int b);

	void doDrawAngleText(QPainter *p,int originX,int originY,int x,int y,
					double angle, const char * text);
	void showPosition()
	{
		QString msg; 
		msg.sprintf("Lat %lf Long %lf",
						map.getBottomLeft().y, map.getBottomLeft().x);
		statusBar()->message(msg);
	}

public slots:
	void open();
	void save();
	void saveAs();
	void readGPS();
	void quit();
	void toggleWaypoints();
	void toggleLandsat();
	void toggleContours();
	void undo();
	void changePolygonRes();
	void setMode(int);
	void setSegType(const QString&);
	void setPolygonType(const QString&);
	void grabLandsat();
	void up();
	void down();
	void left();
	void right();
	void magnify();
	void screenUp();
	void screenDown();
	void screenLeft();
	void screenRight();
	void shrink();
	void removeExcessPoints();
	void commitExcessPoints();
	void grabGPXFromNet();
	void postGPX();
	void removePlainTracks();
//	void login();
};

}
#endif // MAINWINDOW_H
