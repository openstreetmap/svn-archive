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
        OsmData() {
            wayTags << "highway";
        }
        void parse(QString filename);
        void parse(QFile *file);
        /** Maps all node IDs for to their node objects. */
        QMap<OsmNodeId, OsmNode> nodes;
        /** List of all way objects. */
        QVector<OsmWay *> ways;
    private:
        OsmWay *currentWay;
        QStringList wayTags;
        void processTag(char *tag);
        void processParam(char *tag, char *name, char *value);
        OsmNodeId nodeid, noderef;
        OsmWayId wayid;
        float lat, lon;
        bool keep;
    private:
        int kept, discarded, nodes_referenced;
};
#endif