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
#include "WayDialogue.h"
#include "LoginDialogue.h"

#include <qxml.h>
#include "OSMParser2.h"
#include "GPXParser2.h"

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
                                    osmhttp("www.openstreetmap.org"),
									tpPixmap("images/trackpoint.png")
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
    segpens["minor road"]= QPen (QColor(128,128,128), 2);
    segpens["residential road"]= QPen (QColor(128,128,128), 1);
    segpens["B road"]= QPen (QColor(64,64,64), 2);
    segpens["A road"]= QPen (Qt::black, 2);
    segpens["motorway"]= QPen (Qt::blue, 2);
    segpens["railway"]= QPen (Qt::gray, 2);
    segpens["permissive footpath"]= QPen (Qt::green, 1);
    segpens["permissive bridleway"]= QPen (QColor(192,96,0), 1);
    segpens["track"]= QPen (QColor(192,192,192), 2);
    segpens["new forest track"]=QPen(QColor(128,64,0),2);
    segpens["new forest cycle path"]= QPen (QColor(128,0,0), 2);
    cerr<<"done segpens" << endl;


    // Construct the menus.
    QPopupMenu* fileMenu = new QPopupMenu(this);
    // 29/10/05 Only open "OSM" now
    fileMenu->insertItem("&Open",this,SLOT(open()),CTRL+Key_O);
    fileMenu->insertItem("&Save",this,SLOT(save()),CTRL+Key_S);
    fileMenu->insertItem("Save &as...",this,SLOT(saveAs()),CTRL+Key_A);
    fileMenu->insertItem("Save GPX...",this,SLOT(saveGPX()),CTRL+Key_X);
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
	fileMenu->insertItem("Upload waypoints",this, SLOT(uploadNewWaypoints()));
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
	editMenu->insertItem("Change serial port", this, SLOT(changeSerialPort()));
//      editMenu->insertItem("Undo",this,SLOT(undo()),CTRL+Key_Z);
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
    QPixmap breakseg = mmLoadPixmap("images","breakseg.png");
    QPixmap ways = mmLoadPixmap("images","ways.png");
    QPixmap uploadways = mmLoadPixmap("images","uploadways.png");
    QPixmap waydelete = mmLoadPixmap("images","waydelete.png");
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
//      moveDockWindow (toolbar2,Qt::DockLeft);


   
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
    modeButtons[ACTION_BREAK_SEG]= new QToolButton
            (breakseg,"Break segment","",mapper,SLOT(map()),toolbar2);
    new QToolButton
            (deleteseg,"Delete Segment","",this,
             SLOT(deleteSelectedSeg()),toolbar2);
    wayButton = new QToolButton
            (ways,"Ways on/off","",this,
             SLOT(toggleWays()),toolbar2);
	wayButton->setToggleButton(true);
	wayButton->setOn(false);

    new QToolButton
            (uploadways,"Upload current way","",this,
             SLOT(uploadWay()),toolbar2);
	
    new QToolButton
            (waydelete,"Delete Way","",this,SLOT(deleteWay()),toolbar2);
			

   
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

	clearSegments();

    setFocusPolicy(QWidget::ClickFocus);

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
    QFileDialog* fd = new QFileDialog( this,"openbox",true );
    fd->setViewMode( QFileDialog::List );
    fd->setCaption(tr("Open osm or gpx...")); 
    fd->setFilter("GPS Exchange (*.gpx)");
    fd->addFilter("Openstreetmap data (*.osm)");
    fd->setMode( QFileDialog::ExistingFile );

    QString filename;
    if ( fd->exec() != QDialog::Accepted )
    {   delete fd;
        return;
    }
    filename = fd->selectedFile();
    if (filename.isEmpty())
    {   delete fd;
        return;
    }
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
    	cerr<<"filename=" << filename<<endl;
    	QFile file(filename);
    	QXmlInputSource source(&file);
    	QXmlSimpleReader reader;
    	reader.setContentHandler(&parser);
    	reader.parse(source);
    	comp = parser.getComponents(); 
		map.centreAt(comp->getAveragePoint());
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

void MainWindow2::logoutFromLiveUpdate()
{
    username = "";
    password = "";
    liveUpdate = false;
    showPosition();
}

void MainWindow2::grabOSMFromNet()
{
    QString url="http://www.openstreetmap.org/api/0.3/map";
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
        url.sprintf("/api/0.3/map?bbox=%lf,%lf,%lf,%lf",
                            bottomLeft.x,bottomLeft.y,
                            topRight.x,topRight.y);
        cerr<<"SENDING URL: "<<url<<endl;

        if(!osmhttp.isMakingRequest())
        {
        	osmhttp.disconnect 
				(SIGNAL(responseReceived(const QByteArray&,void*)));
            QObject::connect
                    (&osmhttp,SIGNAL(responseReceived(const QByteArray&,void*)),
                         this, SLOT(loadComponents(const QByteArray&,void*)));
            osmhttp.setAuthentication(uname,pwd);
            osmhttp.sendRequest("GET", url);
        }
    }
}

void MainWindow2::loadComponents(const QByteArray& array,void*)
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


// 240306 hacky upload stuff removed as batch upload of nodes no longer
// appears to be supported by the server.
void MainWindow2::uploadOSM()
{
	QMessageBox::information(this,
                        "Batch OSM upload temporarily unavailable",
                        "Batch OSM upload temporarily unavailable"); 
}


void MainWindow2::readGPS()
{
    try
    {
        GPSDevice2 device ("Garmin", serialPort);
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
    QString filename = QFileDialog::getSaveFileName("","*.osm",this);
    if(filename!="")
        saveFile(filename);
}

void MainWindow2::saveFile(const QString& filename)
{
//      components->toGPX(filename);   
    curFilename = filename;
    QFile file (filename);
    if(file.open(IO_WriteOnly))
    {
        QTextStream strm(&file);
        components->toOSM(strm,true);
        file.close();
    }
}

void MainWindow2::saveGPX()
{
    QString filename = QFileDialog::getSaveFileName("","*.gpx",this);
    QFile file (filename);
    if(file.open(IO_WriteOnly))
    {
        QTextStream strm(&file);
        components->toGPX(strm);
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

	if(m!=ACTION_BREAK_SEG)
		clearSegments();

    movingNode = NULL;
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
	for(int count=0; count<selSeg.size(); count++)
	{
		if(selSeg[count])
			selSeg[count]->setType(curSegType);
	}

	if(!makingWay)	
    {
        // UPLOAD IF IN LIVE MODE
        if(liveUpdate && selSeg[segCount])
        {
        //      selSeg->uploadToOSM(username,password);
            QByteArray xml = selSeg[segCount]->toOSM();
            QString url;
            url.sprintf ("/api/0.3/segment/%d", selSeg[segCount]->getOSMID());
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
	//drawTrackPoints(p);
    drawSegments(p);
    drawNodes(p);
    curPainter = NULL;
    if(movingNode)
    {
            /*
        bitBlt(this,0,0, &savedPixmap,0,0,savedPixmap.width(),
                        savedPixmap.height(), Qt::CopyROP);
                        */
        drawMoving(p);
    }
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
    Segment *curSeg;
    QString segname;

    components->rewindSegments();

    while(!components->endSegment())
    {
        curSeg = components->nextSegment();
        drawSegment(p,curSeg);
    }
}

void MainWindow2::drawSegment(QPainter& p, Segment *curSeg)
{
        ScreenPos pt1, pt2;
        double dx, dy;
        QFont f("Helvetica",10,QFont::Bold,true);
        QFontMetrics fm(f);
        p.setFont(f);

		bool found = false;

		for(int count=0; count<selSeg.size(); count++)
		{
			if(curSeg==selSeg[count])
			{
				found=true;
				break;
			}
		}


        QPen curPen = (found) ?
                        QPen(Qt::yellow,5) : segpens[curSeg->getType()];

        curPen.setStyle ((curSeg->getOSMID()>0) ?  Qt::SolidLine: Qt::DotLine );
		
		if(curSeg->belongsToWay())
		{
			//cerr<<"segment belongs to a way"<<endl;
			curPen.setWidth(4);
		}
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

void MainWindow2::drawNodes(QPainter& p)
{
    int count=0;
    components->rewindNodes();
    while(!components->endNode())
    {
        drawNode(p,components->nextNode());
    }
}

void MainWindow2::drawTrackPoints(QPainter& p)
{
    int count=0;
    components->rewindTrackPoints();
    while(!components->endTrackPoint())
    {
        drawTrackPoint(p,components->nextTrackPoint());
    }
}

void MainWindow2::drawMoving(QPainter& p)
{
    drawNode(p,movingNode);
    for(int count=0; count<movingNodeSegs.size(); count++)
        drawSegment(p,movingNodeSegs[count]);
}

void MainWindow2::drawNode(QPainter& p,Node* node)
{
    ScreenPos pos = map.getScreenPos(node->getLon(),node->getLat());
    if(map.pt_within_map(pos))
    {
        WaypointRep* img=nodeReps[node->getType()];
        if(img) img->draw(p,pos.x,pos.y,node->getName());

        if(!selSeg[segCount] && (ptsv[0].size() || ptsv[1].size()))
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

        if(node==pts[0] || node==pts[1] || node==movingNode)
        {
            p.setPen(QPen(Qt::red,3));
            p.drawEllipse( pos.x - 16, pos.y - 16, 32, 32 );
        }
    }
}

void MainWindow2::drawTrackPoint(QPainter &p,TrackPoint *tp)
{
    ScreenPos pos = map.getScreenPos(tp->getLon(),tp->getLat());
	p.drawPixmap(pos.x,pos.y,tpPixmap);
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
                //components->deleteNode(movingNode);
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
                repaint(false);
            }
            break;
        case ACTION_DELETE_NODE:
            n = components->getNearestNode(p.y,p.x,LIMIT);
            if(n)
            {
                QString url;
                osmhttp.unlock();
                components->deleteNode(n);
                if(liveUpdate && n->getOSMID()>0)
                {
                    url.sprintf ("/api/0.3/node/%d", n->getOSMID());
                    osmhttp.setAuthentication(username, password);
                    osmhttp.sendRequest("DELETE", url);
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
                            osmhttp.sendRequest("DELETE", url);
                        }
                        */
                        delete containingSegs[count];
                }
               
                osmhttp.lock();
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
                	Segment *segx= components->addNewSegment
							(pts[0],pts[1],"",curSegType);
					nodeHandler.setEmit
							(segx,this,SLOT(doaddseg(void*)));
					if(liveUpdate && !osmhttp.isMakingRequest())
					{
						QByteArray xml = pts[1]->toOSM();
						QString url = "/api/0.3/node/0";
						osmhttp.disconnect 
								(SIGNAL(responseReceived(const QByteArray&,
														 void*)));
						osmhttp.setAuthentication(username, password);
						newUploadedNode = pts[1];
						osmhttp.scheduleCommand("PUT",url,xml,
							&nodeHandler,
							SLOT(newNodeAdded(const QByteArray&,void*)),
							newUploadedNode);
					}
				}
				else
				{
                	Segment *segx=
					    components->addNewSegment(pts[0],pts[1],"",curSegType);
					doaddseg(segx);
				}

                pts[0]=pts[1]=NULL;
                nSelectedPoints=0;

				update();
			}

				
					
            break;
		case ACTION_BREAK_SEG:
			if(selSeg[segCount])
			{
                n = components->getNearestNode(p.y,p.x,LIMIT);
                if(!n)
                    n=components->addNewNode(p.y,p.x,"","node");
				std::pair<Segment*,Segment*>* segments = 
						components->breakSegment(selSeg[segCount],n);
				QString url = "/api/0.3/segment/0";
				osmhttp.disconnect
					(SIGNAL(responseReceived(const QByteArray&,void*)));
				/*
				QObject::connect
					(&osmhttp,
					 SIGNAL(responseReceived(const QByteArray&,void*)),
						this, SLOT(newSegmentAdded(const QByteArray&,void*)));
						*/
				osmhttp.setAuthentication(username, password);

				//osmhttp.sendRequest("PUT", url, xml);
				
			
				nodeHandler.setEmit(segments,this,SLOT(addSplitSegs(void*)));

				if(n->getOSMID()<=0)
				{
					osmhttp.scheduleCommand("PUT","/api/0.3/node/0",n->toOSM(),
						&nodeHandler,
						SLOT(newNodeAdded(const QByteArray&,void*)),
						n);
				}

				url.sprintf("/api/0.3/segment/%d",selSeg[segCount]->getOSMID());
				cerr<<"DELETE: URL is: " << url << endl;
				osmhttp.scheduleCommand("DELETE",url);
		

				clearSegments();

			}
			break;

        case  ACTION_SEL_SEG:
            if(nSelectedPoints==0)
            {
                EarthPoint p=map.getEarthPoint(ScreenPos(ev->x(),ev->y()));
                ptsv[0] = components->getNearestNodes (p.y,p.x,LIMIT);
                if(ptsv[0].size())
                {
					// 280306 only push back the new segment if we're making a
					// way if the current last segment is not NULL. In this
					// way, we can re-use the current last segment if selection
					// of the last segment was unsuccessful.

					if(makingWay && segCount!=0 && 
						selSeg[segCount]!=NULL)
					{
						cerr<<"*************ADDING NEW SEG"<<endl;
						selSeg.push_back(NULL);
					}
					else
						selSeg[segCount] = NULL;

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
                    selSeg[segCount] = components->getSeg(ptsv[0],ptsv[1]);   
                    if(selSeg[segCount])
                    {
                        cerr<<"found a selected seg" << endl;
                       
                        // Naming always on when in selected segment mode
                        nameTrackOn();
                    }
               
                    ptsv[0].clear();
                    ptsv[1].clear();

                    update();
                    nSelectedPoints=0;
					
					// If we are making a way, increase segCount to allow
					// storage of multiple selected segments
					// 280306 only do this if we found a selected segment
					
					if(makingWay && selSeg[segCount])
						segCount++;
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
                url.sprintf ("/api/0.3/node/%d", nearest->getOSMID());
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
			doAddNewNode(p.y,p.x,d->getName(),d->getType());
		}

        update();
        delete d;
    }
}

void MainWindow2::mouseMoveEvent(QMouseEvent* ev)
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

void MainWindow2::mouseReleaseEvent(QMouseEvent* ev)
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
                //components->addNode(movingNode);
                if(liveUpdate && movingNode->getOSMID()>0)
                {
                    QByteArray xml = movingNode->toOSM();
                    QString url;
                    url.sprintf ("/api/0.3/node/%d", movingNode->getOSMID());
                    osmhttp.setAuthentication(username, password);
                    osmhttp.sendRequest("PUT", url, xml);
                }
                movingNode = NULL;     
                movingNodeSegs.clear();
                update();
            }
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
            selSeg[segCount]->setName(trackName);
//              UPLOAD IF IN LIVE MODE
            if(liveUpdate)
            {
                //selSeg->uploadToOSM(username,password);
                QByteArray xml = selSeg[segCount]->toOSM();
                QString url;
                url.sprintf ("/api/0.3/segment/%d", 
					selSeg[segCount]->getOSMID());
                osmhttp.setAuthentication(username, password);
                osmhttp.sendRequest("PUT", url, xml);
            }
            trackName = "";
            typingName = true;
            update();
        }
    }

    switch(ev->key())
    {
        case Qt::Key_Left  : left(); break;
        case Qt::Key_Right : right(); break;
        case Qt::Key_Up    : up(); break;
        case Qt::Key_Down  : down(); break;
    }

    // Now use CTRL for the shortcuts so it doesn't interfere with
    // entering text and you can scale up and down etc when entering text
    if(ev->state()==Qt::ControlButton)
    {
        // 11/04/05 prevent movement being too far at large scales
        double dis = 0.1/map.getScale();

        switch(ev->key())
        {
            case Qt::Key_Plus  : magnify(); break;
            case Qt::Key_Minus : shrink(); break;
                                 
            // Remember the ZX Spectrum? :-)
            case Qt::Key_5     : screenLeft(); break;
            case Qt::Key_6     : screenDown(); break;
            case Qt::Key_7     : screenUp(); break;
            case Qt::Key_8     : screenRight(); break;
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

    if(selSeg[segCount])
    {
        Node *n1 = selSeg[segCount]->firstNode(), 
			*n2 = selSeg[segCount]->secondNode();
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


void MainWindow2::newSegmentAdded(const QByteArray& array, void *segment)
{
	Segment *seg = (Segment*) segment;
	//Segment *seg=newUploadedSegment;
    QString str = array;
    QStringList ids;
    ids = QStringList::split("\n", str);
    if(seg)
    {
        cerr<<"NEW UPLOADED SEGMENT IS NOT NULL::SETTING ID"<<endl;
		cerr<<"ID IS: " << atoi(ids[0].ascii()) << endl;
        seg->setOSMID(atoi(ids[0].ascii()));
        newUploadedSegment = NULL;
        cerr<<"DONE."<<endl;
    }
    update();
}

void MainWindow2::newWayAdded(const QByteArray& array,void *way)
{
	Way *w=(Way*)way;
	//Way *w=newUploadedWay;
    QString str = array;
    QStringList ids;
    ids = QStringList::split("\n", str);
    if(w)
    {
        cerr<<"NEW UPLOADED Way IS NOT NULL::SETTING ID"<<endl;
		cerr<<"ID = "<< atoi(ids[0].ascii()) << endl;
        w->setOSMID(atoi(ids[0].ascii()));

		// TEMPORARY
		//delete newUploadedWay;

        newUploadedWay = NULL;
        cerr<<"DONE."<<endl;
    }
    update();
}

void MainWindow2::deleteSelectedSeg()
{
    if(selSeg[segCount])
    {
        cerr<<"selseg exists" << endl;
        components->deleteSegment(selSeg[segCount]);
        if(liveUpdate && selSeg[segCount]->getOSMID()>0)
        {
            QString url;
            url.sprintf ("/api/0.3/segment/%d", selSeg[segCount]->getOSMID());
            osmhttp.setAuthentication(username, password);
            osmhttp.sendRequest("DELETE", url);
        }
        delete selSeg[segCount];
        selSeg[segCount] = NULL;
        update();
    }
}

void MainWindow2::handleHttpError(int code,const QString& reasonPhrase)
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

void MainWindow2::handleNetCommError(const QString& error)
{
    QMessageBox::information(this,
                "An error occurred communicating with OSM", error);
}

void MainWindow2::showPosition()
{
        QString msg;
        msg.sprintf("Lat %lf Long %lf",
                        map.getBottomLeft().y, map.getBottomLeft().x);
        if(username!="" && password!="")
            msg+=" Logged in - live update active!";
        statusBar()->message(msg);
}

void MainWindow2::toggleWays()
{
	makingWay = !makingWay; 
	wayButton->setOn(makingWay);
	clearSegments();
}

void MainWindow2::uploadWay()
{
	cerr<<"creating way"<<endl;
	Way *way = new Way;
	cerr<<"setting segments on way"<<endl;
	way->setSegments(selSeg);


	vector<QString> segTypes;

	cerr<<"filling segTypes"<<endl;
    for(std::map<QString,QPen>::iterator i=segpens.begin(); i!=segpens.end();
        i++)
    {
        segTypes.push_back(i->first);
    }

	cerr<<"doing way dialogue"<<endl;
	WayDialogue *wd = new WayDialogue(this,segTypes);
	if(wd->exec())
	{
		way->setName(wd->getName());
		way->setType(wd->getType());
		QByteArray xml = way->toOSM();
		cerr<<"way xml is: "<<xml<<endl;
		components->addWay(way);
		clearSegments();
		if(liveUpdate)
		{
			QString url = "/api/0.3/way/0";
			osmhttp.disconnect
				(SIGNAL(responseReceived(const QByteArray&,void*)));

			newUploadedWay = way;
			osmhttp.setAuthentication(username, password);

			osmhttp.scheduleCommand("PUT",url,xml,
						this,SLOT(newWayAdded(const QByteArray&,void*)),
						newUploadedWay);
		}
	}
}

// doAddNewNode()
// 240306
// adds a new node to the components and uploads it if in live update mode.

Node *MainWindow2::doAddNewNode(double lat,double lon,const QString &name,
									const QString& type)
{
	Node *n = components->addNewNode(lat,lon,name,type);
	if(liveUpdate && !osmhttp.isMakingRequest())
	{
		//n->uploadToOSM(username,password);
		QByteArray xml = n->toOSM();
		QString url = "/api/0.3/node/0";
		osmhttp.disconnect (SIGNAL(responseReceived(const QByteArray&,void*)));
		/*
		QObject::connect (&osmhttp,
						SIGNAL(responseReceived(const QByteArray&,void*)),
                         this, SLOT(newNodeAdded(const QByteArray&,void*)));
						 */
		osmhttp.setAuthentication(username, password);
		newUploadedNode = n;
		//osmhttp.sendRequest("PUT", url, xml);
		osmhttp.scheduleCommand("PUT",url,xml,
						&nodeHandler,SLOT(newNodeAdded(const QByteArray&,void*)),
						newUploadedNode);
	}
	return n;
}


void MainWindow2::addSplitSegs(void *splitsegs)
{
		nodeHandler.discnnect();

		std::pair<Segment*,Segment*>* segments = 
				(std::pair<Segment*,Segment*>*)splitsegs;

		cerr<<"addSplitSegs()"<<endl;
		QString a = segments->first->toOSM();
		QString b = segments->second->toOSM();
		cerr<<"segments->first->toOSM()" << a << endl;
		cerr<<"segments->second->toOSM()" << b << endl;
		osmhttp.scheduleCommand("PUT","/api/0.3/segment/0",
								segments->first->toOSM(),
						this,SLOT(newSegmentAdded(const QByteArray&,void*)),
						segments->first);

		osmhttp.scheduleCommand("PUT","/api/0.3/segment/0",
								segments->second->toOSM(),
						this,SLOT(newSegmentAdded(const QByteArray&,void*)),
						segments->second);

		delete splitsegs; // the pair was originally dynamically allocated

}

void MainWindow2::doaddseg(void *sg)
{
	nodeHandler.discnnect();
	Segment *segx = (Segment*) sg;
	cerr<<"doaddseg()"<<endl;
	if(segx && liveUpdate && !osmhttp.isMakingRequest())
	{
		QByteArray xml = segx->toOSM();
		cerr<<"xml is: "<<xml<<endl;
		QString url = "/api/0.3/segment/0";
		osmhttp.disconnect
					(SIGNAL(responseReceived(const QByteArray&,void*)));
		osmhttp.setAuthentication(username, password);
		osmhttp.scheduleCommand("PUT",url,xml,
						this,SLOT(newSegmentAdded(const QByteArray&,void*)),
						segx);	
	}
	segx=NULL;
	update();
}

void MainWindow2::changeSerialPort()
{
	serialPort = QInputDialog::getText("Enter serial port",
						"Enter serial port, e.g. /dev/ttyS0 or COM1",
						QLineEdit::Normal, serialPort);
}
	
void MainWindow2::deleteWay()
{
	QMessageBox::information(this,
                        "Not implemented yet!",
                        "Not implemented yet!");
	osmhttp.disconnect
			(SIGNAL(responseReceived(const QByteArray&,void*)));
	osmhttp.setAuthentication(username, password);
}

void MainWindow2::uploadNewWaypoints()
{
	vector<Node*> newNodes = components->getNewNodes();

	QString url = "/api/0.3/node/0";
	osmhttp.disconnect (SIGNAL(responseReceived(const QByteArray&,void*)));
	osmhttp.setAuthentication(username, password);

	for(int count=0; count<newNodes.size(); count++)
	{
		if(newNodes[count]->getType()!="trackpoint" &&
			newNodes[count]->getType()!="node")
		{
			osmhttp.scheduleCommand("PUT",url,newNodes[count]->toOSM(),
								&nodeHandler,
								SLOT(newNodeAdded(const QByteArray&,void*)),
								newNodes[count]);
		}
	}
}

}
