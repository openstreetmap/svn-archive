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
#include "SRTMGeneral.h"
#include "HTTPHandler.h"
#include "NodeHandler.h"
#include "Way.h"
#include "SegSplitter.h"
#include "BatchUploader.h"
#include "MapWidget.h"
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

#include <qhttp.h>
//Added by qt3to4:
#include <QPaintEvent>
#include <QResizeEvent>
#include <QMouseEvent>
#include <QKeyEvent>

#include <QAction>

using std::vector;

namespace OpenStreetMap
{

class MainWindow2 : public QMainWindow
{

Q_OBJECT

private:

	MapWidget *widget;

	QAction* modeActions[N_ACTIONS]; 
	QAction *wayAction, *landsatAction, *osmAction, *gpxAction,
						*contoursAction, *showSegmentColoursAction,
						*tiledOSMAction;

	QLineEdit *gcedit;
	QComboBox *country;

	std::map<QString,QString> countryCodes;

public:
	MainWindow2 (double=51.0,double=-1.0,double=4000,
			 		double=640,double=480);

public slots:
	void toggleLandsat();
	void toggleTiledOSM();
	void toggleContours();
	void toggleOSM();
	void toggleGPX();
	void toggleWays();
	void toggleSegmentColours();
	void showMessage(const QString&);
	void doGeocoder();
};

}
#endif // MAINWINDOW_H
