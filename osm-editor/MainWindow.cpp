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

#include <iostream>
#include <sstream>
#include <cstdlib>

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
									map(lat,lon,s/1000), 
									curPolygonType(POLYGON_WOOD),
									polygonRes(0.1)
{
	setCaption("OpenStreetMap Editor");
	resize ( w*s, h*s );	

	actionMode = ACTION_TRACK;
	curSegType = "A road"; 
	selectedTrackpoint = -1;

	segpens["footpath"]= QPen (Qt::green, 2);
	segpens["cycle path"]= QPen (Qt::magenta, 2);
	segpens["bridleway"]=QPen(QColor(192,96,0),2);
	segpens["byway"] = QPen (Qt::red, 2);
	segpens["minor road"]= QPen (Qt::black, 2);
	segpens["B road"]= QPen (Qt::black, 3);
	segpens["A road"]= QPen (Qt::black, 4);
	segpens["motorway"]= QPen (Qt::black, 6);
	segpens["railway"]= QPen (Qt::gray, 4);

	polydata.push_back(PolyData("wood",QColor(192,224,192)));
	polydata.push_back(PolyData("lake",QColor(192,192,255))); 
	polydata.push_back(PolyData("heath",QColor (255,224,192)));
	polydata.push_back(PolyData("urban",QColor (128,128,128)));
	polydata.push_back(PolyData("access land",QColor (192,0,192)));


	// Construct the menus.
	QPopupMenu* fileMenu = new QPopupMenu(this);
	fileMenu->insertItem("&Open",this,SLOT(open()),CTRL+Key_O);
	fileMenu->insertItem("&Save",this,SLOT(save()),CTRL+Key_S);
	fileMenu->insertItem("Save &as...",this,SLOT(saveAs()),CTRL+Key_A);
	fileMenu->insertItem("&Read GPS",this,SLOT(readGPS()),CTRL+Key_R);
	fileMenu->insertItem("&Grab tracks",this,SLOT(grabTracks()),CTRL+Key_G);
	menuBar()->insertItem("&File",fileMenu);

	QPopupMenu* editMenu = new QPopupMenu(this);
	
	editMenu->insertItem("Re&name feature",this,
					SLOT(renameFeature()),CTRL+Key_N);
	editMenu->insertItem("&Toggle waypoints",this,SLOT(toggleWaypoints()),
						CTRL+Key_T);
	editMenu->insertItem("Undo",this,SLOT(undo()),CTRL+Key_Z);
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
	QObject::connect(r,SIGNAL(activated(const QString&)),
						this,SLOT(setSegType(const QString&)));	
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
	modeButtons[ACTION_TRACK] = new QToolButton
			(one,"Create Segments","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_DELETE] = new QToolButton
			(two,"Delete Trackpoints","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_WAYPOINT]= new QToolButton
			(wp,"Edit Waypoints","",mapper,SLOT(map()),toolbar);
	modeButtons[ACTION_POLYGON]= new QToolButton
			(three,"Create Polygons","",mapper,SLOT(map()),toolbar);
	// Setting a blank label as the stretchable widget means that one can
	// stretch the toolbar while keeping the tool buttons bunched up to the 
	// left.
	toolbar->setStretchableWidget(new QLabel(toolbar));

	// Turn the "mode" toolbar buttons into toggle buttons, and set their
	// mapping index for the signal mapper.
	
	for (int count=0; count<3; count++)
	{
		modeButtons[count]->setToggleButton(true);
		mapper->setMapping(modeButtons[count],count);
	}
	
	modeButtons[ACTION_TRACK]->setOn(true);

	// The final stage of implementing the signal mapper: connect mapped()
	// to setMode(). The value set in setMapping() above will be used.
	QObject::connect(mapper,SIGNAL(mapped(int)),this,SLOT(setMode(int)));
	curPolygon=NULL;


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
	curFilename = "";

	trackpoints=true;

	components = new Components;

	setFocusPolicy(QWidget::ClickFocus);
}

MainWindow::~MainWindow()
{
	for (std::map<QString,WaypointRep*>::iterator i=waypointReps.begin();
		 i!=waypointReps.end(); i++)
	{
		delete i->second;
	}

	for(vector<Polygon*>::iterator i=polygons.begin(); i!=polygons.end(); i++)
		delete *i;

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

void MainWindow::setMode(int m)
{
	actionMode=  m;
	cout << "New mode: "<< m << endl;

	// Display the appropriate mode toolbar button and menu item checked, and
	// all the others turned off.
	for (int count=0; count<3; count++)
		modeButtons[count]->setOn(count==m);
}

void MainWindow::setSegType(const QString &t)
{
	curSegType =   t;
	cerr << curSegType << endl;
}

void MainWindow::renameFeature()
{
	if(1)
	{
		QString oldname = QInputDialog::getText("Old name:","Old name:");
		if(oldname!="")
		{
			QString newname = QInputDialog::getText("New name:","New name:");
			if(newname!="")
			{
				// TODO	
			}
		}
	}
	update();
}

// 
void MainWindow::toggleWaypoints()
{
	trackpoints = !trackpoints;
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

void MainWindow::paintEvent(QPaintEvent* ev)
{
	QPainter p (this);

	drawPolygons(p);
	drawTrack(p);
	drawWaypoints(p);
}

void MainWindow::drawPolygons(QPainter& p)
{
	for(vector<Polygon*>::const_iterator i= polygons.begin();
		i != polygons.end(); i++)
	{
		drawPolygon(p,*i);
	}
}

void MainWindow::drawTrack(QPainter& p)
{
	if(components->hasTrack())
	{
	QPen trackPen(Qt::darkGray,2); 
	int curSeg = 0;
	TrackPoint curPt = components->getTrackpoint(0);
	ScreenPos lastPos = map.getScreenPos(curPt.lat,curPt.lon), curPos;
	QPen curPen=trackPen;
	SegDef segdef;
	if(components->nSegdefs()) segdef=components->getSegdef(0);

	for(int count=1; count<components->nTrackpoints(); count++)
	{
		if(count-1 == segdef.end)
		{
			curPen=trackPen;
			if(curSeg<components->nSegdefs()-1)
			{
				curSeg++;
				segdef=components->getSegdef(curSeg);
			}
		}

		if(count-1 == segdef.start)
		{
			curPen=segpens[segdef.type];
		}

		curPt = components->getTrackpoint(count);	
		curPos = map.getScreenPos(curPt.lat,curPt.lon);	
		p.setPen(curPen);
		p.drawLine(lastPos.x,lastPos.y,curPos.x,curPos.y);
		if(count==selectedTrackpoint)
			drawTrackpoint(p,Qt::red,curPos.x,curPos.y);
		else if(trackpoints)
			drawTrackpoint(p,curPen,curPos.x,curPos.y,1,count);
		lastPos = curPos;
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
	ScreenPos pos = map.getScreenPos(waypoint.lat,waypoint.lon);
	WaypointRep* img=waypointReps[waypoint.type];
	img->draw(p,pos.x,pos.y,waypoint.name);
	}
}


void MainWindow::drawTrackpoint (QPainter& p,const QPen& pen,int x,int y) 
{
	p.setPen (pen);
	p.drawEllipse ( x-2, y-2, 5, 5 );
}
	
void MainWindow::drawTrackpoint (QPainter& p,const QPen& pen,
									int x,int y, int id,int point)
{
	drawTrackpoint(p,pen,x,y);
	QString label;
	label.sprintf("%d",point);
	p.setFont(QFont("Helvetica",8));
	p.setPen(QColor(128,0,255));
	p.drawText ( x+5, y+5, label);
}

void MainWindow::drawPolygon(QPainter & p, Polygon * polygon)
{
	ScreenPos current;

	QPointArray qpointarray(polygon->size());

	int count=0;

	for(Polygon::iterator i=polygon->begin(); i!=polygon->end(); i++)
	{
		current = map.getScreenPos(*i);
		qpointarray.setPoint(count++,current.x,current.y);
	}

	p.setPen(polydata[polygon->getType()].colour);
	p.setBrush(QBrush(polydata[polygon->getType()].colour,Qt::SolidPattern));
	p.drawPolygon(qpointarray);
	p.setBrush(Qt::NoBrush);

}


void MainWindow::mousePressEvent(QMouseEvent* ev)
{
	int nearest;

	switch(actionMode)
	{
		case ACTION_TRACK:
			if(selectedTrackpoint==-1)
				initSegmentSelection(ev->x(),ev->y());
			else
			{
				endSegmentSelection(ev->x(),ev->y());

				components->addSegdef(selectedTrackpoint,selectedTrackpoint2, 
					curSegType);
				components->printSegDefs();
				update();
				selectedTrackpoint = -1;
			}
			break;

		case ACTION_POLYGON:
			initPolygon();
			break;

		case ACTION_DELETE:
			if(selectedTrackpoint==-1)
				initSegmentSelection(ev->x(),ev->y());
			else
			{
				endSegmentSelection(ev->x(),ev->y());
				components->deleteTrackpoints(selectedTrackpoint,
											selectedTrackpoint2);
				components->printSegDefs();
				update();
				selectedTrackpoint = -1;
			}
			break;

		case ACTION_WAYPOINT:
			editWaypoint(ev->x(),ev->y(),10);	
			break;
	}			
}

void MainWindow::initPolygon()
{
	curPolygonPts.clear();
	mouseDown=true;
}

void MainWindow::initSegmentSelection(int x,int y)
{
	int nearest = findNearestTrackpoint(x,y,10); 

	if(nearest>=0) 
	{
		cout << "initSegmentSelection():Found track point:" << nearest << endl;
		selectedTrackpoint = nearest;
		update();
	}
}

void MainWindow::endSegmentSelection(int x,int y)
{
	if(selectedTrackpoint >= 0)
	{
		int nearest = findNearestTrackpoint(x,y,10); 

		if(nearest>=0) 
		{
			cout << "endSegmentSelection(): Found track point:" 
											<< nearest << endl;
			selectedTrackpoint2 = nearest;
		}
	}
}


int MainWindow::findNearestTrackpoint(int x,int y,int limit)
{
	ScreenPos curPos;
	double prevDist = limit, dist;
	int nearest=-1;
	TrackPoint curPt;
	if(components->hasTrack())
	{
		for(int count=0; count<components->nTrackpoints(); count++)
		{
			curPt = components->getTrackpoint(count);
			curPos = map.getScreenPos(curPt.lat,curPt.lon);	
			if((dist=OpenStreetMap::dist(x,y,curPos.x,curPos.y))<limit)
			{
				if(dist<prevDist)
				{
					prevDist=dist;
					nearest=count;
				}
			}
		}
	}
	return nearest;
}

// TODO: the first part of this is doing almost exactly the same as 
// findNearestTrackpoint(), above. Investigate the possibility of doing this
// more elegantly.
void MainWindow::editWaypoint(int x,int y,int limit)
{
	ScreenPos curPos;
	double prevDist = limit, dist;
	int nearest=-1;
	Waypoint curPt, nearestPt;

	if(components->hasWaypoints())
	{
		for(int count=0; count<components->nWaypoints(); count++)
		{
			curPt = components->getWaypoint(count);
			curPos = map.getScreenPos(curPt.lat,curPt.lon);	
			if((dist=OpenStreetMap::dist(x,y,curPos.x,curPos.y))<limit)
			{
				if(dist<prevDist)
				{
					prevDist=dist;
					nearest=count;
					nearestPt = curPt;
				}
			}
		}
		if(nearest>=0)
		{
			WaypointDialogue* d = new WaypointDialogue
					(this,waypointReps,"Edit waypoint",
					nearestPt.type,nearestPt.name);
			if(d->exec())
			{
				components->alterWaypoint(nearest,d->getName(),d->getType());
				update();
			}
			delete d;
		}
	}
}

void MainWindow::mouseMoveEvent(QMouseEvent* ev)
{

	if(actionMode==ACTION_POLYGON && mouseDown)
	{
		QPainter p (this);
		p.setPen(polydata[curPolygonType].colour);
		if(curPolygonPts.size()>=1)
		{
			ScreenPos prev = *(curPolygonPts.end()-1);

			p.drawLine(prev.x,prev.y,ev->x(),ev->y());	

			// Only add if a given distance to last point -
			// otherwise Freemap will be overloaded with polygon 
			// points !!!
			// 0.1 maybe a tad too coarse, 0.05 more definitely too fine.
			if(OpenStreetMap::dist (prev.x,prev.y,ev->x(),ev->y())
				>= polygonRes*1000*map.getScale()) 
			{
				curPolygonPts.push_back
						(ScreenPos(ev->x(),ev->y()));
			}
		}
		else
		{
			curPolygonPts.push_back (ScreenPos(ev->x(),ev->y()));
		}
	}
}

void MainWindow::mouseReleaseEvent(QMouseEvent* ev)
{
	switch(actionMode)
	{

		case ACTION_POLYGON:
			endPolygon(ev->x(),ev->y());
			break;
	}
}

void MainWindow::endPolygon(int x,int y)
{
	vector<ScreenPos>::iterator i;
	curPolygonPts.push_back(ScreenPos(x,y));
	curPolygon = new Polygon;

	for(i=curPolygonPts.begin(); i!=curPolygonPts.end(); i++)
		curPolygon->push_back(map.getGridRef(*i));	

	curPolygon->setType(curPolygonType);
	polygons.push_back(curPolygon);

	update();
}

	
void MainWindow::keyPressEvent(QKeyEvent* ev)
{
	// 11/04/05 prevent movement being too far at large scales
	double dis = 0.1/map.getScale();

	switch(ev->key())
	{
		case Qt::Key_Left  : map.move (-dis,   0); update(); break;
		case Qt::Key_Right : map.move ( dis,   0); update(); break;
		case Qt::Key_Up    : map.move (   0, dis); update(); break;
		case Qt::Key_Down  : map.move (   0,-dis); update(); break;
		case Qt::Key_Plus  : map.rescale(2,width(),height());	   
							 update(); 
							 break;
		case Qt::Key_Minus : map.rescale(0.5,width(),height());	   
							 update(); 
							 break;
	}
}

void MainWindow::grabTracks()
{
	QString username, password;
	LoginDialogue *d = new LoginDialogue(this);
	if(d->exec())
	{
		username = d->getUsername();
		password = d->getPassword();
#ifdef XMLRPC
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
				LatLon llNW = map.getTopLeftLL();
				LatLon llSE = map.getLatLon(ScreenPos(width(),height()));
				
				
				param_array = XmlRpcValue::makeArray();
				param_array.arrayAppendItem(XmlRpcValue::makeString(token));
				param_array.arrayAppendItem
						(XmlRpcValue::makeDouble(llNW.lat));
				param_array.arrayAppendItem
						(XmlRpcValue::makeDouble(llNW.lon));
				param_array.arrayAppendItem
						(XmlRpcValue::makeDouble(llSE.lat));
				param_array.arrayAppendItem
						(XmlRpcValue::makeDouble(llSE.lon));
				result=client.call("openstreetmap.getPoints",param_array);
				XmlRpcValue array=result.getArray();
				cerr << "array size : " << array.arraySize() << endl;
				QString wpName;
				for(int count=0; count<array.arraySize(); count+=2)
				{
					XmlRpcValue value = array.arrayGetItem(count);
					double curLat = value.getDouble();
					value = array.arrayGetItem(count+1);
					double curLon = value.getDouble();
					wpName.sprintf("OSM-%03d",count);	
					cout << "lat:" << curLat << " lon:" << curLon  << endl;
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
#endif
	}
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
