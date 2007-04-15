#!/usr/bin/perl
# This Script filters OSM Files so they can be processed by grep, ..
#
# Joerg Ostertag <openstreetmap@ostertag.name>

use strict;
use warnings;

my ($verbose,$debug);

use Data::Dumper;

##################################################################
package File;
##################################################################

use IO::File;

# -----------------------------------------------------------------------------
# Open Data File in predefined Directories
sub data_open($){
    my $file_name = shift;

    my $file_with_path="$file_name";
    my $size = (-s $file_with_path)||0;
    if ( $size < 270 ) {
	warn "cannot Open $file_name ($size) Bytes is too small)\n"
	    if $verbose || $debug;
	return 0;
    }

    print "Opening $file_with_path" if $debug;
    my $fh;
    if ( $file_with_path =~ m/\.gz$/ ) {
	$fh = IO::File->new("gzip -dc $file_with_path|")
	    or die("cannot open $file_with_path: $!");
    } elsif ( $file_with_path =~ m/\.bz2$/ ) {
	    $fh = IO::File->new("bzip2 -dc $file_with_path|")
		or die("cannot open $file_with_path: $!");
	} else {
	    $fh = IO::File->new("<$file_with_path")
		or die("cannot open $file_with_path: $!");
	}
    return $fh;
}

##################################################################
package Main;
##################################################################
use IO::File;

if ( @ARGV < 1 ){
    print "Need Filename(s) to convert\n";
    exit 1;
}

my $start_time=time();
    
my $count=0;
while ( my $file_name = shift @ARGV ) {
    my $out_file_name=$file_name;

    # Only process .osm Files for now
    next unless ( $out_file_name=~s/\.osm/-single-line.osm/);

    $count ++;

    print "Converting $file_name --> $out_file_name\n";
    my $fh = File::data_open($file_name);
    my $fo = IO::File->new(">$out_file_name");
    my $mode ='';
    my $line ='';
    while ( my $new_line = $fh->getline() ) {
	chomp $new_line;
	$line .= $new_line;
	if ( $new_line =~m/^\s*<(\D+) id=/ ) {
	    $mode = $1 unless $1 eq "seg";
	}
	if ( $mode eq "node" ) {
	    if ( ( $new_line =~ m,</node>, ) ||
		 ( $new_line =~ m,<node [^>]*/>, ) ) {
		print $fo "$line\n";
		#print "$mode: $line\n";
		$line ='';
	    }
	} elsif ( $mode eq "segment" ) {
	    if ( ( $new_line =~ m,</segment>, ) ||
		 ( $new_line =~ m,<segment [^>]*/>, ) ) {
		print $fo "$line\n";
		#print "$mode: $line\n";
		$line ='';
	    }
	} elsif ( $mode eq "way" ) {
	    if ( ( $new_line =~ m,</way>, ) ||
		 ( $new_line =~ m,<way [^>]*/>, ) ) {
		print $fo "$line\n";
		#print "$mode: $line\n";
		$line ='';
	    }
	} else {
	    #print "$mode: $line\n";
	    print $fo "$line\n";
	    $line ='';
	}	    
    }
    print $fo "$line\n";
    $fo->close();
    $fh->close();
}
if ( $verbose) {
    printf "Converting $count  OSM Files in  %.0f sec\n",time()-$start_time;
}
