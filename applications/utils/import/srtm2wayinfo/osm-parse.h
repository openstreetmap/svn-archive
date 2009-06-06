#ifndef __OSM_PARSE_H__
#define __OSM_PARSE_H__

class QFile;

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
  */
class OsmNode
{
    public:
        OsmNode() { lat_ = 361; lon_ = 361; used = 0;}
        OsmNode(QStringRef lat_ref, QStringRef lon_ref)
        {
            lat_ = lat_ref.toString().toFloat();
            lon_ = lon_ref.toString().toFloat();
            used = 0;
        }
        float lat() { return lat_; }
        float lon() { return lon_; }
        void incUsageCounter() {used++;}
    private:
        float lat_, lon_;
        uint used;
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
        int id;
        QVector<int> nodes;
};

class OsmData
{
    public:
        OsmData() {
            wayTags << "highway";
        }
        void parse(QString filename);
        void parse(QFile *file);
        QMap<int, OsmNode> nodes;
        QVector<OsmWay *> ways;
    private:
        OsmWay *currentWay;
        QStringList wayTags;
};
#endif