#ifndef _PROJECTIONS_LATLONG_HPP
#define _PROJECTIONS_LATLONG_HPP

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

#include <geometry/projections/epsg_traits.hpp>

namespace projection
{
	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{
		namespace latlong
		{


			/* very loosely based upon DMA code by Bradford W. Drew */


			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_latlong_other : public base_t_fi<base_latlong_other<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_fi<base_latlong_other<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_fi<base_latlong_other<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;


				inline base_latlong_other(const PAR& par)
					: base_t_fi<base_latlong_other<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{

				        xy_x = lp_lon / this->m_par.a;
				        xy_y = lp_lat / this->m_par.a;
				}

				inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
				{

				        lp_lat = xy_y * this->m_par.a;
				        lp_lon = xy_x * this->m_par.a;
				}
			};

			// Lat/long (Geodetic)
			template <typename PAR>
			void setup_lonlat(PAR& par)
			{
			        par.is_latlong = 1;
			        par.x0 = 0.0;
			        par.y0 = 0.0;
				// par.inv = inverse;
				// par.fwd = forward;
			}

			// Lat/long (Geodetic alias)
			template <typename PAR>
			void setup_latlon(PAR& par)
			{
			        par.is_latlong = 1;
			        par.x0 = 0.0;
			        par.y0 = 0.0;
				// par.inv = inverse;
				// par.fwd = forward;
			}

			// Lat/long (Geodetic alias)
			template <typename PAR>
			void setup_latlong(PAR& par)
			{
			        par.is_latlong = 1;
			        par.x0 = 0.0;
			        par.y0 = 0.0;
				// par.inv = inverse;
				// par.fwd = forward;
			}

			// Lat/long (Geodetic alias)
			template <typename PAR>
			void setup_longlat(PAR& par)
			{
			        par.is_latlong = 1;
			        par.x0 = 0.0;
			        par.y0 = 0.0;
				// par.inv = inverse;
				// par.fwd = forward;
			}

		} // namespace latlong
	} //namespaces impl
	#endif // doxygen

	/*!
		\brief Lat/long (Geodetic) projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		\par Example
		\image html ex_lonlat.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct lonlat_other : public impl::latlong::base_latlong_other<LL, XY, PAR>
	{
		inline lonlat_other(const PAR& par) : impl::latlong::base_latlong_other<LL, XY, PAR>(par)
		{
			impl::latlong::setup_lonlat(this->m_par);
		}
	};

	/*!
		\brief Lat/long (Geodetic alias) projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		\par Example
		\image html ex_latlon.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct latlon_other : public impl::latlong::base_latlong_other<LL, XY, PAR>
	{
		inline latlon_other(const PAR& par) : impl::latlong::base_latlong_other<LL, XY, PAR>(par)
		{
			impl::latlong::setup_latlon(this->m_par);
		}
	};

	/*!
		\brief Lat/long (Geodetic alias) projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		\par Example
		\image html ex_latlong.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct latlong_other : public impl::latlong::base_latlong_other<LL, XY, PAR>
	{
		inline latlong_other(const PAR& par) : impl::latlong::base_latlong_other<LL, XY, PAR>(par)
		{
			impl::latlong::setup_latlong(this->m_par);
		}
	};

	/*!
		\brief Lat/long (Geodetic alias) projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		\par Example
		\image html ex_longlat.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct longlat_other : public impl::latlong::base_latlong_other<LL, XY, PAR>
	{
		inline longlat_other(const PAR& par) : impl::latlong::base_latlong_other<LL, XY, PAR>(par)
		{
			impl::latlong::setup_longlat(this->m_par);
		}
	};

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{

		// Factory entry(s)
		template <typename LL, typename XY, typename PAR>
		class lonlat_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<lonlat_other<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		class latlon_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<latlon_other<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		class latlong_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<latlong_other<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		class longlat_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<longlat_other<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		inline void latlong_init(impl::base_factory<LL, XY, PAR>& factory)
		{
			factory.add_to_factory("lonlat", new lonlat_entry<LL, XY, PAR>);
			factory.add_to_factory("latlon", new latlon_entry<LL, XY, PAR>);
			factory.add_to_factory("latlong", new latlong_entry<LL, XY, PAR>);
			factory.add_to_factory("longlat", new longlat_entry<LL, XY, PAR>);
		}

	} // namespace impl
	// Create EPSG specializations
	// (Proof of Concept, only for some)

	template<typename LLR, typename XY, typename PAR>
	struct epsg_traits<4326, LLR, XY, PAR>
	{
		typedef longlat_other<LLR, XY, PAR> type;
		static inline std::string par()
		{
			return "+proj=longlat +ellps=WGS84 +datum=WGS84";
		}
	};


	#endif // doxygen

}

#endif // _PROJECTIONS_LATLONG_HPP

