#include "MainWindow.h"

#include "LayerDock.h"
#include "MapView.h"
#include "PropertiesDock.h"
#include "Command/Command.h"
#include "Interaction/CreateNodeInteraction.h"
#include "Interaction/CreateRoadInteraction.h"
#include "Interaction/CreateWayInteraction.h"
#include "Interaction/EditInteraction.h"
#include "Interaction/ZoomInteraction.h"
#include "Map/Coord.h"
#include "Map/DownloadOSM.h"
#include "Map/ImportGPX.h"
#include "Map/ImportOSM.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"
#include "Sync/SyncOSM.h"
#include "GeneratedFiles/ui_AboutDialog.h"
#include "GeneratedFiles/ui_UploadMapDialog.h"

#include <QtCore/QSettings>
#include <QtGui/QDialog>
#include <QtGui/QFileDialog>
#include <QtGui/QMessageBox>
#include <QtGui/QMouseEvent>

#define MAJORVERSION "0"
#define MINORVERSION "05"

#include "Map/TrackPoint.h"
#include "Map/Way.h"

MainWindow::MainWindow(void)
: theDocument(0)
{
	setupUi(this);
	theView = new MapView(this);
	setCentralWidget(theView);
	theDocument = new MapDocument;
	theView->setDocument(theDocument);
	theDocument->history().setActions(editUndoAction,editRedoAction);

	theLayers = new LayerDock(this);
	theLayers->setAllowedAreas(Qt::LeftDockWidgetArea | Qt::RightDockWidgetArea);
	addDockWidget(Qt::LeftDockWidgetArea, theLayers);

	theProperties = new PropertiesDock(this);
	theProperties->setAllowedAreas(Qt::LeftDockWidgetArea | Qt::RightDockWidgetArea);
	addDockWidget(Qt::RightDockWidgetArea, theProperties);
	on_editPropertiesAction_triggered();
}

MainWindow::~MainWindow(void)
{
	delete theDocument;
}

void MainWindow::invalidateView(bool UpdateDock)
{
	theView->update();
	theLayers->updateContent();
	if (UpdateDock)
		theProperties->resetValues();
}

PropertiesDock* MainWindow::properties()
{
	return theProperties;
}

MapDocument* MainWindow::document()
{
	return theDocument;
}

void MainWindow::on_editRedoAction_triggered()
{
	theDocument->history().redo();
	invalidateView();
}

void MainWindow::on_editUndoAction_triggered()
{
	theDocument->history().undo();
	invalidateView();
}

void MainWindow::on_editPropertiesAction_triggered()
{
	theProperties->setSelection(0);
	theView->launch(new EditInteraction(theView));
}

void MainWindow::on_editRemoveAction_triggered()
{
	emit remove_triggered();
}

void MainWindow::on_editMoveAction_triggered()
{
	emit move_triggered();
}

void MainWindow::on_editAddAction_triggered()
{
	emit add_triggered();
}

void MainWindow::on_editReverseAction_triggered()
{
	emit reverse_triggered();
}

void MainWindow::on_fileImportAction_triggered()
{
	QString s = QFileDialog::getOpenFileName(
		this,
		tr("Open track file"),
		"",
		tr("GPS Exchange format (*.gpx)\nOpenStreetMap format (*.osm)"));
	if (!s.isNull())
	{
		bool OK = false;
		MapLayer* NewLayer = new MapLayer(tr("Import %1").arg(s.right(s.length()-s.lastIndexOf('/')-1)));
		if (s.right(4).toLower() == ".gpx")
			OK = importGPX(this, s, theDocument, NewLayer);
		if (s.right(4).toLower() == ".osm")
			OK = importOSM(this, s, theDocument, NewLayer);
		if (OK)
		{
			on_viewZoomAllAction_triggered();
			on_editPropertiesAction_triggered();
			theDocument->history().setActions(editUndoAction,editRedoAction);
		}
		else
		{
			delete NewLayer;
			QMessageBox::warning(this,tr("Not a valid file"),tr("The file could not be opened"));
		}
	}
}

void MainWindow::on_fileOpenAction_triggered()
{
	QString s = QFileDialog::getOpenFileName(
		this,
		tr("Open track file"),
		"",
		tr("GPS Exchange format (*.gpx)\nOpenStreetMap format (*.osm)"));
	if (!s.isNull())
	{
		MapDocument* NewDoc = new MapDocument;
		MapLayer* NewLayer = new MapLayer(tr("Open %1").arg(s.right(s.length()-s.lastIndexOf('/')-1)));
		bool OK = false;
		if (s.right(4).toLower() == ".gpx")
			OK = importGPX(this, s, NewDoc, NewLayer);
		if (s.right(4).toLower() == ".osm")
			OK = importOSM(this, s, NewDoc, NewLayer);
		if (OK)
		{
			theProperties->setSelection(0);
			delete theDocument;
			theDocument = NewDoc;
			theView->setDocument(theDocument);
			on_viewZoomAllAction_triggered();
			on_editPropertiesAction_triggered();
			theDocument->history().setActions(editUndoAction,editRedoAction);
		}
		else
		{
			delete NewDoc;
			delete NewLayer;
			QMessageBox::warning(this,tr("Not a valid file"),tr("The file could not be opened"));
		}
	}
}

void MainWindow::on_fileUploadAction_triggered()
{
	on_editPropertiesAction_triggered();
	QDialog * dlg = new QDialog(this);
	QSettings Sets;
	Sets.beginGroup("downloadosm");
	Ui::UploadMapDialog ui;
	ui.setupUi(dlg);
	ui.Website->setText("www.openstreetmap.org");
	ui.Username->setText(Sets.value("user").toString());
	ui.Password->setText(Sets.value("password").toString());
	if (dlg->exec() == QDialog::Accepted)
	{
		Sets.setValue("user",ui.Username->text());
		Sets.setValue("password",ui.Password->text());
		syncOSM(this,ui.Website->text(),ui.Username->text(),ui.Password->text());
	}
	delete dlg;

}

void MainWindow::on_fileDownloadAction_triggered()
{
	if ( downloadOSM(this,theView->projection().viewport(),theDocument))
	{
		on_viewZoomAllAction_triggered();
		on_editPropertiesAction_triggered();
	}
	else
		QMessageBox::warning(this,tr("Error downloading"),tr("The map could not be downloaded"));
}

void MainWindow::on_helpAboutAction_triggered()
{
	QDialog dlg(this);
	Ui::AboutDialog About;
	About.setupUi(&dlg);
	About.Version->setText(About.Version->text().arg(MAJORVERSION).arg(MINORVERSION));
	dlg.exec();
}

void MainWindow::on_viewZoomAllAction_triggered()
{
	std::pair<bool, CoordBox> BBox(boundingBox(theDocument));
	if (BBox.first)
	{
		theView->projection().setViewport(BBox.second,theView->rect());
		invalidateView();
	}
}

void MainWindow::on_viewZoomInAction_triggered()
{
	theView->projection().zoom(1.33333, theView->rect());
	invalidateView();
}

void MainWindow::on_viewZoomOutAction_triggered()
{
	theView->projection().zoom(0.75, theView->rect());
	invalidateView();
}

void MainWindow::on_viewZoomWindowAction_triggered()
{
	theView->launch(new ZoomInteraction(theView));
}
void MainWindow::on_createWayAction_triggered()
{
	theView->launch(new CreateWayInteraction(this, theView,true));
}

void MainWindow::on_createLinearWayAction_triggered()
{
	theView->launch(new CreateWayInteraction(this, theView,false));
}

void MainWindow::on_createRoadAction_triggered()
{
	theView->launch(new CreateRoadInteraction(theView));
}

void MainWindow::on_createNodeAction_triggered()
{
	theView->launch(new CreateNodeInteraction(theView));
}

MapLayer* MainWindow::activeLayer()
{
	return theLayers->activeLayer();
}


