# 
#
# listrelations.pl by gary68
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
# 
#


use strict ;
use warnings ;

use File::stat ;
use Time::localtime ; 

use OSM::osm 4.4 ;

my $program = "listrelations.pl" ;
my $usage = $program . " file.osm out.htm out.csv" ;
my $version = "1.0" ;


my $wayId ;
my $wayUser ;
my @wayNodes ;
my @wayTags ;
my $nodeId ;
my $nodeUser ;
my $nodeLat ;
my $nodeLon ;
my @nodeTags ;
my $aRef1 ;
my $aRef2 ;

my $relationId ;
my $relationUser ;
my @relationMembers ;
my @relationTags ;
my $placeCount = 0 ;

my $relationCount = 0 ;
my $members = 0 ;
my @member;
my $wayCount = 0 ; my $invalidWayCount = 0 ; my %invalidWays ;
my $nodeCount = 0 ;

my %number = () ;

my $osmName ; 
my $htmlName ; my $html ;
my $csvName ; my $csv ;


$osmName = shift||'';
if (!$osmName)
{
	die (print $usage, "\n");
}

$htmlName = shift||'';
if (!$htmlName)
{
	die (print $usage, "\n");
}

$csvName = shift||'';
if (!$csvName)
{
	die (print $usage, "\n");
}

print "\n$program $version \nfor file $osmName\n\n" ;


print "parsing relations...\n" ;
openOsmFile ($osmName) ;
print "- skipping nodes...\n" ;
skipNodes() ;
print "- skipping ways...\n" ;
skipWays() ;
print "- checking...\n" ;

my $line = 0 ;

open ($html, ">", $htmlName) || die ("Can't open html output file") ;
open ($csv, ">", $csvName) || die ("Can't open gpx output file") ;

printHTMLHeader ($html, "List of relations by Gary68") ;

print $html "<H1>List of relations by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Info</H2>\n" ;
print $html "<p>", stringFileInfo ($osmName), "</p>\n" ;

print $html "<H2>Data</H2>\n" ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Line", "RelationId", "Type", "Name", "Ref", "#members") ;

print $csv $program . " " . stringFileInfo ($osmName), "\n" ;
print $csv "line;relationId;type;name;ref;members\n" ;

($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
if ($relationId != -1) {
	@relationMembers = @$aRef1 ;
	@relationTags = @$aRef2 ;
}

while ($relationId != -1) {
	$line++ ;	
	my $members = scalar (@relationMembers) ;

	my $name = "-" ;
	my $ref = "-" ;
	my $type = "-" ;

	my $i ;
	if (scalar (@relationTags) > 0) {
		for ($i=0; $i<scalar (@relationTags); $i++) {
			#print "${$relationTags[$i]}[0] = ${$relationTags[$i]}[1]\n" ; 
			if ( ${$relationTags[$i]}[0] eq "name") { $name =  ${$relationTags[$i]}[1] ; }
			if ( ${$relationTags[$i]}[0] eq "ref") { $ref =  ${$relationTags[$i]}[1] ; }
			if ( ${$relationTags[$i]}[0] eq "type") { $type =  ${$relationTags[$i]}[1] ; }
		}
	}

	if (defined $number{$type}) {
		$number{$type}++ ;
	}
	else {
		$number{$type} = 1 ;
	}

	printHTMLRowStart ($html) ;
	printHTMLCellLeft ($html, $line) ;
	printHTMLCellLeft ($html, historyLink ("relation", $relationId) . "(osm) " . analyzerLink ($relationId) . "(analyzer)" ) ;
	printHTMLCellLeft ($html, $type) ;
	printHTMLCellLeft ($html, $name) ;
	printHTMLCellLeft ($html, $ref) ;
	printHTMLCellLeft ($html, $members) ;
	printHTMLRowEnd ($html) ;
	
	print $csv $line, ";" ; 
	print $csv $relationId, ";" ; 
	print $csv $type, ";" ; 
	print $csv "\"", $name,  "\"", ";" ; 
	print $csv "\"", $ref,  "\"",  ";" ; 
	print $csv $members, "\n" ; 

	#next
	($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
	if ($relationId != -1) {
		@relationMembers = @$aRef1 ;
		@relationTags = @$aRef2 ;
	}
}

printHTMLTableFoot ($html) ;

print $html "<h2>Statistics</h2>" ;
printHTMLTableHead ($html) ;
printHTMLTableHeadings ($html, "Type", "Number") ;

my $key ;
foreach $key (sort keys %number) {
	printHTMLRowStart ($html) ;
	printHTMLCellLeft ($html, $key) ;
	printHTMLCellLeft ($html, $number{$key}) ;
	printHTMLRowEnd ($html) ;
}
printHTMLTableFoot ($html) ;


printHTMLFoot ($html) ;

closeOsmFile () ;

close ($html) ;
close ($csv) ;

print "\n$line relations found and listed.\n";
print "\n$program finished.\n";


