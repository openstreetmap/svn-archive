use strict;
use warnings;
use Test::More;

# Ensure a recent version of Test::Pod::Coverage
my $min_tpc = 1.08;
eval "use Test::Pod::Coverage $min_tpc";
plan skip_all => "Test::Pod::Coverage $min_tpc required for testing POD coverage"
    if $@;

# Test::Pod::Coverage doesn't require a minimum Pod::Coverage version,
# but older versions don't recognize some common documentation styles
my $min_pc = 0.18;
eval "use Pod::Coverage $min_pc";
plan skip_all => "Pod::Coverage $min_pc required for testing POD coverage"
    if $@;

my $ok = 1;
my @modules = all_modules();
plan( tests => scalar @modules );

for my $module ( @modules ) {
    my $params;
    if( $module eq 'Geo::OSM::MapFeatures' ){
        $$params{trustme} = ['debug_download'];
    } elsif( $module eq 'Geo::OSM::MapFeatures::Feature' ){
        $$params{also_private} = ['new', 'stringify'];
    } elsif( $module eq 'Geo::OSM::MapFeatures::Feature::Key' ){
        $$params{also_private} = ['new', 'stringify'];
    } elsif( $module eq 'Geo::OSM::MapFeatures::Feature::Value' ){
        $$params{also_private} = ['new', 'init', 'compare', 'stringify'];
    } elsif( $module eq 'Geo::OSM::MapFeatures::Feature::Type' ){
        $$params{also_private} = ['new', 'stringify'];
    } elsif( $module =~ /^Geo::OSM::MapFeatures::Feature::Value::/ ){
        $$params{also_private} = ['init', 'stringify'];
    }

    my $thisok = pod_coverage_ok( $module, $params, "Pod coverage on $module" );
    $ok = 0 unless $thisok;
}
