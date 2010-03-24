#!/usr/bin/perl

# Helper code for or/p
#

# -------------------------------------------------------------------
# sub find_area_center($way)
#
# finds the centre point for an area where to place a text or
# icon.
#
# simply returns the centre of the bounding box.

sub find_area_center
{
    my $way = shift;
    my $nodes;
    if (ref $way eq 'way')
    {
        $nodes = $way->{'nodes'};
    }
    elsif (ref $way eq 'multipolygon')
    {
        foreach (@{$way->{'outer'}})
        {
            push(@$nodes, @{$_->{'nodes'}});
        }
    }

    # TOOD: work in projected coordinates - will allow wrapping over date-line,
    # and handle polar azimuthal projection well

    my $maxlat = -180;
    my $maxlon = -180;
    my $minlat = 180;
    my $minlon = 180;

    foreach (@$nodes)
    {
        $maxlat = $_->{"lat"} if ($_->{"lat"} > $maxlat);
        $maxlon = $_->{"lon"} if ($_->{"lon"} > $maxlon);
        $minlat = $_->{"lat"} if ($_->{"lat"} < $minlat);
        $minlon = $_->{"lon"} if ($_->{"lon"} < $minlon);
    }

    return [ ($maxlat + $minlat) / 2, ($maxlon + $minlon) / 2 ];
}

1;
