#include <stdio.h>
#include <stdlib.h>
#include "ways.h"
#include "relations.h"
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




void detachNodes(struct ways* p){
	struct attachedNodes *temp;
	temp=p->nodes;
	while (temp!=NULL)
	{
		temp->node->ways=detachway(temp->node->ways,p);
		p->nodes=temp->nextNode;
		free(temp);
		temp=p->nodes;
	}
}







struct attachedNodes * attachnode(struct attachedNodes* p, struct nodes *s, int invert){
	//printf("%p\t%p\n",p,s);
	// For inverted ways, we add to the front rather than to the back
	if( invert )
	{
		struct attachedNodes *n = (struct attachedNodes *) calloc(1,sizeof(struct attachedNodes));
		if (n==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		n->nextNode=p;
		n->node=s;
		return n;
	}
	if (p==NULL)
	{
		p = (struct attachedNodes *) calloc(1,sizeof(struct attachedNodes));
		if (p==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		p->nextNode=NULL;
		p->node=s;
	}
	else
	{
		p->nextNode=attachnode(p->nextNode,s,0);
	}
	return p;
}




void saveAttachedNodes(struct attachedNodes *p){
	if (p!=NULL)
	{
		fprintf(fp,"		<nd ref=\"%li\" />\n",p->node->ID);
		saveAttachedNodes(p->nextNode);
	}
}




void saveWay(struct ways *p){

	if (postgres)
	{
		struct tags *t;
		struct attachedNodes *n;
		int part = 0;

		if (p->nodes == NULL)
		{
			printf("Way %li doesn't have nodes, ignoring", p->wayID);
			return;
		}

		n = p->nodes;
		long seqid = 1;
		long wayID = p->wayID + part*10000000;
		fprintf(fp_w, "INSERT INTO ways VALUES (%li, GeomFromText('LINESTRING(", wayID);

		for (;n->nextNode;n = n->nextNode)
		{
			fprintf(fp_w, "%1.6f %1.6f,", n->node->lat, n->node->lon);
			fprintf(fp_wn, "%li\t%li\t%li\n", wayID, seqid++, n->node->ID);
		}
		fprintf(fp_w, "%1.6f %1.6f)', 4326));\n", n->node->lat, n->node->lon);
		fprintf(fp_wn, "%li\t%li\t%li\n", wayID, seqid++, n->node->ID);

		for (t = p->tag; t != NULL; t = t->nextTag)
			fprintf(fp_wt, "%li\t%s\t%s\n", wayID, t->key, t->value);
	}		
	else
	{
			
		if (p->type==ROAD)
			fprintf(fp,"	<way id=\"%li\" >\n",p->wayID);
		else if (p->type==AREA)
			fprintf(fp,"    <way id=\"%li\" >\n",p->wayID);
		else fprintf(stderr,"unkown wayType in saveWay\n");
		saveAttachedNodes(p->nodes);
		saveTags(p->tag,NULL);
		if (p->type==ROAD)
			fprintf(fp,"	</way>\n");
		else if (p->type==AREA)
			fprintf(fp,"    </way>\n");
	}
}

void saveWays(){
	int count = 0;
	struct rb_traverser tr;
	struct ways * p;
	rb_t_init (&tr, way_table);
	while ((p=(struct ways *) rb_t_next(&tr))!=NULL)
	{
		count++;
		if( (count%1024) == 0 )
			fprintf(stderr, "\rExporting ways: %d ", count);
		saveWay(p);
	}
	fprintf(stderr, "\rExported ways: %d \n", count);
}


 
void addNode2Way(struct ways * way,struct nodes * node, int invert){
	way->nodes=attachnode(way->nodes,node, invert);
	node->ways=attachway(node->ways,way);
	if (way->max_lon < node->lon) way->max_lon=node->lon;
	if (way->max_lat < node->lat) way->max_lat=node->lat;
	if (way->min_lon > node->lon) way->min_lon=node->lon;
	if (way->min_lat > node->lat) way->min_lat=node->lat;

	node->used = 1;
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
	storeway->nodes=NULL;
	storeway->rels=NULL;
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

