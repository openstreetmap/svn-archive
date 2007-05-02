#!/usr/bin/perl

BEGIN {
    unshift(@INC,"../perl_lib");
    unshift(@INC,"~/svn.openstreetmap.org/applications/utils/perl_lib");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/applications/utils/perl_lib");
}


use strict;
use warnings;

use Data::Dumper;
use Getopt::Long;

use Geo::OSM::Planet;
use Utils::Debug;
use Utils::LWP::Utils;
use Pod::Usage;

my ($man,$help);

use strict;
use warnings;

my $do_print_filename=0;
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

	     'print-filename'     => \$do_print_filename,
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

my $filename = mirror_planet();
if ( $do_print_filename ) {
    print "$filename\n";
}



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

planet-mirror.pl -v 

 Download the most current planet-xxxxxx.osm.bz and tell what's going on

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


=item B<--print-filename>

print the filename of the mirrored osm file


=item B<-v>

Print out what the programm is doing.

If used twice (-v -v ) you get status updates even while 
downloading the File.

=back


=head1 COPYRIGHT

Copyright 2006, Jörg Ostertag

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=head1 AUTHOR

Jörg Ostertag (planet-count-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
