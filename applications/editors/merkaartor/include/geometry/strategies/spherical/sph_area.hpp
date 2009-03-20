// Generic Geometry Library
//
// Copyright Barend Gehrels 1995-2009, Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande 2008, 2009
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef _GEOMETRY_STRATEGY_SPHERICAL_AREA_HPP
#define _GEOMETRY_STRATEGY_SPHERICAL_AREA_HPP


#include <geometry/strategies/spherical/sph_distance.hpp>

#include <geometry/geometries/segment.hpp>


namespace geometry
{
	namespace strategy
	{
		namespace area
		{
			/*!
				\brief Area calculation by spherical excess
				\tparam P type of points of rings/polygons
				\author Barend Gehrels. Adapted from:
				- http://www.soe.ucsc.edu/~pang/160/f98/Gems/GemsIV/sph_poly.c
				- http://williams.best.vwh.net/avform.htm
				\note The version in Gems didn't account for polygons crossing the 180 meridian.
				\note This version works for convex and non-convex polygons, for 180 meridian
				crossing polygons and for polygons with holes. However, some cases (especially
				180 meridian cases) must still be checked.
				\note The version which sums angles, which is often seen, doesn't handle non-convex
				polygons correctly.
				\note The version which sums longitudes, see
				http://trs-new.jpl.nasa.gov/dspace/bitstream/2014/40409/1/07-03.pdf, is simple
				and works well in most cases but not in 180 meridian crossing cases. This probably
				could be solved.
			*/
			template<typename P>
			class by_spherical_excess
			{
				private :
					struct excess_sum
					{
						double sum;
						inline excess_sum() : sum(0) {}
						inline double area() const
						{
							return - sum * constants::average_earth_radius * constants::average_earth_radius;
						}
					};

					// Distances are calculated on unit sphere here
					strategy::distance::haversine<P, P> m_unit_sphere;

				public :
					typedef excess_sum state_type;

					by_spherical_excess()
						: m_unit_sphere(1)
					{}

					inline bool operator()(const segment<const P>& segment, state_type& state) const
					{
						if (get<0>(segment.first) != get<0>(segment.second))
						{
							typedef point_ll<typename coordinate_type<P>::type, cs::geographic<radian> > PR;
							PR p1, p2;

							// Select transformation strategy and transform to radians (if necessary)
							typename strategy_transform<
										typename cs_tag<P>::type,
										typename cs_tag<PR>::type,
										typename coordinate_system<P>::type,
										typename coordinate_system<PR>::type,
										dimension<P>::value,
										dimension<PR>::value,
										P, PR>::type transform_strategy;

							transform_strategy(segment.first, p1);
							transform_strategy(segment.second, p2);

							// Distance p1 p2
							double a = m_unit_sphere(segment.first, segment.second);
							// Sides on unit sphere to south pole
							double b = 0.5 * math::pi - p2.lat();
							double c = 0.5 * math::pi - p1.lat();
							// Semi parameter
							double s = 0.5 * (a + b + c);

							// E: spherical excess, using l'Huiller's formula
							// [tg(e / 4)]2   =   tg[s / 2]  tg[(s-a) / 2]  tg[(s-b) / 2]  tg[(s-c) / 2]
							double E = 4.0 * atan(sqrt(fabs(tan(s / 2) * tan((s - a) / 2) * tan((s - b) / 2) * tan((s - c) / 2))));

							E = fabs(E);

							// In right direction: positive, add area. In left direction: negative, subtract area.
							// Longitude comparisons are not so obvious. If one is negative, other is positive,
							// we have to take the date into account.
							// TODO: check this / enhance this, should be more robust. See also the "grow" for ll
							// TODO: use minmax or "smaller"/"compare" strategy for this
							double lon1 = p1.lon() < 0 ? p1.lon() + math::two_pi : p1.lon();
							double lon2 = p2.lon() < 0 ? p2.lon() + math::two_pi : p2.lon();

							if (lon2 < lon1)
							{
								E= -E;
							}

							state.sum += E;
						}
						return true;
					}
			};

		} // namespace area

	} // namespace strategy


	#ifndef DOXYGEN_NO_STRATEGY_SPECIALIZATIONS
	template <typename LL>
	struct strategy_area<geographic_tag, LL>
	{
		typedef strategy::area::by_spherical_excess<LL> type;
	};
	#endif

} // namespace geometry


#endif // _GEOMETRY_STRATEGY_SPHERICAL_AREA_HPP
