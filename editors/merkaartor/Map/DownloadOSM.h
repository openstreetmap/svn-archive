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
#include <QtCore/QObject>
#include <QtCore/QEventLoop>

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


