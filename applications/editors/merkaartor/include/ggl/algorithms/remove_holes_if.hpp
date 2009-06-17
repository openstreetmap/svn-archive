// Generic Geometry Library
//
// Copyright Barend Gehrels 1995-2009, Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande 2008, 2009
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef GGL_ALGORITHM_REMOVE_HOLES_IF_HPP
#define GGL_ALGORITHM_REMOVE_HOLES_IF_HPP

#include <algorithm>

#include <ggl/algorithms/area.hpp>
#include <ggl/algorithms/perimeter.hpp>

#include <ggl/core/interior_rings.hpp>



namespace ggl
{



#ifndef DOXYGEN_NO_IMPL
namespace impl { namespace remove_holes_if {


template<typename Polygon, typename Predicate>
struct polygon_remove_holes_if
{
    static inline void modify(Polygon& poly, Predicate const& predicate)
    {
        typename interior_type<Polygon>::type& rings = interior_rings(poly);

        // Remove rings using erase-remove-idiom
        // http://en.wikipedia.org/wiki/Erase-remove_idiom
        rings.erase(
            std::remove_if(boost::begin(rings), boost::end(rings), predicate),
            boost::end(rings));
    }
};

}} // namespace impl::remove_holes_if


#endif // DOXYGEN_NO_IMPL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch {

// Default implementation does nothing
template <typename Tag, typename Geometry, typename Predicate>
struct remove_holes_if
{
    static inline void modify(Geometry&, Predicate const& )
    {}
};



template <typename Geometry, typename Predicate>
struct remove_holes_if<polygon_tag, Geometry, Predicate>
    : impl::remove_holes_if::polygon_remove_holes_if<Geometry, Predicate>
{
};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH

/*!
    \brief Remove holes from a geometry (polygon, multi-polygon) using a specified condition
 */
template <typename Geometry, typename Predicate>
inline void remove_holes_if(Geometry& geometry, Predicate const& predicate)
{
    dispatch::remove_holes_if
        <
            typename tag<Geometry>::type,
            Geometry,
            Predicate
        >::modify(geometry, predicate);
}







// CONVENIENT PREDICATES might be moved elsewhere
template <typename Ring>
struct elongated_hole
{
    inline elongated_hole(double ratio)
        : m_ratio(ratio)
    {}

    inline bool operator()(Ring const& ring) const
    {
        if (ring.size() >= 4)
        {
            double a = area(ring);
            double p = perimeter(ring);
            return std::abs(a/p) < m_ratio;
        }
        // Rings with less then 4 points (including closing)
        // are also considered as small and thus removed
        return true;
    }
private :
    double m_ratio;
};


template <typename Ring>
struct invalid_hole
{
    inline bool operator()(Ring const& ring) const
    {
        return ring.size() < 4;
    }
};


} // namespace ggl


#endif // GGL_ALGORITHM_REMOVE_HOLES_IF_HPP
