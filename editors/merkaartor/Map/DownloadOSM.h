#ifndef MERKATOR_DOWNLOADOSM_H_
#define MERKATOR_DOWNLOADOSM_H_

class MapDocument;

class QHttp;
class QString;
class QMainWindow;
class QProgressDialog;
class MainWindow;
class CoordBox;

#include <QtCore/QByteArray>
#include <QtCore/QEventLoop>
#include <QtCore/QObject>
#include <QtNetwork/QHttp>


class Downloader : public QObject
{
	Q_OBJECT

	public:
		Downloader(const QString& aWeb, const QString& aUser, const QString& aPwd, bool aUse04Api);

		bool request(const QString& Method, const QString& URL, const QString& Out);
		bool go(const QString& url);
		QByteArray& content();
		int resultCode();
		QString getURLToFetch(const QString& What);
		QString getURLToFetch(const QString& What, const QString& Id);
		QString getURLToCreate(const QString& What);
		QString getURLToUpdate(const QString& What, const QString& Id);
		QString getURLToDelete(const QString& What, const QString& Id);

	public slots:
		void finished( int id, bool error );

	private:
		QHttp Request;
		QString Web, User, Password;
		QByteArray Content;
		int Result;
		int Id;
		bool Error;
		QEventLoop Loop;
		bool Use04Api;
};

class DownloadReceiver : public QObject
{
	Q_OBJECT

	public:
		DownloadReceiver(QMainWindow* aWindow, QHttp& aRequest);

		bool go(const QString& url);
		QByteArray& content();

	public slots:
		void finished( int id, bool error );
		void transferred(int Now, int Total);
		void animate();

	private:
		QHttp& Request;
		QMainWindow* Main;
		QByteArray Content;
		int Id;
		bool OK;
		QProgressDialog* ProgressDialog;
};

bool downloadOSM(MainWindow* aParent, const CoordBox& aBox , MapDocument* theDocument);

bool checkForConflicts(MapDocument* theDocument);

#endif


