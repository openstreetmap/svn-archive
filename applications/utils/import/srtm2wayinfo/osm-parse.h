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
class QFile;

/** Typedef for node IDs. Allows easy identification of node IDs in code and changing to a different data type possible. */
typedef int OsmNodeId;
/** Typedef for way IDs. Allows easy identification of way IDs in code and changing to a different data type possible. */
typedef int OsmWayId;
/** Typedef for relation IDs. Allows easy identification of relation IDs in code and changing to a different data type possible. */
typedef int OsmRelationId;



/** Stores information about an OSM node.
  * Most functions should be defined in the header so the can be inlined
  * because they will be called very often.
  * This class should be optimized for storage size and can store different
  * values together. All data is accessed via functions to allow unpacking of
  * data.
  */
class OsmNode
{
    public:
        /** Default constructor.
          * Creates an invalid node.
          * \note This function is required for QList<OsmNode>.
          */
        OsmNode() { lat_ = 361; lon_ = 361; order = 0;}
        /** Constructor with initialisation.
          * Takes the QStringRefs from the XML parser and constructs a node. */
        OsmNode(QStringRef lat_ref, QStringRef lon_ref)
        {
            lat_ = lat_ref.toString().toFloat();
            lon_ = lon_ref.toString().toFloat();
            order = 0;
        }
        OsmNode(float lat, float lon)
        {
            lat_ = lat;
            lon_ = lon;
            order = 0;
        }
        /** Return latitude value. */
        float lat() { return lat_; }
        /** Return longitude value. */
        float lon() { return lon_; }
        /** Increase order of this node.
          * \note The counter is only required to count up to order 2, but is
          * allowed to count up to any value. */
        void incOrder() { order++; }
        /** Check if a node is an intersection. */
        bool isIntersection() { return order >= 2; }
    private:
        float lat_, lon_;
        uint order;
};

/** Stores information about a way. */
class OsmWay
{
    public:
        /** Create a new way. */
        OsmWay(QStringRef id_ref)
        {
            id = id_ref.toString().toInt();
        }

        OsmWay(OsmWayId id_)
        {
            id = id_;
        }

        /** Add a node to this way. */
        void addNode(QStringRef node_ref)
        {
            nodes.append(node_ref.toString().toInt());
        }

        void addNode(OsmNodeId nodeid)
        {
            nodes.append(nodeid);
        }

        /** Way id. */
        OsmWayId id;

        /** List of all node IDs that are part of this way. */
        QVector<OsmNodeId> nodes;
};

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