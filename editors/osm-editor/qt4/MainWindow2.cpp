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


MainWindow2::MainWindow2(double lat,double lon, double s,double w,double h) 
{
    setWindowTitle("OpenStreetMap Editor");
    resize ( w, h );       

	widget=new MapWidget(this,lat,lon,s,w,h);
	QObject::connect(widget,SIGNAL(message(const QString&)),
							this,SLOT(showMessage(const QString&)));

    // Construct the menus.
	QMenu *fileMenu = menuBar()->addMenu("&File");

    fileMenu->addAction("&Open",widget,SLOT(open()),Qt::CTRL+Qt::Key_O);
    fileMenu->addAction("&Save",widget,SLOT(save()),Qt::CTRL+Qt::Key_S);
    fileMenu->addAction("Save &as...",widget,SLOT(saveAs()),Qt::CTRL+Qt::Key_A);
    fileMenu->addAction("Save GPX...",widget,SLOT(saveGPX()),
					Qt::CTRL+Qt::Key_X);
    fileMenu->addAction("&Read GPS",widget,SLOT(readGPS()),Qt::CTRL+Qt::Key_R);
    fileMenu->addAction("&Grab Landsat",widget,SLOT(grabLandsat()),
					Qt::CTRL+Qt::Key_G);
    fileMenu->addAction("Grab OSM from &Net",widget,SLOT(grabOSMFromNet()),
                                Qt::CTRL+Qt::Key_N);
    fileMenu->addAction("Grab OSM GPX tracks",widget,SLOT(grabGPXFromNet()));
    fileMenu->addAction("&Upload OSM",widget,SLOT(uploadOSM()),
					Qt::CTRL+Qt::Key_U);
    fileMenu->addAction("&Quit", widget, SLOT(quit()), Qt::ALT+Qt::Key_Q);
    fileMenu->addAction("Login to live update",widget,
                        SLOT(loginToLiveUpdate()));
    fileMenu->addAction("Logout from live update",widget,
                        SLOT(logoutFromLiveUpdate()));
	fileMenu->addAction("Upload waypoints",widget, SLOT(uploadNewWaypoints()));
	fileMenu->addAction("Batch upload",widget, SLOT(batchUpload()));

	QMenu *editMenu = menuBar()->addMenu("&Edit");
   
	/*
    editMenu->addAction("&Toggle nodes",this,SLOT(toggleNodes()),
                        Qt::CTRL+Qt::Key_T);
	*/
    editMenu->addAction("Toggle &Landsat",this,SLOT(toggleLandsat()),
                        Qt::CTRL+Qt::Key_L);
    editMenu->addAction("Toggle &contours",this,SLOT(toggleContours()),
                        Qt::CTRL+Qt::Key_C);
    editMenu->addAction("Toggle segment colours",this,
					SLOT(toggleSegmentColours()));
    editMenu->addAction("Remove trac&k points",widget,SLOT(removeTrackPoints()),
                        Qt::CTRL+Qt::Key_K);
	editMenu->addAction("Change serial port", widget, SLOT(changeSerialPort()));

    QToolBar* toolbar=new QToolBar(this);
	addToolBar(toolbar);

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
    QPixmap tiledOSM = mmLoadPixmap("images","tiledosm.png");

	gcedit = new QLineEdit(this);
	toolbar->addWidget(gcedit);
	QObject::connect(gcedit,SIGNAL(returnPressed()), this,SLOT(doGeocoder()));

    toolbar->addAction(left_pixmap,"Move left",widget,SLOT(left()));
    toolbar->addAction(right_pixmap,"Move right",widget,SLOT(right()));
    toolbar->addAction(up_pixmap,"Move up",widget,SLOT(up()));
    toolbar->addAction(down_pixmap,"Move down",widget,SLOT(down()));
    toolbar->addAction(magnify_pixmap,"Zoom in",widget,SLOT(magnify()));
    toolbar->addAction(shrink_pixmap,"Zoom out",widget,SLOT(shrink()));

    toolbar->addAction 
            (deleteseg,"Delete Selected Segment/Way/GPX Track",widget,
             SLOT(deleteSelectedSeg()));

    wayAction = toolbar->addAction
            (ways,"Way construction on/off",this,
             SLOT(toggleWays()));
	wayAction->setCheckable(true);
	wayAction->setChecked(false);

    toolbar->addAction
            (uploadways,"Upload current way",widget,
             SLOT(uploadWay()));
	
	
    toolbar->addAction
            (editway,"Way Details/Edit Way",widget,
             SLOT(changeWayDetails()));

			
    osmAction = toolbar->addAction
            (osm,"OSM Data On/Off",this,
             SLOT(toggleOSM()));
	osmAction->setCheckable(true);
	osmAction->setChecked(true);

    gpxAction = toolbar->addAction
            (gpx,"OSM GPX Tracks On/Off",this,
             SLOT(toggleGPX()));
	gpxAction->setCheckable(true);
	gpxAction->setChecked(true);

    landsatAction = toolbar->addAction
            (landsat,"Landsat On/Off",this,
             SLOT(toggleLandsat()));
	landsatAction->setCheckable(true);
	landsatAction->setChecked(false);

    tiledOSMAction = toolbar->addAction
            (tiledOSM,"Tiled OSM On/Off",this,
             SLOT(toggleTiledOSM()));
	tiledOSMAction->setCheckable(true);
	tiledOSMAction->setChecked(false);

    contoursAction = toolbar->addAction
            (contours,"SRTM Contours On/Off",this,
             SLOT(toggleContours()));
	contoursAction->setCheckable(true);
	contoursAction->setChecked(false);
   
    showSegmentColoursAction = toolbar->addAction
            (segcol,"Segment Type Indication On/Off",this,
             SLOT(toggleSegmentColours()));
	showSegmentColoursAction->setCheckable(true);
	showSegmentColoursAction->setChecked(false);

    QToolBar  *toolbar2 = new QToolBar(this);
	addToolBar(toolbar2);
   
    modeActions[ACTION_NODE]= new QAction
            (wp,"Edit Nodes",widget);
    modeActions[ACTION_MOVE_NODE]= new QAction
            (objectmanip,"Move Node",widget);
    modeActions[ACTION_DELETE_NODE]= new QAction
            (two,"Delete Node",widget);
    modeActions[ACTION_SEL_SEG]= new QAction
            (selseg_pixmap,"Select segment",widget);
    modeActions[ACTION_SEL_WAY]= new QAction
            (selway_pixmap,"Select way",widget);
    modeActions[ACTION_NEW_SEG]= new QAction
            (formnewseg,"New segment",widget);
    modeActions[ACTION_BREAK_SEG]= new QAction
            (breakseg,"Break segment",widget);
    modeActions[ACTION_SEL_TRACK]= new QAction
            (seltrk,"Select section of track",widget);

    // Turn the "mode" toolbar buttons into toggle buttons, and set their
    // mapping index for the signal mapper.
   
   
    for (int count=0; count<N_ACTIONS; count++)
    {
		toolbar2->addAction(modeActions[count]);
		QObject::connect(modeActions[count],SIGNAL(triggered()),
							mapper,SLOT(map()));
        mapper->setMapping(modeActions[count],count);
    }
   
    // The final stage of implementing the signal mapper: connect mapped()
    // to setMode(). The value set in setMapping() above will be used.
    QObject::connect(mapper,SIGNAL(mapped(int)),widget,SLOT(setMode(int)));


	setCentralWidget(widget);
}

void MainWindow2::toggleLandsat()
{
	widget->toggleLandsat();
	landsatAction->toggle();
}

void MainWindow2::toggleOSM()
{
	widget->toggleOSM();
	osmAction->toggle();
}

void MainWindow2::toggleGPX()
{
	widget->toggleGPX();
	gpxAction->toggle();
}

void MainWindow2::toggleContours()
{
	widget->toggleContours();
	contoursAction->toggle();
}

void MainWindow2::toggleSegmentColours()
{
	widget->toggleSegmentColours();
	showSegmentColoursAction->toggle();
}
void MainWindow2::toggleWays()
{
	widget->toggleWays();
	wayAction->toggle();
}

void MainWindow2::toggleTiledOSM()
{
	widget->toggleTiledOSM();
	tiledOSMAction->toggle();
}

void MainWindow2::showMessage(const QString& msg)
{
	statusBar()->showMessage(msg);
}

void MainWindow2::doGeocoder()
{
	cerr<<"MainWindow2::doGeocoder()" << endl;
	widget->geocoderLookup(gcedit->text(), "UK");
}


}
