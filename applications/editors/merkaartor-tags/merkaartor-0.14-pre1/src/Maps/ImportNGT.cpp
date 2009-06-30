#include "ImportNGT.h"
#include "Command/DocumentCommands.h"
#include "Maps/MapDocument.h"
#include "Maps/TrackPoint.h"
#include "Maps/TrackSegment.h"

#include <QtCore/QFile>
#include <QtCore/QStringList>
#include <QtCore/QTextStream>

#include <math.h>

bool importNGT(QWidget* /* aParent */, const QString& aFilename, MapDocument* theDocument, MapLayer* theLayer)
{
	QFile f(aFilename);
	if (f.open(QIODevice::ReadOnly))
	{
		QTextStream s(&f);
		CommandList* theList  = new CommandList(MainWindow::tr("Import NGT"), NULL);
		TrackSegment* theSegment = new TrackSegment;
		while (!f.atEnd())
		{
			QString Line(f.readLine());
			QStringList Items(Line.split('|'));
			if (Items.count() >= 5)
			{
				TrackPoint* Pt = new TrackPoint(Coord(int(Items[4].toDouble()*INT_MAX), int(Items[3].toDouble()*INT_MAX)));
				Pt->setLastUpdated(MapFeature::Log);
				theList->add(new AddFeatureCommand(theLayer,Pt, true));
				theSegment->add(Pt);
			}
		}
		if (theList->empty())
		{
			delete theList;
			delete theSegment;
		}
		else
		{
			theList->add(new AddFeatureCommand(theLayer,theSegment,true));
			theDocument->addHistory(theList);
		}
		return true;
	}
	return false;
}
