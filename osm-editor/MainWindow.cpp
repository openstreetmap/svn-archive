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
#include <cmath>

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

#include "curlstuff.h"

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
									polygonRes(0.05),
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

	doingName = false;

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
	fileMenu->insertItem("Open OSM",this,SLOT(openOSM()));
	fileMenu->insertItem("&Save",this,SLOT(save()),CTRL+Key_S);
	fileMenu->insertItem("Save &as...",this,SLOT(saveAs()),CTRL+Key_A);
	fileMenu->insertItem("Save OSM...",this,SLOT(saveOSM()));
	fileMenu->insertItem("&Read GPS",this,SLOT(readGPS()),CTRL+Key_R);
	fileMenu->insertItem("&Grab Landsat",this,SLOT(grabLandsat()),CTRL+Key_G);
	fileMenu->insertItem("Grab GPX from Net",this,SLOT(grabGPXFromNet()),
								CTRL+Key_N);
	fileMenu->insertItem("Post GPX",this,SLOT(postGPX()),CTRL+Key_P);
//	fileMenu->insertItem("Login",this,SLOT(login()),CTRL+Key_I);
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
	editMenu->insertItem("Remove plain trac&ks",this,SLOT(removePlainTracks()),
						CTRL+Key_K);
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
	QPixmap linknewpoint = mmLoadPixmap("images","linknewpoint.png");
	QPixmap formnewseg = mmLoadPixmap("images","formnewseg.png");
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
	modeButtons[ACTION_LINK]= new QToolButton
			(linknewpoint,"Link point to seg","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_NEW_SEG]= new QToolButton
			(formnewseg,"New segment","",mapper,SLOT(map()),toolbar);


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
	waypointReps["hill"] = new WaypointRep(
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
	waypointReps["railway station"] = new WaypointRep(
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
	open2(false);
}

void MainWindow::openOSM()
{
	open2(true);
}

void MainWindow::open2(bool osm)
{
	QString filename = QFileDialog::getOpenFileName("","*.gpx",this);
	if(filename!="")
	{
		Components *newComponents = doOpen(filename,osm);
		if(newComponents)
		{
			cout << "!!! Deleting existing components !!!" << endl;
			delete components;	
			components = newComponents;
			curFilename = filename;
			update();
		}
	}
}

Components * MainWindow::doOpen(const QString& filename, bool osm)
{
	Components * comp;


	GPXParser parser;
	parser.setOSM(osm);
	QFile file(filename);
	QXmlInputSource source(&file);
	QXmlSimpleReader reader;
	reader.setContentHandler(&parser);
	cerr<<"PARSING GPX"<<endl;
	reader.parse(source);
	cerr<<"DONE PARSING GPX"<<endl;
	comp = parser.getComponents();	
	cerr<<"DONE doOpen"<<endl;	
	return comp;
}

void MainWindow::grabGPXFromNet()
{
	QString url=QInputDialog::getText("Enter URL",
										"Enter URL of GPX server:");
	Components * netComponents; 
	statusBar()->message("Grabbing GPX...");
	EarthPoint bottomLeft = ll_to_gr(map.getBottomLeft()),
			   topRight = ll_to_gr(map.getTopRight());
	CURL_LOAD_DATA *httpResponse = grab_gpx(url.ascii(),
					bottomLeft.x,bottomLeft.y,topRight.x,topRight.y);
	if(httpResponse)
	{
		statusBar()->message("Done.");
		char *strdata = new char[httpResponse->nbytes+1];
		memcpy(strdata,httpResponse->data,httpResponse->nbytes);
		strdata[httpResponse->nbytes]='\0';
		free(httpResponse->data);
		free(httpResponse);

//		cout << "Response:" << endl << strdata << endl;

		
		QString gpx(strdata);
		QXmlInputSource source;
		source.setData(gpx);

		GPXParser parser;
		parser.setOSM(true);
		QXmlSimpleReader reader;
		reader.setContentHandler(&parser);
		reader.parse(source);
		netComponents = parser.getComponents();	

		if(components)
		{
			components->merge(netComponents);
			delete netComponents;
		}
		else
		{
			components = netComponents;
		}
		
		

		delete[] strdata;
	}
}

/* UPDATED 151005 not present in Windows version 141005 */
void MainWindow::postGPX()
{
	LoginDialogue *ld=new LoginDialogue(this);
	if(ld->exec())
	{
		QString url=ld->getURL(), username=ld->getUsername(),
					password=ld->getPassword();
		std::string gpx = components->toGPX(true);
		char *cgpx = new char[gpx.size()+1];

		// necessary because a curl function has no const
		strcpy(cgpx,gpx.c_str()); 

		statusBar()->message("Posting map data...");
		CURL_LOAD_DATA *resp = post_gpx(url,cgpx,username,password);
		statusBar()->message("Done.");
		delete[] cgpx;

		char *strdata = new char[resp->nbytes+1];
		memcpy(strdata,resp->data,resp->nbytes);
		strdata[resp->nbytes]='\0';
		free(resp->data);
		free(resp);

//		cout << "Response:" << endl << strdata << endl;
		delete[] strdata;
	}
	delete ld;
}
/* END UPDATE */

void MainWindow::removePlainTracks()
{
	components->removeSegs("track");
	update();
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

void MainWindow::saveOSM()
{
	QString filename = QFileDialog::getSaveFileName("","*.gpx",this);
	if(filename!="")
		saveFile(filename,true);
}

void MainWindow::saveFile(const QString& filename, bool osm)
{
	components->toGPX(filename,osm);
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

	// Wipe any currently selected points
	nSelectedPoints = 0;
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
		//angle*=-180/M_PI;
		curPainter->setFont(QFont("Helvetica",fontsize));
		doDrawAngleText(curPainter,x,y,x,y,angle,text);
	}
}

void MainWindow::doDrawAngleText(QPainter *p,int originX,int originY,int x,
				int y,double angle, const char * text)
{
	angle *= 180/M_PI;
	p->translate(originX,originY);
	p->rotate(angle);
	p->drawText(x-originX,y-originY,text);
	p->rotate(-angle);
	p->translate(-originX,-originY);
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
		TrackPoint curPt,prevPt;
		TrackPoint junk;
		TrackSeg *curSeg;
		ScreenPos lastPos , curPos, lastDrawnPos,
				  avPos,
				  maxP1, maxP2;

		QPen curPen=(doingClonedTrack) ? QPen(QColor(255,0,128),2): 
												trackPen;
		QFont f("Helvetica",10,QFont::Bold,true);
		QFontMetrics fm(f);
		QString segname;

		bool onScreen, overlappingPts = false;

		double dx, dy, dist, maxDist=0;

		for(int seg=0; seg<components->nSegs(); seg++)
		{
			curSeg = components->getSeg(seg);
			if(!doingClonedTrack) 
			{
				/* UPDATE 15/10/05 not present in Windows 141005 */
				curPen = (curSeg==selectedSeg) ? QPen(Qt::yellow,5) :
						(curSeg->isLocked()) ? 
							QPen(QColor(255,200,138),2)	 : 
								segpens[curSeg->getType()];
				/* END UPDATE */
			}

			maxDist = 0;
			curPt = curSeg->getPoint(0);
			prevPt = curPt;
			junk = curPt;
			lastPos = map.getScreenPos(curPt.lon,curPt.lat);
			lastDrawnPos = lastPos;

			maxP1  = maxP2 =  lastPos;
			for(int pt=0; pt<curSeg->nPoints(); pt++)
			{
				curPt = curSeg->getPoint(pt);	
				curPos = map.getScreenPos(curPt.lon,curPt.lat);	
				p.setPen(curPen);


				if(pt)
				{
					onScreen = 
					((curPos.x>=0 && curPos.x<width() &&
					  curPos.y>=0 && curPos.y<height()) ||
					 (lastPos.x>=0 && lastPos.x<width() &&
					  lastPos.y>=0 && lastPos.y<height()));
		
					// Overlapping points are those for which the difference
					// in both x and y is 2 or less.
					overlappingPts = 
							abs(curPos.x-lastDrawnPos.x)<=2 && 
							 abs(curPos.y-lastDrawnPos.y) <= 2;

				

					/** NEW  */
					// Disconnect tracks surveyed over separate occasions
				

					// 21/09/05 Only draw line if at least one point is on 
					// screen and the two points are not occupying the same 
					// screen position

					if( onScreen && !overlappingPts && 
					(curSeg->getType()!="track"||
					 curPt.connected(junk,750,2)))
					{
						p.drawLine(lastDrawnPos.x,lastDrawnPos.y,
									curPos.x,curPos.y);
					}
				
					dy = curPos.y - lastPos.y; 
					dx = curPos.x - lastPos.x;

					dist = sqrt(dy*dy + dx*dx);

					// To find the maximum inter-point length in the current 
					// segment, for displaying the segment name. Only count
					// if in the middle half of the segment, to avoid the name
					// "hanging off the end"
					if(dist>maxDist 
						&& pt>=curSeg->nPoints()/4 && pt<curSeg->nPoints()*3/4)
					{
						maxDist = dist;
						maxP1 = lastPos; 
						maxP2 = curPos;
					}
				}

				// 21/09/05 Don't draw the point if its screen position is the
				// same as the last one
				if( !overlappingPts &&
					curPos.x>=0 && curPos.x<width() && curPos.y>=0 &&
					curPos.y<height())
				{
					if(trackpoints)
					{
						if(doingClonedTrack)
							drawTrackpoint(p,curPen.color(),curPos.x,
											curPos.y,8);
						else
							drawTrackpoint(p,curPen.color(),curPos.x,curPos.y,
											1,pt);
					}
					lastDrawnPos = curPos;
					junk = curPt;
				}

				for (int selPt=0; selPt<nSelectedPoints; selPt++)
				{
					if(pts[selPt]==curPt)
						drawTrackpoint(p,Qt::red,curPos.x,curPos.y,10);
				}
				lastPos = curPos;
				prevPt = curPt;
			}

			// Write name of segment on longest line  
			if((segname=curSeg->getName())!="")
			{
				dy=maxP2.y-maxP1.y;
				dx=maxP2.x-maxP1.x;
				double angle = atan2(dy,dx);
				p.setFont(f);
				doDrawAngleText(&p,maxP1.x,maxP1.y,maxP1.x,maxP1.y,
								angle,segname.ascii());
			}
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
	EarthPoint pt;
	QString name;
	double LIMIT=map.earthDist(10);
	int nearest;

	switch(actionMode)
	{
		case ACTION_TRACK:
			if(nSelectedPoints==0)
			{
				pts[0] = components->findNearestTrackpoint
						(map.getEarthPoint(ScreenPos(ev->x(),ev->y())),LIMIT);
				update();
				nSelectedPoints++;
			}
			else
			{
				pts[1] = components->findNearestTrackpoint(map.getEarthPoint(ScreenPos(ev->x(),ev->y())),LIMIT);
				components->segmentiseTrack(curSegType,pts[0],pts[1],LIMIT);
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
				pts[0] = components->findNearestTrackpoint(map.getEarthPoint(ScreenPos(ev->x(),ev->y())),LIMIT);
				update();
				nSelectedPoints++;
			}
			else
			{
				pts[1] = components->findNearestTrackpoint(map.getEarthPoint(ScreenPos(ev->x(),ev->y())),LIMIT);
				components->deleteTrackpoints(pts[0],pts[1],LIMIT);
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
				pt = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				savedWpt.lat = pt.y;
				savedWpt.lon = pt.x;
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

		case ACTION_LINK:
			if(nSelectedPoints<2)
			{
				pts[nSelectedPoints++] = 
						components->findNearestTrackpoint(map.getEarthPoint(ScreenPos(ev->x(),ev->y())),LIMIT);
				update();
			}
			else
			{
				EarthPoint ep=map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				pts[2] = components->findNearestTrackpoint(ep,LIMIT);
				if(pts[2].null())
				{
					components->linkNewPoint(pts[0],pts[1],ep,LIMIT);
				}
				else
				{
					components->linkNewPoint(pts[0],pts[1],pts[2],LIMIT);
				}
				update();
				nSelectedPoints=0;
			}
			break;

		case ACTION_NEW_SEG:
			if(nSelectedPoints==0)
			{
				pts[0] = components->findNearestTrackpoint(map.getEarthPoint(ScreenPos(ev->x(),ev->y())),LIMIT);
				update();
				nSelectedPoints++;
			}
			else
			{
				pts[1] = components->findNearestTrackpoint(map.getEarthPoint(ScreenPos(ev->x(),ev->y())),LIMIT);
				components->formNewSeg(curSegType,pts[0],pts[1],LIMIT);
				update();
				nSelectedPoints=0;
			}
			break;

	}			
}

void MainWindow::resizeEvent(QResizeEvent * ev)
{
	map.resizeTopLeft(width(), height());
	landsatManager.grab();
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
			EarthPoint prevLL = *(curPolygonPts.end()-1),
					   prevGR = ll_to_gr(prevLL),
					   currentLL = map.getEarthPoint(ev->x(),ev->y()),
					   curGR = ll_to_gr(currentLL);
			ScreenPos  prevScreenPos = map.getScreenPos(prevLL);

			p.drawLine(prevScreenPos.x,prevScreenPos.y,ev->x(),ev->y());	

			// Only add if a given distance to last point -
			// otherwise Freemap will be overloaded with polygon 
			// points !!!
			// 0.1 maybe a tad too coarse, 0.05 more definitely too fine.
			
			if(OpenStreetMap::dist (prevGR.x,prevGR.y,curGR.x,curGR.y)
				>= polygonRes*1000) 
			{
			
				curPolygonPts.push_back(currentLL);
			}
		}
		else
		{
			curPolygonPts.push_back (map.getEarthPoint(ev->x(),ev->y()));
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
			selectedSeg = components->getNearestSeg(p,LIMIT);
			if(selectedSeg)
			{
			trackName = "";
			int tp = selectedSeg->findNearestTrackpoint(p,map.earthDist(100));
			if (tp>=0 && tp<selectedSeg->nPoints()-1)
			{
				TrackPoint pt1 = selectedSeg->getPoint(tp),
						   pt2 = selectedSeg->getPoint(tp+1);
				double dy=pt2.lat-pt1.lat;
				double dx=pt2.lon-pt1.lon;
				nameAngle = atan2(dy,dx);
				doingName = true;
				namePos = map.getScreenPos (pt1.lon,pt1.lat);
				curNamePos = namePos;
			}
			update();
			}
			break;
	}
}

void MainWindow::endPolygon(int x,int y)
{
	vector<EarthPoint>::iterator i;
	curPolygonPts.push_back(map.getEarthPoint(x,y));
	polygon = new Polygon(curPolygonType);

	for(i=curPolygonPts.begin(); i!=curPolygonPts.end(); i++)
//		polygon->addPoint(map.getEarthPoint(*i));	
		polygon->addPoint(*i);	

	components->addPolygon(polygon);

	update();
}

	
void MainWindow::keyPressEvent(QKeyEvent* ev)
{
	if(actionMode==ACTION_NAME_TRACK && doingName)
	{
		if(ev->ascii()>=32 && ev->ascii()<=127)
		{
			trackName += ev->text();
			QPainter p(this);
			p.setPen(Qt::black);
			QFont f ("Helvetica",10,QFont::Bold,true);
			p.setFont(f);
			QFontMetrics fm(f);

			cerr << "calling doDrawAngleText " << endl;
			doDrawAngleText(&p,namePos.x,namePos.y,curNamePos.x,
							curNamePos.y,
							-nameAngle,ev->text());
			curNamePos.x += fm.width(ev->text());

		}
		else if (ev->key()==Key_Return)
		{
			doingName = false;
			selectedSeg->setName(trackName);
			selectedSeg = NULL;
			update();
		}
	}
	else
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
			case Qt::Key_H     : screenLeft(); break;
			case Qt::Key_J     : screenDown(); break;
			case Qt::Key_K     : screenUp(); break;
			case Qt::Key_L     : screenRight(); break;
		}
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

void MainWindow::screenLeft()
{
	map.movePx(-width(),0);
	landsatManager.grab(1.0);	
	update();
}

void MainWindow::screenRight()
{
	map.movePx(width(),0);
	landsatManager.grab(1.0);	
	update();
}

void MainWindow::screenUp()
{
	map.movePx(0,-height());
	landsatManager.grab(1.0);	
	update();
}

void MainWindow::screenDown()
{
	map.movePx(0,height());
	landsatManager.grab(1.0);	
	update();
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
