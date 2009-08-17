/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * The basic types (nodes, ways) and efficient storage for them.
  */
#ifndef __OSMTYPES_H__
#define __OSMTYPES_H__

#include <QStringRef>
#include <QVector>
#include <QMap>
#include <QDebug>
#include <QTemporaryFile>

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
  * easier to read for a human.
  */
class OsmNode
{
    public:
        /** Default constructor.
          * Creates an invalid node.
          * \note This function is required for QList<OsmNode>.
          */
        OsmNode() { lat_ = 0; lon_ = 0; }

        /** Constructor with initialisation. */
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

    private:
        float lat_, lon_;
};

/** Stores information about a way. */
class OsmWay
{
    public:

        /** Sets the way id */
        void setId(OsmWayId id_)
        {
            id = id_;
        }

        /** Add a node to this way. */
        void addNode(OsmNodeId nodeid)
        {
            nodes.append(nodeid);
        }

        /** Deletes all data and make this way object reusable. */
        void clear()
        {
            nodes.clear();
            id = 0;
        }

        /** Way id. */
        OsmWayId id;

        /** List of all node IDs that are part of this way. */
        QVector<OsmNodeId> nodes;
};


/** Provides optimized storage for OsmNode objects.
  * Can be configured to behave differently depending on the input
  * dataset size.
  * \note This is an abstract base class.
  */
class OsmNodeStorage
{
    public:
        virtual OsmNode& operator[](OsmNodeId id) = 0;
};

/** Node storage for a small amount of nodes. */
class OsmNodeStorageSmall: public OsmNodeStorage
{
    public:
        OsmNodeStorageSmall() { qDebug() << "Small node storage."; };
        virtual OsmNode& operator[](OsmNodeId id) { return nodes[id]; }
    private:
        QMap<OsmNodeId, OsmNode> nodes;
};

/** Node storage for a small amount of nodes. */
class OsmNodeStorageMedium: public OsmNodeStorage
{
    public:
        OsmNodeStorageMedium();
        virtual OsmNode& operator[](OsmNodeId id);
    private:
        QMap<int, OsmNode*> blocks; /*maps blocknr to block-array*/
        OsmNode dummyNode;
};

/** Node storage for a large amount of nodes. */
class OsmNodeStorageLarge: public OsmNodeStorage
{
    public:
        OsmNodeStorageLarge();
        virtual OsmNode& operator[](OsmNodeId id);
    private:
        OsmNode** nodes;
        QMap<OsmNodeId, OsmNode> negative_nodes;
        OsmNode dummyNode;
};

/** FIFO system storing ways. */
class OsmWayStorage
{
    public:
        virtual ~OsmWayStorage() {}
        virtual void append(OsmWay &input)=0;
        virtual void startReading() {};
        virtual bool get(OsmWay &output)=0;
};

class OsmWayStorageMem: public OsmWayStorage
{
    public:
        OsmWayStorageMem() { pos = 0; }
        virtual void append(OsmWay &input);
        virtual bool get(OsmWay &output);
    private:
        QVector<OsmWay *> ways;
        int pos;
};

class OsmWayStorageDisk: public OsmWayStorage
{
    public:
        OsmWayStorageDisk(QString tmp_dir);
        virtual ~OsmWayStorageDisk();
        virtual void append(OsmWay &input);
        virtual bool get(OsmWay &output);
        virtual void startReading();
    private:
        QDataStream stream;
        QTemporaryFile *file;
};

#endif
