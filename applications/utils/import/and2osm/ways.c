#include <stdio.h>
#include <stdlib.h>
#include "ways.h"
#include "segments.h"
#include "tags.h"
#include "osm.h"
#include "rb.h"

extern int postgres;


/* File descriptor for .osm file. */
extern FILE *fp;

/* File descriptors for postgres sql files. */
extern FILE *fp_n;
extern FILE *fp_nt;
extern FILE *fp_w;
extern FILE *fp_wn;
extern FILE *fp_wt;


struct rb_table *way_table;




void detachsegments(struct ways* p){
	struct attachedSegments *temp;
	temp=p->segments;
	while (temp!=NULL)
	{
		temp->segment->ways=detachway(temp->segment->ways,p);
		if(temp->segment->ways==NULL)
		{
			//empty segment
			deleteSegment(temp->segment);
		}
		p->segments=temp->nextSegment;
		free(temp);
		temp=p->segments;
	}
	

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
		p->segment=s;
	}
	else
	{
		p->nextSegment=attachsegment(p->nextSegment,s);
	}
	return p;
}




void saveAttachedSegments(struct attachedSegments *p){
	if (p!=NULL)
	{
		fprintf(fp,"		<seg id=\"%li\" />\n",p->segment->ID);
//		printf("(%2.5f, %2.5f)-(%2.5f,%2.5f)\n",p->Segment->from->lon,p->Segment->from->lat,p->Segment->to->lon,p->Segment->to->lat);
		saveAttachedSegments(p->nextSegment);
	}
}




void saveWay(struct ways *p){

	if (postgres)
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
			fprintf(fp_w, "%1.6f %1.6f,", s->segment->from->lat, s->segment->from->lon);
			fprintf(fp_wn, "%li\t%li\t%li\n", p->wayID, seqid++, s->segment->from->ID);
			if (s->nextSegment == NULL)
			{
				fprintf(fp_w, "%1.6f %1.6f)', 4326));\n", s->segment->to->lat, s->segment->to->lon);
				fprintf(fp_wn, "%li\t%li\t%li\n", p->wayID, seqid++, s->segment->to->ID);
				break;
			}
		}

		for (t = p->tag; t != NULL; t = t->nextTag)
			fprintf(fp_wt, "%li\t%s\t%s\n", p->wayID, t->key, t->value);
	}		
	else
	{
			
		if (p->type==ROAD)
			fprintf(fp,"	<way id=\"%li\" >\n",p->wayID);
		else if (p->type==AREA)
			fprintf(fp,"    <way id=\"%li\" >\n",p->wayID);
		else fprintf(stderr,"unkown wayType in saveWay\n");
		saveTags(p->tag,NULL);
		saveAttachedSegments(p->segments);
		if (p->type==ROAD)
			fprintf(fp,"	</way>\n");
		else if (p->type==AREA)
			fprintf(fp,"    </way>\n");
	}
}

void saveWays(){
	struct rb_traverser tr;
	struct ways * p;
	rb_t_init (&tr, way_table);
	while ((p=(struct ways *) rb_t_next(&tr))!=NULL)
	{
		saveWay(p);
	}
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
	struct ways *storeway, **p;
	static long wayID = 0;
	storeway = (struct ways *) calloc(1,sizeof(struct ways));
	if (storeway==NULL)
	{
		fprintf(stderr,"out of memory\n");
		exit(1);
	}
	storeway->type=wayType;
	wayID = incr(wayID);
	storeway->wayID=wayID;
	storeway->tag=NULL;
	storeway->segments=NULL;
	storeway->min_lat=999;
	storeway->min_lon=999;
	storeway->max_lat=-1;
	storeway->max_lon=-1;
	p=(struct ways **) rb_probe (way_table, storeway);
	if (*p!=storeway)
	{
		//item was already in list
		free(storeway);
		printf("way with duplicate ID found, should not occur!!!\n");
	}
	return *p;
}






int compare_ways (const void *pa, const void *pb, void *param)
{
  const struct ways *a = pa;
  const struct ways *b = pb;

  return (a->wayID > b->wayID) - (a->wayID < b->wayID);

}




void init_ways()
{
	if (way_table==NULL)
	{
		way_table=rb_create (compare_ways, NULL,NULL);
	}
	else
	{
		printf("error: text_table is inited twice\n");
		exit(1);
	}
	return;
};

