#!/usr/bin/perl

BEGIN {
    unshift(@INC,"../perl_lib");
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
my $planet_dir='';
my $no_symlink=0;
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

	     'print-filename'  => \$do_print_filename,
	     'no-symlink'      => \$no_symlink,
	     'planet-dir:s'    => \$planet_dir,
	     )
    or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

Geo::OSM::Planet::planet_dir($planet_dir)
    if $planet_dir;

my $new_filename = mirror_planet();

if ( !  $new_filename ) {
    print "ERROR: No new File found\n";
    exit -1;
}

if ( ! $no_symlink ) {
    my $planet_filename =  $new_filename;
    $planet_filename =~ s/(planet)-\d+(\.osm\..+)$/$1$2/;
    if ( -e $planet_filename ) {
	unlink($planet_filename);
    }
    symlink($new_filename,$planet_filename)
	or warn "cannot symlink $new_filename ==> $planet_filename: $@\n";
    
}

if ( $do_print_filename ) {
    print "$new_filename\n";
}



##################################################################
# Usage/manual

__END__

=head1 NAME

B<planet-mirror.pl> Version 0.1

=head1 DESCRIPTION

B<planet-mirror.pl> is a program download the current planet.osm

This program will have a look at http://planet.openstreetmap.org/ 
and see if there is a newer planet-xx.osm.7z File to download.
If ther is it will download it to ~/osm/planet/
After this it will Sanitize the File and write the result to
planet-xx-a.osm.7z
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

=item B<--planet-dir=[path-to-planet-files]>

The ddirectory to put and check the planet Files.
Default is ~/osm/planet/

=item B<--no-symlink>

normally the current planet file is symlinked to 
a file named planet.osm.* in the smae directory.
If this option is set this will not be done.

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
