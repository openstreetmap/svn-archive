#ifndef GGL_PROJECTIONS_TMERC_HPP
#define GGL_PROJECTIONS_TMERC_HPP

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
#include <ggl/projections/impl/function_overloads.hpp>
#include <ggl/projections/impl/pj_mlfn.hpp>

#include <ggl/projections/epsg_traits.hpp>

namespace ggl { namespace projection
{
    #ifndef DOXYGEN_NO_IMPL
    namespace impl { namespace tmerc{ 
            static const double EPS10 = 1.e-10;
            static const double FC1 = 1.;
            static const double FC2 = .5;
            static const double FC3 = .16666666666666666666;
            static const double FC4 = .08333333333333333333;
            static const double FC5 = .05;
            static const double FC6 = .03333333333333333333;
            static const double FC7 = .02380952380952380952;
            static const double FC8 = .01785714285714285714;

            struct par_tmerc
            {
                double esp;
                double ml0;
                double en[EN_SIZE];
            };

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_tmerc_ellipsoid : public base_t_fi<base_tmerc_ellipsoid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_tmerc m_proj_parm;

                inline base_tmerc_ellipsoid(const Parameters& par)
                    : base_t_fi<base_tmerc_ellipsoid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                	double al, als, n, cosphi, sinphi, t;
                
                        /*
                         * Fail if our longitude is more than 90 degrees from the 
                         * central meridian since the results are essentially garbage. 
                         * Is error -20 really an appropriate return value?
                         * 
                         *  http://trac.osgeo.org/proj/ticket/5
                         */
                        if( lp_lon < -HALFPI || lp_lon > HALFPI )
                        {
                            xy_x = HUGE_VAL;
                            xy_y = HUGE_VAL;
                            throw proj_exception(  -14);
                            return;
                        }
                
                	sinphi = sin(lp_lat); cosphi = cos(lp_lat);
                	t = fabs(cosphi) > 1e-10 ? sinphi/cosphi : 0.;
                	t *= t;
                	al = cosphi * lp_lon;
                	als = al * al;
                	al /= sqrt(1. - this->m_par.es * sinphi * sinphi);
                	n = this->m_proj_parm.esp * cosphi * cosphi;
                	xy_x = this->m_par.k0 * al * (FC1 +
                		FC3 * als * (1. - t + n +
                		FC5 * als * (5. + t * (t - 18.) + n * (14. - 58. * t)
                		+ FC7 * als * (61. + t * ( t * (179. - t) - 479. ) )
                		)));
                	xy_y = this->m_par.k0 * (pj_mlfn(lp_lat, sinphi, cosphi, this->m_proj_parm.en) - this->m_proj_parm.ml0 +
                		sinphi * al * lp_lon * FC2 * ( 1. +
                		FC4 * als * (5. - t + n * (9. + 4. * n) +
                		FC6 * als * (61. + t * (t - 58.) + n * (270. - 330 * t)
                		+ FC8 * als * (1385. + t * ( t * (543. - t) - 3111.) )
                		))));
                }

                inline void inv(cartesian_type& xy_x, cartesian_type& xy_y, geographic_type& lp_lon, geographic_type& lp_lat) const
                {
                	double n, con, cosphi, d, ds, sinphi, t;
                
                	lp_lat = pj_inv_mlfn(this->m_proj_parm.ml0 + xy_y / this->m_par.k0, this->m_par.es, this->m_proj_parm.en);
                	if (fabs(lp_lat) >= HALFPI) {
                		lp_lat = xy_y < 0. ? -HALFPI : HALFPI;
                		lp_lon = 0.;
                	} else {
                		sinphi = sin(lp_lat);
                		cosphi = cos(lp_lat);
                		t = fabs(cosphi) > 1e-10 ? sinphi/cosphi : 0.;
                		n = this->m_proj_parm.esp * cosphi * cosphi;
                		d = xy_x * sqrt(con = 1. - this->m_par.es * sinphi * sinphi) / this->m_par.k0;
                		con *= t;
                		t *= t;
                		ds = d * d;
                		lp_lat -= (con * ds / (1.-this->m_par.es)) * FC2 * (1. -
                			ds * FC4 * (5. + t * (3. - 9. *  n) + n * (1. - 4 * n) -
                			ds * FC6 * (61. + t * (90. - 252. * n +
                				45. * t) + 46. * n
                		   - ds * FC8 * (1385. + t * (3633. + t * (4095. + 1574. * t)) )
                			)));
                		lp_lon = d*(FC1 -
                			ds*FC3*( 1. + 2.*t + n -
                			ds*FC5*(5. + t*(28. + 24.*t + 8.*n) + 6.*n
                		   - ds * FC7 * (61. + t * (662. + t * (1320. + 720. * t)) )
                		))) / cosphi;
                	}
                }
            };

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_tmerc_spheroid : public base_t_fi<base_tmerc_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_tmerc m_proj_parm;

                inline base_tmerc_spheroid(const Parameters& par)
                    : base_t_fi<base_tmerc_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                	double b, cosphi;
                
                        /*
                         * Fail if our longitude is more than 90 degrees from the 
                         * central meridian since the results are essentially garbage. 
                         * Is error -20 really an appropriate return value?
                         * 
                         *  http://trac.osgeo.org/proj/ticket/5
                         */
                        if( lp_lon < -HALFPI || lp_lon > HALFPI )
                        {
                            xy_x = HUGE_VAL;
                            xy_y = HUGE_VAL;
                            throw proj_exception(  -14);
                            return;
                        }
                
                	b = (cosphi = cos(lp_lat)) * sin(lp_lon);
                	if (fabs(fabs(b) - 1.) <= EPS10) throw proj_exception();;
                	xy_x = this->m_proj_parm.ml0 * log((1. + b) / (1. - b));
                	if ((b = fabs( xy_y = cosphi * cos(lp_lon) / sqrt(1. - b * b) )) >= 1.) {
                		if ((b - 1.) > EPS10) throw proj_exception();
                		else xy_y = 0.;
                	} else
                		xy_y = acos(xy_y);
                	if (lp_lat < 0.) xy_y = -xy_y;
                	xy_y = this->m_proj_parm.esp * (xy_y - this->m_par.phi0);
                }

                inline void inv(cartesian_type& xy_x, cartesian_type& xy_y, geographic_type& lp_lon, geographic_type& lp_lat) const
                {
                	double h, g;
                
                	h = exp(xy_x / this->m_proj_parm.esp);
                	g = .5 * (h - 1. / h);
                	h = cos(this->m_par.phi0 + xy_y / this->m_proj_parm.esp);
                	lp_lat = asin(sqrt((1. - h * h) / (1. + g * g)));
                	if (xy_y < 0.) lp_lat = -lp_lat;
                	lp_lon = (g || h) ? atan2(g, h) : 0.;
                }
            };

            template <typename Parameters>
            void setup(Parameters& par, par_tmerc& proj_parm)  /* general initialization */
            {
            	if (par.es) {
                    pj_enfn(par.es, proj_parm.en);
            
            		proj_parm.ml0 = pj_mlfn(par.phi0, sin(par.phi0), cos(par.phi0), proj_parm.en);
            		proj_parm.esp = par.es / (1. - par.es);
                // par.inv = e_inverse;
                // par.fwd = e_forward;
            	} else {
            		proj_parm.esp = par.k0;
            		proj_parm.ml0 = .5 * proj_parm.esp;
                // par.inv = s_inverse;
                // par.fwd = s_forward;
            	}
            }


            // Transverse Mercator
            template <typename Parameters>
            void setup_tmerc(Parameters& par, par_tmerc& proj_parm)
            {
                setup(par, proj_parm);
            }

            // Universal Transverse Mercator (UTM)
            template <typename Parameters>
            void setup_utm(Parameters& par, par_tmerc& proj_parm)
            {
            	int zone;
            	if (!par.es) throw proj_exception(-34);
            	par.y0 = pj_param(par.params, "bsouth").i ? 10000000. : 0.;
            	par.x0 = 500000.;
            	if (pj_param(par.params, "tzone").i) /* zone input ? */
            		if ((zone = pj_param(par.params, "izone").i) > 0 && zone <= 60)
            			--zone;
            		else
            			throw proj_exception(-35);
            	else /* nearest central meridian input */
            		if ((zone = int_floor((adjlon(par.lam0) + PI) * 30. / PI)) < 0)
            			zone = 0;
            		else if (zone >= 60)
            			zone = 59;
            	par.lam0 = (zone + .5) * PI / 30. - PI;
            	par.k0 = 0.9996;
            	par.phi0 = 0.;
                setup(par, proj_parm);
            }

        }} // namespace impl::tmerc
    #endif // doxygen 

    /*!
        \brief Transverse Mercator projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Cylindrical
         - Spheroid
         - Ellipsoid
        \par Example
        \image html ex_tmerc.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct tmerc_ellipsoid : public impl::tmerc::base_tmerc_ellipsoid<Geographic, Cartesian, Parameters>
    {
        inline tmerc_ellipsoid(const Parameters& par) : impl::tmerc::base_tmerc_ellipsoid<Geographic, Cartesian, Parameters>(par)
        {
            impl::tmerc::setup_tmerc(this->m_par, this->m_proj_parm);
        }
    };

    /*!
        \brief Universal Transverse Mercator (UTM) projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Cylindrical
         - Spheroid
         - zone= south
        \par Example
        \image html ex_utm.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct utm_ellipsoid : public impl::tmerc::base_tmerc_ellipsoid<Geographic, Cartesian, Parameters>
    {
        inline utm_ellipsoid(const Parameters& par) : impl::tmerc::base_tmerc_ellipsoid<Geographic, Cartesian, Parameters>(par)
        {
            impl::tmerc::setup_utm(this->m_par, this->m_proj_parm);
        }
    };

    /*!
        \brief Transverse Mercator projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Cylindrical
         - Spheroid
         - Ellipsoid
        \par Example
        \image html ex_tmerc.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct tmerc_spheroid : public impl::tmerc::base_tmerc_spheroid<Geographic, Cartesian, Parameters>
    {
        inline tmerc_spheroid(const Parameters& par) : impl::tmerc::base_tmerc_spheroid<Geographic, Cartesian, Parameters>(par)
        {
            impl::tmerc::setup_tmerc(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_IMPL
    namespace impl
    {

        // Factory entry(s)
        template <typename Geographic, typename Cartesian, typename Parameters>
        class tmerc_entry : public impl::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    if (par.es)
                        return new base_v_fi<tmerc_ellipsoid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                    else
                        return new base_v_fi<tmerc_spheroid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        class utm_entry : public impl::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_fi<utm_ellipsoid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        inline void tmerc_init(impl::base_factory<Geographic, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("tmerc", new tmerc_entry<Geographic, Cartesian, Parameters>);
            factory.add_to_factory("utm", new utm_entry<Geographic, Cartesian, Parameters>);
        }

    } // namespace impl 
    // Create EPSG specializations
    // (Proof of Concept, only for some)

    template<typename LatLongRadian, typename Cartesian, typename Parameters>
    struct epsg_traits<2000, LatLongRadian, Cartesian, Parameters>
    {
        typedef tmerc_ellipsoid<LatLongRadian, Cartesian, Parameters> type;
        static inline std::string par()
        {
            return "+proj=tmerc +lat_0=0 +lon_0=-62 +k=0.9995000000000001 +x_0=400000 +y_0=0 +ellps=clrk80 +units=m";
        }
    };


    template<typename LatLongRadian, typename Cartesian, typename Parameters>
    struct epsg_traits<2001, LatLongRadian, Cartesian, Parameters>
    {
        typedef tmerc_ellipsoid<LatLongRadian, Cartesian, Parameters> type;
        static inline std::string par()
        {
            return "+proj=tmerc +lat_0=0 +lon_0=-62 +k=0.9995000000000001 +x_0=400000 +y_0=0 +ellps=clrk80 +units=m";
        }
    };


    template<typename LatLongRadian, typename Cartesian, typename Parameters>
    struct epsg_traits<2002, LatLongRadian, Cartesian, Parameters>
    {
        typedef tmerc_ellipsoid<LatLongRadian, Cartesian, Parameters> type;
        static inline std::string par()
        {
            return "+proj=tmerc +lat_0=0 +lon_0=-62 +k=0.9995000000000001 +x_0=400000 +y_0=0 +ellps=clrk80 +towgs84=725,685,536,0,0,0,0 +units=m";
        }
    };


    template<typename LatLongRadian, typename Cartesian, typename Parameters>
    struct epsg_traits<2003, LatLongRadian, Cartesian, Parameters>
    {
        typedef tmerc_ellipsoid<LatLongRadian, Cartesian, Parameters> type;
        static inline std::string par()
        {
            return "+proj=tmerc +lat_0=0 +lon_0=-62 +k=0.9995000000000001 +x_0=400000 +y_0=0 +ellps=clrk80 +towgs84=72,213.7,93,0,0,0,0 +units=m";
        }
    };


    template<typename LatLongRadian, typename Cartesian, typename Parameters>
    struct epsg_traits<2039, LatLongRadian, Cartesian, Parameters>
    {
        typedef tmerc_ellipsoid<LatLongRadian, Cartesian, Parameters> type;
        static inline std::string par()
        {
            return "+proj=tmerc +lat_0=31.73439361111111 +lon_0=35.20451694444445 +k=1.0000067 +x_0=219529.584 +y_0=626907.39 +ellps=GRS80 +towgs84=-48,55,52,0,0,0,0 +units=m";
        }
    };


    template<typename LatLongRadian, typename Cartesian, typename Parameters>
    struct epsg_traits<29118, LatLongRadian, Cartesian, Parameters>
    {
        typedef utm_ellipsoid<LatLongRadian, Cartesian, Parameters> type;
        static inline std::string par()
        {
            return "+proj=utm +zone=18 +ellps=GRS67 +units=m";
        }
    };


    template<typename LatLongRadian, typename Cartesian, typename Parameters>
    struct epsg_traits<29119, LatLongRadian, Cartesian, Parameters>
    {
        typedef utm_ellipsoid<LatLongRadian, Cartesian, Parameters> type;
        static inline std::string par()
        {
            return "+proj=utm +zone=19 +ellps=GRS67 +units=m";
        }
    };


    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_TMERC_HPP

