#ifndef GGL_PROJECTIONS_SOMERC_HPP
#define GGL_PROJECTIONS_SOMERC_HPP

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
    namespace impl { namespace somerc{ 
            static const double EPS = 1.e-10;
            static const int NITER = 6;

            struct par_somerc
            {
                double K, c, hlf_e, kR, cosp0, sinp0;
            };

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_somerc_ellipsoid : public base_t_fi<base_somerc_ellipsoid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_somerc m_proj_parm;

                inline base_somerc_ellipsoid(const Parameters& par)
                    : base_t_fi<base_somerc_ellipsoid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                	double phip, lamp, phipp, lampp, sp, cp;
                
                	sp = this->m_par.e * sin(lp_lat);
                	phip = 2.* atan( exp( this->m_proj_parm.c * (
                		log(tan(FORTPI + 0.5 * lp_lat)) - this->m_proj_parm.hlf_e * log((1. + sp)/(1. - sp)))
                		+ this->m_proj_parm.K)) - HALFPI;
                	lamp = this->m_proj_parm.c * lp_lon;
                	cp = cos(phip);
                	phipp = aasin(this->m_proj_parm.cosp0 * sin(phip) - this->m_proj_parm.sinp0 * cp * cos(lamp));
                	lampp = aasin(cp * sin(lamp) / cos(phipp));
                	xy_x = this->m_proj_parm.kR * lampp;
                	xy_y = this->m_proj_parm.kR * log(tan(FORTPI + 0.5 * phipp));
                }

                inline void inv(cartesian_type& xy_x, cartesian_type& xy_y, geographic_type& lp_lon, geographic_type& lp_lat) const
                {
                	double phip, lamp, phipp, lampp, cp, esp, con, delp;
                	int i;
                
                	phipp = 2. * (atan(exp(xy_y / this->m_proj_parm.kR)) - FORTPI);
                	lampp = xy_x / this->m_proj_parm.kR;
                	cp = cos(phipp);
                	phip = aasin(this->m_proj_parm.cosp0 * sin(phipp) + this->m_proj_parm.sinp0 * cp * cos(lampp));
                	lamp = aasin(cp * sin(lampp) / cos(phip));
                	con = (this->m_proj_parm.K - log(tan(FORTPI + 0.5 * phip)))/this->m_proj_parm.c;
                	for (i = NITER; i ; --i) {
                		esp = this->m_par.e * sin(phip);
                		delp = (con + log(tan(FORTPI + 0.5 * phip)) - this->m_proj_parm.hlf_e *
                			log((1. + esp)/(1. - esp)) ) *
                			(1. - esp * esp) * cos(phip) * this->m_par.rone_es;
                		phip -= delp;
                		if (fabs(delp) < EPS)
                			break;
                	}
                	if (i) {
                		lp_lat = phip;
                		lp_lon = lamp / this->m_proj_parm.c;
                	} else
                		throw proj_exception();
                }
            };

            // Swiss. Obl. Mercator
            template <typename Parameters>
            void setup_somerc(Parameters& par, par_somerc& proj_parm)
            {
            	double cp, phip0, sp;
            	proj_parm.hlf_e = 0.5 * par.e;
            	cp = cos(par.phi0);
            	cp *= cp;
            	proj_parm.c = sqrt(1 + par.es * cp * cp * par.rone_es);
            	sp = sin(par.phi0);
            	proj_parm.cosp0 = cos( phip0 = aasin(proj_parm.sinp0 = sp / proj_parm.c) );
            	sp *= par.e;
            	proj_parm.K = log(tan(FORTPI + 0.5 * phip0)) - proj_parm.c * (
            		log(tan(FORTPI + 0.5 * par.phi0)) - proj_parm.hlf_e *
            		log((1. + sp) / (1. - sp)));
            	proj_parm.kR = par.k0 * sqrt(par.one_es) / (1. - sp * sp);
                // par.inv = e_inverse;
                // par.fwd = e_forward;
            }

        }} // namespace impl::somerc
    #endif // doxygen 

    /*!
        \brief Swiss. Obl. Mercator projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Cylindrical
         - Ellipsoid
         - For CH1903
        \par Example
        \image html ex_somerc.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct somerc_ellipsoid : public impl::somerc::base_somerc_ellipsoid<Geographic, Cartesian, Parameters>
    {
        inline somerc_ellipsoid(const Parameters& par) : impl::somerc::base_somerc_ellipsoid<Geographic, Cartesian, Parameters>(par)
        {
            impl::somerc::setup_somerc(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_IMPL
    namespace impl
    {

        // Factory entry(s)
        template <typename Geographic, typename Cartesian, typename Parameters>
        class somerc_entry : public impl::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_fi<somerc_ellipsoid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        inline void somerc_init(impl::base_factory<Geographic, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("somerc", new somerc_entry<Geographic, Cartesian, Parameters>);
        }

    } // namespace impl 
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_SOMERC_HPP

