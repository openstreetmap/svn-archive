// SegSplitter
// This class represents the process of splitting a segment.
// Because of the many different asynchronous HTTP requests going off, it
// makes sense to encapsulate the whole process in its own class.


#include "SegSplitter.h"

namespace OpenStreetMap
{

// splitSeg() is the method which kicks it off
// Parameters: a pointer to the segment to be split, and the EarthPoint to
// split it at
//
void SegSplitter::splitSeg(Segment *seg,const EarthPoint& p,int limit)
{
	bool liveUpdate = true;

	Way *w;
	// Get the node nearest the split point
	Node *n = components->getNearestNode(p.y,p.x,limit);

	// If there isn't an existing node there, add one 
	if(!n)
		n=components->addNewNode(p.y,p.x,"","node");

	// If the segment is in a way, get hold of the way and remove the
	// segment from it. Store the index of the segment in the way; the
	// split segments will need to be added at that position later.
	if(wayID=seg->getWayID())
	{
		w = components->getWayByID(wayID);
		wayIndex = w->removeSegment(seg);
	}

	// Actually break the segment; this returns a pair of the two new segments
	segments = components->breakSegment(seg,n);
	QString url = "/api/0.3/segment/0";

	// Upload the node to the server
	// This will call nodeAdded() when the response comes back
	if(n->getOSMID()<=0 && liveUpdate)
	{
		osmhttp->scheduleCommand("PUT","/api/0.3/node/0",n->toOSM(),
						this,
						SLOT(nodeAdded(const QByteArray&,void*)),
						n,SLOT(handleError(const QString&)));
	}

	// Delete the old segment
	if(liveUpdate)
	{
		url.sprintf("/api/0.3/segment/%d",seg->getOSMID());
		osmhttp->scheduleCommand("DELETE",url,this,NULL,NULL,
									SLOT(handleError(const QString&)));
	}	
	// If not in live update the only thing we need to do is add the
	// segments to the way
	else
	{
		if(wayID)
		{
			w->addSegmentAt(wayIndex,segments->second);
			w->addSegmentAt(wayIndex,segments->first);
		}
	}
}

// nodeAdded()
// called when the node at the break point has been added on the server
void SegSplitter::nodeAdded(const QByteArray& resp, void *node)
{
	Node *n = (Node*)node;
    QString str = resp;
    QStringList ids;

    if(!str.isNull())cerr<<"STR=" << str << endl;
    ids = QStringList::split("\n", str);

	// Set the node ID to the ID returned
    if(n)
    {
        n->setOSMID(atoi(ids[0].ascii()));

		// Call addSplitSegs() to add the two new segments
		addSplitSegs();
    }
}

// This method simply uploads the two new segments.
// The callback on receiving the response from the server is 
// splitSegAdded(), see below

void SegSplitter::addSplitSegs()
{

	QString a = segments->first->toOSM();
	QString b = segments->second->toOSM();


	osmhttp->scheduleCommand("PUT","/api/0.3/segment/0",
					segments->first->toOSM(), this,
							SLOT(splitSegAdded(const QByteArray&,void*)),
						segments->first,
						SLOT(handleError(const QString&)));

	osmhttp->scheduleCommand("PUT","/api/0.3/segment/0",
								segments->second->toOSM(), this,
								SLOT(splitSegAdded(const QByteArray&,void*)),
						segments->second,
						SLOT(handleError(const QString&)));
}

// splitSegAdded()
// Callback function from uploading the two new segments to the server
void SegSplitter::splitSegAdded(const QByteArray& resp, void *segment)
{
	// Set the ID
	Segment *seg = (Segment*) segment;
    QString str = resp;
    QStringList ids;
    ids = QStringList::split("\n", str);
    if(seg)
    {
        seg->setOSMID(atoi(ids[0].ascii()));
    }

	cerr << "segments->first->getOSMID():" << segments->first->getOSMID()<<endl;
	cerr << "segments->2nd->getOSMID():" << segments->second->getOSMID()<<endl;
	cerr << "wayID: " << wayID << endl;
	// This if will only be true after both segments have been uploaded 
	if(segments->first->getOSMID() && segments->second->getOSMID())
	{
		// If the original segment belonged in a way...
		if(wayID>0)
		{
			// get the way
			Way *w=components->getWayByID(wayID);

			// Add the two new segments to the way at the same place
			w->addSegmentAt(wayIndex,segments->second);
			w->addSegmentAt(wayIndex,segments->first);

			// Upload the altered way
			QString url;
			url.sprintf("/api/0.3/way/%d",w->getOSMID());
			osmhttp->scheduleCommand("PUT",url, w->toOSM(),this, 
							SLOT(finished(const QByteArray&,void*)),
							NULL,
							SLOT(handleError(const QString&)));
		}
		else
			emit done();
	}
}	

// We're done (at last!!!)
void SegSplitter::finished(const QByteArray&, void *)
{
	emit done();
}

SegSplitter::~SegSplitter()
{
	if(segments)
		delete segments;
}

void SegSplitter::handleHttpError(int i,const QString& e)
{
	QString eout;
	cerr << "HTTP error: " << i << " " << e << endl;
	eout.sprintf("HTTP error %d %s", i, e.ascii());
    emit error(eout);	
}

void SegSplitter::handleError(const QString& e)
{
	cerr << "Error: " << e << endl;
    emit error(e);	
}
}
