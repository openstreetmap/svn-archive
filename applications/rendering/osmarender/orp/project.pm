use Geo::Proj4;

our ($minlat, $maxlat, $minlon, $maxlon);
our $project;

my $xyscale = 0.663;            # fudge to give output size consistent with plain or/p

my $wgs84 = Geo::Proj4->new(proj => 'latlon', datum => 'WGS84') or die Geo::Proj4->error;
my $proj = Geo::Proj4->new(init => get_variable("projection")) or die Geo::Proj4->error;

# Approximate projected bounds; TODO: support x,y bounds
my $minx = ($wgs84->transform($proj, [$minlon, 0.5*($minlat+$maxlat)])->[0]) or die Geo::Proj4->error;
my $miny = ($wgs84->transform($proj, [0.5*($minlon+$maxlon), $minlat])->[1]) or die Geo::Proj4->error;
my $maxx = ($wgs84->transform($proj, [$maxlon, 0.5*($minlat+$maxlat)])->[0]) or die Geo::Proj4->error;
my $maxy = ($wgs84->transform($proj, [0.5*($minlon+$maxlon), $maxlat])->[1]) or die Geo::Proj4->error;

sub project_geo
{
    my ($lat, $lon) = @{shift()};
    my $point = $wgs84->transform($proj, [$lon, $lat]) or die Geo::Proj4->error;
    my ($x, $y) = @$point;
    return [
            ($x - $minx) * $xyscale,
            $documentHeight - ($y - $miny) * $xyscale
           ];
}

$project = \&project_geo;

our (%gridx, %gridy);
# Grid lines
if (get_variable("showGrid") eq "yes" && get_variable("gridMajor")) {
    # grid spacing in grid units.
    my $gridMajor = get_variable("gridMajor", 1000);
    # how many minor divisions make up a major one
    my $gridMinor = get_variable("gridMinor", 100);
    foreach my $i (int(1+$miny/$gridMinor) .. int($maxy/$gridMinor)) {
        $gridy{$documentHeight - ($gridMinor * $i - $miny) * $xyscale}
          = {class => $i * $gridMinor % $gridMajor ? 'map-grid-line-minor' : 'map-grid-line-major',
             id => "grid-hori-$i",
             label => $i};
    }
    foreach my $i (int(1+$minx/$gridMinor) .. int($maxx/$gridMinor)) {
        $gridx{($gridMinor * $i - $minx) * $xyscale}
          = {class => $i * $gridMinor % $gridMajor ? 'map-grid-line-minor' : 'map-grid-line-major',
             id => "grid-hori-$i",
             label => $i};
    }
}

1;
