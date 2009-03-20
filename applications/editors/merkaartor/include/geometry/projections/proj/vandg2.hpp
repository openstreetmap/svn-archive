#ifndef _PROJECTIONS_VANDG2_HPP
#define _PROJECTIONS_VANDG2_HPP

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
		namespace vandg2
		{
			static const double TOL = 1e-10;
			static const double TWORPI = 0.63661977236758134308;

			struct par_vandg2
			{
				int vdg3;
			};

			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_vandg2_spheroid : public base_t_f<base_vandg2_spheroid<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_f<base_vandg2_spheroid<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_f<base_vandg2_spheroid<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;

				par_vandg2 m_proj_parm;

				inline base_vandg2_spheroid(const PAR& par)
					: base_t_f<base_vandg2_spheroid<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{
					double x1, at, bt, ct;

					bt = fabs(TWORPI * lp_lat);
					if ((ct = 1. - bt * bt) < 0.)
						ct = 0.;
					else
						ct = sqrt(ct);
					if (fabs(lp_lon) < TOL) {
						xy_x = 0.;
						xy_y = PI * (lp_lat < 0. ? -bt : bt) / (1. + ct);
					} else {
						at = 0.5 * fabs(PI / lp_lon - lp_lon / PI);
						if (this->m_proj_parm.vdg3) {
							x1 = bt / (1. + ct);
							xy_x = PI * (sqrt(at * at + 1. - x1 * x1) - at);
							xy_y = PI * x1;
						} else {
							x1 = (ct * sqrt(1. + at * at) - at * ct * ct) /
								(1. + at * at * bt * bt);
							xy_x = PI * x1;
							xy_y = PI * sqrt(1. - x1 * (x1 + 2. * at) + TOL);
						}
						if ( lp_lon < 0.) xy_x = -xy_x;
						if ( lp_lat < 0.) xy_y = -xy_y;
					}
				}
			};

			// van der Grinten II
			template <typename PAR>
			void setup_vandg2(PAR& /*par*/, par_vandg2& proj_parm)
			{
				proj_parm.vdg3 = 0;
				// par.inv = 0;
				// par.fwd = s_forward;
			}

			// van der Grinten III
			template <typename PAR>
			void setup_vandg3(PAR& par, par_vandg2& proj_parm)
			{
				proj_parm.vdg3 = 1;
				par.es = 0.;
				// par.fwd = s_forward;
			}

		} // namespace vandg2
	} //namespaces impl
	#endif // doxygen

	/*!
		\brief van der Grinten II projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Miscellaneous
		 - Spheroid
		 - no inverse
		\par Example
		\image html ex_vandg2.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct vandg2_spheroid : public impl::vandg2::base_vandg2_spheroid<LL, XY, PAR>
	{
		inline vandg2_spheroid(const PAR& par) : impl::vandg2::base_vandg2_spheroid<LL, XY, PAR>(par)
		{
			impl::vandg2::setup_vandg2(this->m_par, this->m_proj_parm);
		}
	};

	/*!
		\brief van der Grinten III projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Miscellaneous
		 - Spheroid
		 - no inverse
		\par Example
		\image html ex_vandg3.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct vandg3_spheroid : public impl::vandg2::base_vandg2_spheroid<LL, XY, PAR>
	{
		inline vandg3_spheroid(const PAR& par) : impl::vandg2::base_vandg2_spheroid<LL, XY, PAR>(par)
		{
			impl::vandg2::setup_vandg3(this->m_par, this->m_proj_parm);
		}
	};

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{

		// Factory entry(s)
		template <typename LL, typename XY, typename PAR>
		class vandg2_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_f<vandg2_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		class vandg3_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_f<vandg3_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		inline void vandg2_init(impl::base_factory<LL, XY, PAR>& factory)
		{
			factory.add_to_factory("vandg2", new vandg2_entry<LL, XY, PAR>);
			factory.add_to_factory("vandg3", new vandg3_entry<LL, XY, PAR>);
		}

	} // namespace impl
	#endif // doxygen

}

#endif // _PROJECTIONS_VANDG2_HPP

