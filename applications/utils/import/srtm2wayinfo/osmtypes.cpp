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
#define LARGE_BLOCK_SHIFT 8
/** Defines the block size. 2^BLOCK_SHIFT nodes are allocated at a time. */
#define MEDIUM_BLOCK_SHIFT 5

OsmNodeStorageLarge::OsmNodeStorageLarge()
{
    qDebug() << "Large node storage.";
    nodes = new OsmNode*[(1 << (31 - LARGE_BLOCK_SHIFT))];
}

/** Returns the node object for the given id.
  * If required additional storage space is allocated. */
OsmNode& OsmNodeStorageLarge::operator[](OsmNodeId id)
{
    if (id < 0) {
        /* This saves a lot of overhead for the usually very few IDs */
        return negative_nodes[id];
    }
    int block  =  (id >> LARGE_BLOCK_SHIFT);
    int offset = id & ((1 << LARGE_BLOCK_SHIFT)-1);
    if (!nodes[block]) {
        nodes[block] = (OsmNode *)malloc((1 << LARGE_BLOCK_SHIFT) * sizeof(OsmNode));
        if (!nodes[block]) {
            qCritical() << "Error allocating nodes";
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
    int block  =  (id >> MEDIUM_BLOCK_SHIFT);
    int offset = id & ((1 << MEDIUM_BLOCK_SHIFT)-1);
    if (!blocks.contains(block)) {
        blocks[block] = (OsmNode *)malloc((1 << MEDIUM_BLOCK_SHIFT) * sizeof(OsmNode));
        if (!blocks[block]) {
            qCritical() << "Error allocating nodes";
            exit(1);
        }
    }
    return blocks[block][offset];
}
