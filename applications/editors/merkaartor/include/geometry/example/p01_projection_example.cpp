// Generic Geometry Library
// Projection example 1, direct


#include <geometry/geometry.hpp>

#include <geometry/geometries/cartesian2d.hpp>
#include <geometry/geometries/latlong.hpp>

#include <geometry/io/wkt/streamwkt.hpp>

#include <geometry/projections/parameters.hpp>
#include <geometry/projections/proj/robin.hpp>


int main()
{
	using namespace geometry;

	// Initialize projection parameters
	projection::parameters par = projection::init("+ellps=WGS84 +units=m");

	// Construct a Robinson projection, using specified point types
	// (This delivers a projection without virtual methods. Note that in p02 example
	//  the projection is created using a factory, which delivers a projection with virtual methods)
	projection::robin_spheroid<point_ll_deg, point_2d> prj(par);

	// Define Amsterdam / Barcelona in decimal degrees / degrees/minutes
	point_ll_deg amsterdam = parse<point_ll_deg>("52.4N", "5.9E");
	point_ll_deg barcelona = parse<point_ll_deg>("41 23'N", "2 11'E");

	point_2d pa, pb;

	// Now do the projection. "Forward" means from latlong to meters.
	// (Note that a map projection might fail. This is not 'exceptional'.
	// Therefore the forward function does not throw but returns false)
	if (prj.forward(amsterdam, pa) && prj.forward(barcelona, pb))
	{
		std::cout << "Amsterdam: " << pa << std::endl << "Barcelona: " << pb << std::endl;

		std::cout << "Distance (unprojected):" << distance(amsterdam, barcelona) / 1000.0 << " km" << std::endl;
		std::cout << "Distance (  projected):" << distance(pa, pb) / 1000.0 << " km" << std::endl;

		// Do the inverse projection. "Inverse" means from meters to latlong
		// It also might fail or might not exist, not all projections
		// have their inverse implemented
		point_ll_deg a1;
		if (prj.inverse(pa, a1))
		{
			std::cout << "Amsterdam (original): " << amsterdam  << std::endl
				<< "Amsterdam (projected, and back):" << a1 << std::endl;
			std::cout << "Distance a-a': " << distance(amsterdam, a1) << " meter" << std::endl;
		}
	}

	return 0;
}
