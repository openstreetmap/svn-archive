#ifndef _PROJECTIONS_LAEA_HPP
#define _PROJECTIONS_LAEA_HPP

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
#include <geometry/projections/impl/pj_qsfn.hpp>
#include <geometry/projections/impl/pj_auth.hpp>

namespace projection
{
	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{
		namespace laea
		{
			static const double EPS10 = 1.e-10;
			static const int NITER = 20;
			static const double CONV = 1.e-10;
			static const int N_POLE = 0;
			static const int S_POLE = 1;
			static const int EQUIT = 2;
			static const int OBLIQ = 3;

			struct par_laea
			{
				double sinb1;
				double cosb1;
				double xmf;
				double ymf;
				double mmf;
				double qp;
				double dd;
				double rq;
				double apa[APA_SIZE];
				int  mode;
			};

			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_laea_ellipsoid : public base_t_fi<base_laea_ellipsoid<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_fi<base_laea_ellipsoid<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_fi<base_laea_ellipsoid<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;

				par_laea m_proj_parm;

				inline base_laea_ellipsoid(const PAR& par)
					: base_t_fi<base_laea_ellipsoid<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{
					double coslam, sinlam, sinphi, q, sinb=0.0, cosb=0.0, b=0.0;

					coslam = cos(lp_lon);
					sinlam = sin(lp_lon);
					sinphi = sin(lp_lat);
					q = pj_qsfn(sinphi, this->m_par.e, this->m_par.one_es);
					if (this->m_proj_parm.mode == OBLIQ || this->m_proj_parm.mode == EQUIT) {
						sinb = q / this->m_proj_parm.qp;
						cosb = sqrt(1. - sinb * sinb);
					}
					switch (this->m_proj_parm.mode) {
					case OBLIQ:
						b = 1. + this->m_proj_parm.sinb1 * sinb + this->m_proj_parm.cosb1 * cosb * coslam;
						break;
					case EQUIT:
						b = 1. + cosb * coslam;
						break;
					case N_POLE:
						b = HALFPI + lp_lat;
						q = this->m_proj_parm.qp - q;
						break;
					case S_POLE:
						b = lp_lat - HALFPI;
						q = this->m_proj_parm.qp + q;
						break;
					}
					if (fabs(b) < EPS10) throw proj_exception();;
					switch (this->m_proj_parm.mode) {
					case OBLIQ:
						xy_y = this->m_proj_parm.ymf * ( b = sqrt(2. / b) )
						   * (this->m_proj_parm.cosb1 * sinb - this->m_proj_parm.sinb1 * cosb * coslam);
						goto eqcon;
						break;
					case EQUIT:
						xy_y = (b = sqrt(2. / (1. + cosb * coslam))) * sinb * this->m_proj_parm.ymf;
				eqcon:
						xy_x = this->m_proj_parm.xmf * b * cosb * sinlam;
						break;
					case N_POLE:
					case S_POLE:
						if (q >= 0.) {
							xy_x = (b = sqrt(q)) * sinlam;
							xy_y = coslam * (this->m_proj_parm.mode == S_POLE ? b : -b);
						} else
							xy_x = xy_y = 0.;
						break;
					}
				}

				inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
				{
					double cCe, sCe, q, rho, ab=0.0;

					switch (this->m_proj_parm.mode) {
					case EQUIT:
					case OBLIQ:
						if ((rho = hypot(xy_x /= this->m_proj_parm.dd, xy_y *=  this->m_proj_parm.dd)) < EPS10) {
							lp_lon = 0.;
							lp_lat = this->m_par.phi0;
							return;
						}
						cCe = cos(sCe = 2. * asin(.5 * rho / this->m_proj_parm.rq));
						xy_x *= (sCe = sin(sCe));
						if (this->m_proj_parm.mode == OBLIQ) {
							q = this->m_proj_parm.qp * (ab = cCe * this->m_proj_parm.sinb1 + xy_y * sCe * this->m_proj_parm.cosb1 / rho);
							xy_y = rho * this->m_proj_parm.cosb1 * cCe - xy_y * this->m_proj_parm.sinb1 * sCe;
						} else {
							q = this->m_proj_parm.qp * (ab = xy_y * sCe / rho);
							xy_y = rho * cCe;
						}
						break;
					case N_POLE:
						xy_y = -xy_y;
					case S_POLE:
						if (!(q = (xy_x * xy_x + xy_y * xy_y)) ) {
							lp_lon = 0.;
							lp_lat = this->m_par.phi0;
							return;
						}
						/*
						q = this->m_proj_parm.qp - q;
						*/
						ab = 1. - q / this->m_proj_parm.qp;
						if (this->m_proj_parm.mode == S_POLE)
							ab = - ab;
						break;
					}
					lp_lon = atan2(xy_x, xy_y);
					lp_lat = pj_authlat(asin(ab), this->m_proj_parm.apa);
				}
			};

			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_laea_spheroid : public base_t_fi<base_laea_spheroid<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_fi<base_laea_spheroid<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_fi<base_laea_spheroid<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;

				par_laea m_proj_parm;

				inline base_laea_spheroid(const PAR& par)
					: base_t_fi<base_laea_spheroid<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{
					double  coslam, cosphi, sinphi;

					sinphi = sin(lp_lat);
					cosphi = cos(lp_lat);
					coslam = cos(lp_lon);
					switch (this->m_proj_parm.mode) {
					case EQUIT:
						xy_y = 1. + cosphi * coslam;
						goto oblcon;
					case OBLIQ:
						xy_y = 1. + this->m_proj_parm.sinb1 * sinphi + this->m_proj_parm.cosb1 * cosphi * coslam;
				oblcon:
						if (xy_y <= EPS10) throw proj_exception();;
						xy_x = (xy_y = sqrt(2. / xy_y)) * cosphi * sin(lp_lon);
						xy_y *= this->m_proj_parm.mode == EQUIT ? sinphi :
						   this->m_proj_parm.cosb1 * sinphi - this->m_proj_parm.sinb1 * cosphi * coslam;
						break;
					case N_POLE:
						coslam = -coslam;
					case S_POLE:
						if (fabs(lp_lat + this->m_par.phi0) < EPS10) throw proj_exception();;
						xy_y = FORTPI - lp_lat * .5;
						xy_y = 2. * (this->m_proj_parm.mode == S_POLE ? cos(xy_y) : sin(xy_y));
						xy_x = xy_y * sin(lp_lon);
						xy_y *= coslam;
						break;
					}
				}

				inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
				{
					double  cosz=0.0, rh, sinz=0.0;

					rh = hypot(xy_x, xy_y);
					if ((lp_lat = rh * .5 ) > 1.) throw proj_exception();;
					lp_lat = 2. * asin(lp_lat);
					if (this->m_proj_parm.mode == OBLIQ || this->m_proj_parm.mode == EQUIT) {
						sinz = sin(lp_lat);
						cosz = cos(lp_lat);
					}
					switch (this->m_proj_parm.mode) {
					case EQUIT:
						lp_lat = fabs(rh) <= EPS10 ? 0. : asin(xy_y * sinz / rh);
						xy_x *= sinz;
						xy_y = cosz * rh;
						break;
					case OBLIQ:
						lp_lat = fabs(rh) <= EPS10 ? this->m_par.phi0 :
						   asin(cosz * this->m_proj_parm.sinb1 + xy_y * sinz * this->m_proj_parm.cosb1 / rh);
						xy_x *= sinz * this->m_proj_parm.cosb1;
						xy_y = (cosz - sin(lp_lat) * this->m_proj_parm.sinb1) * rh;
						break;
					case N_POLE:
						xy_y = -xy_y;
						lp_lat = HALFPI - lp_lat;
						break;
					case S_POLE:
						lp_lat -= HALFPI;
						break;
					}
					lp_lon = (xy_y == 0. && (this->m_proj_parm.mode == EQUIT || this->m_proj_parm.mode == OBLIQ)) ?
						0. : atan2(xy_x, xy_y);
				}
			};

			// Lambert Azimuthal Equal Area
			template <typename PAR>
			void setup_laea(PAR& par, par_laea& proj_parm)
			{
				double t;
				if (fabs((t = fabs(par.phi0)) - HALFPI) < EPS10)
					proj_parm.mode = par.phi0 < 0. ? S_POLE : N_POLE;
				else if (fabs(t) < EPS10)
					proj_parm.mode = EQUIT;
				else
					proj_parm.mode = OBLIQ;
				if (par.es) {
					double sinphi;
					par.e = sqrt(par.es);
					proj_parm.qp = pj_qsfn(1., par.e, par.one_es);
					proj_parm.mmf = .5 / (1. - par.es);
					pj_authset(par.es, proj_parm.apa);
					switch (proj_parm.mode) {
					case N_POLE:
					case S_POLE:
						proj_parm.dd = 1.;
						break;
					case EQUIT:
						proj_parm.dd = 1. / (proj_parm.rq = sqrt(.5 * proj_parm.qp));
						proj_parm.xmf = 1.;
						proj_parm.ymf = .5 * proj_parm.qp;
						break;
					case OBLIQ:
						proj_parm.rq = sqrt(.5 * proj_parm.qp);
						sinphi = sin(par.phi0);
						proj_parm.sinb1 = pj_qsfn(sinphi, par.e, par.one_es) / proj_parm.qp;
						proj_parm.cosb1 = sqrt(1. - proj_parm.sinb1 * proj_parm.sinb1);
						proj_parm.dd = cos(par.phi0) / (sqrt(1. - par.es * sinphi * sinphi) *
						   proj_parm.rq * proj_parm.cosb1);
						proj_parm.ymf = (proj_parm.xmf = proj_parm.rq) / proj_parm.dd;
						proj_parm.xmf *= proj_parm.dd;
						break;
					}
				// par.inv = e_inverse;
				// par.fwd = e_forward;
				} else {
					if (proj_parm.mode == OBLIQ) {
						proj_parm.sinb1 = sin(par.phi0);
						proj_parm.cosb1 = cos(par.phi0);
					}
				// par.inv = s_inverse;
				// par.fwd = s_forward;
				}
			}

		} // namespace laea
	} //namespaces impl
	#endif // doxygen

	/*!
		\brief Lambert Azimuthal Equal Area projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Azimuthal
		 - Spheroid
		 - Ellipsoid
		\par Example
		\image html ex_laea.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct laea_ellipsoid : public impl::laea::base_laea_ellipsoid<LL, XY, PAR>
	{
		inline laea_ellipsoid(const PAR& par) : impl::laea::base_laea_ellipsoid<LL, XY, PAR>(par)
		{
			impl::laea::setup_laea(this->m_par, this->m_proj_parm);
		}
	};

	/*!
		\brief Lambert Azimuthal Equal Area projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Azimuthal
		 - Spheroid
		 - Ellipsoid
		\par Example
		\image html ex_laea.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct laea_spheroid : public impl::laea::base_laea_spheroid<LL, XY, PAR>
	{
		inline laea_spheroid(const PAR& par) : impl::laea::base_laea_spheroid<LL, XY, PAR>(par)
		{
			impl::laea::setup_laea(this->m_par, this->m_proj_parm);
		}
	};

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{

		// Factory entry(s)
		template <typename LL, typename XY, typename PAR>
		class laea_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					if (par.es)
						return new base_v_fi<laea_ellipsoid<LL, XY, PAR>, LL, XY, PAR>(par);
					else
						return new base_v_fi<laea_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		inline void laea_init(impl::base_factory<LL, XY, PAR>& factory)
		{
			factory.add_to_factory("laea", new laea_entry<LL, XY, PAR>);
		}

	} // namespace impl
	#endif // doxygen

}

#endif // _PROJECTIONS_LAEA_HPP

