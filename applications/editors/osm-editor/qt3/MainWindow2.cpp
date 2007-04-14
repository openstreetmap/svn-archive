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
	displayOSM = true;
	displayGPX = true;
	showSegmentColours = false;

    actionMode = ACTION_NODE;
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
    segpens["minor road"]= SegPen (QPen(QColor(192,192,192), 2),true);
    segpens["residential road"]= SegPen(QPen (QColor(192,192,192), 1), true);
    segpens["B road"]= SegPen(QPen (QColor(253,191,111), 4), true);
    segpens["A road"]= SegPen(QPen (QColor(251,128,95), 4), true);
    segpens["motorway"]= SegPen(QPen (QColor(128,155,192), 4), true);
    segpens["railway"]= SegPen(QPen (Qt::black, 2), false);
    segpens["permissive footpath"]= SegPen(QPen (QColor(0,192,0), 2), false);
    segpens["permissive bridleway"]= SegPen(QPen (QColor(170,85,0), 2), false);
    segpens["track"]= SegPen(QPen (QColor(128,128,128), 3), false);
    segpens["new forest track"]=SegPen(QPen(QColor(170,85,0),2), false);
    segpens["new forest cycle path"]= SegPen(QPen (Qt::magenta, 2), false);
    cerr<<"done segpens" << endl;

    areapens["wood"]= QPen (QColor(192,255,192));
    areapens["heath"]= QPen (QColor(255,255,192));
    areapens["lake"]= QPen (QColor(0,0,128));

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
    fileMenu->insertItem("Grab OSM GPX tracks",this,SLOT(grabGPXFromNet()));
    fileMenu->insertItem("&Upload OSM",this,SLOT(uploadOSM()),CTRL+Key_U);
    fileMenu->insertItem("&Quit", this, SLOT(quit()), ALT+Key_Q);
    fileMenu->insertItem("Login to live update",this,
                        SLOT(loginToLiveUpdate()));
    fileMenu->insertItem("Logout from live update",this,
                        SLOT(logoutFromLiveUpdate()));
	fileMenu->insertItem("Upload waypoints",this, SLOT(uploadNewWaypoints()));
	fileMenu->insertItem("Batch upload",this, SLOT(batchUpload()));
    menuBar()->insertItem("&File",fileMenu);
    QPopupMenu* editMenu = new QPopupMenu(this);
   
    editMenu->insertItem("&Toggle nodes",this,SLOT(toggleNodes()),
                        CTRL+Key_T);
    editMenu->insertItem("Toggle &Landsat",this,SLOT(toggleLandsat()),
                        CTRL+Key_L);
    editMenu->insertItem("Toggle &contours",this,SLOT(toggleContours()),
                        CTRL+Key_C);
    editMenu->insertItem("Toggle segment colours",this,
					SLOT(toggleSegmentColours()));
    editMenu->insertItem("Remove trac&k points",this,SLOT(removeTrackPoints()),
                        CTRL+Key_K);
	editMenu->insertItem("Change serial port", this, SLOT(changeSerialPort()));
//      editMenu->insertItem("Undo",this,SLOT(undo()),CTRL+Key_Z);
    menuBar()->insertItem("&Edit",editMenu);

    QToolBar* toolbar=new QToolBar(this);
    toolbar->setHorizontalStretchable(true);

	/* 050706 remove - now all way attribute editing via dialog box
    new QLabel("Way type: ", toolbar);
    QComboBox* r = new QComboBox(toolbar);
    for(std::map<QString,SegPen>::iterator i=segpens.begin(); i!=segpens.end();
        i++)
    {
        r->insertItem(i->first);
    }
	r->setCurrentText("track");

    QObject::connect(r,SIGNAL(activated(const QString&)),
                        this,SLOT(setSegType(const QString&)));
						*/

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
    QPixmap seltrk = mmLoadPixmap("images","seltrk.png");
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
    QPixmap selway_pixmap = mmLoadPixmap("images","selway.png");
    QPixmap osm = mmLoadPixmap("images","osm.png");
    QPixmap gpx = mmLoadPixmap("images","gpx.png");
    QPixmap landsat = mmLoadPixmap("images","landsat.png");
    QPixmap contours = mmLoadPixmap("images","contours.png");
    QPixmap segcol = mmLoadPixmap("images","segcol.png");
    QPixmap editway = mmLoadPixmap("images","editway.png");

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
    modeButtons[ACTION_SEL_WAY]= new QToolButton
            (selway_pixmap,"Select way","",mapper,SLOT(map()),toolbar2);
    modeButtons[ACTION_NEW_SEG]= new QToolButton
            (formnewseg,"New segment","",mapper,SLOT(map()),toolbar2);
    modeButtons[ACTION_BREAK_SEG]= new QToolButton
            (breakseg,"Break segment","",mapper,SLOT(map()),toolbar2);
    modeButtons[ACTION_SEL_TRACK]= new QToolButton
            (seltrk,"Select section of track","",mapper,SLOT(map()),toolbar2);
    new QToolButton
            (deleteseg,"Delete Selected Segment/Way/GPX Track","",this,
             SLOT(deleteSelectedSeg()),toolbar);
    wayButton = new QToolButton
            (ways,"Way construction on/off","",this,
             SLOT(toggleWays()),toolbar);
	wayButton->setToggleButton(true);
	wayButton->setOn(false);

    new QToolButton
            (uploadways,"Upload current way","",this,
             SLOT(uploadWay()),toolbar);
	
	
    new QToolButton
            (editway,"Way Details/Edit Way","",this,
             SLOT(changeWayDetails()),toolbar);

			
    osmButton = new QToolButton
            (osm,"OSM Data On/Off","",this,
             SLOT(toggleOSM()),toolbar);
	osmButton->setToggleButton(true);
	osmButton->setOn(true);

    gpxButton = new QToolButton
            (gpx,"OSM GPX Tracks On/Off","",this,
             SLOT(toggleGPX()),toolbar);
	gpxButton->setToggleButton(true);
	gpxButton->setOn(true);

    landsatButton = new QToolButton
            (landsat,"Landsat On/Off","",this,
             SLOT(toggleLandsat()),toolbar);
	landsatButton->setToggleButton(true);
	landsatButton->setOn(false);

    contoursButton = new QToolButton
            (contours,"SRTM Contours On/Off","",this,
             SLOT(toggleContours()),toolbar);
	contoursButton->setToggleButton(true);
	contoursButton->setOn(false);
   
    showSegmentColoursButton = new QToolButton
            (segcol,"Segment Type Indication On/Off","",this,
             SLOT(toggleSegmentColours()),toolbar);
	showSegmentColoursButton->setToggleButton(true);
	showSegmentColoursButton->setOn(false);

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
    nodeReps["point of interest"] = new WaypointRep
            ("images/interest.png");
    nodeReps["suburb"] = new WaypointRep(
            "images/place.png","Helvetica",16, Qt::black);
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
    osmtracks = new Components2;

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

	selWay = NULL;
	splitter = NULL;

	uploader = NULL;
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

		osmhttp.setAuthentication(uname,pwd);
		osmhttp.scheduleCommand("GET",url,QByteArray(),
								this,
								SLOT(loadComponents(const QByteArray&,void*)));
    }
}

void MainWindow2::grabGPXFromNet()
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
        statusBar()->message("Grabbing tracks from OSM...");
        EarthPoint bottomLeft = map.getBottomLeft(),
               topRight = map.getTopRight();
        QString url;
        url.sprintf("/api/0.3/trackpoints?bbox=%lf,%lf,%lf,%lf&page=0",
                            bottomLeft.x,bottomLeft.y,
                            topRight.x,topRight.y);
        cerr<<"SENDING URL: "<<url<<endl;

		osmhttp.setAuthentication(uname,pwd);
		osmhttp.scheduleCommand("GET",url,QByteArray(),
								this,
								SLOT(loadOSMTracks(const QByteArray&,void*)));
    }
}

void MainWindow2::loadComponents(const QByteArray& array,void*)
{
	//QMessageBox::information(this,"Received data", "Data has been received.");
	cerr << "loadComponents()" << endl;
	cerr << "array:" << array << endl;

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
	{
		cerr<<"setMode(): CLEARING SEGMENTS" << endl;
		clearSegments();
	}	

    movingNode = NULL;
    pts[0]=pts[1]=NULL;
    doingName = false;
    update();
}

/* 050706 all this goes (all done via dialog; also can't change a segment
 * type anymore. YOU *WILL* USE WAYS, WHETHER YOU LIKE IT OR NOT!!!! AND 
 * THAT'S AN *ORDER* !!!! 
void MainWindow2::setSegType(const QString &t)
{
    // live change of selected segment
    curSegType =   t;

	// Now set the type of the way if a way is selected, rather than a segment
	if(selWay)
		selWay->setType(curSegType);
	else
	{
		for(int count=0; count<selSeg.size(); count++)
			selSeg[count]->setType(curSegType);
	}

        // UPLOAD IF IN LIVE MODE
        if(liveUpdate)
        {
			if(selWay)
			{
            	QByteArray xml = selWay->toOSM();
            	QString url;
            	url.sprintf ("/api/0.3/way/%d", selWay->getOSMID());
            	osmhttp.setAuthentication(username, password);
				osmhttp.scheduleCommand("PUT",url,xml);
			}
			else if(!makingWay)
			{
				for(int ct=0; ct<selSeg.size(); ct++)
				{
            		QByteArray xml = selSeg[ct]->toOSM();
            		QString url;
            		url.sprintf("/api/0.3/segment/%d",selSeg[ct]->getOSMID());
            		osmhttp.setAuthentication(username, password);
					osmhttp.scheduleCommand("PUT",url,xml);
				}
			}
        }
    update();
}
*/

void MainWindow2::toggleNodes()
{
    trackpoints = !trackpoints;
    update();
}

void MainWindow2::toggleLandsat()
{
	landsatButton->setOn(landsatManager.toggleDisplay());
    update();
}

void MainWindow2::toggleOSM()
{
	displayOSM = !displayOSM;
	osmButton->setOn(displayOSM);
    update();
}

void MainWindow2::toggleGPX()
{
	displayGPX = !displayGPX;
	gpxButton->setOn(displayGPX);
    update();
}

void MainWindow2::toggleContours()
{
    contours = !contours;
	contoursButton->setOn(contours);
    update();
}

void MainWindow2::toggleSegmentColours()
{
   	showSegmentColours = !showSegmentColours; 
	showSegmentColoursButton->setOn(showSegmentColours);
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
            /*
        bitBlt(this,0,0, &savedPixmap,0,0,savedPixmap.width(),
                        savedPixmap.height(), Qt::CopyROP);
                        */
        drawMoving(p);
    }
}

void MainWindow2::drawLandsat(QPainter& p)
{
    landsatManager.drawTilesNew(p);
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

void MainWindow2::drawAreas(QPainter& p)
{
	if(displayOSM)
	{
		for(int count=0; count<components->nAreas(); count++)
			drawArea(p,components->getArea(count));
	}
}

// draw an area
// WARNING! All segments must be orientated in the same direction for this
// to work!!!
void MainWindow2::drawArea(QPainter& p, Area *area)
{
	cerr << "area type: " << area->getType() << endl;
	if(areapens.find(area->getType()) != areapens.end())
	{
		cerr << "that type exists!" << endl;
		QPen pen = areapens[area->getType()];
		p.setPen(pen);
		p.setBrush(QBrush(pen.color(),Qt::SolidPattern));

		QPointArray pointarray(area->nSegments()+1);
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
			pointarray.setPoint(added++,pos.x,pos.y);
		}

		while(count<area->nSegments())
		{
			if(area->getSegment(count))
			{
				pos = map.getScreenPos
				(area->getSegment(count)->secondNode()->getLon(),
				 area->getSegment(count)->secondNode()->getLat());
				pointarray.setPoint(added++,pos.x,pos.y);
			}
			count++;
		}

		p.drawPolygon(pointarray,false,0,added);
		p.setBrush(Qt::NoBrush);
	}
}

void MainWindow2::drawSegments(QPainter& p)
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

void MainWindow2::drawSegment(QPainter& p, Segment *curSeg)
{
        ScreenPos pt1, pt2;
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
                        QPen(Qt::yellow,5) : QPen(QColor(128,128,128),1) );

        curPen.setStyle ((curSeg->getOSMID()>0) ?  Qt::SolidLine: Qt::DotLine );
		
        if(curSeg->hasNodes())
        {
            pt1=map.getScreenPos(curSeg->firstNode()->getLon(),
                                curSeg->firstNode()->getLat());
            pt2=map.getScreenPos(curSeg->secondNode()->getLon(),
                                curSeg->secondNode()->getLat());
            if(map.pt_within_map(pt1) || map.pt_within_map(pt2))
            {
				// Draw segments belonging to ways (only) in the correct colour
				if(curSeg->belongsToWay() && !found && !foundWay)
				{

					Way *w=components->getWayByID(curSeg->getWayID());
					//curPen = segpens[curSeg->getType()].pen;
					if(segpens.find(w->getType()) != segpens.end())
					{
						curPen = segpens[w->getType()].pen;

						if(segpens[w->getType()].casing)
						{
							p.setPen(QPen(Qt::black,curPen.width()+2));
                			p.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);
						/*
            			p.drawEllipse( pt1.x - 5, pt1.y - 5, 10, 10 );
            			p.drawEllipse( pt2.x - 5, pt2.y - 5, 10, 10 );
						*/

						//cerr<<"segment belongs to a way"<<endl;
						//curPen.setWidth(4);
						}
					}

					// If the segment is the longest segment in a way, draw its
					// name
					if(w->getName()!="")
					{
						dy=pt2.y-pt1.y;
						dx=pt2.x-pt1.x;
						if(w && fm.width(w->getName()) <=fabs(dx) &&
									curSeg==w->longestSegment())
						{
                    		double angle = atan2(dy,dx);
                    		doDrawAngleText(&p,pt1.x,pt1.y,pt1.x,pt1.y,
                                angle,w->getName().ascii());
						}
                	}
				}
				// If the user has selected to display unwayed segments in
				// colour, find the appropriate colour.
				else if (showSegmentColours && !found && !foundWay) 
				{
					cerr << "FOUND A SEG TO COLOUR*** " ;
					cerr << "Type: "<< curSeg->getType() << endl;
					if(segpens.find(curSeg->getType()) != segpens.end())
					{
						curPen = segpens[curSeg->getType()].pen;
						curPen.setWidth(1); // unwayed segments always 1
					}
				}

				int s = (curSeg->belongsToWay()) ? 4:8;
        		p.setPen(curPen);
                p.drawLine(pt1.x,pt1.y,pt2.x,pt2.y);
				p.setBrush(Qt::SolidPattern);
        		//p.setPen(Qt::black);
				p.fillRect( pt1.x-s/2, pt1.y-s/2, s, s, QColor(128,128,128) );
				p.fillRect( pt2.x-s/2, pt2.y-s/2, s, s, QColor(128,128,128) );
				//p.drawEllipse( pt2.x - 4, pt2.y - 4, 8, 8 );
            }
        }
}

void MainWindow2::drawGPX(QPainter& p)
{
	if(displayGPX)
	{
		drawTrackPoints(p,osmtracks,QColor(255,192,128),false);
	}
}

void MainWindow2::drawNodes(QPainter& p)
{
	if(displayOSM)
	{
		for(int count=0; count<components->nNodes(); count++)
    	{
        	drawNode(p,components->getNode(count));
    	}
	}
}

void MainWindow2::drawTrackPoints(QPainter& p,Components2 *comp,QColor colour,
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
			//p.drawPixmap(currentPos.x,currentPos.y,tpPixmap);
			p.setFont(QFont("Helvetica",8));
			p.drawText(currentPos.x+3,currentPos.y+3,idAsText);
		}

		prev = current;
		prevPos = currentPos;
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

void MainWindow2::drawTrackPoint(QPainter &p,TrackPoint *tp)
{
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
		case ACTION_BREAK_SEG:
			if(selSeg.size()==1 && splitter==NULL)
			{
				splitter = new SegSplitter;
				splitter->setComponents(components);
				splitter->setHTTPHandler(&osmhttp);
				splitter->splitSeg(selSeg[0],p,LIMIT);
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
			
			/*
			if(makingWay && segCount!=0 && selSeg[segCount]!=NULL)
				selSeg.push_back(NULL);
			*/

			s1= components->getNearestSegment(p.y,p.x,LIMIT);
			if(s1)
			{
				cerr<<"getNearestSegment: ID=" << s1->getOSMID() << endl;
					
				// If we're in way select mode, select the whole
				// parent way of the segment
				if(actionMode==ACTION_SEL_WAY)
				{
					cerr<<"mode=ACTION_SEL_WAY" << endl;
					clearSegments();
					int wayID = s1->getWayID();
					if(wayID)
					{
						cerr<<"wayID=" << wayID << endl;
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
					if(makingWay)
					{
						for(int ct=0; ct<selSeg.size(); ct++)
							if(selSeg[ct])cerr<< selSeg[ct]->getOSMID() << " ";
						cerr<<endl;
					}
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
        d = new WaypointDialogue
                    (this,nodeReps,"Edit node",
                    nearest->getType(),nearest->getName());
        if(d->exec())
        {
            nearest->setName(d->getName());
            nearest->setType(d->getType());
            if(liveUpdate && nearest->getOSMID()>0)
            {
                //nearest->uploadToOSM(username,password);
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
                    osmhttp.scheduleCommand("PUT", url, xml);
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
        else if (ev->key()==Key_Return && selSeg.size()==1)
        {
            selSeg[0]->setName(trackName);
//              UPLOAD IF IN LIVE MODE
            if(liveUpdate)
            {
                //selSeg->uploadToOSM(username,password);
                QByteArray xml = selSeg[0]->toOSM();
                QString url;
                url.sprintf ("/api/0.3/segment/%d", 
					selSeg[0]->getOSMID());
                osmhttp.setAuthentication(username, password);
                osmhttp.scheduleCommand("PUT", url, xml);
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
	landsatManager.clearTiles();
    landsatManager.grabAll();
    showPosition();
    update();
    LIMIT=map.earthDist(10);
}

void MainWindow2::shrink()
{
    map.rescale(0.5);
	landsatManager.clearTiles();
    landsatManager.grabAll();
    showPosition();
    update();
    LIMIT=map.earthDist(10);
}

void MainWindow2::updateWithLandsatCheck()
{
		/*
    if(landsatManager.needMoreData())
        landsatManager.forceGrab();
    showPosition();
    update();
	*/
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
        cerr<<"NEW UPLOADED Way/Area IS NOT NULL::SETTING ID"<<endl;
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
	cerr<<"toggleWays(): CLEARING SEGMENTS"<<endl;
	clearSegments();
}

// uploadWay()
// also uploads areas

void MainWindow2::uploadWay()
{
	Way *way = new Way(components);
	way->setSegments(selSeg);
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
	if(wd->exec())
	{
		way->setName(wd->getName());
		way->setType(wd->getType());
		way->setRef(wd->getRef()); // areas shouldn't have refs really
		way->setArea(wd->isArea());
		QByteArray xml = way->toOSM();
		cerr<<"way xml is: "<<xml<<endl;
		if(wd->isArea())
			components->addArea((Area*)way);
		else
			components->addWay(way);
		cerr<<"uploadWay(): CLEARING SEGMENTS"<<endl;
		clearSegments();
		if(liveUpdate)
		{
			QString url = wd->isArea() ? "/api/0.3/area/0" :
										"/api/0.3/way/0";
			//url.sprintf("/api/0.3/%s/0", type.ascii());

			newUploadedWay = way;
			osmhttp.setAuthentication(username, password);

			osmhttp.scheduleCommand("PUT",url,xml,
						this,SLOT(newWayAdded(const QByteArray&,void*)),
						newUploadedWay);
		}
	}
}

void MainWindow2::changeWayDetails()
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

	if(selWay && !selWay->isArea())
	{
		WayDialogue *wd = new WayDialogue(this,segTypes,areaTypes,
								selWay->getName(), selWay->getType(),
								selWay->getRef());
		if(wd->exec() && !wd->isArea())
		{
			selWay->setName(wd->getName());
			selWay->setType(wd->getType());
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

Node *MainWindow2::doAddNewNode(double lat,double lon,const QString &name,
									const QString& type)
{
	cerr<<"doAddNewNode: name=" << name << " type=" << type<<endl;
	Node *n = components->addNewNode(lat,lon,name,type);
	if(liveUpdate)
	{
		//n->uploadToOSM(username,password);
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

void MainWindow2::splitterDone()
{
	if(splitter)
	{
		delete splitter;
		splitter = NULL;
		update();
	}
}

void MainWindow2::doaddseg(void *sg)
{
	nodeHandler.discnnect();
	Segment *segx = (Segment*) sg;
	cerr<<"doaddseg()"<<endl;
	if(segx && liveUpdate && segx->firstNode()->getOSMID()>0 &&
					segx->secondNode()->getOSMID()>0)
	{
		QByteArray xml = segx->toOSM();
		cerr<<"xml is: "<<xml<<endl;
		QString url = "/api/0.3/segment/0";
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
	
void MainWindow2::uploadNewWaypoints()
{
	vector<Node*> newNodes = components->getNewNodes();

	QString url = "/api/0.3/node/0";
	osmhttp.setAuthentication(username, password);

	for(int count=0; count<newNodes.size(); count++)
	{
		if(newNodes[count]->getType()!="trackpoint" &&
			newNodes[count]->getType()!="node" &&
			// Stop people uploading those ****** Garmin waypoints !!!!
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

void MainWindow2::loadOSMTracks(const QByteArray& array,void*)
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
void MainWindow2::batchUpload()
{
	// Only upload if selected GPX track...
	if(uploader==NULL && liveUpdate && tpts[0]>=0 && tpts[1]>=0)
	{
		uploader = new BatchUploader;
		QObject::connect(uploader,SIGNAL(done()),this,SLOT(batchUploadDone()));
		QObject::connect(uploader,SIGNAL(error(const QString&)),this,
						SLOT(batchUploadError(const QString&)));
		uploader->setComponents(components);
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

void MainWindow2::batchUploadDone()
{
	if(uploader)
	{
		delete uploader;
		uploader = NULL;
		update();
	}
}

void MainWindow2::batchUploadError(const QString& error)
{
	QMessageBox::warning(this,"Error with batch upload",error);
	batchUploadDone();
}

void MainWindow2::segSplitterError(const QString& error)
{
	QMessageBox::warning(this,"Error with segment splitting",error);
	splitterDone();
}

}
