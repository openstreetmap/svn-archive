#ifndef GGL_PROJECTIONS_STERE_HPP
#define GGL_PROJECTIONS_STERE_HPP

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

#include <boost/concept_check.hpp>
#include <boost/math/special_functions/hypot.hpp>

#include <ggl/extensions/gis/projections/impl/base_static.hpp>
#include <ggl/extensions/gis/projections/impl/base_dynamic.hpp>
#include <ggl/extensions/gis/projections/impl/projects.hpp>
#include <ggl/extensions/gis/projections/impl/factory_entry.hpp>
#include <ggl/extensions/gis/projections/impl/pj_tsfn.hpp>

namespace ggl { namespace projection
{
    #ifndef DOXYGEN_NO_DETAIL
    namespace detail { namespace stere{ 
            static const double EPS10 = 1.e-10;
            static const double TOL = 1.e-8;
            static const int NITER = 8;
            static const double CONV = 1.e-10;
            static const int S_POLE = 0;
            static const int N_POLE = 1;
            static const int OBLIQ = 2;
            static const int EQUIT = 3;

            struct par_stere
            {
                double phits;
                double sinX1;
                double cosX1;
                double akm1;
                int    mode;
            };
                inline double
            ssfn_(double phit, double sinphi, double eccen) {
                sinphi *= eccen;
                return (tan (.5 * (HALFPI + phit)) *
                   pow((1. - sinphi) / (1. + sinphi), .5 * eccen));
            }

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_stere_ellipsoid : public base_t_fi<base_stere_ellipsoid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_stere m_proj_parm;

                inline base_stere_ellipsoid(const Parameters& par)
                    : base_t_fi<base_stere_ellipsoid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                    double coslam, sinlam, sinX=0.0, cosX=0.0, X, A, sinphi;
                
                    coslam = cos(lp_lon);
                    sinlam = sin(lp_lon);
                    sinphi = sin(lp_lat);
                    if (this->m_proj_parm.mode == OBLIQ || this->m_proj_parm.mode == EQUIT) {
                        sinX = sin(X = 2. * atan(ssfn_(lp_lat, sinphi, this->m_par.e)) - HALFPI);
                        cosX = cos(X);
                    }
                    switch (this->m_proj_parm.mode) {
                    case OBLIQ:
                        A = this->m_proj_parm.akm1 / (this->m_proj_parm.cosX1 * (1. + this->m_proj_parm.sinX1 * sinX +
                           this->m_proj_parm.cosX1 * cosX * coslam));
                        xy_y = A * (this->m_proj_parm.cosX1 * sinX - this->m_proj_parm.sinX1 * cosX * coslam);
                        goto xmul;
                    case EQUIT:
                        A = 2. * this->m_proj_parm.akm1 / (1. + cosX * coslam);
                        xy_y = A * sinX;
                xmul:
                        xy_x = A * cosX;
                        break;
                    case S_POLE:
                        lp_lat = -lp_lat;
                        coslam = - coslam;
                        sinphi = -sinphi;
                    case N_POLE:
                        xy_x = this->m_proj_parm.akm1 * pj_tsfn(lp_lat, sinphi, this->m_par.e);
                        xy_y = - xy_x * coslam;
                        break;
                    }
                    xy_x = xy_x * sinlam;
                }

                inline void inv(cartesian_type& xy_x, cartesian_type& xy_y, geographic_type& lp_lon, geographic_type& lp_lat) const
                {
                    double cosphi, sinphi, tp=0.0, phi_l=0.0, rho, halfe=0.0, halfpi=0.0;
                    int i;
                
                    rho = boost::math::hypot(xy_x, xy_y);
                    switch (this->m_proj_parm.mode) {
                    case OBLIQ:
                    case EQUIT:
                        cosphi = cos( tp = 2. * atan2(rho * this->m_proj_parm.cosX1 , this->m_proj_parm.akm1) );
                        sinphi = sin(tp);
                                if( rho == 0.0 )
                            phi_l = asin(cosphi * this->m_proj_parm.sinX1);
                                else
                            phi_l = asin(cosphi * this->m_proj_parm.sinX1 + (xy_y * sinphi * this->m_proj_parm.cosX1 / rho));
                
                        tp = tan(.5 * (HALFPI + phi_l));
                        xy_x *= sinphi;
                        xy_y = rho * this->m_proj_parm.cosX1 * cosphi - xy_y * this->m_proj_parm.sinX1* sinphi;
                        halfpi = HALFPI;
                        halfe = .5 * this->m_par.e;
                        break;
                    case N_POLE:
                        xy_y = -xy_y;
                    case S_POLE:
                        phi_l = HALFPI - 2. * atan(tp = - rho / this->m_proj_parm.akm1);
                        halfpi = -HALFPI;
                        halfe = -.5 * this->m_par.e;
                        break;
                    }
                    for (i = NITER; i--; phi_l = lp_lat) {
                        sinphi = this->m_par.e * sin(phi_l);
                        lp_lat = 2. * atan(tp * pow((1.+sinphi)/(1.-sinphi),
                           halfe)) - halfpi;
                        if (fabs(phi_l - lp_lat) < CONV) {
                            if (this->m_proj_parm.mode == S_POLE)
                                lp_lat = -lp_lat;
                            lp_lon = (xy_x == 0. && xy_y == 0.) ? 0. : atan2(xy_x, xy_y);
                            return;
                        }
                    }
                    throw proj_exception();;
                }
            };

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_stere_spheroid : public base_t_fi<base_stere_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_stere m_proj_parm;

                inline base_stere_spheroid(const Parameters& par)
                    : base_t_fi<base_stere_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                    double  sinphi, cosphi, coslam, sinlam;
                
                    sinphi = sin(lp_lat);
                    cosphi = cos(lp_lat);
                    coslam = cos(lp_lon);
                    sinlam = sin(lp_lon);
                    switch (this->m_proj_parm.mode) {
                    case EQUIT:
                        xy_y = 1. + cosphi * coslam;
                        goto oblcon;
                    case OBLIQ:
                        xy_y = 1. + this->m_proj_parm.sinX1 * sinphi + this->m_proj_parm.cosX1 * cosphi * coslam;
                oblcon:
                        if (xy_y <= EPS10) throw proj_exception();;
                        xy_x = (xy_y = this->m_proj_parm.akm1 / xy_y) * cosphi * sinlam;
                        xy_y *= (this->m_proj_parm.mode == EQUIT) ? sinphi :
                           this->m_proj_parm.cosX1 * sinphi - this->m_proj_parm.sinX1 * cosphi * coslam;
                        break;
                    case N_POLE:
                        coslam = - coslam;
                        lp_lat = - lp_lat;
                    case S_POLE:
                        if (fabs(lp_lat - HALFPI) < TOL) throw proj_exception();;
                        xy_x = sinlam * ( xy_y = this->m_proj_parm.akm1 * tan(FORTPI + .5 * lp_lat) );
                        xy_y *= coslam;
                        break;
                    }
                }

                inline void inv(cartesian_type& xy_x, cartesian_type& xy_y, geographic_type& lp_lon, geographic_type& lp_lat) const
                {
                    double  c, rh, sinc, cosc;
                
                    sinc = sin(c = 2. * atan((rh = boost::math::hypot(xy_x, xy_y)) / this->m_proj_parm.akm1));
                    cosc = cos(c);
                    lp_lon = 0.;
                    switch (this->m_proj_parm.mode) {
                    case EQUIT:
                        if (fabs(rh) <= EPS10)
                            lp_lat = 0.;
                        else
                            lp_lat = asin(xy_y * sinc / rh);
                        if (cosc != 0. || xy_x != 0.)
                            lp_lon = atan2(xy_x * sinc, cosc * rh);
                        break;
                    case OBLIQ:
                        if (fabs(rh) <= EPS10)
                            lp_lat = this->m_par.phi0;
                        else
                            lp_lat = asin(cosc * this->m_proj_parm.sinX1 + xy_y * sinc * this->m_proj_parm.cosX1 / rh);
                        if ((c = cosc - this->m_proj_parm.sinX1 * sin(lp_lat)) != 0. || xy_x != 0.)
                            lp_lon = atan2(xy_x * sinc * this->m_proj_parm.cosX1, c * rh);
                        break;
                    case N_POLE:
                        xy_y = -xy_y;
                    case S_POLE:
                        if (fabs(rh) <= EPS10)
                            lp_lat = this->m_par.phi0;
                        else
                            lp_lat = asin(this->m_proj_parm.mode == S_POLE ? - cosc : cosc);
                        lp_lon = (xy_x == 0. && xy_y == 0.) ? 0. : atan2(xy_x, xy_y);
                        break;
                    }
                }
            };

            template <typename Parameters>
            void setup(Parameters& par, par_stere& proj_parm)  /* general initialization */
            {
                boost::ignore_unused_variable_warning(par);
                boost::ignore_unused_variable_warning(proj_parm);
                double t;
                if (fabs((t = fabs(par.phi0)) - HALFPI) < EPS10)
                    proj_parm.mode = par.phi0 < 0. ? S_POLE : N_POLE;
                else
                    proj_parm.mode = t > EPS10 ? OBLIQ : EQUIT;
                proj_parm.phits = fabs(proj_parm.phits);
                if (par.es) {
                    double X;
                    switch (proj_parm.mode) {
                    case N_POLE:
                    case S_POLE:
                        if (fabs(proj_parm.phits - HALFPI) < EPS10)
                            proj_parm.akm1 = 2. * par.k0 /
                               sqrt(pow(1+par.e,1+par.e)*pow(1-par.e,1-par.e));
                        else {
                            proj_parm.akm1 = cos(proj_parm.phits) /
                               pj_tsfn(proj_parm.phits, t = sin(proj_parm.phits), par.e);
                            t *= par.e;
                            proj_parm.akm1 /= sqrt(1. - t * t);
                        }
                        break;
                    case EQUIT:
                        proj_parm.akm1 = 2. * par.k0;
                        break;
                    case OBLIQ:
                        t = sin(par.phi0);
                        X = 2. * atan(ssfn_(par.phi0, t, par.e)) - HALFPI;
                        t *= par.e;
                        proj_parm.akm1 = 2. * par.k0 * cos(par.phi0) / sqrt(1. - t * t);
                        proj_parm.sinX1 = sin(X);
                        proj_parm.cosX1 = cos(X);
                        break;
                    }
                // par.inv = e_inverse;
                // par.fwd = e_forward;
                } else {
                    switch (proj_parm.mode) {
                    case OBLIQ:
                        proj_parm.sinX1 = sin(par.phi0);
                        proj_parm.cosX1 = cos(par.phi0);
                    case EQUIT:
                        proj_parm.akm1 = 2. * par.k0;
                        break;
                    case S_POLE:
                    case N_POLE:
                        proj_parm.akm1 = fabs(proj_parm.phits - HALFPI) >= EPS10 ?
                           cos(proj_parm.phits) / tan(FORTPI - .5 * proj_parm.phits) :
                           2. * par.k0 ;
                        break;
                    }
                // par.inv = s_inverse;
                // par.fwd = s_forward;
                }
            }


            // Stereographic
            template <typename Parameters>
            void setup_stere(Parameters& par, par_stere& proj_parm)
            {
                proj_parm.phits = pj_param(par.params, "tlat_ts").i ?
                    proj_parm.phits = pj_param(par.params, "rlat_ts").f : HALFPI;
                setup(par, proj_parm);
            }

            // Universal Polar Stereographic
            template <typename Parameters>
            void setup_ups(Parameters& par, par_stere& proj_parm)
            {
                /* International Ellipsoid */
                par.phi0 = pj_param(par.params, "bsouth").i ? - HALFPI: HALFPI;
                if (!par.es) throw proj_exception(-34);
                par.k0 = .994;
                par.x0 = 2000000.;
                par.y0 = 2000000.;
                proj_parm.phits = HALFPI;
                par.lam0 = 0.;
                setup(par, proj_parm);
            }

        }} // namespace detail::stere
    #endif // doxygen 

    /*!
        \brief Stereographic projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Azimuthal
         - Spheroid
         - Ellipsoid
         - lat_ts=
        \par Example
        \image html ex_stere.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct stere_ellipsoid : public detail::stere::base_stere_ellipsoid<Geographic, Cartesian, Parameters>
    {
        inline stere_ellipsoid(const Parameters& par) : detail::stere::base_stere_ellipsoid<Geographic, Cartesian, Parameters>(par)
        {
            detail::stere::setup_stere(this->m_par, this->m_proj_parm);
        }
    };

    /*!
        \brief Universal Polar Stereographic projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Azimuthal
         - Spheroid
         - Ellipsoid
         - south
        \par Example
        \image html ex_ups.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct ups_ellipsoid : public detail::stere::base_stere_ellipsoid<Geographic, Cartesian, Parameters>
    {
        inline ups_ellipsoid(const Parameters& par) : detail::stere::base_stere_ellipsoid<Geographic, Cartesian, Parameters>(par)
        {
            detail::stere::setup_ups(this->m_par, this->m_proj_parm);
        }
    };

    /*!
        \brief Stereographic projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Azimuthal
         - Spheroid
         - Ellipsoid
         - lat_ts=
        \par Example
        \image html ex_stere.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct stere_spheroid : public detail::stere::base_stere_spheroid<Geographic, Cartesian, Parameters>
    {
        inline stere_spheroid(const Parameters& par) : detail::stere::base_stere_spheroid<Geographic, Cartesian, Parameters>(par)
        {
            detail::stere::setup_stere(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_DETAIL
    namespace detail
    {

        // Factory entry(s)
        template <typename Geographic, typename Cartesian, typename Parameters>
        class stere_entry : public detail::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    if (par.es)
                        return new base_v_fi<stere_ellipsoid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                    else
                        return new base_v_fi<stere_spheroid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        class ups_entry : public detail::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_fi<ups_ellipsoid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        inline void stere_init(detail::base_factory<Geographic, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("stere", new stere_entry<Geographic, Cartesian, Parameters>);
            factory.add_to_factory("ups", new ups_entry<Geographic, Cartesian, Parameters>);
        }

    } // namespace detail 
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_STERE_HPP

