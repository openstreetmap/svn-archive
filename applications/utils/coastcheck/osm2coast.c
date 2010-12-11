/*
#-----------------------------------------------------------------------------
# osm2coast - extracts coastline data from planet.
# Use: osm2coast planet.osm.gz >coastline.osm
#-----------------------------------------------------------------------------
# by Martijn van Oosterhout 2007
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-----------------------------------------------------------------------------
*/

#define _GNU_SOURCE
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <getopt.h>
#include <errno.h>
#include <math.h>
#include <stdint.h>

#include <libxml/xmlstring.h>
#include <libxml/xmlreader.h>

#include "input.h"

#define MAX_NODE_ID (2000*1000*1000)
#define MAX_NODES_PER_WAY 2000

static int count_node,    max_node;
static int count_way,     max_way;
static int count_rel,     max_rel;

/* Since {node,way} elements are not nested we can guarantee the 
   values in an end tag must match those of the corresponding 
   start tag and can therefore be cached.
*/
static double node_lon, node_lat;
static int32_t *nds;
static int nd_count;
static unsigned char *bitmap;
int pass, wanted;
static int osm_id;

static void printStatus(void)
{
  if( isatty(STDERR_FILENO) )
    fprintf(stderr, "\rProcessing: Node(%dk) Way(%dk) Relation(%dk)",
            count_node/1000, count_way/1000, count_rel/1000);
}


void StartElement(xmlTextReaderPtr reader, const xmlChar *name)
{
    xmlChar *xid, *xlat, *xlon, *xk, *xv /*, *xrole, *xtype*/;

    if (xmlStrEqual(name, BAD_CAST "node")) {
        if( pass == 1 )
        {
            xid  = xmlTextReaderGetAttribute(reader, BAD_CAST "id");
            xlon = xmlTextReaderGetAttribute(reader, BAD_CAST "lon");
            xlat = xmlTextReaderGetAttribute(reader, BAD_CAST "lat");
            assert(xid); assert(xlon); assert(xlat);

            osm_id  = strtol((char *)xid, NULL, 10);
            node_lon = strtod((char *)xlon, NULL);
            node_lat = strtod((char *)xlat, NULL);

            if (osm_id > max_node)
                max_node = osm_id;

            xmlFree(xid);
            xmlFree(xlon);
            xmlFree(xlat);
        }
        count_node++;
        if (count_node%10000 == 0)
            printStatus();
    } else if (xmlStrEqual(name, BAD_CAST "tag")) {
        if( pass == 0 )
        {
            xk = xmlTextReaderGetAttribute(reader, BAD_CAST "k");
            xv = xmlTextReaderGetAttribute(reader, BAD_CAST "v");
            assert(xk);

            /* 'created_by' and 'source' are common and not interesting to mapnik renderer */
            if (strcmp((char *)xk, "natural")==0 && strcmp((char *)xv, "coastline")==0)
                wanted = 1;
            xmlFree(xv);
            xmlFree(xk);
        }
    } else if (xmlStrEqual(name, BAD_CAST "way")) {
        xid  = xmlTextReaderGetAttribute(reader, BAD_CAST "id");
        assert(xid);
        osm_id = strtol((char *)xid, NULL, 10);

        if (osm_id > max_way)
            max_way = osm_id;

        count_way++;
        if (count_way%1000 == 0)
            printStatus();

        wanted = 0;
        nd_count = 0;
        xmlFree(xid);
    } else if (xmlStrEqual(name, BAD_CAST "nd")) {
        xid  = xmlTextReaderGetAttribute(reader, BAD_CAST "ref");
        assert(xid);

        int id = strtol((char *)xid, NULL, 10);
        if( id > 0 && nd_count <= MAX_NODES_PER_WAY)
            nds[nd_count] = id;
        nd_count++;
        xmlFree(xid);
    } else if (xmlStrEqual(name, BAD_CAST "relation")) {
        count_rel++;
        if (count_rel%1000 == 0)
            printStatus();

    } else if (xmlStrEqual(name, BAD_CAST "member")) {
        /* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "osm")) {
        /* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "bound")) {
        /* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "changeset")) {
        /* ignore */
    } else {
        fprintf(stderr, "%s: Unknown element name: %s\n", __FUNCTION__, name);
    }
}

void EndElement(const xmlChar *name)
{
    if (xmlStrEqual(name, BAD_CAST "node")) {
        if( pass == 1 )
        {
            if( osm_id > MAX_NODE_ID )
            {
              fprintf( stderr, "Exceeded maximum node ID: %d\n", MAX_NODE_ID );
              exit(1);
            }
            if( bitmap[ osm_id >> 3 ] & (1<<(osm_id&7)) )
                printf( "<node id=\"%d\" lat=\"%f\" lon=\"%f\" />\n", osm_id, node_lat, node_lon );
        }
    } else if (xmlStrEqual(name, BAD_CAST "way")) {
        if( nd_count > MAX_NODES_PER_WAY )
        {
            fprintf(stderr, "Exceeded maximum node count (%d > %d) way=%d\n", nd_count, MAX_NODES_PER_WAY, osm_id );
        }

        if( pass == 0 && wanted )
        {
            int i;
            printf( "<way id=\"%d\">\n", osm_id );
            for( i=0; i<nd_count; i++ )
            {
                if( nds[i] > MAX_NODE_ID )
                {
                  fprintf( stderr, "Exceeded maximum node ID: %d\n", MAX_NODE_ID );
                  exit(1);
                }
                bitmap[ nds[i] >> 3 ] |= (1<<(nds[i]&7));
                printf( "<nd ref=\"%d\" />\n", nds[i] );
            }
            printf( "</way>\n" );
        }
    } else if (xmlStrEqual(name, BAD_CAST "relation")) {
        /* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "tag")) {
        /* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "nd")) {
        /* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "member")) {
	/* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "osm")) {
        printStatus();
    } else if (xmlStrEqual(name, BAD_CAST "bound")) {
        /* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "changeset")) {
        /* ignore */
    } else {
        fprintf(stderr, "%s: Unknown element name: %s\n", __FUNCTION__, name);
    }
}

static void processNode(xmlTextReaderPtr reader) {
    xmlChar *name;
    name = xmlTextReaderName(reader);
    if (name == NULL)
        name = xmlStrdup(BAD_CAST "--");
	
    switch(xmlTextReaderNodeType(reader)) {
        case XML_READER_TYPE_ELEMENT:
            StartElement(reader, name);
            if (xmlTextReaderIsEmptyElement(reader))
                EndElement(name); /* No end_element for self closing tags! */
            break;
        case XML_READER_TYPE_END_ELEMENT:
            EndElement(name);
            break;
        case XML_READER_TYPE_SIGNIFICANT_WHITESPACE:
            /* Ignore */
            break;
        default:
            fprintf(stderr, "Unknown node type %d\n", xmlTextReaderNodeType(reader));
            break;
    }

    xmlFree(name);
}

static int streamFile(char *filename) {
    xmlTextReaderPtr reader;
    int ret = 0;

    reader = inputUTF8(filename);

    if (reader != NULL) {
        ret = xmlTextReaderRead(reader);
        while (ret == 1) {
            processNode(reader);
            ret = xmlTextReaderRead(reader);
        }

        if (ret != 0) {
            fprintf(stderr, "%s : failed to parse\n", filename);
            return ret;
        }

        xmlFreeTextReader(reader);
    } else {
        fprintf(stderr, "Unable to open %s\n", filename);
        return 1;
    }
    return 0;
}

void exit_nicely(void)
{
    fprintf(stderr, "Error occurred, cleaning up\n");
    exit(1);
}
 
static void usage(const char *arg0)
{
    const char *name = basename(arg0);

    fprintf(stderr, "Usage:\n");
    fprintf(stderr, "\t%s planet.osm.gz >coastline.osm\n", name);
    fprintf(stderr, "\n");
}

int main(int argc, char *argv[])
{
    fprintf(stderr, "osm2coast SVN version %s $Rev: 4895 $ \n", VERSION);

    if (argc != 2) {
        usage(argv[0]);
        exit(EXIT_FAILURE);
    }
    
    nice(10);

    LIBXML_TEST_VERSION

    nds = malloc( sizeof(nds[0]) * (MAX_NODES_PER_WAY + 1) );
    bitmap = calloc( MAX_NODE_ID /8 + 1, 1 );
    
    count_node = max_node = 0;
    count_way = max_way = 0;
    count_rel = max_rel = 0;

    printf("<osm version=\"0.5\">\n");
    fprintf(stderr, "\nReading in file: %s\n", argv[1]);
    if (streamFile(argv[1]) != 0)
        exit_nicely();

    pass = 1;
    
    count_node = max_node = 0;
    count_way = max_way = 0;
    count_rel = max_rel = 0;

    fprintf(stderr, "\nReading in file: %s\n", argv[1]);
    if (streamFile(argv[1]) != 0)
        exit_nicely();

    /* Try to detect output error */
    if( printf("</osm>\n") < 0 )
      exit(1);
    xmlCleanupParser();
    xmlMemoryDump();

    if (count_node || count_way || count_rel) {
        fprintf(stderr, "\n");
        fprintf(stderr, "Node stats: total(%d), max(%d)\n", count_node, max_node);
        fprintf(stderr, "Way stats: total(%d), max(%d)\n", count_way, max_way);
        fprintf(stderr, "Relation stats: total(%d), max(%d)\n", count_rel, max_rel);
    }

    return 0;
}

