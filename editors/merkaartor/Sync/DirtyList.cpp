#include "Sync/DirtyList.h"
#include "Command/Command.h"
#include "Map/Coord.h"
#include "Map/ExportOSM.h"
#include "Map/MapDocument.h"
#include "Map/Road.h"
#include "Map/TrackPoint.h"
#include "Map/Way.h"

#include <QtCore/QBuffer>
#include <QtCore/QEventLoop>
#include <QtGui/QDialog>
#include <QtGui/QListWidget>
#include <QtGui/QMessageBox>
#include <QtGui/QProgressDialog>
#include <QtNetwork/QHttp>
#include <QtNetwork/QTcpSocket>

#include <algorithm>


bool isPartOfWay(MapDocument* theDocument, TrackPoint* Pt)
{
	// if the user has added special tags, that fine also
	for (unsigned int i=0; i<Pt->tagSize(); ++i)
		if (Pt->tagKey(i) != "created_by")
			return true;
	for (unsigned int j=0; j<theDocument->numLayers(); ++j)
	{
		MapLayer* theLayer = theDocument->layer(j);
		for (unsigned i=0; i<theLayer->size(); ++i)
		{
			Way* W = dynamic_cast<Way*>(theLayer->get(i));
			if (W)
			{
				if ( (W->from() == Pt) || (W->to() ==Pt) || (W->controlFrom() == Pt) || (W->controlTo() == Pt) )
					return true;
			}
		}
	}
	return false;
}



DirtyList::DirtyList(MapDocument* aDoc, const QString& aWeb, const QString& aUser, const QString& aPwd)
: theDocument(aDoc), Web(aWeb), User(aUser), Pwd(aPwd)
{
}

DirtyList::~DirtyList(void)
{
}

bool DirtyList::isAdded(MapFeature* F)
{
	if (Way* W = dynamic_cast<Way*>(F))
		return isAdded(W);
	else if (TrackPoint* Pt = dynamic_cast<TrackPoint*>(F))
		return isAdded(Pt);
	else if (Road* R = dynamic_cast<Road*>(F))
		return isAdded(R);
	return false;
}

bool DirtyList::isAdded(Way* W)
{
	if (W && (W->lastUpdated() == MapFeature::User))
	{
		Added.push_back(W);
		if (IsExecuting)
			return executeAdded(W);
		else
			describeAdded(W);
	}
	return false;
}

bool DirtyList::isAdded(TrackPoint* Pt)
{
	if (Pt && isPartOfWay(theDocument,Pt) && (Pt->lastUpdated() == MapFeature::User))
	{
		Added.push_back(Pt);
		if (IsExecuting)
			return executeAdded(Pt);
		else
			describeAdded(Pt);
	}
	return false;
}

bool DirtyList::isAdded(Road* R)
{
	if (R && (R->lastUpdated() == MapFeature::User))
	{
		Added.push_back(R);
		if (IsExecuting)
			return executeAdded(R);
		else
			describeAdded(R);
	}
	return false;
}

bool DirtyList::isUpdated(MapFeature* F)
{
	std::vector<MapFeature*>::iterator i = std::find(Added.begin(),Added.end(),F);
	if (i != Added.end())
		return true;
	i = std::find(Changed.begin(),Changed.end(),F);
	if (i != Changed.end())
		return true;
	return false;
}

bool DirtyList::isChanged(MapFeature* F)
{
	if (F->lastUpdated() != MapFeature::User) return false;
	if (Way* W = dynamic_cast<Way*>(F))
		return isChanged(W);
	else if (TrackPoint* Pt = dynamic_cast<TrackPoint*>(F))
		return isChanged(Pt);
	else if (Road* R = dynamic_cast<Road*>(F))
		return isChanged(R);
	return false;
}

bool DirtyList::isChanged(Way* W)
{
	if (W->lastUpdated() != MapFeature::User) return false;
	if (isUpdated(W)) return IsExecuting;
	Changed.push_back(W);
	if (IsExecuting)
		return executeChanged(W);
	else
		describeChanged(W);
	return false;
}

bool DirtyList::isChanged(Road* R)
{
	if (R->lastUpdated() != MapFeature::User) return false;
	if (isUpdated(R)) return IsExecuting;
	Changed.push_back(R);
	if (IsExecuting)
		return executeChanged(R);
	else
		describeChanged(R);
	return false;
}

bool DirtyList::isChanged(TrackPoint *Pt)
{
	if (Pt->lastUpdated() != MapFeature::User) return false;
	if (isUpdated(Pt)) return IsExecuting;
	if (isPartOfWay(theDocument,Pt))
	{
		Changed.push_back(Pt);
		if (IsExecuting)
			return executeChanged(Pt);
		else
			describeChanged(Pt);
	}
	return false;
}

void DirtyList::describeAdded(Way* W)
{
	if (W->controlFrom() || W->controlTo())
		Ui.ChangesList->addItem(QString("IGNORE bezier link %1").arg(W->id()));
	else
		Ui.ChangesList->addItem(QString("ADD link %1").arg(W->id()));
}

void DirtyList::describeAdded(Road* R)
{
	Ui.ChangesList->addItem(QString("ADD road %1").arg(R->id()));
}

void DirtyList::describeAdded(TrackPoint* Pt)
{
	Ui.ChangesList->addItem(QString("ADD trackpoint %1").arg(Pt->id()));
}

void DirtyList::describeChanged(Way* W)
{
	Ui.ChangesList->addItem(QString("UPDATE link %1").arg(W->id()));
}

void DirtyList::describeChanged(TrackPoint* Pt)
{
	Ui.ChangesList->addItem(QString("UPDATE trackpoint %1").arg(Pt->id()));
}

void DirtyList::describeChanged(Road* R)
{
	Ui.ChangesList->addItem(QString("UPDATE road %1").arg(R->id()));
}

bool DirtyList::sendRequest(const QString& URL, const QString& Data, QString& Rcv)
{
	QByteArray ba(Data.toUtf8());
	QBuffer Buf(&ba);

	QHttp Link;
	connect(&Link,SIGNAL(requestFinished(int, bool)), this,SLOT(on_Request_finished(int, bool)));
	Link.setHost(Web);
	Link.setUser(User, Pwd);
	QHttpRequestHeader Request("PUT",URL);
	Request.setValue("Host",Web);
	int y = Request.minorVersion();
	y = Request.majorVersion();

	Finished = false;
	FinishedId = Link.request(Request,ba);
	QEventLoop Loop;
	
	while (!Finished)
	{
		Loop.processEvents(QEventLoop::ExcludeUserInputEvents);
	}
	QByteArray Content = Link.readAll();
	int x = Link.lastResponse().statusCode();
	
	if (x==200)
	{
		Rcv = QString::fromUtf8(Content.data());
		return true;
	}
	else
		QMessageBox::warning(Progress,tr("Error uploading request"),tr("There was an error uploading this request (%1)").arg(x));
	return false;

}

bool DirtyList::executeAdded(Way* W)
{
	if (W->controlFrom() || W->controlTo())
		return false;
	Progress->setValue(++Task);
	Progress->setLabelText(QString("ADD link %1").arg(W->id()));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);

	QString DataIn, DataOut, OldId;
	OldId = W->id();
	W->setId("0");
	DataIn = wrapOSM(exportOSM(*W));
	W->setId(OldId);
	QString URL("/api/0.3/segment/0");
	if (sendRequest(URL,DataIn,DataOut))
	{
		// chop off extra spaces, newlines etc
		W->setId("segment_"+QString::number(DataOut.toInt()));
		W->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}

bool DirtyList::executeAdded(Road *R)
{
	Progress->setValue(++Task);
	Progress->setLabelText(QString("ADD road %1").arg(R->id()));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);

	QString DataIn, DataOut, OldId;
	OldId = R->id();
	R->setId("0");
	DataIn = wrapOSM(exportOSM(*R));
	R->setId(OldId);
	QString URL("/api/0.3/way/0");
	if (sendRequest(URL,DataIn,DataOut))
	{
		// chop off extra spaces, newlines etc
		R->setId("way_"+QString::number(DataOut.toInt()));
		R->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}


bool DirtyList::executeAdded(TrackPoint* Pt)
{
	Progress->setValue(++Task);
	Progress->setLabelText(QString("ADD trackpoint %1").arg(Pt->id()));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);

	QString DataIn, DataOut, OldId;
	OldId = Pt->id();
	Pt->setId("0");
	DataIn = wrapOSM(exportOSM(*Pt));
	Pt->setId(OldId);
	QString URL("/api/0.3/node/0");
	if (sendRequest(URL,DataIn,DataOut))
	{
		// chop off extra spaces, newlines etc
		Pt->setId("node_"+QString::number(DataOut.toInt()));
		Pt->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}

static QString stripToOSMId(const QString& id)
{
	int f = id.lastIndexOf("_");
	if (f>0)
		return id.right(id.length()-(f+1));
	return id;
}

bool DirtyList::executeChanged(Way* W)
{
	Progress->setValue(++Task);
	Progress->setLabelText(QString("UPDATE link %1").arg(W->id()));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
	QString URL("/api/0.3/segment/%1");
	URL = URL.arg(stripToOSMId(W->id()));
	QString DataIn, DataOut;
	DataIn = wrapOSM(exportOSM(*W));
	if (sendRequest(URL,DataIn,DataOut))
	{
		W->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return true;
}

bool DirtyList::executeChanged(Road* R)
{
	Progress->setValue(++Task);
	Progress->setLabelText(QString("UPDATE road %1").arg(R->id()));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
	QString URL("/api/0.3/way/%1");
	URL = URL.arg(stripToOSMId(R->id()));
	QString DataIn, DataOut;
	DataIn = wrapOSM(exportOSM(*R));
	if (sendRequest(URL,DataIn,DataOut))
	{
		R->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return true;
}

bool DirtyList::executeChanged(TrackPoint* Pt)
{
	Progress->setValue(++Task);
	Progress->setLabelText(QString("UPDATE trackpoint %1").arg(Pt->id()));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
	QString URL("/api/0.3/node/%1");
	URL = URL.arg(stripToOSMId(Pt->id()));
	QString DataIn, DataOut;
	DataIn = wrapOSM(exportOSM(*Pt));
	if (sendRequest(URL,DataIn,DataOut))
	{
		Pt->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}

bool DirtyList::showChanges(QWidget* aParent)
{
	IsExecuting = false;
	Added.clear();
	Changed.clear();
	QDialog* dlg = new QDialog(aParent);
	Ui.setupUi(dlg);

	theDocument->history().buildDirtyList(*this);

	bool ok = (dlg->exec() == QDialog::Accepted);

	Task = Ui.ChangesList->count();
	delete dlg;
	return ok;
}

bool DirtyList::executeChanges(QWidget* aParent)
{
	IsExecuting = true;
	Added.clear();
	Changed.clear();
	Progress = new QProgressDialog(aParent);
	Progress->setMinimumDuration(0);
	Progress->setMaximum(Task);
	Progress->show();
	Task = 0;
	theDocument->history().buildDirtyList(*this);
	delete Progress;
	return true;
}

void DirtyList::on_Request_finished(int id, bool err)
{
	if ( (id == FinishedId) || err)
	{
		Finished = true;
		FinishedError = err;
	}
}


