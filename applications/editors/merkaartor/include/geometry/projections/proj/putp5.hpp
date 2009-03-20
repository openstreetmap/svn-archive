#ifndef _PROJECTIONS_PUTP5_HPP
#define _PROJECTIONS_PUTP5_HPP

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
		namespace putp5
		{
			static const double C = 1.01346;
			static const double D = 1.2158542;

			struct par_putp5
			{
				double A, B;
			};

			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_putp5_spheroid : public base_t_fi<base_putp5_spheroid<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_fi<base_putp5_spheroid<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_fi<base_putp5_spheroid<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;

				par_putp5 m_proj_parm;

				inline base_putp5_spheroid(const PAR& par)
					: base_t_fi<base_putp5_spheroid<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{
					xy_x = C * lp_lon * (this->m_proj_parm.A - this->m_proj_parm.B * sqrt(1. + D * lp_lat * lp_lat));
					xy_y = C * lp_lat;
				}

				inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
				{
					lp_lat = xy_y / C;
					lp_lon = xy_x / (C * (this->m_proj_parm.A - this->m_proj_parm.B * sqrt(1. + D * lp_lat * lp_lat)));
				}
			};

			template <typename PAR>
			void setup(PAR& par, par_putp5& /*proj_parm*/)
			{
				par.es = 0.;
				// par.inv = s_inverse;
				// par.fwd = s_forward;
			}


			// Putnins P5
			template <typename PAR>
			void setup_putp5(PAR& par, par_putp5& proj_parm)
			{
				proj_parm.A = 2.;
				proj_parm.B = 1.;
				setup(par, proj_parm);
			}

			// Putnins P5'
			template <typename PAR>
			void setup_putp5p(PAR& par, par_putp5& proj_parm)
			{
				proj_parm.A = 1.5;
				proj_parm.B = 0.5;
				setup(par, proj_parm);
			}

		} // namespace putp5
	} //namespaces impl
	#endif // doxygen

	/*!
		\brief Putnins P5 projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Pseudocylindrical
		 - Spheroid
		\par Example
		\image html ex_putp5.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct putp5_spheroid : public impl::putp5::base_putp5_spheroid<LL, XY, PAR>
	{
		inline putp5_spheroid(const PAR& par) : impl::putp5::base_putp5_spheroid<LL, XY, PAR>(par)
		{
			impl::putp5::setup_putp5(this->m_par, this->m_proj_parm);
		}
	};

	/*!
		\brief Putnins P5' projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Pseudocylindrical
		 - Spheroid
		\par Example
		\image html ex_putp5p.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct putp5p_spheroid : public impl::putp5::base_putp5_spheroid<LL, XY, PAR>
	{
		inline putp5p_spheroid(const PAR& par) : impl::putp5::base_putp5_spheroid<LL, XY, PAR>(par)
		{
			impl::putp5::setup_putp5p(this->m_par, this->m_proj_parm);
		}
	};

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{

		// Factory entry(s)
		template <typename LL, typename XY, typename PAR>
		class putp5_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<putp5_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		class putp5p_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<putp5p_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		inline void putp5_init(impl::base_factory<LL, XY, PAR>& factory)
		{
			factory.add_to_factory("putp5", new putp5_entry<LL, XY, PAR>);
			factory.add_to_factory("putp5p", new putp5p_entry<LL, XY, PAR>);
		}

	} // namespace impl
	#endif // doxygen

}

#endif // _PROJECTIONS_PUTP5_HPP

