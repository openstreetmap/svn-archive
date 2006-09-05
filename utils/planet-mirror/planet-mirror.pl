#!/usr/bin/perl

BEGIN {
    unshift(@INC,"../perl");
    unshift(@INC,"~/svn.openstreetmap.org/utils/perl");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/utils/perl");
}


use strict;
use warnings;

use Data::Dumper;
use Getopt::Long;

use Geo::OSM::Planet;
use Utils::Debug;
use Utils::LWP::Utils;

my ($man,$help);

use strict;
use warnings;
    
# ------------------------------------------------------------------
# Set defaults and get options from command line
Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug:+'    => \$DEBUG,      
	     'd:+'        => \$DEBUG,      
	     'verbose:+'  => \$VERBOSE,
	     'v:+'        => \$VERBOSE,
	     'MAN'        => \$man, 
	     'man'        => \$man, 
	     'h|help|x'   => \$help, 

	     'no-mirror'  => \$Utils::LWP::Utils::NO_MIRROR,
	     'proxy=s'    => \$Utils::LWP::Utils::PROXY,

	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

mirror_planet();



##################################################################
# Usage/manual

__END__

=head1 NAME

B<planet-mirror.pl> Version 0.1

=head1 DESCRIPTION

B<planet-mirror.pl> is a program download the current planet.osm

This program will have a look at http://planet.openstreetmap.org/ 
and see if there is a newer planet-xx.osm.bz2 File to download.
If ther is it will download it to ~/osm/planet/
After this it will Sanitize the File and write the result to
planet-xx-a.osm.bz2
If you want your planet File to always be up to date you can i
add the following line to your crontab
 01 9 * * * /home/<yourname>/svn.openstreetmap.org/utils/planet-mirror/planet-mirror.pl

=head1 SYNOPSIS

B<Common usages:>

planet-mirror.pl [--man] [-d] [-v] [-h] [--no-proxy] [--no-mirror]

=head1 OPTIONS

=over 2

=item B<--man> Complete documentation

This shows the Complete documentation


=item B<--no-mirror>

Do not try mirroring the files from the original Server. Only use
files found on local Filesystem.


=item B<--proxy>

use proxy for download

=item B<--osm-file=path/planet.osm>

Select the "path/planet.osm" file to use for the checks



=back
