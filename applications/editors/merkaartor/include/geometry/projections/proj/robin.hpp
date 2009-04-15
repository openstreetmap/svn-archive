#ifndef _PROJECTIONS_ROBIN_HPP
#define _PROJECTIONS_ROBIN_HPP

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
#include <geometry/projections/impl/function_overloads.hpp>

namespace projection
{
	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{
		namespace robin
		{
			static const double FXC = 0.8487;
			static const double FYC = 1.3523;
			static const double C1 = 11.45915590261646417544;
			static const double RC1 = 0.08726646259971647884;
			static const int NODES = 18;
			static const double ONEEPS = 1.000001;
			static const double EPS = 1e-8;

			/* note: following terms based upon 5 deg. intervals in degrees. */
			static struct COEFS {
				double c0, c1, c2, c3;
			} X[] = {
			{1,	-5.67239e-12,	-7.15511e-05,	3.11028e-06},
			{0.9986,	-0.000482241,	-2.4897e-05,	-1.33094e-06},
			{0.9954,	-0.000831031,	-4.4861e-05,	-9.86588e-07},
			{0.99,	-0.00135363,	-5.96598e-05,	3.67749e-06},
			{0.9822,	-0.00167442,	-4.4975e-06,	-5.72394e-06},
			{0.973,	-0.00214869,	-9.03565e-05,	1.88767e-08},
			{0.96,	-0.00305084,	-9.00732e-05,	1.64869e-06},
			{0.9427,	-0.00382792,	-6.53428e-05,	-2.61493e-06},
			{0.9216,	-0.00467747,	-0.000104566,	4.8122e-06},
			{0.8962,	-0.00536222,	-3.23834e-05,	-5.43445e-06},
			{0.8679,	-0.00609364,	-0.0001139,	3.32521e-06},
			{0.835,	-0.00698325,	-6.40219e-05,	9.34582e-07},
			{0.7986,	-0.00755337,	-5.00038e-05,	9.35532e-07},
			{0.7597,	-0.00798325,	-3.59716e-05,	-2.27604e-06},
			{0.7186,	-0.00851366,	-7.0112e-05,	-8.63072e-06},
			{0.6732,	-0.00986209,	-0.000199572,	1.91978e-05},
			{0.6213,	-0.010418,	8.83948e-05,	6.24031e-06},
			{0.5722,	-0.00906601,	0.000181999,	6.24033e-06},
			{0.5322, 0.,0.,0.}  },
			Y[] = {
			{0,	0.0124,	3.72529e-10,	1.15484e-09},
			{0.062,	0.0124001,	1.76951e-08,	-5.92321e-09},
			{0.124,	0.0123998,	-7.09668e-08,	2.25753e-08},
			{0.186,	0.0124008,	2.66917e-07,	-8.44523e-08},
			{0.248,	0.0123971,	-9.99682e-07,	3.15569e-07},
			{0.31,	0.0124108,	3.73349e-06,	-1.1779e-06},
			{0.372,	0.0123598,	-1.3935e-05,	4.39588e-06},
			{0.434,	0.0125501,	5.20034e-05,	-1.00051e-05},
			{0.4968,	0.0123198,	-9.80735e-05,	9.22397e-06},
			{0.5571,	0.0120308,	4.02857e-05,	-5.2901e-06},
			{0.6176,	0.0120369,	-3.90662e-05,	7.36117e-07},
			{0.6769,	0.0117015,	-2.80246e-05,	-8.54283e-07},
			{0.7346,	0.0113572,	-4.08389e-05,	-5.18524e-07},
			{0.7903,	0.0109099,	-4.86169e-05,	-1.0718e-06},
			{0.8435,	0.0103433,	-6.46934e-05,	5.36384e-09},
			{0.8936,	0.00969679,	-6.46129e-05,	-8.54894e-06},
			{0.9394,	0.00840949,	-0.000192847,	-4.21023e-06},
			{0.9761,	0.00616525,	-0.000256001,	-4.21021e-06},
			{1., 0.,0.,0} };

			// template class, using CRTP to implement forward/inverse
			template <typename LL, typename XY, typename PAR>
			struct base_robin_spheroid : public base_t_fi<base_robin_spheroid<LL, XY, PAR>, LL, XY, PAR>
			{

				typedef typename base_t_fi<base_robin_spheroid<LL, XY, PAR>, LL, XY, PAR>::LL_T LL_T;
				typedef typename base_t_fi<base_robin_spheroid<LL, XY, PAR>, LL, XY, PAR>::XY_T XY_T;


				inline base_robin_spheroid(const PAR& par)
					: base_t_fi<base_robin_spheroid<LL, XY, PAR>, LL, XY, PAR>(*this, par) {}

				inline double  V(const COEFS& C, double z) const
				{ return (C.c0 + z * (C.c1 + z * (C.c2 + z * C.c3))); }
				inline double DV(const COEFS& C, double z) const
				{ return (C.c1 + z * (C.c2 + C.c2 + z * 3. * C.c3)); }

				inline void fwd(LL_T& lp_lon, LL_T& lp_lat, XY_T& xy_x, XY_T& xy_y) const
				{
					int i;
					double dphi;

					i = int_floor((dphi = fabs(lp_lat)) * C1);
					if (i >= NODES) i = NODES - 1;
					dphi = RAD_TO_DEG * (dphi - RC1 * i);
					xy_x = V(X[i], dphi) * FXC * lp_lon;
					xy_y = V(Y[i], dphi) * FYC;
					if (lp_lat < 0.) xy_y = -xy_y;
				}

				inline void inv(XY_T& xy_x, XY_T& xy_y, LL_T& lp_lon, LL_T& lp_lat) const
				{
					int i;
					double t, t1;
					struct COEFS T;

					lp_lon = xy_x / FXC;
					lp_lat = fabs(xy_y / FYC);
					if (lp_lat >= 1.) { /* simple pathologic cases */
						if (lp_lat > ONEEPS) throw proj_exception();
						else {
							lp_lat = xy_y < 0. ? -HALFPI : HALFPI;
							lp_lon /= X[NODES].c0;
						}
					} else { /* general problem */
						/* in Y space, reduce to table interval */
						for (i = int_floor(lp_lat * NODES);;) {
							if (Y[i].c0 > lp_lat) --i;
							else if (Y[i+1].c0 <= lp_lat) ++i;
							else break;
						}
						T = Y[i];
						/* first guess, linear interp */
						t = 5. * (lp_lat - T.c0)/(Y[i+1].c0 - T.c0);
						/* make into root */
						T.c0 -= lp_lat;
						for (;;) { /* Newton-Raphson reduction */
							t -= t1 = V(T,t) / DV(T,t);
							if (fabs(t1) < EPS)
								break;
						}
						lp_lat = (5 * i + t) * DEG_TO_RAD;
						if (xy_y < 0.) lp_lat = -lp_lat;
						lp_lon /= V(X[i], t);
					}
				}
			};

			// Robinson
			template <typename PAR>
			void setup_robin(PAR& par)
			{
				par.es = 0.;
				// par.inv = s_inverse;
				// par.fwd = s_forward;
			}

		} // namespace robin
	} //namespaces impl
	#endif // doxygen

	/*!
		\brief Robinson projection
		\ingroup projections
		\tparam LL latlong point type
		\tparam XY xy point type
		\tparam PAR parameter type
		\par Projection characteristics
		 - Pseudocylindrical
		 - Spheroid
		\par Example
		\image html ex_robin.gif
	*/
	template <typename LL, typename XY, typename PAR = parameters>
	struct robin_spheroid : public impl::robin::base_robin_spheroid<LL, XY, PAR>
	{
		inline robin_spheroid(const PAR& par) : impl::robin::base_robin_spheroid<LL, XY, PAR>(par)
		{
			impl::robin::setup_robin(this->m_par);
		}
	};

	#ifndef DOXYGEN_NO_IMPL
	namespace impl
	{

		// Factory entry(s)
		template <typename LL, typename XY, typename PAR>
		class robin_entry : public impl::factory_entry<LL, XY, PAR>
		{
			public :
				virtual projection<LL, XY>* create_new(const PAR& par) const
				{
					return new base_v_fi<robin_spheroid<LL, XY, PAR>, LL, XY, PAR>(par);
				}
		};

		template <typename LL, typename XY, typename PAR>
		inline void robin_init(impl::base_factory<LL, XY, PAR>& factory)
		{
			factory.add_to_factory("robin", new robin_entry<LL, XY, PAR>);
		}

	} // namespace impl
	#endif // doxygen

}

#endif // _PROJECTIONS_ROBIN_HPP

