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

static bool isDelim(char c)
{
    return (c == ' ') || (c == '<') || (c == '\t') || (c == '>') || (c == '/');
}

void OsmData::processTag(char *tag)
{
    //qDebug() << "tag" << tag;
    if (!strncmp(tag, "node", 5)) {
        //qDebug() << "node" << nodeid << lat << lon;
        nodes[nodeid] = OsmNode(lat, lon);
    } else if (!strncmp(tag, "way", 4)) {
        keep = true;
        currentWay = new OsmWay(wayid);
    } else if (!strncmp(tag, "nd", 3)) {
        currentWay->addNode(noderef);
    } else if (!strncmp(tag, "/way", 5)) {
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
//             discarded++;
        }
        currentWay = 0;
    }
}

void OsmData::processParam(char *tag, char *name, char *value)
{
    //qDebug() << "\tparam" << tag << name << value;
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
    }
}

/** Parse an OSM-XML file.
  * Stores information about the parsed data in the "nodes" and "ways" arrays.
  * \note This parser doesn't fail when the input data is invalid. It goes on
  *        reading and produces unpredictable results. However there should be
  *        no possiblity of a buffer overrun.
  */
void OsmData::parse(QFile *file)
{
    qDebug() << "Using new parser!";
    QDataStream stream(file);
    #define BUFFER_LEN 1024
    #define TAG_LEN 16
    #define PARAM_NAME_LEN 16
    #define PARAM_VALUE_LEN 16
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
            /*TODO: if ((state == state_waiting_for_param_end) && !isParamEnd(c)) continue;*/
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
                //if (c == '/') state = state_waiting_for_tag_start; //End tag
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
                    //qDebug() << "\tValue2:" << param_value;
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
}
