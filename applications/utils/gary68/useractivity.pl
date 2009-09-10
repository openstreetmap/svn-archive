# 
#
# useractivity.pl by gary68
#
#
#
# Copyright (C) 2009, Gerhard Schwanz
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

use strict ;
use warnings ;

use OSM::osm ;
use File::stat;
use Time::localtime;

my $program = "useractivity.pl" ;
my $usage = $program . " file1.osm file2.osm out.htm" ;
my $version = "1.0 BETA" ;

my $wayId1 ; my $wayUser1 ; my @wayNodes1 ; my @wayTags1 ;
my $wayId2 ; my $wayUser2 ; my @wayNodes2 ; my @wayTags2 ;

my $nodeId1 ; my $nodeUser1 ; my $nodeLat1 ; my $nodeLon1 ; my @nodeTags1 ; my $nodeVersion1 ; my $nodeTimestamp1 ; my $nodeUid1 ; my $nodeChangeset1 ;
my $nodeId2 ; my $nodeUser2 ; my $nodeLat2 ; my $nodeLon2 ; my @nodeTags2 ; my $nodeVersion2 ; my $nodeTimestamp2 ; my $nodeUid2 ; my $nodeChangeset2 ;
my $aRef1 ; my $aRef2 ;

my $time0 = time() ; 

my $html ; my $htmlName ;
my $osm1Name ; my $osm2Name ;

my $file1 ; my $file2 ; my $bz1 ; my $bz2 ; my $isBz21 ; my $isBz22 ; my $line1 ; my $line2 ; my $bzerrno ;
my $file1Name ; my $file2Name ;

###############
# get parameter
###############
$osm1Name = shift||'';
if (!$osm1Name)
{
	die (print $usage, "\n");
}

$osm2Name = shift||'';
if (!$osm2Name)
{
	die (print $usage, "\n");
}

$htmlName = shift||'';
if (!$htmlName)
{
	die (print $usage, "\n");
}

print "\n$program $version for files $osm1Name $osm2Name\n\n" ;


# test ways 1
# expand way fields
# test ways
# copy ways file 2
# test ways file 2


#------------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------------

openOsm1File ($osm1Name) ;
openOsm2File ($osm2Name) ;
processNodes() ;
processWays() ;
closeOsm1File () ;
closeOsm2File () ;
outputConsole() ;
outputHTML() ;


die () ;

my $nodeNumber1 = 0 ;
openOsm1File ($osm1Name) ;
($nodeId1, $nodeVersion1, $nodeTimestamp1, $nodeUid1, $nodeUser1, $nodeChangeset1, $nodeLat1, $nodeLon1, $aRef1) = getNode1 () ;
if ($nodeId1 != -1) {
	@nodeTags1 = @$aRef1 ;
}

while ($nodeId1 != -1) {
	$nodeNumber1++ ;
	#print "id=$nodeId1\nlon=$nodeLon1\nlat=$nodeLat1\nuid=$nodeUid1\nuser=$nodeUser1\nset=$nodeChangeset1\n" ;
	#foreach my $t (@nodeTags1) { print $t->[0] . ":" . $t->[1] . "\n" ;} 
	#print "\n" ;

	# next
	($nodeId1, $nodeVersion1, $nodeTimestamp1, $nodeUid1, $nodeUser1, $nodeChangeset1, $nodeLat1, $nodeLon1, $aRef1) = getNode1 () ;
	if ($nodeId1 != -1) {
		@nodeTags1 = @$aRef1 ;
	}
}
closeOsm1File () ;
print "$nodeNumber1 nodes read in file 1\n" ; 


my $nodeNumber2 = 0 ;
openOsm2File ($osm2Name) ;
($nodeId2, $nodeVersion2, $nodeTimestamp2, $nodeUid2, $nodeUser2, $nodeChangeset2, $nodeLat2, $nodeLon2, $aRef1) = getNode2 () ;
if ($nodeId2 != -1) {
	@nodeTags2 = @$aRef1 ;
}

while ($nodeId2 != -1) {
	$nodeNumber2++ ;
	#print "id=$nodeId2\nlon=$nodeLon2\nlat=$nodeLat2\nuid=$nodeUid2\nuser=$nodeUser2\nset=$nodeChangeset2\n" ;
	#foreach my $t (@nodeTags2) { print $t->[0] . ":" . $t->[1] . "\n" ;} 
	#print "\n" ;

	# next
	($nodeId2, $nodeVersion2, $nodeTimestamp2, $nodeUid2, $nodeUser2, $nodeChangeset2, $nodeLat2, $nodeLon2, $aRef1) = getNode2 () ;
	if ($nodeId2 != -1) {
		@nodeTags2 = @$aRef1 ;
	}
}
closeOsm2File () ;
print "$nodeNumber2 nodes read in file 2\n" ; 



########
# FINISH
########

print "\n$program finished after ", stringTimeSpent (time()-$time0), "\n\n" ;


#------------------------------------------------------------------------------------
# Node procesing
#------------------------------------------------------------------------------------

sub processNodes {

}

#------------------------------------------------------------------------------------
# Way procesing
#------------------------------------------------------------------------------------

sub processWays {

}

#------------------------------------------------------------------------------------
# Output functions
#------------------------------------------------------------------------------------

sub outputConsole {

}

sub outputHTML {

}



#------------------------------------------------------------------------------------
# Basic object operations
#------------------------------------------------------------------------------------

sub getNode1 {
	my ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) ;
	my @gTags = () ;
	if($line1 =~ /^\s*\<node/) {
		if ( (grep /user=/, $line1) and (grep /uid=/, $line1) ) {
			($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) = 
			($line1 =~ /^\s*\<node id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+uid=[\'\"](.+)[\'\"].+user=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"].+lat=[\'\"](.+)[\'\"].+lon=[\'\"](.+)[\'\"]/) ;
		}
		else {
			($id, $version, $timestamp, $changeset, $lat, $lon) = 
			($line1 =~ /^\s*\<node id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"].+lat=[\'\"](.+)[\'\"].+lon=[\'\"](.+)[\'\"]/) ;
			$user = "unknown" ; $uid = 0 ;
		}
		if (!defined $user) { $user = "unknown" ; }
		if (!defined $uid) { $uid = 0 ; }

		# TODO copy sub
		if (!$id or (! (defined ($lat))) or ( ! (defined ($lon))) ) {
			print "WARNING reading osm file, line follows (expecting id, lon, lat and user for node):\n", $line1, "\n" ; 
		}
		else {
			if ( (grep (/">/, $line1)) or (grep (/'>/, $line1)) ) {                  # more lines, get tags
				nextLine1() ;
				while (!grep(/<\/node>/, $line1)) {
					my ($k, $v) = ($line1 =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
					if ( (defined ($k)) and (defined ($v)) ) {
						my $tag = [$k, $v] ; push @gTags, $tag ;
					}
					else { 
						#print "WARNING tag not recognized: ", $line1, "\n" ; 
					}
					nextLine1() ;
				}
				nextLine1() ;
			}
			else {
				nextLine1() ;
			}
		}
	}
	else {
		return (-1, -1, -1, -1, -1) ; 
	} # node
	return ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon, \@gTags) ; # in main @array = @$ref
} # getNode1

sub getNode2 {
	my ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) ;
	my @gTags = () ;
	if($line2 =~ /^\s*\<node/) {
		if ( (grep /user=/, $line2) and (grep /uid=/, $line2) ) {
			($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon) = 
			($line2 =~ /^\s*\<node id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+uid=[\'\"](.+)[\'\"].+user=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"].+lat=[\'\"](.+)[\'\"].+lon=[\'\"](.+)[\'\"]/) ;
		}
		else {
			($id, $version, $timestamp, $changeset, $lat, $lon) = 
			($line2 =~ /^\s*\<node id=[\'\"](\d+)[\'\"].+version=[\'\"](\d+)[\'\"].+timestamp=[\'\"](.+)[\'\"].+changeset=[\'\"](\d+)[\'\"].+lat=[\'\"](.+)[\'\"].+lon=[\'\"](.+)[\'\"]/) ;
			$user = "unknown" ; $uid = 0 ;
		}
		if (!defined $user) { $user = "unknown" ; }
		if (!defined $uid) { $uid = 0 ; }

		if (!$id or (! (defined ($lat))) or ( ! (defined ($lon))) ) {
			print "WARNING reading osm file 2, line follows (expecting id, lon, lat and user for node):\n", $line2, "\n" ; 
		}
		else {
			if ( (grep (/">/, $line2)) or (grep (/'>/, $line2)) ) {                  # more lines, get tags
				nextLine2() ;
				while (!grep(/<\/node>/, $line2)) {
					my ($k, $v) = ($line2 =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
					if ( (defined ($k)) and (defined ($v)) ) {
						my $tag = [$k, $v] ; push @gTags, $tag ;
					}
					else { 
						#print "WARNING tag not recognized file 2: ", $line2, "\n" ; 
					}
					nextLine2() ;
				}
				nextLine2() ;
			}
			else {
				nextLine2() ;
			}
		}
	}
	else {
		return (-1, -1, -1, -1, -1) ; 
	} # node
	return ($id, $version, $timestamp, $uid, $user, $changeset, $lat, $lon, \@gTags) ; # in main @array = @$ref
} # getNode2



# TODO copy 1->2
sub getWay1 {
	my $gId ; my $gU ; my @gTags ; my @gNodes ;
	if($line1 =~ /^\s*\<way/) {
		my ($id)   = ($line1 =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/); # get way id
		my ($u) = ($line1 =~ /^.+user=[\'\"](.*)[\'\"]/);       # get value // REGEX???
		if (!$u) { $u = "unknown" ; }
		if (!$id) { print "ERROR reading osm file, line follows (expecting way id):\n", $line1, "\n" ; }
		unless ($id) { next; }
		nextLine1() ;
		while (not($line1 =~ /\/way>/)) { # more way data
			my ($node) = ($line1 =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/); # get node id
			my ($k, $v) = ($line1 =~ /^\s*\<tag k=[\'\"](.+)[\'\"]\s*v=[\'\"](.+)[\'\"]/) ;
			if ($node) {
				push @gNodes, $node ;
			}
			if ($k and defined($v)) { my $tag = [$k, $v] ; push @gTags, $tag ; }
			nextLine1() ;
		}
		nextLine1() ;
		$gId = $id ; $gU = $u ;
	}
	else {
		return (-1, -1, -1, -1) ;
	}
	return ($gId, $gU, \@gNodes, \@gTags) ;
} # getWay1




#------------------------------------------------------------------------------------
# Basic file operations
#------------------------------------------------------------------------------------

sub openOsm1File {
	$file1Name = shift ;
	if (grep /.bz2/, $file1Name) { $isBz21 = 1 ; } else { $isBz21 = 0 ; }
	if ($isBz21) {
		$bz1 = bzopen($file1Name, "rb") or die "Cannot open $file1Name: $bzerrno\n" ;
	}
	else {
		open ($file1, "<", $file1Name) || die "can't open osm file1" ;
	}
	nextLine1() ;		
	while ( ! (grep /\<node/, $line1) ) {
		nextLine1() ;
	}
	return 1 ;
}

sub closeOsm1File {
	if ($isBz21) { $bz1->bzclose() ; }
	else { close ($file1) ; }
}

sub nextLine1 {
	if ($isBz21) { $bz1->bzreadline($line1) ; }
	else { $line1 = <$file1> ; }
}

sub openOsm2File {
	$file2Name = shift ;
	if (grep /.bz2/, $file2Name) { $isBz22 = 1 ; } else { $isBz22 = 0 ; }
	if ($isBz22) {
		$bz2 = bzopen($file2Name, "rb") or die "Cannot open $file2Name: $bzerrno\n" ;
	}
	else {
		open ($file2, "<", $file2Name) || die "can't open osm file2" ;
	}
	nextLine2() ;		
	while ( ! (grep /\<node/, $line2) ) {
		nextLine2() ;
	}
	return 1 ;
}

sub closeOsm2File {
	if ($isBz22) { $bz2->bzclose() ; }
	else { close ($file2) ; }
}

sub nextLine2 {
	if ($isBz22) { $bz2->bzreadline($line2) ; }
	else { $line2 = <$file2> ; }
}

