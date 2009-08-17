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


void OsmWayStorageMem::append(OsmWay &way)
{
    OsmWay *cloned = new OsmWay(way);
    cloned->nodes.squeeze();
    ways.append(cloned);
}

bool OsmWayStorageMem::get(OsmWay &output)
{
    if (pos >= ways.size()) {
        return false;
    }
    output = *(ways.at(pos++));
    return true;
}

OsmWayStorageDisk::OsmWayStorageDisk(QString tmp_dir)
{
    file = new QTemporaryFile(tmp_dir+"/ways_XXXXXX");
    if (file->open()) {
        stream.setDevice(file);
    } else {
        qCritical() << "Could not create temporary file in" << tmp_dir;
        exit(1);
    }
}


void OsmWayStorageDisk::append(OsmWay &input)
{
    stream << input.id << input.nodes;
}

void OsmWayStorageDisk::startReading()
{
    file->seek(0);
}


bool OsmWayStorageDisk::get(OsmWay &output)
{
    stream >> output.id >> output.nodes;
    return stream.status() == QDataStream::Ok;
}


OsmWayStorageDisk::~OsmWayStorageDisk()
{
    stream.setDevice(0);
    file->close();
    //delete file;
}