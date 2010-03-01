#ifndef GGL_PROJECTIONS_KROVAK_HPP
#define GGL_PROJECTIONS_KROVAK_HPP

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

#include <ggl/extensions/gis/projections/impl/base_static.hpp>
#include <ggl/extensions/gis/projections/impl/base_dynamic.hpp>
#include <ggl/extensions/gis/projections/impl/projects.hpp>
#include <ggl/extensions/gis/projections/impl/factory_entry.hpp>

namespace ggl { namespace projection
{
    #ifndef DOXYGEN_NO_DETAIL
    namespace detail { namespace krovak{ 

            struct par_krovak
            {
                double    C_x;
            };
            
            
            
            
            
            /**
               NOTES: According to EPSG the full Krovak projection method should have
                      the following parameters.  Within PROJ.4 the azimuth, and pseudo
                      standard parallel are hardcoded in the algorithm and can't be 
                      altered from outside.  The others all have defaults to match the
                      common usage with Krovak projection.
            
              lat_0 = latitude of centre of the projection
                     
              lon_0 = longitude of centre of the projection
              
              ** = azimuth (true) of the centre line passing through the centre of the projection
            
              ** = latitude of pseudo standard parallel
               
              k  = scale factor on the pseudo standard parallel
              
              x_0 = False Easting of the centre of the projection at the apex of the cone
              
              y_0 = False Northing of the centre of the projection at the apex of the cone
            
             **/
            
            
            

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_krovak_ellipsoid : public base_t_fi<base_krovak_ellipsoid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_krovak m_proj_parm;

                inline base_krovak_ellipsoid(const Parameters& par)
                    : base_t_fi<base_krovak_ellipsoid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                /* calculate xy from lat/lon */
                
                    
                    
                
                /* Constants, identical to inverse transform function */
                    double s45, s90, e2, e, alfa, uq, u0, g, k, k1, n0, ro0, ad, a, s0, n;
                    double gfi, u, fi0, deltav, s, d, eps, ro;
                
                
                    s45 = 0.785398163397448;    /* 45 DEG */
                    s90 = 2 * s45;
                    fi0 = this->m_par.phi0;    /* Latitude of projection centre 49 DEG 30' */
                
                   /* Ellipsoid is used as Parameter in for.c and inv.c, therefore a must 
                      be set to 1 here.
                      Ellipsoid Bessel 1841 a = 6377397.155m 1/f = 299.1528128,
                      e2=0.006674372230614;
                   */
                    a =  1; /* 6377397.155; */
                    /* e2 = this->m_par.es;*/       /* 0.006674372230614; */
                    e2 = 0.006674372230614;
                    e = sqrt(e2);
                
                    alfa = sqrt(1. + (e2 * pow(cos(fi0), 4)) / (1. - e2));
                
                    uq = 1.04216856380474;      /* DU(2, 59, 42, 42.69689) */
                    u0 = asin(sin(fi0) / alfa);
                    g = pow(   (1. + e * sin(fi0)) / (1. - e * sin(fi0)) , alfa * e / 2.  );
                
                    k = tan( u0 / 2. + s45) / pow  (tan(fi0 / 2. + s45) , alfa) * g;
                
                    k1 = this->m_par.k0;
                    n0 = a * sqrt(1. - e2) / (1. - e2 * pow(sin(fi0), 2));
                    s0 = 1.37008346281555;       /* Latitude of pseudo standard parallel 78 DEG 30'00" N */
                    n = sin(s0);
                    ro0 = k1 * n0 / tan(s0);
                    ad = s90 - uq;
                
                /* Transformation */
                
                    gfi =pow ( ((1. + e * sin(lp_lat)) /
                               (1. - e * sin(lp_lat))) , (alfa * e / 2.));
                
                    u= 2. * (atan(k * pow( tan(lp_lat / 2. + s45), alfa) / gfi)-s45);
                
                    deltav = - lp_lon * alfa;
                
                    s = asin(cos(ad) * sin(u) + sin(ad) * cos(u) * cos(deltav));
                    d = asin(cos(u) * sin(deltav) / cos(s));
                    eps = n * d;
                    ro = ro0 * pow(tan(s0 / 2. + s45) , n) / pow(tan(s / 2. + s45) , n)   ;
                
                   /* x and y are reverted! */
                    xy_y = ro * cos(eps) / a;
                    xy_x = ro * sin(eps) / a;
                
                        if( !pj_param(this->m_par.params, "tczech").i )
                      {
                        xy_y *= -1.0;
                        xy_x *= -1.0;
                      }
                
                            return;
                }
                
                
                

                inline void inv(cartesian_type& xy_x, cartesian_type& xy_y, geographic_type& lp_lon, geographic_type& lp_lat) const
                {
                    /* calculate lat/lon from xy */
                
                /* Constants, identisch wie in der Umkehrfunktion */
                    double s45, s90, fi0, e2, e, alfa, uq, u0, g, k, k1, n0, ro0, ad, a, s0, n;
                    double u, deltav, s, d, eps, ro, fi1, xy0;
                    int ok;
                
                    s45 = 0.785398163397448;    /* 45 DEG */
                    s90 = 2 * s45;
                    fi0 = this->m_par.phi0;    /* Latitude of projection centre 49 DEG 30' */
                
                
                   /* Ellipsoid is used as Parameter in for.c and inv.c, therefore a must 
                      be set to 1 here.
                      Ellipsoid Bessel 1841 a = 6377397.155m 1/f = 299.1528128,
                      e2=0.006674372230614;
                   */
                    a = 1; /* 6377397.155; */
                    /* e2 = this->m_par.es; */      /* 0.006674372230614; */
                    e2 = 0.006674372230614;
                    e = sqrt(e2);
                
                    alfa = sqrt(1. + (e2 * pow(cos(fi0), 4)) / (1. - e2));
                    uq = 1.04216856380474;      /* DU(2, 59, 42, 42.69689) */
                    u0 = asin(sin(fi0) / alfa);
                    g = pow(   (1. + e * sin(fi0)) / (1. - e * sin(fi0)) , alfa * e / 2.  );
                
                    k = tan( u0 / 2. + s45) / pow  (tan(fi0 / 2. + s45) , alfa) * g;
                
                    k1 = this->m_par.k0;
                    n0 = a * sqrt(1. - e2) / (1. - e2 * pow(sin(fi0), 2));
                    s0 = 1.37008346281555;       /* Latitude of pseudo standard parallel 78 DEG 30'00" N */
                    n = sin(s0);
                    ro0 = k1 * n0 / tan(s0);
                    ad = s90 - uq;
                
                
                /* Transformation */
                   /* revert y, x*/
                    xy0=xy_x;
                    xy_x=xy_y;
                    xy_y=xy0;
                
                        if( !pj_param(this->m_par.params, "tczech").i )
                      {
                        xy_x *= -1.0;
                        xy_y *= -1.0;
                      }
                
                    ro = sqrt(xy_x * xy_x + xy_y * xy_y);
                    eps = atan2(xy_y, xy_x);
                    d = eps / sin(s0);
                    s = 2. * (atan(  pow(ro0 / ro, 1. / n) * tan(s0 / 2. + s45)) - s45);
                
                    u = asin(cos(ad) * sin(s) - sin(ad) * cos(s) * cos(d));
                    deltav = asin(cos(s) * sin(d) / cos(u));
                
                    lp_lon = this->m_par.lam0 - deltav / alfa;
                
                /* ITERATION FOR lp_lat */
                   fi1 = u;
                
                   ok = 0;
                   do
                   {
                       lp_lat = 2. * ( atan( pow( k, -1. / alfa)  *
                                            pow( tan(u / 2. + s45) , 1. / alfa)  *
                                            pow( (1. + e * sin(fi1)) / (1. - e * sin(fi1)) , e / 2.)
                                           )  - s45);
                
                      if (fabs(fi1 - lp_lat) < 0.000000000000001) ok=1;
                      fi1 = lp_lat;
                
                   }
                   while (ok==0);
                
                   lp_lon -= this->m_par.lam0;
                
                            return;
                }
                
            };

            // Krovak
            template <typename Parameters>
            void setup_krovak(Parameters& par, par_krovak& proj_parm)
            {
                double ts;
                /* read some Parameters,
                 * here Latitude Truescale */
                ts = pj_param(par.params, "rlat_ts").f;
                proj_parm.C_x = ts;
                
                /* we want Bessel as fixed ellipsoid */
                par.a = 6377397.155;
                par.e = sqrt(par.es = 0.006674372230614);
                    /* if latitude of projection center is not set, use 49d30'N */
                if (!pj_param(par.params, "tlat_0").i)
                        par.phi0 = 0.863937979737193;
             
                    /* if center long is not set use 42d30'E of Ferro - 17d40' for Ferro */
                    /* that will correspond to using longitudes relative to greenwich    */
                    /* as input and output, instead of lat/long relative to Ferro */
                if (!pj_param(par.params, "tlon_0").i)
                        par.lam0 = 0.7417649320975901 - 0.308341501185665;
                    /* if scale not set default to 0.9999 */
                if (!pj_param(par.params, "tk").i)
                        par.k0 = 0.9999;
                /* always the same */
                // par.inv = e_inverse;
             
                // par.fwd = e_forward;
            }

        }} // namespace detail::krovak
    #endif // doxygen 

    /*!
        \brief Krovak projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Pseudocylindrical
         - Ellps
        \par Example
        \image html ex_krovak.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct krovak_ellipsoid : public detail::krovak::base_krovak_ellipsoid<Geographic, Cartesian, Parameters>
    {
        inline krovak_ellipsoid(const Parameters& par) : detail::krovak::base_krovak_ellipsoid<Geographic, Cartesian, Parameters>(par)
        {
            detail::krovak::setup_krovak(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_DETAIL
    namespace detail
    {

        // Factory entry(s)
        template <typename Geographic, typename Cartesian, typename Parameters>
        class krovak_entry : public detail::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_fi<krovak_ellipsoid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        inline void krovak_init(detail::base_factory<Geographic, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("krovak", new krovak_entry<Geographic, Cartesian, Parameters>);
        }

    } // namespace detail 
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_KROVAK_HPP

