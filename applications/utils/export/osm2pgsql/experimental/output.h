/* Common output layer interface */

/* Each output layer must provide methods for 
 * storing:
 * - Nodes (Points of interest etc)
 * - Way geometries
 * Associated tags: name, type etc. 
*/

#ifndef OUTPUT_H
#define OUTPUT_H

#include "keyvals.h"
#include "middle.h"

struct output_t {
    int (*start)(int dropcreate);
    void (*stop)(void);
    void (*cleanup)(void);
    void (*process)(struct middle_t *mid);
    int (*node)(int id, struct keyval *tags, double node_lat, double node_lon);
    int (*way)(int id, struct keyval *tags, struct osmSegLL *segll, int count);
};

#endif
