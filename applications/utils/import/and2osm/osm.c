/******************************************************************************
 * Copyright (c) 2007  Marc Kessels
 * Copyright (c) 2007  Jeroen Dekkers <jeroen@dekkers.cx>
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
 */


#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "osm.h"
#include "ways.h"
#include "segments.h"
#include "nodes.h"
#include "tags.h"


#include "2AND.h"
/*datatypes*/


extern int postgres;
extern int osmChange;
extern char FileID[16];

/* File descriptor for .osm file. */
FILE *fp=NULL;

/* File descriptors for postgres sql files. */
FILE *fp_n=NULL;
FILE *fp_nt=NULL;
FILE *fp_w=NULL;
FILE *fp_wn=NULL;
FILE *fp_wt=NULL;


/*useful functions*/
long incr (long i) {
	if (postgres)
		return i+1;
	else
		return i-1;
}

int openOutput()
{
	if (postgres)
	{
		fp_n=fopen("nodes.sql","w");
		fp_nt=fopen("node_tags.sql","w");
		fp_w=fopen("ways.sql","w");
		fp_wn=fopen("way_nodes.sql","w");
		fp_wt=fopen("way_tags.sql","w");
		if ((fp_n==NULL) || (fp_nt==NULL) ||(fp_w==NULL) ||(fp_wn==NULL) ||(fp_wt==NULL))
		{
			printf("error opening files, exiting...");
			return -1;
		}

		fprintf(fp_n, "BEGIN;\n");
		fprintf(fp_n, "PREPARE nodes_insert (int, numeric, numeric) AS INSERT INTO nodes VALUES ($1, SetSRID(MakePoint($2, $3),4326));\n");
		fprintf(fp_nt, "COPY node_tags (node_id, k, v) FROM stdin;\n");
		fprintf(fp_w, "BEGIN;\n");
		fprintf(fp_wn, "COPY way_nodes (way_id, seq, node_id) FROM stdin;\n");
		fprintf(fp_wt, "COPY way_tags (way_id, k, v) FROM stdin;\n");
	}
	else
	{
		fp=fopen("AND2osm.osm","w");
		if (fp==NULL)
		{
			printf("error opening file, exiting...");
			return -1;
		}
		if( osmChange )
		        fprintf(fp,"<osmChange version=\"0.3\" generator=\"2AND\">\n<create>\n");
                else
        		fprintf(fp,"<osm version=\"0.4\">\n");
	}
	
	return 0;
}


void save(){
	saveNodes();
	saveSegments();
	saveWays();
}



int closeOutput()
{
	if (postgres)
	{
		fprintf(fp_n, "COMMIT;\n");
		fprintf(fp_nt, "\\.\n");
		fprintf(fp_w, "COMMIT;\n");
		fprintf(fp_wn, "\\.\n");
		fprintf(fp_wt, "\\.\n");

		fclose(fp_n);
		fclose(fp_nt);
		fclose(fp_w);
		fclose(fp_wn);
		fclose(fp_wt);
	}
	else 
	{
	        if( osmChange )
        		fprintf(fp,"</create>\n</osmChange>\n");
                else
        		fprintf(fp,"</osm>\n");
		fclose(fp);
	}
	return 0;
}

int invertRoad(DBFHandle hDBF, long recordnr)
{
  if( DBFReadIntegerAttribute( hDBF, recordnr, 9 )== 2)
    return 1;
  return 0;
}

struct tags * mkTagList(DBFHandle hDBF,long recordnr,int fileType,struct tags *p,struct nodes * from, struct nodes * to){
	char name[100];
	if (fileType==NODE)
	{
		//printf("in mkTagList\n");
		
		//all files except *r_r
		
		//Field 0: Type=Double, Title=`AREA', Width=20, Decimals=5
		//Field 1: Type=Double, Title=`PERIMETER', Width=20, Decimals=5
		//Field 2: Type=Integer, Title=`ONLY_', Width=11, Decimals=0
		//Field 3: Type=Integer, Title=`ND_1', Width=11, Decimals=0
		if (DBFReadIntegerAttribute( hDBF, recordnr, 3 )!=0)
		{
		        int ID = DBFReadIntegerAttribute( hDBF, recordnr, 3 );
			sprintf(name,"%i",ID);
			
			if( from!=to)
			{
//				printf("\rAARRRGGGG:a nodeID can be attached to a way........(rec=%ld)\n",recordnr);
                                /* Not actually an error, occurs whenever
                                 * an object consists of multiple parts. In
                                 * that case the first node and last node are
                                 * in different parts and thus obviously
                                 * different... */
				Err_ND_attached_to_way++;
                        }
			if (from->ANDID==0)
				from->ANDID=ID;
			else if (from->ANDID!=ID && ID < 10000000)
			{	
				printf("\rone node shouldn't get more than one ANDID! patch needed!%li %i\n",from->ANDID,ID);
				Err_more_NDIDs_per_node++;
			}
			p=addtag(p,FileID,name,NULL);
			from->required = 1;
		}
		
		//Field 4: Type=Integer, Title=`ND_2', Width=2, Decimals=0
		//Field 5: Type=Integer, Title=`ND_3', Width=2, Decimals=0
		//Field 6: Type=Integer, Title=`ND_4', Width=3, Decimals=0
		switch (DBFReadIntegerAttribute( hDBF, recordnr, 6 ))
		{
			case 1: p=addtag(p,"highway","motorway_junction",NULL); break;//junction(exit)
			//case 2: p=addtag(p,"highway","motorway_junction",NULL); break;//intersection
			//case 3: p=addtag(p,"highway","motorway_junction",NULL); break;//Border node
			//case 4: p=addtag(p,"highway","motorway_junction",NULL); break;//Directional information
			//case 5: p=addtag(p,"highway","motorway_junction",NULL); break;//Toll Booth Info
			//case 9: p=addtag(p,"highway","motorway_junction",NULL); break;//Level dead end, location other than a type 3 or 10-27 where a level may terminate
			case 10: p=addtag(p,"place","city",NULL); break;//capital city
			case 11: p=addtag(p,"place","city",NULL);
			         p=addtag(p,"population","500000",NULL);break;//Large city > 500,000
			case 12: p=addtag(p,"place","city",NULL);
			         p=addtag(p,"population","100000",NULL);break;//Large city > 500,000
			case 13: p=addtag(p,"place","town",NULL);
				 p=addtag(p,"population","50000",NULL);break;//Large city > 500,000
			case 14: p=addtag(p,"place","town",NULL);
				 p=addtag(p,"population","10000",NULL);break;//Large city > 500,000
			case 15: p=addtag(p,"place","village",NULL);
				 p=addtag(p,"population","5000",NULL);break;//Large city > 500,000
			case 16: p=addtag(p,"place","village",NULL);
				 p=addtag(p,"population","1000",NULL);break;//Large city > 500,000
			case 17: p=addtag(p,"place","village",NULL);break;//Large city > 500,000
			
			case 30: p=addtag(p,"railway","station",NULL); break;
			case 36: p=addtag(p,"aminity","parking",NULL); break;
			case 40: p=addtag(p,"aeroway","aerodrome",NULL); break;
			case 41: p=addtag(p,"aeroway","aerodrome",NULL); break;
			case 42: p=addtag(p,"aeroway","aerodrome",NULL); break;
			case 43: p=addtag(p,"aeroway","aerodrome",NULL); break;
			case 44: p=addtag(p,"aeroway","helipad",NULL); break;
			//case 45: p=addtag(p,"aeroway","seaplane base",NULL); break;
			case 46: p=addtag(p,"aeroway","aerodrome",NULL); break;
			case 47: p=addtag(p,"aeroway","aerodrome",NULL); break;
			case 48: p=addtag(p,"aeroway","aerodrome",NULL); break;
			case 80: p=addtag(p,"amenity","parking",NULL); 
				 p=addtag(p,"amenity","fuel",NULL); break;
			case 81: p=addtag(p,"amenity","parking",NULL); break;
			case 82: p=addtag(p,"amenity","parking",NULL); 
				 p=addtag(p,"amenity","fuel",NULL); break;
			case 83: p=addtag(p,"amenity","parking",NULL); 
				 p=addtag(p,"amenity","restaurant",NULL); 
				 p=addtag(p,"amenity","fuel",NULL); break;
			case 84: p=addtag(p,"amenity","parking",NULL); 
				 p=addtag(p,"amenity","restaurant",NULL); 
				 p=addtag(p,"tourism","hotel",NULL); 
				 p=addtag(p,"amenity","fuel",NULL); break;	 
			case 91: p=addtag(p,"natural","water",NULL);break;	 
			case 92: p=addtag(p,"waterway","river",NULL); break;	
			case 94: p=addtag(p,"natural","water",NULL); break;	
			case 95: p=addtag(p,"place","city",NULL); break;
			case 96: p=addtag(p,"natural","wood",NULL); break;	
			case 97: p=addtag(p,"natural","water",NULL); break;	
			case 98: p=addtag(p,"place","city",NULL); break;
			case 99: p=addtag(p,"aeroway","aerodrome",NULL); break;	
			case 101: p=addtag(p,"leisure","park",NULL); break;	
			case 102: p=addtag(p,"leisure","park",NULL); break;	
			case 103: p=addtag(p,"boundary","town",NULL); break;	
			case 104: p=addtag(p,"landuse","cemetery",NULL); break;	
			case 105: p=addtag(p,"sport","golf",NULL); break;	
			case 106: p=addtag(p,"natural","beach",NULL); break;	
			case 107: p=addtag(p,"natural","marsh",NULL); break;	
			case 109: p=addtag(p,"landuse","industrial",NULL); break;	
		}
		//Field 7: Type=Integer, Title=`ND_5', Width=3, Decimals=0
		//Field 8: Type=Integer, Title=`ND_6', Width=2, Decimals=0
		//Field 9: Type=Integer, Title=`ND_7', Width=2, Decimals=0
		//Field 10: Type=String, Title=`ND_8', Width=10, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 10 )))
		{
		//printf("adding name\n"); 
			sprintf(name,"%s",DBFReadStringAttribute( hDBF, recordnr, 10 ));
			p=addtag(p,"postal_code",name,NULL); /*efficient gebruik maken van rv!*/
			//		
		}
		//Field 12: Type=String, Title=`ND_10', Width=10, Decimals=0
		name[0]='\0';
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 12 )))//prefix name
		{
			sprintf(name,"%s",DBFReadStringAttribute( hDBF, recordnr, 12 ));
		}	
		//Field 11: Type=String, Title=`ND_9', Width=199, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 11 )))// name
		{
			sprintf(name+strlen(name),"%s",DBFReadStringAttribute( hDBF, recordnr, 11 ));
		}	
		//Field 13: Type=String, Title=`ND_11', Width=20, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 13 )))// postfix name
		{
			sprintf(name+strlen(name),"%s",DBFReadStringAttribute( hDBF, recordnr, 13 ));
		}	
		if (strcmp(name,"")!=0) p=addtag(p,"name",name,NULL); 
		
		//Field 14: Type=String, Title=`ND_12', Width=2, Decimals=0
		//Field 15: Type=String, Title=`ND_13', Width=10, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 15 )))
		{
		//printf("adding name\n"); 
			sprintf(name,"%s",DBFReadStringAttribute( hDBF, recordnr, 15));
			p=addtag(p,"AND_ND_CODE",name,NULL); 
		}
		
		//Field 16: Type=Integer, Title=`ND_14', Width=3, Decimals=0
		//Field 17: Type=Integer, Title=`ND_15', Width=2, Decimals=0
		//Field 18: Type=String, Title=`ND_16', Width=60, Decimals=0
		//Field 19: Type=String, Title=`ND_17', Width=10, Decimals=0
		//Field 20: Type=String, Title=`ND_18', Width=20, Decimals=0
		//Field 21: Type=Integer, Title=`ND_19', Width=3, Decimals=0
		//Field 22: Type=String, Title=`ND_20', Width=2, Decimals=0
		//Field 23: Type=String, Title=`ND_21', Width=60, Decimals=0
		//Field 24: Type=String, Title=`ND_22', Width=10, Decimals=0
		//Field 25: Type=String, Title=`ND_23', Width=20, Decimals=0
		//Field 26: Type=Integer, Title=`ND_24', Width=3, Decimals=0
		//Field 27: Type=Integer, Title=`ND_25', Width=10, Decimals=0
		if (DBFReadIntegerAttribute( hDBF, recordnr, 27 )>0)
		{
		//printf("adding name\n"); 
			sprintf(name,"%i",DBFReadIntegerAttribute( hDBF, recordnr, 6 ));
			p=addtag(p,"AND_ND_CODE",name,NULL); 
		}
		
		//Field 28: Type=String, Title=`ND_26', Width=60, Decimals=0
		//Field 29: Type=String, Title=`ND_27', Width=30, Decimals=0
		//Field 30: Type=String, Title=`ND_28', Width=60, Decimals=0
		//Field 31: Type=String, Title=`ND_29', Width=60, Decimals=0
		
#ifdef DEBUG
		/* We've taken over attribute 31 for debugging purposes, just stuff it into the debug tag */
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 31 )))
		{
		//printf("adding name\n"); 
			sprintf(name,"%s",DBFReadStringAttribute( hDBF, recordnr, 31));
			p=addtag(p,"AND_DEBUG",name,NULL); 
		}
#endif	
	}
	else   // ROADS
	{
		
	        /* The AND_NODE_IDs are inverted with respect to the shapefile */
		int inverted = 0;
		
		//nosr_r
		//r_r
		//Field 0: Type=Integer, Title=`FNODE_', Width=11, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 0)))
		{
		        int ID = DBFReadIntegerAttribute( hDBF, recordnr, 0 );
#ifdef DEBUG
			sprintf(name,"%i",ID);
			p=addtag(p,"AND_FROM_NODE",name,NULL);
#endif
			if (from->ANDID != 0 && from->ANDID != ID && to->ANDID == 0 )
			        to->ANDID = ID;
			if (to->ANDID == ID )
			        inverted = 1;
			if ((from->ANDID!=ID) && (to->ANDID!=ID))
			{
//				printf("\rway referres to unattached ANDID! from=%li to=%li new_from=%i,name=%s, %f,%f \n",from->ANDID,to->ANDID,ID,DBFReadStringAttribute( hDBF, recordnr, 15 ),from->lat, from->lon);
				Err_fromID_without_ANDID++;
			}
			from->required = from->used = 1;
		}
			
			
		//Field 1: Type=Integer, Title=`TNODE_', Width=11, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 1)))
		{
		        int ID = DBFReadIntegerAttribute( hDBF, recordnr, 1 );
#ifdef DEBUG
			sprintf(name,"%i",ID);
			p=addtag(p,"AND_TO_NODE",name,NULL);
#endif
			if (to->ANDID != 0 && to->ANDID != ID && from->ANDID == 0 )
			        from->ANDID = ID;
                        if (from->ANDID == ID )
                                inverted = 1;
			if ((from->ANDID!=ID) && (to->ANDID!=ID))
			{
		//		printf("\rway referres to unattached ANDID! %li %li %i,%s, %f,%f \n",from->ANDID,to->ANDID,DBFReadIntegerAttribute( hDBF, recordnr, 0 ),DBFReadStringAttribute( hDBF, recordnr, 15 ),from->lat, from->lon);
				Err_toID_without_ANDID++;
			}
			to->required = to->used = 1;
		}
		if (inverted)
		{
		        oneway_way_reversed++;
#ifdef DEBUG
		        p=addtag(p,"AND-inverted","yes",NULL);
#endif
                }
			
		//Field 2: Type=Integer, Title=`LPOLY_', Width=11, Decimals=0
		//Field 3: Type=Integer, Title=`RPOLY_', Width=11, Decimals=0
		//Field 4: Type=Double, Title=`LENGTH', Width=20, Decimals=5
		//Field 5: Type=Integer, Title=`NLD6_', Width=11, Decimals=0
		//Field 6: Type=Integer, Title=`RD_1', Width=11, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 6)))
		{
			sprintf(name,"%i",DBFReadIntegerAttribute( hDBF, recordnr, 6 ));
			p=addtag(p,FileID,name,NULL);
		}
		
		//Field 7: Type=Integer, Title=`RD_2', Width=8, Decimals=0
		//Field 8: Type=Integer, Title=`RD_3', Width=5, Decimals=0
		//Field 9: Type=Integer, Title=`RD_4', Width=2, Decimals=0
		if (DBFReadIntegerAttribute( hDBF, recordnr, 9 )==1)
		{
			if (from->ANDID==DBFReadIntegerAttribute( hDBF, recordnr, 0 ))
				p=addtag(p,"oneway","1",NULL);
			else
				p=addtag(p,"oneway","-1",NULL);
		}	
		else if (DBFReadIntegerAttribute( hDBF, recordnr, 9 )==2)
		{
			if (from->ANDID==DBFReadIntegerAttribute( hDBF, recordnr, 0 ))
				p=addtag(p,"oneway","-1",NULL);
			else
				p=addtag(p,"oneway","1",NULL);
		}
		//Field 10: Type=Integer, Title=`RD_5', Width=3, Decimals=0  // RD_TYPE
		switch (DBFReadIntegerAttribute( hDBF, recordnr, 10 ))
		{
			case 1: p=addtag(p,"highway","motorway",NULL); break;
			case 2: p=addtag(p,"highway","trunk",NULL); break;
			case 3: p=addtag(p,"highway","primary",NULL); break;
			case 4: p=addtag(p,"highway","secondary",NULL); break;
			case 5: p=addtag(p,"highway","tertiary",NULL); break;
			case 6:
			{
				if (DBFReadIntegerAttribute( hDBF, recordnr, 26 )==-1)
				{
					p=addtag(p,"highway","pedestrian",NULL); 
				}
				else
				{
					p=addtag(p,"highway","unclassified",NULL); 
				}
				
			}
			break;	
			case 7: p=addtag(p,"route","ferry;motorcars=yes;hgv=yes",NULL); break;
			case 9: p=addtag(p,"route","ferry;motorcars=no;foot=yes",NULL); break;
			case 30: p=addtag(p,"railway","rail",NULL); break;
			case 50: p=addtag(p,"highway","service",NULL); break;
			case 58: p=addtag(p,"highway","footway",NULL); break;
			case 59: p=addtag(p,"highway","virtual",NULL); break;
			default: printf("unkown road type %i\n",DBFReadIntegerAttribute( hDBF, recordnr, 10 )); break;
	
		}

		//Field 11: Type=Integer, Title=`RD_6', Width=3, Decimals=0
		//Field 12: Type=Integer, Title=`RD_7', Width=2, Decimals=0  // RD_LEVEL
		{
		        int level = DBFReadIntegerAttribute( hDBF, recordnr, 12 );
		        if (level < 6)
		        {
		                sprintf(name, "%i", level);
		                p=addtag(p,"AND:importance_level",name,NULL);
		        }
        		if (level==10)
	        		p=addtag(p,"railway","rail",NULL); 
                }
	        		
			
		
		
		//Field 13: Type=Integer, Title=`RD_8', Width=2, Decimals=0
		//Field 14: Type=String, Title=`RD_9', Width=1, Decimals=0
		//Field 15: Type=String, Title=`RD_10', Width=60, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 15 )))
		{
		//printf("adding name\n"); 
			sprintf(name,"%s",DBFReadStringAttribute( hDBF, recordnr, 15 ));
			p=addtag(p,"name",name,NULL); /*efficient gebruik maken van rv!*/
//		printf("added name %s\n",name);
		}
		
		//Field 16: Type=String, Title=`RD_11', Width=12, Decimals=0
		name[0]='\0';
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 16 )))//nat ref
		{
			sprintf(name,"%s",DBFReadStringAttribute( hDBF, recordnr, 16 ));
		}	
		//Field 17: Type=String, Title=`RD_12', Width=12, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 17 )))// nat ref2
		{
			sprintf(name+strlen(name),";%s",DBFReadStringAttribute( hDBF, recordnr, 17 ));
		}	
		//Field 18: Type=String, Title=`RD_13', Width=12, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 18 )))// nat ref3
		{
			sprintf(name+strlen(name),";%s",DBFReadStringAttribute( hDBF, recordnr, 18 ));
		}	
		if (strcmp(name,"")!=0) p=addtag(p,"ref",name,NULL); 
		
		
		
		
		
		
		
		//Field 19: Type=String, Title=`RD_14', Width=12, Decimals=0
		name[0]='\0';
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 19 )))//int ref
		{
			sprintf(name,"%s",DBFReadStringAttribute( hDBF, recordnr, 19 ));
		}	
		//Field 20: Type=String, Title=`RD_15', Width=12, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 20 )))// int ref2
		{
			sprintf(name+strlen(name),";%s",DBFReadStringAttribute( hDBF, recordnr, 20 ));
		}	
		//Field 21: Type=String, Title=`RD_16', Width=12, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 21 )))// int ref3
		{
			sprintf(name+strlen(name),";%s",DBFReadStringAttribute( hDBF, recordnr, 21 ));
		}	
		if (strcmp(name,"")!=0) p=addtag(p,"int_ref",name,NULL); 
		
		
		//Field 22: Type=Integer, Title=`RD_17', Width=2, Decimals=0
		if (DBFReadIntegerAttribute( hDBF, recordnr, 22 )==1)
		{
			p=addtag(p,"tunnel","yes",NULL);
			
			if ((DBFReadIntegerAttribute( hDBF, recordnr, 25 )<1)||(DBFReadIntegerAttribute( hDBF, recordnr, 25 )>8))
			{
				p=addtag(p,"layer","-1",NULL); 
			}
		}
		else if(DBFReadIntegerAttribute( hDBF, recordnr, 22 )==2)
		{
			p=addtag(p,"bridge","yes",NULL);
			
			if ((DBFReadIntegerAttribute( hDBF, recordnr, 25 )<1)||(DBFReadIntegerAttribute( hDBF, recordnr, 25 )>8))
			{
				p=addtag(p,"layer","1",NULL); 
			}
		}
		else if(DBFReadIntegerAttribute( hDBF, recordnr, 22 )!=0)
			printf("\nunkown tunnel code%i\n",(DBFReadIntegerAttribute( hDBF, recordnr, 22 ))); 
		//Field 23: Type=Integer, Title=`RD_18', Width=2, Decimals=0
		//Field 23: Type=Integer, Title=`RD_18', Width=2, Decimals=0
		if (DBFReadIntegerAttribute( hDBF, recordnr, 23 )!=0)
			p=addtag(p,"toll","yes",NULL);

		//Field 24: Type=Integer, Title=`RD_19', Width=5, Decimals=0
		//Field 25: Type=Integer, Title=`RD_20', Width=2, Decimals=0
		switch (DBFReadIntegerAttribute( hDBF, recordnr, 25 ))
		{
			case 1: p=addtag(p,"layer","-4",NULL); break;
			case 2: p=addtag(p,"layer","-3",NULL); break;
			case 3: p=addtag(p,"layer","-2",NULL); break;
			case 4: p=addtag(p,"layer","-1",NULL); break;
			case 5: p=addtag(p,"layer","1",NULL); break;
			case 6: p=addtag(p,"layer","2",NULL); break;
			case 7: p=addtag(p,"layer","3",NULL); break;
			case 8: p=addtag(p,"layer","4",NULL); break;
		}

		//Field 26: Type=Integer, Title=`RD_21', Width=3, Decimals=0
		switch (DBFReadIntegerAttribute( hDBF, recordnr, 26 ))
		{
			case -1: 
			if (DBFReadIntegerAttribute( hDBF, recordnr, 10 )!=6)
			{
				p=addtag(p,"foot","yes",NULL); 
				p=addtag(p,"motorcar","no",NULL); 
				p=addtag(p,"motorcycle","no",NULL); 
				p=addtag(p,"hgv","no",NULL);
			}
			break;
				
			case 3: p=addtag(p,"maxweight","3.5",NULL); break;
			case 28: p=addtag(p,"maxweight","28",NULL); break;
			case 40: p=addtag(p,"maxweight","40",NULL); break;
		}

		//Field 27: Type=Integer, Title=`RD_22', Width=2, Decimals=0
		if (DBFReadIntegerAttribute( hDBF, recordnr, 27 )>0)
			p=addtag(p,"toll","hgv",NULL); 
		//Field 28: Type=String, Title=`RD_23', Width=60, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr,28 )))
		{
			//printf("\r%s           \n",DBFReadStringAttribute( hDBF, recordnr, 28 ));
			sprintf(name,"%s",DBFReadStringAttribute( hDBF, recordnr, 28 ));
       			char *result = NULL;
   			result = strtok( name, "@");
   			while( result != NULL )
   			{
				switch (*result)
				{
					case 's'://slip road and stub link
						if (*(name+1)=='l')
						{
							if (DBFReadIntegerAttribute( hDBF, recordnr, 10 )==1)
								p=addtag(p,"highway","motorway_link",NULL);
							else
								p=addtag(p,"highway","trunk_link",NULL);
						}
						break;
					case 'r': //roundabout
						p=addtag(p,"junction","roundabout",NULL);
						break;
					case 'l': //lay-by and long haul
						if (*(name+1)=='b')
						{
							p=addtag(p,"highway","layby",NULL);
						}
						break;
					case '4': //4-wheel drive
						p=addtag(p,"grade","5",NULL);
						break;
					case 'u': //unsealed
						p=addtag(p,"surface","unpaved",NULL);
						break;
					case 'f': //functional class
						break;
					case 'h': //house numbers
						break;
					default:
						printf("\runkown RD_OTHER attribute\n");
						break;
				}
				
       				result = strtok( NULL, "@" );
       			}
   

		}	
		//Field 29: Type=Integer, Title=`RD_24', Width=3, Decimals=0
		//Field 30: Type=Integer, Title=`RD_25', Width=11, Decimals=0
		//Field 31: Type=Integer, Title=`RD_26', Width=11, Decimals=0
	}
	 //printf("attr %i\n", DBFReadIntegerAttribute( hDBF, recordnr, 10 ));
	
	 /*if (psShape->nSHPType!=SHPT_POINT)
	 {
		 p=addtag(p,"source","AND",NULL); 
		 p=addtag(p,"source-ref","www.and.com",NULL); 
	 }
	 if (psShape->nSHPType=SHPT_ARC)
		 fprintf(fp,"	</way>\n",-1-i);
	 else if (psShape->nSHPType=SHPT_POLYGON)
		 fprintf(fp,"    </area>\n",-1-i);
		
*/
	 return p; /*should be pointer to first item in tag-list*/
 }
 
