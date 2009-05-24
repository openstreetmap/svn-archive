#include "osm-parse.h"
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

#define WAYS
void OsmData::parse(QFile *file)
{
    bool keep = false;
    int i = 0, kept = 0, discarded = 0, nodes_referenced = 0;
    QXmlStreamReader xml(file);
    qDebug() << "started parsing";
    while (!xml.atEnd()) {
        xml.readNext();
        //qDebug() << "read element" << xml.name().toString() << xml.errorString() << file->errorString();
		#ifdef WAYS
        if (xml.isEndElement() && xml.name() == "way") {
            if (keep) {
				currentWay->nodes.squeeze();
                ways.append(currentWay);
				nodes_referenced += currentWay->nodes.count();
                kept++;
            } else {
                delete currentWay;
                discarded++;
            }
            currentWay = 0;
            continue;
        }
		#endif

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

#ifdef WAYS
        if (xml.name() == "way") {
            keep = false;
            currentWay = new OsmWay(xml.attributes().value("id"));
        }

        if (xml.name() == "nd") {
            currentWay->addNode(xml.attributes().value("ref"));
        }
#endif
    }
    qDebug() << kept << discarded << nodes_referenced;
}

int main(void)
{
    OsmData data;
    data.parseFile("/dev/stdin");
    qDebug() << data.nodes.count() << data.ways.count();
	qDebug() << data.ways.capacity();
}
