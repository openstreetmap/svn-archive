#include "DownloadOSM.h"

#include "MainWindow.h"
#include "Map/Coord.h"
#include "Map/ImportOSM.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"

#include "GeneratedFiles/ui_DownloadMapDialog.h"

#include <QtCore/QEventLoop>
#include <QtCore/QSettings>
#include <QtGui/QComboBox>
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

bool downloadOSM(QMainWindow* aParent, const CoordBox& aBox , MapDocument* theDocument)
{
	QDialog * dlg = new QDialog(aParent);
	QSettings Sets;
	Sets.beginGroup("downloadosm");
	Ui::DownloadMapDialog ui;
	ui.setupUi(dlg);
	ui.Website->setText("www.openstreetmap.org");
	QStringList DefaultBookmarks;
	DefaultBookmarks << "London" << "51.47" << "-0.20" << "51.51" << "-0.08";
//	DefaultBookmarks << "Rotterdam" << "51.89" << "4.43" << "51.93" << "4.52";
	QStringList Bookmarks(DefaultBookmarks);
	QVariant V = Sets.value("bookmarks");
	if (!V.isNull())
		Bookmarks = V.toStringList();
	for (unsigned int i=0; i<Bookmarks.size(); i+=5)
		ui.Bookmarks->addItem(Bookmarks[i]);
	ui.Username->setText(Sets.value("user").toString());
	ui.Password->setText(Sets.value("password").toString());
	bool OK = true;
	if (dlg->exec() == QDialog::Accepted)
	{
		Sets.setValue("user",ui.Username->text());
		Sets.setValue("password",ui.Password->text());
		CoordBox Clip(Coord(0,0),Coord(0,0));
		if (ui.FromBookmark->isChecked())
		{
			unsigned int idx = ui.Bookmarks->currentIndex()*5+1;
			Clip = CoordBox(Coord(angToRad(Bookmarks[idx].toDouble()),angToRad(Bookmarks[idx+1].toDouble())),
				Coord(angToRad(Bookmarks[idx+2].toDouble()),angToRad(Bookmarks[idx+3].toDouble())));
		}
		else if (ui.FromView->isChecked())
		{
			Clip = aBox;
		}
		else if (ui.FromviewAndAdd->isChecked())
		{
			Clip = aBox;
			Bookmarks.insert(0,ui.NewBookmark->text());
			Bookmarks.insert(1,QString::number(radToAng(Clip.bottomLeft().lat())));
			Bookmarks.insert(2,QString::number(radToAng(Clip.bottomLeft().lon())));
			Bookmarks.insert(3,QString::number(radToAng(Clip.topRight().lat())));
			Bookmarks.insert(4,QString::number(radToAng(Clip.topRight().lon())));
			Sets.setValue("bookmarks",Bookmarks);
		}
		OK = downloadOSM(aParent,ui.Website->text(),ui.Username->text(),ui.Password->text(),Clip,theDocument);
	}
	delete dlg;
	return OK;
}

