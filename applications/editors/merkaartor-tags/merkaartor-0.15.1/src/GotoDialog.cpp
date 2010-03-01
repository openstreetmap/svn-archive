//
// C++ Implementation: GotoDialog
//
// Description:
//
//
// Author: Chris Browet <cbro@semperpax.com>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//
#include "GotoDialog.h"

#include <QMessageBox>

#include "Preferences/MerkaartorPreferences.h"
#include "NameFinder/namefinderwidget.h"
#include "Utils/OsmLink.h"

GotoDialog::GotoDialog(const MapView& aView, QWidget *parent)
	:QDialog(parent)
{
	setupUi(this);

	setWindowFlags(windowFlags() & ~Qt::WindowContextHelpButtonHint);
	setWindowFlags(windowFlags() | Qt::MSWindowsFixedSizeDialogHint);

	CoordBox B = aView.viewport();
	int OsmZoom = int((log((360.0 / intToAng(B.latDiff()))) / log(2.0)) + 1);
	OsmZoom = qMin(OsmZoom, 18);
	OsmZoom = qMax(OsmZoom, 1);

	int idx = 0;
	int selIdx = -1;
	double dist = 6372.795;
	double d;
	BookmarkList* Bookmarks = M_PREFS->getBookmarks();
	BookmarkListIterator i(*Bookmarks);
	while (i.hasNext()) {
		i.next();

		if (i.value().deleted == false) {
			coordBookmark->addItem(i.key());
			CoordBox C = i.value().Coordinates;
			if ((d = C.center().distanceFrom(B.center())) < dist) {
				dist = d;
				selIdx = idx;
			}
		}
		++idx;
	}
	coordBookmark->setCurrentIndex(selIdx);

	searchWidget = new NameFinder::NameFinderWidget(this);
	connect(searchWidget, SIGNAL(selectionChanged()), this, SLOT(searchWidget_selectionChanged()));
	connect(searchWidget, SIGNAL(doubleClicked()), this, SLOT(searchWidget_doubleClicked()));
	connect(searchWidget, SIGNAL(done()), this, SLOT(searchWidget_done()));
	verticalLayout_4->addWidget(searchWidget);

	coordOSM->setText( QString("http://www.openstreetmap.org/?lat=%1&lon=%2&zoom=%3")
		.arg(QString::number(intToAng(B.center().lat()), 'f', 4))
		.arg(QString::number(intToAng(B.center().lon()), 'f', 4))
		.arg(QString::number(OsmZoom))
		);
	coordOsmApi->setText( QString("http://www.openstreetmap.org/api/%1/map?bbox=%2,%3,%4,%5")
		.arg(M_PREFS->apiVersion())
		.arg(QString::number(intToAng(B.bottomLeft().lon()), 'f', 4))
		.arg(QString::number(intToAng(B.bottomLeft().lat()), 'f', 4))
		.arg(QString::number(intToAng(B.topRight().lon()), 'f', 4))
		.arg(QString::number(intToAng(B.topRight().lat()), 'f', 4))
		);
	coordOsmXApi->setText( QString("http://xapi.openstreetmap.org/api/0.5/*[bbox=%1,%2,%3,%4]")
		.arg(QString::number(intToAng(B.bottomLeft().lon()), 'f', 4))
		.arg(QString::number(intToAng(B.bottomLeft().lat()), 'f', 4))
		.arg(QString::number(intToAng(B.topRight().lon()), 'f', 4))
		.arg(QString::number(intToAng(B.topRight().lat()), 'f', 4))
		);
	coordCoord->setText( QString("%1, %2, %3, %4")
		.arg(QString::number(intToAng(B.bottomLeft().lon()), 'f', 4))
		.arg(QString::number(intToAng(B.bottomLeft().lat()), 'f', 4))
		.arg(QString::number(intToAng(B.topRight().lon()), 'f', 4))
		.arg(QString::number(intToAng(B.topRight().lat()), 'f', 4))
		);
	coordSpan->setText( QString("%1, %2, %3, %4")
		.arg(QString::number(intToAng(B.center().lat()), 'f', 4))
		.arg(QString::number(intToAng(B.center().lon()), 'f', 4))
		.arg(QString::number(intToAng(B.latDiff()), 'f', 4))
		.arg(QString::number(intToAng(B.lonDiff()), 'f', 4))
		);

	resize(1,1);
}

void GotoDialog::on_searchButton_clicked()
{
		QString searchString = NameFinderEdit->text();
		searchWidget->search(searchString);
		searchButton->setEnabled(false);
}
void GotoDialog::on_buttonBox_clicked(QAbstractButton * button)
{
	if (buttonBox->buttonRole(button) == QDialogButtonBox::AcceptRole) {
		if (rbBookmark->isChecked()) {
			theNewViewport = M_PREFS->getBookmarks()->value(coordBookmark->currentText()).Coordinates;
		} else
		if (rbOSM->isChecked()) {
			OsmLink ol(coordOSM->text());
			if (!ol.isValid()) {
				QMessageBox::warning(this, QApplication::translate("GotoDialog", "Invalid OSM url"),
					QApplication::translate("GotoDialog", "The specified url is invalid!"));
				return;
			}
			theNewViewport = ol.getCoordBox();
		} else
		if (rbCoord->isChecked()) {
			QStringList tokens = coordCoord->text().split(",");
			if (tokens.size() < 4) {
				QMessageBox::warning(this, QApplication::translate("GotoDialog", "Invalid Coordinates format"),
					QApplication::translate("GotoDialog", "Coordinates must be: '<left lon>, <bottom lat>, <right lon>, <top lat>'"));
				return;
			}
			theNewViewport = CoordBox(Coord(angToInt(tokens[1].toDouble()), angToInt(tokens[0].toDouble())), Coord(angToInt(tokens[3].toDouble()), angToInt(tokens[2].toDouble())));
		} else
		if (rbSpan->isChecked()) {
			QStringList tokens = coordSpan->text().split(",");
			if (tokens.size() < 4) {
				QMessageBox::warning(this, QApplication::translate("GotoDialog", "Invalid Coordinates format"),
					QApplication::translate("GotoDialog", "Coordinates must be: '<center lat>, <center lon>, <span lat>, <span lon>'"));
				return;
			}
			theNewViewport = CoordBox(
								Coord(
									angToInt(tokens[0].toDouble() - tokens[2].toDouble() / 2),
									angToInt(tokens[1].toDouble() - tokens[3].toDouble() / 2)),
								Coord(
									angToInt(tokens[0].toDouble() + tokens[2].toDouble() / 2),
									angToInt(tokens[1].toDouble() + tokens[3].toDouble() / 2))
								);
		}
		accept();
	}
}

void GotoDialog::searchWidget_selectionChanged()
{
	QPointF centerPoint = searchWidget->selectedCoords();
	int zoom = searchWidget->selectedZoom();
	// The API doesn't like request for too large bounding boxes so reset to a default
	if (zoom < 15)
		zoom = 15;
	coordOSM->setText( QString("http://www.openstreetmap.org/?lat=%1&lon=%2&zoom=%3")
		.arg(QString::number(centerPoint.x(), 'f', 4))
		.arg(QString::number(centerPoint.y(), 'f', 4))
				.arg(QString::number(zoom))
		);
	rbOSM->setChecked(true);

}

void GotoDialog::on_NameFinderEdit_textChanged(const QString & text)
{
	if (!text.isEmpty()) {
		searchButton->setDefault(true);
	} else {
		searchButton->setDefault(false);
		buttonBox->button(QDialogButtonBox::Ok)->setDefault(true);
	}
}

void GotoDialog::searchWidget_doubleClicked()
{
	buttonBox->button(QDialogButtonBox::Ok)->click();
}

void GotoDialog::searchWidget_done()
{
	searchButton->setEnabled(true);
}

void GotoDialog::changeEvent(QEvent * event)
{
		if (event->type() == QEvent::LanguageChange)
				retranslateUi(this);
}

