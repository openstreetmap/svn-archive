#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "rb.h"
#include "osm.h"
#include "tags.h"
#include "nodes.h"

extern int postgres;
extern int Nodes_Deleted;

/* File descriptor for .osm file. */
extern FILE *fp;

/* File descriptors for postgres sql files. */
extern FILE *fp_n;
extern FILE *fp_nt;
extern FILE *fp_w;
extern FILE *fp_wn;
extern FILE *fp_wt;




struct rb_table * nodes_table;


void saveNode(struct nodes *p){
	if (postgres)
		fprintf(fp_n, "EXECUTE nodes_insert(%li, %1.6f, %1.6f);\n", p->ID, p->lat, p->lon);
	else 
		fprintf(fp,"	<node id=\"%li\" lat=\"%1.5f\" lon=\"%1.5f\" >\n",p->ID,p->lat,p->lon);
	
	saveTags(p->tag,p);
	if (!postgres)
		fprintf(fp,"		<tag k=\"source\" v=\"AND\" />\n	</node>\n");
}

void saveNodes(){
	int count = 0;
	struct rb_traverser tr;
	struct nodes * p;
	rb_t_init (&tr, nodes_table);
	while ((p=(struct nodes *) rb_t_next(&tr))!=NULL)
	{
		if( !p->used )
		{
			Nodes_Deleted++;
			continue;
		}
		count++;
		if( (count%1024) == 0 )
			fprintf(stderr, "\rExporting nodes: %d ", count);
		saveNode(p);
	}
	fprintf(stderr, "\rExported nodes: %d \n", count);
}

int compare_nodes (const void *pa, const void *pb, void *param){
	const struct nodes * na=pa;
	const struct nodes * nb=pb;
	if (na->lat < nb->lat) return -1;
	if (na->lat > nb->lat) return 1;
	if (na->lon < nb->lon) return -1;
	if (na->lon > nb->lon) return 1;
	return 0;
}

struct nodes * addNode(double lat, double lon){
	static long nodeID;
	struct nodes  * storenode;
	struct nodes ** p;
	storenode = (struct nodes *) calloc(1,sizeof(struct nodes));
	if (storenode==NULL)
	{
		fprintf(stderr,"out of memory\n");
		exit(1);
	}

	nodeID = incr(nodeID);
	storenode->ID=nodeID;
	storenode->lat=lat;
	storenode->lon=lon;
	storenode->tag=NULL;
	storenode->segments=NULL;
	p=(struct nodes  **) rb_probe (nodes_table, storenode);
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
};
