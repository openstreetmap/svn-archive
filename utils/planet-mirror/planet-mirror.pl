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
my $osm_file;

# Set defaults and get options from command line
Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug:+'              => \$DEBUG,      
	     'd:+'                  => \$DEBUG,      
	     'verbose:+'            => \$VERBOSE,
	     'v:+'                  => \$VERBOSE,
	     'MAN'                  => \$man, 
	     'man'                  => \$man, 
	     'h|help|x'             => \$help, 
	     'osm=s'                => \$osm_file,
	     'proxy=s'              => \$PROXY,
	     'no-mirror=s'              => \$NO_MIRROR,
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

if ( ! -s $osm_file ) {
    $osm_file = mirror_planet();
};


##################################################################
# Usage/manual

__END__

=head1 NAME

B<planet-mirror.pl> Version 0.01

=head1 DESCRIPTION

B<planet-mirror.pl> is a program download the currentplanet.osm

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
