// Generic Geometry Library
//
// Copyright Bruno Lalande 2008, 2009
// Copyright Barend Gehrels 1995-2009, Geodan Holding S.V. Amsterdam, the Netherlands.
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)


#ifndef _GEOMETRY_NSPHERE_CONCEPT_HPP
#define _GEOMETRY_NSPHERE_CONCEPT_HPP


#include <boost/concept_check.hpp>

#include <geometry/core/access.hpp>
#include <geometry/core/radius.hpp>



namespace geometry
{
	/*!
		\brief Checks Nsphere concept (const version)
		\ingroup concepts
		\details The ConstNsphere concept check the same as the Nsphere concept,
		but does not check write access.
	*/
	template <typename S>
	struct ConstNsphere
	{
		private :
			typedef typename point_type<S>::type P;
			typedef typename radius_type<S>::type R;

			/// Internal structure to check if access is OK for all dimensions
			template <size_t D, size_t N>
			struct dimension_checker
			{
				static void check()
				{
					typedef typename coordinate_type<S>::type T;
					const S* s = 0;
					T coord(geometry::get<D>(*s));
					(void)sizeof(coord); // To avoid "unused variable" warnings
					dimension_checker<D + 1, N>::check();
				}
			};

			template <size_t N>
			struct dimension_checker<N, N>
			{
				static void check() {}
			};

		public :
			/// BCCL macro to check the ConstNsphere concept
			BOOST_CONCEPT_USAGE(ConstNsphere)
			{
				static const size_t N = dimension<S>::value;
				dimension_checker<0, N>::check();
				dimension_checker<0, N>::check();

				// Check radius access
				const S* s = 0;
				R coord(geometry::get_radius<0>(*s));
				(void)sizeof(coord); // To avoid "unused variable" warnings
			}
	};


	/*!
		\brief Checks nsphere concept, using Boost Concept Check Library and metafunctions
		\ingroup concepts
	*/
	template <typename S>
	struct Nsphere
	{
		private :
			BOOST_CONCEPT_ASSERT((ConstNsphere<S>));

			typedef typename point_type<S>::type P;
			typedef typename radius_type<S>::type R;

			/// Internal structure to check if access is OK for all dimensions
			template <size_t D, size_t N>
			struct dimension_checker
			{
				static void check()
				{
					S* s;
					geometry::set<D>(*s, geometry::get<D>(*s));
					dimension_checker<D + 1, N>::check();
				}
			};

			template <size_t N>
			struct dimension_checker<N, N>
			{
				static void check() {}
			};

		public :
			/// BCCL macro to check the Nsphere concept
			BOOST_CONCEPT_USAGE(Nsphere)
			{
				static const size_t N = dimension<S>::value;
				dimension_checker<0, N>::check();
				dimension_checker<0, N>::check();

				// Check radius access
				S* s = 0;
				set_radius<0>(*s, get_radius<0>(*s));

			}
	};


}


#endif
