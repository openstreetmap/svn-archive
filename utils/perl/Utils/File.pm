##################################################################
package Utils::File;
##################################################################

use Exporter;
@ISA = qw( Exporter );
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
@EXPORT = qw( data_open
	      file_needs_re_generation
	      mkdir_if_needed
	      newest_filename
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
# Open Data File in predefined Directories
sub data_open($){
    my $filename = shift;
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
    } else {
	$fh = IO::File->new("$filename",'r')
	    or die("cannot open $filename: $!");
    }
    return $fh;
}

# ------------------------------------------------------------------
# Open Data File in predefined Directories
sub file_needs_re_generation($$){
    my $src_filename = shift;
    my $dst_filename = shift;

    unless ( -e $src_filename ){
	print STDERR "No Update makes sense, since we lack the source File: $src_filename\n" 
	    if $VERBOSE>1;
	return 0;
    }

    # dst file does not exist
    unless ( -e $dst_filename ){
	print STDERR "Update needed. $dst_filename has no size\n" 
	    if $VERBOSE>1;
	return 1;
    }

    my ($src_mtime) = (stat($src_filename))[9] || 0;
    my ($dst_mtime) = (stat($dst_filename))[9] || 0;

    my $update_needed=$src_mtime > $dst_mtime;
    if (  $update_needed ) {
	if ( $VERBOSE>1 ) {
	    print STDERR "Update needed.\n";
	    print STDERR "$dst_filename\t".localtime($dst_mtime)." is older than \n";
	    print STDERR "$src_filename\t".localtime($src_mtime)."\n";
	}
    }
    return $update_needed;
}

# Given a filename it checks if we have an unpacked Version which is newer 
sub newest_filename($){
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
    my $dir = shift;
    -d "$dir" or mkpath "$dir"
        or die "Cannot create Directory $dir: $!\n";
}

1;
