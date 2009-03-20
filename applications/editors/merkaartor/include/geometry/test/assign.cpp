// Generic Geometry Library test file
//
// Copyright Barend Gehrels, 1995-2009, Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande 2008
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)


#include <boost/test/included/test_exec_monitor.hpp>

#include <geometry/algorithms/assign.hpp>
#include <geometry/algorithms/num_points.hpp>

#include <geometry/geometries/geometries.hpp>

#include "common.hpp"


using namespace geometry;

template <typename L>
void check_linestring_2d(const L& line)
{
	BOOST_CHECK((boost::size(line) == 3));
	BOOST_CHECK((geometry::num_points(line) == 3));

	typedef typename point_type<L>::type P;
	const P& p0 = line[0];
	BOOST_CHECK(get<0>(p0) == 1);
	BOOST_CHECK(get<1>(p0) == 2);

	const P& p1 = line[1];
	BOOST_CHECK(get<0>(p1) == 3);
	BOOST_CHECK(get<1>(p1) == 4);

	const P& p2 = line[2];
	BOOST_CHECK(get<0>(p2) == 5);
	BOOST_CHECK(get<1>(p2) == 6);
}


template <typename P>
void test_assign_linestring_2d()
{
	geometry::linestring<P> line;

	// Test assignment of plain array (note that this is only possible if adapted c-array is included!
	const double coors[3][2] = { {1, 2}, {3, 4}, {5, 6} };
	geometry::assign(line, coors);
	check_linestring_2d(line);

	// Test assignment of point array
	P points[3];
	geometry::assign(points[0], 1, 2);
	geometry::assign(points[1], 3, 4);
	geometry::assign(points[2], 5, 6);
	geometry::assign(line, points);
	check_linestring_2d(line);

	// Test assignment of array with different point-type
	boost::tuple<float, float> tuples[3];
	tuples[0] = boost::make_tuple(1, 2);
	tuples[1] = boost::make_tuple(3, 4);
	tuples[2] = boost::make_tuple(5, 6);
	geometry::assign(line, tuples);
	check_linestring_2d(line);
}

template <typename P>
void test_assign_box_2d()
{

	typedef box<P> B;
	B b;
	geometry::assign(b, 1, 2, 3, 4);
	BOOST_CHECK((get<min_corner, 0>(b) == 1));
	BOOST_CHECK((get<min_corner, 1>(b) == 2));
	BOOST_CHECK((get<max_corner, 0>(b) == 3));
	BOOST_CHECK((get<max_corner, 1>(b) == 4));

	geometry::assign_zero(b);
	BOOST_CHECK((get<min_corner, 0>(b) == 0));
	BOOST_CHECK((get<min_corner, 1>(b) == 0));
	BOOST_CHECK((get<max_corner, 0>(b) == 0));
	BOOST_CHECK((get<max_corner, 1>(b) == 0));

	geometry::assign_inverse(b);
	BOOST_CHECK((get<min_corner, 0>(b) > 9999));
	BOOST_CHECK((get<min_corner, 1>(b) > 9999));
	BOOST_CHECK((get<max_corner, 0>(b) < 9999));
	BOOST_CHECK((get<max_corner, 1>(b) < 9999));

}




template <typename P>
void test_assign_point_3d()
{
	P p;
	geometry::assign(p, 1, 2, 3);
	BOOST_CHECK(get<0>(p) == 1);
	BOOST_CHECK(get<1>(p) == 2);
	BOOST_CHECK(get<2>(p) == 3);

	geometry::impl::assign::assign_value(p, 123);
	BOOST_CHECK(get<0>(p) == 123);
	BOOST_CHECK(get<1>(p) == 123);
	BOOST_CHECK(get<2>(p) == 123);

	geometry::assign_zero(p);
	BOOST_CHECK(get<0>(p) == 0);
	BOOST_CHECK(get<1>(p) == 0);
	BOOST_CHECK(get<2>(p) == 0);

}


template <typename P>
void test_assign_point_2d()
{
	P p;
	geometry::assign(p, 1, 2);
	BOOST_CHECK(get<0>(p) == 1);
	BOOST_CHECK(get<1>(p) == 2);

	geometry::impl::assign::assign_value(p, 123);
	BOOST_CHECK(get<0>(p) == 123);
	BOOST_CHECK(get<1>(p) == 123);

	geometry::assign_zero(p);
	BOOST_CHECK(get<0>(p) == 0);
	BOOST_CHECK(get<1>(p) == 0);
}


int test_main(int, char* [])
{
	test_assign_point_3d<int[3]>();
	test_assign_point_3d<float[3]>();
	test_assign_point_3d<double[3]>();
	test_assign_point_3d<test_point>();
	test_assign_point_3d<point<int, 3, geometry::cs::cartesian> >();
	test_assign_point_3d<point<float, 3, geometry::cs::cartesian> >();
	test_assign_point_3d<point<double, 3, geometry::cs::cartesian> >();

	test_assign_point_2d<int[2]>();
	test_assign_point_2d<float[2]>();
	test_assign_point_2d<double[2]>();
	test_assign_point_2d<point<int, 2, geometry::cs::cartesian> >();
	test_assign_point_2d<point<float, 2, geometry::cs::cartesian> >();
	test_assign_point_2d<point<double, 2, geometry::cs::cartesian> >();

	test_assign_box_2d<int[2]>();
	test_assign_box_2d<float[2]>();
	test_assign_box_2d<double[2]>();
	test_assign_box_2d<point<int, 2, geometry::cs::cartesian> >();
	test_assign_box_2d<point<float, 2, geometry::cs::cartesian> >();
	test_assign_box_2d<point<double, 2, geometry::cs::cartesian> >();

	test_assign_linestring_2d<point<int, 2, geometry::cs::cartesian> >();
	test_assign_linestring_2d<point<float, 2, geometry::cs::cartesian> >();
	test_assign_linestring_2d<point<double, 2, geometry::cs::cartesian> >();


	return 0;
}
