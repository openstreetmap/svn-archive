/** \file
  * Create altitude relations from an OSMData object.
  */
#include "relations.h"

#include "osm-parse.h"
#include "srtm.h"
 
#include <QIODevice>

/** Create a new relation writer object.
  * \param downloader Optional downloader object. If none is given
  *        a new one is constructed and initialized.*/
RelationWriter::RelationWriter(OsmData *data, QIODevice *output, SrtmDownloader *downloader)
{
    this->data = data;
    this->output = output;
    relationId = -1;
    if (!downloader) {
        downloader = new SrtmDownloader();
        downloader->loadFileList();
    }
    this->downloader = downloader;
}

/** Write relations to file. */
void RelationWriter::writeRelations()
{
    output->write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<osm version=\"0.6\" generator=\"SRTM2Wayinfo\">\n");
    foreach (OsmWay *way, data->ways) {
        processWay(way);
    }
    output->write("</osm>\n");
}

/** Process a single way.
  * Creates a new relation for every part of the way between to intersections.
  */
void RelationWriter::processWay(OsmWay *way)
{
    OsmNodeId startNode = 0, lastNode = 0; //No node 0 exists
    float length = 0, up = 0, down = 0;
    OsmNodeId id;
    foreach (id, way->nodes) {
        if (startNode == 0) {
            startNode = id;
            lastNode = id;
            continue;
        }
        calc(lastNode, id, &length, &up, &down);
        lastNode = id;
        if (data->nodes[id].isIntersection()) {
            writeRelation(way->id, startNode, id, length, up, down);
            startNode = id;
            length = 0;
            up = 0;
            down = 0;
        }
    }
    if (startNode != id) {
        //Write info for last segment of way
        writeRelation(way->id, startNode, id, length, up, down);
    }
}

/** Writes a single relation to file. */
void RelationWriter::writeRelation(OsmWayId wayId, OsmNodeId startNode, OsmNodeId endNode, float length, float up, float down)
{
    QString text =
        QString("<relation id=\"%1\" visible=\"true\">\n"
        "\t<member type=\"way\" ref=\"%2\" role=\"\"/>\n"
        "\t<member type=\"node\" ref=\"%3\" role=\"from\"/>\n"
        "\t<member type=\"node\" ref=\"%4\" role=\"to\"/>\n"
        "\t<tag k=\"length\" v=\"%5\"/>\n"
        "\t<tag k=\"up\" v=\"%6\"/>\n"
        "\t<tag k=\"down\" v=\"%7\"/>\n"
        "</relation>\n").arg(relationId--).arg(wayId).arg(startNode).arg(endNode).arg(length, 3, 'f').arg(up, 3, 'f').arg(down, 4, 'f');
    output->write(text.toAscii());
}

/** Calculates distance and altitude differences between two nodes. */
void RelationWriter::calc(OsmNodeId from, OsmNodeId to, float *length, float *up, float *down)
{
    *length += 1.0; //TODO
    float alt_from = downloader->getAltitudeFromLatLon(data->nodes[from].lat(), data->nodes[from].lon());
    float alt_to = downloader->getAltitudeFromLatLon(data->nodes[to].lat(), data->nodes[to].lon());
    if (alt_from == SRTM_DATA_VOID || alt_to == SRTM_DATA_VOID) return; //Do nothing when no data is available.
    float diff = alt_to - alt_from;
    if (diff > 0.0) {
        *up += diff;
    } else {
        *down -= diff;
    }
}