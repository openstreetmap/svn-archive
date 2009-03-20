// Generic Geometry Library
//
// Copyright Barend Gehrels 1995-2009, Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande 2008, 2009
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)


#ifndef _GEOMETRY_RADIAN_ACCESS_HPP
#define _GEOMETRY_RADIAN_ACCESS_HPP


#include <geometry/core/access.hpp>
#include <geometry/core/cs.hpp>

#include <geometry/util/math.hpp>


namespace geometry
{

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{
		template <size_t D, typename G, typename DR>
		struct radian_access
		{
		};

		template <size_t D, typename G, template<typename> class CS>
		struct radian_access<D, G, CS<radian> >
		{
			typedef typename coordinate_type<G>::type T;

			static inline T get(const G& geometry)
			{
				return geometry::get<D>(geometry);
			}

			static inline void set(G& geometry, const typename coordinate_type<G>::type& radians)
			{
				geometry::set<D>(geometry, radians);
			}

		};

		template <size_t D, typename G, template<typename> class CS>
		struct radian_access<D, G, CS<degree> >
		{
			typedef typename coordinate_type<G>::type T;

			static inline T get(const G& geometry)
			{
				return geometry::get<D>(geometry) * math::d2r;
			}

			static inline void set(G& geometry, const typename coordinate_type<G>::type& radians)
			{
				geometry::set<D>(geometry, radians * math::r2d);
			}

		};

	} // namespace impl
	#endif


	/*!
		\brief get a coordinate value of a point, result is in RADIAN
		\details also if coordinate system was in degree, result is in radian
		\return coordinate value
		\ingroup access
		\tparam D dimension
		\tparam G geometry
		\param geometry geometry to get coordinate value from
	*/
	template <size_t D, typename G>
	inline typename coordinate_type<G>::type get_as_radian(const G& geometry)
	{
		return impl::radian_access<D, G, typename coordinate_system<G>::type>::get(geometry);
	};


	/*!
		\brief assign coordinate value (which is in radian) to a point
		\details if coordinate system of point is in degree, will be converted to degree
		\ingroup access
		\tparam D dimension
		\tparam G geometry
		\param geometry geometry to assign coordinate to
		\param radians coordinate value to assign
	*/
	template <size_t D, typename G>
	inline void set_from_radian(G& geometry, const typename coordinate_type<G>::type& radians)
	{
		impl::radian_access<D, G, typename coordinate_system<G>::type>::set(geometry, radians);
	}


}


#endif // _GEOMETRY_RADIAN_ACCESS_HPP
