/********************************************************************************
 * some code copied from 2AND.c
 *
 * Project: AND2osm
 * Purpose: Scan known roads and try to determine the IDs of their endpoints
 *          by finding the common nodeID where two roads meet.
 *
 * Author: Martijn van Oosterhout <kleptog@svana.org>
 */
 
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include "rb.h"
#include "shapefil.h"

#define DISPLAY_FREQUENCY 1000
#define FILENAME "020"

struct nodeid
{
      long ID;
      int count;
      int pref;
      struct nodeid *next;
};

struct node
{
      double lat, lon;
      struct nodeid *nodeids;
};
 
struct rb_table * nodes_table;

int compare_nodes (const void *pa, const void *pb, void *param){
	const struct node * na=pa;
	const struct node * nb=pb;
	if (na->lat < nb->lat) return -1;
	if (na->lat > nb->lat) return 1;
	if (na->lon < nb->lon) return -1;
	if (na->lon > nb->lon) return 1;
	return 0;
}

struct node * addNode(double lat, double lon){
	struct node  * storenode;
	struct node ** p;
	storenode = (struct node *) calloc(1,sizeof(struct node));
	if (storenode==NULL)
	{
		fprintf(stderr,"out of memory\n");
		exit(1);
	}

	storenode->lat=lat;
	storenode->lon=lon;
	p=(struct node  **) rb_probe (nodes_table, storenode);
	if (*p!=storenode)
	{
		//item was already in list
		free(storenode);
	}
	return *p;
}

void init_nodes(){
	if (nodes_table==NULL)
	{
		nodes_table=rb_create (compare_nodes, NULL,NULL);
	}
	else
	{
		printf("error: nodes_table is inited twice\n");
		exit(1);
	}
	return;
}

void addCandidateID( struct node *n, int ID, int pref )
{
      struct nodeid *p = n->nodeids;
      
      while( p != NULL )
      {
          if( p->ID == ID )
          {
              p->count++;
              break;
          }
          p = p->next;
      }
      if( !p )
      {
          p = (struct nodeid*)calloc(1,sizeof(struct nodeid));
          p->ID = ID;
          p->next = n->nodeids;
          p->count = 1;
          p->pref = pref;
          n->nodeids = p;
      }
}

/* First argument is marked as "preferred". So we have a way of breaking ties */
static void addCandidateIDs(struct node *n, int ID1, int ID2 )
{
    addCandidateID( n, ID1, 1 );
    addCandidateID( n, ID2, 0 );
}

int ERR_ambiguous;
int ERR_nontrivial;
int NodeCount;
void dumpOutput( SHPHandle shp, DBFHandle dbf)
{
	struct rb_traverser tr;
	struct node * p;
	rb_t_init (&tr, nodes_table);
	NodeCount = -1;
	while ((p=(struct node *) rb_t_next(&tr))!=NULL)
	{
		struct nodeid *id;
		struct nodeid *top = NULL;
		
		if( !p->nodeids )  // Dummy node
		    continue;
		    
		int max = 0, non_trivial = 0;
		for( id = p->nodeids; id; id = id->next )
		{
                    if( id->count == 1 )
                        continue;
                    if( max == 0 )
                    {
                        max = id->count;
                        top = id;
                        continue;
                    }
                    non_trivial = 1;
                }
                if( non_trivial )
                {
                    ERR_nontrivial++;

                }
                else if( top == NULL )
                {
                    ERR_ambiguous++;
                    char buffer[1000];
                    buffer[0]=0;
                    
                    if( (ERR_ambiguous % 100000) == 0 )
                    {
                        printf( "Ambiguous: " );
                        for( id=p->nodeids; id; id=id->next )
                        {
                            printf( "%ldx%d, ", id->ID, id->count );
                        }
                        printf("\n");
                    }
                    double x=0.0;
                    SHPObject *shape = SHPCreateSimpleObject( SHPT_POINT, 1, &p->lon, &p->lat, &x );
                    int shapeid = SHPWriteObject( shp, -1, shape );
                    SHPDestroyObject(shape);
                    for( id = p->nodeids; id; id = id->next )
                    {
                        if( id->pref )
                            DBFWriteIntegerAttribute( dbf, shapeid, 3, id->ID );
                        sprintf( buffer+strlen(buffer), "%ldx%d,", id->ID, id->count );
                    }
                    DBFWriteStringAttribute( dbf, shapeid, 31, buffer);
                    NodeCount++;
                }
                else
                {
                        double x=0.0;
                        SHPObject *shape = SHPCreateSimpleObject( SHPT_POINT, 1, &p->lon, &p->lat, &x );
                        int shapeid = SHPWriteObject( shp, -1, shape );
                        SHPDestroyObject(shape);
                        DBFWriteIntegerAttribute( dbf, shapeid, 3, top->ID );
        		NodeCount++;
                }
	}
}
    

void createOutput(const char *filename)
{
        SHPHandle shp = SHPCreate( filename, SHPT_POINT );
        if( !shp )
        {
                printf("Couldn't create shapefile '%s': %s\n", filename, strerror(errno) );
                return;
        }
        DBFHandle dbf = DBFCreate( filename );
        if( !dbf )
        {
                printf("Couldn't create dbffile '%s': %s\n", filename, strerror(errno) );
                return;
        }
        DBFAddField( dbf, "AREA", FTDouble, 20, 5 );
        DBFAddField( dbf, "PERIMETER", FTDouble, 20, 5 );
        DBFAddField( dbf, "NLD6_", FTInteger, 11, 0 );
        DBFAddField( dbf, "ND_1", FTInteger, 11, 0 );
        DBFAddField( dbf, "ND_2", FTInteger,  2, 0 );
        DBFAddField( dbf, "ND_3", FTInteger,  2, 0 );
        DBFAddField( dbf, "ND_4", FTInteger,  3, 0 );
        DBFAddField( dbf, "ND_5", FTInteger,  3, 0 );
        DBFAddField( dbf, "ND_6", FTInteger,  2, 0 );
        DBFAddField( dbf, "ND_7", FTInteger,  2, 0 );
        
        DBFAddField( dbf, "ND_8", FTString, 30, 0 );
        DBFAddField( dbf, "ND_9", FTString, 2, 0 ); //200
        DBFAddField( dbf, "ND_10", FTString, 10, 0 );
        DBFAddField( dbf, "ND_11", FTString, 20, 0 );
        DBFAddField( dbf, "ND_12", FTString, 2, 0 );
        DBFAddField( dbf, "ND_13", FTString, 10, 0 );
        
        DBFAddField( dbf, "ND_14", FTInteger,  3, 0 );
        DBFAddField( dbf, "ND_15", FTInteger,  2, 0 );
        DBFAddField( dbf, "ND_16", FTString, 2, 0 ); //200
        DBFAddField( dbf, "ND_17", FTString, 10, 0 );
        DBFAddField( dbf, "ND_18", FTString, 20, 0 );

        DBFAddField( dbf, "ND_19", FTInteger,  3, 0 );
        DBFAddField( dbf, "ND_20", FTString, 2, 0 );
        DBFAddField( dbf, "ND_21", FTString, 6, 0 ); //6
        DBFAddField( dbf, "ND_22", FTString, 10, 0 );
        DBFAddField( dbf, "ND_23", FTString, 20, 0 );

        DBFAddField( dbf, "ND_24", FTInteger,  3, 0 );
        DBFAddField( dbf, "ND_25", FTInteger, 10, 0 );
        DBFAddField( dbf, "ND_26", FTString, 6, 0 ); //60
        DBFAddField( dbf, "ND_27", FTString, 3, 0 ); //30
        DBFAddField( dbf, "ND_28", FTString, 6, 0 ); //60
        DBFAddField( dbf, "ND_29", FTString, 60, 0 ); //60
        
        dumpOutput(shp,dbf);
        
        SHPClose(shp);
        DBFClose(dbf);
}
int readfile(char * inputfile)
{
    SHPHandle	hSHP;
    DBFHandle   hDBF;
    int		nShapeType, nEntities;
    long 	i;
    char	szTitle[12];
    double 	adfMinBound[4], adfMaxBound[4];


/* -------------------------------------------------------------------- */
/*      Open the passed shapefile.                                      */
/* -------------------------------------------------------------------- */
    hSHP = SHPOpen( inputfile, "rb" );
	printf("%s\n",inputfile);
    if( hSHP == NULL )
    {
	printf( "Unable to open: %s\n", inputfile );
	return(1);
    }


    hDBF = DBFOpen( inputfile, "rb" );
    if( hDBF == NULL )
    {
	printf( "DBFOpen(%s,\"r\") failed.\n", inputfile );
	return(2);
    }
    
    /* -------------------------------------------------------------------- */
    /*      Print out the file bounds.                                      */
    /* -------------------------------------------------------------------- */
    SHPGetInfo( hSHP, &nEntities, &nShapeType, adfMinBound, adfMaxBound );

    printf( "Shapefile Type: %s   # of Shapes: %d\n",
	    SHPTypeName( nShapeType ), nEntities );
    
    /*printf( "File Bounds: (%12.8f,%12.8f,%g,%g)\n"
		    "         to  (%12.8f,%12.8f,%g,%g)\n",
    adfMinBound[0], 
    adfMinBound[1], 
    adfMinBound[2], 
    adfMinBound[3], 
    adfMaxBound[0], 
    adfMaxBound[1], 
    adfMaxBound[2], 
    adfMaxBound[3] );*/


/* -------------------------------------------------------------------- */
/*        get file type                                                 */
/* -------------------------------------------------------------------- */
    DBFGetFieldInfo( hDBF, 0, szTitle, NULL, NULL );

    if(strcmp(szTitle,"FNODE_")==0 )
    {
/* -------------------------------------------------------------------- */
/*	Skim over the list of shapes, printing all the vertices.	*/
/* -------------------------------------------------------------------- */
            
            for( i = 0; i <nEntities; i++ )
            {
                long		j;
                SHPObject	*psShape;
                struct node *prevNode,*lastNode,*firstNode;

                psShape = SHPReadObject( hSHP, i );
                if ((i%DISPLAY_FREQUENCY)==0)
                {
                    printf("\r%7li/%7i   %7i/%7i                       ",i,nEntities,0,psShape->nVertices);
                    fflush(stdout);
                }
                if( psShape->nParts != 1 )
                {
                    printf("Not prepared to deal with multiple parts, skipping %li\n", i );
                    continue;
                }
                if( psShape->nVertices <= 1 )
                {
                    printf("Not prepared to deal with %d vertices, skipping %li\n", psShape->nVertices, i );
                    continue;
                }

		prevNode=lastNode=firstNode=NULL;
		for( j = 0; j < psShape->nVertices; j++ )
		{
			//printf("%i/%i\n",j,psShape->nVertices);
			prevNode=lastNode;
			lastNode=addNode(psShape->padfY[j],psShape->padfX[j]);
			//printf("%p\n",lastNode);
			if (j==0)
				firstNode=lastNode;
		}
		int ID1 = DBFReadIntegerAttribute( hDBF, i, 0 );
		int ID2 = DBFReadIntegerAttribute( hDBF, i, 1 );

		addCandidateIDs( firstNode, ID1, ID2 );
		addCandidateIDs( lastNode, ID2, ID1 );
		SHPDestroyObject( psShape );
	    }
    }
    else
    {
	    fprintf(stderr,"filetype unkown! %s\n",inputfile);
	    return -1;
    }
    printf("\r%7i/%7i                                     \n",nEntities,nEntities);
    fflush(stdout);

    SHPClose( hSHP );
    DBFClose( hDBF );

    return(0);

}

int main(int argc, char**argv)
{
    int c;
    init_nodes();
    
    while ((c = getopt (argc, argv, "?C:")) != -1)
            switch (c)
            {
            case 'C':
                    if( chdir( optarg ) < 0 )
                    {
                            fprintf(stderr, "Failed to change to directory '%s': %s\n", optarg, strerror(errno) );
                            exit(1);
                    }
                    break;
            case '?':
            default:
                    /* Getopt will print an error message for us. */
                    fprintf( stderr, "Usage: mknodedb [-C dir]\n"
                                     "   -C dir                           - Change to given directory before starting\n"
                                     );
                    exit(1);
            }
            
    readfile( FILENAME "_r_r");
    readfile( FILENAME "_nosr_r");
    createOutput("and_nodes");
    printf( "total: %d  ambiguous: %d  nontrivial: %d\n", NodeCount, ERR_ambiguous, ERR_nontrivial );
    return 0;
}
