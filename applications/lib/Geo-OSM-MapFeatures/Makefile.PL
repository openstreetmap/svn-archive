use strict;
use warnings;
use ExtUtils::MakeMaker;

WriteMakefile(
    NAME                => 'Geo::OSM::MapFeatures',
    AUTHOR              => 'Knut Arne Bjørndal <bob+osm@cakebox.net>',
    VERSION_FROM        => 'lib/Geo/OSM/MapFeatures.pm',
    ABSTRACT_FROM       => 'lib/Geo/OSM/MapFeatures.pm',
    PL_FILES            => {},
    PREREQ_PM => {
        'Test::More' => 0,
    },
    dist                => { COMPRESS => 'gzip -9f', SUFFIX => 'gz', },
    clean               => { FILES => 'Geo-OSM-MapFeatures-*' },
);
