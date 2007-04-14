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

// 180306 started way provision (multiple selected segments)
// 180306 changed 0.2 API calls to 0.3; renamed newnode and newsegment to
// node/0 and segment/0.

#include "MapWidget.h"
#include "functions.h"
#include "SRTMConGen.h"

#include <iostream>
#include <sstream>
#include <cstdlib>
#include <cmath>

#include <qapplication.h>
#include <qpainter.h>
#include <qfontmetrics.h>
#include <qmenubar.h>
#include <qfiledialog.h>
#include <qmessagebox.h>
#include <qinputdialog.h>
#include <qtoolbar.h>
#include <qlabel.h>
#include <qsignalmapper.h>
#include <qtextstream.h>
//Added by qt3to4:
#include <QResizeEvent>
#include <QPixmap>
#include <QMouseEvent>
#include <QKeyEvent>
#include <QPaintEvent>

#include <QPolygon>

#include "WaypointDialogue.h"
#include "WayDialogue.h"
#include "LoginDialogue.h"

#include <qxml.h>
#include "OSMParser2.h"
#include "GPXParser2.h"
#include "Geocoder.h"

//#include "qmdcodec.h"

#include <cmath>

#ifdef XMLRPC
#include <string>
#include <XmlRpcCpp.h>
#endif


using std::cout;
using std::endl;
using std::cerr;

namespace OpenStreetMap
{


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
        QTextStream strm(&lbl,QIODevice::ReadOnly);
        QString word;
        while(!strm.atEnd())
        {
            strm>>word;
            p.drawText(labelX,labelY,word);
            labelY += fm.height();
        }
    }
}

MapWidget::MapWidget(QMainWindow *mainwin,
					double lat,double lon, double s,double w,double h) :
									QWidget(mainwin),
                                    map(lon,lat,s,w,h),
                                    landsatManager(this,400,
										"onearth.jpl.nasa.gov",
			"/wms.cgi?request=GetMap&layers=global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg"),
								   osmTileManager(this,400,
									"www.openstreetmap.org",
							 		"/api/0.3/map?"),		
                                    osmhttp("www.openstreetmap.org"),
									tpPixmap(":/images/trackpoint.png"),
									geocoder ("brainoff.com")
{
    LIMIT=map.earthDist(10);

    newUploadedNode =NULL;
    newUploadedSegment = NULL;

    contours = false;
    wptSaved = false;
	displayOSM = true;
	displayGPX = true;
	showSegmentColours = false;

    actionMode = ACTION_WAY_BUILD;
    curSegType = "track";
    nSelectedPoints = 0;
    nSelectedTrackPoints = 0;
	tpts[0] = tpts[1] = -1;

    doingName = false;

	// change these to match the renderer 
	
    segpens["footpath"]= SegPen (QPen (Qt::green, 2), false );
    segpens["path"]= SegPen (QPen (QColor(0,192,0), 2), false);
    segpens["cycle path"]= SegPen(QPen (Qt::magenta, 2), false);
    segpens["bridleway"]= SegPen (QPen(QColor(192,96,0),2), false);
    segpens["byway"] = SegPen (QPen (Qt::red, 2), false);
    segpens["RUPP"] = SegPen (QPen (QColor(192,0,0), 2), false);
    segpens["minor road"]= SegPen (QPen(QColor(192,192,192), 2),true);
    segpens["residential road"]= SegPen(QPen (QColor(192,192,192), 1), true);
    segpens["B road"]= SegPen(QPen (QColor(253,191,111), 4), true);
    segpens["A road"]= SegPen(QPen (QColor(251,128,95), 4), true);
    segpens["Trunk A road"]= SegPen(QPen (QColor(127,201,127), 4), true);
    segpens["motorway"]= SegPen(QPen (QColor(128,155,192), 4), true);
    segpens["railway"]= SegPen(QPen (Qt::black, 2), false);
    segpens["permissive footpath"]= SegPen(QPen (QColor(0,192,0), 2), false);
    segpens["permissive bridleway"]= SegPen(QPen (QColor(170,85,0), 2), false);
    segpens["track"]= SegPen(QPen (QColor(128,128,128), 3), false);
    segpens["new forest track"]=SegPen(QPen(QColor(255,64,0),2), false);
    segpens["new forest cycle path"]= SegPen(QPen (Qt::magenta, 2), false);
    cerr<<"done segpens" << endl;

    areapens["wood"]= QPen (QColor(128,255,128));
    areapens["heath"]= QPen (QColor(255,255,192));
    areapens["lake"]= QPen (QColor(0,0,128));
    areapens["park"]= QPen (QColor(192,255,192));


    nodeReps["pub"] = new WaypointRep
            (":/images/pub.png","Helvetica",10, QColor(170,85,0));
    nodeReps["church"] = new WaypointRep ( ":/images/church.png");
    nodeReps["viewpoint"] = new WaypointRep(":/images/viewpoint.png");
    nodeReps["farm"] = new WaypointRep(":/images/farm.png",
                    "Helvetica",8,Qt::red);
    nodeReps["hill"] = new WaypointRep(
                    ":/images/peak.png","Helvetica",10, Qt::magenta);
    nodeReps["hamlet"] = new WaypointRep(
                    ":/images/place.png","Helvetica",12, Qt::black);
    nodeReps["village"] = new WaypointRep(
                    ":/images/place.png","Helvetica",16, Qt::black);
    nodeReps["small town"] = new WaypointRep(
                    ":/images/place.png","Helvetica",20, Qt::black);
    nodeReps["large town"] = new WaypointRep(
                    ":/images/place.png","Helvetica",24, Qt::black);
    nodeReps["car park"] = new WaypointRep(
                    ":/images/carpark.png", "Helvetica",8,Qt::blue);
    nodeReps["railway station"] = new WaypointRep(
                    ":/images/station.png", "Helvetica",10,Qt::red);
    nodeReps["mast"] = new WaypointRep(
                    ":/images/mast.png");
    nodeReps["point of interest"] = new WaypointRep
            (":/images/interest.png");
    nodeReps["suburb"] = new WaypointRep(
            ":/images/place.png","Helvetica",16, Qt::black);
    nodeReps["trackpoint"] = new WaypointRep(
                    ":/images/trackpoint.png","Helvetica",8,Qt::black);
    nodeReps["node"] = new WaypointRep(
                    ":/images/node.png","Helvetica",8,Qt::black);
    nodeReps["waypoint"] = new WaypointRep(
                    ":/images/waypoint.png","Helvetica",8,Qt::black);

    nodeReps["campsite"] = new WaypointRep(
                    ":/images/campsite.png","Helvetica",8,QColor(0,128,0));
    nodeReps["restaurant"] = new WaypointRep(
                    ":/images/restaurant.png","Helvetica",8,QColor(128,0,0));
    nodeReps["bridge"] = new WaypointRep(":/images/bridge.png");
    nodeReps["tea shop"] = new WaypointRep(
                    ":/images/teashop.png","Helvetica",8,Qt::magenta);
    nodeReps["country park"] = new WaypointRep(":/images/park.png",
                            "Helvetica",8,QColor(0,192,0));
    nodeReps["industrial area"] = new WaypointRep(":/images/industry.png",
                            "Helvetica",8,Qt::darkGray);
    nodeReps["barn"] = new WaypointRep(":/images/barn.png");
    curFilename = curFiletype = "";

    trackpoints=true;

    components = new Components2;
    osmtracks = new Components2;

	clearSegments();

	setFocus(Qt::ActiveWindowFocusReason);
    setFocusPolicy(Qt::ClickFocus);

    showPosition();

    username = "";
    password = "";

    liveUpdate = false;

    cerr<<"end constructor"<<endl;
    movingNode = NULL;     

	makingWay = false;

    QObject::connect(&osmhttp,SIGNAL(httpErrorOccurred(int,const QString&)),
                    this,SLOT(handleHttpError(int,const QString&)));
    QObject::connect(&osmhttp,SIGNAL(errorOccurred(const QString&)),
                    this,SLOT(handleNetCommError(const QString&)));

	serialPort = "/dev/ttyS0";

	selWay = builtWay = NULL;
	splitter = NULL;

	uploader = NULL;

}

MapWidget::~MapWidget()
{
    for (std::map<QString,WaypointRep*>::iterator i=nodeReps.begin();
         i!=nodeReps.end(); i++)
    {
        delete i->second;
    }

    components->destroy();
    delete components;
}

void MapWidget::open()
{
    QFileDialog* fd = new QFileDialog( this,"Open osm or gpx...", ".");
    fd->setViewMode( QFileDialog::List );
	QStringList types;
	types << "GPS Exchange (*.gpx)" << "Openstreetmap data (*.osm)";
	fd->setFilters(types);
    fd->setFileMode( QFileDialog::ExistingFile );

    if ( fd->exec() != QDialog::Accepted )
    {   delete fd;
        return;
    }
	QStringList files = fd->selectedFiles();
    if (files.isEmpty())
    {   delete fd;
        return;
    }
    QString filename = files[0];
    if (fd->selectedFilter().contains("osm"))
    {  // OSM DATA
        Components2 *newComponents = doOpen(filename);
        if(newComponents)
        {
            try
            {
                map.centreAt(newComponents->getAveragePoint());
                if(components)
                {
                    components->merge(newComponents);
                    delete newComponents;
                }
                else
                {
                    components = newComponents;
                }
                curFilename = filename;
                showPosition();
                update();
            }
            catch(QString str)
            {
                // blank track, trackseg etc
            }
        }
    }
    else   
    {   // GPX Data
        Components2 * comp;
		GPXParser2 parser;
    	QFile file(filename);
    	QXmlInputSource source(&file);
    	QXmlSimpleReader reader;
    	reader.setContentHandler(&parser);
    	reader.parse(source);
    	comp = parser.getComponents(); 
		map.centreAt(comp->getAverageTrackPoint());
		if(components)
		{
			components->merge(comp);
			delete comp;
		}
		else
		{
			components = comp;
		}
		showPosition();
		update();
    }
    /* Delete fd; ? */
	delete fd;
}

Components2 * MapWidget::doOpen(const QString& filename)
{
    cerr<<"doOpen"<<endl;
    Components2 * comp;

   
    OSMParser2 parser;
    QFile file(filename);
    QXmlInputSource source(&file);
    QXmlSimpleReader reader;
    reader.setContentHandler(&parser);
    reader.parse(source);
    comp = parser.getComponents(); 
    return comp;
   
}

void MapWidget::loginToLiveUpdate()
{
    QString str = "WARNING!!! The login and password you supply will be\n"\
                  "stored in osm-editor's memory and sent to the server\n"\
                  "each time you perform a live update action.\n"\
                  "This will happen until\n"\
                "you select Logout or exit osm-editor. If you\n "\
                "leave the computer, someone else will be able to modify\n "\
                "OSM data!!! Press Cancel next if you're unhappy!";
//    QMessageBox::warning(this,"Warning!",str);
    LoginDialogue *ld=new LoginDialogue(this);
    if(ld->exec())
    {
        username = ld->getUsername();
        password = ld->getPassword();
        liveUpdate = true;
        showPosition();
    }
    delete ld;
}

void MapWidget::logoutFromLiveUpdate()
{
    username = "";
    password = "";
    liveUpdate = false;
    showPosition();
}

void MapWidget::grabOSMFromNet()
{
	if(osmTileManager.isActive())
	{
		QMessageBox::information(this,
				"Cannot grab if tiled retrieval active",
				"You cannot grab OSM data if tiled retrieval is active.");
	}
	else
	{
    	QString url="http://www.openstreetmap.org/api/0.3/map";
    	QString uname="", pwd="";
    	Components2 * netComponents;
    	emit message("Grabbing data from OSM...");
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
        	emit message("Grabbing data from OSM...");
        	EarthPoint bottomLeft = map.getBottomLeft(),
               topRight = map.getTopRight();
        	QString url;
        	url.sprintf("/api/0.3/map?bbox=%lf,%lf,%lf,%lf",
                            bottomLeft.x,bottomLeft.y,
                            topRight.x,topRight.y);

			osmhttp.setAuthentication(uname,pwd);
			osmhttp.scheduleCommand("GET",url,QByteArray(),
								this,
								SLOT(loadComponents(const QByteArray&,void*)));
    	}
	}
}

void MapWidget::grabGPXFromNet()
{
    QString url;
    QString uname="", pwd="";
    EarthPoint bottomLeft = map.getBottomLeft(), topRight = map.getTopRight();
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
        emit message("Grabbing tracks from OSM...");
        EarthPoint bottomLeft = map.getBottomLeft(),
               topRight = map.getTopRight();
        QString url;
        url.sprintf("/api/0.3/trackpoints?bbox=%lf,%lf,%lf,%lf&page=0",
                            bottomLeft.x,bottomLeft.y,
                            topRight.x,topRight.y);

		osmhttp.setAuthentication(uname,pwd);
		osmhttp.scheduleCommand("GET",url,QByteArray(),
								this,
								SLOT(loadOSMTracks(const QByteArray&,void*)));
    }
}

void MapWidget::loadComponents(const QByteArray& array,void*)
{
	//QMessageBox::information(this,"Received data", "Data has been received.");
	cerr << "loadComponents()" << endl;

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
	showPosition();
    update();
}


// 240306 hacky upload stuff removed as batch upload of nodes no longer
// appears to be supported by the server.
void MapWidget::uploadOSM()
{
	QMessageBox::information(this,
                        "Batch OSM upload temporarily unavailable",
                        "Batch OSM upload temporarily unavailable"); 
}


void MapWidget::readGPS()
{
    try
    {
        GPSDevice2 device ("Garmin", serialPort.toAscii().constData());
        Components2 *c = device.getSurveyedComponents();
        if(c)
        {
            map.centreAt(c->getAveragePoint());     
            cerr << "GPS read done. " << std::endl;
            if(components)
            {
                components->merge(c);
                delete c;
            }
            else
            {
                components = c;
            }
            showPosition();
            update();
        }
    }
    catch(QString str)
    {
        cerr<<str.toAscii().constData()<<endl;
    }
}

void MapWidget::save()
{
    if(curFilename == "")
        saveAs();
    else if (curFiletype=="osm")
        saveOSM(curFilename);
	else
		saveGPX(curFilename);
}

void MapWidget::saveAs()
{
    QFileDialog* fd = new QFileDialog( this,"Save data...", ".");
    fd->setViewMode( QFileDialog::List );
	QStringList types;
	types << "GPS Exchange (*.gpx)" << "Openstreetmap data (*.osm)";
	fd->setFilters(types);
    fd->setFileMode( QFileDialog::AnyFile );

    if ( fd->exec() != QDialog::Accepted )
    {   delete fd;
        return;
    }
	QStringList files = fd->selectedFiles();
    if (files.isEmpty())
    {   delete fd;
        return;
    }
    QString filename = files[0];
    if (fd->selectedFilter().contains("osm"))
    {  // OSM DATA
		saveOSM(filename);
    }
    else   
    {   // GPX Data
		saveGPX(filename);
    }
    /* Delete fd; ? */
	delete fd;
}

void MapWidget::saveOSM(const QString& filename)
{
//      components->toGPX(filename);   
    curFilename = filename;
	curFiletype = "osm";
    QFile file (filename);
    if(file.open(QIODevice::WriteOnly))
    {
        QTextStream strm(&file);
        components->toOSM(strm,true);
        file.close();
    }
}

void MapWidget::saveGPX(const QString& filename)
{
    curFilename = filename;
	curFiletype = "gpx";
    QFile file (filename);
    if(file.open(QIODevice::WriteOnly))
    {
        QTextStream strm(&file);
        components->toGPX(strm);
        file.close();
    }
}
void MapWidget::quit()
{
    QApplication::exit(0);
}

void MapWidget::setMode(int m)
{
    actionMode=  m;

    // Display the appropriate mode toolbar button and menu item checked, and
    // all the others turned off.
	/* 120706 doesn't seem to be necessary in Qt4
    for (int count=0; count<N_ACTIONS; count++)
        modeActions[count]->setChecked(count==m);
	*/

    // Wipe any currently selected points
    nSelectedPoints = 0;

	cerr<<"setMode(): CLEARING SEGMENTS" << endl;
	clearSegments();

	if (m!=ACTION_WAY_BUILD && builtWay) 
	{
		if(builtWay->getOSMID()<=0)
			delete builtWay;
		builtWay = NULL;
	}

    movingNode = NULL;
    pts[0]=pts[1]=NULL;
    doingName = false;
    update();
}

void MapWidget::toggleNodes()
{
    trackpoints = !trackpoints;
    update();
}

void MapWidget::toggleLandsat()
{
	landsatManager.toggleDisplay();
    update();
}

void MapWidget::toggleTiledOSM()
{
	if(liveUpdate)
	{
		osmTileManager.setAuthentication(username,password);
		osmTileManager.toggleDisplay();
    	update();
	}
	else
	{
		QMessageBox::information(this,"Can only do in live update mode!",
					"You need to be in Live Update mode to do this.");
	}
}

void MapWidget::toggleOSM()
{
	displayOSM = !displayOSM;
    update();
}

void MapWidget::toggleGPX()
{
	displayGPX = !displayGPX;
    update();
}

void MapWidget::toggleContours()
{
    contours = !contours;
    update();
}

void MapWidget::toggleSegmentColours()
{
   	showSegmentColours = !showSegmentColours; 
    update();
}

void MapWidget::undo()
{
    //TODO
    update();       
}

void MapWidget::paintEvent(QPaintEvent* ev)
{
    QPainter p(this);
    drawLandsat(p);
	drawAreas(p);
	drawGPX(p);
    curPainter = &p; // needed for the contour "callback"
    drawContours();
	drawTrackPoints(p,components,QColor(255,128,192),true);
    drawSegments(p);
    drawNodes(p);
    curPainter = NULL;
    if(movingNode)
    {
        drawMoving(p);
    }
}

void MapWidget::drawLandsat(QPainter& p)
{
    landsatManager.drawTilesNew(p);
}

void MapWidget::drawContours()
{
    if(contours)
    {
        SRTMConGen congen(map,1);
        congen.generate(this);
    }
}

void MapWidget::drawContour(int x1,int y1,int x2,int y2,int r,int g,int b)
{
    if(curPainter)
    {
        curPainter->setPen(QColor(r,g,b));
        curPainter->drawLine(x1,y1,x2,y2);
    }
}

void MapWidget::drawAngleText(int fontsize,double angle,int x,int y,int r,
                                int g, int b, char *text)
{
    if(curPainter)
    {
        curPainter->setFont(QFont("Helvetica",fontsize));
        doDrawAngleText(curPainter,x,y,x,y,angle,text);
    }
}

void MapWidget::doDrawAngleText(QPainter *p,int originX,int originY,int x,
                int y,double angle, const char * text)
{
    angle *= 180/M_PI;
    p->translate(originX,originY);
    p->rotate(angle);
    p->drawText(x-originX,y-originY,text);
    p->rotate(-angle);
    p->translate(-originX,-originY);
}

void MapWidget::heightShading(int x1,int y1,int x2,int y2,int x3,int y3,
                                int x4,int y4,int r,int g, int b)
{

}

void MapWidget::drawAreas(QPainter& p)
{
	if(displayOSM)
	{
		for(int count=0; count<components->nWays(); count++)
			if(components->getWay(count)->isArea())
				drawArea(p,components->getWay(count));
	}
}

// draw an area
// WARNING! All segments must be orientated in the same direction for this
// to work!!!
void MapWidget::drawArea(QPainter& p, Way *area)
{
	if(areapens.find(area->getType()) != areapens.end())
	{
		cerr << "that type exists!" << endl;
		QPen pen = areapens[area->getType()];
		p.setPen(pen);
		p.setBrush(QBrush(pen.color(),Qt::SolidPattern));

		QPolygon polygon;
		ScreenPos pos;

		int count=0, added=0;
		while(!area->getSegment(count) && count<area->nSegments())
		{
			count++;
		}

		if(count<area->nSegments())
		{
			pos = map.getScreenPos
				(area->getSegment(count)->firstNode()->getLon(),
				 area->getSegment(count)->firstNode()->getLat());
			polygon << QPoint(pos.x,pos.y);
		}

		while(count<area->nSegments())
		{
			if(area->getSegment(count))
			{
				pos = map.getScreenPos
				(area->getSegment(count)->secondNode()->getLon(),
				 area->getSegment(count)->secondNode()->getLat());
				polygon << QPoint(pos.x,pos.y);
			}
			count++;
		}

		p.drawPolygon(polygon);
		p.setBrush(Qt::NoBrush);
	}
}

void MapWidget::drawSegments(QPainter& p)
{
	if(displayOSM)
	{
    	Segment *curSeg;
    	QString segname;


		for(int count=0; count<components->nSegments(); count++)
    	{
        	drawSegment(p,components->getSegment(count));
    	}
	}
}

void MapWidget::drawSegment(QPainter& p, Segment *curSeg)
{
	ScreenPos pt1=map.getScreenPos(curSeg->firstNode()->getLon(),
                                curSeg->firstNode()->getLat()),
		  	  pt2=map.getScreenPos(curSeg->secondNode()->getLon(),
						curSeg->secondNode()->getLat());
		
	// 270706 do this test at the beginning to improve speed
	if(map.pt_within_map(pt1) || map.pt_within_map(pt2))
	{
        double dx, dy;
        QFont f("Helvetica",10,QFont::Bold,false);

        QFontMetrics fm(f);
        p.setFont(f);

		bool found = false, foundWay = false;

		for(int count=0; count<selSeg.size(); count++)
		{
			if(curSeg==selSeg[count])
			{
				found=true;
				break;
			}
		}

		if(selWay && components->getWayByID(curSeg->getWayID())==selWay)
		{
			foundWay=true;
		}

		// 030706 draw only ways in colours. un-wayed segments shown in
		// a neutral colour to encourage way tagging and make things clearer.
		
        QPen curPen = (foundWay) ? QPen(QColor(255,170,0),5) : ( (found) ?
                        QPen(Qt::yellow,5) : 
						QPen((curSeg->getOSMID()>0) ? QColor(128,128,128):
								QColor(192,192,192),1) );

        curPen.setStyle ((curSeg->getOSMID()>0) ?  Qt::SolidLine: Qt::DotLine );
		
        if(curSeg->hasNodes())
        {
				// Draw segments belonging to ways (only) in the correct colour
				if(curSeg->getWayStatus() && !found && !foundWay)
				{
					Way *w;
					if(w=components->getWayByID(curSeg->getWayID()))
					{
						if(segpens.find(w->getType()) != segpens.end())
						{
							curPen = segpens[w->getType()].pen;

							if(segpens[w->getType()].casing)
							{
								p.setPen(QPen(Qt::black,curPen.width()+2));
                				p.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);
							}
						}

						// If the segment is the longest segment in a way, draw
						// its name
						if(w->getName()!="")
						{
							dy=pt2.y-pt1.y;
							dx=pt2.x-pt1.x;
							if(fm.width(w->getName()) <=fabs(dx) &&
									curSeg==w->longestSegment())
							{
                    			double angle = atan2(dy,dx);
                    			doDrawAngleText(&p,pt1.x,pt1.y,pt1.x,pt1.y,
                                angle,w->getName().toAscii().constData());
							}
                		}
					}
					else
					{
						curPen = QPen(QColor(128,128,128), 3);
					}
				}
				// If the user has selected to display unwayed segments in
				// colour, find the appropriate colour.
				else if (showSegmentColours && !found && !foundWay) 
				{
					if(segpens.find(curSeg->getType()) != segpens.end())
					{
						curPen = segpens[curSeg->getType()].pen;
        				curPen.setStyle ((curSeg->getOSMID()>0) ?  
										Qt::SolidLine: Qt::DotLine );
						curPen.setWidth(1); // unwayed segments always 1
					}
				}

				if(1)
				{
					int s = (curSeg->belongsToWay()) ? 4:8;
        			p.setPen(curPen);
                	p.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);
					p.setBrush(Qt::SolidPattern);
					p.fillRect( pt1.x-s/2,pt1.y-s/2,s,s,QColor(128,128,128) );
					p.fillRect( pt2.x-s/2,pt2.y-s/2,s,s,QColor(128,128,128) );
				}
            
        }
	}
}

void MapWidget::drawGPX(QPainter& p)
{
	if(displayGPX)
	{
		drawTrackPoints(p,osmtracks,QColor(255,192,128),false);
	}
}

void MapWidget::drawNodes(QPainter& p)
{
	if(displayOSM)
	{
		for(int count=0; count<components->nNodes(); count++)
    	{
        	drawNode(p,components->getNode(count));
    	}
	}
}

void MapWidget::drawTrackPoints(QPainter& p,Components2 *comp,QColor colour,
									bool join)
{
	TrackPoint *prev = NULL, *current;
	ScreenPos prevPos, currentPos;
	QString idAsText;

	for(int count=0; count<comp->nTrackPoints(); count++)
    {
		current = comp->getTrackPoint(count);
    	currentPos = map.getScreenPos(current->getLon(),current->getLat());
		if(prev)
		{
			p.setPen ( (count>tpts[0] && count<=tpts[1] && comp==components ) ?
					QPen(colour,3): QPen(colour,1) );
			if(join && OpenStreetMap::dist(current->getLat(),current->getLon(),
									prev->getLat(),prev->getLon() ) <= 0.05)
			{
				p.drawLine(prevPos.x,prevPos.y,currentPos.x,currentPos.y);
			}
		}
		int r = (count==tpts[0] || count==tpts[1] && comp==components ) ? 8:4;
		p.setBrush((count==tpts[0] || count==tpts[1] && comp==components ) ? 
						Qt::SolidPattern: Qt::NoBrush);
		p.drawEllipse(currentPos.x-r/2,currentPos.y-r/2,r,r);

		if(comp==components)
		{
			idAsText.sprintf("%d", count);
			p.setFont(QFont("Helvetica",8));
			p.drawText(currentPos.x+3,currentPos.y+3,idAsText);
		}

		prev = current;
		prevPos = currentPos;
    }
}

void MapWidget::drawMoving(QPainter& p)
{
    drawNode(p,movingNode);
    for(int count=0; count<movingNodeSegs.size(); count++)
        drawSegment(p,movingNodeSegs[count]);
}

void MapWidget::drawNode(QPainter& p,Node* node)
{
    ScreenPos pos = map.getScreenPos(node->getLon(),node->getLat());
    if(map.pt_within_map(pos))
    {
		vector<Segment*> containingSegs = components->getSegs(node);

		// don't draw nodes of type 'node' which belong to segments
		if((containingSegs.empty()||node->getType()!="node") &&
				nodeReps.find(node->getType()) != nodeReps.end())
		{
        	WaypointRep* img=nodeReps[node->getType()];
        	if(img) img->draw(p,pos.x,pos.y,node->getName());

        	if(node==pts[0] || node==pts[1] || node==movingNode)
        	{
            	p.setPen(QPen(Qt::red,3));
				p.setBrush(Qt::NoBrush);
            	p.drawEllipse( pos.x - 16, pos.y - 16, 32, 32 );
        	}
		}
    }
}

void MapWidget::drawTrackPoint(QPainter &p,TrackPoint *tp)
{
}

void MapWidget::removeTrackPoints()
{
    components->removeTrackPoints();
    update();
}

void MapWidget::mousePressEvent(QMouseEvent* ev)
{
    EarthPoint p = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
    QString name;
    int nearest;
    Node *n;
	Segment *s1;

    switch(actionMode)
    {
        case ACTION_NODE:
            editNode(ev->x(),ev->y(),LIMIT);       
            break;

        case ACTION_MOVE_NODE:
            movingNode = components->getNearestNode(p.y,p.x,LIMIT);
            if(movingNode)
            {
                movingNodeSegs = components->getSegs(movingNode);
                movingNodes.clear();
                for(int count=0; count<movingNodeSegs.size(); count++)
                {
                    if(movingNodeSegs[count]->firstNode()!=movingNode)
                    {
                        movingNodes.push_back
                                (movingNodeSegs[count]->firstNode());
                    }
                    else
                    {
                        movingNodes.push_back
                                (movingNodeSegs[count]->secondNode());
                    }
                }
                movingNodes.push_back(movingNode);
                repaint();
            }
            break;
        case ACTION_DELETE_NODE:
            n = components->getNearestNode(p.y,p.x,LIMIT);
            if(n)
            {
                QString url;
                components->deleteNode(n);
                if(liveUpdate && n->getOSMID()>0)
                {
                    url.sprintf ("/api/0.3/node/%d", n->getOSMID());
                    osmhttp.setAuthentication(username, password);
                    osmhttp.scheduleCommand("DELETE", url);
                }
               
                // If this node is part of any segments, delete the segments
                vector<Segment*> containingSegs = components->getSegs(n);
                for(int count=0; count<containingSegs.size(); count++)
                {
                        components->deleteSegment(containingSegs[count]);
                        /* Deleting a node will effectively delete the
                         * segments on the server - so no need to do this
                         *
                        if(liveUpdate&&containingSegs[count]->getOSMID()>0)
                        {
                            url.sprintf ("/api/0.2/segment/%d",
                                    containingSegs[count]->getOSMID());
                            osmhttp.scheduleCommand("DELETE", url);
                        }
                        */
                        delete containingSegs[count];
                }
               
                delete n;
                update();
            }
            break;
        case ACTION_NEW_SEG:
            if(nSelectedPoints==0)
            {
                pts[0] = components->getNearestNode(p.y,p.x,LIMIT);
                if(!pts[0] || pts[0]->getOSMID()<=0)
                    pts[0]=doAddNewNode(p.y,p.x,"","node");
                update();
                nSelectedPoints++;
            }
            else
            {
                pts[1] = components->getNearestNode(p.y,p.x,LIMIT);
                if(!pts[1] || pts[1]->getOSMID()<=0)
				{
					pts[1] = components->addNewNode(p.y,p.x,"","node");
                	Segment *segx= components->addNewSegment (pts[0],pts[1]);
					nodeHandler.setEmit
							(segx,this,SLOT(doaddseg(void*)));
					if(liveUpdate)
					{
						QByteArray xml = pts[1]->toOSM();
						QString url = "/api/0.3/node/0";
						osmhttp.setAuthentication(username, password);
						newUploadedNode = pts[1];
						osmhttp.scheduleCommand("PUT",url,xml,
							&nodeHandler,
							SLOT(newNodeAdded(const QByteArray&,void*)),
							newUploadedNode,
							SLOT(handleNetCommError(const QString&)), this);
					}
				}
				else
				{
                	Segment *segx=
					    components->addNewSegment(pts[0],pts[1]);
					doaddseg(segx);
				}

                pts[0]=pts[1]=NULL;
                nSelectedPoints=0;

				update();
			}

				
					
            break;
		case ACTION_WAY_BUILD:
				if(!builtWay)
					builtWay = new Way(components);
                pts[0] = components->getNearestNode(p.y,p.x,LIMIT);
                if(!pts[0] || pts[0]->getOSMID()<=0)
				{
					if(!pts[0]) 
						pts[0] = components->addNewNode(p.y,p.x,"","node");
					if(pts[1])
					{
                		Segment *segx=
								components->addNewSegment(pts[1],pts[0]);
						nodeHandler.setEmit
							(segx,this,SLOT(doaddseg(void*)));
					}
					if(liveUpdate)
					{
						QByteArray xml = pts[0]->toOSM();
						QString url = "/api/0.3/node/0";
						osmhttp.setAuthentication(username, password);
						osmhttp.scheduleCommand("PUT",url,xml,
							&nodeHandler,
							SLOT(newNodeAdded(const QByteArray&,void*)),
							pts[0],	
							SLOT(handleNetCommError(const QString&)), this);
					}
				}
				else if (pts[1])
				{
                	Segment *segx=
					    components->addNewSegment(pts[1],pts[0]);
					doaddseg(segx);
				}

				pts[1]=pts[0];
				update();
					
            	break;

		case ACTION_BREAK_SEG:
			// 030806 no longer use selected segment - use nearest segment
			// to click
			s1= components->getNearestSegment(p.y,p.x,LIMIT);
			if(s1 && splitter==NULL)
			{
				splitter = new SegSplitter;
				splitter->setComponents(components);
				splitter->setHTTPHandler(&osmhttp);
				splitter->splitSeg(s1,p,LIMIT);
				QObject::connect(splitter,SIGNAL(done()),this,
									SLOT(splitterDone()));
				QObject::connect(splitter,SIGNAL(error(const QString&)),this,
									SLOT(segSplitterError(const QString&)));
			}
			break;

        case  ACTION_SEL_SEG:
		case  ACTION_SEL_WAY:

			/* NEW CODE TO SELECT SEGMENT DIRECTLY RATHER THAN VIA NODES */

			// 280306 only push back the new segment if we're making a
			// way if the current last segment is not NULL. In this
			// way, we can re-use the current last segment if selection
			// of the last segment was unsuccessful.
			
			s1= components->getNearestSegment(p.y,p.x,LIMIT);
			if(s1)
			{
					
				// If we're in way select mode, select the whole
				// parent way of the segment
				if(actionMode==ACTION_SEL_WAY)
				{
					cerr<<"mode=ACTION_SEL_WAY" << endl;
					clearSegments();
					int wayID = s1->getWayID();
					if(wayID)
					{
						selWay = components->getWayByID(wayID);
					}
				}
				// Otherwise, just select the segment
				else
				{
					if(!makingWay)
						clearSegments();
					selWay = NULL;
					selSeg.push_back(s1);
				}
			}
			update();
            break;
        case ACTION_SEL_TRACK:
            if(nSelectedTrackPoints==0)
            {
                if((tpts[0] = components->getNearestTrackPoint(p.y,p.x,LIMIT))
						>=0)
				{
                	nSelectedTrackPoints++;
					tpts[1] = -1;
					update();
				}
            }
            else
            {
                tpts[1] = components->getNearestTrackPoint(p.y,p.x,LIMIT);
				if(tpts[1]>=0)
				{
					nSelectedTrackPoints = 0;
					update();
				}
			}

            break;
    }               
}

void MapWidget::resizeEvent(QResizeEvent * ev)
{
    map.resizeTopLeft(width(), height());
    update();
    LIMIT=map.earthDist(10);
}


void MapWidget::editNode(int x,int y,int limit)
{
    Node *nearest = NULL;   
    WaypointDialogue *d;

    EarthPoint p = map.getEarthPoint(ScreenPos(x,y));
    if((nearest=components->getNearestNode(p.y,p.x,LIMIT)) != NULL)
    {
        d = new WaypointDialogue
                    (this,nodeReps,"Edit node",
                    nearest->getType(),nearest->getName());
        if(d->exec())
        {
            nearest->setName(d->getName());
            nearest->setType(d->getType());
            if(liveUpdate && nearest->getOSMID()>0)
            {
                QByteArray xml = nearest->toOSM();
                QString url;
                url.sprintf ("/api/0.3/node/%d", nearest->getOSMID());
                osmhttp.setAuthentication(username, password);
                osmhttp.scheduleCommand("PUT", url, xml);
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
			doAddNewNode(p.y,p.x,d->getName(),d->getType());
		}

        update();
        delete d;
    }
}

void MapWidget::mouseMoveEvent(QMouseEvent* ev)
{
    EarthPoint p = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
    switch(actionMode)
    {
        case ACTION_MOVE_NODE:
            if(movingNode)
            {

                movingNode->setCoords(p.y,p.x);

                ScreenPos min, max, cur;
                min.x=width(); min.y=height(); max.x=0; max.y=0;

                for(int count=0; count<movingNodes.size(); count++)
                {
                    cur = map.getScreenPos(movingNodes[count]->getLon(),
                                            movingNodes[count]->getLat());
                    if(cur.x<min.x)
                        min.x=cur.x;
                    if(cur.y<min.y)
                        min.y=cur.y;
                    if(cur.x>max.x)
                        max.x=cur.x;
                    if(cur.y>max.y)
                        max.y=cur.y;
                }

                repaint(min.x-19,min.y-19,(max.x-min.x)+38,(max.y-min.y)+38);
            }
    }
}

void MapWidget::mouseReleaseEvent(QMouseEvent* ev)
{
    EarthPoint p = map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
    QString name;
    double LIMIT=map.earthDist(10);
    switch(actionMode)
    {
        case ACTION_MOVE_NODE:
            if(movingNode)
            {
                movingNode->setCoords(p.y,p.x);
                if(liveUpdate && movingNode->getOSMID()>0)
                {
                    QByteArray xml = movingNode->toOSM();
                    QString url;
                    url.sprintf ("/api/0.3/node/%d", movingNode->getOSMID());
                    osmhttp.setAuthentication(username, password);
                    osmhttp.scheduleCommand("PUT", url, xml);
                }
                movingNode = NULL;     
                movingNodeSegs.clear();
                update();
            }
    }
}

void MapWidget::keyPressEvent(QKeyEvent* ev)
{
    bool typingName = false;

    switch(ev->key())
    {
        case Qt::Key_Left  : left(); break;
        case Qt::Key_Right : right(); break;
        case Qt::Key_Up    : up(); break;
        case Qt::Key_Down  : down(); break;
    }

    // Now use CTRL for the shortcuts so it doesn't interfere with
    // entering text and you can scale up and down etc when entering text
    if(ev->modifiers()==Qt::ControlModifier)
    {
        // 11/04/05 prevent movement being too far at large scales
        double dis = 0.1/map.getScale();

        switch(ev->key())
        {
            case Qt::Key_Plus  : magnify(); break;
            case Qt::Key_Minus : shrink(); break;
        }
    }
}

void MapWidget::left()
{
    map.movePx(-landsatManager.getTileSize(),0);
    landsatManager.grab();
	osmTileManager.grab();
    showPosition();
    update();
}

void MapWidget::right()
{
    map.movePx(landsatManager.getTileSize(),0);
    landsatManager.grab();
	osmTileManager.grab();
    showPosition();
    update();
}

void MapWidget::up()
{
    map.movePx(0,-landsatManager.getTileSize());
    landsatManager.grab();
	osmTileManager.grab();
    showPosition();
    update();
}

void MapWidget::down()
{
    map.movePx(0,landsatManager.getTileSize());
    landsatManager.grab();
	osmTileManager.grab();
    showPosition();
    update();
}

void MapWidget::magnify()
{
    map.rescale(2);
	landsatManager.clearRequests();
    landsatManager.grab();
    showPosition();
    update();
    LIMIT=map.earthDist(10);
}

void MapWidget::shrink()
{
    map.rescale(0.5);
	landsatManager.clearRequests();
    landsatManager.grab();
	osmTileManager.grab();
    showPosition();
    update();
    LIMIT=map.earthDist(10);
}

void MapWidget::grabLandsat()
{
    // 01/05/05 grab three times current screen width and height (i.e. 9
    // times screen area) and centre at current
    // map centre. This will be configurable.
   

	cerr << "MapWidget::grabLandsat()" << endl;
    landsatManager.forceGrab();
    update();


}

void MapWidget::nameTrackOn()
{

    if(selSeg.size()==1)
    {
        Node *n1 = selSeg[0]->firstNode(), 
			*n2 = selSeg[0]->secondNode();
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


void MapWidget::newSegmentAdded(const QByteArray& array, void *segment)
{
	Segment *seg = (Segment*) segment;
    QString str = array;
    QStringList ids;
    ids = str.split("\n");
    if(seg)
    {
        cerr<<"NEW UPLOADED SEGMENT IS NOT NULL::SETTING ID"<<endl;
        seg->setOSMID(atoi(ids[0].toAscii().constData()));
        newUploadedSegment = NULL;
        cerr<<"DONE."<<endl;

		// 030806 if in "easy" way build mode, add segment to the current way
		if(builtWay && actionMode==ACTION_WAY_BUILD)
			builtWay->addSegment(seg);
    }
    update();
}

void MapWidget::newWayAdded(const QByteArray& array,void *way)
{
	Way *w=(Way*)way;
    QString str = array;
    QStringList ids;
    ids = str.split("\n");
    if(w)
    {
        cerr<<"NEW UPLOADED Way/Area IS NOT NULL::SETTING ID"<<endl;
        w->setOSMID(atoi(ids[0].toAscii().constData()));

		// TEMPORARY
		//delete newUploadedWay;

        newUploadedWay = NULL;
        cerr<<"DONE."<<endl;
    }
    update();
}

void MapWidget::deleteSelectedSeg()
{
	// Now deletes a way if a way is selected
    if(actionMode==ACTION_SEL_SEG && selSeg.size()==1)
    {
        cerr<<"selseg exists" << endl;
        components->deleteSegment(selSeg[0]);
        if(liveUpdate && selSeg[0]->getOSMID()>0)
        {
            QString url;
			int wayID;
            url.sprintf ("/api/0.3/segment/%d", selSeg[0]->getOSMID());
            osmhttp.setAuthentication(username, password);
            osmhttp.scheduleCommand("DELETE", url);

			// 180506 If the segment is in a way, remove the segment from the
			// way and upload the changes to OSM
			if(wayID=selSeg[0]->getWayID())
			{
				Way *w = components->getWayByID(wayID);
				w->removeSegment(selSeg[0]);
            	url.sprintf ("/api/0.3/way/%d", wayID);
				osmhttp.scheduleCommand("PUT",url);
			}
        }
		cerr<<"deleteSelectedSeg(): setting segmnet to NULL" << endl;
        delete selSeg[0];
        selSeg.clear();
        update();
    }
	else if (actionMode==ACTION_SEL_WAY && selWay)
	{
        components->deleteWay(selWay);
        if(liveUpdate && selWay->getOSMID()>0)
        {
            QString url;
            url.sprintf ("/api/0.3/way/%d", selWay->getOSMID());
            osmhttp.setAuthentication(username, password);
            osmhttp.scheduleCommand("DELETE", url);
        }
        delete selWay;
        selWay = NULL;
        update();
	}
	// 060706 now also deletes GPX track if selected
	else if (actionMode==ACTION_SEL_TRACK && tpts[0]>=0 && tpts[1]>=0)
	{
		components->deleteTrackPoints(tpts[0],tpts[1]);
		tpts[0] = tpts[1] = -1;
		update();
	}
}

void MapWidget::handleHttpError(int code,const QString& reasonPhrase)
{
    if(code!=410)
    {
        QString errMsg;
        errMsg.sprintf("Error: %d ",code);
        errMsg += reasonPhrase;
        QMessageBox::information(this,
                        "An error occurred communicating with OSM",
                                errMsg);
    }
}

void MapWidget::handleNetCommError(const QString& error)
{
    QMessageBox::information(this,
                "An error occurred communicating with OSM", error);
}

void MapWidget::showPosition()
{
        QString msg;
        msg.sprintf("Lat %lf Long %lf",
                        map.getBottomLeft().y, map.getBottomLeft().x);
        if(username!="" && password!="")
            msg+=" Logged in - live update active!";
        emit message(msg);
}

void MapWidget::toggleWays()
{
	makingWay = !makingWay; 
	cerr<<"toggleWays(): CLEARING SEGMENTS"<<endl;
	clearSegments();
}

// uploadWay()
// also uploads areas

void MapWidget::uploadWay()
{
	Way *way;
	// If builtWay exists we upload that.
	// (builtWay is either the way constructed via 'easy build' mode, or the
	// way constructed via a batch upload)
	if(builtWay)
	{
		way = builtWay;
		builtWay = NULL;
		pts[0] = pts[1] = NULL;
	}
	// Otherwise create a way from the selected segments. This is used 
	// typically when constructing a way out of existing segments.
	else
	{
		way = new Way(components);

		way->setSegments(selSeg);
	}

	vector<QString> segTypes, areaTypes;

	for(std::map<QString,SegPen>::iterator i=segpens.begin(); 
						i!=segpens.end(); i++)
	{
		segTypes.push_back(i->first);
	}
	for(std::map<QString,QPen>::iterator i=areapens.begin(); i!=areapens.end();
        i++)
	{
		areaTypes.push_back(i->first);
	}

	WayDialogue *wd = new WayDialogue(this,segTypes,areaTypes);
	wd->setNote(way->getNote());
	if(wd->exec())
	{
		way->setName(wd->getName());
		//way->setArea(wd->isArea());
		way->setType(wd->getType());
		way->setNote(wd->getNote());

		if(!wd->isArea())
			way->setRef(wd->getRef()); // areas shouldn't have refs really
		components->addWay(way);

		QByteArray xml = way->toOSM();
		cerr<<"uploadWay(): CLEARING SEGMENTS"<<endl;
		clearSegments();
		if(liveUpdate)
		{
				/*
			QString url = wd->isArea() ? "/api/0.3/area/0" :
										"/api/0.3/way/0";
										*/
			QString url = "/api/0.3/way/0";

			newUploadedWay = way;
			osmhttp.setAuthentication(username, password);

			osmhttp.scheduleCommand("PUT",url,xml,
						this,SLOT(newWayAdded(const QByteArray&,void*)),
						newUploadedWay);
		}
	}
}

void MapWidget::changeWayDetails()
{
	vector<QString> segTypes, areaTypes;

	cerr<<"filling segTypes"<<endl;
    for(std::map<QString,SegPen>::iterator i=segpens.begin(); i!=segpens.end();
        i++)
    {
        segTypes.push_back(i->first);
    }
	cerr<<"filling areaTypes"<<endl;
    for(std::map<QString,QPen>::iterator i=areapens.begin(); i!=areapens.end();
        i++)
    {
        areaTypes.push_back(i->first);
    }

	if(selWay)
	{
		selWay->printTags();
		WayDialogue *wd = new WayDialogue(this,segTypes,areaTypes,
								selWay->getName(), selWay->getType(),
								selWay->getRef());
		wd->setNote(selWay->getNote());
		if(wd->exec())
		{
			selWay->setName(wd->getName());
			selWay->setType(wd->getType());
			selWay->setNote(wd->getNote());
			//selWay->setArea(wd->isArea());
			selWay->setRef(wd->getRef());
			QByteArray xml = selWay->toOSM();
			if(liveUpdate)
			{
				QString url;
				url.sprintf("/api/0.3/way/%d", selWay->getOSMID());
				osmhttp.setAuthentication(username, password);
				osmhttp.scheduleCommand("PUT",url,xml);
			}
		}
		delete wd;
	}
}

// doAddNewNode()
// 240306
// adds a new node to the components and uploads it if in live update mode.

Node *MapWidget::doAddNewNode(double lat,double lon,const QString &name,
									const QString& type)
{
	Node *n = components->addNewNode(lat,lon,name,type);
	if(liveUpdate)
	{
		QByteArray xml = n->toOSM();
		QString url = "/api/0.3/node/0";
		osmhttp.setAuthentication(username, password);
		osmhttp.scheduleCommand("PUT",url,xml,
						&nodeHandler,
						SLOT(newNodeAdded(const QByteArray&,void*)),
						n,SLOT(handleNetCommError(const QString&)),this);
	}
	return n;
}

void MapWidget::splitterDone()
{
	if(splitter)
	{
		delete splitter;
		splitter = NULL;
		update();
	}
}

void MapWidget::doaddseg(void *sg)
{
	nodeHandler.discnnect();
	Segment *segx = (Segment*) sg;
	cerr<<"doaddseg()"<<endl;
	if(segx && liveUpdate && segx->firstNode()->getOSMID()>0 &&
					segx->secondNode()->getOSMID()>0)
	{
		QByteArray xml = segx->toOSM();
		QString url = "/api/0.3/segment/0";
		osmhttp.setAuthentication(username, password);
		osmhttp.scheduleCommand("PUT",url,xml,
						this,SLOT(newSegmentAdded(const QByteArray&,void*)),
						segx);	
	}


	segx=NULL;
	
	update();
}

void MapWidget::changeSerialPort()
{
	serialPort = QInputDialog::getText(this,"Enter serial port",
						"Enter serial port, e.g. /dev/ttyS0 or COM1",
						QLineEdit::Normal, serialPort);
}
	
void MapWidget::uploadNewWaypoints()
{
	vector<Node*> newNodes = components->getNewNodes();

	QString url = "/api/0.3/node/0";
	osmhttp.setAuthentication(username, password);

	for(int count=0; count<newNodes.size(); count++)
	{
		if(newNodes[count]->getType()!="trackpoint" &&
			newNodes[count]->getType()!="node" &&
			// Stop people uploading those ****** Garmin waypoints !!!!
			newNodes[count]->getName()!="GARMIN" &&
			newNodes[count]->getName().left(3)!="GRM") 
		{
			osmhttp.scheduleCommand("PUT",url,newNodes[count]->toOSM(),
								&nodeHandler,
								SLOT(newNodeAdded(const QByteArray&,void*)),
								newNodes[count],
								SLOT(handleNetCommError(const QString&)),
								this);
		}
	}
}

void MapWidget::loadOSMTracks(const QByteArray& array,void*)
{
	//QMessageBox::information(this,"Received data", "GPX data received.");

	Components2 * comp;
	GPXParser2 parser;
    QXmlInputSource source;

    source.setData(array);
	QXmlSimpleReader reader;
	reader.setContentHandler(&parser);
	reader.parse(source);
	comp = parser.getComponents(); 
	if(osmtracks)
	{
		osmtracks->merge(comp);
		delete comp;
	}
	else
	{
		osmtracks = comp;
	}
	showPosition();
	update();
}

// This method uploads a selected section of the GPX track as OSM nodes and
// segments.
// Loop through all trackpoints, make a node and upload each one
// After loading nodes, create and upload the segments between them.
void MapWidget::batchUpload()
{
	// Only upload if selected GPX track...
	if(uploader==NULL && liveUpdate && tpts[0]>=0 && tpts[1]>=0)
	{
		uploader = new BatchUploader(components);
		QObject::connect(uploader,SIGNAL(done(Way*)),this,
						SLOT(batchUploadDone(Way*)));
		QObject::connect(uploader,SIGNAL(error(const QString&)),this,
						SLOT(batchUploadError(const QString&)));
		osmhttp.setAuthentication(username,password);
		uploader->setHTTPHandler(&osmhttp);
		uploader->batchUpload(tpts[0],tpts[1]);
    }
	else if (tpts[0]<0 || tpts[1]<0)
	{
		QMessageBox::warning(this,"No track selected", 
					"ERROR - no track selected!");
	}
	else if (uploader)
	{
		QMessageBox::warning(this,"Already doing a batch upload", 
					"ERROR - already doing a batch upload!");
	}
	else
	{
		QMessageBox::warning(this,"Need to be in live update mode", 
					"ERROR - need to be in live update mode!");
	}

}

void MapWidget::batchUploadDone(Way *way)
{
	if(uploader)
	{
		delete uploader;
		uploader = NULL;

		// 050806 set the built way to the way constructed via batch upload
		if(builtWay && builtWay->getOSMID()<=0)
			delete builtWay;
		builtWay = way;

		update();
	}
}

void MapWidget::batchUploadError(const QString& error)
{
	QMessageBox::warning(this,"Error with batch upload",error);
	delete uploader;
	uploader = NULL;
}

void MapWidget::segSplitterError(const QString& error)
{
	QMessageBox::warning(this,"Error with segment splitting",error);
	splitterDone();
}

void MapWidget::geocoderLookup(const QString& place,const QString &country)
{
	if(1)
	{
		cerr << "MapWidget::geocoderLookup: place=" <<
				place.toAscii().constData() << " country="
				<< country.toAscii().constData() << endl;

		QString url;
		/*
		url.sprintf("/gc.php?place=%s&country=%s",
				place.toAscii().constData(),
				country.toAscii().constData() );
		*/
		url.sprintf("/geocoder/rest/?city=%s,%s",
				place.toAscii().constData(),
				country.toAscii().constData() );
		cerr << "Geocoder URL: " << url.toAscii().constData() << endl;
		geocoder.scheduleCommand("GET",url,QByteArray(),
								this,
								SLOT(geocoderParse(const QByteArray&,void*)),
								NULL,
							SLOT(handleGeocoderError(const QString&)), this);
	}
	else
		cerr << "Geocoder already active" << endl;
}

void MapWidget::handleGeocoderError(const QString& error)
{
	/*
	if(geocoder!=NULL)
	{
		cerr<<"Geocoder Error: " << error.toAscii().constData() << endl;
		cerr<<"setting geocoder to NULL"<<endl;
		delete geocoder;
		geocoder=NULL;
	}
	else
		cerr<<"geocoder is NULL" << endl;
	*/
}

void MapWidget::geocoderParse(const QByteArray& data, void*)
{

	cerr<<"geocoderParse " << endl;
	Geocoder gparser;
	QXmlInputSource source;
	cerr<<"setting data " << endl;
    source.setData(data);
	QXmlSimpleReader reader;
	cerr<<"setting content handler " << endl;
	reader.setContentHandler(&gparser);
	cerr<<"parsing " << endl;
	reader.parse(source);
	cerr<<"centreAt " << endl;
	cerr << "point=" << gparser.getPoint().x<<","<<gparser.getPoint().y<<endl;
	if (gparser.valid())
	{
		map.centreAt(gparser.getPoint() );
		showPosition();
		update();
	}
	else
	{
		QMessageBox::information(this,"Couldn't find place",
								"Couldn't find that place!");
	}
}

}
