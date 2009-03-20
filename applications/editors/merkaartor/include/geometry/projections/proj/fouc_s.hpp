#ifndef _PROJECTIONS_FOUC_S_HPP
#define _PROJECTIONS_FOUC_S_HPP

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
		namespace fouc_s
		{
			static const int MAX_ITER = 10;
			static const double LOOP_TOL = 1e-7;

			struct par_fouc_s
			{
				double n, n1;
			};

			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_fouc_s_spheroid : public base_t_fi<base_fouc_s_spheroid<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_fi<base_fouc_s_spheroid<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_fi<base_fouc_s_spheroid<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;

				par_fouc_s m_proj_parm;

				inline base_fouc_s_spheroid(const PAR& par)
					: base_t_fi<base_fouc_s_spheroid<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{
					double t;

					t = cos(lp_lat);
					xy_x = lp_lon * t / (this->m_proj_parm.n + this->m_proj_parm.n1 * t);
					xy_y = this->m_proj_parm.n * lp_lat + this->m_proj_parm.n1 * sin(lp_lat);
				}

				inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
				{
					double V;
					int i;

					if (this->m_proj_parm.n) {
						lp_lat = xy_y;
						for (i = MAX_ITER; i ; --i) {
							lp_lat -= V = (this->m_proj_parm.n * lp_lat + this->m_proj_parm.n1 * sin(lp_lat) - xy_y ) /
								(this->m_proj_parm.n + this->m_proj_parm.n1 * cos(lp_lat));
							if (fabs(V) < LOOP_TOL)
								break;
						}
						if (!i)
							lp_lat = xy_y < 0. ? -HALFPI : HALFPI;
					} else
						lp_lat = aasin(xy_y);
					V = cos(lp_lat);
					lp_lon = xy_x * (this->m_proj_parm.n + this->m_proj_parm.n1 * V) / V;
				}
			};

			// Foucaut Sinusoidal
			template <typename PAR>
			void setup_fouc_s(PAR& par, par_fouc_s& proj_parm)
			{
				proj_parm.n = pj_param(par.params, "dn").f;
				if (proj_parm.n < 0. || proj_parm.n > 1.)
					throw proj_exception(-99);
				proj_parm.n1 = 1. - proj_parm.n;
				par.es = 0;
				// par.inv = s_inverse;
				// par.fwd = s_forward;
			}

		} // namespace fouc_s
	} //namespaces impl
	#endif // doxygen

	/*!
		\brief Foucaut Sinusoidal projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Pseudocylindrical
		 - Spheroid
		\par Example
		\image html ex_fouc_s.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct fouc_s_spheroid : public impl::fouc_s::base_fouc_s_spheroid<LL, XY, PAR>
	{
		inline fouc_s_spheroid(const PAR& par) : impl::fouc_s::base_fouc_s_spheroid<LL, XY, PAR>(par)
		{
			impl::fouc_s::setup_fouc_s(this->m_par, this->m_proj_parm);
		}
	};

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{

		// Factory entry(s)
		template <typename LL, typename XY, typename PAR>
		class fouc_s_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<fouc_s_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		inline void fouc_s_init(impl::base_factory<LL, XY, PAR>& factory)
		{
			factory.add_to_factory("fouc_s", new fouc_s_entry<LL, XY, PAR>);
		}

	} // namespace impl
	#endif // doxygen

}

#endif // _PROJECTIONS_FOUC_S_HPP

