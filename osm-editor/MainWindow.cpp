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
#include "MainWindow.h"
#include "functions.h"
#include "SRTMConGen.h"
#include "RemoveExcessDialogue.h"

#include <iostream>
#include <sstream>
#include <cstdlib>

#include <qapplication.h>
#include <qpainter.h>
#include <qfontmetrics.h>
#include <qpopupmenu.h>
#include <qmenubar.h>
#include <qfiledialog.h>
#include <qmessagebox.h>
#include <qinputdialog.h>
#include <qtoolbar.h>
#include <qlabel.h>
#include <qsignalmapper.h>
#include <qtextstream.h>

#include "WaypointDialogue.h"
#include "LoginDialogue.h"

#include <qxml.h>
#include "GPXParser.h"

#ifdef XMLRPC
#include <string>
#include <XmlRpcCpp.h>
#endif

#include "landsat.h"

using std::cout;
using std::endl;
using std::cerr;

namespace OpenStreetMap 
{

QPixmap mmLoadPixmap(const QString& directory, const QString& filename) ;

void WaypointRep::draw(QPainter & p,int x,int y, const QString& label)
{
	QString lbl = label; // to get round lack of const in QTextStream
	if(!image.isNull()) 
	{
		p.drawPixmap(x-image.width()/2,y-image.height()/2,image);
	}

	// Only attempt to draw supplied label if this image type expects one
	if(labelData)
	{
		QFontMetrics fm(labelData->font);
		int labelY = (image.isNull() ? y:y + image.height()/2), 
			labelX = (image.isNull() ? x:x + image.width()/2);

		p.setPen(labelData->colour);
		p.setFont(labelData->font);

		// Draw feature label one word per line
		QTextStream strm(&lbl,IO_ReadOnly);
		QString word;
		while(!strm.atEnd())
		{
			strm>>word;
			p.drawText(labelX,labelY,word);
			labelY += fm.height();
		}
	}
}

MainWindow::MainWindow(double lat,double lon, double s,double w,double h) :
									map(lon,lat,s,w,h), 
									polygonRes(0.1),
									landsatManager(this)
{
	cerr<<lat<<" "<<lon<<endl;
	cerr<<map.getBottomLeft().x<<endl;
	cerr<<map.getBottomLeft().y<<endl;
	setCaption("OpenStreetMap Editor");
	resize ( w, h );	

	contours = false;
	wptSaved = false;

	actionMode = ACTION_TRACK;
	curSegType = "A road"; 
	selectedTrackpoint = -1;
	nSelectedPoints = 0;


	segpens["footpath"]= QPen (Qt::green, 2);
	segpens["cycle path"]= QPen (Qt::magenta, 2);
	segpens["bridleway"]=QPen(QColor(192,96,0),2);
	segpens["byway"] = QPen (Qt::red, 2);
	segpens["minor road"]= QPen (Qt::black, 2);
	segpens["B road"]= QPen (Qt::black, 3);
	segpens["A road"]= QPen (Qt::black, 4);
	segpens["motorway"]= QPen (Qt::black, 6);
	segpens["railway"]= QPen (Qt::gray, 4);
	segpens["permissive footpath"]= QPen (Qt::green, 1);
	segpens["permissive bridleway"]= QPen (QColor(192,96,0), 1);
	segpens["track"]= QPen (Qt::darkGray, 2);

	polydata["wood"]=QColor(192,224,192);
	polydata["lake"]=QColor(192,192,255); 
	polydata["heath"]=QColor (255,224,192);
	polydata["urban"]=QColor (128,128,128);
	polydata["access land"]=QColor (192,0,192);


	// Construct the menus.
	QPopupMenu* fileMenu = new QPopupMenu(this);
	fileMenu->insertItem("&Open",this,SLOT(open()),CTRL+Key_O);
	fileMenu->insertItem("&Save",this,SLOT(save()),CTRL+Key_S);
	fileMenu->insertItem("Save &as...",this,SLOT(saveAs()),CTRL+Key_A);
	fileMenu->insertItem("&Read GPS",this,SLOT(readGPS()),CTRL+Key_R);
	fileMenu->insertItem("&Grab Landsat",this,SLOT(grabLandsat()),CTRL+Key_G);
	fileMenu->insertItem("&Quit", this, SLOT(quit()), ALT+Key_Q);
	menuBar()->insertItem("&File",fileMenu);

	QPopupMenu* editMenu = new QPopupMenu(this);
	
	editMenu->insertItem("&Toggle waypoints",this,SLOT(toggleWaypoints()),
						CTRL+Key_T);
	editMenu->insertItem("Toggle &Landsat",this,SLOT(toggleLandsat()),
						CTRL+Key_L);
	editMenu->insertItem("Toggle &contours",this,SLOT(toggleContours()),
						CTRL+Key_C);
//	editMenu->insertItem("Undo",this,SLOT(undo()),CTRL+Key_Z);
	editMenu->insertItem("Remove e&xcess trackpoints",
					this,SLOT(removeExcessPoints()),CTRL+Key_X);
	editMenu->insertItem("Co&mmit excess trackpoint changes",this,
							SLOT(commitExcessPoints()),CTRL+Key_M);
	editMenu->insertItem("Change pol&ygon resolution",this,
							SLOT(changePolygonRes()),CTRL+Key_Y);
	menuBar()->insertItem("&Edit",editMenu);

	QToolBar* toolbar=new QToolBar(this);
	toolbar->setHorizontalStretchable(true);

	new QLabel("Segment: ", toolbar);
	QComboBox* r = new QComboBox(toolbar);
	for(std::map<QString,QPen>::iterator i=segpens.begin(); i!=segpens.end();
		i++)
	{
		r->insertItem(i->first);
	}

	new QLabel("Polygon: ", toolbar);
	QComboBox* seg = new QComboBox(toolbar);
	for(std::map<QString,QPen>::iterator i=polydata.begin();i!=polydata.end();
		i++)
	{
		seg->insertItem(i->first);
	}

	QObject::connect(r,SIGNAL(activated(const QString&)),
						this,SLOT(setSegType(const QString&)));	
	QObject::connect(seg,SIGNAL(activated(const QString&)),
						this,SLOT(setPolygonType(const QString&)));	
	// Do the toolbar buttons to change the mode.
	//
	// Construct a signal mapper so that each mode button can be mapped to
	// one slot (i.e. setMode()). The setMapping() method of the signal
	// mapper enables you to hook up a particular value to each button,
	// enabling a range of buttons representing values to be mapped to one
	// slot which takes an int.
	

	QSignalMapper* mapper = new QSignalMapper(this);


	QPixmap one = mmLoadPixmap("images","one.png");
	QPixmap two = mmLoadPixmap("images","two.png");
	QPixmap wp = mmLoadPixmap("images","waypoint.png");
	QPixmap three = mmLoadPixmap("images","three.png");
	QPixmap nametracks = mmLoadPixmap("images","nametracks.png");
	QPixmap objectmanip = mmLoadPixmap("images","objectmanip.png");
	QPixmap left_pixmap = mmLoadPixmap("images","arrow_left.png");
	QPixmap right_pixmap = mmLoadPixmap("images","arrow_right.png");
	QPixmap up_pixmap = mmLoadPixmap("images","arrow_up.png");
	QPixmap down_pixmap = mmLoadPixmap("images","arrow_down.png");
	QPixmap magnify_pixmap = mmLoadPixmap("images","magnify.png");
	QPixmap shrink_pixmap = mmLoadPixmap("images","shrink.png");

	new QToolButton(left_pixmap,"Move left","",this,SLOT(left()),toolbar);
	new QToolButton(right_pixmap,"Move right","",this,SLOT(right()),toolbar);
	new QToolButton(up_pixmap,"Move up","",this,SLOT(up()),toolbar);
	new QToolButton(down_pixmap,"Move down","",this,SLOT(down()),toolbar);
	new QToolButton(magnify_pixmap,"Zoom in","",this,SLOT(magnify()),toolbar);
	new QToolButton(shrink_pixmap,"Zoom out","",this,SLOT(shrink()),toolbar);


	modeButtons[ACTION_TRACK] = new QToolButton
			(one,"Create Segments","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_DELETE] = new QToolButton
			(two,"Delete Trackpoints","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_WAYPOINT]= new QToolButton
			(wp,"Edit Waypoints","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_POLYGON]= new QToolButton
			(three,"Create Polygons","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_NAME_TRACK]= new QToolButton
			(nametracks,"Name tracks","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_MOVE_WAYPOINT]= new QToolButton
			(objectmanip,"Move Waypoint","",mapper,SLOT(map()),toolbar);


	toolbar->setStretchableWidget(new QLabel(toolbar));

	// Setting a blank label as the stretchable widget means that one can
	// stretch the toolbar while keeping the tool buttons bunched up to the 
	// left.

	// Turn the "mode" toolbar buttons into toggle buttons, and set their
	// mapping index for the signal mapper.
	
	for (int count=0; count<N_ACTIONS; count++)
	{
		modeButtons[count]->setToggleButton(true);
		mapper->setMapping(modeButtons[count],count);
	}
	
	modeButtons[ACTION_TRACK]->setOn(true);

	// The final stage of implementing the signal mapper: connect mapped()
	// to setMode(). The value set in setMapping() above will be used.
	QObject::connect(mapper,SIGNAL(mapped(int)),this,SLOT(setMode(int)));
	polygon=new Polygon("wood");
	curPolygonType = "wood";


	waypointReps["pub"] = new WaypointRep 
			("images/pub.png","Helvetica",10, QColor(170,85,0));
	waypointReps["church"] = new WaypointRep ( "images/church.png");
	waypointReps["viewpoint"] = new WaypointRep("images/viewpoint.png");
	waypointReps["farm"] = new WaypointRep("images/farm.png",
					"Helvetica",8,Qt::red);
	waypointReps["summit"] = new WaypointRep(
					"images/peak.png","Helvetica",10, Qt::magenta);
	waypointReps["hamlet"] = new WaypointRep(
					"images/place.png","Helvetica",12, Qt::black);
	waypointReps["village"] = new WaypointRep(
					"images/place.png","Helvetica",16, Qt::black);
	waypointReps["small town"] = new WaypointRep(
					"images/place.png","Helvetica",20, Qt::black);
	waypointReps["large town"] = new WaypointRep(
					"images/place.png","Helvetica",24, Qt::black);
	waypointReps["car park"] = new WaypointRep(
					"images/carpark.png", "Helvetica",8,Qt::blue);
	waypointReps["station"] = new WaypointRep(
					"images/station.png", "Helvetica",10,Qt::red);
	waypointReps["mast"] = new WaypointRep(
					"images/mast.png");
	waypointReps["locality"] = new WaypointRep(
					"Helvetica",12,Qt::black);
	waypointReps["point of interest"] = new WaypointRep
			("images/interest.png");
	waypointReps["suburb"] = new WaypointRep(
			"images/place.png","Helvetica",16, Qt::black);
	waypointReps["caution"] = new WaypointRep(
					"images/caution.png"); 
	waypointReps["amenity"] = new WaypointRep(
				   	"images/amenity.png","Helvetica",8, Qt::red);

	waypointReps["waypoint"] = new WaypointRep(
					"images/waypoint.png","Helvetica",8,Qt::black);

	waypointReps["campsite"] = new WaypointRep(
					"images/campsite.png","Helvetica",8,QColor(0,128,0));
	waypointReps["restaurant"] = new WaypointRep(
					"images/restaurant.png","Helvetica",8,QColor(128,0,0));
	waypointReps["bridge"] = new WaypointRep("images/bridge.png");
	waypointReps["tunnel"] = new WaypointRep("images/tunnel.png");
	waypointReps["tea shop"] = new WaypointRep(
					"images/teashop.png","Helvetica",8,Qt::magenta);
	waypointReps["country park"] = new WaypointRep("images/park.png",
							"Helvetica",8,QColor(0,192,0));
	waypointReps["industrial area"] = new WaypointRep("images/industry.png",
							"Helvetica",8,Qt::darkGray);
	waypointReps["barn"] = new WaypointRep("images/barn.png");
	curFilename = "";

	trackpoints=true;

	components = new Components;

	setFocusPolicy(QWidget::ClickFocus);

	showPosition();
}

MainWindow::~MainWindow()
{
	for (std::map<QString,WaypointRep*>::iterator i=waypointReps.begin();
		 i!=waypointReps.end(); i++)
	{
		delete i->second;
	}


	delete components;
}

void MainWindow::open()
{
	QString filename = QFileDialog::getOpenFileName("","*.gpx",this);
	if(filename!="")
	{
		Components *newComponents = doOpen(filename);
		if(newComponents)
		{
			cout << "!!! Deleting existing components !!!" << endl;
			delete components;	
			components = newComponents;
			curFilename = filename;
			update();

			components->toGPX("dump.gpx");
		}
	}
}

Components * MainWindow::doOpen(const QString& filename)
{
	Components * comp;


	GPXParser parser;
	QFile file(filename);
	QXmlInputSource source(&file);
	QXmlSimpleReader reader;
	reader.setContentHandler(&parser);
	reader.parse(source);
	comp = parser.getComponents();	
	
	return comp;
}

void MainWindow::readGPS()
{
	GPSDevice device ("Garmin", "/dev/ttyS0");
	Track *track = device.getTrack();
	cerr << "GPS read of track done. " << std::endl;
	Waypoints *waypoints = device.getWaypoints();
	cerr << "GPS read of waypoints done. " << std::endl;
	components->clearAll();
	components->addTrack(track);
	components->setWaypoints(waypoints);
	cerr<<"readGPS() done"<<endl;
	update();
}

void MainWindow::save()
{
	if(curFilename == "")
		saveAs();
	else
		saveFile(curFilename);
}

void MainWindow::saveAs()
{
	QString filename = QFileDialog::getSaveFileName("","*.gpx",this);
	if(filename!="")
		saveFile(filename);
}

void MainWindow::saveFile(const QString& filename)
{
	components->toGPX(filename);
	curFilename = filename;
}

void MainWindow::quit()
{
	QApplication::exit(0);
}

void MainWindow::setMode(int m)
{
	actionMode=  m;

	// Display the appropriate mode toolbar button and menu item checked, and
	// all the others turned off.
	for (int count=0; count<N_ACTIONS; count++)
		modeButtons[count]->setOn(count==m);
}

void MainWindow::setSegType(const QString &t)
{
	curSegType =   t;
}

void MainWindow::setPolygonType(const QString &t)
{
	curPolygonType =   t;
}

// 
void MainWindow::toggleWaypoints()
{
	trackpoints = !trackpoints;
	update();
}

void MainWindow::toggleLandsat()
{
	landsatManager.toggleDisplay();
	update();
}

void MainWindow::toggleContours()
{
	contours = !contours;
	update();
}

// 
void MainWindow::undo()
{
	//TODO
	update();	
}

// 
void MainWindow::changePolygonRes()
{
	polygonRes=QInputDialog::getDouble("Enter new polygon resolution:",
						"Enter new polygon resolution:", polygonRes,
						0,1,2);
}

void MainWindow::removeExcessPoints()
{
	RemoveExcessDialogue *rex=new RemoveExcessDialogue(this);
	if(rex->exec())
	{
		double angle=rex->getAngle(), distance=rex->getDistance();
		if(distance<0) cerr<<"DIST LESS THAN 0"<<endl;
		if(rex->reset() || !components->isCloned())
			components->cloneTrack();
		components->deleteExcessTrackPoints(angle*(M_PI/180),distance);
	}
	delete rex;
	update();
}

void MainWindow::commitExcessPoints()
{
	components->updateTrack();
	update();
}

void MainWindow::paintEvent(QPaintEvent* ev)
{
	QPainter p(this);
	drawLandsat(p);
	drawPolygons(p);
	curPainter = &p; // needed for the contour "callback"
	drawContours();
	drawTrack(p);
	drawWaypoints(p);
	curPainter = NULL;
}

void MainWindow::drawLandsat(QPainter& p)
{
	landsatManager.draw(p);
}

void MainWindow::drawPolygons(QPainter& p)
{
	for(int count=0; count<components->nPolygons(); count++)
	{
		drawPolygon(p,components->getPolygon(count));
	}
}

void MainWindow::drawContours()
{
	if(contours)
	{
		SRTMConGen congen(map,1);
		congen.generate(this);
	}
}

void MainWindow::drawContour(int x1,int y1,int x2,int y2,int r,int g,int b)
{
	if(curPainter)
	{
		curPainter->setPen(QColor(r,g,b));
		curPainter->drawLine(x1,y1,x2,y2);
	}
}

void MainWindow::drawAngleText(int fontsize,double angle,int x,int y,int r,
								int g, int b, char *text)
{
	if(curPainter)
	{
		angle*=-180/M_PI;
		curPainter->translate(x,y);
		curPainter->rotate(angle);
		curPainter->setFont(QFont("Helvetica",fontsize));
		curPainter->drawText(0,0,text);
		curPainter->rotate(-angle);
		curPainter->translate(-x,-y);
	}
}

void MainWindow::heightShading(int x1,int y1,int x2,int y2,int x3,int y3,
								int x4,int y4,int r,int g, int b)
{

}

void MainWindow::drawTrack(QPainter& p)
{
	doDrawTrack(p,false);
	if(components->isCloned())
	{
		components->setActiveCloned();
		doDrawTrack(p,true);
		components->setActiveNormal();
	}
}

void MainWindow::doDrawTrack(QPainter& p, bool doingClonedTrack)
{
	if(components->hasTrack())
	{
		QPen trackPen(Qt::darkGray,2); 
		TrackPoint curPt;
		TrackSeg *curSeg;
		ScreenPos lastPos = map.getScreenPos(curPt.lon,curPt.lat), curPos,
				  avPos;
		QPen curPen=(doingClonedTrack) ? QPen(QColor(255,0,128),2): 
												trackPen;
		QFont f("Helvetica",8);
		QFontMetrics fm(f);
		QString segname;

		for(int seg=0; seg<components->nSegs(); seg++)
		{
			curSeg = components->getSeg(seg);
			if(!doingClonedTrack) curPen = segpens[curSeg->getType()];
			curPt = curSeg->getPoint(0);
			lastPos = map.getScreenPos(curPt.lon,curPt.lat);
			for(int pt=1; pt<curSeg->nPoints(); pt++)
			{
				curPt = curSeg->getPoint(pt);	
				curPos = map.getScreenPos(curPt.lon,curPt.lat);	
				p.setPen(curPen);
				p.drawLine(lastPos.x,lastPos.y,curPos.x,curPos.y);
				if(pt==curSeg->nPoints()/2 && (segname=curSeg->getID())!="")
				{
					QRect r=fm.boundingRect(segname);
					avPos.x = (curPos.x+lastPos.x)/2;
					avPos.y = (curPos.y+lastPos.y)/2;
					p.fillRect(avPos.x-r.width()/2-2,
								avPos.y-fm.ascent()-2,
								r.width()+4,
								fm.height()+4,Qt::yellow);
//								curPen.color());
					p.setPen(QColor(128,0,0));
					p.setFont(f);
					p.drawText(avPos.x-r.width()/2,
								avPos.y,
								segname);
				}
				/*
				if(curPt.lon==p1.x && curPt.lat==p1.y)//=selectedTrackpoint)
					drawTrackpoint(p,Qt::red,curPos.x,curPos.y,10);
				else  */
				if(trackpoints)
				{
					if(doingClonedTrack)
						drawTrackpoint(p,curPen.color(),curPos.x,curPos.y,8);
					else
						drawTrackpoint(p,curPen.color(),curPos.x,curPos.y,1,pt);
				}
				lastPos = curPos;

			}
		}

		if(nSelectedPoints)
		{
			ScreenPos sp=map.getScreenPos(p1);
			drawTrackpoint(p,Qt::red,sp.x,sp.y,10);
		}
	}
}


void MainWindow::drawWaypoints(QPainter& p)
{
	if(components->hasWaypoints())
	{
		for(int count=0; count<components->nWaypoints(); count++)
		{
			drawWaypoint(p,components->getWaypoint(count));
		}
	}
}


void MainWindow::drawWaypoint(QPainter& p,const Waypoint &waypoint)
{
	if(components->hasWaypoints())
	{
	ScreenPos pos = map.getScreenPos(waypoint.lon,waypoint.lat);
	WaypointRep* img=waypointReps[waypoint.type];
	if(img)img->draw(p,pos.x,pos.y,waypoint.name);
	}
}


void MainWindow::drawTrackpoint (QPainter& p,const QBrush& brush,int x,int y,
				int d) 
{
	p.setBrush (brush);
	p.drawEllipse ( x-d/2, y-d/2, d, d);
}
	
void MainWindow::drawTrackpoint (QPainter& p,const QBrush& brush,
									int x,int y, int id,int point)
{
	drawTrackpoint(p,brush,x,y,5);
	QString label;
	label.sprintf("%d",point);
	p.setFont(QFont("Helvetica",8));
//	p.setPen(QColor(128,0,255));
	p.drawText ( x+5, y+5, label);
}

void MainWindow::drawPolygon(QPainter & p, Polygon * polygon)
{
	ScreenPos current;

	QPointArray qpointarray(polygon->size());


	for(int count=0; count<polygon->size(); count++)
	{
		current = map.getScreenPos(polygon->getPoint(count));
		qpointarray.setPoint(count,current.x,current.y);
	}

	p.setPen(polydata[polygon->getType()]);
	p.setBrush(QBrush(polydata[polygon->getType()].color(),Qt::SolidPattern));
	p.drawPolygon(qpointarray);
	p.setBrush(Qt::NoBrush);

}


void MainWindow::mousePressEvent(QMouseEvent* ev)
{
	EarthPoint p;
	QString name;
	double LIMIT=map.earthDist(10);
	int nearest;

	switch(actionMode)
	{
		case ACTION_TRACK:
			if(nSelectedPoints==0)
			{
				p1 = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				update();
				nSelectedPoints++;
			}
			else
			{
				p2 = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				components->segmentiseTrack(curSegType,p1,p2,LIMIT);
				update();
				nSelectedPoints=0;
			}
			break;

		case ACTION_POLYGON:
			initPolygon();
			break;

		case ACTION_DELETE:
			if(nSelectedPoints==0)
			{
				p1 = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				nSelectedPoints++;
			}
			else
			{
				p2 = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				components->deleteTrackpoints(p1,p2,LIMIT);
				update();
				nSelectedPoints=0;
			}
			break;

		case ACTION_WAYPOINT:
			editWaypoint(ev->x(),ev->y(),10);	
			break;

		case ACTION_MOVE_WAYPOINT:
			if(wptSaved)
			{
				p = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				savedWpt.lat = p.y;
				savedWpt.lon = p.x;
				components->addWaypoint(savedWpt);
				wptSaved=false;
				update();
			}
			else if((nearest=findNearestWaypoint(ev->x(),ev->y(),10))!=-1)
			{
				savedWpt = components->getWaypoint(nearest);
				components->deleteWaypoint(nearest);
				wptSaved=true;
				update();
			}
			break;
	}			
}

void MainWindow::resizeEvent(QResizeEvent * ev)
{
	map.resizeTopLeft(width(), height());
	update();
}

void MainWindow::initPolygon()
{
	curPolygonPts.clear();
	mouseDown=true;
}


// TODO: the first part of this is doing almost exactly the same as 
// findNearestTrackpoint(), above. Investigate the possibility of doing this
// more elegantly.
void MainWindow::editWaypoint(int x,int y,int limit)
{
	int nearest=-1;
	WaypointDialogue *d;

	if(components->hasWaypoints())
	{
		if((nearest=findNearestWaypoint(x,y,limit)) != -1)
		{
			d = new WaypointDialogue
					(this,waypointReps,"Edit waypoint",
					components->getWaypoint(nearest).type,
					components->getWaypoint(nearest).name);
			if(d->exec())
			{
				components->alterWaypoint(nearest,d->getName(), d->getType());
			}
		}
		else
		{
			d = new WaypointDialogue
					(this,waypointReps,"Add waypoint","waypoint","");
			if(d->exec())
			{
				EarthPoint p = map.getEarthPoint(ScreenPos(x,y));
				components->addWaypoint(Waypoint(d->getName(),p.y,p.x,
										d->getType()));
			}
		}
		update();
		delete d;
	}
}

int MainWindow::findNearestWaypoint(int x,int y,int limit)
{
	ScreenPos curPos;
	double prevDist = limit, dist;
	int nearest=-1;
	Waypoint curPt;

	for(int count=0; count<components->nWaypoints(); count++)
	{
		curPt = components->getWaypoint(count);
		curPos = map.getScreenPos(curPt.lon,curPt.lat);	
		if((dist=OpenStreetMap::dist(x,y,curPos.x,curPos.y))<limit)
		{
			if(dist<prevDist)
			{
				prevDist=dist;
				nearest=count;
			}
		}
	}

	return nearest;
}

void MainWindow::mouseMoveEvent(QMouseEvent* ev)
{

	if(actionMode==ACTION_POLYGON && mouseDown)
	{
		QPainter p (this);
		p.setPen(polydata[polygon->getType()]);
		if(curPolygonPts.size()>=1)
		{
			ScreenPos prev = *(curPolygonPts.end()-1);

			p.drawLine(prev.x,prev.y,ev->x(),ev->y());	

			// Only add if a given distance to last point -
			// otherwise Freemap will be overloaded with polygon 
			// points !!!
			// 0.1 maybe a tad too coarse, 0.05 more definitely too fine.
			/*
			if(OpenStreetMap::dist (prev.x,prev.y,ev->x(),ev->y())
				>= polygonRes*1000*map.getScale()) 
			{
			*/
				curPolygonPts.push_back
						(ScreenPos(ev->x(),ev->y()));
			//}
		}
		else
		{
			curPolygonPts.push_back (ScreenPos(ev->x(),ev->y()));
		}
	}
}

void MainWindow::mouseReleaseEvent(QMouseEvent* ev)
{
	EarthPoint p;
	QString name;
	double LIMIT=map.earthDist(10);
	cout<<"action mode:" << actionMode<<endl;
	switch(actionMode)
	{

		case ACTION_POLYGON:
			endPolygon(ev->x(),ev->y());
			break;
		case ACTION_NAME_TRACK:
			p = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
			name=QInputDialog::getText("Enter track name","Enter track name");
			components->nameTrackSeg(p,name,LIMIT);
			break;
	}
}

void MainWindow::endPolygon(int x,int y)
{
	vector<ScreenPos>::iterator i;
	curPolygonPts.push_back(ScreenPos(x,y));
	polygon = new Polygon(curPolygonType);

	for(i=curPolygonPts.begin(); i!=curPolygonPts.end(); i++)
		polygon->addPoint(map.getEarthPoint(*i));	

	components->addPolygon(polygon);

	update();
}

	
void MainWindow::keyPressEvent(QKeyEvent* ev)
{
	// 11/04/05 prevent movement being too far at large scales
	double dis = 0.1/map.getScale();

	switch(ev->key())
	{
		case Qt::Key_Left  : left(); break; 
		case Qt::Key_Right : right(); break; 
		case Qt::Key_Up    : up(); break; 
		case Qt::Key_Down  : down(); break; 
		case Qt::Key_Plus  : magnify(); break; 
		case Qt::Key_Minus : shrink(); break; 
	}
}

void MainWindow::left()
{
	double dis = 0.1/map.getScale();
	map.move(-dis,0);
	updateWithLandsatCheck();
}

void MainWindow::right()
{
	double dis = 0.1/map.getScale();
	map.move(dis,0);
	updateWithLandsatCheck();
}

void MainWindow::up()
{
	double dis = 0.1/map.getScale();
	map.move(0,dis);
	updateWithLandsatCheck();
}

void MainWindow::down()
{
	double dis = 0.1/map.getScale();
	map.move(0,-dis);
	updateWithLandsatCheck();
}

void MainWindow::magnify()
{
	map.rescale(2);
	landsatManager.grab();
	showPosition();
	update();
}

void MainWindow::shrink()
{
	map.rescale(0.5);
	landsatManager.grab();
	showPosition();
	update();
}

void MainWindow::updateWithLandsatCheck()
{
	if(landsatManager.needMoreData())
		landsatManager.forceGrab();
	showPosition();
	update();
}
void MainWindow::grabTracks()
{
#if !defined(XMLRPC)
	QMessageBox::information(this,"XMLRPC not enabled",
				 "Not able to log into OpenStreetmap, as XMLRPC support was not compiled into the program.");
#else
	QString username, password;
	LoginDialogue *d = new LoginDialogue(this);
	if(d->exec())
	{
		username = d->getUsername();
		password = d->getPassword();
		std::string token;
		XmlRpcValue result;
		XmlRpcValue param_array;
		XmlRpcClient::Initialize("test","0.1");
		try
		{
			XmlRpcClient client("http://www.openstreetmap.org/api/xml.jsp");
			param_array = XmlRpcValue::makeArray();
			param_array.arrayAppendItem
					(XmlRpcValue::makeString(username.ascii()));
			param_array.arrayAppendItem
					(XmlRpcValue::makeString(password.ascii()));

			result = client.call("openstreetmap.login",param_array);
			if((token=result.getString())!="ERROR")
			{
				QMessageBox::information(this,"Login successful",
											"Login successful");
				EarthPoint llSW=map.getBottomLeft();
				EarthPoint llNE=map.getEarthPoint(ScreenPos(width(),height()));
				
				
				param_array = XmlRpcValue::makeArray();
				param_array.arrayAppendItem(XmlRpcValue::makeString(token));
				param_array.arrayAppendItem
						(XmlRpcValue::makeDouble(llSW.y));
				param_array.arrayAppendItem
						(XmlRpcValue::makeDouble(llSW.x));
				param_array.arrayAppendItem
						(XmlRpcValue::makeDouble(llNE.y));
				param_array.arrayAppendItem
						(XmlRpcValue::makeDouble(llNE.x));
				result=client.call("openstreetmap.getPoints",param_array);
				XmlRpcValue array=result.getArray();
				QString wpName;
				for(int count=0; count<array.arraySize(); count+=2)
				{
					XmlRpcValue value = array.arrayGetItem(count);
					double curLat = value.getDouble();
					value = array.arrayGetItem(count+1);
					double curLon = value.getDouble();
					wpName.sprintf("OSM-%03d",count);	
					components->addWaypoint
							(Waypoint(wpName,curLat,curLon,"waypoint"));
				}
				update();	
			}
			else
			{
				QMessageBox::warning(this,"Can't login",
								"OpenStreetMap did not recognise your login");
			}
		}
		catch (XmlRpcFault& fault)
		{
			QMessageBox::warning(this,"Fault",fault.getFaultString().c_str());
		}
	}
	else
	{
		QMessageBox::warning(this,"Login dialog box failed",
				     "Unable to log in");
	}
#endif
}

void MainWindow::grabLandsat()
{
	// 01/05/05 grab three times current screen width and height (i.e. 9 
	// times screen area) and centre at current
	// map centre. This will be configurable.
	

	landsatManager.forceGrab();
	update();


}

// ripped off from Mapmaker

QPixmap mmLoadPixmap(const QString& directory, const QString& filename) 
{
	QString fullpath;

	if (directory != "")
	{
		fullpath = directory+"/"+filename;
	}
	else
	{
		fullpath=filename;
	}

	QPixmap pixmap (fullpath);

	// if (pixmap.isNull()) ...
			

	return pixmap;
}

}
