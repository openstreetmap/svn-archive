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
#include "GPSDevice2.h"
#include "Components2.h"
#include "Map.h"
#include "LandsatManager2.h"
#include "SRTMGeneral.h"
#include "HTTPHandler.h"
#include "NodeHandler.h"
#include "Way.h"
#include "SegSplitter.h"
#include "BatchUploader.h"
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
#include <qcstring.h>

#include <qhttp.h>

using std::vector;

// Mouse action modes
// These need to match the order that the toolbar buttons are added
// N_ACTIONS should always be the last
enum { ACTION_NODE, ACTION_MOVE_NODE, ACTION_DELETE_NODE,
		ACTION_SEL_SEG, ACTION_SEL_WAY, ACTION_NEW_SEG, ACTION_BREAK_SEG,
		ACTION_SEL_TRACK, N_ACTIONS };

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

struct SegPen
{
	QPen pen;
	bool casing;

	SegPen() { casing=false; }
	SegPen(const QPen& p, bool c) { pen=p; casing=c; }
};

class MainWindow2 : public QMainWindow, public DrawSurface
{

Q_OBJECT

private:
	//GridRef topleft;
	//double scale;
	Map map;

	double LIMIT;

	// key data
	Components2 * components;
	Components2 * osmtracks;

	// on-screen data representations 
	std::map<QString,WaypointRep*> nodeReps; 
	std::map<QString,SegPen> segpens;
	std::map<QString,QPen> areapens;

	// currently selected trackpoints

	// selected segment(s)
	vector<Segment*> selSeg;
	int segCount;

	Way *selWay;

	// current mouse action mode
	int actionMode;

	// other stuff
	QString curSegType; 
	bool trackpoints, contours, displayOSM, displayGPX, showSegmentColours;
	QString curFilename; 
	bool mouseDown;

	QToolButton* modeButtons[N_ACTIONS]; 
	QToolButton *wayButton, *landsatButton, *osmButton, *gpxButton,
						*contoursButton, *showSegmentColoursButton;

	QComboBox * modes;

	QPixmap landsatPixmap;


	void saveFile(const QString&);

	LandsatManager2 landsatManager;

	Node *pts[2];
	int tpts[2];
	vector<Node*> ptsv[2];
	vector<Node*> movingNodes;
	int nSelectedPoints, nSelectedTrackPoints;

	QPainter *curPainter;

	bool wptSaved;

	double nameAngle;
	bool doingName;
	ScreenPos namePos, curNamePos;
	QString trackName;


	QString username, password;
	bool liveUpdate;

	HTTPHandler osmhttp;
	NodeHandler nodeHandler;

	Node *newUploadedNode, *movingNode;
	vector<Segment*> movingNodeSegs;
	Segment *newUploadedSegment;
	Way *newUploadedWay;
	QPixmap savedPixmap;

	SegSplitter *splitter;
	BatchUploader *uploader;

	bool makingWay;

	QString serialPort;

	QPixmap tpPixmap;

	void clearSegments()
	{
		selSeg.clear();
		//selSeg.push_back(NULL);
		//segCount = 0;
	}

	vector<Node*> gpxNodes;

public:
	MainWindow2 (double=51.0,double=-1.0,double=4000,
			 		double=640,double=480);
	~MainWindow2();
	Components2 * doOpen(const QString&);


	void paintEvent(QPaintEvent*);
	void mousePressEvent(QMouseEvent*);
	void mouseReleaseEvent(QMouseEvent*);
	void mouseMoveEvent(QMouseEvent*);
	void resizeEvent(QResizeEvent * ev);
	void keyPressEvent(QKeyEvent*);
	void drawLandsat(QPainter&);
	void drawContours();
	void rescale(double);

	void updateWithLandsatCheck();
	Map getMap() { return map; }

	void drawContour(int,int,int,int,int,int,int);
	void drawAngleText(int,double,int,int,int,int,int,char*);
	void heightShading(int x1,int y1,int x2,int y2,int x3,int y3,
							int x4,int y4,int r,int g,int b);

	void doDrawAngleText(QPainter *p,int originX,int originY,int x,int y,
					double angle, const char * text);
	void showPosition();
	void drawGPX(QPainter&);
	void drawSegments(QPainter&);
	void drawSegment(QPainter&,Segment*);
	void drawNodes(QPainter&);
	void drawNode(QPainter&,Node*);
	void drawAreas(QPainter&);
	void drawArea(QPainter&,Area*);
	void drawTrackPoints(QPainter &p,Components2*,QColor,bool);
	void drawTrackPoint(QPainter &p,TrackPoint *tp);
	void drawMoving(QPainter&);
	void editNode(int,int,int);
	void nameTrackOn();
	Node * doAddNewNode(double lat,double lon,const QString &name,
									const QString& type);

public slots:
	void open();
	void save();
	void saveAs();
	void saveGPX();
	void readGPS();
	void quit();
	void toggleLandsat();
	void toggleContours();
	void toggleOSM();
	void toggleGPX();
	void undo();
	void setMode(int);
	//void setSegType(const QString&);
	void toggleNodes();
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
	void loginToLiveUpdate();
	void grabOSMFromNet();
	void uploadOSM();
	void logoutFromLiveUpdate();
	void removeTrackPoints();
	void newSegmentAdded(const QByteArray& array,void*);
	void newWayAdded(const QByteArray& array,void*);
	void loadComponents(const QByteArray&,void*);
	void deleteSelectedSeg();
	void handleHttpError(int,const QString&);
	void handleNetCommError(const QString& error);
	void toggleWays();
	void uploadWay();
	void doaddseg(void*);
	void changeSerialPort();
	void uploadNewWaypoints();
	void grabGPXFromNet();
	void loadOSMTracks(const QByteArray& array,void*);
	void splitterDone();
	void changeWayDetails();
	void batchUpload();
	void batchUploadDone();
	void batchUploadError(const QString& error);
	void segSplitterError(const QString& error);
	void toggleSegmentColours();

signals:
	void newNodeAddedSig();
};

}
#endif // MAINWINDOW_H
