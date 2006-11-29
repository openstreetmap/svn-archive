#include "DownloadOSM.h"

#include "MainWindow.h"
#include "MapView.h"
#include "Map/Coord.h"
#include "Map/ImportGPX.h"
#include "Map/ImportOSM.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"
#include "Map/TrackSegment.h"

#include "GeneratedFiles/ui_DownloadMapDialog.h"

#include <QtCore/QBuffer>
#include <QtCore/QSettings>
#include <QtCore/QTimer>
#include <QtGui/QComboBox>
#include <QtGui/QMainWindow>
#include <QtGui/QMessageBox>
#include <QtGui/QProgressBar>
#include <QtGui/QProgressDialog>
#include <QtGui/QStatusBar>

/* DOWNLOADER */

Downloader::Downloader(const QString& aWeb, const QString& aUser, const QString& aPwd, bool aUse04Api)
: Web(aWeb), User(aUser), Password(aPwd), Use04Api(aUse04Api)
{
	Request.setHost(aWeb);
	Request.setUser(User,Password);
	connect(&Request,SIGNAL(requestFinished(int, bool)), this,SLOT(finished(int, bool)));
}

bool Downloader::go(const QString& url)
{
	Error = false;
	Id = Request.get(url);
	Loop.exec();
	Content = Request.readAll();
	Result = Request.lastResponse().statusCode();
	return !Error;
}

bool Downloader::request(const QString& Method, const QString& URL, const QString& Data)
{
	QByteArray ba(Data.toUtf8());
	QBuffer Buf(&ba);

	Error = false;

	QHttpRequestHeader Header(Method,URL);
	Header.setValue("Host",Web);

	Id = Request.request(Header,ba);

	Loop.exec();
	Content = Request.readAll();
	Result = Request.lastResponse().statusCode();
	return !Error;
}

QByteArray& Downloader::content()
{
	return Content;
}

void Downloader::finished(int id, bool error)
{
	if (error)
		Error = true;
	if (Id == id)
		Loop.exit();
}

int Downloader::resultCode()
{
	return Result;
}

QString Downloader::getURLToFetch(const QString &What)
{
	QString URL("/api/0.3/%1?%2=");
	if (Use04Api)
		URL = QString("/api/0.4/%1?%2=");
	return URL.arg(What).arg(What);
}


QString Downloader::getURLToFetch(const QString &What, const QString& Id)
{
	QString URL("/api/0.3/%1/%2");
	if (Use04Api)
		URL = QString("/api/0.4/%1/%2");
	return URL.arg(What).arg(Id);
}

QString Downloader::getURLToCreate(const QString &What)
{
	QString URL("/api/0.3/%1/0");
	if (Use04Api)
		URL = QString("api/0.4/%1/create");
	return URL.arg(What);
}

QString Downloader::getURLToUpdate(const QString &What, const QString& Id)
{
	QString URL("/api/0.3/%1/%2");
	if (Use04Api)
		URL = QString("/api/0.4/%1/%2");
	return URL.arg(What).arg(Id);
}

QString Downloader::getURLToDelete(const QString &What, const QString& Id)
{
	QString URL("/api/0.3/%1/%2");
	if (Use04Api)
		URL = QString("/api/0.4/%1/%2");
	return URL.arg(What).arg(Id);
}


/* DOWNLOADRECEIVER */

DownloadReceiver::DownloadReceiver(QMainWindow* aWindow, QHttp& aRequest)
: Request(aRequest), Main(aWindow)
{
	connect(&Request,SIGNAL(requestFinished(int, bool)), this,SLOT(finished(int, bool)));
	connect(&Request,SIGNAL(dataReadProgress(int,int)), this,SLOT(transferred(int,int)));
	connect(&Request,SIGNAL(dataSendProgress(int,int)), this,SLOT(transferred(int,int)));
}

bool DownloadReceiver::go(const QString& url)
{
	QTimer* t = new QTimer(this);
	connect(t,SIGNAL(timeout()),this,SLOT(animate()));
	OK = true;
	Id = Request.get(url);
	ProgressDialog = new QProgressDialog(Main);
	ProgressDialog->setWindowModality(Qt::ApplicationModal);
	QProgressBar* Bar = new QProgressBar(ProgressDialog);
	Bar->setTextVisible(false);
	ProgressDialog->setBar(Bar);
	ProgressDialog->setMinimumDuration(0);
	ProgressDialog->setLabelText("Downloading from OSM");
	ProgressDialog->setMaximum(11);
	t->start(200);
	int r = ProgressDialog->exec();
	delete t;
	delete ProgressDialog;
	if (r == QProgressDialog::Rejected)
		return false;
	Content = Request.readAll();
	if (!OK)
		QMessageBox::warning(Main,MainWindow::tr("Download failed"),Request.errorString());
	return OK;
}

void DownloadReceiver::animate()
{
	ProgressDialog->setValue((ProgressDialog->value()+1) % 11);
}

void DownloadReceiver::finished(int id, bool error)
{
	if (error)
		OK = false;
	if (Id == id)
		ProgressDialog->done(QDialog::Accepted);
}

void DownloadReceiver::transferred(int Now, int Total)
{
	ProgressDialog->setLabelText("Waiting for reply...");
}

QByteArray& DownloadReceiver::content()
{
	return Content;
}

bool downloadOSM(QMainWindow* aParent, const QString& aWeb, const QString& aUser, const QString& aPassword, bool Use04Api, const CoordBox& aBox , MapDocument* theDocument)
{
	if (checkForConflicts(theDocument))
	{
		QMessageBox::warning(aParent,MainWindow::tr("Unresolved conflicts"), MainWindow::tr("Please resolve existing conflicts first"));
		return false;
	}
	QHttp Request;
	Request.setHost(aWeb);
	Request.setUser(aUser, aPassword);

	QString URL("/api/0.3/map?bbox=%1,%2,%3,%4");
	if (Use04Api)
		URL = QString("/api/0.4/map?bbox=%1,%2,%3,%4");
	URL = URL.arg(radToAng(aBox.bottomLeft().lon())).arg(radToAng(aBox.bottomLeft().lat())).arg(radToAng(aBox.topRight().lon())).arg(radToAng(aBox.topRight().lat()));
	DownloadReceiver Rcv(aParent, Request);

	if (!Rcv.go(URL))
	{
		aParent->setCursor(QCursor(Qt::ArrowCursor));
		return false;
	}
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
	Downloader Down(aWeb, aUser, aPassword, Use04Api);
	MapLayer* theLayer = new MapLayer("Download");
	bool OK = importOSM(aParent, Rcv.content(), theDocument, theLayer, &Down);
	if (!OK)
		delete theLayer;
	return OK;
}


bool downloadTracksFromOSM(QMainWindow* Main, const QString& aWeb, const QString& aUser, const QString& aPassword, bool Use04Api, const CoordBox& aBox , MapDocument* theDocument)
{
	Downloader theDownloader(aWeb, aUser, aPassword, Use04Api);
	MapLayer* trackLayer = new MapLayer("Downloaded tracks");
	theDocument->add(trackLayer);
	QProgressDialog ProgressDialog(Main);
	ProgressDialog.setWindowModality(Qt::ApplicationModal);
	QProgressBar* Bar = new QProgressBar(&ProgressDialog);
	Bar->setTextVisible(false);
	ProgressDialog.setBar(Bar);
	ProgressDialog.setMinimumDuration(0);
	ProgressDialog.setMaximum(11);
	ProgressDialog.setValue(1);
	ProgressDialog.show();
	for (unsigned int Page=0; ;++Page)
	{
		ProgressDialog.setLabelText(QString("Downloading trackpoints %1-%2").arg(Page*5000+1).arg(Page*5000+5000));
		QString URL("/api/0.3/trackpoints?bbox=%1,%2,%3,%4&page=%5");
		if (Use04Api)
			URL = QString("/api/0.4/trackpoints?bbox=%1,%2,%3,%4&page=%5");
		URL = URL.arg(radToAng(aBox.bottomLeft().lon())).
				arg(radToAng(aBox.bottomLeft().lat())).
				arg(radToAng(aBox.topRight().lon())).
				arg(radToAng(aBox.topRight().lat())).
				arg(Page);
		if (!theDownloader.go(URL))
			return false;
		if (theDownloader.resultCode() != 200)
			return false;
		unsigned int Before = trackLayer->size();
		QByteArray Ar(theDownloader.content());
		bool OK = importGPX(Main, Ar, theDocument, trackLayer, false);
		if (!OK)
			return false;
		if (Before == trackLayer->size())
			break;
	}
	return true;
}


bool checkForConflicts(MapDocument* theDocument)
{
	for (FeatureIterator it(theDocument); !it.isEnd(); ++it)
		if (it.get()->lastUpdated() == MapFeature::OSMServerConflict)
			return true;
	return false;
}

bool downloadOSM(MainWindow* aParent, const CoordBox& aBox , MapDocument* theDocument)
{
	static bool DownloadRaw = true;
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
	ui.IncludeTracks->setChecked(DownloadRaw);
	ui.Use04Api->setChecked(Sets.value("use04api").toBool());
	bool OK = true;
	if (dlg->exec() == QDialog::Accepted)
	{
		Sets.setValue("user",ui.Username->text());
		Sets.setValue("password",ui.Password->text());
		Sets.setValue("use04api",ui.Use04Api->isChecked());
		DownloadRaw = false;
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
		aParent->view()->setUpdatesEnabled(false);
		OK = downloadOSM(aParent,ui.Website->text(),ui.Username->text(),ui.Password->text(),ui.Use04Api->isChecked(),Clip,theDocument);
		if (OK && ui.IncludeTracks->isChecked())
			OK = downloadTracksFromOSM(aParent,ui.Website->text(),ui.Username->text(),ui.Password->text(),ui.Use04Api->isChecked(),Clip,theDocument);
		aParent->view()->setUpdatesEnabled(true);
		if (OK)
		{
			aParent->view()->projection().setViewport(Clip,aParent->view()->rect());
			aParent->invalidateView();
		}
	}
	delete dlg;
	return OK;
}

