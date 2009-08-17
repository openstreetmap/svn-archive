/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * Minimalistic OSM parser (old and slow).
  * Only handles the attributes required for this project and ignores everything else.
  * \note This is a old version of the parser which uses QXmlStreamReader. It is therefore
  * standard compliant, but also takes much longer to parse the data (5 to 10 times slower).
  */
#include "osm-parse.h"

#include <QString>
#include <QFile>
#include <QDebug>
#include <QXmlStreamReader>

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
    int kept = 0, discarded = 0, nodes_referenced = 0;
    QXmlStreamReader xml(file);
    while (!xml.atEnd()) {
        xml.readNext();
        if (xml.isEndElement() && xml.name() == "way") {
            if (keep) {
                ways->append(currentWay);
                foreach(OsmNodeId nodeid, currentWay.nodes) {
                    (*nodes)[nodeid].incOrder();
                }
                nodes_referenced += currentWay.nodes.count();
                kept++;
            } else {
                discarded++;
            }
            continue;
        }

        if (!xml.isStartElement()) continue;

        i++;
        if ((i & 65535) == 0) qDebug() << i;
        if (xml.name() == "tag") {
            if (wayTags.contains(xml.attributes().value("k").toString())) {
                keep = true;
            }
            continue;
        }

        if (xml.name() == "node") {
            nodes_total++;
            OsmNodeId nodeid = xml.attributes().value("id").toString().toInt();
            (*nodes)[nodeid] = OsmNode(
                xml.attributes().value("lat").toString().toFloat(),
                xml.attributes().value("lon").toString().toFloat());
            continue;
        }

        if (xml.name() == "way") {
            keep = false;
            currentWay.clear();
            currentWay.setId(xml.attributes().value("id").toString().toInt());
        }

        if (xml.name() == "nd") {
            currentWay.addNode(xml.attributes().value("ref").toString().toInt());
        }
    }
    qDebug() << "Kept:" << kept << "Discarded:" << discarded <<  "Noderefs:" << nodes_referenced << "Nodes:" << nodes_total;
}
