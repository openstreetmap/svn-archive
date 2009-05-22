
class QString;
class QFile;
#include <QObject>
#include <QStringRef>
#include <QList>
#include <QMap>
#include <QStringList>
#include <QXmlStreamReader>

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
            id = id_ref.toString();
        }
        
        void addNode(QStringRef node_ref)
        {
            nodes.append(node_ref.toString().toInt());
        }
        QString id;
        QList<int> nodes;
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
        QList<OsmWay *> ways;
    private:
        OsmWay *currentWay;
};


//////////7

#include <QString>
#include <QFile>
#include <QDebug>

void OsmData::parseFile(QString filename)
{
    QFile f(filename);
    f.open(QIODevice::ReadOnly);
    parse(&f);
    f.close();
}

void OsmData::parse(QFile *file)
{
    bool keep = false;
    int i = 0, kept=0, discarded=0;
    QXmlStreamReader xml(file);
    qDebug() << "started parsing";
    while (!xml.atEnd()) {
        xml.readNext();
        //qDebug() << "read element" << xml.name().toString() << xml.errorString() << file->errorString();
        if (xml.isEndElement() && xml.name() == "way") {
            if (keep) {
                ways.append(currentWay);
                kept++;
            } else {
                delete currentWay;
                discarded++;
            }
            currentWay = 0;
            continue;
        }

        if (!xml.isStartElement()) continue;

        i++;
        if ((i & 65535) == 0) qDebug() << i;
        if (xml.name() == "tag") {
            if (currentWay && wayTags.contains(xml.attributes().value("k").toString())) {
                keep = true;
            }
            continue;
        }

        if (xml.name() == "node") {
            nodes[xml.attributes().value("id").toString().toInt()] = OsmNode(
                xml.attributes().value("lat"),
                xml.attributes().value("lon"));
            continue;
        }

        if (xml.name() == "way") {
            keep = false;
            currentWay = new OsmWay(xml.attributes().value("id"));
        }

        if (xml.name() == "nd") {
            currentWay->addNode(xml.attributes().value("ref"));
        }
    }
    qDebug() << kept << discarded;
}

int main(void)
{
    OsmData data;
    data.parseFile("/dev/stdin");
    qDebug() << data.nodes.count() << data.ways.count();
}
