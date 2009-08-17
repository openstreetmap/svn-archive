/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * Minimalistic OSM parser.
  * Only handles the attributes required for this project and ignores everything else.
  */
#ifndef __OSM_PARSE_H__
#define __OSM_PARSE_H__

#include <QStringRef>
#include <QMap>
#include <QVector>
#include <QStringList>

#include "osmtypes.h"

class QFile;

/** Parses and stores all (relevant) information contained in an OSM file. */
class OsmData
{
    public:
        OsmData(OsmWayStorage *ways, OsmNodeStorage *nodes) {
            wayTags << "highway";
            this->nodes = nodes;
            this->ways = ways;
        }
        void parse(QString filename);
        void parse(QFile *file);
        /** Maps all node IDs for to their node objects. */
        OsmNodeStorage *nodes;
        /** List of all way objects. */
        OsmWayStorage *ways;
    protected:
        OsmWay currentWay;
        QStringList wayTags;
        void processTag(char *tag);
        void processParam(char *tag, char *name, char *value);
        OsmNodeId nodeid, noderef;
        OsmWayId wayid;
        float lat, lon;
        bool keep;
    /* For debugging / optimization only: */
    private:
        int kept, discarded, nodes_referenced, nodes_total;
};
#endif