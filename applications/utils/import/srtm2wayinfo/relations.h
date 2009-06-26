/** \file
  * Create altitude relations from an OSMData object.
  */
#ifndef __RELATIONS_H__
#define __RELATIONS_H__

#include "osm-parse.h"

class QFile;
class SrtmDownloader;

/** Writes relations and calculates the neccessary data. */
class RelationWriter
{
    public:
        RelationWriter(OsmData *data, QIODevice *output, SrtmDownloader *downloader = 0);
        void writeRelations();
    private:
        void processWay(OsmWay *way);
        void calc(OsmNodeId from, OsmNodeId to, float *length, float *up, float *down);
        void writeRelation(OsmWayId wayId, OsmNodeId startNode, OsmNodeId endNode,
            float length, float up, float down);
        OsmData *data;
        QIODevice *output;
        SrtmDownloader *downloader;
        OsmRelationId relationId;
};

#endif
