/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * Minimalistic OSM parser.
  * Only handles the attributes required for this project and ignores everything else.
  */
#include "osm-parse.h"

#include <QString>
#include <QFile>
#include <QDebug>

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

/** Checks if c is a character that ends a parameter value. */
static inline bool isDelim(char c)
{
    return (c == ' ') || (c == '<') || (c == '\t') || (c == '>') || (c == '/');
}

// NOTE: These defines are for debugging only.
// #define NO_NODES
// #define NO_WAYS
// #define STOP_AT_FIRST_WAY


void OsmData::processTag(char *tag)
{
    #ifndef NO_NODES
    if (!strncmp(tag, "node", 5)) {
        (*nodes)[nodeid] = OsmNode(lat, lon);
        nodes_total++;
        if (!(nodes_total & 0x3ffff)) qDebug() << "Nodes:" << nodes_total;
    }
    #else
    #warning Parsing of nodes is disabled.
    #endif
    
    #ifndef NO_WAYS
//     else
    if (!strncmp(tag, "way", 4)) {
        keep = false;
        currentWay.clear();
        currentWay.setId(wayid);
    } else if (!strncmp(tag, "nd", 3)) {
        currentWay.addNode(noderef);
    } else if (!strncmp(tag, "/way", 5)) {
        if (keep) {
            ways->append(currentWay);
            foreach(OsmNodeId nodeid, currentWay.nodes) {
                (*nodes)[nodeid].incOrder();
            }
            nodes_referenced += currentWay.nodes.count();
            kept++;
        } else {
            //The way object will be reused
            discarded++;
        }
    }
    #else
    #warning Parsing of ways is disabled.
    #ifdef STOP_AT_FIRST_WAY
        if (!strncmp(tag, "way", 4)) {
            exit(0);
        }
    #endif
    #endif
}

void OsmData::processParam(char *tag, char *name, char *value)
{
    if (!strcmp("lat", name)) {
        lat = atof(value);
    } else if (!strcmp("lon", name)) {
        lon = atof(value);
    } else if (!strcmp("ref", name)) {
        noderef = atoi(value);
    } else if (!strcmp("id", name)) {
        if (!strcmp("node", tag)) {
            nodeid = atoi(value);
        } else {
            wayid = atoi(value);
        }
    } else if (!strcmp("k", name) && !strcmp("highway", value)) {
        keep = true;
    }
}

/** Length of the main read buffer. */
#define BUFFER_LEN 1024
/** Maximum tag name length. Longer values are truncated.*/
#define TAG_LEN 16
/** Maximum parameter name length. Longer values are truncated.*/
#define PARAM_NAME_LEN 16
/** Maximum parameter value length. Longer values are truncated. */
#define PARAM_VALUE_LEN 16

/** Parse an OSM-XML file.
  * Stores information about the parsed data in the "nodes" and "ways" arrays.
  * \note This parser doesn't fail when the input data is invalid. It goes on
  *        reading and produces unpredictable results. However there should be
  *        no possiblity of a buffer overrun.
  */
void OsmData::parse(QFile *file)
{
    kept = discarded = nodes_referenced = nodes_total = 0;
    QDataStream stream(file);
    char buffer[BUFFER_LEN];
    char tag[TAG_LEN];
    char param_name[PARAM_NAME_LEN];
    char param_value[PARAM_VALUE_LEN];
    int tag_pos, name_pos, value_pos;
    int count;
    bool inside_quotes;
    enum {
        state_waiting_for_tag_start,
        state_waiting_for_tag_end,
        state_waiting_for_param_end,
        state_tag,
        state_param_name,
        state_param_value,
    } state;

    do {
        count = stream.readRawData(buffer, BUFFER_LEN);
        int i;
        for (i = 0; i < count; i++) {
            char c = buffer[i];
            //Shortcuts to keep processing time low
            if ((state == state_waiting_for_tag_start) && (c != '<')) continue;
            if ((state == state_waiting_for_tag_end) && (c != '>')) continue;
            if (c == '<') {
                //A < always starts a new tag (except in CDATA areas which we don't have in OSM data)
                state = state_tag;
                tag_pos = 0;
                continue;
            }
            if (c == '>') {
                if (tag_pos) {
                    tag[tag_pos] = 0;
                }
                if (value_pos) { //Value is terminated by this end-of-tag-char
                    param_value[value_pos] = 0;
                    //qDebug() << "\tValue1:" << param_value;
                    processParam(tag, param_name, param_value);
                }
                processTag(tag);
                state = state_waiting_for_tag_start;
            }
            /******************************************************************/
            if (state == state_tag) {
                if (isblank(c)) {
                    if (!tag_pos) continue; //Tag text has not started yes
                    else {
                        state = state_param_name;
                        name_pos = 0;
                        tag[tag_pos] = 0;
                        tag_pos = 0;
                        //qDebug() << "Tag:" << tag;
                    }
                } else if (tag_pos < TAG_LEN-1) {
                    tag[tag_pos++] = c;
                }
            }
            /******************************************************************/
            if (state == state_param_name) {
                if ((c == '"') || (c == '\'') || isblank(c)) continue; //Ignore some chars
                if (c == '=') {
                    //Name ended
                    state = state_param_value;
                    value_pos = 0;
                    inside_quotes = false;
                    param_name[name_pos] = 0;
                    //qDebug() << "\tParam:" << param_name;
                } else if (name_pos < PARAM_NAME_LEN-1) {
                    param_name[name_pos++] = c;
                }
            }
            /******************************************************************/
            if (state == state_param_value) {
                if (c == '"' || c == '\'') {
                    inside_quotes = !inside_quotes;
                    continue;
                }
                if (!inside_quotes && isDelim(c)) {
                    param_value[value_pos] = 0;
                    state = state_param_name;
                    name_pos = 0;
                    processParam(tag, param_name, param_value);
                    value_pos = 0;
                    continue;
                }
                if ((c == '=' || isblank(c))) continue;
                if (value_pos < PARAM_VALUE_LEN-1) {
                    param_value[value_pos++] = c;
                }
            }
        }
    }
    while (count > 0);
    qDebug() << "Kept:" << kept << "Discarded:" << discarded <<  "Noderefs:" << nodes_referenced << "Nodes:" << nodes_total;
}
