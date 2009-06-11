/** \file
  * Minimalistic OSM parser.
  * Only handles the attributes required for this project and ignores everything else.
  */
#include "osm-parse.h"

#include <QString>
#include <QFile>
#include <QDebug>
#include <stdio.h>

/** Parse an OSM-XML file.
  * This function is just a wrapper that creates an QFile object
  * and calls parse(QFile *).
  */
void OsmData::parse(QString filename)
{
    QFile f(filename);
    f.open(QIODevice::ReadOnly);
    parse(&f);
    f.close();
}

/** Parse an OSM-XML file.
  * Stores information about the parsed data in the "nodes" and "ways" arrays.
  */
void OsmData::parse(QFile *file)
{
    bool keep = false;
    int i = 0;
    //kept = 0, discarded = 0, nodes_referenced = 0;
    QXmlStreamReader xml(file);
    while (!xml.atEnd()) {
        xml.readNext();
        //qDebug() << "read element" << xml.name().toString() << xml.errorString() << file->errorString();
        if (xml.isEndElement() && xml.name() == "way") {
            if (keep) {
                currentWay->nodes.squeeze();
                ways.append(currentWay);
                foreach(OsmNodeId nodeid, currentWay->nodes) {
                    nodes[nodeid].incOrder();
                }
                //nodes_referenced += currentWay->nodes.count();
                //kept++;
            } else {
                delete currentWay;
                //discarded++;
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
            OsmNodeId nodeid = xml.attributes().value("id").toString().toInt();
            nodes[nodeid] = OsmNode(
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
    //qDebug() << kept << discarded << nodes_referenced;
}
