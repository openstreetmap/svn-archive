#
# 
# PERL osm module by gary68
#
##########################
# DON'T EDIT UNDER WINDOWS
##########################
#
# !!! store as osm.pm in folder OSM in lib directory !!!
#
# This module contains a lot of useful functions for working with osm files and data. it also
# includes functions for calculation and output.
#
#
# Copyright (C) 2008, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#
#
# version 2
# - added html table functions
#
# Version 3
# - added project and angle
# - added support for bz2 files
#
# Version 4
# - add support for relations
# - select multiple ways in JOSM link
# - getNode2, getWay2: return tags as arrays
#
# Version 4.1
# - getBugs added
# 
# Version 4.2
# - map compare link added
# 
# Version 4.3
# - regex for k/v changed
# 
# Version 4.4
# -added relation analyzer link
#
# Version 4.41 (gary68)
# - changed regex for openosmfile from /node/ to /<node/ - seems better since changesets are now in planet...
#
# Version 4.5 (gary68)
# - OSB address changed
#
# Version 4.6 (gary68)
# - getnode2 error correction
#
# Version 4.7 (gary68)
# - hidden iframe for josm links
#
# Version 4.8
# - josm dont select added
#
# Version 4.9
# - APIgetWay new
#
# Version 5.0
# - new osm link function
#
# Version 5.1
# - new hash function
#


#
# USAGE
#
# analyzerLink ($id) 					> $htmlString, link to relation analyzer
# angle (x1,y1,x2,y2)					> angle (N=0,E=90...)
# APIgetWay ($id)					> ($wayId, $wayUser, \@wayNodes, \@wayTags)
# binSearch ($value, @ref)				> $index or -1
# closeOsmFile ()
# checkOverlap (w1xMin, w1yMin, w1xMax, w1yMax, w2xMin, w2yMin, w2xMax, w2yMax)   > 0=no overlap, 1=overlap
# crossing (g1x1,g1y1,g1x2,g1y2,g2x1,g2y1,g2x2,g2y2) 	> ($sx, $sy) 
# distance (x1,y1,x2,y2) 				> $distance in km
# getBugs ($lon, $lat, $bugsDownDist, $bugsMaxDist)	> pos, down dist in deg, max dist in km -> html text
# getNode ()						> ($gId, $gLon, $gLat, $gU, \@gTags) ; # in main @array = @$ref
# getNode2 ()						> ($gId, $gLon, $gLat, $gU, \@gTags) ; # in main @array = @$ref // returns k/v as array, not string!
# getRelation 
# getWay ()						> ($gId, $gU, \@gNodes, \@gTags) ; # in main @array = @$ref
# getWay2 ()						> ($gId, $gU, \@gNodes, \@gTags) ; # in main @array = @$ref // returns k/v as array, not string!
# hashValue ($lon, $lat)				> $hashValue 0.1 deg
# hashValue2 ($lon, $lat)				> $hashValue 0.01 deg
# historyLink ($type, $key) 				> $htmlString
# josmLinkDontSelect ($lon, $lat, $span)		> $htmlString
# josmLinkSelectWay ($lon, $lat, $span, $wayId)		> $htmlString
# josmLinkSelectWays ($lon, $lat, $span, @wayIds)	> $htmlString
# josmLinkSelectNode ($lon, $lat, $span, $nodeId)	> $htmlString
# DON'T USE ANYMORE! josmLink ($lon, $lat, $span, $wayId)	> $htmlString
# mapCompareLink ($lon, $lat, $zoom)			> $htmlString
# openOsmFile ($file)					> osm file open and $line set to first node (*.osm or *.osm.bz2)
# osbLink ($lon, $lat, $zoom) 					> $htmlString
# osmLink ($lon, $lat, $zoom) 					> $htmlString
# osmLinkMarkerWay ($lon, $lat, $zoom, $way)		> $htmlString
# picLinkMapnik ($lon, $lat, $zoom)			> $htmlString
# picLinkOsmarender ($lon, $lat, $zoom)			> $htmlString
# printGPXHeader ($file)
# printGPXFoot ($file) 
# printGPXWaypoint ($file, $lon, $lat, $text) 
# printHTMLCellCenter ($file, $value)
# printHTMLCellLeft ($file, $value)
# printHTMLCellRight ($file, $value)
# printHTMLFoot ($file) 				> print foot to file
# printHTMLHeader ($file, $title) 			> print header to file
# printHTMLHeaderiFrame ($file)				> print iFrame code for josm links, call before body
# printHTMLRowStart ($file)
# printHTMLRowEnd ($file)
# printHTMLTableFoot ($file)
# printHTMLTableHead ($file)
# printHTMLTableHeadings ($file, @list)
# printHTMLTableRowLeft ($file, @list)
# printHTMLTableRowRight ($file, @list)
# printNodeList ($file, @list) 
# printProgress ($program, $osm, $startTime, $fullCount, $actualCount) 
# printWayList ($file, @list) 
# project (x1, y1, angle, dist)				> (x2,y2)
# shortestDistance ($gx1, $gy1, $gx2, $gy2, $nx, $ny)	> roughly the distance of node to segment in km
# skipNodes ()
# skipWays ()
# stringFileInfo ($file)				> $string
# stringTimeSpent ($timeSpent in seconds) 		> $string
# tileNumber ($lat,$lon,$zoom) 				> ($xTile, $yTile)
#




package OSM::osm ; 

use strict;
use warnings;

use LWP::Simple;
use LWP::Simple;
use Math::Trig;
use File::stat;
use Time::localtime;
use List::Util qw[min max] ;
use Compress::Bzip2 ;		# install packet "libcompress-bzip2-perl"
					# if you have problems with this module/library then just comment out all lines using these functions 
					# and don't use zipped files

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK) ;

$VERSION = '5.1' ; 

my $apiUrl = "http://www.openstreetmap.org/api/0.6/" ; # way/Id

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (analyzerLink getBugs getNode getNode2 getWay getWay2 getRelation crossing historyLink hashValue hashValue2 tileNumber openOsmFile osmLink osmLinkMarkerWay osbLink mapCompareLink josmLink josmLinkDontSelect josmLinkSelectWay josmLinkSelectWays josmLinkSelectNode printHTMLHeader printHTMLFoot stringTimeSpent distance angle project picLinkMapnik picLinkOsmarender stringFileInfo closeOsmFile skipNodes skipWays binSearch printProgress printNodeList printWayList printGPXHeader printGPXFoot printGPXWaypoint checkOverlap shortestDistance printHTMLTableHead printHTMLTableFoot printHTMLTableHeadings printHTMLTableRowLeft printHTMLTableRowRight printHTMLCellLeft  printHTMLCellCenter printHTMLCellRight printHTMLRowStart printHTMLRowEnd printHTMLiFrameHeader APIgetWay) ;

our $line ; 
our $file ; 
our $fileName ;

my $bz ; my $isBz2 ;

######
# file
######
sub openOsmFile {
	$fileName = shift ;

	if (grep /.bz2/, $fileName) { $isBz2 = 1 ; } else { $isBz2 = 0 ; }

	if ($isBz2) {
		$bz = bzopen($fileName, "rb") or die "Cannot open $fileName: $bzerrno\n" ;
	}
	else {
		open ($file, "<", $fileName) || die "can't open osm file" ;
	}

	nextLine() ;		
	while ( ! (grep /\<node/, $line) ) {
		nextLine() ;
		#print "LINE: $line" ;
	}
	return 1 ;
}

sub closeOsmFile {
	if ($isBz2) {
		$bz->bzclose() ;
	}
	else {
		close ($file) ;
	}
}

sub stringFileInfo {
	my $file = shift ;
	my $string = "file " . $file . " " . ctime(stat($file)->mtime) ;
	return ($string) ;
}

sub nextLine {
	if ($isBz2) {
		$bz->bzreadline($line) ;
	}
	else {
		$line = <$file> ;
	}
}


#######
# NODES
#######
sub skipNodes {
	while ( ! (grep /<way/, $line) ) {
		nextLine() ;		
	}
}



sub getNode {
	my $gId ;
	my $gLon ;
	my $gLat ;
	my $gU ;
	my @gTags = () ;
	if($line =~ /^\s*\<node/) {

		my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/);	# get node id
		my ($lon) = ($line =~ /^.+lon=[\'\"]([-\d,\.]+)[\'\"]/);	# get position
		my ($lat) = ($line =~ /^.+lat=[\'\"]([-\d,\.]+)[\'\"]/);	# get position

		#my ($u) = ($line =~ /^.+user=[\'\"]([-\w\d\s]+)[\'\"]/);	# get value 
		my ($u) = ($line =~ /^.+user=[\'\"](.+)[\'\"]/);	# get value 

		if (!$u) {
			$u = "unknown" ;
		}

		if (!$id or (! (defined ($lat))) or ( ! (defined ($lon))) ) {
			print "WARNING reading osm file, line follows (expecting id, lon, lat and user for node):\n", $line, "\n" ; 
		}

		unless ($id) { next; }
		if  (! (defined ($lat))) { next; }
		if  (! (defined ($lon))) { next; }
		if ( (grep (/">/, $line)) or (grep (/'>/, $line)) ) {                  # more lines, get tags
			nextLine() ;
			while (!grep(/<\/node>/, $line)) {

				#my ($k) = ($line =~ /^\s*\<tag k=[\'\"]([-\w\d\s\.\,\;\:]+)[\'\"]/);   # get key
				#my ($v) = ($line =~ /v=[\'\"]([-\w\d\s\.\,\;\:äöüÄÖÜß\/\(\)\+\&]+)[\'\"]/) ;
				#my ($k) = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]/);   # get key
				#my ($v) = ($line =~ /v=[\'\"](.+)[\'\"]/) ;
				my ($k, $v) = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;

				#print "line = $line" ;
				#print "key  = $k\n" ;
				#print "val  = $v\n\n" ;

				if ( (defined ($k)) and (defined ($v)) ) {
					my $tag = $k . ":" . $v ;
					push @gTags, $tag ;
				}
				else {
					#print "WARNING tag not recognized: ", $line, "\n" ;
				}
				nextLine() ;
			}
			nextLine() ;
		}
		else {
			nextLine() ;
		}
		$gId = $id ;
		$gLon = $lon ;
		$gLat = $lat ;
		$gU = $u ;
	} # node
	else {
		return (-1, -1, -1, -1, -1) ; 
	} # node
	#print "$gId $gLon $gLat $gU\n" ; 
	return ($gId, $gLon, $gLat, $gU, \@gTags) ; # in main @array = @$ref
} # getNode

sub getNode2 {
	my $gId ;
	my $gLon ;
	my $gLat ;
	my $gU ;
	my @gTags = () ;
	if($line =~ /^\s*\<node/) {

		my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/);	# get node id
		my ($lon) = ($line =~ /^.+lon=[\'\"]([-\d,\.]+)[\'\"]/);	# get position
		my ($lat) = ($line =~ /^.+lat=[\'\"]([-\d,\.]+)[\'\"]/);	# get position

		#my ($u) = ($line =~ /^.+user=[\'\"]([-\w\d\s]+)[\'\"]/);	# get value 
		my ($u) = ($line =~ /^.+user=[\'\"](.+)[\'\"]/);	# get value 

		if (!$u) {
			$u = "unknown" ;
		}

		if (!$id or (! (defined ($lat))) or ( ! (defined ($lon))) ) {
			print "WARNING reading osm file, line follows (expecting id, lon, lat and user for node):\n", $line, "\n" ; 
		}
		else {
			if ( (grep (/">/, $line)) or (grep (/'>/, $line)) ) {                  # more lines, get tags
				nextLine() ;
				while (!grep(/<\/node>/, $line)) {
	
					my ($k, $v) = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
	
					if ( (defined ($k)) and (defined ($v)) ) {
						my $tag = [$k, $v] ;
						push @gTags, $tag ;
					}
					else {
						#print "WARNING tag not recognized: ", $line, "\n" ;
					}
					nextLine() ;
				}
				nextLine() ;
			}
			else {
				nextLine() ;
			}

		}

		$gId = $id ;
		$gLon = $lon ;
		$gLat = $lat ;
		$gU = $u ;
	} # node
	else {
		return (-1, -1, -1, -1, -1) ; 
	} # node
	#print "$gId $gLon $gLat $gU\n" ; 
	return ($gId, $gLon, $gLat, $gU, \@gTags) ; # in main @array = @$ref
} # getNode2


######
# WAYS
######

sub skipWays {
	while ( ! (grep /<relation/, $line) ) {
		nextLine() ;		
	}
}


sub getWay {
	my $gId ;
	my $gU ;
	my @gTags ;
	my @gNodes ;
	if($line =~ /^\s*\<way/) {
		my ($id)   = ($line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/); # get way id
		my ($u) = ($line =~ /^.+user=[\'\"](.*)[\'\"]/);       # get value // REGEX???
		if (!$u) {
			$u = "unknown" ;
		}
		if (!$id) {
			print "ERROR reading osm file, line follows (expecting way id):\n", $line, "\n" ; 
		}
		unless ($id) { next; }


		nextLine() ;
		while (not($line =~ /\/way>/)) { # more way data
			#get nodes and type
			my ($node) = ($line =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/); # get node id

			#my ($k)   = ($line =~ /^\s*\<tag k=[\'\"]([-\w\d\s\.\,\:]+)[\'\"]/); # get key
			#my ($v) = ($line =~ /v=[\'\"]([-\w\d\s\.\,\;\:äöüÄÖÜß\/\(\)\+\&]+)[\'\"]/) ;
			#my ($k)   = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]/); # get key
			#my ($v) = ($line =~ /v=[\'\"](.+)[\'\"]/) ;
			my ($k, $v) = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;

			if (!(($node) or ($k and defined($v) ))) {
				#print "WARNING tag not recognized", $line, "\n" ; 
			}
		
			if ($node) {
				push @gNodes, $node ;
			}

			#get tags 
			if ($k and defined($v)) {
				my $tag = $k . ":" . $v ;
				push @gTags, $tag ;
			}
			nextLine() ;
		}
		nextLine() ;
		$gId = $id ;
		$gU = $u ;
	}
	else {
		return (-1, -1, -1, -1) ;
	}
	return ($gId, $gU, \@gNodes, \@gTags) ;
} # way

sub getWay2 {
	my $gId ;
	my $gU ;
	my @gTags ;
	my @gNodes ;
	if($line =~ /^\s*\<way/) {
		my ($id)   = ($line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/); # get way id
		my ($u) = ($line =~ /^.+user=[\'\"](.*)[\'\"]/);       # get value // REGEX???
		if (!$u) {
			$u = "unknown" ;
		}
		if (!$id) {
			print "ERROR reading osm file, line follows (expecting way id):\n", $line, "\n" ; 
		}
		unless ($id) { next; }


		nextLine() ;
		while (not($line =~ /\/way>/)) { # more way data
			#get nodes and type
			my ($node) = ($line =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/); # get node id

			#my ($k)   = ($line =~ /^\s*\<tag k=[\'\"]([-\w\d\s\.\,\:]+)[\'\"]/); # get key
			#my ($v) = ($line =~ /v=[\'\"]([-\w\d\s\.\,\;\:äöüÄÖÜß\/\(\)\+\&]+)[\'\"]/) ;
			#my ($k)   = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]/); # get key
			#my ($v) = ($line =~ /v=[\'\"](.+)[\'\"]/) ;
			my ($k, $v) = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;

			if (!(($node) or ($k and defined($v) ))) {
				#print "WARNING tag not recognized", $line, "\n" ; 
			}
		
			if ($node) {
				push @gNodes, $node ;
			}

			#get tags 
			if ($k and defined($v)) {
				my $tag = [$k, $v] ;
				push @gTags, $tag ;
			}
			nextLine() ;
		}
		nextLine() ;
		$gId = $id ;
		$gU = $u ;
	}
	else {
		return (-1, -1, -1, -1) ;
	}
	return ($gId, $gU, \@gNodes, \@gTags) ;
} # getWay2


###########
# RELATIONS
###########

sub getRelation {
	my $gId ;
	my $gU ;
	my @gMembers = () ;
	my @gTags = () ;

	if ($line =~ /^\s*\<relation/) {

		my ($id)   = ($line =~ /^\s*\<relation id=[\'\"](\d+)[\'\"]/); # get rel id
		my ($u) = ($line =~ /^.+user=[\'\"](.*)[\'\"]/);     
		if (!$u) {
			$u = "unknown" ;
		}
		if (!$id) {
			print "ERROR reading osm file, line follows (expecting relation id):\n", $line, "\n" ; 
		}
		unless ($id) { next ; }

		nextLine() ;
		while (not($line =~ /\/relation>/)) { # more data
			if ($line =~ /<member/) {
				#print "PM line: $line\n" ;
				my ($memberType)   = ($line =~ /^\s*\<member type=[\'\"]([\w]*)[\'\"]/); 
				my ($memberRef) = ($line =~ /^.+ref=[\'\"](\d*)[\'\"]/);       
				my ($memberRole) = ($line =~ /^.+role=[\'\"](.*)[\'\"]/);
				if (!$memberRole) { $memberRole = "none" ; }
				my @member = [$memberType, $memberRef, $memberRole] ;
				#print "PM: $memberType # $memberRef # $memberRole\n" ;
				push @gMembers, @member ;
			}
			if ($line =~ /<tag/) {

				#my ($k)   = ($line =~ /^\s*\<tag k=[\'\"]([-\w\d\s\.\,\:]+)[\'\"]/); # get key
				#my ($v) = ($line =~ /v=[\'\"]([-\w\d\s\.\,\;\:äöüÄÖÜß\/\(\)\+\&]+)[\'\"]/) ;
				#my ($k)   = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]/); # get key
				#my ($v) = ($line =~ /v=[\'\"](.+)[\'\"]/) ;
				my ($k, $v) = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;

				if (!(($k and defined($v) ))) {
					#print "WARNING tag not recognized", $line, "\n" ; 
					$k = "unknown" ; $v = "unknown" ;
				}
				my $tag = [$k, $v] ;
				push @gTags, $tag ;
			}
			nextLine() ;
		}
		nextLine() ;

		$gId = $id ;
		$gU = $u ;
	}
	else {
		return (-1, -1, -1, -1) ;
	}
	return ($gId, $gU, \@gMembers, \@gTags) ;
}


###########
# CROSSINGS
###########

# crossing
sub crossing {

	my ($g1x1) = shift ;
	my ($g1y1) = shift ;
	my ($g1x2) = shift ;
	my ($g1y2) = shift ;
	
	my ($g2x1) = shift ;
	my ($g2y1) = shift ;
	my ($g2x2) = shift ;
	my ($g2y2) = shift ;

	#printf "g1: %f/%f   %f/%f\n", $g1x1, $g1y1, $g1x2, $g1y2 ;
	#printf "g2: %f/%f   %f/%f\n", $g2x1, $g2y1, $g2x2, $g2y2 ;



	# wenn punkte gleich, dann 0 !!!
	# nur geraden pr fen, wenn node ids ungleich !!!

	if (($g1x1 == $g2x1) and ($g1y1 == $g2y1)) { # p1 = p1 ?
		#print "gleicher punkt\n" ;
		return (0, 0) ;
	}
	if (($g1x1 == $g2x2) and ($g1y1 == $g2y2)) { # p1 = p2 ?
		#print "gleicher punkt\n" ;
		return (0, 0) ;
	}
	if (($g1x2 == $g2x1) and ($g1y2 == $g2y1)) { # p2 = p1 ?
		#print "gleicher punkt\n" ;
		return (0, 0) ;
	}

	if (($g1x2 == $g2x2) and ($g1y2 == $g2y2)) { # p2 = p1 ?
		#print "gleicher punkt\n" ;
		return (0, 0) ;
	}


	my $g1m ;
	if ( ($g1x2-$g1x1) != 0 )  {
		$g1m = ($g1y2-$g1y1)/($g1x2-$g1x1) ; # steigungen
	}
	else {
		$g1m = 999999 ;
	}

	my $g2m ;
	if ( ($g2x2-$g2x1) != 0 ) {
		$g2m = ($g2y2-$g2y1)/($g2x2-$g2x1) ;
	}
	else {
		$g2m = 999999 ;
	}

	#printf "Steigungen: m1=%f m2=%f\n", $g1m, $g2m ;

	if ($g1m == $g2m) {   # parallel
		#print "parallel\n" ;
		return (0, 0) ;
	}

	my ($g1b) = $g1y1 - $g1m * $g1x1 ; # abschnitte
	my ($g2b) = $g2y1 - $g2m * $g2x1 ;

	#printf "b1=%f b2=%f\n", $g1b, $g2b ;

	
	# wenn punkt auf gerade, dann 1 - DELTA Pr fung !!! delta?


	my ($sx) = ($g2b-$g1b) / ($g1m-$g2m) ;             # schnittpunkt
	my ($sy) = ($g1m*$g2b - $g2m*$g1b) / ($g1m-$g2m);

	#print "schnitt: ", $sx, "/", $sy, "\n"	;

	my ($g1xmax) = max ($g1x1, $g1x2) ;
	my ($g1xmin) = min ($g1x1, $g1x2) ;	
	my ($g1ymax) = max ($g1y1, $g1y2) ;	
	my ($g1ymin) = min ($g1y1, $g1y2) ;	

	my ($g2xmax) = max ($g2x1, $g2x2) ;
	my ($g2xmin) = min ($g2x1, $g2x2) ;	
	my ($g2ymax) = max ($g2y1, $g2y2) ;	
	my ($g2ymin) = min ($g2y1, $g2y2) ;	

	if 	(($sx >= $g1xmin) and
		($sx >= $g2xmin) and
		($sx <= $g1xmax) and
		($sx <= $g2xmax) and
		($sy >= $g1ymin) and
		($sy >= $g2ymin) and
		($sy <= $g1ymax) and
		($sy <= $g2ymax)) {
		#print "*******IN*********\n" ;
		return ($sx, $sy) ;
	}
	else {
		#print "OUT\n" ;
		return (0, 0) ;
	}

} # crossing



####################
# string linkHistory
####################
sub historyLink {
	my ($type, $key) = @_;
	return "<a href=\"http://www.openstreetmap.org/browse/$type/$key/history\">$key</a>";
}



##############
# TILE NUMBERS
##############
sub tileNumber {
  my ($lon,$lat,$zoom) = @_;
  my $xtile = int( ($lon+180)/360 *2**$zoom ) ;
  my $ytile = int( (1 - log(tan($lat*pi/180) + sec($lat*pi/180))/pi)/2 *2**$zoom ) ;
  return(($xtile, $ytile));
}


############
# hashValues
############
sub hashValue {
	my $lon = shift ;
	my $lat = shift ;

	my $lo = int ($lon*10) * 10000 ;
	my $la = int ($lat*10) ;

	return ($lo+$la) ;
}

sub hashValue2 {
	my $lon = shift ;
	my $lat = shift ;

	my $lo = int ($lon*100) * 100000 ;
	my $la = int ($lat*100) ;

	return ($lo+$la) ;
}


######
# calc
######
sub angle {
#
# angle from point 1 to point 2
# N = 0, O = 90, S = 180, W = 270
#
    my ($x1, $y1, $x2, $y2) = @_ ;

    my $d_lat = ($y2-$y1) * 111.11 ;
    my $d_lon = ($x2-$x1) * cos($y1/360*3.14*2) * 111.11 ;
    my $a = - rad2deg(atan2($d_lat,$d_lon)) + 90 ;

    if ($a < 0) { $a += 360 ; }

    return $a ;
}

sub project {
#
# project point from point by angle and distance in km
# N = 0, O = 90, S = 180, W = 270
#
#
	my ($x1, $y1, $angle, $dist) = @_ ;
	my $x2; my $y2 ;
	my $dLat ; my $dLon ;

	$dLat = $dist * cos ($angle/360*3.141592654*2) ; 
	$dLon = $dist * sin ($angle/360*3.141592654*2) ; 

	$x2 = $x1 + $dLon / (111.11 * cos($y1/360*3.14*2) ) ;
	$y2 = $y1 + $dLat / 111.11 ;

	return ($x2, $y2) ;
}

sub distance {
	my ($x1, $y1, $x2, $y2) = @_ ;
	my ($d_lat) = ($y2 - $y1) * 111.11 ;
	my ($d_lon) = ($x2 - $x1) * cos ( $y1 / 360 * 3.14 * 2 ) * 111.11;
	my ($dist) = sqrt ($d_lat*$d_lat+$d_lon*$d_lon);
	return ($dist) ;
}

sub shortestDistance {
	#
	# distance in km ONLY ROUGHLY !!! TODO
	# better calc point on line first and then calc distance with function above!
	#
	my ($gx1, $gy1, $gx2, $gy2, $nx, $ny) = @_ ;
	my $m ; my $b ; my $t ;

	$t = $gx2 - $gx1 ;
	if ($t == 0) {
		my ($d1) = distance ($gx1, $gy1, $nx, $ny) ;
		my ($d2) = distance ($gx2, $gy2, $nx, $ny) ; 
		my ($d3) = distance ($gx1, $gy1, $gx2, $gy2) ;
		my ($d4) = abs ($nx - $gx1) * 111.11 * cos ( $gy1 / 360 * 3.14 * 2 ) ;
		if ( ($d1 <= $d3) and ($d2 <= $d3) ) {
			return (abs ($d4)) ;
		} 
		else {
			return (999) ;
		}
	}
	else {
		my ($d10) = distance ($gx1, $gy1, $nx, $ny) ;
		my ($d20) = distance ($gx2, $gy2, $nx, $ny) ; 
		my ($d30) = distance ($gx1, $gy1, $gx2, $gy2) ;

		$m = ($gy2 - $gy1) / $t ;
		$b = $gy1 - $m * $gx1 ;
		my ($d40) = ($ny - $m * $nx - $b) / sqrt ($m * $m + 1) ;
		
		if ( ($d10 <= $d30) and ($d20 <= $d30) ) {
			my $result = abs ($d40 * 111.11) ; 			
			# print "dist = $result\n" ;
			return $result ;
		}
		else {
			return (999) ;
		}
	}
}

sub checkOverlap {
        my ($w1xMin, $w1yMin, $w1xMax, $w1yMax, $w2xMin, $w2yMin, $w2xMax, $w2yMax) = @_ ;

        my $result = 1 ;

        if ($w1xMin > $w2xMax) { $result = 0 ; }
        if ($w2xMin > $w1xMax) { $result = 0 ; }
        if ($w1yMin > $w2yMax) { $result = 0 ; }
        if ($w2yMin > $w1yMax) { $result = 0 ; }

        return $result ;
}

#######
# links
#######

sub picLinkMapnik {
	my $lon = shift ;
	my $lat = shift ;
	my $zoom = shift ;
	my (@res) = tileNumber ($lon, $lat, $zoom) ;
	my $link = "<img src=\"http://tile.openstreetmap.org/" . $zoom . "/" . $res[0] . "/" . $res[1] . ".png\">" ;
	return ($link) ;
}

sub picLinkOsmarender {
	my $lon = shift ;
	my $lat = shift ;
	my $zoom = shift ;
	my (@res) = tileNumber ($lon, $lat, $zoom) ;
	my $link = "<img src=\"http://tah.openstreetmap.org/Tiles/tile/" . $zoom . "/" . $res[0] . "/" . $res[1] . ".png\">" ;
	return ($link) ;
}



sub osmLink {
	my $lon = shift ;
	my $lat = shift ;
	my $zoom = shift ;
	my $string = "<A HREF=\"http://www.openstreetmap.org/?mlat=" . $lat . "&mlon=" . $lon . "&zoom=" . $zoom . "\">OSM</A>" ;
	return ($string) ;
}

sub osmLinkMarkerWay {
	my $lon = shift ;
	my $lat = shift ;
	my $zoom = shift ;
	my $way = shift ;
	my $string = "<A HREF=\"http://www.openstreetmap.org/?mlat=" . $lat . "&mlon=" . $lon . "&zoom=" . $zoom . "&way=" . $way . "\">OSM marked</A>" ;
	return ($string) ;
}

sub osbLink {
	my $lon = shift ;
	my $lat = shift ;
	my $zoom = shift ;
	my $string = "<A HREF=\"http://openstreetbugs.schokokeks.org/?lon=" . $lon . "&lat=" . $lat . "&zoom=" . $zoom . "\">OSB</A>" ;
	return ($string) ;
}

sub mapCompareLink {
	my $lon = shift ;
	my $lat = shift ;
	my $zoom = shift ;
	my $string = "<A HREF=\"http://tools.geofabrik.de/mc/?mt0=mapnik&mt1=tah&lon=" . $lon . "&lat=" . $lat . "&zoom=" . $zoom . "\">mapcompare</A>" ;
	return ($string) ;
}

sub josmLink {
#
# DON'T USE ANY LONGER
# 
	my $lon = shift ;
	my $lat = shift ;
	my $span = shift ;
	my $way = shift ;
	my ($string) = "<A HREF=\"http://localhost:8111/load_and_zoom?" ;
	my $temp = $lon - $span ;
	$string = $string . "left=" . $temp ;
	$temp = $lon + $span ;
	$string = $string . "&right=" . $temp ;
	$temp = $lat + $span ;
	$string = $string . "&top=" . $temp ;
	$temp = $lat - $span ;
	$string = $string . "&bottom=" . $temp ;
	$string = $string . "&select=way" . $way ;
	$string = $string . "\" target=\"hiddenIframe\">Local JOSM</a>" ;
	return ($string) ;
}

sub josmLinkDontSelect {
	my $lon = shift ;
	my $lat = shift ;
	my $span = shift ;
	my $way = shift ;
	my ($string) = "<A HREF=\"http://localhost:8111/load_and_zoom?" ;
	my $temp = $lon - $span ;
	$string = $string . "left=" . $temp ;
	$temp = $lon + $span ;
	$string = $string . "&right=" . $temp ;
	$temp = $lat + $span ;
	$string = $string . "&top=" . $temp ;
	$temp = $lat - $span ;
	$string = $string . "&bottom=" . $temp ;
	$string = $string . "\" target=\"hiddenIframe\">Local JOSM</a>" ;
	return ($string) ;
}

sub josmLinkSelectWay {
	my $lon = shift ;
	my $lat = shift ;
	my $span = shift ;
	my $way = shift ;
	my ($string) = "<A HREF=\"http://localhost:8111/load_and_zoom?" ;
	my $temp = $lon - $span ;
	$string = $string . "left=" . $temp ;
	$temp = $lon + $span ;
	$string = $string . "&right=" . $temp ;
	$temp = $lat + $span ;
	$string = $string . "&top=" . $temp ;
	$temp = $lat - $span ;
	$string = $string . "&bottom=" . $temp ;
	$string = $string . "&select=way" . $way ;
	$string = $string . "\" target=\"hiddenIframe\">Local JOSM</a>" ;
	return ($string) ;
}

sub josmLinkSelectWays {
	my ($lon, $lat, $span, @ways) = @_ ;
	my ($string) = "<A HREF=\"http://localhost:8111/load_and_zoom?" ;
	my $temp = $lon - $span ;
	$string = $string . "left=" . $temp ;
	$temp = $lon + $span ;
	$string = $string . "&right=" . $temp ;
	$temp = $lat + $span ;
	$string = $string . "&top=" . $temp ;
	$temp = $lat - $span ;
	$string = $string . "&bottom=" . $temp ;
	$string = $string . "&select=way" . $ways[0] ;
	if (scalar @ways > 1) {
		my $i ;
		for ($i=1; $i < scalar @ways; $i++) {
			$string = $string . ",way" . $ways[$i] ;
		}
	}
	$string = $string . "\" target=\"hiddenIframe\">Local JOSM</a>" ;
	return ($string) ;
}

sub josmLinkSelectNode {
	my $lon = shift ;
	my $lat = shift ;
	my $span = shift ;
	my $node = shift ;
	my ($string) = "<A HREF=\"http://localhost:8111/load_and_zoom?" ;
	my $temp = $lon - $span ;
	$string = $string . "left=" . $temp ;
	$temp = $lon + $span ;
	$string = $string . "&right=" . $temp ;
	$temp = $lat + $span ;
	$string = $string . "&top=" . $temp ;
	$temp = $lat - $span ;
	$string = $string . "&bottom=" . $temp ;
	$string = $string . "&select=node" . $node ;
	$string = $string . "\" target=\"hiddenIframe\">Local JOSM</a>" ;
	return ($string) ;
}

sub analyzerLink {
	my $id = shift ;

	my $result = "<A HREF=\"http://betaplace.emaitie.de/webapps.relation-analyzer/analyze.jsp?relationId=" . $id . "\">" . $id . "</A>" ;
	return $result ;
}


#####
# GPX
#####

sub printGPXHeader {
	my $file = shift ;

	print $file "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" ;
	print $file "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"Gary68script\" version=\"1.1\"\n" ;
	print $file "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" ;
	print $file "    xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" ;
}




sub printGPXFoot {
	my $file = shift ;

	print $file "</gpx>\n" ;
}




sub printGPXWaypoint {
	my ($file, $lon, $lat, $text) = @_ ;

	print $file "<wpt lat=\"", $lat, "\" lon=\"", $lon, "\">" ;
	print $file "<desc>", $text, "</desc></wpt>\n" ;
}

#######
# other
#######

sub stringTimeSpent {
	my $timeSpent = shift ;
	my $string ;
	$string =  ($timeSpent/(60*60))%99 . " hours, " . ($timeSpent/60)%60 . " minutes and " . $timeSpent%60 . " seconds" ;
	return ($string) ;
}

sub binSearch {
    my ($find, $aRef) = @_ ;        

    my ($lower, $upper) = (0, @$aRef - 1) ;

    my $result ;

    while ($upper >= $lower) {
	$result = int( ($lower + $upper) / 2) ;
	if ($aRef->[$result] < $find) {
	    $lower = $result + 1 ;
	}
	elsif ($aRef->[$result] > $find) {
	    $upper = $result - 1 ;
	} 
	else {
	    return ($result) ; 
	}
    }
    return (-1) ;         
}

sub printProgress {
	my $program = shift ;
	my $osm = shift ;
	my $startTime = shift ;
	my $fullCount = shift ;
	my $actualCount = shift ;

	my ($percent) = $actualCount / $fullCount * 100 ;
	my ($time_spent) = (time() - $startTime) / 3600 ;
	my ($tot_time) = $time_spent / $actualCount * $fullCount ; 
	my ($to_go) = $tot_time - $time_spent ;
	printf STDERR "$program - file: %s %d/100 Ttot=%2.1fhrs Ttogo=%2.1fhrs   \n", $osm, $percent, $tot_time, $to_go ; 
}


######
# html
######

sub printHTMLiFrameHeader {
	my $file = shift ;
	my $title = shift ;
	print $file "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"";
	print $file "  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
	print $file "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n";
	print $file "<head><title>", $title, "</title>\n";
	print $file "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n";
	print $file "</head>\n";
	print $file "<iframe style=\"display:none\" id=\"hiddenIframe\" name=\"hiddenIframe\"></iframe>\n" ;
	print $file "<body>\n";
	return (1) ;
}

sub printHTMLHeader {
	my $file = shift ;
	my $title = shift ;
	print $file "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"";
	print $file "  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
	print $file "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n";
	print $file "<head><title>", $title, "</title>\n";
	print $file "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n";
	print $file "</head>\n<body>\n";
	return (1) ;
}

sub printHTMLFoot {
	my $file = shift ;
	print $file "</body>\n</html>\n" ;
	return (1) ;
}

sub printWayList {
	my ($file, @list) = @_ ;
	print $file "<table border=\"1\">\n";
	print $file "<tr>\n" ;
	print $file "<th>Line</th>\n" ;
	print $file "<th>WayId</th>\n" ;
	print $file "</tr>\n" ;

	my $i = 0 ;
	foreach (@list) {
		$i++ ;
		print $file "<tr><td>$i</td><td>", historyLink ("way", $_) , "</td></tr>\n" ;
	}

	print $file "</table>\n";
}

sub printNodeList {
	my ($file, @list) = @_ ;
	print $file "<table border=\"1\">\n";
	print $file "<tr>\n" ;
	print $file "<th>Line</th>\n" ;
	print $file "<th>NodeId</th>\n" ;
	print $file "</tr>\n" ;

	my $i = 0 ;
	foreach (@list) {
		$i++ ;
		print $file "<tr><td>$i</td><td>", historyLink ("node", $_) , "</td></tr>\n" ;
	}

	print $file "</table>\n";
}

sub printHTMLTableHead {
	my ($file) = shift ;
	print $file "<table border=\"1\">\n" ;
}

sub printHTMLTableFoot {
	my ($file) = shift ;
	print $file "</table>\n" ;
}

sub printHTMLTableHeadings {
	my ($file, @list) = @_ ;
	print $file "<tr>\n" ; 
	foreach (@list) { print $file "<th>" . $_ . "</th>\n" ; }
	print $file "</tr>\n" ; 
}

sub printHTMLTableRowLeft {
	my ($file, @list) = @_ ;
	print $file "<tr>\n" ; 
	foreach (@list) { print $file "<td align=\"left\">" . $_ . "</td>\n" ; }
	print $file "</tr>\n" ; 
}

sub printHTMLTableRowRight {
	my ($file, @list) = @_ ;
	print $file "<tr>\n" ; 
	foreach (@list) { print $file "<td align=\"right\">" . $_ . "</td>\n" ; }
	print $file "</tr>\n" ; 
}

sub printHTMLCellLeft {
	my ($file) = shift ;
	my ($value) = shift ;
	print $file "<td align=\"left\">" . $value . "</td>\n" ;
}

sub printHTMLCellCenter {
	my ($file) = shift ;
	my ($value) = shift ;
	print $file "<td align=\"center\">" . $value . "</td>\n" ;
}

sub printHTMLCellRight {
	my ($file) = shift ;
	my ($value) = shift ;
	print $file "<td align=\"right\">" . $value . "</td>\n" ;
}

sub printHTMLRowStart {
	my ($file) = shift ;
	print $file "<tr>\n" ;
}

sub printHTMLRowEnd {
	my ($file) = shift ;
	print $file "</tr>\n" ;
}

sub printHTMLiFrame {
	my ($file) = shift ;
	print $file "<iframe style=\"display:none\" id=\"hiddenIframe\" name=\"hiddenIframe\"></iframe>\n" ;
}

sub getBugs {
	my ($lon, $lat, $bugsDownDist, $bugsMaxDist) = @_ ;
	my $resultString = "" ;
	my ($x1, $y1, $x2, $y2 ) ;
	my %lon = () ;
	my %lat = () ;
	my %text = () ;
	my %open = () ;
	my %user = () ;

	$x1 = $lon - $bugsDownDist ;
	$x2 = $lon + $bugsDownDist ;
	$y1 = $lat - $bugsDownDist ;
	$y2 = $lat + $bugsDownDist ;
	#print "get bugs $x1, $y1, $x2, $y2...\n" ; 

	sleep 1.5 ;
	my ($url) = 'http://openstreetbugs.appspot.com/getBugs?b=' . $y1 . '&t=' . $y2 . '&l=' . $x1 . '&r=' . $x2 ;
	my ($content) = get $url ;
	if (!defined $content) {
		$resultString =  "bugs request error<br>" ;
	}
	else {
		# process string
		#print "CONTENT\n", $content, "\n\n" ;
		open my $sh, '<', \$content or die $!;
		while (<$sh>) {
			my $line = $_ ;
			#print "actual line: $line\n" ;
			my ($id)   = ($line =~ /^putAJAXMarker\((\d+),/) ;
			my ($text)   = ($line =~ /^.*\"([-\w\W\d\D\s\']+)\"/) ;
			my ($user)   = ($line =~ /^.*\[([-\w\W\d\D\s\']+)\]/) ;
			my ($lon, $lat) = ($line =~ /,([-]?[\d]+\.[\d]+),([-]?[\d]+\.[\d]+)/);
			my ($open)   = ($line =~ /.*(\d)\);$/) ;
			if (!$user) { $user = "-" ; }
			#print "\nfields found: $id $text $user $lon $lat $open\n\n" ;
			$text =~ s/<hr \/>/:::/g ;  # replace <HR /> horizontal rulers by ":::"
			$lon{$id} = $lon;
			$lat{$id} = $lat ;
			$text{$id} = $text ;
			if ($open == 0) { $open{$id} = "OPEN" ; } else { $open{$id} = "CLOSED" ; }
			$user{$id} = $user ;

		}
		close $sh or die $!;
		my $id ;
		foreach $id (keys %lon) {
			my ($d) = distance ($lon, $lat, $lon{$id}, $lat{$id}) ;
			#print "check id: $id, distance: $d", , "\n" ;
			if ($d < $bugsMaxDist) {
				$d = int ($d * 1000) ;
				$resultString = $resultString . "<strong>" . $open{$id} . "</strong>" . " (" . $d . "m)<br>" ;
				$resultString = $resultString . $text{$id} . "<br>" ;
			}
		}
	}

	#print "$resultString\n\n" ; 
	return $resultString ;
}


sub APIgetWay {
#
# wayId == 0 returned if error
#
	my ($wayId) = shift ;

	my $content ;
	my $url ;
	my $try = 0 ;
	my $wayUser = "" ;
	my @wayNodes = () ;
	my @wayTags = () ;

	#print "\nAPI request for way $wayId\n" ;

	while ( (!defined($content)) and ($try < 4) ) {
		$url = $apiUrl . "way/" . $wayId ;
		$content = get $url ;
		$try++ ;
	}

	#print "API result:\n$content\n\n" ;

	if (!defined $content) {
		print "ERROR: error receiving OSM query result for way $wayId\n" ;
		$wayId = 0 ;
		$content = "" ;
	}
	if (grep(/<error>/, $content)) {
		print "ERROR: invalid OSM query result for way $wayId\n" ;	
		$wayId = 0 ;
	}
	
	if (defined $content) {
		# parse $content
		if ($wayId != 0) {
			my (@lines) = split /\n/, $content ;
			foreach my $line (@lines) {
				if (grep /<way id/, $line ) {
					my ($u) = ($line =~ /^.+user=[\'\"](.*)[\'\"]/) ;
					if (defined $u) { $wayUser = $u ; } 
				}
				if (grep /<nd ref/, $line ) {
					my ($node) = ($line =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/) ;
					if (defined $node) { push @wayNodes, $node ; }
				}
				if (grep /<tag k=/, $line ) {
					my ($k, $v) = ($line =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
					if ( (defined $k) and (defined $v) ) { push @wayTags, [$k, $v] ; }
				}
			}
		}
	}

	#print "\nAPI result:\n$wayId\nNodes: @wayNodes\nTags: " ;
	#foreach my $t (@wayTags) { print "$t->[0]:$t->[1] \n" ; }

	return ($wayId, $wayUser, \@wayNodes, \@wayTags) ;
}


1 ;


