#ifndef __OSM_PARSE_H__
#define __OSM_PARSE_H__

class QString;
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

class OsmNode
{
    public:
        OsmNode() { lat = 361; lon = 361; }
        OsmNode(QStringRef lat_ref, QStringRef lon_ref)
        {
            lat = lat_ref.toString().toFloat();
            lon = lon_ref.toString().toFloat();
        }
        float lat, lon;
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
    private:
            QStringList wayTags;
    public:
        OsmData() {
            wayTags << "highway";
        }
        void parseFile(QString filename);
        void parse(QFile *file);
        QMap<int, OsmNode> nodes;
        QVector<OsmWay *> ways;
    private:
        OsmWay *currentWay;
};
#endif