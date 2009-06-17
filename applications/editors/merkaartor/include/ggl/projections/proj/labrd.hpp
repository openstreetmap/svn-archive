#ifndef GGL_PROJECTIONS_LABRD_HPP
#define GGL_PROJECTIONS_LABRD_HPP

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

#include <ggl/projections/impl/base_static.hpp>
#include <ggl/projections/impl/base_dynamic.hpp>
#include <ggl/projections/impl/projects.hpp>
#include <ggl/projections/impl/factory_entry.hpp>

namespace ggl { namespace projection
{
    #ifndef DOXYGEN_NO_IMPL
    namespace impl { namespace labrd{
            static const double EPS = 1.e-10;

            struct par_labrd
            {
                double Az, kRg, p0s, A, C, Ca, Cb, Cc, Cd;
                int  rot;
            };

            // template class, using CRTP to implement forward/inverse
            template <typename LatLong, typename Cartesian, typename Parameters>
            struct base_labrd_ellipsoid : public base_t_fi<base_labrd_ellipsoid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>
            {

                typedef typename base_t_fi<base_labrd_ellipsoid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>::LL_T LL_T;
                typedef typename base_t_fi<base_labrd_ellipsoid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>::XY_T XY_T;

                par_labrd m_proj_parm;

                inline base_labrd_ellipsoid(const Parameters& par)
                    : base_t_fi<base_labrd_ellipsoid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>(*this, par) {}

                inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
                {
                    double V1, V2, ps, sinps, cosps, sinps2, cosps2, I1, I2, I3, I4, I5, I6,
                        x2, y2, t;

                    V1 = this->m_proj_parm.A * log( tan(FORTPI + .5 * lp_lat) );
                    t = this->m_par.e * sin(lp_lat);
                    V2 = .5 * this->m_par.e * this->m_proj_parm.A * log ((1. + t)/(1. - t));
                    ps = 2. * (atan(exp(V1 - V2 + this->m_proj_parm.C)) - FORTPI);
                    I1 = ps - this->m_proj_parm.p0s;
                    cosps = cos(ps);    cosps2 = cosps * cosps;
                    sinps = sin(ps);    sinps2 = sinps * sinps;
                    I4 = this->m_proj_parm.A * cosps;
                    I2 = .5 * this->m_proj_parm.A * I4 * sinps;
                    I3 = I2 * this->m_proj_parm.A * this->m_proj_parm.A * (5. * cosps2 - sinps2) / 12.;
                    I6 = I4 * this->m_proj_parm.A * this->m_proj_parm.A;
                    I5 = I6 * (cosps2 - sinps2) / 6.;
                    I6 *= this->m_proj_parm.A * this->m_proj_parm.A *
                        (5. * cosps2 * cosps2 + sinps2 * (sinps2 - 18. * cosps2)) / 120.;
                    t = lp_lon * lp_lon;
                    xy_x = this->m_proj_parm.kRg * lp_lon * (I4 + t * (I5 + t * I6));
                    xy_y = this->m_proj_parm.kRg * (I1 + t * (I2 + t * I3));
                    x2 = xy_x * xy_x;
                    y2 = xy_y * xy_y;
                    V1 = 3. * xy_x * y2 - xy_x * x2;
                    V2 = xy_y * y2 - 3. * x2 * xy_y;
                    xy_x += this->m_proj_parm.Ca * V1 + this->m_proj_parm.Cb * V2;
                    xy_y += this->m_proj_parm.Ca * V2 - this->m_proj_parm.Cb * V1;
                }

                inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
                {
                    double x2, y2, V1, V2, V3, V4, t, t2, ps, pe, tpe, s,
                        I7, I8, I9, I10, I11, d, Re;
                    int i;

                    x2 = xy_x * xy_x;
                    y2 = xy_y * xy_y;
                    V1 = 3. * xy_x * y2 - xy_x * x2;
                    V2 = xy_y * y2 - 3. * x2 * xy_y;
                    V3 = xy_x * (5. * y2 * y2 + x2 * (-10. * y2 + x2 ));
                    V4 = xy_y * (5. * x2 * x2 + y2 * (-10. * x2 + y2 ));
                    xy_x += - this->m_proj_parm.Ca * V1 - this->m_proj_parm.Cb * V2 + this->m_proj_parm.Cc * V3 + this->m_proj_parm.Cd * V4;
                    xy_y +=   this->m_proj_parm.Cb * V1 - this->m_proj_parm.Ca * V2 - this->m_proj_parm.Cd * V3 + this->m_proj_parm.Cc * V4;
                    ps = this->m_proj_parm.p0s + xy_y / this->m_proj_parm.kRg;
                    pe = ps + this->m_par.phi0 - this->m_proj_parm.p0s;
                    for ( i = 20; i; --i) {
                        V1 = this->m_proj_parm.A * log(tan(FORTPI + .5 * pe));
                        tpe = this->m_par.e * sin(pe);
                        V2 = .5 * this->m_par.e * this->m_proj_parm.A * log((1. + tpe)/(1. - tpe));
                        t = ps - 2. * (atan(exp(V1 - V2 + this->m_proj_parm.C)) - FORTPI);
                        pe += t;
                        if (fabs(t) < EPS)
                            break;
                    }
                /*
                    if (!i) {
                    } else {
                    }
                */
                    t = this->m_par.e * sin(pe);
                    t = 1. - t * t;
                    Re = this->m_par.one_es / ( t * sqrt(t) );
                    t = tan(ps);
                    t2 = t * t;
                    s = this->m_proj_parm.kRg * this->m_proj_parm.kRg;
                    d = Re * this->m_par.k0 * this->m_proj_parm.kRg;
                    I7 = t / (2. * d);
                    I8 = t * (5. + 3. * t2) / (24. * d * s);
                    d = cos(ps) * this->m_proj_parm.kRg * this->m_proj_parm.A;
                    I9 = 1. / d;
                    d *= s;
                    I10 = (1. + 2. * t2) / (6. * d);
                    I11 = (5. + t2 * (28. + 24. * t2)) / (120. * d * s);
                    x2 = xy_x * xy_x;
                    lp_lat = pe + x2 * (-I7 + I8 * x2);
                    lp_lon = xy_x * (I9 + x2 * (-I10 + x2 * I11));
                }
            };

            // Laborde
            template <typename Parameters>
            void setup_labrd(Parameters& par, par_labrd& proj_parm)
            {
                double Az, sinp, R, N, t;
                proj_parm.rot    = pj_param(par.params, "bno_rot").i == 0;
                Az = pj_param(par.params, "razi").f;
                sinp = sin(par.phi0);
                t = 1. - par.es * sinp * sinp;
                N = 1. / sqrt(t);
                R = par.one_es * N / t;
                proj_parm.kRg = par.k0 * sqrt( N * R );
                proj_parm.p0s = atan( sqrt(R / N) * tan(par.phi0) );
                proj_parm.A = sinp / sin(proj_parm.p0s);
                t = par.e * sinp;
                proj_parm.C = .5 * par.e * proj_parm.A * log((1. + t)/(1. - t)) +
                    - proj_parm.A * log( tan(FORTPI + .5 * par.phi0))
                    + log( tan(FORTPI + .5 * proj_parm.p0s));
                t = Az + Az;
                proj_parm.Ca = (1. - cos(t)) * ( proj_parm.Cb = 1. / (12. * proj_parm.kRg * proj_parm.kRg) );
                proj_parm.Cb *= sin(t);
                proj_parm.Cc = 3. * (proj_parm.Ca * proj_parm.Ca - proj_parm.Cb * proj_parm.Cb);
                proj_parm.Cd = 6. * proj_parm.Ca * proj_parm.Cb;
                // par.inv = e_inverse;
                // par.fwd = e_forward;
            }

        }} // namespace impl::labrd
    #endif // doxygen

    /*!
        \brief Laborde projection
        \ingroup projections
        \tparam LatLong latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Cylindrical
         - Spheroid
         - Special for Madagascar
        \par Example
        \image html ex_labrd.gif
    */
    template <typename LatLong, typename Cartesian, typename Parameters = parameters>
    struct labrd_ellipsoid : public impl::labrd::base_labrd_ellipsoid<LatLong, Cartesian, Parameters>
    {
        inline labrd_ellipsoid(const Parameters& par) : impl::labrd::base_labrd_ellipsoid<LatLong, Cartesian, Parameters>(par)
        {
            impl::labrd::setup_labrd(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_IMPL
    namespace impl
    {

        // Factory entry(s)
        template <typename LatLong, typename Cartesian, typename Parameters>
        class labrd_entry : public impl::factory_entry<LatLong, Cartesian, Parameters>
        {
            public :
                virtual projection<LatLong, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_fi<labrd_ellipsoid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>(par);
                }
        };

        template <typename LatLong, typename Cartesian, typename Parameters>
        inline void labrd_init(impl::base_factory<LatLong, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("labrd", new labrd_entry<LatLong, Cartesian, Parameters>);
        }

    } // namespace impl
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_LABRD_HPP

