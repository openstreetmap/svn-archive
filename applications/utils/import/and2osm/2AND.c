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
  "$Id: AND2osm.c,v 1.0 2007/07/14 15:15:00 Marc Kessels";

#include "shapefil.h"	
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "osm.h"




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
    //i=338;
    {
	long		j;
        SHPObject	*psShape;
	char 		name[100];
	struct tags * tagList;
	struct nodes *prevNode,*lastNode,*firstNode;
	struct segments *lastSegment;
	struct ways * way;
	psShape = SHPReadObject( hSHP, i );
	if ((i/100)*100==i) printf("\r%7li/%7li   %7li/%7li                       ",i,nEntities,0,psShape->nVertices);
	//printf("\n*******************************\n");
	//printf("%i,name=%s\n",i,DBFReadStringAttribute( hDBF, i, 15 ) );

	if (psShape->nVertices>0)
	{
		if (psShape->nSHPType==SHPT_POLYGON) way=newWay(AREA); else way=newWay(ROAD);
	};
	
	prevNode=lastNode=firstNode=NULL;
	for( j = 0; j < psShape->nVertices; j++ )
	{
		if ((j>0)&&((j/1000)*1000==j)) printf("\r%7li/%7li   %7li/%7li                       ",i,nEntities,j,psShape->nVertices);
		fflush(stdout);
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
		//    printf(" seg\n");
		    lastSegment=newSegment(prevNode,lastNode);
		  //  printf(" seg2way\n");
		    addSegment2Way(way,lastSegment);
	    }
	//    printf(" [%12.8f,%12.8f]\n",psShape->padfX[j],psShape->padfY[j]);
	   
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
	if (psShape->nVertices>0)
	{
		way->tag=mkTagList(hDBF, i,fileType,way->tag);
	}
	else
	{
		firstNode->tag=mkTagList(hDBF, i,fileType,firstNode->tag); 
		
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
}
