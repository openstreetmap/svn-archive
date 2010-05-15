/* Data types to hold OSM node, segment, way data */

#ifndef OSMTYPES_H
#define OSMTYPES_H


struct osmNode {
    int id;
    double lon;
    double lat;
};

struct osmWay {
    int id;
    int nds[1];  /* In actual usage it contains all the nodes, 0 terminated */
};
/* exit_nicely - called to cleanup after fatal error */
void exit_nicely(void);

#endif
