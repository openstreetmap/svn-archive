#include "shapefil.h"

#ifndef _OSM_H_INCLUDED
#define _OSM_H_INCLUDED



#define ROAD 1
#define NODE 2
#define AREA 3



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
	double lat;
	double lon;
	struct tags * tag; /*contains attached tags */
	struct attachedSegments *segments;
};


struct attachedSegments{
	struct attachedSegments *nextSegment;
	struct segments *segment;
};

struct ways{
	int type; /*0=way, 1=area*/
	long wayID;
	double max_lat;
	double max_lon;
	double min_lat;
	double min_lon;
	struct tags * tag;
	struct attachedSegments *segments;
	struct ways * next;
};


struct attachedWays{
	struct attachedWays *nextWay;
	struct ways *way;
};

struct segments{
	long ID;
	struct nodes * from;
	struct nodes * to;
	struct segments * next;
	struct attachedWays *ways;
};



int openOutput();
int closeOutput();
void save();
long incr (long i);



long Err_ND_attached_to_way;
long Err_more_NDIDs_per_node;
long Err_oneway_way_reversed;
long Err_toID_without_ANDID;
long Err_fromID_without_ANDID;


struct tags * mkTagList(DBFHandle hDBF,long recordnr,int fileType,struct tags *p,struct nodes * from, struct nodes * to);

#endif  // _OSM_H_INCLUDED
