##################################################################
package Utils::File;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( data_open
	      file_needs_re_generation
	      mkdir_if_needed
	      newest_unpacked_filename
	      expand_filename
	      );
use strict;
use warnings;

use IO::File;
use Utils::Debug;

use File::Basename;
use File::Copy;
use File::Path;
use Time::Local;

# -----------------------------------------------------------------------------
# Expand ~/ against Homedir of user
sub expand_filename($){
    my $filename = shift;
    $filename =~ s/^\~/$ENV{HOME}/;
    return $filename;
}

# -----------------------------------------------------------------------------
# Open Data File in predefined Directories
sub data_open($){
    my $filename = expand_filename(shift);
    my $fh;

    # If it's already an open File
    if ( ref($filename) =~ m/IO::File/ ) {
	return $filename;
    }

    if ( $filename eq "-" ) {
	$fh = IO::File->new('<&STDIN');
	$fh or  die("cannot open $filename: $!");
	return $fh;
    } else {
        # Note: This test is wrong for pipes...
	my $size = (-s $filename)||0;
	if ( $size < 270 ) {
	    warn "cannot Open $filename ($size) Bytes is too small)\n"
		if $VERBOSE || $DEBUG;
	    return undef;
	}
    }

    printf STDERR "Opening $filename\n" if $DEBUG;
    if ( $filename =~ m/\.gz$/ ) {
	$fh = IO::File->new("gzip -dc $filename|")
	    or die("cannot open $filename: $!");
    } elsif ( $filename =~ m/\.bz2$/ ) {
	$fh = IO::File->new("bzip2 -dc $filename|")
	    or die("cannot open $filename: $!");
    } elsif ( $filename =~ m/\.7z$/ ) {
	printf STDERR "Opening $filename with 7z\n" if $DEBUG;
	$fh = IO::File->new("7z e  -so $filename |")
	    or die("cannot open $filename: $!");
    } else {
	$fh = IO::File->new("$filename",'r')
	    or die("cannot open $filename: $!");
    }
    return $fh;
}

# ------------------------------------------------------------------
# Open Data File in predefined Directories
sub file_needs_re_generation($$){
    my $src_filename = expand_filename(shift);
    my $dst_filename = expand_filename(shift);

    unless ( -e $src_filename ){
	print STDERR "No Update makes sense, since we lack the source File: $src_filename\n" 
	    if $VERBOSE>1;
	return 0;
    }

    # dst file does not exist
    unless ( $dst_filename && -e $dst_filename ){
	print STDERR "Update needed. $dst_filename has no size\n" 
	    if $VERBOSE>1;
	return 1;
    }

    my ($src_mtime) = (stat($src_filename))[9] || 0;
    my ($dst_mtime) = (stat($dst_filename))[9] || 0;

    my $update_needed=$src_mtime > $dst_mtime;
    if ( $VERBOSE>5 ) {
	print STDERR "Update needed.\n";
	print STDERR localtime($dst_mtime)."\t$dst_filename is ";
	print STDERR ($update_needed?"older":"newer")." than \n";
	print STDERR localtime($src_mtime)."\t$src_filename\n";
    }
    return $update_needed;
}

# ------------------------------------------------------------------
# Given a filename it checks if we have an unpacked
# Version which is new enough
# ARGS: filename.osm.gz|filename.osm
# RETURNS:
#   filename.osm if: it exists and is the newest
#   filename.osm.gz: if no current filename.osm exists
#   undef:           if we cant find any of the files
sub newest_unpacked_filename($){
    my $filename = shift;

    my $filename_unpacked = $filename;
    $filename_unpacked =~ s/\.(gz|bz2|bz)$//;
    if ( file_needs_re_generation($filename,$filename_unpacked)) {
	return $filename if -s $filename;
    } else {
	return $filename_unpacked if -s $filename_unpacked;
    }
    return undef;
}

# ------------------------------------------------------------------
# Create Directory if needed and die if not possible
sub mkdir_if_needed($){
    my $dir = expand_filename(shift);
    -d "$dir" or mkpath "$dir"
        or die "Cannot create Directory $dir: $!\n";
}

1;

__END__

=head1 NAME

File.pm

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
