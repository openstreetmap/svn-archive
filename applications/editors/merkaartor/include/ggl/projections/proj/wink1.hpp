#ifndef GGL_PROJECTIONS_WINK1_HPP
#define GGL_PROJECTIONS_WINK1_HPP

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

#include <boost/math/special_functions/hypot.hpp>

#include <ggl/projections/impl/base_static.hpp>
#include <ggl/projections/impl/base_dynamic.hpp>
#include <ggl/projections/impl/projects.hpp>
#include <ggl/projections/impl/factory_entry.hpp>

namespace ggl { namespace projection
{
    #ifndef DOXYGEN_NO_IMPL
    namespace impl { namespace wink1{ 

            struct par_wink1
            {
                double cosphi1;
            };

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_wink1_spheroid : public base_t_fi<base_wink1_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_wink1 m_proj_parm;

                inline base_wink1_spheroid(const Parameters& par)
                    : base_t_fi<base_wink1_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                	xy_x = .5 * lp_lon * (this->m_proj_parm.cosphi1 + cos(lp_lat));
                	xy_y = lp_lat;
                }

                inline void inv(cartesian_type& xy_x, cartesian_type& xy_y, geographic_type& lp_lon, geographic_type& lp_lat) const
                {
                	lp_lat = xy_y;
                	lp_lon = 2. * xy_x / (this->m_proj_parm.cosphi1 + cos(lp_lat));
                }
            };

            // Winkel I
            template <typename Parameters>
            void setup_wink1(Parameters& par, par_wink1& proj_parm)
            {
            	proj_parm.cosphi1 = cos(pj_param(par.params, "rlat_ts").f);
            	par.es = 0.;
                // par.inv = s_inverse;
                // par.fwd = s_forward;
            }

        }} // namespace impl::wink1
    #endif // doxygen 

    /*!
        \brief Winkel I projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Pseudocylindrical
         - Spheroid
         - lat_ts=
        \par Example
        \image html ex_wink1.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct wink1_spheroid : public impl::wink1::base_wink1_spheroid<Geographic, Cartesian, Parameters>
    {
        inline wink1_spheroid(const Parameters& par) : impl::wink1::base_wink1_spheroid<Geographic, Cartesian, Parameters>(par)
        {
            impl::wink1::setup_wink1(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_IMPL
    namespace impl
    {

        // Factory entry(s)
        template <typename Geographic, typename Cartesian, typename Parameters>
        class wink1_entry : public impl::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_fi<wink1_spheroid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        inline void wink1_init(impl::base_factory<Geographic, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("wink1", new wink1_entry<Geographic, Cartesian, Parameters>);
        }

    } // namespace impl 
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_WINK1_HPP

