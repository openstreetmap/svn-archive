/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * Create altitude relations from an OSMData object.
  */
#ifndef __RELATIONS_H__
#define __RELATIONS_H__

#include "osm-parse.h"
#include <math.h>

/** Maximum distance between sampling points. Half SRTM grid size. */
#define MAX_DIST 0.045

class QFile;
class SrtmDownloader;

/** Writes relations and calculates the neccessary data. */
class RelationWriter
{
    public:
        RelationWriter(OsmData *data, QIODevice *output, SrtmDownloader *downloader);
        void writeRelations();
    private:
        void processWay(OsmWay *way);
        void calcUpDown(float from_lat, float from_lon, float to_lat, float to_lon, float *up, float *down);
        void calc(OsmNodeId from, OsmNodeId to, float *length, float *up, float *down);
        void writeRelation(OsmWayId wayId, OsmNodeId startNode, OsmNodeId endNode,
            float length, float up, float down);
        //Inlined for performance reasons
        float distance(float from_lat, float from_lon, float to_lat, float to_lon) {
            //Distances are small => Assume earth is flat in this region to simplify calculation and avoid rounding errors that easily get very large with great circle distance calculations
            float delta_lat = (to_lat - from_lat) * 111.11; //TODO: Exact value
            float delta_lon = (to_lon - from_lon) * 111.11 * cos(to_lat/360.0*2.0*M_PI);
            return sqrt(delta_lat*delta_lat + delta_lon*delta_lon);
        }
        OsmData *data;
        QIODevice *output;
        SrtmDownloader *downloader;
        OsmRelationId relationId;
};

#endif
