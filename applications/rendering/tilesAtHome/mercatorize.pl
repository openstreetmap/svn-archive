#!/usr/bin/perl
use Geo::Proj4;
#project lat lon values into mlat mlon values, using mercator projection
# it takes infile, outfile as arguments

mercatorize(@ARGV);

#-----------------------------------------------------------------------------
# Transform lat/lon to mercator coordinates (in preprocess)
#-----------------------------------------------------------------------------
sub mercatorize
{
    my $proj = Geo::Proj4->new(proj => "merc");
    my ($inputFile) = @_;

    open(my $fr, "<", $inputFile) || return;
    while (my $line = <$fr>) {
        my ($lat, $lon)=undef;
	if ($line =~ /^(.*lat=['"]?)([^'"])+(['"]?.*$)/) {
            $lat = $2; 
        } 
	if ($line =~ /^(.*lon=['"]?)([^'"])+(['"]?.*$)/) {
	    $lon = $2; 
        }
	if ($lat or $lon) {
	    my ($x, $y) = $proj->forward($lat, $lon);
	    $line =~ s/(lat=['"][^'"]+['"]|lon=['"][^'"]+['"])/$1 mlat="$y" mlon="$x"/;
	}
	print $line;
    }
    close $fr; 
    return 1;
}

