#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "ways.h"
#include "rb.h"
#include "osm.h"
#include "relations.h"
extern int postgres;


/* File descriptor for .osm file. */
extern FILE *fp;

/* File descriptors for postgres sql files. */
extern FILE *fp_n;
extern FILE *fp_nt;
extern FILE *fp_w;
extern FILE *fp_wn;
extern FILE *fp_wt;


struct rb_table * relations_table=NULL;




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

struct attachedWays * detachway(struct attachedWays * p, struct ways * s) {
	//printf("%p\t%p\n",p,s);
	struct attachedWays * rv,*temp;
	if ((p!=NULL)&&(p->way==s))
	{
		//first attached way is way we are looking for
		rv=p->nextWay;
		free(p);
	}
	else
	{
		rv=p;
		while (p->nextWay!=NULL)
		{
			if (p->nextWay->way==s)
			{
				temp=p->nextWay;
				p->nextWay=p->nextWay->nextWay;
				free(temp);
				return rv;
			}
			p=p->nextWay;
		}	
	}
	return rv;
}



void addWay2Relation( struct ways *way, struct relations *rel )
{
	way->rels=attachrelation(way->rels,rel);
	rel->ways=attachway(rel->ways,way);
	return;
}


struct attachedRels * attachrelation(struct attachedRels * p, struct relations * s) {
	//printf("%p\t%p\n",p,s);
	if (p==NULL)
	{
		p = (struct attachedRels *) calloc(1,sizeof(struct attachedRels));
		if (p==NULL)
		{
			fprintf(stderr,"out of memory\n");
			exit(1);
		}
		p->nextRel=NULL;
		p->rel=s;
	}
	else
	{
		p->nextRel=attachrelation(p->nextRel,s);
	}
	return p;
}







/* We don't output relations yet */
long saveRelation(struct relations *p){
	
	assert(fp!=NULL);
	assert(p!=NULL);
//	if (!postgres)
//		fprintf(fp,"	<segment id=\"%li\" from=\"%li\" to=\"%li\" />\n",p->ID,(p->from)->ID,(p->to)->ID);
	return 0;
}
		
void saveRelations(){
	if (!postgres)
	{
		int count = 0;
		struct rb_traverser tr;
		struct relations * p;
		rb_t_init (&tr, relations_table);
		while ((p=(struct relations *) rb_t_next(&tr))!=NULL)
		{
			count++;
			if( (count%1024) == 0 )
				fprintf(stderr, "\rExporting relations: %d ", count);
			saveRelation(p);
		}
		fprintf(stderr, "\rExported relations: %d \n", count);
	}
}

void deleteRelation(struct relations * p)
{
	rb_delete (relations_table, (const void *) p);
}
 

struct relations * addRelation()
{
	static long relationID=0;
	struct relations * storerelation;
	struct relations ** p;
	storerelation = (struct relations *) calloc(1,sizeof(struct relations));
	if (storerelation==NULL)
	{
		fprintf(stderr,"out of memory\n");
		exit(1);
	}


	relationID = incr(relationID);
	storerelation->ID=relationID;
	storerelation->next=NULL;
	storerelation->ways=NULL;
	p=(struct relations **) rb_probe (relations_table, storerelation);
	if (*p!=storerelation)
	{
		//item was already in list
		free(storerelation);
	}
	else
	{		
//		from->segments=attachsegment(from->segments,*p,0);
		/*update to node's segment list*/
//		to->segments=attachsegment(to->segments,*p,0);
	}
	
	return *p;
}

int compare_relations (const void *pa, const void *pb, void *param)
{
	const struct relations * a=pa;
	const struct relations * b=pb;
	return (a->ID > b->ID) - (a->ID < b->ID);
}
	




void init_relations()
{
	if (relations_table==NULL)
	{
		relations_table=rb_create (compare_relations, NULL,NULL);
	}
	else
	{
		printf("error: relations is inited twice\n");
		exit(1);
	}
	return;
};
