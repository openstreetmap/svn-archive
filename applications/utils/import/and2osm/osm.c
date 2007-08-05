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
#include "2AND.h"
/*datatypes*/

extern int postgres;

long text_depth;
long node_depth;
long textID=-1;
struct textnode { /* the tree node: */
	long hash;
	char *text; /* points to the text */
	struct textnode *left; /* left child */
	struct textnode *right; /* right child */
};



/*root nodes of lists*/
struct textnode *root_text=NULL;
struct nodes *root_node=NULL;
struct segments *root_segment=NULL;
struct ways *root_way=NULL;


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



long hash_ID(long x) {
 long h = 0;
 int i = 0;

 for(h = i = 0; i < sizeof(long)*8; i++) {
  h = (h << 1) + (x & 1); 
  x >>= 1; 
 }

 return h;
}


long hash_lat(double x) {
	return hash_ID(x*1000000);
}



/* djb2
 * This algorithm was first reported by Dan Bernstein
 * many years ago in comp.lang.c
 */
long hash_text(char *str)
{
	long hash = 5381;
	int c; 
	while ((c = *str++)) hash = ((hash << 5) + hash) + c; // hash*33 + c
	return hash_ID(hash);
}











/*output*/

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
		fprintf(fp,"<osm version=\"0.4\">\n");
	}
	
	return 0;
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
		fprintf(fp,"</osm>\n");
		fclose(fp);
	}
	return 0;
}


void saveTag(struct tags *p){
	fprintf(fp,"	<tag k=\"%s\" v=\"%s\" />\n",p->key,p->value);
	return;
}

void saveTags(struct tags *p){
//	printf("in saveTags %p\n",p);
	if (p!=NULL)
	{
		saveTag(p);
		saveTags(p->nextTag);
	}
	return;
}

void saveNode(struct nodes *p){
	fprintf(fp,"	<node id=\"%li\" lat=\"%1.5f\" lon=\"%1.5f\" >\n",p->ID,p->lat,p->lon);
	saveTags(p->tag);
	fprintf(fp,"		<tag k=\"source\" v=\"AND\" />\n");
	fprintf(fp,"		<tag k=\"source-ref\" v=\"www.and.com\" />\n");
	fprintf(fp,"	</node>\n");
	
}

void saveNodes(struct nodes *p){
	if (p!=NULL)
	{
		saveNodes(p->btree_l);
		saveNode(p);
		saveNodes(p->btree_h);
	}
}

long saveSegment(struct segments *p){
	
	if (fp==NULL)
	{
		printf("saveSegment file not open!!!");
		exit(1);
	}
	if (p==NULL)
	{
		printf("saveSegment null-pointer to struct segments !!!");
		exit(1);
	}
	if (p->from==NULL)
	{
		printf("saveSegment null-pointer to struct nodes from !!!");
		exit(1);
	}
	if (p->to==NULL)
	{
		printf("saveSegment null-pointer to struct nodes to !!!");
		exit(1);
	}
	if (fp!=NULL) fprintf(fp,"	<segment id=\"%li\" from=\"%li\" to=\"%li\" />\n",p->ID,(p->from)->ID,(p->to)->ID);
	return 0;
}
		
void saveSegments(struct segments *p){
	while (p!=NULL)
	{
		saveSegment(p);
		p=p->next;
	}
}

void saveAttachedSegments(struct attachedSegments *p){
	if (p!=NULL)
	{
		fprintf(fp,"		<seg id=\"%li\" />\n",p->Segment->ID);
//		printf("(%2.5f, %2.5f)-(%2.5f,%2.5f)\n",p->Segment->from->lon,p->Segment->from->lat,p->Segment->to->lon,p->Segment->to->lat);
		saveAttachedSegments(p->nextSegment);
	}
}




void saveWay(struct ways *p){

	if (p->type==ROAD)
		fprintf(fp,"	<way id=\"%li\" >\n",p->wayID);
	else if (p->type==AREA)
		fprintf(fp,"    <way id=\"%li\" >\n",p->wayID);
	else fprintf(stderr,"unkown wayType in saveWay\n");	
	saveTags(p->tag);
	saveAttachedSegments(p->segments);
	

	
	if (p->type==ROAD)
		fprintf(fp,"	</way>\n");
	else if (p->type==AREA)
		fprintf(fp,"    </way>\n");

}	

void saveWays(struct ways *p){
	while (p!=NULL)
	{
		saveWay(p);
		p=p->next;
	}
}

void saveNode_pg(struct nodes *p)
{
	struct tags *t;

	fprintf(fp_n, "EXECUTE nodes_insert(%li, %1.6f, %1.6f);\n", p->ID, p->lat, p->lon);

	for (t = p->tag; t != NULL; t = t->nextTag)
    
		fprintf(fp_nt, "%li\t%s\t%s\n", p->ID, t->key, t->value);
}

void saveNodes_pg(struct nodes *p){
	if (p!=NULL)
	{
		saveNodes_pg(p->btree_l);
		saveNode_pg(p);
		saveNodes_pg(p->btree_h);
	}
}

void saveWay_pg(struct ways *p)
{
	struct tags *t;
	struct attachedSegments *s; 
	long seqid = 1;

	if (p->segments == NULL)
	{
		printf("Way %li doesn't have segments, ignoring", p->wayID);
		return;
	}
  
	fprintf(fp_w, "INSERT INTO ways VALUES (%li, GeomFromText('LINESTRING(", p->wayID);

	for (s = p->segments;;s = s->nextSegment)
	{
		fprintf(fp_w, "%1.6f %1.6f,", s->Segment->from->lat, s->Segment->from->lon);
		fprintf(fp_wn, "%li\t%li\t%li\n", p->wayID, seqid++, s->Segment->from->ID);
		if (s->nextSegment == NULL)
		{
			fprintf(fp_w, "%1.6f %1.6f)', 4326));\n", s->Segment->to->lat, s->Segment->to->lon);
			fprintf(fp_wn, "%li\t%li\t%li\n", p->wayID, seqid++, s->Segment->to->ID);
			break;
		}
	}

	for (t = p->tag; t != NULL; t = t->nextTag)
		fprintf(fp_wt, "%li\t%s\t%s\n", p->wayID, t->key, t->value);
}

void saveWays_pg(struct ways *p){
	while (p!=NULL)
	{
		saveWay_pg(p);
		p=p->next;
	}
}

void save(){
	if (postgres)
	{
		saveNodes_pg(root_node);
		saveWays_pg(root_way);
	}
	else
	{
		saveNodes(root_node);
		saveSegments(root_segment);
		saveWays(root_way);
	}
}
	

struct textnode *addText(struct textnode * p, char * text,char ** rv){
	int cond;
	unsigned long hash;
	hash=hash_text(text);
	if (p == NULL) { /* a new word has arrived */
		p = (struct textnode *) calloc(1,sizeof(struct textnode)); /* make a new node */
		textID--;
		if (p==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		p->text = (char *) calloc(1,(strlen(text)+1)*sizeof(char));
		if (p==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		strcpy(p->text,text);
		p->left = p->right = NULL;
		p->hash=hash;
		if (rv!=NULL) *rv=p->text;
		text_depth=0;
	}
	else if (hash<p->hash)
		p->left = addText(p->left, text,rv);/* less than into left subtree */
	else if (hash>p->hash)
		p->right = addText(p->right, text,rv); /* greater than into right subtree */
	else  if ((cond = strcmp(text, p->text)) == 0) {
		//hashes are the same, so biggest change texts are the same
		//printf("text found\n"); 
		if ((rv)!=NULL) *rv=p->text;/*text found*/
		text_depth=0;
		}
	else if (cond > 0) 
		p->right = addText(p->right, text,rv); /* greater than into right subtree */
	else 
		p->left = addText(p->left, text,rv);/* less than into left subtree */
	

	text_depth++;
	return p;
}

struct tags * addtag(struct tags *p,char * tag_key, char * tag_value,struct tags **rv){
//	printf("in addtag\n"); 
	if (p==NULL)
	{
		/*new tag arrived*/
		p = (struct tags *) calloc(1,sizeof(struct tags));
		if (p==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		p->nextTag=NULL;
//		printf("%s %s\n",tag_key,tag_value);
		root_text=addText(root_text,tag_key,&(p->key));
		if (text_depth>text_maxdepth)
		{
			text_maxdepth=text_depth;
		//	printf("\nnew text depth:%li/%li\n",text_maxdepth,textID);
		}
//		printf("%s %p\n",p->key,root_text);
		root_text=addText(root_text,tag_value,&(p->value));
		if (text_depth>text_maxdepth)
		{
			text_maxdepth=text_depth;
		//	printf("\nnew text depth:%li/%li\n",text_maxdepth,textID);
		}
		if (rv!=NULL) *rv=p;
	}
	else
		p->nextTag=addtag(p->nextTag, tag_key, tag_value,rv);
	return p;
}

struct nodes * mkNode(double lat, double lon,struct nodes *p ,struct nodes **rv){
	static long nodeID = 0;
	long long hashed_lat;
	hashed_lat=hash_lat(lat);
	//printf("in mkNode\n");
	if (p == NULL) /* a new Node has arrived */
	{
		//printf("new node\n");
		p = (struct nodes *) calloc(1,sizeof(struct nodes));
		if (p==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		if (rv!=NULL) *rv=p;
		nodeID = incr(nodeID);
		p->ID=nodeID;
		p->hashed_lat=hashed_lat;
		p->lat=lat;
		p->lon=lon;
		p->btree_l=NULL;
		p->btree_h=NULL;
		p->tag=NULL;
		p->segments=NULL;
		
		//p->ways=NULL;
		node_depth=0;
/*		saveNode(p->ID,lat,lon);*/
	}
	else if (hashed_lat< p->hashed_lat) 
	{
		p->btree_l=mkNode(lat,lon,p->btree_l,rv);
	}
	else if (hashed_lat > p->hashed_lat)
	{
		p->btree_h=mkNode(lat,lon,p->btree_h,rv);
	}
	else if (lat< p->lat)
	{
		//should not be reached, unless hashing is again not ok...
		p->btree_l=mkNode(lat,lon,p->btree_l,rv);
	}
	else if (lat > p->lat)
	{
		//should not be reached, unless hashing is again not ok...
		p->btree_h=mkNode(lat,lon,p->btree_h,rv);
	}
	
	else if /*lat=p->lat*/ (lon< p->lon)
	{
		p->btree_l=mkNode(lat,lon,p->btree_l,rv);
	}
	else if (lon > p->lon)
	{
		p->btree_h=mkNode(lat,lon,p->btree_h,rv);
	}
	else /*lat=p->lat && lon=p->lon*/
	{	
		if (rv!=NULL) *rv=p;
		node_depth=0;
		//printf("node found (%8.5f,%8.5f)=(%8.5f,%8.5f) %f=%f %li,%p\n",lon,lat,p->lon,p->lat,hash_lat(hashed_lat),hash_lat(p->hashed_lat),p->ID,(*rv)->ID);
	}
	//printf("out mkNode %p\n",p);
	node_depth++;
	return p;
}

struct nodes * newNode(double lat, double lon){
	struct nodes * rv;
	root_node=mkNode(lat,lon,root_node,&rv);
	if (node_depth>node_maxdepth)
	{
		node_maxdepth=node_depth;
		//printf("\nnew node depth:%li/%li\n",node_maxdepth,nodeID);
	}
	return rv;
}

struct attachedSegments * attachsegment(struct attachedSegments* p, struct segments *s){
	//printf("%p\t%p\n",p,s);
	if (p==NULL)
	{
		p = (struct attachedSegments *) calloc(1,sizeof(struct attachedSegments));
		if (p==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		p->nextSegment=NULL;
		p->Segment=s;
	}
	else
	{
		p->nextSegment=attachsegment(p->nextSegment,s);
	}
	return p;
}	

struct segments * newSegment(struct nodes * from, struct nodes * to){
	static long segmentID = 0;
	static struct segments *lastsegment=NULL;
	struct attachedSegments *p;
	struct segments *s;
	/*check if already a segment exists between from and to, having same direction!*/

	p=from->segments;
	while (p!=NULL)
	{
		if (((p->Segment)->from==from)&&((p->Segment)->to==to))
			return p->Segment;
		p=p->nextSegment;
	}
	
	if (p == NULL) /* a new Segment has arrived ,if statment a bit overdone... */
	{
		s = (struct segments *) calloc(1,sizeof(struct segments));
		if (s==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		segmentID = incr(segmentID);
		s->ID=segmentID;
		s->from=from;
		s->to=to;
		s->next=NULL;
		s->ways=NULL;
		if (lastsegment!=NULL) lastsegment->next=s;
		lastsegment=s;
		if (root_segment==NULL) root_segment=s;
		/*update from node's segment list*/
	//printf("new segment\n");

		from->segments=attachsegment(from->segments,s);
		/*update to node's segment list*/
		to->segments=attachsegment(to->segments,s);
	
	}
	return s;
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
			sprintf(name,"AND=%i",DBFReadIntegerAttribute( hDBF, recordnr, 3 ));
			
			if( from!=to)
				//printf("\rAARRRGGGG:a nodeID can be attached to a way........\n");
				Err_ND_attached_to_way++;
			if (from->ANDID==0)
				from->ANDID=DBFReadIntegerAttribute( hDBF, recordnr, 3 );
			else if (from->ANDID!=DBFReadIntegerAttribute( hDBF, recordnr, 3 ))
			{	
				//printf("\rone node should get more than one ANDID! patch needed!%li %li\n",from->ANDID,DBFReadIntegerAttribute( hDBF, recordnr, 3 ));
				Err_more_NDIDs_per_node++;
			}
			
			p=addtag(p,"external-ID",name,NULL);
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
		
	}
	else
	{
		
	
			
		
		//nosr_r
		//r_r
		//Field 0: Type=Integer, Title=`FNODE_', Width=11, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 0)))
		{
			sprintf(name,"%i",DBFReadIntegerAttribute( hDBF, recordnr, 0 ));
			p=addtag(p,"AND_FROM_NODE",name,NULL);
		/*	if ((from->ANDID!=DBFReadIntegerAttribute( hDBF, recordnr, 0 )) &&
						  (to->ANDID!=DBFReadIntegerAttribute( hDBF, recordnr, 0 )))
			{
				printf("\rway referres to unattached ANDID! %i %i %i,%f,%f %s\n",from->ANDID,to->ANDID,DBFReadIntegerAttribute( hDBF, recordnr, 0 ),DBFReadStringAttribute( hDBF, recordnr, 15 ),from->lat, from->lon);
		//		Err_oneway_way_reversed++;
			}*/
		}
			
			
		//Field 1: Type=Integer, Title=`TNODE_', Width=11, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 1)))
		{
			sprintf(name,"%i",DBFReadIntegerAttribute( hDBF, recordnr, 1 ));
			p=addtag(p,"AND_TO_NODE",name,NULL);
/*			if ((from->ANDID!=DBFReadIntegerAttribute( hDBF, recordnr, 1 )) &&
						  (to->ANDID!=DBFReadIntegerAttribute( hDBF, recordnr, 1 )))
			{
				printf("\rway referres to unattached ANDID! %i %i %i %s\n",from->ANDID,to->ANDID,DBFReadIntegerAttribute( hDBF, recordnr, 1 ),DBFReadStringAttribute( hDBF, recordnr, 15 ));
			}*/
			/*if (to->ANDID!=DBFReadIntegerAttribute( hDBF, recordnr, 0 ))
			{
				//printf("\nway in wrong direction! patch required!\n");
				Err_oneway_way_reversed++;
			}*/
		}
			
		//Field 2: Type=Integer, Title=`LPOLY_', Width=11, Decimals=0
		//Field 3: Type=Integer, Title=`RPOLY_', Width=11, Decimals=0
		//Field 4: Type=Double, Title=`LENGTH', Width=20, Decimals=5
		//Field 5: Type=Integer, Title=`NLD6_', Width=11, Decimals=0
		//Field 6: Type=Integer, Title=`RD_1', Width=11, Decimals=0
		if (!(DBFIsAttributeNULL( hDBF, recordnr, 6)))
		{
			sprintf(name,"AND=%i",DBFReadIntegerAttribute( hDBF, recordnr, 6 ));
			p=addtag(p,"external-ID",name,NULL);
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
		//Field 10: Type=Integer, Title=`RD_5', Width=3, Decimals=0
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
		//Field 12: Type=Integer, Title=`RD_7', Width=2, Decimals=0
		if (DBFReadIntegerAttribute( hDBF, recordnr, 12 )==10)
			p=addtag(p,"railway","rail",NULL); 
			
		
		
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
		if (strcmp(name,"")!=0) p=addtag(p,"nat_ref",name,NULL); 
		
		
		
		
		
		
		
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
	/*	if (!(DBFIsAttributeNULL( hDBF, recordnr,28 )))
			printf("\n%s %f,%f\n",DBFReadStringAttribute( hDBF, recordnr, 28 ),from->ANDID, to->ANDID);*/
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
 
 struct attachedWays * attachway(struct attachedWays * p, struct ways * s) {
	//printf("%p\t%p\n",p,s);
	 if (p==NULL)
	 {
		 p = (struct attachedWays *) calloc(1,sizeof(struct attachedWays));
		 if (p==NULL)
		 {
			 fprintf(stderr,"out of memory\n");
			 exit(1);
		 }
		 p->nextWay=NULL;
		 p->way=s;
	 }
	 else
	 {
		 p->nextWay=attachway(p->nextWay,s);
	 }
	 return p;
 }	
 


 
 void addSegment2Way(struct ways * way,struct segments * segment){
	way->segments=attachsegment(way->segments,segment);
	segment->ways=attachway(segment->ways,way);
	if (way->max_lon < segment->from->lon) way->max_lon=segment->from->lon;
	if (way->max_lon < segment->to->lon) way->max_lon=segment->to->lon;
	if (way->max_lat < segment->from->lat) way->max_lat=segment->from->lat;
	if (way->max_lat < segment->to->lat) way->max_lat=segment->to->lat;
	if (way->min_lon > segment->from->lon) way->min_lon=segment->from->lon;
	if (way->min_lon > segment->to->lon) way->min_lon=segment->to->lon;
	if (way->min_lat > segment->from->lat) way->min_lat=segment->from->lat;
	if (way->min_lat > segment->to->lat) way->min_lat=segment->to->lat;

	
	//printf("node from %i to %i, (%f,%f)-(%f-%f)",segment->from->ID, segment->to->ID,segment->from->lon,segment->from->lat,segment->to->lon,segment->to->lat);
	return;
}



struct ways *newWay(int wayType){
	static long wayID = 0;
	static struct ways *lastway=NULL;
	if (lastway==NULL)
	{
		root_way=calloc(1,sizeof(struct ways));
		if (root_way==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		lastway=root_way;

	}
	else
	{
		lastway->next=calloc(1,sizeof(struct ways));
		if (lastway->next==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		lastway=lastway->next;
	}
	lastway->type=wayType;
	wayID = incr(wayID);
	lastway->wayID=wayID;
	lastway->tag=NULL;
	lastway->segments=NULL;
	lastway->next=NULL;
	lastway->min_lat=999;
	lastway->min_lon=999;
	lastway->max_lat=-1;
	lastway->max_lon=-1;
	
	return lastway;
}
