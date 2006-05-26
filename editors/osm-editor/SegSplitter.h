#ifndef SEGSPLITTER_H
#define SEGSPLITTER_H

#include "Components2.h"
#include "HTTPHandler.h"
#include <qcstring.h>

#include <utility>

namespace OpenStreetMap
{

class SegSplitter : public QObject
{
Q_OBJECT

private:
	Components2 *components;
	HTTPHandler *osmhttp;
	std::pair<Segment*,Segment*> * segments;
	int wayID, wayIndex;

public:
	SegSplitter() { segments=NULL; }
	~SegSplitter();
	void setComponents(Components2 *comp) { components=comp; }
	void setHTTPHandler(HTTPHandler *handler) { osmhttp=handler; }

	void splitSeg(Segment*,const EarthPoint&,int);
	void addSplitSegs();

public slots:
	void nodeAdded(const QByteArray&,void*);
	void splitSegAdded(const QByteArray&,void*);
	void finished(const QByteArray&,void*);

signals:
	void done();
};

}

#endif // SEGSPLITTER_H
