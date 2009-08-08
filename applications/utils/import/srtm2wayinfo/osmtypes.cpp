/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 * Parts of this file are taken from osm2pgsql.
 */
/** \file
  * The basic types (nodes, ways) and efficient storage for them.
  */
#include "osmtypes.h"
#include <QDebug>

/** Defines the block size. 2^BLOCK_SHIFT nodes are allocated at a time. */
#define BLOCK_SHIFT 5

/** Number of nodes in a block. */
#define PER_BLOCK  (1 << BLOCK_SHIFT)

/** Number of blocks. */
#define NUM_BLOCKS (1 << (31 - BLOCK_SHIFT))

/** Returns the block id for a given nodeid. */
static inline int id2block(int id)
{
    return (id >> BLOCK_SHIFT);
}

/** Returns the offset into the block for a given nodeid.
\sa id2block()
*/
static inline int id2offset(int id)
{
    return id & (PER_BLOCK-1);
}

OsmNodeStorageLarge::OsmNodeStorageLarge()
{
    qDebug() << "Large node storage.";
    nodes = new OsmNode*[NUM_BLOCKS];
}

/** Returns the node object for the given id.
  * If required additional storage space is allocated. */
OsmNode& OsmNodeStorageLarge::operator[](OsmNodeId id)
{
    if (id < 0) {
        /* This saves a lot of overhead for the usually very few IDs */
        return negative_nodes[id];
    }
    int block  = id2block(id);
    int offset = id2offset(id);
    if (!nodes[block]) {
        nodes[block] = (OsmNode *)malloc(PER_BLOCK * sizeof(OsmNode));
        if (!nodes[block]) {
            qDebug() << "Error allocating nodes";
            exit(1);
        }
    }
    return nodes[block][offset];
}

/********************************************************************************/

OsmNodeStorageMedium::OsmNodeStorageMedium()
{
    qDebug() << "Medium node storage.";
}

/** Returns the node object for the given id.
  * If required additional storage space is allocated. */
OsmNode& OsmNodeStorageMedium::operator[](OsmNodeId id)
{
    int block  = id2block(id);
    int offset = id2offset(id);
    if (!blocks.contains(block)) {
        blocks[block] = (OsmNode *)malloc(PER_BLOCK * sizeof(OsmNode));
        if (!blocks[block]) {
            qDebug() << "Error allocating nodes";
            exit(1);
        }
    }
    return blocks[block][offset];
}
