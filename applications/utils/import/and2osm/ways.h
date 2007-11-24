#include "osm.h"
void detachnodes(struct ways* p);
struct attachedNodes * attachnode(struct attachedNodes* p, struct nodes *s,int invert);
void saveWays();
 void addNode2Way(struct ways * way,struct nodes * node, int invert);
 struct ways *newWay(int wayType);
 void init_ways();
 
