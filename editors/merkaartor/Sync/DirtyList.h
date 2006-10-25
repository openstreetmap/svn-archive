#ifndef MERKATOR_DIRTYLIST_H_
#define MERKATOR_DIRTYLIST_H_

class MapDocument;
class MapFeature;
class Road;
class TrackPoint;
class Way;

class QProgressDialog;
class QWidget;

#include "GeneratedFiles/ui_SyncListDialog.h"

#include <QtCore/QObject>
#include <QtCore/QString>

#include <vector>

class DirtyList : public QObject
{
	Q_OBJECT

	public:
		DirtyList(MapDocument* aDoc, const QString& aWeb, const QString& aUser, const QString& aPwd);
		~DirtyList(void);

		bool isAdded(MapFeature* F);
		bool isAdded(Way* W);
		bool isAdded(TrackPoint* Pt);
		bool isAdded(Road* R);
		bool isChanged(MapFeature* F);
		bool isChanged(Way* W);
		bool isChanged(TrackPoint* Pt);
		bool isChanged(Road* R);
		bool showChanges(QWidget* Parent);
		bool executeChanges(QWidget* Parent);

	private slots:
		void on_Request_finished(int id, bool);
	private:
		void describeAdded(Way* W);
		void describeAdded(TrackPoint* Pt);
		void describeAdded(Road* R);
		void describeChanged(Way* W);
		void describeChanged(TrackPoint* Pt);
		void describeChanged(Road* R);
		bool executeAdded(Way* W);
		bool executeAdded(TrackPoint* Pt);
		bool executeAdded(Road* R);
		bool executeChanged(Way* W);
		bool executeChanged(TrackPoint* Pt);
		bool executeChanged(Road* R);

		bool isUpdated(MapFeature* F);
		bool sendRequest(const QString& URL, const QString& Out, QString& Rcv);

		MapDocument* theDocument;
		Ui::SyncListDialog Ui;
		std::vector<MapFeature*> Added, Changed;
		bool IsExecuting;
		QProgressDialog* Progress;
		unsigned int Task;
		QString Web,User,Pwd;
		bool Finished;
		bool FinishedError;
		int FinishedId;
};

#endif


