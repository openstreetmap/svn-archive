#ifndef GGL_PROJECTIONS_ECK4_HPP
#define GGL_PROJECTIONS_ECK4_HPP

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
    namespace impl { namespace eck4{
            static const double C_x = .42223820031577120149;
            static const double C_y = 1.32650042817700232218;
            static const double RC_y = .75386330736002178205;
            static const double C_p = 3.57079632679489661922;
            static const double RC_p = .28004957675577868795;
            static const double EPS = 1e-7;
            static const int NITER = 6;


            // template class, using CRTP to implement forward/inverse
            template <typename LatLong, typename Cartesian, typename Parameters>
            struct base_eck4_spheroid : public base_t_fi<base_eck4_spheroid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>
            {

                typedef typename base_t_fi<base_eck4_spheroid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>::LL_T LL_T;
                typedef typename base_t_fi<base_eck4_spheroid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>::XY_T XY_T;


                inline base_eck4_spheroid(const Parameters& par)
                    : base_t_fi<base_eck4_spheroid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>(*this, par) {}

                inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
                {
                    double p, V, s, c;
                    int i;

                    p = C_p * sin(lp_lat);
                    V = lp_lat * lp_lat;
                    lp_lat *= 0.895168 + V * ( 0.0218849 + V * 0.00826809 );
                    for (i = NITER; i ; --i) {
                        c = cos(lp_lat);
                        s = sin(lp_lat);
                        lp_lat -= V = (lp_lat + s * (c + 2.) - p) /
                            (1. + c * (c + 2.) - s * s);
                        if (fabs(V) < EPS)
                            break;
                    }
                    if (!i) {
                        xy_x = C_x * lp_lon;
                        xy_y = lp_lat < 0. ? -C_y : C_y;
                    } else {
                        xy_x = C_x * lp_lon * (1. + cos(lp_lat));
                        xy_y = C_y * sin(lp_lat);
                    }
                }

                inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
                {
                    double c;

                    lp_lat = aasin(xy_y / C_y);
                    lp_lon = xy_x / (C_x * (1. + (c = cos(lp_lat))));
                    lp_lat = aasin((lp_lat + sin(lp_lat) * (c + 2.)) / C_p);
                }
            };

            // Eckert IV
            template <typename Parameters>
            void setup_eck4(Parameters& par)
            {
                par.es = 0.;
                // par.inv = s_inverse;
                // par.fwd = s_forward;
            }

        }} // namespace impl::eck4
    #endif // doxygen

    /*!
        \brief Eckert IV projection
        \ingroup projections
        \tparam LatLong latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Pseudocylindrical
         - Spheroid
        \par Example
        \image html ex_eck4.gif
    */
    template <typename LatLong, typename Cartesian, typename Parameters = parameters>
    struct eck4_spheroid : public impl::eck4::base_eck4_spheroid<LatLong, Cartesian, Parameters>
    {
        inline eck4_spheroid(const Parameters& par) : impl::eck4::base_eck4_spheroid<LatLong, Cartesian, Parameters>(par)
        {
            impl::eck4::setup_eck4(this->m_par);
        }
    };

    #ifndef DOXYGEN_NO_IMPL
    namespace impl
    {

        // Factory entry(s)
        template <typename LatLong, typename Cartesian, typename Parameters>
        class eck4_entry : public impl::factory_entry<LatLong, Cartesian, Parameters>
        {
            public :
                virtual projection<LatLong, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_fi<eck4_spheroid<LatLong, Cartesian, Parameters>, LatLong, Cartesian, Parameters>(par);
                }
        };

        template <typename LatLong, typename Cartesian, typename Parameters>
        inline void eck4_init(impl::base_factory<LatLong, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("eck4", new eck4_entry<LatLong, Cartesian, Parameters>);
        }

    } // namespace impl
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_ECK4_HPP

