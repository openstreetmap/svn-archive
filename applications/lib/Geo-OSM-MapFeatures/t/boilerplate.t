#!perl -T

use strict;
use warnings;
use Test::More tests => 14;

sub not_in_file_ok {
    my ($filename, %regex) = @_;
    open( my $fh, '<', $filename )
        or die "couldn't open $filename for reading: $!";

    my %violated;

    while (my $line = <$fh>) {
        while (my ($desc, $regex) = each %regex) {
            if ($line =~ $regex) {
                push @{$violated{$desc}||=[]}, $.;
            }
        }
    }

    if (%violated) {
        fail("$filename contains boilerplate text");
        diag "$_ appears on lines @{$violated{$_}}" for keys %violated;
    } else {
        pass("$filename contains no boilerplate text");
    }
}

sub module_boilerplate_ok {
    my ($module) = @_;
    not_in_file_ok($module =>
        'the great new $MODULENAME'   => qr/ - The great new /,
        'boilerplate description'     => qr/Quick summary of what the module/,
        'stub function definition'    => qr/function[12]/,
    );
}

not_in_file_ok(README =>
  "The README is used..."       => qr/The README is used/,
  "'version information here'"  => qr/to provide version information/,
);

not_in_file_ok(Changes =>
  "placeholder date/time"       => qr(Date/time)
);

module_boilerplate_ok('lib/Geo/OSM/MapFeatures.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Key.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Type.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Value.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Value/Date.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Value/NumWithUnit.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Value/Range.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Value/Num.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Value/Time.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Value/Userdef.pm');
module_boilerplate_ok('lib/Geo/OSM/MapFeatures/Feature/Value/List.pm');
