/******************************************************************************
 * $Id: AND2osm.c,v 1.0 2007/07/14 25:10:29 Marc Kessels $
 * based on:
 * $Id: shpdump.c,v 1.10 2002/04/10 16:59:29 warmerda Exp $
 *
 * Project:  AND2osm
 * Purpose:  convert map-data provided by AND (www.and.com) to the
 * 	     openstreetmap community (www.openstreetmap.org). 
 *	     
 * Author:   Frank Warmerdam, warmerdam@pobox.com
 *	     Marc Kessels, osm at kessels.name
 ******************************************************************************
 * Copyright (c) 1999, Frank Warmerdam
 * Copyright (c) 2007, Marc Kessels
 * Copyright (c) 2007  Jeroen Dekkers <jeroen@dekkers.cx>
 *
 * This software is available under the following "MIT Style" license,
 * or at the option of the licensee under the LGPL (see LICENSE.LGPL).  This
 * option is discussed in more detail in shapelib.html.
 *
 * --
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ******************************************************************************
 *
 * $Log: shpdump.c,v $
 * Revision 1.0 2007/07/14 15:14:00 Marc Kessels
 * renamed to AND2osm.c 
 * added dbf input
 * added osm output
 *
 * Revision 1.10  2002/04/10 16:59:29  warmerda
 * added -validate switch
 *
 * Revision 1.9  2002/01/15 14:36:07  warmerda
 * updated email address
 *
 * Revision 1.8  2000/07/07 13:39:45  warmerda
 * removed unused variables, and added system include files
 *
 * Revision 1.7  1999/11/05 14:12:04  warmerda
 * updated license terms
 *
 * Revision 1.6  1998/12/03 15:48:48  warmerda
 * Added report of shapefile type, and total number of shapes.
 *
 * Revision 1.5  1998/11/09 20:57:36  warmerda
 * use SHPObject.
 *
 * Revision 1.4  1995/10/21 03:14:49  warmerda
 * Changed to use binary file access.
 *
 * Revision 1.3  1995/08/23  02:25:25  warmerda
 * Added support for bounds.
 *
 * Revision 1.2  1995/08/04  03:18:11  warmerda
 * Added header.
 *
 */

//static char rcsid[] = 
//  "$Id: AND2osm.c,v 0.4 2007/07/29 11:04:00 Marc Kessels";

//#include "shapefil.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <math.h>
#include "osm.h"
#include "ways.h"
#include "relations.h"
#include "nodes.h"
#include "tags.h"

/* Control how often the display is update udring processing */
#define DISPLAY_FREQUENCY   4096

int postgres = 0;
int osmChange = 0;
int simplify = 0;
char FileID[16];

static int use_boundingbox = 0;
static double mybox_min[2],mybox_max[2];

int testoverlap(SHPObject *psShape)
{
	double bbox_min[2],bbox_max[2];
	long j;
	if (!use_boundingbox)
		return -1;

	bbox_min[0]=psShape->dfXMin;
	bbox_min[1]=psShape->dfYMin;
	bbox_max[0]=psShape->dfXMax;
	bbox_max[1]=psShape->dfYMax;


	if (SHPCheckBoundsOverlap(bbox_min,bbox_max,mybox_min,mybox_max,2))
	{
		for ( j = 0; j < psShape->nVertices; j++ )
		{
			if (	  (psShape->padfY[j]<mybox_max[1])
				&&(psShape->padfY[j]>mybox_min[1])
				&&(psShape->padfX[j]<mybox_max[0])
				&&(psShape->padfX[j]>mybox_min[0]))
			return -1;
		}
	}
	return 0;
}

#define LON_PROJECT(_x) ((_x)*M_PI/180.0)
#define LAT_PROJECT(_y) (asinh(tan((_y)*M_PI/180.0)))

int Nodes_Deleted, Nodes_Skipped;
// Return true if the nodes are in a straight line (with certain margins).
// Note the projection code: straight here is in the Mercator projection
int Colinear( struct nodes *n1, struct nodes *n2, double y, double x )
{
//	printf("(%f,%f)(%f,%f)(%f,%f)\n", n1->lat, n1->lon, n2->lat, n2->lon, y, x);
	double x1 = LON_PROJECT(x) - LON_PROJECT(n1->lon);
	double y1 = LAT_PROJECT(y) - LAT_PROJECT(n1->lat);
	double x2 = LON_PROJECT(n2->lon) - LON_PROJECT(n1->lon);
	double y2 = LAT_PROJECT(n2->lat) - LAT_PROJECT(n1->lat);
	
	double area = x1*y2 - x2*y1;
	double base = sqrt(x1*x1 + y1*y1);
	double height = fabs(2*area/base);
	
//	if( n1->ANDID == 420534 )
//		printf("(%g,%g)(%g,%g) -> area=%e, base=%e, height=%e\n", x1,y1,x2,y2,area,base,height);

	if( height < 5e-7 )
	{
		Nodes_Skipped++;
		return 1;
	}
	else
		return 0;
}

int readfile(char * inputfile)
{
    SHPHandle	hSHP;
    DBFHandle   hDBF;
    int fileType;
    int		nShapeType, nEntities;
    long 	i;
    char	szTitle[12];
    double 	adfMinBound[4], adfMaxBound[4];


/* -------------------------------------------------------------------- */
/*      Open the passed shapefile.                                      */
/* -------------------------------------------------------------------- */
    hSHP = SHPOpen( inputfile, "rb" );
    if( hSHP == NULL )
    {
	fprintf(stderr,  "Unable to open: %s\n", inputfile );
	return(1);
    }


    hDBF = DBFOpen( inputfile, "rb" );
    if( hDBF == NULL )
    {
	fprintf(stderr,  "DBFOpen(%s,\"r\") failed.\n", inputfile );
	return(2);
    }
    
    /* -------------------------------------------------------------------- */
    /*      Print out the file bounds.                                      */
    /* -------------------------------------------------------------------- */
    SHPGetInfo( hSHP, &nEntities, &nShapeType, adfMinBound, adfMaxBound );

    fprintf(stderr,  "Shapefile '%s' Type: %s   # of Shapes: %d\n",
	    inputfile, SHPTypeName( nShapeType ), nEntities );
    
    sprintf( FileID, "AND_%s", inputfile+4 );
    
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
    if (strcmp(szTitle,"AREA")==0) fileType=NODE;
    else if (strcmp(szTitle,"FNODE_")==0) fileType=ROAD;
    else
    {
	    fprintf(stderr,"filetype unkown! %s\n",inputfile);
	    return -1;
    }

/* -------------------------------------------------------------------- */
/*	Skim over the list of shapes, printing all the vertices.	*/
/* -------------------------------------------------------------------- */
    
    for( i = 0; i <nEntities; i++ )
    //i=11951;
    {
	long		j,iPart;
        SHPObject	*psShape;
	struct nodes *prevNode,*lastNode,*firstNode;
	struct ways * way=NULL;
	struct relations *relation = NULL;

	psShape = SHPReadObject( hSHP, i );
	if ((i%DISPLAY_FREQUENCY)==0)
	{
	    fprintf(stderr, "\r%7li/%7i   %7i/%7i                      ",i,nEntities,0,psShape->nVertices);
	}
	//printf("\n*******************************\n");
	



//for bounding box:
/*	printf( "\nShape:%d (%s)  nVertices=%d, nParts=%d\n"
                "  Bounds:(%12.3f,%12.3f, %g, %g)\n"
                "      to (%12.3f,%12.3f, %g, %g)\n",
	        i, SHPTypeName(psShape->nSHPType),
                psShape->nVertices, psShape->nParts,
                psShape->dfXMin, psShape->dfYMin,
                psShape->dfZMin, psShape->dfMMin,
                psShape->dfXMax, psShape->dfYMax,
                psShape->dfZMax, psShape->dfMMax );
*/
	
	if (testoverlap(psShape))
	{
/*
		if (fileType==ROAD)
			printf("\n%i,name=%s ",i,DBFReadStringAttribute( hDBF, i, 15 ) );
		else
			printf("\n%i,name=%s ",i,DBFReadStringAttribute( hDBF, i, 11 ) );
*/
		//printf("%i\n",psShape->nVertices);
		int invert = 0;
		
		if (simplify && fileType == ROAD)
			if( invertRoad(hDBF, i) )
				invert = 1;
		
  		if (psShape->nVertices>1)
		{
			if (psShape->nSHPType==SHPT_POLYGON) way=newWay(AREA); else way=newWay(ROAD);
		};
		prevNode=lastNode=firstNode=NULL;
		for( j = 0, iPart=1; j < psShape->nVertices; j++ )
		{
			if ((j>0)&&((j%DISPLAY_FREQUENCY)==0))
			{
				fprintf(stderr, "\r%7li/%7i   %7li/%7i                   ",i,nEntities,j,psShape->nVertices);
			}
			//printf("%i/%i\n",j,psShape->nVertices);
			/* The AND data put nodes whenever two roads cross
			 * eachover, even if they don't intersect. This is
			 * bad for OSM. So here we detect if this node is
			 * colinear with the previous and the next one, in
			 * whic case it can be completely skipped. Except if
			 * it's a required node. */
			prevNode=lastNode;
			lastNode=addNode(psShape->padfY[j],psShape->padfX[j]);

			if( simplify && !lastNode->required &&
			    j > 0 &&           // There's a previous
			    (j+1) < psShape->nVertices && // and a next
			    (iPart == psShape->nParts || psShape->panPartStart[iPart] != (j+1) ) && // and no gap comming
			    (psShape->panPartStart[iPart-1] != j ) ) // or just gone
			{
				if( Colinear( prevNode, lastNode, psShape->padfY[j+1],psShape->padfX[j+1] ) )
				{
					lastNode = prevNode;
					continue;
				}
			}
			//printf("%p\n",lastNode);
			if (j==0)
			{
				firstNode=lastNode;
				if(way)
					addNode2Way(way,lastNode, invert);
			}
			else
			{
				if( iPart < psShape->nParts
					&& psShape->panPartStart[iPart] == j )
				{
					iPart++;
					if( !relation )
					{
						relation = addRelation();
						addWay2Relation(way, relation);
					}
						
					if (psShape->nSHPType==SHPT_POLYGON) way=newWay(AREA); else way=newWay(ROAD);
					addWay2Relation(way, relation);
					//printf("\rdividing\n");
					if(fileType == ROAD)
						printf("Multipart road??? (rec=%ld)\n", i);
				}
				else
				{
					//  printf(" seg2way\n");
					addNode2Way(way,lastNode, invert);
				}
			}
/*			    printf("%i [%12.8f,%12.8f]\n",iPart,psShape->padfX[j],psShape->padfY[j]);*/

		}
		//printf("\n");
		if (psShape->nVertices>1)
		{
			if( !invert )
				way->tag=mkTagList(hDBF, i,fileType,way->tag,firstNode,lastNode);
			else
				way->tag=mkTagList(hDBF, i,fileType,way->tag,lastNode,firstNode);
		}
		else
		{
			firstNode->tag=mkTagList(hDBF, i,fileType,firstNode->tag,firstNode,lastNode);

		}
	}
	SHPDestroyObject( psShape );
    }
    fprintf(stderr, "\r%7i/%7i                                     \r",nEntities,nEntities);

    SHPClose( hSHP );
    DBFClose( hDBF );

#ifdef USE_DBMALLOC
    malloc_dump(2);
#endif

    return(0);

}

static void bbox_error(void)
{
	fprintf (stderr, "Invalid argument to -b: %s\n", optarg);
	exit(1);
}


#define FILENAME "020"
int main(int argc, char ** argv )
{
	int c;
	int do_borders = 1;
	char *s, *s1, *s2, *s3, *s4, *p;
	

	init_tags();
	init_ways();
	init_nodes();
	init_relations();
	while ((c = getopt (argc, argv, "b:np?C:cs")) != -1)
		switch (c)
		{
		case 'b':
			s = strdup(optarg);
			if (!s)
			{
				fprintf (stderr, "malloc failed\n");
				exit(1);
			}
			s1 = strtok(s, ",");
			s2 = strtok(NULL, ",");
			s3 = strtok(NULL, ",");
			s4 = strtok(NULL, ",");

			if (s4 == NULL)
				bbox_error();

			mybox_min[0] = strtod(s1, &p);
			if (p == s1)
				bbox_error();
			mybox_min[1] = strtod(s2, &p);
			if (p == s2)
				bbox_error();
			mybox_max[0] = strtod(s3, &p);
			if (p == s3)
				bbox_error();
			mybox_max[1] = strtod(s4, &p);
			if (p == s4)
				bbox_error();

			printf ("minlon: %f, minlat: %f, maxlon: %f, maxlat: %f\n",
				mybox_min[0], mybox_min[1], mybox_max[0], mybox_max[1]);
			use_boundingbox=1;
			break;
		case 'n':
			do_borders = 0;
			break;
		case 'p':
			postgres = 1;
			break;
		case 's':
			simplify = 1;
			break;
		case 'C':
			if( chdir( optarg ) < 0 )
			{
				fprintf(stderr, "Failed to change to directory '%s': %s\n", optarg, strerror(errno) );
				exit(1);
			}
			break;
		case 'c':
			osmChange = 1;
			break;
		case '?':
		default:
			/* Getopt will print an error message for us. */
			fprintf( stderr, "Usage: 2AND [-C dir] [-b minlon,minlat,maxlon,maxlat] [-n] [-p]\n"
			                 "   -C dir                           - Change to given directory before starting\n"
			                 "   -b minlon,minlat,maxlon,maxlat   - Bounding box to extract\n"
			                 "   -n                               - Do not extract borders\n"
			                 "   -p                               - Output postgresql SQL files\n"
			                 "   -s                               - simplify output by removing redundant nodes\n"
			                 "   -c                               - Output osmChange format\n");
			exit(1);
		}
	
	Err_ND_attached_to_way=0;
	Err_more_NDIDs_per_node=0;
	oneway_way_reversed=0;	
	Err_toID_without_ANDID=0;
	Err_fromID_without_ANDID=0;

	/*readfiles*/
	
	if (access( "and_nodes.shp", F_OK) == 0)
		readfile( "and_nodes");
	readfile(FILENAME "_nosr_p");
	readfile(FILENAME "_nosr_r");
	if (do_borders) 
	{
		readfile(FILENAME "_admin0");
		readfile(FILENAME "_admin1");
		readfile(FILENAME "_admin8");
	}
	readfile(FILENAME "_a");
	readfile(FILENAME "_ce");
	readfile(FILENAME "_c");
	readfile(FILENAME "_f");
	readfile(FILENAME "_gf");
	readfile(FILENAME "_in");
	readfile(FILENAME "_i");
	if (do_borders)
	{
		readfile(FILENAME "_o");
	}
	readfile(FILENAME "_pk");
	readfile(FILENAME "_r_p");
	readfile(FILENAME "_r_r");
	readfile(FILENAME "_w");
	openOutput();
	save();
	closeOutput();
	printf("\nErr_ND_attached_to_way=%li\n",Err_ND_attached_to_way);
	printf("Err_more_NDIDs_per_node=%li\n",Err_more_NDIDs_per_node);
	printf("Reversed oneway roads=%li\n",oneway_way_reversed);
	printf("way \"toID\" refers to not present AND ID=%li\n",Err_toID_without_ANDID);
	printf("way \"fromID\" refers to not present AND ID=%li\n",Err_fromID_without_ANDID);
	if( simplify )
		printf("Useless inbetween nodes skipped: %d, deleted: %d\n", Nodes_Skipped, Nodes_Deleted);
	return 0;
	
}
