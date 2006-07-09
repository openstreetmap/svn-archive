#include "BatchUploader.h"
#include "Segment.h"

namespace OpenStreetMap
{

// This method uploads a selected section of the GPX track as OSM nodes and
// segments.
// Loop through all trackpoints, make a node and upload each one
// After loading nodes, create and upload the segments between them.
void BatchUploader::batchUpload(int tp1, int tp2)
{
	this->tp1 = tp1;
	this->tp2 = tp2;
	count = tp1;

	TrackPoint *tp = components->getTrackPoint(tp1);

	// Make a node from the current trackpoint, add to components
	nodes[count] = components->addNewNode 
			(tp->getLat(), tp->getLon(), "", "node");

	QObject::connect(osmhttp,SIGNAL(httpErrorOccurred(int,const QString&)),
						this,SLOT(handleHttpError(int,const QString&)));
	QObject::connect(osmhttp,SIGNAL(errorOccurred(const QString&)),
						this,SLOT(handleError(const QString&)));

	osmhttp->scheduleCommand("PUT","/api/0.3/node/0",nodes[count]->toOSM(),
								this,
								SLOT(nodeAdded(const QByteArray&,void*)),
								nodes[count],SLOT(handleError(const QString&)));
}

// nodeAdded()
void BatchUploader::nodeAdded(const QByteArray& resp, void *node)
{
	cerr << "BatchUploader::nodeAdded()" << endl;
	Node *n = (Node*)node;
    QString str = resp;
    QStringList ids;

    if(!str.isNull())cerr<<"STR=" << str << endl;
    ids = QStringList::split("\n", str);

	// Set the node ID to the ID returned
    if(n)
    {
		cerr << "node ID: " << ids[0] << endl;
        n->setOSMID(atoi(ids[0].ascii()));

		cerr << "count=" << count << endl;

		if(count != tp1)
		{
			cerr << "NOT FIRST NODE - SO UPLOADING A SEGMENT" << endl;
			if(nodes[count-1] && nodes[count])
			{
				Segment *segx =
					components->addNewSegment(nodes[count-1],nodes[count]);

				osmhttp->scheduleCommand
						("PUT","/api/0.3/segment/0",segx->toOSM(),
							this, SLOT(segmentAdded(const QByteArray&,void*)),
							segx, SLOT(handleError(const QString&)));
			}
		}
		else
		{
			cerr << "FIRST NODE - SO UPLOADING ANOTHER NODE" << endl;
			TrackPoint *tp = components->getTrackPoint(++count);

			// Make a node from the current trackpoint, add to components
			nodes[count] = components->addNewNode 
					(tp->getLat(), tp->getLon(), "", "node");
	
			osmhttp->scheduleCommand
					("PUT","/api/0.3/node/0",nodes[count]->toOSM(), this,
								SLOT(nodeAdded(const QByteArray&,void*)),
								nodes[count],SLOT(handleError(const QString&)));
		}

    }
}

void BatchUploader::segmentAdded(const QByteArray& resp, void *segment)
{
	cerr << "BatchUploader::segmentAdded()" << endl;

	Segment *s = (Segment*)segment;
    QString str = resp;
    QStringList ids;

    ids = QStringList::split("\n", str);

	// Set the node ID to the ID returned
    if(s)
    {
		s->setOSMID(atoi(ids[0].ascii()));
		cerr << "semgnet ID: " << ids[0] << endl;
		if(count<tp2)
		{
			TrackPoint *tp = components->getTrackPoint(++count);


			nodes[count] = components->addNewNode 
				(tp->getLat(), tp->getLon(), "", "node");
			osmhttp->scheduleCommand
					("PUT","/api/0.3/node/0",nodes[count]->toOSM(), this,
								SLOT(nodeAdded(const QByteArray&,void*)),
								nodes[count],SLOT(handleError(const QString&)));
		}
		else
			emit done();
    }
}

void BatchUploader::handleHttpError(int i,const QString& e)
{
	QString eout;
	cerr << "HTTP error: " << i << " " << e << endl;
	eout.sprintf("HTTP error %d %s", i, e.ascii());
    emit error(eout);	
}

void BatchUploader::handleError(const QString& e)
{
	cerr << "Error: " << e << endl;
    emit error(e);	
}

}
