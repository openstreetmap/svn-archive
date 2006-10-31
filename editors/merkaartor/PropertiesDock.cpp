#include "PropertiesDock.h"
#include "MainWindow.h"
#include "TagModel.h"
#include "Command/FeatureCommands.h"
#include "Command/TrackPointCommands.h"
#include "Command/WayCommands.h"
#include "Map/Coord.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"
#include "Map/Road.h"
#include "Map/TrackPoint.h"
#include "Map/Way.h"

#include <QtGui/QHeaderView>
#include <QtGui/QLineEdit>
#include <QtGui/QTableView>

PropertiesDock::PropertiesDock(MainWindow* aParent)
: QDockWidget(aParent), Main(aParent), CurrentUi(0), Selection(0), NowShowing(NoUiShowing)
{
	setMinimumSize(220,100);
	switchToNoUi();
	setWindowTitle(tr("Properties"));
	theModel = new TagModel(aParent);
}

PropertiesDock::~PropertiesDock(void)
{
	delete theModel;
}

MapFeature* PropertiesDock::selection()
{
	return Selection;
}

void PropertiesDock::setSelection(MapFeature* S)
{
	Selection = S;
	if (dynamic_cast<Way*>(Selection))
		switchToWayUi();
	else if (dynamic_cast<TrackPoint*>(Selection))
		switchToTrackPointUi();
	else if (dynamic_cast<Road*>(Selection))
		switchToRoadUi();
	else
		switchToNoUi();
	resetValues();
}

void PropertiesDock::switchToTrackPointUi()
{
	if (NowShowing == TrackPointUiShowing) return;
	NowShowing = TrackPointUiShowing;
	QWidget* NewUi = new QWidget(this);
	TrackPointUi.setupUi(NewUi);
	TrackPointUi.TagView->verticalHeader()->hide();
	setWidget(NewUi);
	if (CurrentUi)
		delete CurrentUi;
	CurrentUi = NewUi;
	connect(TrackPointUi.Longitude,SIGNAL(textChanged(const QString&)),this, SLOT(on_TrackPointLon_textChanged(const QString&)));
	connect(TrackPointUi.Latitude,SIGNAL(textChanged(const QString&)),this, SLOT(on_TrackPointLat_textChanged(const QString&)));
	setWindowTitle(tr("Properties - Trackpoint"));
}

void PropertiesDock::switchToWayUi()
{
	if (NowShowing == WayUiShowing) return;
	NowShowing = WayUiShowing;
	QWidget* NewUi = new QWidget(this);
	WayUi.setupUi(NewUi);
	WayUi.TagView->verticalHeader()->hide();
	setWidget(NewUi);
	if (CurrentUi)
		delete CurrentUi;
	CurrentUi = NewUi;
	connect(WayUi.Width,SIGNAL(textChanged(const QString&)),this, SLOT(on_WayWidth_textChanged(const QString&)));
	setWindowTitle(tr("Properties - Link"));
}

void PropertiesDock::switchToNoUi()
{
	if (NowShowing == NoUiShowing) return;
	NowShowing = NoUiShowing;
	QWidget* NewUi = new QWidget(this);
	setWidget(NewUi);
	if (CurrentUi)
		delete CurrentUi;
	CurrentUi = NewUi;
	setWindowTitle(tr("Properties"));
}

void PropertiesDock::switchToRoadUi()
{
	if (NowShowing == RoadUiShowing) return;
	NowShowing = RoadUiShowing;
	QWidget* NewUi = new QWidget(this);
	RoadUi.setupUi(NewUi);
	RoadUi.TagView->verticalHeader()->hide();
	setWidget(NewUi);
	if (CurrentUi)
		delete CurrentUi;
	CurrentUi = NewUi;
	connect(RoadUi.Name,SIGNAL(textChanged(const QString&)),this, SLOT(on_RoadName_textChanged(const QString&)));
	setWindowTitle(tr("Properties - Road"));
}

void PropertiesDock::resetValues()
{
	// to prevent slots to change the values also
	MapFeature* Current = Selection;
	Selection = 0;
	theModel->setFeature(0);
	if (Way* W = dynamic_cast<Way*>(Current))
	{
		WayUi.Width->setText(QString::number(W->width()));
		WayUi.Id->setText(W->id());
		theModel->setFeature(Current);
		WayUi.TagView->setModel(theModel);
	}
	else if (TrackPoint* Pt = dynamic_cast<TrackPoint*>(Current))
	{
		TrackPointUi.Id->setText(Pt->id());
		TrackPointUi.Latitude->setText(QString::number(radToAng(Pt->position().lat()),'g',8));
		TrackPointUi.Longitude->setText(QString::number(radToAng(Pt->position().lon()),'g',8));
		theModel->setFeature(Current);
		TrackPointUi.TagView->setModel(theModel);
	}
	else if (Road* R = dynamic_cast<Road*>(Current))
	{
		RoadUi.Id->setText(R->id());
		RoadUi.Name->setText(R->tagValue("name",""));
		theModel->setFeature(Current);
		RoadUi.TagView->setModel(theModel);
	}
	Selection = Current;
}

void PropertiesDock::on_WayWidth_textChanged(const QString& )
{
	if (WayUi.Width->text().isEmpty()) return;
	Way* W = dynamic_cast<Way*>(Selection);
	if (W)
	{
		W->setLastUpdated(MapFeature::User);
		Main->document()->history().add(
			new WaySetWidthCommand(W,WayUi.Width->text().toDouble()));
		Main->invalidateView(false);
		theModel->setFeature(Selection);
	}
}
void PropertiesDock::on_TrackPointLat_textChanged(const QString&)
{
	if (TrackPointUi.Latitude->text().isEmpty()) return;
	TrackPoint* Pt = dynamic_cast<TrackPoint*>(Selection);
	if (Pt)
	{
		Pt->setLastUpdated(MapFeature::User);
		Main->document()->history().add(
			new MoveTrackPointCommand(Pt,
				Coord(angToRad(TrackPointUi.Latitude->text().toDouble()),Pt->position().lon())));
		Main->invalidateView(false);
	}
}

void PropertiesDock::on_TrackPointLon_textChanged(const QString&)
{
	if (TrackPointUi.Longitude->text().isEmpty()) return;
	TrackPoint* Pt = dynamic_cast<TrackPoint*>(Selection);
	if (Pt)
	{
		Pt->setLastUpdated(MapFeature::User);
		Main->document()->history().add(
			new MoveTrackPointCommand(Pt,
				Coord(Pt->position().lat(),angToRad(TrackPointUi.Longitude->text().toDouble()))));
		Main->invalidateView(false);
	}
}

void PropertiesDock::on_RoadName_textChanged(const QString&)
{
	if (Selection)
	{
		if (RoadUi.Name->text().isEmpty())
			Main->document()->history().add(
				new ClearTagCommand(Selection,"name"));
		else
			Main->document()->history().add(
				new SetTagCommand(Selection,"name",RoadUi.Name->text()));
		theModel->setFeature(Selection);
	}
}


