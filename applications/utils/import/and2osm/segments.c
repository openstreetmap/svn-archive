#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "ways.h"
#include "rb.h"
#include "osm.h"
#include "segments.h"
extern int postgres;


/* File descriptor for .osm file. */
extern FILE *fp;

/* File descriptors for postgres sql files. */
extern FILE *fp_n;
extern FILE *fp_nt;
extern FILE *fp_w;
extern FILE *fp_wn;
extern FILE *fp_wt;


struct rb_table * segments_table=NULL;




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













long saveSegment(struct segments *p){
	
	assert(fp!=NULL);
	assert(p!=NULL);
	assert(p->from!=NULL);
	assert(p->to!=NULL);
	if (!postgres)
		fprintf(fp,"	<segment id=\"%li\" from=\"%li\" to=\"%li\" />\n",p->ID,(p->from)->ID,(p->to)->ID);
	return 0;
}
		
void saveSegments(){
	if (!postgres)
	{
			
		struct rb_traverser tr;
		struct segments * p;
		rb_t_init (&tr, segments_table);
		while ((p=(struct segments *) rb_t_next(&tr))!=NULL)
		{
			saveSegment(p);
		}
	}
}

void deleteSegment(struct segments * p)
{
	rb_delete (segments_table, (const void *) p);
}
 


struct segments * addSegment(struct nodes * from, struct nodes * to)
{
	static long segmentID=0;
	struct segments * storesegment;
	struct segments ** p;
	storesegment = (struct segments *) calloc(1,sizeof(struct segments));
	if (storesegment==NULL)
	{
		fprintf(stderr,"out of memory\n");
		exit(1);
	}


	segmentID = incr(segmentID);
	storesegment->ID=segmentID;
	storesegment->from=from;
	storesegment->to=to;
	storesegment->next=NULL;
	storesegment->ways=NULL;
	p=(struct segments **) rb_probe (segments_table, storesegment);
	if (*p!=storesegment)
	{
		//item was already in list
		free(storesegment);
	}
	else
	{		
		from->segments=attachsegment(from->segments,*p);
		/*update to node's segment list*/
		to->segments=attachsegment(to->segments,*p);
	}
	
	return *p;
}

int compare_segments (const void *pa, const void *pb, void *param)
{
	const struct segments * a=pa;
	const struct segments * b=pb;
	return (a->ID > b->ID) - (a->ID < b->ID);
}
	




void init_segments()
{
	if (segments_table==NULL)
	{
		segments_table=rb_create (compare_segments, NULL,NULL);
	}
	else
	{
		printf("error: segments_table is inited twice\n");
		exit(1);
	}
	return;
};
