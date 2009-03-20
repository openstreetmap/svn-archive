#ifndef _PROJECTIONS_BOGGS_HPP
#define _PROJECTIONS_BOGGS_HPP

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
		namespace boggs
		{
			static const int NITER = 20;
			static const double EPS = 1e-7;
			static const double ONETOL = 1.000001;
			static const double FXC = 2.00276;
			static const double FXC2 = 1.11072;
			static const double FYC = 0.49931;
			static const double FYC2 = 1.41421356237309504880;


			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_boggs_spheroid : public base_t_f<base_boggs_spheroid<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_f<base_boggs_spheroid<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_f<base_boggs_spheroid<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;


				inline base_boggs_spheroid(const PAR& par)
					: base_t_f<base_boggs_spheroid<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{
					double theta, th1, c;
					int i;

					theta = lp_lat;
					if (fabs(fabs(lp_lat) - HALFPI) < EPS)
						xy_x = 0.;
					else {
						c = sin(theta) * PI;
						for (i = NITER; i; --i) {
							theta -= th1 = (theta + sin(theta) - c) /
								(1. + cos(theta));
							if (fabs(th1) < EPS) break;
						}
						theta *= 0.5;
						xy_x = FXC * lp_lon / (1. / cos(lp_lat) + FXC2 / cos(theta));
					}
					xy_y = FYC * (lp_lat + FYC2 * sin(theta));
				}
			};

			// Boggs Eumorphic
			template <typename PAR>
			void setup_boggs(PAR& par)
			{
				par.es = 0.;
				// par.fwd = s_forward;
			}

		} // namespace boggs
	} //namespaces impl
	#endif // doxygen

	/*!
		\brief Boggs Eumorphic projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Pseudocylindrical
		 - no inverse
		 - Spheroid
		\par Example
		\image html ex_boggs.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct boggs_spheroid : public impl::boggs::base_boggs_spheroid<LL, XY, PAR>
	{
		inline boggs_spheroid(const PAR& par) : impl::boggs::base_boggs_spheroid<LL, XY, PAR>(par)
		{
			impl::boggs::setup_boggs(this->m_par);
		}
	};

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{

		// Factory entry(s)
		template <typename LL, typename XY, typename PAR>
		class boggs_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_f<boggs_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		inline void boggs_init(impl::base_factory<LL, XY, PAR>& factory)
		{
			factory.add_to_factory("boggs", new boggs_entry<LL, XY, PAR>);
		}

	} // namespace impl
	#endif // doxygen

}

#endif // _PROJECTIONS_BOGGS_HPP

