#include "DownloadOSM.h"

#include "MainWindow.h"
#include "Map/Coord.h"
#include "Map/ImportOSM.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"

#include <QtCore/QEventLoop>
#include <QtGui/QMainWindow>
#include <QtGui/QMessageBox>
#include <QtGui/QStatusBar>
#include <QtNetwork/QHttp>

DownloadReceiver::DownloadReceiver(QMainWindow* aWindow, QHttp& aRequest)
: Request(aRequest), Main(aWindow), Break(false)
{
	connect(&Request,SIGNAL(requestFinished(int, bool)), this,SLOT(finished(int, bool)));
	connect(&Request,SIGNAL(dataReadProgress(int,int)), this,SLOT(transferred(int,int)));
	connect(&Request,SIGNAL(dataSendProgress(int,int)), this,SLOT(transferred(int,int)));
}

bool DownloadReceiver::go(const QString& url)
{
	Done = ToDo = 0;
	OK = true;
	Id = Request.get(url);
	QEventLoop Loop;
	while (!Break)
	{
		Loop.processEvents(QEventLoop::ExcludeUserInputEvents);
	}
	Content = Request.readAll();
	Main->statusBar()->clearMessage();
	if (!OK)
		QMessageBox::warning(Main,MainWindow::tr("Download failed"),Request.errorString());
	return OK;
}

void DownloadReceiver::finished(int id, bool error)
{
	if (Id == id)
		Break = true;
	if (error)
		OK = false;
}

void DownloadReceiver::transferred(int Now, int Total)
{
	Done += Now;
	ToDo += Total-Now;
	if (ToDo < 0)
		ToDo = 0;
	Main->statusBar()->showMessage(QString("Transferred %1 bytes, still to do %2 bytes").arg(Done).arg(ToDo));
}

QByteArray& DownloadReceiver::content()
{
	return Content;
}

bool downloadOSM(QMainWindow* aParent, const QString& aWeb, const QString& aUser, const QString& aPassword, const CoordBox& aBox , MapDocument* theDocument)
{
	if (checkForConflicts(theDocument))
	{
		QMessageBox::warning(aParent,MainWindow::tr("Unresolved conflicts"), MainWindow::tr("Please resolve existing conflicts first"));
		return false;
	}
	aParent->setCursor(QCursor(Qt::WaitCursor));
	QHttp Request;
	Request.setHost(aWeb);
	Request.setUser(aUser, aPassword);

	QString URL("/api/0.3/map?bbox=%1,%2,%3,%4");
	URL = URL.arg(radToAng(aBox.bottomLeft().lon())).arg(radToAng(aBox.bottomLeft().lat())).arg(radToAng(aBox.topRight().lon())).arg(radToAng(aBox.topRight().lat()));
	DownloadReceiver Rcv(aParent, Request);

	if (!Rcv.go(URL))
	{
		aParent->setCursor(QCursor(Qt::ArrowCursor));
		return false;
	}
	aParent->setCursor(QCursor(Qt::ArrowCursor));
	int x = Request.lastResponse().statusCode();
	switch (x)
	{
	case 200:
		break;
	case 401:
		QMessageBox::warning(aParent,MainWindow::tr("Download failed"),MainWindow::tr("Username/password invalid"));
		return false;
	default:
		QMessageBox::warning(aParent,MainWindow::tr("Download failed"),MainWindow::tr("Unexpected http status code (%1)").arg(x));
		return false;
	}
	MapLayer* theLayer = new MapLayer("Download");
	bool OK = importOSM(aParent, Rcv.content(), theDocument, theLayer);
	if (!OK)
		delete theLayer;
	return OK;
}

bool checkForConflicts(MapDocument* theDocument)
{
	for (FeatureIterator it(theDocument); !it.isEnd(); ++it)
		if (it.get()->lastUpdated() == MapFeature::OSMServerConflict)
			return true;
	return false;
}


