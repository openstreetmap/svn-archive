/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * Create altitude relations from an OSMData object.
  */
#include "relations.h"
#include "osm-parse.h"
#include "srtm.h"

#include <QIODevice>

/** Create a new relation writer object. */
RelationWriter::RelationWriter(OsmData *data, QIODevice *output, SrtmDownloader *downloader)
{
    this->data = data;
    this->output = output;
    relationId = -1;
    this->downloader = downloader;
}

/** Write relations to file. */
void RelationWriter::writeRelations()
{
    output->write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<osm version=\"0.6\" generator=\"SRTM2Wayinfo\">\n");
    OsmWay way;
    data->ways->startReading();
    while (data->ways->get(way)) {
        processWay(&way);
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
        if ((*data->nodes)[id].isIntersection()) {
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
    //Note: QString::arg is not locale dependent except if the parameter is written as %L1
    QString text =
        QString("<relation id=\"%1\" visible=\"true\">\n"
        "\t<member type=\"way\" ref=\"%2\" role=\"\"/>\n"
        "\t<member type=\"node\" ref=\"%3\" role=\"from\"/>\n"
        "\t<member type=\"node\" ref=\"%4\" role=\"to\"/>\n"
        "\t<tag k=\"length\" v=\"%5\"/>\n"
        "\t<tag k=\"up\" v=\"%6\"/>\n"
        "\t<tag k=\"down\" v=\"%7\"/>\n"
        "\t<tag k=\"type\" v=\"altitude\">\n"
        "</relation>\n").arg(relationId--).arg(wayId).arg(startNode).arg(endNode).arg(length, 3, 'f').arg(up, 3, 'f').arg(down, 4, 'f');
    output->write(text.toAscii());
}

/** Calculates distance and altitude differences between two nodes. */
void RelationWriter::calc(OsmNodeId from, OsmNodeId to, float *length, float *up, float *down)
{
    *length += distance((*data->nodes)[from].lat(), (*data->nodes)[from].lon(), (*data->nodes)[to].lat(), (*data->nodes)[to].lon());
    calcUpDown((*data->nodes)[from].lat(), (*data->nodes)[from].lon(), (*data->nodes)[to].lat(), (*data->nodes)[to].lon(), up, down);
}

/** Calculate the altitude differences.
  *
  * If the distance between two nodes is longer than the average distance between 2 SRTM points
  * the segment is split recursively until it is shorter than this distance.
  */
void RelationWriter::calcUpDown(float from_lat, float from_lon, float to_lat, float to_lon, float *up, float *down)
{
    float dist = distance(from_lat, from_lon, to_lat, to_lon);
    if (dist < MAX_DIST) {
        float alt_from = downloader->getAltitudeFromLatLon(from_lat, from_lon);
        float alt_to   = downloader->getAltitudeFromLatLon(to_lat, to_lon);
        if (alt_from == SRTM_DATA_VOID || alt_to == SRTM_DATA_VOID) return; //Do nothing when no data is available.
        float diff = alt_to - alt_from;
        if (diff > 0.0) {
            *up += diff;
        } else {
            *down -= diff;
        }
    } else {
        float center_lat = (from_lat + to_lat) / 2;
        float center_lon = (from_lon + to_lon) / 2;
        //Go on recursively
        calcUpDown(from_lat, from_lon, center_lat, center_lon, up, down);
        calcUpDown(center_lat, center_lon, to_lat, to_lon, up, down);
    }
}

