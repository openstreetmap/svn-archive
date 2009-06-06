#ifndef __OSM_PARSE_H__
#define __OSM_PARSE_H__

/** \file
  * Minimalistic OSM parser.
  * Only handles the attributes required for this project and ignores everything else.
  */

class QFile;

typedef int OsmNodeId;
typedef int OsmWayId;
typedef int OsmRelationId;

#include <QObject>
#include <QStringRef>
#include <QList>
#include <QMap>
#include <QHash>
#include <QVector>
#include <QStringList>
#include <QXmlStreamReader>
#include <QLinkedList>

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

class OsmWay
{
    public:
        OsmWay(QStringRef id_ref)
        {
            id = id_ref.toString().toInt();
        }
        
        void addNode(QStringRef node_ref)
        {
            nodes.append(node_ref.toString().toInt());
        }
        OsmWayId id;
        QVector<OsmNodeId> nodes;
};

class OsmData
{
    public:
        OsmData() {
            wayTags << "highway";
        }
        void parse(QString filename);
        void parse(QFile *file);
        QMap<OsmNodeId, OsmNode> nodes;
        QVector<OsmWay *> ways;
    private:
        OsmWay *currentWay;
        QStringList wayTags;
};
#endif