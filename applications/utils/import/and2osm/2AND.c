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

static char rcsid[] = 
  "$Id: AND2osm.c,v 0.4 2007/07/29 11:04:00 Marc Kessels";

#include "shapefil.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "osm.h"

int testoverlap(double xmin, double ymin,double  xmax,double ymax)
{
	double bbox_xmin,bbox_ymin,bbox_xmax,bbox_ymax;
//	return -1; //remove this line if you want to use a bounding box!!!!!!!!!!!!!!!!!!!!!!!
//susteren
	bbox_ymin=51.056;
	bbox_ymax=51.074;
	bbox_xmin=5.835;
	bbox_xmax=5.881;

//amsterdam
/*	bbox_ymin=52.367;
	bbox_ymax=52.370;
	bbox_xmin=4.878;
	bbox_xmax=4.880;
*/
	//test if any of the points are within bbox
	if ((xmin>bbox_xmin)&&(xmin<bbox_xmax)&&(ymin>bbox_ymin)&&(ymin<bbox_ymax)) return -1;
	if ((xmax>bbox_xmin)&&(xmax<bbox_xmax)&&(ymax>bbox_ymin)&&(ymax<bbox_ymax)) return -1;
	//points could still be completely around bbox
	if ((xmin<bbox_xmin)&&(xmax>bbox_xmax))
	{
		if ((ymin<bbox_ymax)&&(ymin>bbox_ymin)) return -1;
		if ((ymax<bbox_ymax)&&(ymax>bbox_ymin)) return -1;
	}
	if ((ymin<bbox_ymin)&&(ymax>bbox_ymax))
	{
		if ((xmin<bbox_xmax)&&(xmin>bbox_xmin)) return -1;
		if ((xmax<bbox_xmax)&&(xmax>bbox_xmin)) return -1;
	}
	

	return 0;
}











int readfile(char * inputfile)

{
    SHPHandle	hSHP;
    DBFHandle   hDBF;
    int fileType;
    int		nShapeType, nEntities, i;
    char	szTitle[12];
    double 	adfMinBound[4], adfMaxBound[4];


/* -------------------------------------------------------------------- */
/*      Open the passed shapefile.                                      */
/* -------------------------------------------------------------------- */
    hSHP = SHPOpen( inputfile, "rb" );
	printf("%s\n",inputfile);
    if( hSHP == NULL )
    {
	printf( "Unable to open:%s\n", inputfile );
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
	char 		name[100];
	struct tags * tagList;
	struct nodes *prevNode,*lastNode,*firstNode;
	struct segments *lastSegment;
	struct ways * way;

	psShape = SHPReadObject( hSHP, i );
	if ((i/100)*100==i) printf("\r%7li/%7li   %7li/%7li                       ",i,nEntities,0,psShape->nVertices);
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
	
	if (testoverlap(psShape->dfXMin, psShape->dfYMin,psShape->dfXMax, psShape->dfYMax))
	{
/*
		if (fileType==ROAD)
			printf("\n%i,name=%s ",i,DBFReadStringAttribute( hDBF, i, 15 ) );
		else
			printf("\n%i,name=%s ",i,DBFReadStringAttribute( hDBF, i, 11 ) );
*/
		//printf("%i\n",psShape->nVertices);
  if (psShape->nVertices>1)
		{
			if (psShape->nSHPType==SHPT_POLYGON) way=newWay(AREA); else way=newWay(ROAD);
		};
		prevNode=lastNode=firstNode=NULL;
		for( j = 0, iPart=1; j < psShape->nVertices; j++ )
		{
			if ((j>0)&&((j/1000)*1000==j))
			{
				printf("\r%7li/%7li   %7li/%7li                   ",i,nEntities,j,psShape->nVertices);
				fflush(stdout);
			}
			//printf("%i/%i\n",j,psShape->nVertices);
			prevNode=lastNode;
			lastNode=newNode(psShape->padfY[j],psShape->padfX[j]);
			//printf("%p\n",lastNode);
			if (j==0)
			{
				firstNode=lastNode;
			}
			else
			{
				if( iPart < psShape->nParts
					&& psShape->panPartStart[iPart] == j )
				{
					iPart++;
					printf("\rdividing\n");
				}
				else
				{
					//    printf(" seg\n");
					lastSegment=newSegment(prevNode,lastNode);
					//  printf(" seg2way\n");
					addSegment2Way(way,lastSegment);
    }
			}
/*			    printf("%i [%12.8f,%12.8f]\n",iPart,psShape->padfX[j],psShape->padfY[j]);*/

		}
		//printf("\n");
	/*	if (psShape->nSHPType==SHPT_POLYGON)
		{
			if (lastNode!=firstNode)
			{
				lastSegment=newSegment(lastNode,firstNode);
				addSegment2Way(way,lastSegment);
			}
		}
	*/
		if (psShape->nVertices>1)
		{
			way->tag=mkTagList(hDBF, i,fileType,way->tag,firstNode,lastNode);
		}
		else
		{
			firstNode->tag=mkTagList(hDBF, i,fileType,firstNode->tag,firstNode,lastNode);

		}
	}
	SHPDestroyObject( psShape );
    }
    printf("\n");

    SHPClose( hSHP );
    DBFClose( hDBF );

#ifdef USE_DBMALLOC
    malloc_dump(2);
#endif

    return(0);

}


#define FILENAME "020"
int main(int argc, char ** argv )
{

	Err_ND_attached_to_way=0;
	Err_more_NDIDs_per_node=0;
	Err_oneway_way_reversed=0;	
	
	/*readfiles*/
	
	openOutput();
	readfile(FILENAME "_nosr_p");
	readfile(FILENAME "_nosr_r");
	readfile(FILENAME "_admin0");
	readfile(FILENAME "_admin1");
	readfile(FILENAME "_admin8");
	readfile(FILENAME "_a");
	readfile(FILENAME "_ce");
	readfile(FILENAME "_c");
	readfile(FILENAME "_f");
	readfile(FILENAME "_gf");
	readfile(FILENAME "_in");
	readfile(FILENAME "_i");
	readfile(FILENAME "_o");
	readfile(FILENAME "_pk");
	readfile(FILENAME "_r_p");
	readfile(FILENAME "_r_r");
	readfile(FILENAME "_w");
	save();
	closeOutput();
	printf("\n Err_ND_attached_to_way=%i\n",Err_ND_attached_to_way);
	printf("\n Err_more_NDIDs_per_node=%i\n",Err_more_NDIDs_per_node);
	printf("\n Err_oneway_way_reversed=%i\n",Err_oneway_way_reversed);
	return 0;	
	
}
