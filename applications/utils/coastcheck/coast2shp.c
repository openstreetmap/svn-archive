/*
#-----------------------------------------------------------------------------
# coast2shp - converts list of polygons into shapefilesfile into PostgreSQL
# compatible output suitable to be rendered by mapnik
# Use: coast2shp polygonlist coastline.osm.gz
#-----------------------------------------------------------------------------
# Based upon osm2pgsql:
#   Original Python implementation by Artem Pavlenko
#   Re-implementation by Jon Burgess, Copyright 2006
# Reused for coast2shp by Martijn van Oosterhout 2007-2008
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

#include <libxml/xmlstring.h>
#include <libxml/xmlreader.h>

#include <shapefil.h>
#include <proj_api.h>

#include "osmtypes.h"
#include "keyvals.h"
#include "input.h"
#include "rb.h"

/* Mercator projection limits for a square map.
 * Points at 90 degrees are off at infinity
 */
const double merc_lat_min = -85.0511;
const double merc_lat_max = +85.0511;

int MAX_VERTICES = 1024*1024;

static int count_node,    max_node;
static int count_way,     max_way;
static int count_rel,     max_rel;

/* Since {node,way} elements are not nested we can guarantee the 
   values in an end tag must match those of the corresponding 
   start tag and can therefore be cached.
*/
static double node_lon, node_lat;
static struct keyval nds;
static int osm_id;
struct rb_table *nodes_table;
struct rb_table *ways_table;

int verbose;
int latlong;

static projPJ pj_ll, pj_merc;

static void project_init(void)
{
        pj_ll   = pj_init_plus("+proj=latlong +ellps=GRS80 +no_defs +over");
        pj_merc = pj_init_plus("+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs +over");

        if (!pj_ll || !pj_merc) {
                fprintf(stderr, "Projection code failed to initialise\n");
                exit(1);
        }
}

static void project_exit(void)
{
        pj_free(pj_ll);
        pj_ll = NULL;
        pj_free(pj_merc);
        pj_merc = NULL;
}

static void printStatus(void)
{
    if( isatty(STDERR_FILENO) )
      fprintf(stderr, "\rProcessing: Node(%dk) Way(%dk) Relation(%dk)    ",
              count_node/1000, count_way/1000, count_rel/1000);
}


void StartElement(xmlTextReaderPtr reader, const xmlChar *name)
{
    xmlChar *xid, *xlat, *xlon /**xk, *xv, *xrole, *xtype*/;
//    char *k;

    if (xmlStrEqual(name, BAD_CAST "node")) {
        xid  = xmlTextReaderGetAttribute(reader, BAD_CAST "id");
        xlon = xmlTextReaderGetAttribute(reader, BAD_CAST "lon");
        xlat = xmlTextReaderGetAttribute(reader, BAD_CAST "lat");
        assert(xid); assert(xlon); assert(xlat);

        osm_id  = strtol((char *)xid, NULL, 10);
        node_lon = strtod((char *)xlon, NULL);
        node_lat = strtod((char *)xlat, NULL);

        if (osm_id > max_node)
            max_node = osm_id;

        count_node++;
        if (count_node%10000 == 0)
            printStatus();

        xmlFree(xid);
        xmlFree(xlon);
        xmlFree(xlat);
    } else if (xmlStrEqual(name, BAD_CAST "tag")) {
#if 0
        xk = xmlTextReaderGetAttribute(reader, BAD_CAST "k");
        assert(xk);

        /* 'created_by' and 'source' are common and not interesting to mapnik renderer */
        if (strcmp((char *)xk, "created_by") && strcmp((char *)xk, "source")) {
            char *p;
            xv = xmlTextReaderGetAttribute(reader, BAD_CAST "v");
            assert(xv);
            k  = (char *)xmlStrdup(xk);
            while ((p = strchr(k, ' ')))
                *p = '_';

            addItem(&tags, k, (char *)xv, 0);
            xmlFree(k);
            xmlFree(xv);
        }
        xmlFree(xk);
#endif
    } else if (xmlStrEqual(name, BAD_CAST "way")) {
        xid  = xmlTextReaderGetAttribute(reader, BAD_CAST "id");
        assert(xid);
        osm_id   = strtol((char *)xid, NULL, 10);

        if (osm_id > max_way)
            max_way = osm_id;

        count_way++;
        if (count_way%1000 == 0)
            printStatus();

        xmlFree(xid);
    } else if (xmlStrEqual(name, BAD_CAST "nd")) {
        xid  = xmlTextReaderGetAttribute(reader, BAD_CAST "ref");
        assert(xid);

        addItem(&nds, "id", (char *)xid, 0);

        xmlFree(xid);
    } else if (xmlStrEqual(name, BAD_CAST "relation")) {
#if 0
        xid  = xmlTextReaderGetAttribute(reader, BAD_CAST "id");
        assert(xid);
        osm_id   = strtol((char *)xid, NULL, 10);

        if (osm_id > max_rel)
            max_rel = osm_id;

        xmlFree(xid);
#endif
        count_rel++;
        if (count_rel%1000 == 0)
            printStatus();

    } else if (xmlStrEqual(name, BAD_CAST "member")) {
#if 0
	xrole = xmlTextReaderGetAttribute(reader, BAD_CAST "role");
	assert(xrole);

	xtype = xmlTextReaderGetAttribute(reader, BAD_CAST "type");
	assert(xtype);

        xid  = xmlTextReaderGetAttribute(reader, BAD_CAST "ref");
        assert(xid);

        /* Currently we are only interested in 'way' members since these form polygons with holes */
	if (xmlStrEqual(xtype, BAD_CAST "way"))
	    addItem(&members, (char *)xrole, (char *)xid, 0);

        xmlFree(xid);
        xmlFree(xrole);
        xmlFree(xtype);
#endif
    } else if (xmlStrEqual(name, BAD_CAST "osm")) {
        /* ignore */
    } else if (xmlStrEqual(name, BAD_CAST "bound")) {
        /* ignore */
    } else {
        fprintf(stderr, "%s: Unknown element name: %s\n", __FUNCTION__, name);
    }
}

void EndElement(const xmlChar *name)
{
    if (xmlStrEqual(name, BAD_CAST "node")) {
        struct osmNode  * storenode;
        storenode = (struct osmNode *) calloc(1,sizeof(struct osmNode));
        if (storenode==NULL)
        {
                fprintf(stderr,"out of memory\n");
                exit(1);
        }

      if (node_lat < merc_lat_min)
            node_lat = merc_lat_min;
      else if (node_lat > merc_lat_max)
            node_lat = merc_lat_max;

	storenode->id = osm_id;
	storenode->lat = node_lat * DEG_TO_RAD;
	storenode->lon = node_lon * DEG_TO_RAD;

        rb_insert(nodes_table, storenode);
    } else if (xmlStrEqual(name, BAD_CAST "way")) {
        struct osmWay  * storeway;
	struct keyval *p;
	int i;
        storeway = (struct osmWay *) calloc(1,sizeof(struct osmWay)+sizeof(int)*countList(&nds));
        if (storeway==NULL)
        {
                fprintf(stderr,"out of memory\n");
                exit(1);
        }
	storeway->id = osm_id;
	for( i=0, p = popItem(&nds); p; i++, p = popItem(&nds) )
	{
		storeway->nds[i] = strtol(p->value, NULL, 10);
		freeItem(p);
        }
        storeway->nds[i] = 0;
        if( i == 1 )
          fprintf(stderr, "Wierd: way %d only has %d nodes\n", osm_id, i);
        if( i >= 2 )
                rb_insert( ways_table, storeway );
        resetList(&nds);
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
    fprintf(stderr, "\t%s coastline.txt coastline.osm.gz outputprefix\n", name);
    fprintf(stderr, "\n");
}

/* Can be used to compare any osm object, cause it only uses the first two fields */
static int osm_compare (const void *pa, const void *pb, void *param){
        const struct osmNode * na=pa;
        const struct osmNode * nb=pb;
        if (na->id < nb->id) return -1;
        if (na->id > nb->id) return 1;
        return 0;
        param=param;
}

static int processList(char *filename, char *output_prefix)
{
  char out_c[64], out_p[64], out_i[64];
  FILE *in;
  SHPHandle shp_poly, shp_arc, shp_point;
  DBFHandle dbf_poly, dbf_arc, dbf_point;
  
  in = fopen( filename, "rt" );
  if( !in )
  {
      fprintf(stderr, "Error opening file %s: %s\n", filename, strerror(errno) );
      exit_nicely();
  }
  if( strlen( output_prefix ) > sizeof(out_c)-10 )
  {
      fprintf( stderr, "Output prefix too long\n" );
      exit_nicely();
  }
  sprintf( out_c, "%s_c", output_prefix );
  sprintf( out_p, "%s_p", output_prefix );
  sprintf( out_i, "%s_i", output_prefix );
  
  int max_vertex_count = 0;
  
  shp_poly = SHPCreate( out_c,  SHPT_POLYGON );
  shp_arc = SHPCreate( out_i, SHPT_ARC );
  shp_point = SHPCreate( out_p, SHPT_POINT );
  dbf_poly = DBFCreate( out_c );
  dbf_arc = DBFCreate( out_i );
  dbf_point = DBFCreate( out_p );
  DBFAddField( dbf_poly, "type", FTInteger, 5, 0 );
  DBFAddField( dbf_poly, "length", FTInteger, 10, 0 );
  DBFAddField( dbf_poly, "way_id", FTInteger, 10, 0 );
  DBFAddField( dbf_arc, "type", FTInteger, 5, 0 );
  DBFAddField( dbf_arc, "length", FTInteger, 10, 0 );
  DBFAddField( dbf_arc, "way_id", FTInteger, 10, 0 );
  DBFAddField( dbf_point, "type", FTInteger, 5, 0 );
  DBFAddField( dbf_point, "way_id", FTInteger, 10, 0 );
  
  int shp_poly_count = 0, shp_arc_count = 0;
  
  double *v_x = malloc( MAX_VERTICES*sizeof(double) );
  double *v_y = malloc( MAX_VERTICES*sizeof(double) );
  double *v_z = malloc( MAX_VERTICES*sizeof(double) );
  if( !v_x || !v_y || !v_z )
  {
    fprintf( stderr, "Out of memory allocating vertex buffers\n" );
    exit_nicely();
  }
  int line = 0;
  while( !feof(in) )
  {
    char type;
    int length;
    int i;
    line++;
    fscanf( in, "%c", &type );
    if( type == 'P' )
    {
      int t;
      double x, y;
      double z = 0;
      fscanf( in, "%d %lf %lf\n", &t, &y, &x );

      x *= DEG_TO_RAD;
      y *= DEG_TO_RAD;
//      printf( "Before transform (%f,%f)\n", x, y );
      pj_transform(pj_ll, pj_merc, 1, 1, &x, &y, &z);
//      printf( "After transform (%f,%f)\n", x, y );
      
//      if( x > 0.0 && y > 5527259.12027 && x < 78271.516964 && y < 5605266.15512 )
//      {
//        fprintf(stderr, "Found at line %d\n", line );
//      }
      SHPObject *p;
      p = SHPCreateSimpleObject( SHPT_POINT, 1, &x, &y, NULL );
      int idx = SHPWriteObject( shp_point, -1, p );
      if( idx < 0 ) { fprintf(stderr, "Write failure: %m\n"); exit(1); }
      DBFWriteIntegerAttribute( dbf_point, idx, 0, t );
      DBFWriteIntegerAttribute( dbf_point, idx, 1, 0 );
      SHPDestroyObject(p);
      
      continue;
    }
    if( type != 'C' && type != 'I' )
    {
      fprintf( stderr, "Got bad type at offset %ld\n", ftell(in) );
      exit_nicely();
    }
    fscanf( in, "%d ", &length );
    printf("Generating type=%c, length=%d, poly_count=%d, arc_count=%d\n", type, length, shp_poly_count, shp_arc_count );
    struct osmNode *last_match = NULL;
    struct osmNode *first_match = NULL;
    int vertex_count = 0;
    int way_id = 0;
    for( i=0; i<length; i++ )
    {
      struct osmWay key_way;
      struct osmNode key_node;
      int j;
      if( fscanf( in, "%d ", &key_way.id ) != 1 )
      {
        fprintf( stderr, "Failed to read\n");
        exit_nicely();
      }  
      struct osmWay *way_match = rb_find( ways_table, &key_way );
      if( !way_match )
      {
        fprintf( stderr, "Failed to find way %d\n", key_way.id );
        continue;
      }
      if( way_id == 0 )
        way_id = key_way.id;
        
      for( j=0; way_match->nds[j]; j++ )
      {
//        printf("Adding way %d, node %d (%d)\n", key_way.id, j, way_match->nds[j] );
        key_node.id = way_match->nds[j];
        struct osmNode *node_match = rb_find( nodes_table, &key_node );
        if( !node_match )
        {
          fprintf( stderr, "Failed to find node %d\n", key_node.id );
          continue;
        }
        if( !first_match )
          first_match = node_match;
        if( last_match == node_match )
          continue;
        if( last_match && fabs( last_match->lat - node_match->lat ) < 1e-7
                       && fabs( last_match->lon - node_match->lon ) < 1e-7 )
        {
//          printf( "{%.6f,%.6f} ", node_match->lat, node_match->lon );
          continue;
        }
        /* Extend array as necessary */
        if( vertex_count > MAX_VERTICES-10 )
        {
          MAX_VERTICES <<= 1;
          v_x = realloc( v_x, MAX_VERTICES*sizeof(double) );
          v_y = realloc( v_y, MAX_VERTICES*sizeof(double) );
          v_z = realloc( v_z, MAX_VERTICES*sizeof(double) );
          memset( v_z, 0, MAX_VERTICES*sizeof(double) );
          fprintf( stderr, "Resized vertex arrays to %d\n", MAX_VERTICES );
        }
        v_y[vertex_count] = node_match->lat;
        v_x[vertex_count] = node_match->lon;
        
        if( vertex_count > 1 )
        {
          if( fabs( v_y[vertex_count] - v_y[vertex_count-1] ) > 1*DEG_TO_RAD ||
              fabs( v_x[vertex_count] - v_x[vertex_count-1] ) > 1*DEG_TO_RAD )
          {
            fprintf( stderr, "Problem found at way %d, node %d (%d) (%.3f,%.3f %.3f,%.3f)\n", 
                      way_id, j, way_match->nds[j],
                     v_x[vertex_count-1]/DEG_TO_RAD, v_y[vertex_count-1]/DEG_TO_RAD, 
                     v_x[vertex_count]  /DEG_TO_RAD, v_y[vertex_count]  /DEG_TO_RAD );
          }
        }
        printf( "(%.6f,%.6f) ", node_match->lat, node_match->lon );
        vertex_count++;
        last_match = node_match;
      }
      printf("|");
    }
    printf("\n");
    if( type == 'C' && vertex_count < MAX_VERTICES-1 ) // Make sure polygon is closed
    {
      if( last_match != first_match )
      {
        if( fabs( last_match->lat - first_match->lat ) < 1e-7 &&
            fabs( last_match->lon - first_match->lon ) < 1e-7 )
        {
          // If it's close, make sure it's equal
          v_y[vertex_count-1] = first_match->lat;
          v_x[vertex_count-1] = first_match->lon;
        }
        else
        {
          if( fabs( last_match->lat - first_match->lat ) > 0.5 ||
              fabs( last_match->lon - first_match->lon ) > 0.5 )
          {
            fprintf( stderr, "Warning: Overly long last leg: %d\n", way_id );
          }
          // Otherwise, link to the first node
          v_y[vertex_count] = first_match->lat;
          v_x[vertex_count] = first_match->lon;
          vertex_count++;
        }
      }
    }
    if( vertex_count > max_vertex_count )
      max_vertex_count = vertex_count;

#if 0
    if( way_id == 20806670 )
    {
      fprintf( stderr, "Before:\n");
      for(int y=0; y<vertex_count; y++)
        fprintf( stderr, "(%f,%f) ", v_x[y]/DEG_TO_RAD,v_y[y]/DEG_TO_RAD);
      fprintf(stderr, "\n");
    }
#endif
    memset( v_z, 0, sizeof(double) * ((vertex_count < MAX_VERTICES) ? vertex_count : MAX_VERTICES) );
    pj_transform(pj_ll, pj_merc, (vertex_count < MAX_VERTICES) ? vertex_count : MAX_VERTICES, 1, v_x, v_y, v_z);
      
    SHPObject *tmp = SHPCreateSimpleObject( (type == 'C') ? SHPT_POLYGON : SHPT_ARC, 
                                            (vertex_count < MAX_VERTICES) ? vertex_count : MAX_VERTICES, 
                                            v_x, v_y, NULL );
    if( (tmp->dfXMax - tmp->dfXMin) > 30000000 )
    {
      fprintf( stderr, "Oversize: way_id=%d\n", way_id );
      int scoreleft = 0, scoreright = 0;
      for(int y=0; y<vertex_count; y++)
      {
        if( v_x[y] < -20000000 ) scoreleft++;
        if( v_x[y] > +20000000 ) scoreright++;
      }
      for(int y=0; y<vertex_count; y++)
      {
        if( scoreleft < scoreright && v_x[y] < -20000000 ) v_x[y] += 2*20037508.34f;
        if( scoreleft > scoreright && v_x[y] > +20000000 ) v_x[y] -= 2*20037508.34f;
      }
      // Recreate object 
      SHPDestroyObject(tmp);
      tmp = SHPCreateSimpleObject( (type == 'C') ? SHPT_POLYGON : SHPT_ARC, 
                                   (vertex_count < MAX_VERTICES) ? vertex_count : MAX_VERTICES, 
                                   v_x, v_y, NULL );
      if( (tmp->dfXMax - tmp->dfXMin) > 30000000 )
      {
        fprintf( stderr, "Still Oversize: way_id=%d\n", way_id );
//        for( int y=0; y<vertex_count; y++)
//          fprintf( stderr, "(%f,%f), ", v_x[y], v_y[y] );
//        fprintf(stderr, "\n");
      }
    }
#if 0
    if( way_id == 20806670 )
    {
      fprintf( stderr, "After:\n");
      for(int y=0; y<vertex_count; y++)
        fprintf( stderr, "(%f,%f) ", v_x[y],v_y[y]);
      fprintf(stderr, "\n");
    }
#endif
    if( vertex_count < 2 )
    {
      fprintf(stderr, "Way %d only has %d node(s)\n", way_id, vertex_count);
    }
    if( type == 'C' && vertex_count >= 4 )
    {
      int idx = SHPWriteObject( shp_poly, -1, tmp );
      if( idx < 0 ) { fprintf(stderr, "Write failure: %m\n"); exit(1); }
      shp_poly_count++;
      DBFWriteIntegerAttribute( dbf_poly, idx, 0, 0 );
      DBFWriteIntegerAttribute( dbf_poly, idx, 1, vertex_count );
      DBFWriteIntegerAttribute( dbf_poly, idx, 2, way_id );
    }
    else if( vertex_count > 0 )
    {
      tmp->nSHPType = SHPT_ARC;
      int idx = SHPWriteObject( shp_arc, -1, tmp );
      if( idx < 0 ) { fprintf(stderr, "Write failure: %m\n"); exit(1); }
      shp_arc_count++;
      DBFWriteIntegerAttribute( dbf_arc, idx, 0, 1 );
      DBFWriteIntegerAttribute( dbf_arc, idx, 1, vertex_count );
      DBFWriteIntegerAttribute( dbf_arc, idx, 2, way_id );
      
      SHPObject *p;
      p = SHPCreateSimpleObject( SHPT_POINT, 1, &v_x[0], &v_y[0], NULL );
      idx = SHPWriteObject( shp_point, -1, p );
      if( idx < 0 ) { fprintf(stderr, "Write failure: %m\n"); exit(1); }
      DBFWriteIntegerAttribute( dbf_point, idx, 0, 2 );
      DBFWriteIntegerAttribute( dbf_point, idx, 1, way_id );
      SHPDestroyObject(p);
      
      if( type != 'C' )
      {
        p = SHPCreateSimpleObject( SHPT_POINT, 1, &v_x[vertex_count-1], &v_y[vertex_count-1], NULL );
        idx = SHPWriteObject( shp_point, -1, p );
        if( idx < 0 ) { fprintf(stderr, "Write failure: %m\n"); exit(1); }
        DBFWriteIntegerAttribute( dbf_point, idx, 0, 2 );
        DBFWriteIntegerAttribute( dbf_point, idx, 1, -way_id );
        SHPDestroyObject(p);
      }
    }
      
    SHPDestroyObject(tmp);
  }
  fprintf( stderr, "Max vertex count: %d\n", max_vertex_count );
  fprintf( stderr, "Polygons: %d, Arcs: %d\n", shp_poly_count, shp_arc_count );
  if( max_vertex_count >= MAX_VERTICES )
    fprintf( stderr, "objects cropped to %d vertices\n", MAX_VERTICES );
  SHPClose( shp_poly );
  SHPClose( shp_arc );
  SHPClose( shp_point );
  DBFClose( dbf_poly );
  DBFClose( dbf_arc );
  DBFClose( dbf_point );
  return 0;
}

int main(int argc, char *argv[])
{
    fprintf(stderr, "coast2shp SVN version %s $Rev: 4895 $ \n", VERSION);

    if (argc != 4) {
        usage(argv[0]);
        exit(EXIT_FAILURE);
    }

    initList(&nds);
    nodes_table = rb_create (osm_compare, NULL, NULL);
    ways_table = rb_create (osm_compare, NULL, NULL);

    count_node = max_node = 0;
    count_way = max_way = 0;
    count_rel = max_rel = 0;

    project_init();
    LIBXML_TEST_VERSION

    fprintf(stderr, "\nReading in file: %s\n", argv[2]);
    if (streamFile(argv[2]) != 0)
        exit_nicely();

    xmlCleanupParser();
    xmlMemoryDump();

    fprintf(stderr, "\nReading in file: %s\n", argv[1]);
    processList(argv[1], argv[3]);
    
    if (count_node || count_way || count_rel) {
        fprintf(stderr, "\n");
        fprintf(stderr, "Node stats: total(%d), max(%d)\n", count_node, max_node);
        fprintf(stderr, "Way stats: total(%d), max(%d)\n", count_way, max_way);
        fprintf(stderr, "Relation stats: total(%d), max(%d)\n", count_rel, max_rel);
    }

    project_exit();

    return 0;
}

