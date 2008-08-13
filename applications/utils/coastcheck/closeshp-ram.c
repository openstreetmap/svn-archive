/* closeshp - Takes two shape files and bounding box and returns a new shape
 * file with closed polygon as determined by the bounding box.
 * By Martijn van Oosterhout <kleptog@svana.org> Copyright 2008
 * Licence: GPL
 *
 * This is a similar idea to close-areas.pl for osmarender, except
 * considerably easier due to the fact that all the segments are in order
 * and connected already. There are two shapefiles: one where coast2shp has
 * managed to close the way, these are type POLYGON. The second is where the
 * closing didn't work, these are type ARC.
 *
 * The main differences between the two are:
 * - If a closed polygon completely surrounds (but does not touch the edge
 *   of) the bounding box it produce something, where as an ARC does not.
 * - If a polygon intersects the box you always get something whereas if a
 *   arc ends in the box, it gets dropped.
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

#include <shapefil.h>

#define VERBOSE 0
#define TEST 0
#define UNUSED __attribute__((unused))

#define MERC_MAX (20037508.34f)
#define DIVISIONS 400
#define MERC_BLOCK (2*MERC_MAX/DIVISIONS)
/* Number of mercator metres the tiles overlap, so the antialising doesn't cause wierd effects */
#define TILE_OVERLAP  10000
#define MAX_NODES 500000
#define MAX_SEGS  100
int MAX_SUBAREAS;   /* Was a define, not anymore. Auto grown array, starting at... */
#define INIT_MAX_SUBAREAS 1024
#define MAX_NODES_PER_ARC 10000

static double *v_x, *v_y, *v_z;
static int *Parts, *Parttypes;

static char *small_poly_list;
static SHPHandle shp_out;
static DBFHandle dbf_out;

/* Column of error flag */
#define DBF_OUT_ERROR   0
#define DBF_OUT_TILE_X   1
#define DBF_OUT_TILE_Y   2
struct segment
{
  SHPHandle shp;   /* Shapefile of this segment */
  int index;       /* Index of object in shapefile */
  double sx, sy;   /* Where the segment enters the box */
  double ex, ey;   /* Where the segment leaves the box */
  int snode, enode; /* Numbers of nodes in polygon belonging to this segment */
  double sa, ea;   /* Angle (in radians) of start/endpoint from centre of box */
  int next;        /* In joining stage, have we used this one already */
};

/* This is used to track areas that fall completely within a box. After we
 * have dealt will all the areas that intersect, we go through these to
 * determine if they are contained. If a negative subarea is contained with
 * a positive one, we append it to the shape. If a positive has no
 * surrounding we just output it. And a positive within a positive is an
 * error. */
struct subarea
{
  SHPHandle shp;   /* Shapefile of this segment */
  int index;       /* Index of this object in shapefile */
  double x, y;     /* representative point on shape */
  double areasize; /* Positive area is land, negative is lake */
  int used;        /* Have we output this subarea yet */
};

struct state
{
  int x,y;
  
  double lb[2];
  double rt[2];

//  SHPHandle shp;
//  DBFHandle dbf;
//  SHPTree  *shx;
    
  struct segment seg_list[MAX_SEGS];
  int seg_count;
  
  struct subarea *sub_areas;
  int subarea_count;
  int subarea_nodecount;
  
  int enclosed;
};

void OutputSegs( struct state *state );
void Process( struct state *state, SHPHandle shp, DBFHandle dbf, SHPTree *shx, int polygon , SHPObject **objects);
static double CalcArea( const SHPObject *obj );
static void SplitCoastlines( SHPHandle shp_poly, DBFHandle dbf_poly, SHPHandle shp_arc, DBFHandle dbf_arc, char *out_filename );
static int contains( double x, double y, double *v_x, double *v_y, int vertices );
static int CopyShapeToArray( SHPHandle shp, int index, int snode, int enode, int node_count );
static void ResizeSubareas( struct state *state, int count );
void ProcessSubareas(struct state *state, int *pc, int *nc);

int main( int argc, char *argv[] )
{
  char out_filename[256];
  
  if( argc != 4 )
  {
    fprintf( stderr, "closeshp poly_shape arc_shape output_shape\n" );
    return 1;
  }
  char *poly_file = argv[1];
  char *arc_file = argv[2];
  char *out_file = argv[3];

  int poly_count, arc_count;

  /* open shapefiles and dbf files */
  SHPHandle shp_poly = SHPOpen( poly_file, "rb" );
  if( !shp_poly )
  {
    fprintf( stderr, "Couldn't open '%s': %s\n", poly_file, strerror(errno));
    return 1;
  }

  SHPHandle shp_arc = SHPOpen( arc_file, "rb" );
  if( !shp_arc )
  {
    fprintf( stderr, "Couldn't open '%s': %s\n", arc_file, strerror(errno));
    return 1;
  }
  
  DBFHandle dbf_poly = DBFOpen( poly_file, "rb" );
  if( !dbf_poly )
  {
    fprintf( stderr, "Couldn't open DBF file for '%s'\n", poly_file );
    return 1;
  }
  if( DBFGetFieldIndex( dbf_poly, "way_id" ) != 2 )
  {
    fprintf( stderr, "Unexpected format DBF file '%s'\n", poly_file );
    return 1;
  }

  DBFHandle dbf_arc = DBFOpen( arc_file, "rb" );
  if( !dbf_arc )
  {
    fprintf( stderr, "Couldn't open DBF file for '%s'\n", arc_file );
    return 1;
  }
  if( DBFGetFieldIndex( dbf_arc, "way_id" ) != 2 )
  {
    fprintf( stderr, "Unexpected format DBF file '%s'\n", arc_file );
    return 1;
  }

  /* Split coastlines into arc no longer than MAX_NODES_PER_ARC long */
  sprintf( out_filename, "%s_i", out_file );
  SplitCoastlines( shp_poly, dbf_poly, shp_arc, dbf_arc, out_filename );
  
  sprintf( out_filename, "%s_p", out_file );
  shp_out = SHPCreate( out_filename, SHPT_POLYGON );
  if( !shp_out )
  {
    fprintf( stderr, "Couldn't create shapefile '%s': %s\n", out_file, strerror(errno));
    return 1;
  }
  
  dbf_out = DBFCreate( out_filename );
  if( !dbf_out )
  {
    fprintf( stderr, "Couldn't create DBF '%s': %s\n", out_file, strerror(errno));
    return 1;
  }
//  DBFAddField( dbf_out, "way_id", FTInteger, 11, 0 );
//  DBFAddField( dbf_out, "orientation", FTInteger, 1, 0 );
  DBFAddField( dbf_out, "error", FTInteger, 1, 0 );
  DBFAddField( dbf_out, "tile_x", FTInteger, 4, 0 );
  DBFAddField( dbf_out, "tile_y", FTInteger, 4, 0 );

  /* Check shapefiles are the right type */
  {
    int type;
    SHPGetInfo( shp_poly, &poly_count, &type, NULL, NULL );
    if( type != SHPT_POLYGON )
    {
      fprintf( stderr, "'%s' is not a POLYGON shapefile\n", poly_file );
      return 1;
    }
    SHPGetInfo( shp_arc, &arc_count, &type, NULL, NULL );
    if( type != SHPT_ARC )
    {
      fprintf( stderr, "'%s' is not a ARC shapefile\n", arc_file );
      return 1;
    }
  }

  // Cache poly & arc shapefiles
  int i;
  SHPObject **poly_objs = malloc(sizeof(SHPObject *) * poly_count);
  if (!poly_objs) {
      fprintf(stderr, "malloc for %d poly objects failed\n", poly_count);
      exit(1);
  }
   for (i=0; i<poly_count; i++)
      poly_objs[i] = SHPReadObject(shp_poly, i);

  SHPObject **arc_objs = malloc(sizeof(SHPObject *) * arc_count);
  if (!arc_objs) {
      fprintf(stderr, "malloc for %d arc objects failed\n", arc_count);
      exit(1);
  }
  for (i=0; i<arc_count; i++)
      arc_objs[i] = SHPReadObject(shp_arc, i);

  /* Build indexes on files, we need them... */
  SHPTree *shx_poly = SHPCreateTree( shp_poly, 2, 0, NULL, NULL );
  SHPTree *shx_arc  = SHPCreateTree( shp_arc, 2, 0, NULL, NULL );
  if( !shx_poly || !shx_arc )
  {
    fprintf( stderr, "Couldn't open shape indexes\n" );
    return 1;
  }
  
  /* The poly tells us which polygons are big enough to need splitting */
  small_poly_list = malloc( (poly_count>>3) + 1 );
  if( !small_poly_list )
  {
    fprintf( stderr, "Couldn't allocate poly list\n" );
    return 1;
  }
  memset( small_poly_list, 0, (poly_count>>3) + 1 );
  
  v_x = malloc( MAX_NODES * sizeof(double) );
  v_y = malloc( MAX_NODES * sizeof(double) );
  v_z = malloc( MAX_NODES * sizeof(double) );
  
  if( !v_x || !v_y || !v_z)
  {
    fprintf( stderr, "Couldn't allocate memory for nodes\n" );
    return 1;
  }
  memset( v_z, 0, MAX_NODES * sizeof(double) );
  struct state state;
  memset( &state, 0, sizeof(state) );
  ResizeSubareas(&state, INIT_MAX_SUBAREAS);

#if !TEST  
  for( int i=0; i<DIVISIONS; i++ )
    for( int j=0; j<DIVISIONS; j++ )  //Divide the world into mercator blocks approx 100km x 100km
#else
  for( int i=307; i<=307; i++ )
    for( int j=203; j<=203; j++ )  //Divide the world into mercator blocks approx 100km x 100km
#endif
    {
      state.x = i;
      state.y = j;
      
      double left   = -MERC_MAX + (i*MERC_BLOCK) - TILE_OVERLAP;
      double right  = -MERC_MAX + ((i+1)*MERC_BLOCK) + TILE_OVERLAP;
      double bottom = -MERC_MAX + (j*MERC_BLOCK) - TILE_OVERLAP;
      double top    = -MERC_MAX + ((j+1)*MERC_BLOCK) + TILE_OVERLAP;
      
      if( left  < -MERC_MAX ) left  = -MERC_MAX;
      if( right > +MERC_MAX ) right = +MERC_MAX;
      
      state.lb[0] = left;
      state.lb[1] = bottom;
      state.rt[0] = right;
      state.rt[1] = top;
      
      if(isatty(STDERR_FILENO))
//        fprintf( stderr, "\rProcessing (%d,%d)  (%.2f,%.2f)-(%.2f,%.2f)   ", i, j, left, bottom, right, top );
        fprintf( stderr, "\rProcessing (%d,%d)  (%.2f,%.2f)-(%.2f,%.2f)   ", i, j, state.lb[0], state.lb[1], state.rt[0], state.rt[1] );
      state.seg_count = 0;
      state.subarea_count = 0;
      state.subarea_nodecount = 0;
      state.enclosed = 0;
      
      Process( &state, shp_poly, dbf_poly, shx_poly, 1 , poly_objs);
      Process( &state, shp_arc,  dbf_arc,  shx_arc,  0 , arc_objs);
      
      OutputSegs( &state );
    }
    
  SHPDestroyTree( shx_poly );
  SHPDestroyTree( shx_arc );
  DBFClose( dbf_poly );
  DBFClose( dbf_arc );
  DBFClose( dbf_out );
  SHPClose( shp_poly );
  SHPClose( shp_arc );
  SHPClose( shp_out );
  
  printf("\n");
  return 0;
}

static void ResizeSubareas( struct state *state, int count )
{
  if( count < MAX_SUBAREAS )
  {
    fprintf( stderr, "Tried to resize smaller??? (%d < %d)\n", count, MAX_SUBAREAS );
    exit(1);
  }
  fprintf( stderr, "Resizing subarea array to %d\n", count );
  
  struct subarea *new_sa = malloc( count * sizeof(struct subarea) );
  free(Parts);
  Parts = malloc( 2 * count * sizeof(int) );  // parts and Parttypes are allocated in one chunk
  
  if( !new_sa || !Parts )
  {
    fprintf( stderr, "Out of memory resizing subarea array (count=%d)\n", count );
    exit(1);
  }
  
  MAX_SUBAREAS = count;
  memcpy( new_sa, state->sub_areas, state->subarea_count * sizeof(struct subarea) );
  free(state->sub_areas);
  state->sub_areas = new_sa;
  
  Parttypes = Parts + MAX_SUBAREAS;
}

static void SplitCoastlines2( int show, SHPHandle shp, DBFHandle dbf, SHPHandle shp_arc_out, DBFHandle dbf_arc_out )
{
  int count;
  SHPGetInfo( shp, &count, NULL, NULL, NULL );
  for( int i=0; i<count; i++ )
  {
    SHPObject *obj = SHPReadObject( shp, i );
    int way_id = DBFReadIntegerAttribute( dbf, i, 2 );
    
    if( obj->nVertices <= MAX_NODES_PER_ARC )
    {
      int new_id = SHPWriteObject( shp_arc_out, -1, obj );
      if( new_id < 0 ) { fprintf( stderr, "Output failure: %m\n"); exit(1); }
      DBFWriteIntegerAttribute( dbf_arc_out, new_id, 0, way_id );
      DBFWriteIntegerAttribute( dbf_arc_out, new_id, 1, show );
      DBFWriteIntegerAttribute( dbf_arc_out, new_id, 2, obj->nVertices < 4 );  /* Flag not real objects */
      SHPDestroyObject(obj);
      continue;
    }
    int arcs = (obj->nVertices / MAX_NODES_PER_ARC) + 1;
    int len = (obj->nVertices / arcs) + 1;
//    printf( "Splitting object with %d vertices, len=%d, arcs=%d\n", obj->nVertices, len, arcs );
    
    for( int j=0; j<arcs; j++ )
    {
      int this_len = (j==arcs-1)? obj->nVertices - (j*len): len+1;
//      printf( "Subobject start=%d, length=%d\n", j*len, this_len );
      SHPObject *new_obj = SHPCreateSimpleObject( SHPT_ARC, this_len, &obj->padfX[j*len], &obj->padfY[j*len], &obj->padfZ[j*len] );
      int new_id = SHPWriteObject( shp_arc_out, -1, new_obj );
      if( new_id < 0 ) { fprintf( stderr, "Output failure: %m\n"); exit(1); }
      DBFWriteIntegerAttribute( dbf_arc_out, new_id, 0, way_id );
      DBFWriteIntegerAttribute( dbf_arc_out, new_id, 1, show );
      DBFWriteIntegerAttribute( dbf_arc_out, new_id, 2, 0 );
      SHPDestroyObject(new_obj);
    }
    SHPDestroyObject(obj);
  }
}

/* The first two params are currently unused, but if people ever want to get
 * access to the completed bits of coastline, this is where to change it */
static void SplitCoastlines( SHPHandle shp_poly UNUSED, DBFHandle dbf_poly UNUSED, SHPHandle shp_arc, DBFHandle dbf_arc, char *out_filename )
{
  SHPHandle shp_arc_out = SHPCreate( out_filename, SHPT_ARC );
  if( !shp_arc_out )
  {
    fprintf( stderr, "Couldn't create shapefile '%s': %s\n", out_filename, strerror(errno));
    return;
  }
  
  DBFHandle dbf_arc_out = DBFCreate( out_filename );
  if( !dbf_arc_out )
  {
    fprintf( stderr, "Couldn't create DBF '%s': %s\n", out_filename, strerror(errno));
    return;
  }
  DBFAddField( dbf_arc_out, "way_id",   FTInteger, 11, 0 );
  DBFAddField( dbf_arc_out, "complete", FTInteger, 11, 0 );
  DBFAddField( dbf_arc_out, "error",    FTInteger, 11, 0 );

//  SplitCoastlines2( shp_poly, dbf_poly, shp_arc_out, dbf_arc_out );
  SplitCoastlines2( 0, shp_arc,  dbf_arc,  shp_arc_out, dbf_arc_out );
  
  SHPClose( shp_arc_out );
  DBFClose( dbf_arc_out );
}

static double CalcArea( const SHPObject *obj )
{
  int i;
  double base_x = obj->dfXMin;
  double base_y = obj->dfYMin;
  double area = 0;
  int n = (obj->nParts <= 1) ? obj->nVertices : obj->panPartStart[1];
//  if(VERBOSE) printf( "CalcArea: n=%d\n", n );
  for( i=0; i<n; i++ )
  {
    int p = ((i==0)?n:i)-1;
    double x1 = obj->padfX[p] - base_x;
    double x2 = obj->padfX[i] - base_x;
    double y1 = obj->padfY[p] - base_y;
    double y2 = obj->padfY[i] - base_y;
    
//    if(VERBOSE) printf( "i=%d (%f,%f)-(%f,%f) %f\n", i, x1,y1,x2,y2,x1*y2 - y1*x2);
    area += x1*y2 - y1*x2;
  }
  return area;
}

static const int table[3][3] = { {6, 5, 4}, {7, -1, 3}, { 0, 1, 2 } };

/* Determines the quadrant the given point is in. -1 means it's inside the box */
/* The rule is: left and bottom edges are in, right and top edges are out */
static inline int GetPosition( double *lb, double *rt, double X, double Y )
{
  int x, y;

  x = (X >= rt[0]) - ( X < lb[0] ) + 1;
  y = (Y >= rt[1]) - ( Y < lb[1] ) + 1;

  return table[x][y];
}

struct intersect 
{
  double x, y;  /* Coordinates */
  double t;     /* 0..1, where the intersection happens */
};

/* The rule is: left and bottom edges are in, right and top edges are out */
/* All corners are out, except the lower left one */
static int CalculateIntersections( double x1, double y1, double x2, double y2, 
                                   double *lb, double *rt, struct intersect *intersections )
{
  int count = 0;
  double x, y, t;
  
  /* Left side */
  if( (x1 < lb[0]) != (x2 < lb[0]) )
  {
    /* Determine intersection */
    x = lb[0];
    t = (x1-lb[0]) / (x1-x2);
    y = y1 + t*(y2-y1);
    
    if( lb[1] <= y && y < rt[1] ) /* Include only if in range */
      intersections[count++] = (struct intersect){ x: x, y: y, t: t };
  }
  /* Right side */
  if( (x1 >= rt[0]) != (x2 >= rt[0]) )
  {
    /* Determine intersection */
    x = rt[0];
    t = (x1-rt[0]) / (x1-x2);
    y = y1 + t*(y2-y1);
    
    if( lb[1] <= y && y < rt[1] ) /* Include only if in range */
      intersections[count++] = (struct intersect){ x: x, y: y, t: t };
  }
  /* Top side */
  if( (y1 >= rt[1]) != (y2 >= rt[1]) )
  {
    /* Determine intersection */
    y = rt[1];
    t = (y1-rt[1]) / (y1-y2);
    x = x1 + t*(x2-x1);
    
    if( lb[0] <= x && x < rt[0] ) /* Include only if in range */
      intersections[count++] = (struct intersect){ x: x, y: y, t: t };
  }
  /* Bottom side */
  if( (y1 < lb[1]) != (y2 < lb[1]) )
  {
    /* Determine intersection */
    y = lb[1];
    t = (y1-lb[1]) / (y1-y2);
    x = x1 + t*(x2-x1);
    
    if( lb[0] <= x && x < rt[0] ) /* Include only if in range */
      intersections[count++] = (struct intersect){ x: x, y: y, t: t };
  }

  /* Check the count, if we went over we killed the caller's stack... */
  if( count > 2 )
  {
    fprintf( stderr, "Too many intersections (%d)\n", count );
    exit(1);
  }
  
  if( count == 2 )
  {
    /* If there's two intersections, reorder them to match the intersection order */
    if( intersections[0].t > intersections[1].t )
    {
      struct intersect i;
      i = intersections[1];
      intersections[1] = intersections[0];
      intersections[0] = i;
    }
  }
  if(count == 0)
  {
    printf( "\nCalculate intersections: (%.2f,%.2f)-(%.2f,%.2f) hit %d\n", x1, y1, x2, y2, count );
    for( int i=0; i<count; i++ )
      printf( "   (%.2f,%.2f) t=%.6f\n", intersections[i].x, intersections[i].y, intersections[i].t );
  }  
  return count;
}

static int seg_compare( const void *a, const void *b )
{
  const struct segment *aa = (struct segment*)a;
  const struct segment *bb = (struct segment*)b;
  
  if( aa->sa < bb->sa )
    return -1;
  if( aa->sa > bb->sa )
    return 1;
  return 0;
}

/* We currently don't use anything from the source DBF file, but the cabability is there */
void Process( struct state *state, SHPHandle shp, DBFHandle dbf UNUSED, SHPTree *shx, int polygon, SHPObject **objects)
{
  int count;
  int *list = SHPTreeFindLikelyShapes( shx, state->lb, state->rt, &count );
  int poly_start;

  for( int poly = 0; poly < count; poly++ )
  {
    /* Here we track parts that have gone across the box */
    int vertex;
    int intersected = 0;
    
    int id = list[poly];
    /* If this is a small polygon, it's been printed, so skip it */
    if( polygon && small_poly_list[ id>>3 ] & (1<<(id&7)) )
      continue;

    /* Now we have a candidate object, we need to process it */
//    SHPObject *obj = SHPReadObject( shp, id );
    SHPObject *obj = objects[id];
   if (!obj)
      continue;

    /* If it's got less than 4 vertices it's not a real object */
    /* No need to mark it as error here, done in SplitCoastlines */
    if( obj->nVertices < 4 )
    {
    #if 0
      if( polygon )
      {
        int new_id = SHPWriteObject( shp_out, -1, obj );
        if( new_id < 0 ) { fprintf( stderr, "Output failure: %m\n"); exit(1); }
        DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_ERROR, 1 );
        DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_TILE_X, state->x );
        DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_TILE_Y, state->y );
      }
    #endif
      //SHPDestroyObject( obj );
      continue;
    }

    // check for being outside (search results include false positives)
        if (!SHPCheckBoundsOverlap( state->lb, state->rt, &(obj->dfXMin), &(obj->dfXMax), 2))
        {
            //printf( "Shape %d: not in area of interest, but fetched.\n",  id);
            //SHPDestroyObject( obj );
            continue;
        }


    if( polygon &&
        state->lb[0] < obj->dfXMin && obj->dfXMax < state->rt[0] &&
        state->lb[1] < obj->dfYMin && obj->dfYMax < state->rt[1] )
    {
      int sa = state->subarea_count;
      if( sa > MAX_SUBAREAS-5 )
        ResizeSubareas( state, 2*MAX_SUBAREAS );

      state->sub_areas[sa].shp = shp;
      state->sub_areas[sa].index = id;
      state->sub_areas[sa].x = obj->padfX[0];
      state->sub_areas[sa].y = obj->padfY[0];
      state->sub_areas[sa].areasize = CalcArea( obj );
      state->sub_areas[sa].used = 0;

      state->subarea_count++;
      state->subarea_nodecount += obj->nVertices;
      //SHPDestroyObject( obj );
      continue;
    }
  
    if(VERBOSE) fprintf( stderr, "\nProcessing object %d (%d vertices)\n", poly, obj->nVertices );
    /* First we need to move along the object until we leave the box. For polygons we
     * know it will eventually, since we determined already this object is larger
     * than the box */

    for( vertex=0; vertex < obj->nVertices && GetPosition( state->lb, state->rt, obj->padfX[vertex], obj->padfY[vertex] ) == -1; vertex++ )
      ;

    if( vertex == obj->nVertices )
    {
      if( polygon )
        fprintf( stderr, "Object %d did not leave box (%d vertices, polygon:%d) (%.2f,%.2f-%.2f,%.2f)\n", id, 
                obj->nVertices, polygon, obj->dfXMin, obj->dfYMin, obj->dfXMax, obj->dfYMax );
      //SHPDestroyObject( obj );
      continue;
    }
    /* We need to mark this point, so when we loop back we know where to stop */
    poly_start = vertex+1;
    
    /* This tracks the current sector. */
    int curr_sect = GetPosition( state->lb, state->rt, obj->padfX[vertex], obj->padfY[vertex] );
    
    int winding = 0, max_winding = 0, min_winding = 0;
    
    /* We need this flag so when we loop round a polygon we know if we're at the first node or the last */
    int started = 0;

    /* The basic trick is to analyse each new point and determine the
     * sector. As long as the sector doesn't change, we're cool. They're
     * numbered in such a way that any line has to go through the sectors in
     * numerical order (modulo 8) *or* it intersects the box. The
     * reason we do this is that if the line never intersects the box, we need
     * to determine the "winding number" determine if we're inside or outside. */

    if( poly_start == obj->nVertices )
      poly_start = 1;
//    fprintf( stderr, "poly_start=%d, vertex=%d\n", poly_start, vertex );
    for(;;)
    {
      vertex++;
      /* First we need to handle the step to the next node */
      if( polygon )
      {
        /* For polygons we loop around to the start point */
        if( vertex == obj->nVertices )
        {
//          fprintf( stderr, "\nLooping..." );
//          sleep(1);
          vertex = 1;
        }
        if( vertex == poly_start && started )
          break;
      }
      else
      {
        /* Else we just stop at the end */
        if( vertex == obj->nVertices )
          break;
      }
      started = 1;
      
      if( vertex >= obj->nVertices )  /* Shouldn't happen */
      {
        fprintf( stderr, "Somehow %d >= %d\n", vertex, obj->nVertices );
        break;
      }
      
      int sect = GetPosition( state->lb, state->rt, obj->padfX[vertex], obj->padfY[vertex] );
      if( sect == curr_sect )
        continue;
      if(VERBOSE)
      printf("Moved from %d to %d\n", curr_sect, sect );
      /* Now we know we've moved to another sector, so we need to know the intersection points */
      struct intersect intersections[2];
      int int_count;

//      if( id == 133912 ) fprintf(stderr, "\nVertex %d: Moved from sector %d to %d", vertex, curr_sect, sect );
      
      /* If we moved to adjacent positive sector, we don't need to check for intersections... */
      if( sect >= 0 && curr_sect >= 0 && ( ((curr_sect-sect)&7) == 1 || ((sect-curr_sect)&7) == 1 ) )
        int_count = 0;
      else
        int_count = CalculateIntersections( obj->padfX[vertex-1], obj->padfY[vertex-1], 
                                            obj->padfX[vertex],   obj->padfY[vertex],
                                            state->lb, state->rt,
                                            intersections );

      /* There are corner cases with the calculations of intersections, if
       * you move exactly on to the edge. With floating point numbers you'd
       * think it was possibly, but, well, it is. Espescially around the 0
       * meridian. In this case we get zero intersections even though we
       * changed inside/outside. What we do is basically pretend we havn't
       * changed sector at all and check the next point */
      if( int_count == 0 && (sect == -1 || curr_sect == -1) )
      {
        fprintf( stderr, "Went from %d to %d without intersection.\n"
                      "line (%f,%f)-(%f,%f), box (%f,%f)-(%f,%f)\n",
                      curr_sect, sect,
                      obj->padfX[vertex-1], obj->padfY[vertex-1],
                      obj->padfX[vertex],   obj->padfY[vertex],
                      state->lb[0], state->lb[1], state->rt[0], state->rt[1] );
#if TEST
        continue;
#endif
      }
      /* Another possibility is that we went from positive to positive with
       * only one intersection. Not good. Have not yet thought of a
       * heuristic to deal with this case */
      if( int_count == 1 && ( (sect != -1) == (curr_sect != -1) ) )
      {
        fprintf( stderr, "Went from %d to %d with 1 intersection.\n"
                      "line (%f,%f)-(%f,%f), box (%f,%f)-(%f,%f)\n",
                      curr_sect, sect,
                      obj->padfX[vertex-1], obj->padfY[vertex-1],
                      obj->padfX[vertex],   obj->padfY[vertex],
                      state->lb[0], state->lb[1], state->rt[0], state->rt[1] );
      }
      
      /* finally, if we got two intersections, we mave have passed straight through the box */
      if( int_count == 2 && (sect == -1 || curr_sect == -1) )
      {
        fprintf( stderr, "Went from %d to %d with 2 intersections.\n"
                      "line (%f,%f)-(%f,%f), box (%f,%f)-(%f,%f)\n",
                      curr_sect, sect,
                      obj->padfX[vertex-1], obj->padfY[vertex-1],
                      obj->padfX[vertex],   obj->padfY[vertex],
                      state->lb[0], state->lb[1], state->rt[0], state->rt[1] );
      }
      
      struct segment *seg_ptr = &state->seg_list[state->seg_count];
      
      /* Now we know the number of intersections. */
      if( int_count == 0 )
      {
        /* If we have no intersections, we were outside and we're still
         * outside. Then we need to track the winding number. With no
         * intersections we can move a maximum of three sections, so we can
         * tell if we moved clockwise or anti-clockwise. */
        if( !intersected )
        {
          int diff = (sect - curr_sect) & 7;  /* Like mod 8, but always positive */
          if( diff > 4 )
            diff -= 8;
            
          winding += diff;
          
          if( winding < min_winding )
            min_winding = winding;
          if( winding > max_winding )
            max_winding = winding;
        }
      }
      else if( int_count == 1 )
      {
        /* If we have one intersection, we went from inside to out, or vice
         * versa. So we need to add a section or finish an old one. */
        intersected = 1;
        if( curr_sect != -1 )
        {
          /* Going in... */
          seg_ptr->shp = shp;
          seg_ptr->index = id;
          seg_ptr->sx = intersections[0].x;
          seg_ptr->sy = intersections[0].y;
          seg_ptr->snode = vertex;
        }
        else
        {
          /* Going out... */
          seg_ptr->ex = intersections[0].x;
          seg_ptr->ey = intersections[0].y;
          seg_ptr->enode = vertex-1;
          
          (state->seg_count)++;
        }
      }
      else if( int_count == 2 )
      {
        /* If we have two intersections, we went straight through. So we need
         * to make a segment with no internal nodes */
        intersected = 1;
        
        /* Going in and out in one go... */
        seg_ptr->shp = NULL;
        seg_ptr->index = -2;
        seg_ptr->sx = intersections[0].x;
        seg_ptr->sy = intersections[0].y;
        seg_ptr->snode = -1;
        seg_ptr->ex = intersections[1].x;
        seg_ptr->ey = intersections[1].y;
        seg_ptr->enode = -1;
        
        (state->seg_count)++;
      }
      curr_sect = sect;
    }
    /* Generally, if we have a high winding number we consider ourselves
     * inside. However, in the case of bits that stick out, while the ends
     * may show to not have a high winding number, the min/max winding
     * numbers can show that the shape made a loop around the box, even
     * though the endpoints may not look like it. So we also trigger if
     * the end winding number >3 and the greatest difference at least 6 */
    
    /* However, it's more complicated than that. We could be enclosed by a
     * continental landmass, but also by a large lake. So we must take the
     * *smallest* enclosing area to determine whether we're enclosed or not.
     * To facilitate this the enclosed fields tracks the area of the
     * smallest encloser and the sign determines te positive or negativeness
     * of the area. */
    
    if( !intersected )
    {
      double enclosed = 0;
      if ((winding >= 3) || (winding >= 2 && (max_winding-min_winding) > 6))
      {
        if(VERBOSE)
          printf( "Decided enclosed: intersected=%d, winding=%d, max_winding=%d, min_winding=%d\n", intersected, winding, max_winding, min_winding );
        enclosed = +1;
      }
      if (winding < -4) 
      {
        if(VERBOSE)
          printf( "Decided unenclosed: intersected=%d, winding=%d, max_winding=%d, min_winding=%d\n", intersected, winding, max_winding, min_winding );
        enclosed = -1;
      }

      if( enclosed )
      {
        // Here we don't need to worry too much about small areas, since
        // they're unlikely to enclose anything
        double size = CalcArea(obj);
        int intsize = copysign( ceil(10*log2(1+fabs(size))), enclosed ); // Scale the size down to a number that will fit in an integer
        if( state->enclosed == 0 )
          state->enclosed = intsize;
        else if( abs(state->enclosed) > abs(intsize) )
          state->enclosed = intsize;
          
        if(VERBOSE)
          printf( "(%d,%d) New state->enclosed: %d, size=%f\n", state->x, state->y, state->enclosed, size );
      }
    }
    
    //SHPDestroyObject(obj);
  }
  free(list);
//  printf( "segcount: %d\n", state->seg_count );
}

/* Compare function to sort subareas in *descending* order */
static int subarea_compare( const void *a, const void *b )
{
  struct subarea *sa = (struct subarea *)a;
  struct subarea *sb = (struct subarea *)b;
  
  double diff = fabs(sb->areasize) - fabs(sa->areasize);
  
  if( diff > 0 ) return +1;
  if( diff < 0 ) return -1;
  return 0;
}

void OutputSegs( struct state *state )
{
  /* At this point we've processed the whole object and have a list of
   * segments which cross our box. We now compute the angles they make to
   * the centre, sort them on that basis and try to make closed areas. There
   * are as many starts as finishes so it will always finish, but a senseble
   * end result depends on the original polygon having been sane (i.e.
   * simple) */
   
  double centre_x = (state->lb[0] + state->rt[0])/2;
  double centre_y = (state->lb[1] + state->rt[1])/2;

  struct segment *seg_list = state->seg_list;
  int seg_count = state->seg_count;
  
//  fprintf( stderr, "%d sub %d  ", state->subarea_count, state->subarea_nodecount );
  // First we must sort the subareas by decreasing size
  qsort( state->sub_areas, state->subarea_count, sizeof( state->sub_areas[0] ), subarea_compare );
  
  if( seg_count == 0 )
  {
    /* No intersections at all, so we check the winding number. With the
     * water-on-the-right rule we're looking for a positive winding. */
//      if( abs(winding) > 4 )
//        printf( "\nNot intersected, winding = %d, min_winding = %d, max_winding = %d\n", winding, min_winding, max_winding );
      
    if( state->enclosed > 0 )  // Enclosed by a negative area does not count
    {
      seg_list[0].sx = seg_list[0].ex = state->lb[0];
      seg_list[0].sy = seg_list[0].ey = centre_y;
      seg_list[0].snode = seg_list[0].enode = -1;
      seg_list[0].sa = +M_PI;
      seg_list[0].ea = -M_PI;
      seg_list[0].next = -1;
      seg_count++;
    }
  }
  else
  {
    /* Work out the angles */
    for( int k=0; k<seg_count; k++ )
    {
      seg_list[k].sa = atan2( seg_list[k].sy - centre_y, seg_list[k].sx - centre_x );
      seg_list[k].ea = atan2( seg_list[k].ey - centre_y, seg_list[k].ex - centre_x );
      seg_list[k].next = -1;
    }
  }
  if( seg_count > 0 )
  {
    /* Create the helper nodes for the corners */
    for( int k=0; k<4; k++, seg_count++ )
    {
      seg_list[seg_count].sx = seg_list[seg_count].ex = (k<2) ? state->lb[0] : state->rt[0];
      seg_list[seg_count].sy = seg_list[seg_count].ey = (((k+1)&3)<2) ? state->lb[1] : state->rt[1];
      seg_list[seg_count].sa = seg_list[seg_count].ea = atan2( seg_list[seg_count].sy - centre_y, seg_list[seg_count].sx - centre_x );
      seg_list[seg_count].snode = seg_list[seg_count].enode = -2;
      seg_list[seg_count].next = -2;
    }
    
    /* Sort the nodes by increasing angle */
    qsort( seg_list, seg_count, sizeof(seg_list[0]), seg_compare );
    
    for(;;)
    {
      int part_count = 1;
      Parts[0] = 0;
      Parttypes[0] = SHPP_RING;
      
      /* First we need to find an unused segment */
      int curr;
      for( curr=0; curr < seg_count && seg_list[curr].next != -1; curr++ )
        ;
      if( curr == seg_count )
        break;
      int node_count = 0;
      for(;;)
      {
        if(VERBOSE) printf( "Part %d: ndc=%d, curr=%d (%d-%d)\n", part_count, node_count, curr, seg_list[curr].snode, seg_list[curr].enode );
        v_x[node_count] = seg_list[curr].sx;
        v_y[node_count] = seg_list[curr].sy;
        node_count++;

        if( seg_list[curr].snode >= 0 )
        {
          node_count = CopyShapeToArray( seg_list[curr].shp, seg_list[curr].index, seg_list[curr].snode, seg_list[curr].enode, node_count );
          v_x[node_count] = seg_list[curr].ex;
          v_y[node_count] = seg_list[curr].ey;
          node_count++;
        }
        
        double angle = seg_list[curr].ea;
        /* Determine the first unused segment with a start angle greater than the current angle, with wrapping */
        int next;
        for( next = 0; next < seg_count && seg_list[next].sa <= angle; next++ )
          ;
        if( next == seg_count )
          next = 0;
          
        seg_list[curr].next = next;
        /* If we come to an already used segment, we're done */
        if( seg_list[next].next >= 0 )
          break;
          
        curr = next;
      }
      v_x[node_count] = v_x[0];
      v_y[node_count] = v_y[0];
      node_count++;
      
      ProcessSubareas( state, &part_count, &node_count );
      
      if( part_count > MAX_SUBAREAS - 2 )
        fprintf( stderr, "(%d,%d) Subarea overflow: %d > %d\n", state->x, state->y, part_count, MAX_SUBAREAS-2 );
      if( node_count > MAX_NODES - 100 )
        fprintf( stderr, "(%d,%d) Node overflow: %d > %d\n", state->x, state->y, node_count, MAX_NODES - 100 );
//        fprintf( stderr, "Created object: %d verticies\n", node_count );
//      SHPObject *shape = SHPCreateSimpleObject( SHPT_POLYGON, node_count, v_x, v_y, v_z );
      SHPObject *shape = SHPCreateObject( SHPT_POLYGON, -1, 
                                            part_count, Parts, Parttypes, 
                                            node_count, v_x, v_y, v_z, NULL );
      // If a wrongly oriented shape crosses a boundary, sometimes we can see that...
      // Must do this prior to rewinding object
      int inverted = (CalcArea( shape ) < 0);
      if(VERBOSE) printf( "Created shape orientation: %f,%s\n", CalcArea( shape ), !inverted?"good":"bad" );
      SHPRewindObject( NULL, shape );
      int new_id = SHPWriteObject( shp_out, -1, shape );
      if( new_id < 0 ) { fprintf( stderr, "Output failure: %m\n"); exit(1); }
      SHPDestroyObject( shape );
      
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_ERROR, inverted ? 1 : 0 );
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_TILE_X, state->x );
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_TILE_Y, state->y );
    }
    /* Check for any remaining sub areas... we just output them, but mark negative ones as errors */
    for( int k=0; k<state->subarea_count; k++ )
    {
      struct subarea *sub_area = &state->sub_areas[k];
      if( sub_area->used )
        continue;
        
      if(VERBOSE) printf( "Remaining subarea %d (area=%f)\n", k, sub_area->areasize );
      int node_count = CopyShapeToArray( sub_area->shp, sub_area->index, 0, -1, 0 );
      int part_count = 1;
      Parttypes[0] = SHPP_RING;
      
      sub_area->used = 1;  // Need to mark as used first, or it will match itself

      ProcessSubareas( state, &part_count, &node_count );
      
//      SHPObject *obj = SHPReadObject( sub_area->shp, sub_area->index );
      SHPObject *obj = SHPCreateObject( SHPT_POLYGON, -1, 
                                        part_count, Parts, Parttypes, 
                                        node_count, v_x, v_y, v_z, NULL );
      int new_id = SHPWriteObject( shp_out, -1, obj );
      if( new_id < 0 ) { fprintf( stderr, "Output failure: %m\n"); exit(1); }
      SHPDestroyObject( obj );
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_ERROR, (sub_area->areasize > 0)?0:1 );
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_TILE_X, state->x );
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_TILE_Y, state->y );
    }
//    printf( "\nMatching %s with %d vertices, id=%d\n", polygon ? "polygon" : "arc", obj->nVertices, id );
    if(VERBOSE)
    for( int k=0; k<seg_count; k++ )
    {
      printf( "%2d %6d (%11.2f,%11.2f)-(%11.2f,%11.2f) (%5d-%5d) (%4.2f-%4.2f) => %2d\n", 
          k, seg_list[k].index, seg_list[k].sx, seg_list[k].sy, seg_list[k].ex, seg_list[k].ey, 
          seg_list[k].snode, seg_list[k].enode, seg_list[k].sa, seg_list[k].ea,
          seg_list[k].next );
    }
  }
}

static int contains( double x, double y, double *v_x, double *v_y, int vertices )
{
#if 1
  /* Referenced from Wikipedia */
  /* http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html */
    {
      int i, j, c = 0;
      for (i = 0, j = vertices-1; i < vertices; j = i++) {
        if ((((v_y[i]<=y) && (y<v_y[j])) ||
             ((v_y[j]<=y) && (y<v_y[i]))) &&
            (x < (v_x[j] - v_x[i]) * (y - v_y[i]) / (v_y[j] - v_y[i]) + v_x[i]))

          c = !c;
      }
      return c;
    }
#else
  /* Home cooked algorithm, with a bug */
  double last_angle = atan2( v_y[0] - y, v_x[0] - x );
  double angle = 0;
  
#if TEST
  int debug = (round(x) == round(6538707.962709)) && (round(y) == round(2365505.004066));
#endif

  double last_x = v_x[0];
  double last_y = v_y[0];
  
  for( int i=0; i<vertices; i++ )
  {
    /* Only bother checking if we've changed quadrants */
    if( i != vertices-1 &&
        (v_x[i] < x) == (last_x < x) &&
        (v_y[i] < y) == (last_y < y) )
      continue;
    last_x = v_x[i];
    last_y = v_y[i];
    
    double new_angle = atan2( v_y[i] - y, v_x[i] - x );
    
#if TEST
    if(debug)
      printf( "i=%d (%f,%f)-(%f,%f) last_angle=%.2f new_angle=%.2f angle=%.2f offset=%.2f\n", i, v_x[i-1], v_y[i-1], last_x, last_y, last_angle, new_angle, angle, new_angle-angle );
#endif
    /* What we want to do is set angle to the same angle as new_angle module
     * 2pi in such a way that it is within pi of the current angle. */
    double diff = fmod(fmod(new_angle-last_angle,2*M_PI) + 3*M_PI,2*M_PI) - M_PI;
    last_angle = new_angle;
    angle += diff;
    
  }
  double windings = round( angle / (2*M_PI) );

#if TEST
  if(debug)
    printf( "Determined for (%.2f,%.2f) windings=%f\n", x, y, windings );
#endif
  return windings > 0;
#endif
}

static int CopyShapeToArray( SHPHandle shp, int index, int snode, int enode, int node_count )
{
  SHPObject *obj = SHPReadObject( shp, index );
  int k;
  
  if( enode == -1 )   /* This mean to copy the whole object */
    enode = obj->nVertices - 1;
  //          printf( "Copying %d - %d (max %d)\n", seg_list[curr].snode, seg_list[curr].enode, obj->nVertices );
  
  /* The slightly odd construction of the loop is because it need to handle sections of polygon which may wrap around the end of the shape */
  if( snode != enode && enode == 0 )
    enode = obj->nVertices-1;
  // nodes[0] == node[nVertices-1]
  // nodes[1] == node[nVertices]
  for( k = snode; k != enode; k++ )
  {
    if( node_count > MAX_NODES - 100 )
    {
//      fprintf( stderr, "Node overflow...\n" );
      break;
    }
    if( k == obj->nVertices )
    {
      k = 1;
      if( k == enode )
        break;
    }
      
    v_x[node_count] = obj->padfX[k];
    v_y[node_count] = obj->padfY[k];
    node_count++;
  }
  /* k = enode now... */
  v_x[node_count] = obj->padfX[k];
  v_y[node_count] = obj->padfY[k];
  node_count++;
  SHPDestroyObject(obj);
  
  return node_count;
}

void ProcessSubareas(struct state *state, int *pc, int *nc)
{
  int part_count = *pc;
  int node_count = *nc;
  
  if(VERBOSE) printf( "ProcessSubareas(pc=%d,nc=%d)\n", *pc, *nc );
  
  // Setting this simplifies the later code as it can be assumed that
  // every ring ends at the beginning of the next part
  Parts[part_count] = node_count;
  // Now we have the outer ring we need to find if any of the subareas are contained in it
  for( int k=0; k<state->subarea_count; k++ )
  {
    struct subarea *sub_area = &state->sub_areas[k];
    if( sub_area->used )  // Already used, skip
      continue;
    if( !contains( sub_area->x, sub_area->y, v_x, v_y, Parts[1] ) ) // If not contained skip
      continue;
    // Now we have to verify that this new subarea is not contained in any of the existing ones
    if(VERBOSE) printf( "subarea %d is contained (size=%f,%s)\n", k, sub_area->areasize, (sub_area->areasize < 0)?"good":"bad");
    int contained = 1;
    for( int m=1; m < part_count; m++ )
    {
      if( contains( sub_area->x, sub_area->y, v_x+Parts[m], v_y+Parts[m], Parts[m+1]-Parts[m] ) )
      {
        contained = 0;
        if(VERBOSE) printf( "subarea %d is excluded\n", k);
        break;
      }
    }
    if( !contained )
      continue;
      
    // We know the surrounding object has positive area
    if( sub_area->areasize < 0 )
    {
//          printf( "Appending subshape\n" );
      Parttypes[0] = SHPP_OUTERRING; /* Multipart object now */
      // Append to object
      if( part_count >= MAX_SUBAREAS-2 )
      {
       // ResizeSubareas( state, MAX_SUBAREAS*2 )
        fprintf( stderr, "(%d,%d) Parts array overflow (%d)\n", state->x, state->y, MAX_SUBAREAS );
      }
      else
      {
        Parttypes[part_count] = SHPP_INNERRING;
        Parts[part_count] = node_count;
        node_count = CopyShapeToArray( sub_area->shp, sub_area->index, 0, -1, node_count );
        Parts[part_count+1] = node_count;
      }
      
      part_count++;
    }
    else
    {
      // Error, copy object
      SHPObject *obj = SHPReadObject( sub_area->shp, sub_area->index );
      if(VERBOSE) printf( "Outputting error shape (x,y)=(%f,%f), n=%d\n", obj->padfX[0], obj->padfY[0], obj->nVertices );
      int new_id = SHPWriteObject( shp_out, -1, obj );
      if( new_id < 0 ) { fprintf( stderr, "Output failure: %m\n"); exit(1); }
      SHPDestroyObject( obj );
      
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_ERROR, 1 );
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_TILE_X, state->x );
      DBFWriteIntegerAttribute( dbf_out, new_id, DBF_OUT_TILE_Y, state->y );
    }
      
    sub_area->used = 1;
  }
  
  *nc = node_count;
  *pc = part_count;
}
