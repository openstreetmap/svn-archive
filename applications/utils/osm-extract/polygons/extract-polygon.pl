#!/usr/bin/perl

# This script extracts all nodes, segments, and ways lying inside a polygon
#
# The script first compiles a polygon from the data, then simplifies it,
# and then processes the planet file. In processing the file, first a
# bounding box is used to exclude all nodes that lie outside, and the
# more expensive polygon check is only made for those inside.
#
# Author Frederik Ramm <frederik@remote.org> / public domain
# Author Keith Sharp <kms@passback.co.uk> / public domain

# Get this from CPAN!
use Math::Polygon;
use Getopt::Long;
use File::Basename;
use strict;

my $borderpolys = [];
my $currentpoints;
my $minlon = 999;
my $minlat = 999;
my $maxlat = -999;
my $maxlon = -999;

my $used_nodes = {};
my $used_segments = {};

my $help;
my $polyfile;
my $infile;
my $outfile;
my $remainsfile;
my $compress;
my $verbose = 0;

my $prog = basename($0);

sub usage ()
{
	print STDERR << "EOF";

This Perl script will process a planet.osm file and extract the nodes,
segments, and ways, falling within a polygon.

usage: $prog [-h] [-c number] [-i file] [-o file] [-r file] -p file

 -h       : print ths help message and exit.
 -c num   : reduce the number of points in the polygon by num %.  This 
            only takes effect if the polgon has more 100 points.
 -p file  : file containing the polygon definition.
 -i file  : OSM planet file to process.
 -o file  : OSM planet file to output.
 -r file  : (optional) outputs a simple list of all node ids that are within
            your selected area but are also used by segments not selected.
            Some forms of post-processing need this information.

If you do not supply an input or output file input will be read from STDIN
and output written to STDOUT.

A polygon file should be a plain text file with each line containing a
longitude followed by a latitude, for example a polygon defining Great
Britain and Ireland (and some of the smaller islands) would be:

1
	-0.6450E+01	0.4980E+02
	-0.2000E+01	0.4890E+02
	-0.1850E+01	0.4925E+02
	-0.2080E+01	0.4973E+02
	 0.1350E+01	0.5090E+02
	 0.2250E+01	0.5258E+02
	-0.0500E+01	0.6130E+02
	-0.8920E+01	0.5785E+02
	-0.1140E+02	0.5130E+02
	-0.6450E+01	0.4980E+02
END

Multiple polygons can be described by adding additional sections to the
polygon file.

EOF
	exit;
}

GetOptions ('h|help' => \$help,
			'c|compress=i' => \$compress,
			'v|verbose' => \$verbose,
			'i|input=s' => \$infile,
			'o|output=s' => \$outfile,
			'p|polygon=s' => \$polyfile,
			'r|remains=s' => \$remainsfile) || usage ();

if ($help) {
	usage ();
}

if (! $polyfile) {
	usage ();
}

open (PF, "$polyfile") || die "Could not open file: $polyfile: $!";

my $invert;
# initialize border polygon.
while(<PF>)
{
    if (/^(!?)\d/)
    {
        $invert = ($1 eq "!") ? 1 : 0;
        $currentpoints = [];
    }
    elsif (/^END/)
    {
        my $pol = Math::Polygon->new(points => $currentpoints);
	if (($compress > 0 && $compress < 100) && $pol->nrPoints > 99) {
        	my $simp = $pol->simplify($pol->nrPoints*(100-$compress)/100);
        	push(@{$borderpolys}, [$simp,$invert,[$simp->bbox]]);
        } else {
		push(@{$borderpolys}, [$pol,$invert,[$pol->bbox]]);
		}
        undef $currentpoints;
        if( $verbose )
        {
            printf STDERR "Added polygon: %d points (%.2f,%.2f)-(%.2f,%.2f) %s\n",
                $borderpolys->[-1][0]->nrPoints,
                @{$borderpolys->[-1][2]},
                ($borderpolys->[-1][1] ? "exclude" : "include");
        }
    }
    elsif (defined($currentpoints))
    {
        /^\s+([0-9.E+-]+)\s+([0-9.E+-]+)/ or die;
        push(@{$currentpoints}, [$2, $1]);
        $minlat = $2 if ($2 < $minlat);
        $maxlat = $2 if ($2 > $maxlat);
        $minlon = $1 if ($1 < $minlon);
        $maxlon = $1 if ($1 > $maxlon);
    }
}

close (PF);

if ($outfile) {
	open (OF, ">$outfile") || die "Could not open file: $outfile: $!";
} else {
	*OF = *STDOUT;
}

if ($infile) {
	open (IF, "<$infile") || die "Could not open file: $infile: $!";
} else {
	*IF = *STDIN;
}

if ($remainsfile) {
        open (RF, ">$remainsfile") || die "Could not open file: $remainsfile: $!";
}

print OF << "EOF";
<?xml version="1.0" encoding="UTF-8"?>
<osm version="0.3" generator="extract-polygon.pl">
EOF

my $copy;
my $waybuf;
my $count = 0;

while(<IF>) 
{
    $count++;
    if( $verbose and ($count % 10000) == 0 )
    {
        my $perc = tell(IF)*100/(-s IF);
        printf STDERR "\r%.2f%% ", $perc;
    }
    last if /^\s*<\/osm>/;

    # Note: we allow a minus in the ID to allow incomplete files also    
    if (/^\s*<node.*id=["'](-?\d+)['"].*lat=["']([Ee0-9.-]+)["'] lon=["']([Ee0-9.-]+)["']/)
    {
        $copy = 0;

        next if (($2 < $minlat) || ($2 > $maxlat));
        next if (($3 < $minlon) || ($3 > $maxlon));

        my $ll = [$2, $3];

        foreach my $p (@{$borderpolys})
        {
            my($poly,$invert,$bbox) = @$p;
            next if ($ll->[0] < $bbox->[0]) or ($ll->[0] > $bbox->[2]);
            next if ($ll->[1] < $bbox->[1]) or ($ll->[1] > $bbox->[3]);

            if ($poly->contains($ll))
            {
                # If this polygon is for exclusion, we immediately bail and go for the next point
                if( $invert )
                {
                    $copy = 0;
                    last;
                }
                $copy = 1;
            }
        }
        if( $copy )
        {
            $used_nodes->{$1} = 1;
            print OF;
        }
    }
    elsif (/^\s*<segment id=['"](-?\d+)["'] from=['"](-?\d+)["'] to=["'](-?\d+)["']/)
    {
        $copy = 0;
        if ($used_nodes->{$2} && $used_nodes->{$3})
        {
            $used_segments->{$1}=1;
            print OF;
            $copy = 1;
        }
        elsif ($remainsfile)  # Only test if we want the output
        {
            if ($used_nodes->{$2})
            {
                print RF "$2\n";
            }
            if ($used_nodes->{$3})
            {
                print RF "$3\n";
            }
        }
    }
    elsif (/^\s*<way /)
    {
        $copy = 0;
        $waybuf = $_;
        undef $used_nodes;
    }
    elsif ($copy)
    {
        print OF;
    }
    elsif (/^\s*<seg id=['"](.*)["']/)
    {
        if ($used_segments->{$1})
        {
            print OF $waybuf;
            print OF;
            $copy = 1;
        }
        else
        {
            $waybuf .= $_;
        }
    }
}

print OF << "EOF";
</osm>
EOF

close (OF);
close (IF);
close (RF) if ($remainsfile);
print STDERR "\n" if $verbose;

exit;
