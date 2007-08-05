#include <libshp/shapefil.h>

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
	long hashed_lat;
	double lat;
	double lon;
	struct nodes * btree_l;
	struct nodes * btree_h;
	struct tags * tag; /*contains attached tags */
	struct attachedSegments *segments;
	//struct attachedWays *ways;
	unsigned long maxdepth;
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


struct attachedSegments{
	struct attachedSegments *nextSegment;
	struct segments *Segment;
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


int openOutput();
int closeOutput();
void saveTags();
void saveNodes();
void saveSegments();
void saveWays();


void addSegment2Way(struct ways * way,struct segments * segment);
struct ways *newWay(int wayType); 
struct nodes * newNode(double lat, double lon);
struct segments * newSegment(struct nodes * from, struct nodes * to);
struct tags * mkTagList(DBFHandle hDBF,long recordnr,int fileType,struct tags *,struct nodes * from, struct nodes * to);
void save();


long text_maxdepth;
long node_maxdepth;

long Err_ND_attached_to_way;
long Err_more_NDIDs_per_node;
long Err_oneway_way_reversed;
long Err_toID_without_ANDID;
long Err_fromID_without_ANDID;
