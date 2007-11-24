#include "shapefil.h"

#ifndef _OSM_H_INCLUDED
#define _OSM_H_INCLUDED



#define ROAD 1
#define NODE 2
#define AREA 3


struct relations;

/*struct texts{
	
	unsigned char * text;
	struct texts * btree_l;
	struct texts * btree_h;
	
};*/

struct tags{
	char * key;  /*stored in text b-tree to save memory*/
	char * value;  /*stored in text b-tree to save memory*/
	struct tags* nextTag;
};



struct nodes{
	long ID;
	long ANDID;
	short required;
	short used;
	double lat;
	double lon;
	struct tags * tag; /*contains attached tags */
	struct attachedWays *ways;
};


struct attachedNodes {
	struct attachedNodes *nextNode;
	struct nodes *node;
};

struct attachedRels {
	struct attachedRels *nextRel;
	struct relations *rel;
};

struct ways{
	int type; /*0=way, 1=area*/
	long wayID;
	double max_lat;
	double max_lon;
	double min_lat;
	double min_lon;
	struct tags * tag;
	struct attachedNodes *nodes;
	struct attachedRels *rels;
	struct ways * next;
};


struct attachedWays{
	struct attachedWays *nextWay;
	struct ways *way;
};

struct relations {
        long ID;
        struct relations *next;
        struct attachedWays *ways;
};

int openOutput();
int closeOutput();
void save();
long incr (long i);



long Err_ND_attached_to_way;
long Err_more_NDIDs_per_node;
long oneway_way_reversed;
long Err_toID_without_ANDID;
long Err_fromID_without_ANDID;


struct tags * mkTagList(DBFHandle hDBF,long recordnr,int fileType,struct tags *p,struct nodes * from, struct nodes * to);
int invertRoad(DBFHandle hDBF, long reconrdnr);

#endif  // _OSM_H_INCLUDED
