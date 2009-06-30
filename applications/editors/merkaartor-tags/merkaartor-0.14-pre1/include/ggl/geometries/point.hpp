// Generic Geometry Library
//
// Copyright Barend Gehrels 1995-2009, Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande 2008, 2009
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef GGL_GEOMETRIES_POINT_HPP
#define GGL_GEOMETRIES_POINT_HPP

#include <cstddef>

#include <boost/mpl/int.hpp>
#include <boost/static_assert.hpp>

#include <ggl/core/access.hpp>
#include <ggl/core/coordinate_type.hpp>
#include <ggl/core/coordinate_system.hpp>
#include <ggl/core/coordinate_dimension.hpp>
#include <ggl/util/math.hpp>

namespace ggl
{

/*!
    \brief Basic point class, having coordinates defined in a neutral way
    \ingroup Geometry
    \tparam T numeric type, for example double, float, int
    \tparam D coordinate dimension as number of coordinates, for example 2
    \tparam C coordinate system, for example cs::cartesian
*/
template<typename T, std::size_t D, typename C>
class point
{
public:

    // Concept typedefs and members
    typedef T coordinate_type;
    typedef C coordinate_system;

    static const std::size_t coordinate_count = D;

    /// Default constructor, no initialization at all
    inline point()
    {}

    /// Compile time access to coordinate values
    template <std::size_t K>
    inline const T& get() const
    {
        BOOST_STATIC_ASSERT(K < D);
        return m_values[K];
    }

    template <std::size_t K>
    inline void set(T value)
    {
        BOOST_STATIC_ASSERT(K < D);
        m_values[K] = value;
    }

    /***
    /// Examine if point is equal to other point
    inline bool operator==(const point& other) const
    {
        for (register std::size_t i = 0; i < D; ++i)
        {
            if (! math::equals(m_values[i], other.m_values[i]))
            {
                return false;
            }
        }
        return true;
    }

    /// Examine if points are NOT equal
    inline bool operator!=(const point& other) const
    {
        return ! operator==(other);
    }
    ***/

    /// Constructs with one, or optionally two or three values
    inline point(const T& v0, const T& v1 = 0, const T& v2 = 0)
    {
        if (D >= 1) m_values[0] = v0;
        if (D >= 2) m_values[1] = v1;
        if (D >= 3) m_values[2] = v2;
    }

private:

    T m_values[D];
};

// Adapt the point to the concept
#ifndef DOXYGEN_NO_TRAITS_SPECIALIZATIONS
namespace traits
{
template <typename T, std::size_t D, typename C>
struct tag<point<T, D, C> >
{
    typedef point_tag type;
};

template<typename T, std::size_t D, typename C>
struct coordinate_type<point<T, D, C> >
{
    typedef T type;
};

template<typename T, std::size_t D, typename C>
struct coordinate_system<point<T, D, C> >
{
    typedef C type;
};

template<typename T, std::size_t D, typename C>
struct dimension<point<T, D, C> > : boost::mpl::int_<D> {};

template<typename T, std::size_t D, typename C>
struct access<point<T, D, C> >
{
    template <std::size_t I>
    static inline T get(const point<T, D, C>& p)
    {
        return p.template get<I>();
    }

    template <std::size_t I>
    static inline void set(point<T, D, C>& p, const T& value)
    {
        p.template set<I>(value);
    }
};

} // namespace traits
#endif // DOXYGEN_NO_TRAITS_SPECIALIZATIONS

} // namespace ggl

#endif // GGL_GEOMETRIES_POINT_HPP
