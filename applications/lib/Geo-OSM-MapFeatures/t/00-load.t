#!perl -T

use Test::More tests => 12;

BEGIN {
	use_ok( 'Geo::OSM::MapFeatures' );
	use_ok( 'Geo::OSM::MapFeatures::Feature' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Key' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Type' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Value' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Value::Date' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Value::NumWithUnit' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Value::Range' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Value::Num' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Value::Time' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Value::Userdef' );
	use_ok( 'Geo::OSM::MapFeatures::Feature::Value::List' );
}

diag( "Testing Geo::OSM::MapFeatures $Geo::OSM::MapFeatures::VERSION, Perl $], $^X" );
