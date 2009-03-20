#ifndef _PROJECTIONS_PUTP2_HPP
#define _PROJECTIONS_PUTP2_HPP

// Generic Geometry Library - projections (based on PROJ4)
// This file is automatically generated. DO NOT EDIT.

// Copyright Barend Gehrels (1995-2009), Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande (2008-2009)
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

// This file is converted from PROJ4, http://trac.osgeo.org/proj
// PROJ4 is originally written by Gerald Evenden (then of the USGS)
// PROJ4 is maintained by Frank Warmerdam
// PROJ4 is converted to Geometry Library by Barend Gehrels (Geodan, Amsterdam)

// Original copyright notice:

// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

#include <geometry/projections/impl/base_static.hpp>
#include <geometry/projections/impl/base_dynamic.hpp>
#include <geometry/projections/impl/projects.hpp>
#include <geometry/projections/impl/factory_entry.hpp>

namespace projection
{
	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{
		namespace putp2
		{
			static const double C_x = 1.89490;
			static const double C_y = 1.71848;
			static const double C_p = 0.6141848493043784;
			static const double EPS = 1e-10;
			static const int NITER = 10;
			static const double PI_DIV_3 = 1.0471975511965977;


			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_putp2_spheroid : public base_t_fi<base_putp2_spheroid<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_fi<base_putp2_spheroid<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_fi<base_putp2_spheroid<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;


				inline base_putp2_spheroid(const PAR& par)
					: base_t_fi<base_putp2_spheroid<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{
					double p, c, s, V;
					int i;

					p = C_p * sin(lp_lat);
					s = lp_lat * lp_lat;
					lp_lat *= 0.615709 + s * ( 0.00909953 + s * 0.0046292 );
					for (i = NITER; i ; --i) {
						c = cos(lp_lat);
						s = sin(lp_lat);
						lp_lat -= V = (lp_lat + s * (c - 1.) - p) /
							(1. + c * (c - 1.) - s * s);
						if (fabs(V) < EPS)
							break;
					}
					if (!i)
						lp_lat = lp_lat < 0 ? - PI_DIV_3 : PI_DIV_3;
					xy_x = C_x * lp_lon * (cos(lp_lat) - 0.5);
					xy_y = C_y * sin(lp_lat);
				}

				inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
				{
					double c;

					lp_lat = aasin(xy_y / C_y);
					lp_lon = xy_x / (C_x * ((c = cos(lp_lat)) - 0.5));
					lp_lat = aasin((lp_lat + sin(lp_lat) * (c - 1.)) / C_p);
				}
			};

			// Putnins P2
			template <typename PAR>
			void setup_putp2(PAR& par)
			{
				par.es = 0.;
				// par.inv = s_inverse;
				// par.fwd = s_forward;
			}

		} // namespace putp2
	} //namespaces impl
	#endif // doxygen

	/*!
		\brief Putnins P2 projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Pseudocylindrical
		 - Spheroid
		\par Example
		\image html ex_putp2.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct putp2_spheroid : public impl::putp2::base_putp2_spheroid<LL, XY, PAR>
	{
		inline putp2_spheroid(const PAR& par) : impl::putp2::base_putp2_spheroid<LL, XY, PAR>(par)
		{
			impl::putp2::setup_putp2(this->m_par);
		}
	};

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{

		// Factory entry(s)
		template <typename LL, typename XY, typename PAR>
		class putp2_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<putp2_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		inline void putp2_init(impl::base_factory<LL, XY, PAR>& factory)
		{
			factory.add_to_factory("putp2", new putp2_entry<LL, XY, PAR>);
		}

	} // namespace impl
	#endif // doxygen

}

#endif // _PROJECTIONS_PUTP2_HPP

