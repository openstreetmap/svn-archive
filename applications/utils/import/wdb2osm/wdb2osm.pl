#!/usr/bin/perl

=pod

=head1 NAME

wdb2osm.pl -- Converts CIA World Databank II datafiles into .osm files
suitable for loading into JOSM

=head1 SYNOPSIS

perl wdb2osm.pl filename

=head1 DESCRIPTION

Converts a WDB-II datafile as downloaded from
http://www.evl.uic.edu/pape/data/WDB/ into .osm files suitable for
loading into JOSM.

The CIA World DataBank II has land outlines, rivers, and political
boundaries collected by the USA government during the 1980's. Make
sure what you are importing hasn't changed substantially since then.

To get the border for a country in Europe, download the European data
file, then run this script and point it to europe-bdy.txt. It will
create a catalog alongside the txt file containing one file for each
segment in the WDB data.

Then load all these files into JOSM (Warning: this can consume quite a
lot of memory...) and use the hide/show layer feature to find the data
for the border you want. Merge all these into a single layer and
delete the other layers. Tag as appropriate and upload.

=head1 BUGS

This script uses neither warnings, strict or -T. A bit of a hack
really.

The data has lots of flaws. Make sure to check it for sanity before
uploading

=head1 AUTHOR

Knut Arne Bjørndal, bob+osm at cakebox dot net, or bobkare@irc

=head1 COPYRIGHT

(c) 2007 Knut Arne Bjørndal. This program is free software; you can
redistribute it and/or modify it under the GNU GPL.

See http://www.gnu.org/licenses/gpl.txt

=cut

# Get the filename from argument
$filename = $ARGV[0];
if($ARGV[0] eq ""){ print "Usage: perl wdb2osm.pl filename\n"; }
print "Opening $filename:\n";

# Create directory alongside source file
$dirName = $filename;
$dirName =~ s/\.txt//g;
mkdir "$dirName";

## Open file
open( FILE, "< $filename" ) or die "Can't open $filename: $!\n";

$j=0;
## foreach line in the file (read line)
while( my $line = <FILE> ) {
    # Remove tabs
    $line =~ s/\t//g;
    
    # Split line on spaces
    @lineSplit = split(" ",$line);

    if(@lineSplit[0] eq "segment"){
	# Output way of all segments if we have added any
	if( $#nodes ){
	    print OUT "<way id='-1'>\n";
	    foreach(@nodes){
		print OUT "  <nd ref='$_' />\n";
	    }
	    print OUT "</way>\n";
	}
	
	# Add the closing "</osm>" and close the file
	print OUT "</osm>";
	close(OUT);
	
	# Get the remaining "special elements"
	$segmentNumber = @lineSplit[1];
	$rankNumber = @lineSplit[3];
	$points = @lineSplit[5];
	
	# name the file according to the segment number
	$newFile = "segment$segmentNumber";
	# open the file for writing
	$outfile = "$dirName/$newFile.osm";
	print "Opening $outfile\n";
	open OUT, "> $outfile" or die "Can't open $outfile : $!";

	# Initialize IDs.
	# We start nodes at -1 and segments at -2 then alternate as we
	# go along.
	# The first node will actually be -3 and the first segment
	# -5. The way get ID -1 This makes sure we get unique IDs for
	# every element.
	$nodeid = -1; $segmentid = -2;

	# @segments = ();
	@nodes = ();

	# Output header
	print OUT "<?xml version='1.0' encoding='UTF-8'?>\n";
	print OUT "<osm version='0.5' generator='wdb2osm.pl'>\n";
    } elsif( @lineSplit[0] ne "segment" && @lineSplit[0] ne "" ){
	$nodeid-=2;
	print OUT "<node id=\"$nodeid\" lat=\"@lineSplit[0]\" lon=\"@lineSplit[1]\"/>\n";
	push(@nodes, $nodeid);
    }
}

# Output the way with all the segments, then close the file
if( $#segments ){
    print OUT "<way id='-1'>\n";
    foreach(@nodes){
	print OUT "  <nd ref='$_' />\n";
    }
    print OUT "</way>\n";
}
print OUT "</osm>";
close(OUT);
