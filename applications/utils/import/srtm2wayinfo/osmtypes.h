#ifndef __OSMTYPES_H__
#define __OSMTYPES_H__

#include <QStringRef>
#include <QVector>

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
  *
  * We only want to know if a node is a junction. Therefore we can stop counting at
  * order 2. Data about the order is stored in the lat value.
  * Normal lat values: -90° to +90°         (+  0°)
  * 1st order node:    110° to 290°         (+200°)
  * 2nd order node:    310° to 490°         (+400°)
  * I choose 200° instead of 180° to avoid corner cases and because the values are
  * easier to compute for a human.
  */
class OsmNode
{
    public:
        /** Default constructor.
          * Creates an invalid node.
          * \note This function is required for QList<OsmNode>.
          */
        OsmNode() { lat_ = 0; lon_ = 0; }
        /** Constructor with initialisation.
          * Takes the QStringRefs from the XML parser and constructs a node. */
        OsmNode(QStringRef lat_ref, QStringRef lon_ref)
        {
            lat_ = lat_ref.toString().toFloat();
            lon_ = lon_ref.toString().toFloat();
        }
        OsmNode(float lat, float lon)
        {
            lat_ = lat;
            lon_ = lon;
        }
        /** Return latitude value. */
        float lat() {
            if (lat_ <= 90.0) {
                return lat_;
            } else if (lat_ <= 290.0) {
                return lat_ - 200.0;
            } else {
                return lat_ - 400.0;
            }
        }
        /** Return longitude value. */
        float lon() { return lon_; }
        /** Increase order of this node.
          * \note The counter is only required to count up to order 2, but is
          * allowed to count up to any value. */
        void incOrder() {
            if (lat_ <= 290.0) lat_ += 200.0;
        }
        /** Check if a node is an intersection. */
        bool isIntersection() { return lat_ > 290.0; }


        /*void *operator new(size_t size);
        void *operator new(size_t size, OsmNode* node);
        void operator delete(void *p);*/
    private:
        float lat_, lon_;
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

#endif
