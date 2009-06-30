#ifndef GGL_PROJECTIONS_AITOFF_HPP
#define GGL_PROJECTIONS_AITOFF_HPP

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
    #ifndef DOXYGEN_NO_DETAIL
    namespace detail { namespace aitoff{ 

            struct par_aitoff
            {
                double cosphi1;
                int  mode;
            };
            
            
            
            

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_aitoff_spheroid : public base_t_f<base_aitoff_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_aitoff m_proj_parm;

                inline base_aitoff_spheroid(const Parameters& par)
                    : base_t_f<base_aitoff_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                	double c, d;
                
                	if((d = acos(cos(lp_lat) * cos(c = 0.5 * lp_lon)))) {/* basic Aitoff */
                		xy_x = 2. * d * cos(lp_lat) * sin(c) * (xy_y = 1. / sin(d));
                		xy_y *= d * sin(lp_lat);
                	} else
                		xy_x = xy_y = 0.;
                	if (this->m_proj_parm.mode) { /* Winkel Tripel */
                		xy_x = (xy_x + lp_lon * this->m_proj_parm.cosphi1) * 0.5;
                		xy_y = (xy_y + lp_lat) * 0.5;
                	}
                }
            };

            template <typename Parameters>
            void setup(Parameters& par, par_aitoff& proj_parm) 
            {
                // par.inv = 0;
                // par.fwd = s_forward;
            	par.es = 0.;
            }


            // Aitoff
            template <typename Parameters>
            void setup_aitoff(Parameters& par, par_aitoff& proj_parm)
            {
            	proj_parm.mode = 0;
                setup(par, proj_parm);
            }

            // Winkel Tripel
            template <typename Parameters>
            void setup_wintri(Parameters& par, par_aitoff& proj_parm)
            {
            	proj_parm.mode = 1;
            	if (pj_param(par.params, "tlat_1").i)
                    {
            		if ((proj_parm.cosphi1 = cos(pj_param(par.params, "rlat_1").f)) == 0.)
            			throw proj_exception(-22);
                    }
            	else /* 50d28' or acos(2/pi) */
            		proj_parm.cosphi1 = 0.636619772367581343;
                setup(par, proj_parm);
            }

        }} // namespace detail::aitoff
    #endif // doxygen 

    /*!
        \brief Aitoff projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Miscellaneous
         - Spheroid
        \par Example
        \image html ex_aitoff.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct aitoff_spheroid : public detail::aitoff::base_aitoff_spheroid<Geographic, Cartesian, Parameters>
    {
        inline aitoff_spheroid(const Parameters& par) : detail::aitoff::base_aitoff_spheroid<Geographic, Cartesian, Parameters>(par)
        {
            detail::aitoff::setup_aitoff(this->m_par, this->m_proj_parm);
        }
    };

    /*!
        \brief Winkel Tripel projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Miscellaneous
         - Spheroid
         - lat_1
        \par Example
        \image html ex_wintri.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct wintri_spheroid : public detail::aitoff::base_aitoff_spheroid<Geographic, Cartesian, Parameters>
    {
        inline wintri_spheroid(const Parameters& par) : detail::aitoff::base_aitoff_spheroid<Geographic, Cartesian, Parameters>(par)
        {
            detail::aitoff::setup_wintri(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_DETAIL
    namespace detail
    {

        // Factory entry(s)
        template <typename Geographic, typename Cartesian, typename Parameters>
        class aitoff_entry : public detail::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_f<aitoff_spheroid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        class wintri_entry : public detail::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_f<wintri_spheroid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        inline void aitoff_init(detail::base_factory<Geographic, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("aitoff", new aitoff_entry<Geographic, Cartesian, Parameters>);
            factory.add_to_factory("wintri", new wintri_entry<Geographic, Cartesian, Parameters>);
        }

    } // namespace detail 
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_AITOFF_HPP

