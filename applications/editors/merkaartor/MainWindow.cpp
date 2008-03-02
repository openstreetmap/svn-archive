#include "MainWindow.h"

#include "LayerDock.h"
#include "MapView.h"
#include "PropertiesDock.h"
#include "Command/Command.h"
#include "Command/DocumentCommands.h"
#include "Interaction/CreateAreaInteraction.h"
#include "Interaction/CreateDoubleWayInteraction.h"
#include "Interaction/CreateNodeInteraction.h"
#include "Interaction/CreateRoundaboutInteraction.h"
#include "Interaction/CreateSingleWayInteraction.h"
#include "Interaction/EditInteraction.h"
#include "Interaction/ZoomInteraction.h"
#include "Map/Coord.h"
#include "Map/DownloadOSM.h"
#include "Map/ImportGPX.h"
#include "Map/ImportNGT.h"
#include "Map/ImportOSM.h"
#include "Map/MapDocument.h"
#include "Map/MapLayer.h"
#include "Map/MapFeature.h"
#include "Map/Relation.h"
#include "Map/Road.h"
#include "Map/RoadManipulations.h"
#include "Map/TrackPoint.h"
#include "PaintStyle/EditPaintStyle.h"
#include "PaintStyle/PaintStyleEditor.h"
#include "Sync/SyncOSM.h"
#include "GeneratedFiles/ui_AboutDialog.h"
#include "GeneratedFiles/ui_UploadMapDialog.h"
#include "GeneratedFiles/ui_SetPositionDialog.h"
#include "GeneratedFiles/ui_SelectionDialog.h"
#include "Preferences/PreferencesDialog.h"
#include "Preferences/MerkaartorPreferences.h"
#include "Utils/SelectionDialog.h"
#include "QMapControl/imagemanager.h"
#include "QMapControl/mapadapter.h"
#include "QMapControl/wmsmapadapter.h"

#include <QtCore/QDir>
#include <QtCore/QFileInfo>
#include <QtCore/QTimer>
#include <QtGui/QDialog>
#include <QtGui/QFileDialog>
#include <QtGui/QMessageBox>
#include <QtGui/QMouseEvent>

#define MAJORVERSION "0"
#define MINORVERSION "09"


MainWindow::MainWindow(void)
		: theDocument(0)
{
	setupUi(this);

	QStringList Servers = MerkaartorPreferences::instance()->getWmsServers();
	if (Servers.size() == 0) {
		Servers.append("Demis");
		Servers.append("www2.demis.nl");
		Servers.append("/wms/wms.asp?wms=WorldMap&");
		Servers.append("Countries,Borders,Highways,Roads,Cities");
		Servers.append("EPSG:4326");
		Servers.append(",");
		MerkaartorPreferences::instance()->setWmsServers(Servers);
		MerkaartorPreferences::instance()->setSelectedWmsServer(0);
	}

	theView = new MapView(this);
	setCentralWidget(theView);
	theDocument = new MapDocument;
	theView->setDocument(theDocument);
	theDocument->history().setActions(editUndoAction, editRedoAction);

	theLayers = new LayerDock(this);
	theLayers->setAllowedAreas(Qt::LeftDockWidgetArea | Qt::RightDockWidgetArea);
	addDockWidget(Qt::LeftDockWidgetArea, theLayers);

	theProperties = new PropertiesDock(this);
	theProperties->setAllowedAreas(Qt::LeftDockWidgetArea | Qt::RightDockWidgetArea);
	addDockWidget(Qt::RightDockWidgetArea, theProperties);
	on_editPropertiesAction_triggered();
	QDir::setCurrent(MerkaartorPreferences::instance()->getWorkingDir());

	connect (theLayers, SIGNAL(layersChanged(bool)), this, SLOT(adjustLayers(bool)));
}

MainWindow::~MainWindow(void)
{
	MerkaartorPreferences::instance()->setWorkingDir(QDir::currentPath());
	delete MerkaartorPreferences::instance();
	delete theDocument;
}

void MainWindow::adjustLayers(bool adjustViewport)
{
	if (adjustViewport)
		view()->projection().setViewport(view()->projection().viewport(), view()->rect());
	invalidateView(true);
}

void MainWindow::invalidateView(bool UpdateDock)
{
	theView->invalidate();
	//theLayers->updateContent();
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
	invalidateView();
	//TODO: Fix memleak
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

static void changeCurrentDirToFile(const QString& s)
{
	QFileInfo info(s);
	QDir::setCurrent(info.absolutePath());
}


#define FILTER_LOAD_SUPPORTED \
	"Supported formats (*.gpx *.osm *.ngt)\n" \
	"GPS Exchange format (*.gpx)\n" \
	"OpenStreetMap format (*.osm)\n" \
	"Noni GPSPlot format (*.ngt)\n" \
	"All Files (*)"

void MainWindow::on_fileImportAction_triggered()
{
	QString s = QFileDialog::getOpenFileName(
					this,
					tr("Open track file"),
					"", tr(FILTER_LOAD_SUPPORTED));
	if (!s.isNull()) {
		changeCurrentDirToFile(s);
		bool OK = false;
		DrawingMapLayer* NewLayer = new DrawingMapLayer(tr("Import %1").arg(s.right(s.length() - s.lastIndexOf('/') - 1)));
		if (s.right(4).toLower() == ".gpx") {
			OK = importGPX(this, s, theDocument, NewLayer);
			if (OK)
				theDocument->add(NewLayer);
		} else
			if (s.right(4).toLower() == ".osm") {
				view()->setUpdatesEnabled(false);
				OK = importOSM(this, s, theDocument, NewLayer);
				view()->setUpdatesEnabled(true);
			} else
				if (s.right(4).toLower() == ".ngt") {
					view()->setUpdatesEnabled(false);
					OK = importNGT(this, s, theDocument, NewLayer);
					view()->setUpdatesEnabled(true);
				}
		if (OK) {
			on_viewZoomAllAction_triggered();
			on_editPropertiesAction_triggered();
			theDocument->history().setActions(editUndoAction, editRedoAction);
		} else {
			delete NewLayer;
			QMessageBox::warning(this, tr("Not a valid file"), tr("The file could not be opened"));
		}
	}
}

static bool mayDiscardUnsavedChanges(QWidget* aWidget)
{
	return QMessageBox::question(aWidget, MainWindow::tr("Unsaved changes"),
								 MainWindow::tr("The current map contains unsaved changes that will be lost when starting a new one.\n"
												"Do you want to cancel starting a new map or continue and discard the old changes?"),
								 QMessageBox::Discard | QMessageBox::Cancel, QMessageBox::Cancel) == QMessageBox::Discard;
}

void MainWindow::on_fileOpenAction_triggered()
{
	if (hasUnsavedChanges(*theDocument) && !mayDiscardUnsavedChanges(this))
		return;
	QString s = QFileDialog::getOpenFileName(
					this,
					tr("Open track file"),
					"", tr(FILTER_LOAD_SUPPORTED));
	if (!s.isNull()) {
		changeCurrentDirToFile(s);
		MapDocument* NewDoc = new MapDocument;
		DrawingMapLayer* NewLayer = new DrawingMapLayer(tr("Open %1").arg(s.right(s.length() - s.lastIndexOf('/') - 1)));
		bool OK = false;
		if (s.right(4).toLower() == ".gpx") {
			OK = importGPX(this, s, NewDoc, NewLayer);
			if (OK) {
				NewDoc->add(NewLayer);
			}
		} else
			if (s.right(4).toLower() == ".osm") {
				OK = importOSM(this, s, NewDoc, NewLayer);
			} else
				if (s.right(4).toLower() == ".ngt") {
					OK = importNGT(this, s, NewDoc, NewLayer);
					if (OK) {
						NewDoc->add(NewLayer);
					}
				}
		if (OK) {
			theProperties->setSelection(0);
			delete theDocument;
			theDocument = NewDoc;
			theView->setDocument(theDocument);
			theLayers->updateContent();
			on_viewZoomAllAction_triggered();
			on_editPropertiesAction_triggered();
			theDocument->history().setActions(editUndoAction, editRedoAction);
		} else {
			delete NewDoc;
			delete NewLayer;
			QMessageBox::warning(this, tr("Not a valid file"), tr("The file could not be opened"));
		}
	}
}

void MainWindow::on_fileUploadAction_triggered()
{
	on_editPropertiesAction_triggered();
	MerkaartorPreferences* p = MerkaartorPreferences::instance();
	syncOSM(this, p->getOsmWebsite(), p->getOsmUser(), p->getOsmPassword(), p->getProxyUse(),
		p->getProxyHost(), p->getProxyPort());

}

void MainWindow::on_fileDownloadAction_triggered()
{
	if (downloadOSM(this, theView->projection().viewport(), theDocument)) {
		on_editPropertiesAction_triggered();
	} else
		QMessageBox::warning(this, tr("Error downloading"), tr("The map could not be downloaded"));
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
	if (BBox.first) {
		theView->projection().setViewport(BBox.second, theView->rect());
		theView->projection().zoom(0.99, theView->rect().center(), theView->rect());
		invalidateView();
	}
}

void MainWindow::on_viewZoomInAction_triggered()
{
	theView->projection().zoom(1.33333, theView->rect().center(), theView->rect());
	invalidateView();
}

void MainWindow::on_viewZoomOutAction_triggered()
{
	theView->projection().zoom(0.75, theView->rect().center(), theView->rect());
	invalidateView();
}

void MainWindow::on_viewZoomWindowAction_triggered()
{
	theView->launch(new ZoomInteraction(theView));
}

void MainWindow::on_viewSetCoordinatesAction_triggered()
{
	QDialog* Dlg = new QDialog(this);
	Ui::SetPositionDialog Data;
	Data.setupUi(Dlg);
	CoordBox B(theView->projection().viewport());
	Data.Longitude->setText(QString::number(radToAng(B.center().lon())));
	Data.Latitude->setText(QString::number(radToAng(B.center().lat())));
	Data.SpanLongitude->setText(QString::number(radToAng(B.lonDiff())));
	Data.SpanLatitude->setText(QString::number(radToAng(B.latDiff())));
	if (Dlg->exec() == QDialog::Accepted) {
		theView->projection().setViewport(CoordBox(
											   Coord(
												   angToRad(Data.Latitude->text().toDouble() - Data.SpanLatitude->text().toDouble() / 2),
												   angToRad(Data.Longitude->text().toDouble() - Data.SpanLongitude->text().toDouble() / 2)),
											   Coord(
												   angToRad(Data.Latitude->text().toDouble() + Data.SpanLatitude->text().toDouble() / 2),
												   angToRad(Data.Longitude->text().toDouble() + Data.SpanLongitude->text().toDouble() / 2))), theView->rect());
		invalidateView();
	}
	delete Dlg;
}

void MainWindow::on_fileNewAction_triggered()
{
	theView->launch(0);
	theProperties->setSelection(0);
	if (!hasUnsavedChanges(*theDocument) || mayDiscardUnsavedChanges(this)) {
		delete theDocument;
		theDocument = new MapDocument;
		theView->setDocument(theDocument);
		theDocument->history().setActions(editUndoAction, editRedoAction);
		invalidateView();
	}
}

void MainWindow::on_createDoubleWayAction_triggered()
{
	theView->launch(new CreateDoubleWayInteraction(this, theView));
}

void MainWindow::on_createRoundaboutAction_triggered()
{
	theView->launch(new CreateRoundaboutInteraction(this, theView));
}

void MainWindow::on_createRoadAction_triggered()
{
	theView->launch(new CreateSingleWayInteraction(this, theView, false));
}

void MainWindow::on_createCurvedRoadAction_triggered()
{
	theView->launch(new CreateSingleWayInteraction(this, theView, true));
}

void MainWindow::on_createAreaAction_triggered()
{
	theView->launch(new CreateAreaInteraction(this, theView));
}

void MainWindow::on_createNodeAction_triggered()
{
	theView->launch(new CreateNodeInteraction(theView));
}

void MainWindow::on_roadJoinAction_triggered()
{
	CommandList* theList = new CommandList;
	joinRoads(theDocument, theList, theProperties);
	if (theList->empty())
		delete theList;
	else
		theDocument->history().add(theList);
}

void MainWindow::on_roadSplitAction_triggered()
{
	CommandList* theList = new CommandList;
	splitRoads(activeLayer(), theList, theProperties);
	if (theList->empty())
		delete theList;
	else
		theDocument->history().add(theList);
}

void MainWindow::on_roadBreakAction_triggered()
{
	CommandList* theList = new CommandList;
	breakRoads(activeLayer(), theList, theProperties);
	if (theList->empty())
		delete theList;
	else
		theDocument->history().add(theList);
}

void MainWindow::on_createRelationAction_triggered()
{
	Relation* R = new Relation;
	for (unsigned int i = 0; i < theProperties->size(); ++i)
		R->add("", theProperties->selection(i));
	theDocument->history().add(
		new AddFeatureCommand(theLayers->activeLayer(), R, true));
	theProperties->setSelection(R);
}

void MainWindow::on_editMapStyleAction_triggered()
{
	PaintStyleEditor* dlg = new PaintStyleEditor(this, EditPaintStyle::Painters);
	if (dlg->exec() == QDialog::Accepted) {
		EditPaintStyle::Painters = dlg->thePainters;
		for (VisibleFeatureIterator i(theDocument); !i.isEnd(); ++i)
			i.get()->invalidatePainter();
		invalidateView();
	}
	delete dlg;
}

MapLayer* MainWindow::activeLayer()
{
	return theLayers->activeLayer();
}

MapView* MainWindow::view()
{
	return theView;
}

void MainWindow::on_mapStyleSaveAction_triggered()
{
	QString f = QFileDialog::getSaveFileName(this, tr("Save map style"), QString(), tr("Merkaartor map style (*.mas)"));
	if (!f.isNull())
		savePainters(f);
}

void MainWindow::on_mapStyleLoadAction_triggered()
{
	QString f = QFileDialog::getOpenFileName(this, tr("Load map style"), QString(), tr("Merkaartor map style (*.mas)"));
	if (!f.isNull()) {
		loadPainters(f);
		for (VisibleFeatureIterator i(theDocument); !i.isEnd(); ++i)
			i.get()->invalidatePainter();
		invalidateView();
	}
}

void MainWindow::on_toolsPreferencesAction_triggered()
{
	PreferencesDialog* Pref = new PreferencesDialog();

	if (Pref->exec() == QDialog::Accepted) {
		theDocument->getImageLayer()->setMapAdapter(MerkaartorPreferences::instance()->getBgType());
		theLayers->updateContent();
		adjustLayers(true);

		if (MerkaartorPreferences::instance()->getProxyUse()) {
			ImageManager::instance()->setProxy(MerkaartorPreferences::instance()->getProxyHost(),
				MerkaartorPreferences::instance()->getProxyPort());
		}
		emit(preferencesChanged());
	}
}

void MainWindow::on_exportOSMAction_triggered()
{
	QString fileName = QFileDialog::getSaveFileName(this,
		tr("Export OSM"), MerkaartorPreferences::instance()->getWorkingDir() + "/untitled.osm", tr("OSM Files (*.osm)"));

	if (fileName != "")
		theDocument->exportOSM(fileName);
}

void MainWindow::on_editSelectAction_triggered()
{
	SelectionDialog* Sel = new SelectionDialog(this);

	if (Sel->exec() == QDialog::Accepted) {
		MerkaartorPreferences::instance()->setLastSearchName(Sel->edName->text());
		MerkaartorPreferences::instance()->setLastSearchKey(Sel->cbKey->currentText());
		MerkaartorPreferences::instance()->setLastSearchValue(Sel->cbValue->currentText());
		MerkaartorPreferences::instance()->setLastMaxSearchResults(Sel->sbMaxResult->value());

		QRegExp selName(Sel->edName->text(), Qt::CaseInsensitive, QRegExp::RegExp);
		QRegExp selKey(Sel->cbKey->currentText(), Qt::CaseInsensitive, QRegExp::RegExp);
		QRegExp selValue(Sel->cbValue->currentText(), Qt::CaseInsensitive, QRegExp::RegExp);
		int selMaxResult = Sel->sbMaxResult->value();

		std::vector <MapFeature *> selection;
		int added = 0;
		for (VisibleFeatureIterator i(theDocument); !i.isEnd() && added < selMaxResult; ++i) {
			MapFeature* F = i.get();
			if (selName.indexIn(F->description()) == -1) {
				continue;
			}
			int ok = false;
			for (unsigned int j=0; j < F->tagSize(); j++) {
				if ((selKey.indexIn(F->tagKey(j)) > -1) && (selValue.indexIn(F->tagValue(j)) > -1)) {
					ok = true;
					break;
				}
			}
			if (ok) {
				selection.push_back(F);
				++added;
			}
		}
		theProperties->setMultiSelection(selection);
	}
}

void MainWindow::closeEvent(QCloseEvent * event)
{
	if (hasUnsavedChanges(*theDocument) && !mayDiscardUnsavedChanges(this)) {
		event->ignore();
	}
}
