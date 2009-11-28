/*
#-----------------------------------------------------------------------------
# Part of osm2pgsql utility
#-----------------------------------------------------------------------------
# By Artem Pavlenko, Copyright 2007
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-----------------------------------------------------------------------------
*/

#include <iostream>
#include <cstring>
#include <cstdlib>

/* Need to know which geos version we have to work out which headers to include */
#include <geos/version.h>

#if (GEOS_VERSION_MAJOR==3)
/* geos trunk (3.0.0+) */
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/LineString.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/MultiLineString.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/Point.h>
#include <geos/io/WKTReader.h>
#include <geos/io/WKTWriter.h>
#include <geos/util/GEOSException.h>
#include <geos/opLinemerge.h>
using namespace geos::geom;
using namespace geos::io;
using namespace geos::util;
using namespace geos::operation::linemerge;
#else
/* geos-2.2.3 */
#include <geos/geom.h>
#include <geos/io.h>
#include <geos/opLinemerge.h>
using namespace geos;
#endif

#include "build_geometry.h"

typedef std::auto_ptr<Geometry> geom_ptr;

static std::vector<std::string> wkts;
static std::vector<double> areas;


char *get_wkt_simple(osmNode *nodes, int count, int polygon) {
    GeometryFactory gf;
    std::auto_ptr<CoordinateSequence> coords(gf.getCoordinateSequenceFactory()->create(0, 2));

    try
    {
        for (int i = 0; i < count ; i++) {
            Coordinate c;
            c.x = nodes[i].lon;
            c.y = nodes[i].lat;
            coords->add(c, 0);
        }

        geom_ptr geom;
        if (polygon && (coords->getSize() >= 4) && (coords->getAt(coords->getSize() - 1).equals2D(coords->getAt(0)))) {
            std::auto_ptr<LinearRing> shell(gf.createLinearRing(coords.release()));
            geom = geom_ptr(gf.createPolygon(shell.release(), new std::vector<Geometry *>));
            geom->normalize(); // Fix direction of ring
        } else {
            if (coords->getSize() < 2)
                return NULL;
            geom = geom_ptr(gf.createLineString(coords.release()));
        }

        WKTWriter wktw;
        std::string wkt = wktw.write(geom.get());
        return strdup(wkt.c_str());
    }
    catch (...)
    {
        std::cerr << std::endl << "excepton caught processing way" << std::endl;
        return NULL;
    }
}


size_t get_wkt_split(osmNode *nodes, int count, int polygon, double split_at) {
    GeometryFactory gf;
    std::auto_ptr<CoordinateSequence> coords(gf.getCoordinateSequenceFactory()->create(0, 2));
    double area;
    WKTWriter wktw;
    size_t wkt_size = 0;

    try
    {
        for (int i = 0; i < count ; i++) {
            Coordinate c;
            c.x = nodes[i].lon;
            c.y = nodes[i].lat;
            coords->add(c, 0);
        }

        geom_ptr geom;
        if (polygon && (coords->getSize() >= 4) && (coords->getAt(coords->getSize() - 1).equals2D(coords->getAt(0)))) {
            std::auto_ptr<LinearRing> shell(gf.createLinearRing(coords.release()));
            geom = geom_ptr(gf.createPolygon(shell.release(), new std::vector<Geometry *>));
            geom->normalize(); // Fix direction of ring
            area = geom->getArea();
            std::string wkt = wktw.write(geom.get());
            wkts.push_back(wkt);
            areas.push_back(area);
            wkt_size++;
        } else {
            if (coords->getSize() < 2)
                return 0;

            double distance = 0;
            std::auto_ptr<CoordinateSequence> segment;
            segment = std::auto_ptr<CoordinateSequence>(gf.getCoordinateSequenceFactory()->create(0, 2));
            segment->add(coords->getAt(0));
            for(unsigned i=1; i<coords->getSize(); i++) {
                segment->add(coords->getAt(i));
                distance += coords->getAt(i).distance(coords->getAt(i-1));
                if ((distance >= split_at) || (i == coords->getSize()-1)) {
                    geom = geom_ptr(gf.createLineString(segment.release()));
                    std::string wkt = wktw.write(geom.get());
                    wkts.push_back(wkt);
                    areas.push_back(0);
                    wkt_size++;
                    distance=0;
                    segment = std::auto_ptr<CoordinateSequence>(gf.getCoordinateSequenceFactory()->create(0, 2));
                    segment->add(coords->getAt(i));
                }
            }
        }

    }
    catch (...)
    {
        std::cerr << std::endl << "excepton caught processing way" << std::endl;
        wkt_size = 0;
    }
    return wkt_size;
}


char * get_wkt(size_t index)
{
//   return wkts[index].c_str();
	char *result;
	result = (char*) std::malloc( wkts[index].length() + 1);
        // At least give some idea of why we about to seg fault
        if (!result) std::cerr << std::endl << "Unable to allocate memory: " << (wkts[index].length() + 1) << std::endl;
	std::strcpy(result, wkts[index].c_str());
	return result;
}

double get_area(size_t index)
{
    return areas[index];
}

void clear_wkts()
{
   wkts.clear();
   areas.clear();
}

static int coords2nodes(CoordinateSequence * coords, struct osmNode ** nodes) {
    size_t			num_coords;
    size_t			i;
    Coordinate		coord;

    num_coords = coords->getSize();
    *nodes = (struct osmNode *) malloc(num_coords * sizeof(struct osmNode));

    for (i = 0; i < num_coords; i++) {
        coord = coords->getAt(i);
        (*nodes)[i].lon = coord.x;
        (*nodes)[i].lat = coord.y;
    }
    return num_coords;
}

int parse_wkt(const char * wkt, struct osmNode *** xnodes, int ** xcount, int * polygon) {
    GeometryFactory		gf;
    WKTReader		reader(&gf);
    std::string		wkt_string(wkt);
    Geometry *		geometry;
    const Geometry *	subgeometry;
    GeometryCollection *	gc;
    CoordinateSequence *	coords;
    size_t			num_geometries;
    size_t			i;
	
    *polygon = 0;
    try {
        geometry = reader.read(wkt_string);
        switch (geometry->getGeometryTypeId()) {
            // Single geometries
            case GEOS_POLYGON:
                // Drop through
            case GEOS_LINEARRING:
                *polygon = 1;
                // Drop through
            case GEOS_POINT:
                // Drop through
            case GEOS_LINESTRING:
                *xnodes = (struct osmNode **) malloc(2 * sizeof(struct osmNode *));
                *xcount = (int *) malloc(sizeof(int));
                coords = geometry->getCoordinates();
                (*xcount)[0] = coords2nodes(coords, &((*xnodes)[0]));
                (*xnodes)[1] = NULL;
                delete coords;
                break;
            // Geometry collections
            case GEOS_MULTIPOLYGON:
                *polygon = 1;
                // Drop through
            case GEOS_MULTIPOINT:
                // Drop through
            case GEOS_MULTILINESTRING:
                gc = (GeometryCollection *) geometry;
                num_geometries = gc->getNumGeometries();
                *xnodes = (struct osmNode **) malloc((num_geometries + 1) * sizeof(struct osmNode *));
                *xcount = (int *) malloc(num_geometries * sizeof(int));
                for (i = 0; i < num_geometries; i++) {
                    subgeometry = gc->getGeometryN(i);
                    coords = subgeometry->getCoordinates();
                    (*xcount)[0] = coords2nodes(coords, &((*xnodes)[i]));
                    delete coords;
                }
                (*xnodes)[i] = NULL;
                break;
            default:
                std::cerr << std::endl << "unexpected object type while processing PostGIS data" << std::endl;
                delete geometry;
                return -1;
        }
        delete geometry;
    } catch (...) {
        std::cerr << std::endl << "excepton caught parsing PostGIS data" << std::endl;
        return -1;
    }
    return 0;
}

struct polygondata
{
    Polygon*        polygon;
    LinearRing*     ring;
    double          area;
    int             iscontained;
    unsigned        containedbyid;
};

static int polygondata_comparearea(const void* vp1, const void* vp2)
{
    const polygondata* p1 = (const polygondata*)vp1;
    const polygondata* p2 = (const polygondata*)vp2;

    if (p1->area == p2->area) return 0;
    if (p1->area > p2->area) return -1;
    return 1;
}

size_t build_geometry(int osm_id, struct osmNode **xnodes, int *xcount, int make_polygon, double split_at) {
    size_t wkt_size = 0;
    std::auto_ptr<std::vector<Geometry*> > lines(new std::vector<Geometry*>);
    GeometryFactory gf;
    geom_ptr geom;

    try
    {
        for (int c=0; xnodes[c]; c++) {
            std::auto_ptr<CoordinateSequence> coords(gf.getCoordinateSequenceFactory()->create(0, 2));
            for (int i = 0; i < xcount[c]; i++) {
                struct osmNode *nodes = xnodes[c];
                Coordinate c;
                c.x = nodes[i].lon;
                c.y = nodes[i].lat;
                coords->add(c, 0);
            }
            if (coords->getSize() > 1) {
                geom = geom_ptr(gf.createLineString(coords.release()));
                lines->push_back(geom.release());
            }
        }

        //geom_ptr segment(0);
        geom_ptr mline (gf.createMultiLineString(lines.release()));
        //geom_ptr noded (segment->Union(mline.get()));
        LineMerger merger;
        //merger.add(noded.get());
        merger.add(mline.get());
        std::auto_ptr<std::vector<LineString *> > merged(merger.getMergedLineStrings());
        WKTWriter writer;

        // Procces ways into lines or simple polygon list
        polygondata* polys = new polygondata[merged->size()];

        unsigned totalpolys = 0;
        for (unsigned i=0 ;i < merged->size(); ++i)
        {
            std::auto_ptr<LineString> pline ((*merged ) [i]);
            if (make_polygon && pline->getNumPoints() > 3 && pline->isClosed())
            {
                polys[totalpolys].polygon = gf.createPolygon(gf.createLinearRing(pline->getCoordinates()),0);
                polys[totalpolys].ring = gf.createLinearRing(pline->getCoordinates());
                polys[totalpolys].area = polys[totalpolys].polygon->getArea();
                polys[totalpolys].iscontained = 0;
                if (polys[totalpolys].area > 0.0) totalpolys++;
                else delete(polys[totalpolys].polygon);
            }
            else
            {
                        //std::cerr << "polygon(" << osm_id << ") is no good: points(" << pline->getNumPoints() << "), closed(" << pline->isClosed() << "). " << writer.write(pline.get()) << std::endl;
                double distance = 0;
                std::auto_ptr<CoordinateSequence> segment;
                segment = std::auto_ptr<CoordinateSequence>(gf.getCoordinateSequenceFactory()->create(0, 2));
                segment->add(pline->getCoordinateN(0));
                for(unsigned i=1; i<pline->getNumPoints(); i++) {
                    segment->add(pline->getCoordinateN(i));
                    distance += pline->getCoordinateN(i).distance(pline->getCoordinateN(i-1));
                    if ((distance >= split_at) || (i == pline->getNumPoints()-1)) {
                        geom = geom_ptr(gf.createLineString(segment.release()));
                        std::string wkt = writer.write(geom.get());
                        wkts.push_back(wkt);
                        areas.push_back(0);
                        wkt_size++;
                        distance=0;
                        segment = std::auto_ptr<CoordinateSequence>(gf.getCoordinateSequenceFactory()->create(0, 2));
                        segment->add(pline->getCoordinateN(i));
                    }
                }
                //std::string text = writer.write(pline.get());
                //wkts.push_back(text);
                //areas.push_back(0.0);
                //wkt_size++;
            }
        }

//        std::cerr << std::endl << "polygons found = " << totalpolys << std::endl;
        
        if (totalpolys)
        {
            qsort(polys, totalpolys, sizeof(polygondata), polygondata_comparearea);

            unsigned toplevelpolygons = 0;
            int istoplevelafterall;

            for (unsigned i=0 ;i < totalpolys; ++i)
            {
                if (polys[i].iscontained) continue;
                toplevelpolygons++;
                for (unsigned j=i+1; j < totalpolys; ++j)
                {
                    // Does [i] contain a smaller polygon?
                    if (polys[i].polygon->contains(polys[j].polygon))
                    {
                        // are we in a [i] contains [k] contains [j] situation
                        // which would actually make j top level
                        istoplevelafterall = 0;
                        for (unsigned k=i+1; k < j; ++k)
                        {
                            if (polys[k].iscontained && polys[k].containedbyid == i && polys[k].polygon->contains(polys[j].polygon))
                            {
                                istoplevelafterall = 1;
                                break;
                            }
                        }
                        if (!istoplevelafterall)
                        {
                            polys[j].iscontained = 1;
                            polys[j].containedbyid = i;
                        }
                    }
                }
            }
            // polys now is a list of ploygons tagged with which ones are inside each other

            // List of polygons for multipolygon
            std::auto_ptr<std::vector<Geometry*> > polygons(new std::vector<Geometry*>);

            // For each top level polygon create a new polygon including any holes
            for (unsigned i=0 ;i < totalpolys; ++i)
            {
                if (polys[i].iscontained) continue;

                // List of holes for this top level polygon
                std::auto_ptr<std::vector<Geometry*> > interior(new std::vector<Geometry*>);
                for (unsigned j=i+1; j < totalpolys; ++j)
                {
                   if (polys[j].iscontained && polys[j].containedbyid == i)
                   {
                       interior->push_back(polys[j].ring);
                   }
                }
                
                Polygon* poly(gf.createPolygon(polys[i].ring, interior.release()));
                poly->normalize();
                polygons->push_back(poly);
            }

            // Make a multipolygon if required
            if (toplevelpolygons == 1)
            {
                Polygon* poly = (Polygon*)polygons->at(0);
                std::string text = writer.write(poly);
                wkts.push_back(text);
                wkt_size++;
                delete(poly);
            }
            else
            {
                std::auto_ptr<MultiPolygon> multipoly(gf.createMultiPolygon(polygons.release()));
                std::string text = writer.write(multipoly.get());
                wkts.push_back(text);
                wkt_size++;
            }
        }

        for (unsigned i=0; i < totalpolys; ++i)
        {
            delete(polys[i].polygon);
        }
        delete[](polys);
    }
    catch (...)
    {
        std::cerr << std::endl << "excepton caught processing way id=" << osm_id << std::endl;
        wkt_size = 0;
    }

    return wkt_size;
}
