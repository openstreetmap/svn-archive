#include "osm.h"
void detachsegments(struct ways* p);
struct attachedSegments * attachsegment(struct attachedSegments* p, struct segments *s,int invert);
void saveWays();
 void addSegment2Way(struct ways * way,struct segments * segment, int invert);
 struct ways *newWay(int wayType);
 void init_ways();
 
