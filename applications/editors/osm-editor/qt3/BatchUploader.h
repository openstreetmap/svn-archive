#ifndef BATCHUPLOADER_H
#define BATCHUPLOADER_H

#include "Components2.h"
#include "HTTPHandler.h"
#include <qcstring.h>

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

public:
	BatchUploader() {  }
	~BatchUploader() { }
	void setComponents(Components2 *comp) { components=comp; }
	void setHTTPHandler(HTTPHandler *handler) { osmhttp=handler; }

	void batchUpload(int,int);

public slots:
	void nodeAdded(const QByteArray&,void*);
	void segmentAdded(const QByteArray&,void*);
	void handleHttpError(int i,const QString& e);
	void handleError(const QString& e);

signals:
	void done();
	void error(const QString&);
};

}

#endif // BATCHUPLOADER_H
