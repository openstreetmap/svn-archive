// Generic Geometry Library
//
// Copyright Barend Gehrels 1995-2009, Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande 2008, 2009
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef _GEOMETRY_TAGS_HPP
#define _GEOMETRY_TAGS_HPP


/*!
\defgroup traits traits: adapt geometries
\brief Traits classes are small classes or structs to adapt geometries
such that they are recognized by the Generic Geometry Library
*/


namespace geometry
{



	// Tags defining strategies linked to coordinate systems

	/// Tag indicating Cartesian coordinate system family (cartesian,epsg)
	struct cartesian_tag {};


	/// Tag indicating Geographic coordinate system family (geographic)
	struct geographic_tag {};

	/// Tag indicating Spherical coordinate system family (spherical,celestial,...)
	struct spherical_tag {};


	// Tags defining geometry types


	/// "default" tag
	struct geometry_not_recognized_tag {};

	/// OGC Point identifying tag
	struct point_tag {};

	/// OGC Linestring identifying tag
	struct linestring_tag {};

	/// OGC Polygon identifying tag
	struct polygon_tag {};

	/// OGC Multi point identifying tag
	struct multi_point_tag {};

	/// OGC Multi linestring identifying tag
	struct multi_linestring_tag {};

	/// OGC Multi polygon identifying tag
	struct multi_polygon_tag {};

	/// OGC Geometry Collection identifying tag
	struct geometry_collection_tag {};



	/// Convenience (linear) ring identifying tag
	struct ring_tag {};

	/// Convenience 2D or 3D box (mbr) identifying tag
	struct box_tag {};

	/// Convenience 2D (circle) or 3D (sphere) n-sphere identifying tag
	struct nsphere_tag {};

	/// Convenience segment (2-points) identifying tag
	struct segment_tag {};



} // namespace geometry

#endif // _GEOMETRY_TAGS_HPP
