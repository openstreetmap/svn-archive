#ifndef BATCHUPLOADER_H
#define BATCHUPLOADER_H

#include "Components2.h"
#include "HTTPHandler.h"
//#include <q3cstring.h>

#include <map>

namespace OpenStreetMap
{

class BatchUploader : public QObject
{
Q_OBJECT

private:
	Components2 *components;
	HTTPHandler *osmhttp;
	std::map<int,Node*> nodes;
	int tp1, tp2, count;
	Way *way;
	bool successful;

public:
	BatchUploader(Components2* c) { components=c; way=new Way(c); 
   										successful=false;	}
	~BatchUploader();
	Way *getWay() { return way; }
	void setHTTPHandler(HTTPHandler *handler) { osmhttp=handler; }

	void batchUpload(int,int);

public slots:
	void nodeAdded(const QByteArray&,void*);
	void segmentAdded(const QByteArray&,void*);
	void handleHttpError(int i,const QString& e);
	void handleError(const QString& e);

signals:
	void done(Way *);
	void error(const QString&);
};

}

#endif // BATCHUPLOADER_H
