#ifndef GGL_PROJECTIONS_CHAMB_HPP
#define GGL_PROJECTIONS_CHAMB_HPP

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
#include <ggl/projections/impl/aasincos.hpp>

namespace ggl { namespace projection
{
    #ifndef DOXYGEN_NO_IMPL
    namespace impl { namespace chamb{ 
            static const double THIRD = 0.333333333333333333;
            static const double TOL = 1e-9;

            struct VECT { double r, Az; };
            struct CXY { double x, y; }; // x/y for chamb

            struct par_chamb
            {
                struct { /* control point data */
                double phi, lam;
                double cosphi, sinphi;
                VECT v;
                CXY p;
                double Az;
                } c[3];
                CXY p;
                double beta_0, beta_1, beta_2;
            };
            	inline VECT /* distance and azimuth from point 1 to point 2 */
            vect(double dphi, double c1, double s1, double c2, double s2, double dlam) {
            	VECT v;
            	double cdl, dp, dl;
            
            	cdl = cos(dlam);
            	if (fabs(dphi) > 1. || fabs(dlam) > 1.)
            		v.r = aacos(s1 * s2 + c1 * c2 * cdl);
            	else { /* more accurate for smaller distances */
            		dp = sin(.5 * dphi);
            		dl = sin(.5 * dlam);
            		v.r = 2. * aasin(sqrt(dp * dp + c1 * c2 * dl * dl));
            	}
            	if (fabs(v.r) > TOL)
            		v.Az = atan2(c2 * sin(dlam), c1 * s2 - s1 * c2 * cdl);
            	else
            		v.r = v.Az = 0.;
            	return v;
            }
            	inline double /* law of cosines */
            lc(double b,double c,double a) {
            	return aacos(.5 * (b * b + c * c - a * a) / (b * c));
            }

            // template class, using CRTP to implement forward/inverse
            template <typename Geographic, typename Cartesian, typename Parameters>
            struct base_chamb_spheroid : public base_t_f<base_chamb_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>
            {

                 typedef double geographic_type;
                 typedef double cartesian_type;

                par_chamb m_proj_parm;

                inline base_chamb_spheroid(const Parameters& par)
                    : base_t_f<base_chamb_spheroid<Geographic, Cartesian, Parameters>,
                     Geographic, Cartesian, Parameters>(*this, par) {}

                inline void fwd(geographic_type& lp_lon, geographic_type& lp_lat, cartesian_type& xy_x, cartesian_type& xy_y) const
                {
                	double sinphi, cosphi, a;
                	VECT v[3];
                	int i, j;
                
                	sinphi = sin(lp_lat);
                	cosphi = cos(lp_lat);
                	for (i = 0; i < 3; ++i) { /* dist/azimiths from control */
                		v[i] = vect(lp_lat - this->m_proj_parm.c[i].phi, this->m_proj_parm.c[i].cosphi, this->m_proj_parm.c[i].sinphi,
                			cosphi, sinphi, lp_lon - this->m_proj_parm.c[i].lam);
                		if ( ! v[i].r)
                			break;
                		v[i].Az = adjlon(v[i].Az - this->m_proj_parm.c[i].v.Az);
                	}
                	if (i < 3) /* current point at control point */
                        { xy_x = this->m_proj_parm.c[i].p.x; xy_y = this->m_proj_parm.c[i].p.y; }
                	else { /* point mean of intersepts */
                        { xy_x = this->m_proj_parm.p.x; xy_y = this->m_proj_parm.p.y; }
                		for (i = 0; i < 3; ++i) {
                			j = i == 2 ? 0 : i + 1;
                			a = lc(this->m_proj_parm.c[i].v.r, v[i].r, v[j].r);
                			if (v[i].Az < 0.)
                				a = -a;
                			if (! i) { /* coord comp unique to each arc */
                				xy_x += v[i].r * cos(a);
                				xy_y -= v[i].r * sin(a);
                			} else if (i == 1) {
                				a = this->m_proj_parm.beta_1 - a;
                				xy_x -= v[i].r * cos(a);
                				xy_y -= v[i].r * sin(a);
                			} else {
                				a = this->m_proj_parm.beta_2 - a;
                				xy_x += v[i].r * cos(a);
                				xy_y += v[i].r * sin(a);
                			}
                		}
                		xy_x *= THIRD; /* mean of arc intercepts */
                		xy_y *= THIRD;
                	}
                }
            };

            // Chamberlin Trimetric
            template <typename Parameters>
            void setup_chamb(Parameters& par, par_chamb& proj_parm)
            {
            	int i, j;
            	char line[10];
            	for (i = 0;
             i < 3;
             ++i) { /* get control point locations */
            		(void)sprintf(line, "rlat_%d", i+1);
            		proj_parm.c[i].phi = pj_param(par.params, line).f;
            		(void)sprintf(line, "rlon_%d", i+1);
            		proj_parm.c[i].lam = pj_param(par.params, line).f;
            		proj_parm.c[i].lam = adjlon(proj_parm.c[i].lam - par.lam0);
            		proj_parm.c[i].cosphi = cos(proj_parm.c[i].phi);
            		proj_parm.c[i].sinphi = sin(proj_parm.c[i].phi);
            	}
            	for (i = 0;
             i < 3;
             ++i) { /* inter ctl pt. distances and azimuths */
            		j = i == 2 ? 0 : i + 1;
            		proj_parm.c[i].v = vect(proj_parm.c[j].phi - proj_parm.c[i].phi, proj_parm.c[i].cosphi, proj_parm.c[i].sinphi,
            			proj_parm.c[j].cosphi, proj_parm.c[j].sinphi, proj_parm.c[j].lam - proj_parm.c[i].lam);
            		if (! proj_parm.c[i].v.r) throw proj_exception(-25);
            		/* co-linearity problem ignored for now */
            	}
            	proj_parm.beta_0 = lc(proj_parm.c[0].v.r, proj_parm.c[2].v.r, proj_parm.c[1].v.r);
            	proj_parm.beta_1 = lc(proj_parm.c[0].v.r, proj_parm.c[1].v.r, proj_parm.c[2].v.r);
            	proj_parm.beta_2 = PI - proj_parm.beta_0;
            	proj_parm.p.y = 2. * (proj_parm.c[0].p.y = proj_parm.c[1].p.y = proj_parm.c[2].v.r * sin(proj_parm.beta_0));
            	proj_parm.c[2].p.y = 0.;
            	proj_parm.c[0].p.x = - (proj_parm.c[1].p.x = 0.5 * proj_parm.c[0].v.r);
            	proj_parm.p.x = proj_parm.c[2].p.x = proj_parm.c[0].p.x + proj_parm.c[2].v.r * cos(proj_parm.beta_0);
            	par.es = 0.;
                // par.fwd = s_forward;
            }

        }} // namespace impl::chamb
    #endif // doxygen 

    /*!
        \brief Chamberlin Trimetric projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Miscellaneous
         - Spheroid
         - no inverse
         - lat_1= lon_1= lat_2= lon_2= lat_3= lon_3=
        \par Example
        \image html ex_chamb.gif
    */
    template <typename Geographic, typename Cartesian, typename Parameters = parameters>
    struct chamb_spheroid : public impl::chamb::base_chamb_spheroid<Geographic, Cartesian, Parameters>
    {
        inline chamb_spheroid(const Parameters& par) : impl::chamb::base_chamb_spheroid<Geographic, Cartesian, Parameters>(par)
        {
            impl::chamb::setup_chamb(this->m_par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_IMPL
    namespace impl
    {

        // Factory entry(s)
        template <typename Geographic, typename Cartesian, typename Parameters>
        class chamb_entry : public impl::factory_entry<Geographic, Cartesian, Parameters>
        {
            public :
                virtual projection<Geographic, Cartesian>* create_new(const Parameters& par) const
                {
                    return new base_v_f<chamb_spheroid<Geographic, Cartesian, Parameters>, Geographic, Cartesian, Parameters>(par);
                }
        };

        template <typename Geographic, typename Cartesian, typename Parameters>
        inline void chamb_init(impl::base_factory<Geographic, Cartesian, Parameters>& factory)
        {
            factory.add_to_factory("chamb", new chamb_entry<Geographic, Cartesian, Parameters>);
        }

    } // namespace impl 
    #endif // doxygen

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_CHAMB_HPP

