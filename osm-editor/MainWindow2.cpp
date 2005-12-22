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


#include "MainWindow2.h"
#include "functions.h"
#include "SRTMConGen.h"

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
#include <qcstring.h>

#include "WaypointDialogue.h"
#include "LoginDialogue.h"

#include <qxml.h>
//#include "GPXParser.h"
#include "OSMParser2.h"

#include "qmdcodec.h"

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

MainWindow2::MainWindow2(double lat,double lon, double s,double w,double h) :
									map(lon,lat,s,w,h), 
									landsatManager(this,w,h,100),
									osmhttp("www.openstreetmap.org")
{
	cerr<<"constructor"<<endl;
	setCaption("OpenStreetMap Editor");
	resize ( w, h );	

	LIMIT=map.earthDist(10);

	newUploadedNode =NULL;
	newUploadedSegment = NULL;

	contours = false;
	wptSaved = false;

	actionMode = ACTION_NODE;
	curSegType = "A road"; 
	nSelectedPoints = 0;

	doingName = false;

	segpens["footpath"]= QPen (Qt::green, 2);
	segpens["cycle path"]= QPen (Qt::magenta, 2);
	segpens["bridleway"]=QPen(QColor(192,96,0),2);
	segpens["byway"] = QPen (Qt::red, 2);
	segpens["minor road"]= QPen (Qt::black, 2);
	segpens["residential road"]= QPen (Qt::black, 1);
	segpens["B road"]= QPen (Qt::black, 3);
	segpens["A road"]= QPen (Qt::black, 4);
	segpens["motorway"]= QPen (Qt::black, 6);
	segpens["railway"]= QPen (Qt::gray, 4);
	segpens["permissive footpath"]= QPen (Qt::green, 1);
	segpens["permissive bridleway"]= QPen (QColor(192,96,0), 1);
	segpens["track"]= QPen (Qt::darkGray, 2);
	segpens["new forest track"]=QPen(QColor(128,64,0),2);
	segpens["new forest cycle path"]= QPen (QColor(128,0,0), 2);
	cerr<<"done segpens" << endl;


	// Construct the menus.
	QPopupMenu* fileMenu = new QPopupMenu(this);
	// 29/10/05 Only open "OSM" now
	fileMenu->insertItem("&Open",this,SLOT(open()),CTRL+Key_O);
	fileMenu->insertItem("&Save",this,SLOT(save()),CTRL+Key_S);
	fileMenu->insertItem("Save &as...",this,SLOT(saveAs()),CTRL+Key_A);
	fileMenu->insertItem("&Read GPS",this,SLOT(readGPS()),CTRL+Key_R);
	fileMenu->insertItem("&Grab Landsat",this,SLOT(grabLandsat()),CTRL+Key_G);
	fileMenu->insertItem("Grab OSM from &Net",this,SLOT(grabOSMFromNet()),
								CTRL+Key_N);
	fileMenu->insertItem("&Upload OSM",this,SLOT(uploadOSM()),CTRL+Key_U);
	fileMenu->insertItem("&Quit", this, SLOT(quit()), ALT+Key_Q);
	fileMenu->insertItem("Login to live update",this,
						SLOT(loginToLiveUpdate()));
	fileMenu->insertItem("Logout from live update",this,
						SLOT(logoutFromLiveUpdate()));
	menuBar()->insertItem("&File",fileMenu);
	QPopupMenu* editMenu = new QPopupMenu(this);
	
	editMenu->insertItem("&Toggle nodes",this,SLOT(toggleNodes()),
						CTRL+Key_T);
	editMenu->insertItem("Toggle &Landsat",this,SLOT(toggleLandsat()),
						CTRL+Key_L);
	editMenu->insertItem("Toggle &contours",this,SLOT(toggleContours()),
						CTRL+Key_C);
	editMenu->insertItem("Remove trac&k points",this,SLOT(removeTrackPoints()),
						CTRL+Key_K);
//	editMenu->insertItem("Undo",this,SLOT(undo()),CTRL+Key_Z);
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

	cerr<<"done combo box" << endl;
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
	QPixmap deleteseg = mmLoadPixmap("images","deleteseg.png");
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
	QPixmap selseg_pixmap = mmLoadPixmap("images","selseg.png");

	new QToolButton(left_pixmap,"Move left","",this,SLOT(left()),toolbar);
	new QToolButton(right_pixmap,"Move right","",this,SLOT(right()),toolbar);
	new QToolButton(up_pixmap,"Move up","",this,SLOT(up()),toolbar);
	new QToolButton(down_pixmap,"Move down","",this,SLOT(down()),toolbar);
	new QToolButton(magnify_pixmap,"Zoom in","",this,SLOT(magnify()),toolbar);
	new QToolButton(shrink_pixmap,"Zoom out","",this,SLOT(shrink()),toolbar);

	QToolBar  *toolbar2 = new QToolBar(this);
	toolbar2->setHorizontalStretchable(true);
//	moveDockWindow (toolbar2,Qt::DockLeft);


	
	modeButtons[ACTION_NODE]= new QToolButton
			(wp,"Edit Nodes","",mapper,SLOT(map()),toolbar2);
	modeButtons[ACTION_MOVE_NODE]= new QToolButton
			(objectmanip,"Move Node","",mapper,SLOT(map()),toolbar2);
	modeButtons[ACTION_DELETE_NODE]= new QToolButton
			(two,"Delete Node","",mapper,SLOT(map()),toolbar2);
	modeButtons[ACTION_SEL_SEG]= new QToolButton
			(selseg_pixmap,"Select segment","",mapper,SLOT(map()),toolbar2);
	modeButtons[ACTION_NEW_SEG]= new QToolButton
			(formnewseg,"New segment","",mapper,SLOT(map()),toolbar2);
	new QToolButton
			(deleteseg,"Delete Segment","",this,
			 SLOT(deleteSelectedSeg()),toolbar2);

	
	toolbar->setStretchableWidget(new QLabel(toolbar));
	toolbar2->setStretchableWidget(new QLabel(toolbar2));

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
	
	modeButtons[ACTION_NODE]->setOn(true);
	

	// The final stage of implementing the signal mapper: connect mapped()
	// to setMode(). The value set in setMapping() above will be used.
	QObject::connect(mapper,SIGNAL(mapped(int)),this,SLOT(setMode(int)));


	nodeReps["pub"] = new WaypointRep 
			("images/pub.png","Helvetica",10, QColor(170,85,0));
	nodeReps["church"] = new WaypointRep ( "images/church.png");
	nodeReps["viewpoint"] = new WaypointRep("images/viewpoint.png");
	nodeReps["farm"] = new WaypointRep("images/farm.png",
					"Helvetica",8,Qt::red);
	nodeReps["hill"] = new WaypointRep(
					"images/peak.png","Helvetica",10, Qt::magenta);
	nodeReps["hamlet"] = new WaypointRep(
					"images/place.png","Helvetica",12, Qt::black);
	nodeReps["village"] = new WaypointRep(
					"images/place.png","Helvetica",16, Qt::black);
	nodeReps["small town"] = new WaypointRep(
					"images/place.png","Helvetica",20, Qt::black);
	nodeReps["large town"] = new WaypointRep(
					"images/place.png","Helvetica",24, Qt::black);
	nodeReps["car park"] = new WaypointRep(
					"images/carpark.png", "Helvetica",8,Qt::blue);
	nodeReps["railway station"] = new WaypointRep(
					"images/station.png", "Helvetica",10,Qt::red);
	nodeReps["mast"] = new WaypointRep(
					"images/mast.png");
	nodeReps["locality"] = new WaypointRep("images/node.png",
					"Helvetica",12,Qt::black);
	nodeReps["point of interest"] = new WaypointRep
			("images/interest.png");
	nodeReps["suburb"] = new WaypointRep(
			"images/place.png","Helvetica",16, Qt::black);
	nodeReps["caution"] = new WaypointRep(
					"images/caution.png"); 
	nodeReps["amenity"] = new WaypointRep(
				   	"images/amenity.png","Helvetica",8, Qt::red);

	nodeReps["trackpoint"] = new WaypointRep(
					"images/trackpoint.png","Helvetica",8,Qt::black);
	nodeReps["node"] = new WaypointRep(
					"images/node.png","Helvetica",8,Qt::black);
	nodeReps["waypoint"] = new WaypointRep(
					"images/waypoint.png","Helvetica",8,Qt::black);

	nodeReps["campsite"] = new WaypointRep(
					"images/campsite.png","Helvetica",8,QColor(0,128,0));
	nodeReps["restaurant"] = new WaypointRep(
					"images/restaurant.png","Helvetica",8,QColor(128,0,0));
	nodeReps["bridge"] = new WaypointRep("images/bridge.png");
	nodeReps["tunnel"] = new WaypointRep("images/tunnel.png");
	nodeReps["tea shop"] = new WaypointRep(
					"images/teashop.png","Helvetica",8,Qt::magenta);
	nodeReps["country park"] = new WaypointRep("images/park.png",
							"Helvetica",8,QColor(0,192,0));
	nodeReps["industrial area"] = new WaypointRep("images/industry.png",
							"Helvetica",8,Qt::darkGray);
	nodeReps["barn"] = new WaypointRep("images/barn.png");
	curFilename = "";

	trackpoints=true;

	components = new Components2;

	selSeg = NULL;

	setFocusPolicy(QWidget::ClickFocus);

	showPosition();

	username = "";
	password = "";

	liveUpdate = false;

	cerr<<"end constructor"<<endl;
	savedNode = NULL;	

	QObject::connect(&osmhttp,SIGNAL(httpErrorOccurred(const QString&)),
					this,SLOT(handleHttpError(const QString&)));
}

MainWindow2::~MainWindow2()
{
	for (std::map<QString,WaypointRep*>::iterator i=nodeReps.begin();
		 i!=nodeReps.end(); i++)
	{
		delete i->second;
	}

	components->destroy();
	delete components;
}

void MainWindow2::open() 
{
	QString filename = QFileDialog::getOpenFileName("","*.osm",this);
	if(filename!="")
	{
		Components2 *newComponents = doOpen(filename);
		if(newComponents)
		{
			try
			{
				cout << "!!! Deleting existing components !!!" << endl;
				components->destroy();
				delete components;	
				components = newComponents;
				curFilename = filename;
				map.centreAt(components->getAveragePoint());
				showPosition();
				update();
			}
			catch(QString str)
			{
				// blank track, trackseg etc
			}
		}
	}
}

Components2 * MainWindow2::doOpen(const QString& filename)
{
	cerr<<"doOpen"<<endl;
	Components2 * comp;

	
	OSMParser2 parser;
	cerr<<"filename=" << filename<<endl;
	QFile file(filename);
	QXmlInputSource source(&file);
	QXmlSimpleReader reader;
	reader.setContentHandler(&parser);
	reader.parse(source);
	comp = parser.getComponents();	
	return comp;
	
}

void MainWindow2::loginToLiveUpdate()
{
	QString str = "WARNING!!! The login and password you supply will be\n"\
				  "stored in osm-editor's memory and sent to the server\n"\
				  "each time you perform a live update action.\n"\
				  "This will happen until\n"\
				"you select Logout or exit osm-editor. If you\n "\
				"leave the computer, someone else will be able to modify\n "\
				"OSM data!!! Press Cancel next if you're unhappy!";
	QMessageBox::warning(this,"Warning!",str);
	LoginDialogue *ld=new LoginDialogue(this);
	if(ld->exec())
	{
		username = ld->getUsername();
		password = ld->getPassword();
		liveUpdate = true;
	}
	delete ld;
}

void MainWindow2::logoutFromLiveUpdate()
{
	username = "";
	password = "";
	liveUpdate = false;
}

void MainWindow2::grabOSMFromNet()
{
	QString url="http://www.openstreetmap.org/api/0.2/map";
	QString uname="", pwd="";
	Components2 * netComponents; 
	statusBar()->message("Grabbing data from OSM...");
	EarthPoint bottomLeft = map.getBottomLeft(),
			   topRight = map.getTopRight();
	if(username=="" || password=="")
	{
		LoginDialogue *ld=new LoginDialogue(this);
		if(ld->exec())
		{
			uname=ld->getUsername(),
			pwd=ld->getPassword();
		}
		delete ld;
	}
	else
	{
		uname=username;
		pwd = password;
	}

	if(uname!="" && pwd!="")
	{
		statusBar()->message("Grabbing data from OSM...");
		EarthPoint bottomLeft = map.getBottomLeft(),
			   topRight = map.getTopRight();
		QString url;
		url.sprintf("/api/0.2/map?bbox=%lf,%lf,%lf,%lf",
							bottomLeft.x,bottomLeft.y,
							topRight.x,topRight.y);
		cerr<<"SENDING URL: "<<url<<endl;

		if(!osmhttp.isMakingRequest())
		{
	    	osmhttp.disconnect (SIGNAL(responseReceived(const QByteArray&)));
			QObject::connect
					(&osmhttp,SIGNAL(responseReceived(const QByteArray&)),
						 this, SLOT(loadComponents(const QByteArray&)));
			osmhttp.setAuthentication(uname,pwd);
			osmhttp.sendRequest("GET", url);
		}
	}
}

void MainWindow2::loadComponents(const QByteArray& array)
{
	Components2 *netComponents;

	QXmlInputSource source;
	source.setData(array);

	OSMParser2 parser;
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
	update();
}


// uploads new segments only
void MainWindow2::uploadOSM()
{
	LoginDialogue *ld=new LoginDialogue(this);
	char a[1024],b[1024];
	if(ld->exec())
	{
		strcpy(a,ld->getUsername());
		strcpy(b,ld->getPassword());
		components->newUploadToOSM(a,b);
	}
	delete ld;
}

void MainWindow2::readGPS()
{
	try
	{
		GPSDevice2 device ("Garmin", "/dev/ttyS0");
		Components2 *c = device.getSurveyedComponents();
		cerr << "GPS read done. " << std::endl;
		if(components) {components->destroy();delete components;}
		components = c;
		map.centreAt(components->getAveragePoint());
		showPosition();
		update();
	}
	catch(QString str)
	{
		cerr<<str<<endl;
	}
}

void MainWindow2::save()
{
	if(curFilename == "")
		saveAs();
	else
		saveFile(curFilename);
}

void MainWindow2::saveAs()
{
	QString filename = QFileDialog::getSaveFileName("","*.gpx",this);
	if(filename!="")
		saveFile(filename);
}

void MainWindow2::saveFile(const QString& filename)
{
//	components->toGPX(filename);	
	curFilename = filename;
	QFile file (filename);
	if(file.open(IO_WriteOnly))
	{
		QTextStream strm(&file);
		components->toOSM(strm,true);
		file.close();
	}
}

void MainWindow2::quit()
{
	QApplication::exit(0);
}

void MainWindow2::setMode(int m)
{
	actionMode=  m;

	// Display the appropriate mode toolbar button and menu item checked, and
	// all the others turned off.
	for (int count=0; count<N_ACTIONS; count++)
		modeButtons[count]->setOn(count==m);

	// Wipe any currently selected points
	nSelectedPoints = 0;

	selSeg = NULL;
	savedNode = NULL;
	pts[0]=pts[1]=NULL;
	ptsv[0].clear();
	ptsv[1].clear();
	doingName = false;
	update();
}

void MainWindow2::setSegType(const QString &t)
{
	curSegType =   t;
	// live change of selected segment
	if(selSeg)
	{
		selSeg->setType(curSegType);
		// UPLOAD IF IN LIVE MODE
		if(liveUpdate)
		{
		//	selSeg->uploadToOSM(username,password);
			QByteArray xml = selSeg->toOSM();
			QString url;
			url.sprintf ("/api/0.2/segment/%d", selSeg->getOSMID());
			osmhttp.setAuthentication(username, password);
			osmhttp.sendRequest("PUT", url, xml);
		}
	}
	update();
}

void MainWindow2::toggleNodes()
{
	trackpoints = !trackpoints;
	update();
}

void MainWindow2::toggleLandsat()
{
	landsatManager.toggleDisplay();
	update();
}

void MainWindow2::toggleContours()
{
	contours = !contours;
	update();
}

void MainWindow2::undo()
{
	//TODO
	update();	
}

void MainWindow2::paintEvent(QPaintEvent* ev)
{
	QPainter p(this);
	drawLandsat(p);
	curPainter = &p; // needed for the contour "callback"
	drawContours();
	drawSegments(p);
	drawNodes(p);
	curPainter = NULL;
}

void MainWindow2::drawLandsat(QPainter& p)
{
	landsatManager.drawTiles(p);
}

void MainWindow2::drawContours()
{
	if(contours)
	{
		SRTMConGen congen(map,1);
		congen.generate(this);
	}
}

void MainWindow2::drawContour(int x1,int y1,int x2,int y2,int r,int g,int b)
{
	if(curPainter)
	{
		curPainter->setPen(QColor(r,g,b));
		curPainter->drawLine(x1,y1,x2,y2);
	}
}

void MainWindow2::drawAngleText(int fontsize,double angle,int x,int y,int r,
								int g, int b, char *text)
{
	if(curPainter)
	{
		//angle*=-180/M_PI;
		curPainter->setFont(QFont("Helvetica",fontsize));
		doDrawAngleText(curPainter,x,y,x,y,angle,text);
	}
}

void MainWindow2::doDrawAngleText(QPainter *p,int originX,int originY,int x,
				int y,double angle, const char * text)
{
	angle *= 180/M_PI;
	p->translate(originX,originY);
	p->rotate(angle);
	p->drawText(x-originX,y-originY,text);
	p->rotate(-angle);
	p->translate(-originX,-originY);
}

void MainWindow2::heightShading(int x1,int y1,int x2,int y2,int x3,int y3,
								int x4,int y4,int r,int g, int b)
{

}

void MainWindow2::drawSegments(QPainter& p)
{
	QPen curPen;
	Segment *curSeg;
	QFont f("Helvetica",10,QFont::Bold,true);
	QFontMetrics fm(f);
	p.setFont(f);
	QString segname;
	ScreenPos pt1, pt2;
	double dx, dy;

	components->rewindSegments();

	while(!components->endSegment())
	{
		curSeg = components->nextSegment();
		curPen = (curSeg==selSeg) ? 
						QPen(Qt::yellow,5) : segpens[curSeg->getType()];

		curPen.setStyle ((curSeg->getOSMID()>0) ?  Qt::SolidLine: Qt::DotLine );
		p.setPen(curPen);
		if(curSeg->hasNodes())
		{
			pt1=map.getScreenPos(curSeg->firstNode()->getLon(),
								curSeg->firstNode()->getLat());
			pt2=map.getScreenPos(curSeg->secondNode()->getLon(),
								curSeg->secondNode()->getLat());
			if(map.pt_within_map(pt1) || map.pt_within_map(pt2))
			{
				p.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);
				if(curSeg->getName()!="")
				{
					dy=pt2.y-pt1.y;
					dx=pt2.x-pt1.x;
					double angle = atan2(dy,dx);
					doDrawAngleText(&p,pt1.x,pt1.y,pt1.x,pt1.y,
								angle,curSeg->getName().ascii());
				}
			}
		}
	}
}

void MainWindow2::drawNodes(QPainter& p)
{
	int count=0;
	components->rewindNodes();
	while(!components->endNode())
	{
		drawNode(p,components->nextNode());
	}
}


void MainWindow2::drawNode(QPainter& p,Node* node)
{
	ScreenPos pos = map.getScreenPos(node->getLon(),node->getLat());
	if(map.pt_within_map(pos))
	{
		WaypointRep* img=nodeReps[node->getType()];
		if(img) img->draw(p,pos.x,pos.y,node->getName()); 

		if(!selSeg && (ptsv[0].size() || ptsv[1].size()))
		{
			for(int count=0; count<ptsv[0].size(); count++)
			{
				if(node==ptsv[0][count])
				{
					p.setPen(QPen(Qt::yellow,3));
					p.drawEllipse( pos.x - 16, pos.y - 16, 32, 32 );
				}
			}
			for(int count=0; count<ptsv[1].size(); count++)
			{
				if(node==ptsv[1][count])
				{
					p.setPen(QPen(Qt::yellow,3));
					p.drawEllipse( pos.x - 16, pos.y - 16, 32, 32 );
				}
			}
		}

		if(node==pts[0] || node==pts[1])
		{
			p.setPen(QPen(Qt::red,3));
			p.drawEllipse( pos.x - 16, pos.y - 16, 32, 32 );
		}
	}
}

void MainWindow2::removeTrackPoints()
{
	components->removeTrackPoints();
	update();
}

void MainWindow2::mousePressEvent(QMouseEvent* ev)
{
	EarthPoint p = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
	QString name;
	int nearest;
	Node *n;

	cerr<<"actionMode="<<actionMode<<endl;
	switch(actionMode)
	{
		case ACTION_NODE:
			editNode(ev->x(),ev->y(),LIMIT);	
			break;

		case ACTION_MOVE_NODE:
			if(savedNode)
			{
				savedNode->setCoords(p.y,p.x);
				components->addNode(savedNode);
				if(liveUpdate && savedNode->getOSMID()>0)
				{
					QByteArray xml = savedNode->toOSM();
					QString url;
					url.sprintf ("/api/0.2/node/%d", savedNode->getOSMID());
					osmhttp.setAuthentication(username, password);
					osmhttp.sendRequest("PUT", url, xml);
				}
				savedNode = NULL;	
				update();
			}
			else 
			{
				savedNode = components->getNearestNode(p.y,p.x,LIMIT);
				if(savedNode)
				{
					components->deleteNode(savedNode);
					update();
				}
			}
			break;
		case ACTION_DELETE_NODE:
			n = components->getNearestNode(p.y,p.x,LIMIT);
			if(n)
			{
				components->deleteNode(n);
				if(liveUpdate && n->getOSMID()>0)
				{
					QString url;
					url.sprintf ("/api/0.2/node/%d", n->getOSMID());
					osmhttp.setAuthentication(username, password);
					osmhttp.sendRequest("DELETE", url);
				}
				delete n;
				update();
			}
			break;
		case ACTION_NEW_SEG:
			if(nSelectedPoints==0)
			{
				pts[0] = components->getNearestNode(p.y,p.x,LIMIT);
				if(!pts[0])
					pts[0]=components->addNewNode(p.y,p.x,"","node");
				update();
				nSelectedPoints++;
			}
			else
			{
				pts[1] = components->getNearestNode(p.y,p.x,LIMIT);
				if(!pts[1])
					pts[1]=components->addNewNode(p.y,p.x,"","node");
				Segment *seg=
						components->addNewSegment(pts[0],pts[1],"",curSegType);
				if(liveUpdate && !osmhttp.isMakingRequest())
				{
					//seg->uploadToOSM(username,password);
					QByteArray xml = seg->toOSM();
					cerr<<"xml is: "<<xml<<endl;
					QString url = "/api/0.2/newsegment";
					osmhttp.disconnect
							(SIGNAL(responseReceived(const QByteArray&)));
					QObject::connect
							(&osmhttp,
							 SIGNAL(responseReceived(const QByteArray&)),
						 		this, SLOT(newSegmentAdded(const QByteArray&)));
					newUploadedSegment = seg;
					osmhttp.setAuthentication(username, password);
					osmhttp.sendRequest("PUT", url, xml);
				}
				pts[0]=pts[1]=NULL;
				update();
				nSelectedPoints=0;
			}
			break;
		case  ACTION_SEL_SEG:
			if(nSelectedPoints==0)
			{
				EarthPoint p=map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				ptsv[0] = components->getNearestNodes (p.y,p.x,LIMIT);
				if(ptsv[0].size())
				{
					selSeg = NULL;
					cerr<<"SELSEG: FOUND A FIRST POINT "<< endl;
					cerr << ptsv[0][0]->getLat() << endl;
					cerr << ptsv[0][0]->getLon() << endl;
					update();
					nSelectedPoints++;
				}
			}
			else
			{
				EarthPoint p=map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
				ptsv[1] = components->getNearestNodes(p.y,p.x,LIMIT);
				// SELECT THE SEG
				cerr<<"trying to select seg" << endl;
				if(ptsv[1].size())
				{
					cerr<<"SELSEG: SECOND POINT "<< endl;
					cerr << ptsv[1][0]->getLat() << endl;
					cerr << ptsv[1][0]->getLon() << endl;
					selSeg = components->getSeg(ptsv[0],ptsv[1]);	
					if(selSeg) 
					{
						cerr<<"found a selected seg" << endl;
						
						// Naming always on when in selected segment mode
						nameTrackOn();
					}
				
					ptsv[0].clear();
					ptsv[1].clear();

					update();
					nSelectedPoints=0;
				}
			}
			break;
	}			
}

void MainWindow2::resizeEvent(QResizeEvent * ev)
{
	//map.resizeTopLeft(ev->size().width(), ev->size().height());
	map.resizeTopLeft(width(), height());
	//landsatManager.grab();
	//landsatManager.resize(ev->size().width(), ev->size().height());
	landsatManager.resize(width(), height());
	update();
	LIMIT=map.earthDist(10);
}


void MainWindow2::editNode(int x,int y,int limit)
{
	Node *nearest = NULL;	
	WaypointDialogue *d;

	EarthPoint p = map.getEarthPoint(ScreenPos(x,y));
	if((nearest=components->getNearestNode(p.y,p.x,LIMIT)) != NULL)
	{
		cerr<<"creating waypoint dialogue" << endl;
		d = new WaypointDialogue
					(this,nodeReps,"Edit node",
					nearest->getType(),nearest->getName());
		cerr<<"done." << endl;
		if(d->exec())
		{
			nearest->setName(d->getName());
			nearest->setType(d->getType());
			if(liveUpdate)
			{
				//nearest->uploadToOSM(username,password);
				QByteArray xml = nearest->toOSM();
				QString url;
				url.sprintf ("/api/0.2/node/%d", nearest->getOSMID());
				osmhttp.setAuthentication(username, password);
				osmhttp.sendRequest("PUT", url, xml);
			}
		}

		update();
		delete d;
	}
	else
	{
		d = new WaypointDialogue
					(this,nodeReps,"Add node","node","");
		if(d->exec())
		{
			Node *n = components->addNewNode(p.y,p.x,d->getName(),d->getType());
			if(liveUpdate && !osmhttp.isMakingRequest())
			{
				//n->uploadToOSM(username,password);
				QByteArray xml = n->toOSM();
				QString url = "/api/0.2/newnode";
			    osmhttp.disconnect
							(SIGNAL(responseReceived(const QByteArray&)));
				QObject::connect
						(&osmhttp,SIGNAL(responseReceived(const QByteArray&)),
						 this, SLOT(newNodeAdded(const QByteArray&)));
				osmhttp.setAuthentication(username, password);
				newUploadedNode = n;
				osmhttp.sendRequest("PUT", url, xml);
			}
		}

		update();
		delete d;
	}
}

void MainWindow2::mouseMoveEvent(QMouseEvent* ev)
{
}

void MainWindow2::mouseReleaseEvent(QMouseEvent* ev)
{
	EarthPoint p;
	QString name;
	double LIMIT=map.earthDist(10);
	switch(actionMode)
	{

	}
}

void MainWindow2::keyPressEvent(QKeyEvent* ev)
{
	bool typingName = false;

	if(doingName)
	{
		if(ev->ascii()>=32 && ev->ascii()<=127)
		{
			trackName += ev->text();
			QPainter p(this);
			p.setPen(Qt::black);
			QFont f ("Helvetica",10,QFont::Bold,true);
			p.setFont(f);
			QFontMetrics fm(f);

			doDrawAngleText(&p,namePos.x,namePos.y,curNamePos.x,
							curNamePos.y,
							-nameAngle,ev->text());
			curNamePos.x += fm.width(ev->text());
			typingName = true;
		}
		else if (ev->key()==Key_Return)
		{
			selSeg->setName(trackName);
//			UPLOAD IF IN LIVE MODE
			if(liveUpdate)
			{
				//selSeg->uploadToOSM(username,password);
				QByteArray xml = selSeg->toOSM();
				QString url;
				url.sprintf ("/api/0.2/segment/%d", selSeg->getOSMID());
				osmhttp.setAuthentication(username, password);
				osmhttp.sendRequest("PUT", url, xml);
			}
			trackName = "";
			typingName = true;
			update();
		}
	}
	if(!typingName)
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

void MainWindow2::left()
{
	map.movePx(-landsatManager.getTileSize(),0);
	landsatManager.left();
    showPosition();
	update();
}

void MainWindow2::right()
{
	map.movePx(landsatManager.getTileSize(),0);
	landsatManager.right();
    showPosition();
	update();
}

void MainWindow2::up()
{
	map.movePx(0,-landsatManager.getTileSize());
	landsatManager.up();
    showPosition();
	update();
}

void MainWindow2::down()
{
	map.movePx(0,landsatManager.getTileSize());
	landsatManager.down();
    showPosition();
	update();
}

void MainWindow2::screenLeft()
{
	map.movePx(-width(),0);
	landsatManager.grabAll();
    showPosition();
	update();
}

void MainWindow2::screenRight()
{
	map.movePx(width(),0);
	landsatManager.grabAll();
    showPosition();
	update();
}

void MainWindow2::screenUp()
{
	map.movePx(0,-height());
	landsatManager.grabAll();
    showPosition();
	update();
}

void MainWindow2::screenDown()
{
	map.movePx(0,height());
	landsatManager.grabAll();
    showPosition();
	update();
}

void MainWindow2::magnify()
{
	map.rescale(2);
    landsatManager.grabAll();
    showPosition();
    update();
	LIMIT=map.earthDist(10);
}

void MainWindow2::shrink()
{
    map.rescale(0.5);
    landsatManager.grabAll();
    showPosition();
    update();
	LIMIT=map.earthDist(10);
}

void MainWindow2::updateWithLandsatCheck()
{
	if(landsatManager.needMoreData())
		landsatManager.forceGrab();
	showPosition();
	update();
}

void MainWindow2::grabLandsat()
{
	// 01/05/05 grab three times current screen width and height (i.e. 9 
	// times screen area) and centre at current
	// map centre. This will be configurable.
	

	//landsatManager.forceGrab();
	landsatManager.forceGrabAll();
	update();


}

void MainWindow2::nameTrackOn()
{

	if(selSeg)
	{
		Node *n1 = selSeg->firstNode(), *n2 = selSeg->secondNode();
		double dy=n2->getLat()-n1->getLat();
		double dx=n2->getLon()-n1->getLon();
		nameAngle = atan2(dy,dx);
		doingName = true;
		namePos = map.getScreenPos (n1->getLon(),n1->getLat());
		curNamePos = namePos;
		trackName="";
		update();
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


void MainWindow2::newNodeAdded(const QByteArray& array)
{
	QString str = array;
	QStringList ids;

	cerr<<"**** HANDLING A NEW NODE RESPONSE ****" << endl;
	cerr<<"STR=" << str << endl;
	ids = QStringList::split("\n", str);
	cerr<<"ids[0]="<<ids[0]<<endl;			
	if(newUploadedNode)
	{
		cerr<<"NEW UPLOADED NODE IS NOT NULL::SETTING ID"<<endl;
		newUploadedNode->setOSMID(atoi(ids[0].ascii()));
		newUploadedNode = NULL;
		cerr<<"DONE."<<endl;
	}
	else
		cerr<<"NEW UPLAODED NODE IS NULL" << endl;
}

void MainWindow2::newSegmentAdded(const QByteArray& array)
{
	QString str = array;
	QStringList ids;
	ids = QStringList::split("\n", str);
	if(newUploadedSegment)
	{
		cerr<<"NEW UPLOADED SEGMENT IS NOT NULL::SETTING ID"<<endl;
		newUploadedSegment->setOSMID(atoi(ids[0].ascii()));
		newUploadedSegment = NULL;
		cerr<<"DONE."<<endl;
	}
}

void MainWindow2::deleteSelectedSeg()
{
	if(selSeg)
	{
		cerr<<"selseg exists" << endl;
		components->deleteSegment(selSeg);
		if(liveUpdate && selSeg->getOSMID()>0)
		{
			QString url;
			url.sprintf ("/api/0.2/segment/%d", selSeg->getOSMID());
			osmhttp.setAuthentication(username, password);
			osmhttp.sendRequest("DELETE", url);
		}
		delete selSeg;
		selSeg = NULL;
		update();
	}
}

void MainWindow2::handleHttpError(const QString& error)
{
	QMessageBox::information(this,"An error occurred communicating with OSM",
								error);
}
}
