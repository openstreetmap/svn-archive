#!/usr/bin/perl
#
# unmappedplaces.pl
# program takes osm file and outputs unmapped and potentially unmapped places in html or openlayers poi format
#
# written by Gary68
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
# v4
# - new output format
#
# v5
# - places eingeschränkt
#
# v6
# - sorted output
# - unmapped / sparsely mapped
# - different parameters for different places
# - output for slippy map openlayers
# - much better performance
# - time spent format changed
# - count of more places
# - file date now shown in stats
# - grouped lists
# v61
# - minor bug fixed regarding major cities/towns in vincinity
#

use strict;
use warnings;

use File::stat;
use Time::localtime;

use Exporter ;

# constants
my $version = "6.1" ;
my $usage = "usage:  unmappedplaces6.pl <osmfile> <htmlfile> <slippyfile>\n" ;
my %dists ; # in km // STAY BELOW 10km !!!
$dists{"city"} =  4.0 ;
$dists{"town"} =  1.0 ;
$dists{"suburb"} =  0.5 ;
$dists{"village"} =  0.5 ;

my %sparse ; # in km
$sparse{"city"} =  4800 ;
$sparse{"town"} =  400 ;
$sparse{"suburb"} =  60 ;
$sparse{"village"} =  60 ;

my %unmapped ; # in km
$unmapped{"city"} =  2400 ;
$unmapped{"town"} =  200 ;
$unmapped{"suburb"} =  30 ;
$unmapped{"village"} =  30 ;

my $iconRed = "Ol_icon_red_example.png" ;
my $iconBlue = "Ol_icon_blue_example.png" ;

# variables
my $dist ;
my $node_count = 0 ;
my $city_count = 0 ;
my $town_count = 0 ;
my $village_count = 0 ;
my $suburb_count = 0 ;

my $place_count = 0 ;
my $unmapped = 0 ;
my $sparselyMapped = 0 ;

my $placekey ;
my $placekey1 ;
my $placekey2 ;
my $nodekey ;
my $progress ;
my $i ;
my $key ;
my $key2 ;

my $place_type_temp ;
my $place_name_temp ;

my $d_lat ;
my $d_lon ;

my $time0 ;
my $time1 ;
my $time2 ;
my $timespent ;

my $html_file ;
my $slippy_file ;

my %node_lon ;
my %node_lat ;

my %place_name ;
my %place_type ;
my %place_lon ;
my %place_lat ;
my %place_nodes_inside ;
my %place_superior ;
my %place_superior_dist ;

my %node_hash ;
my $hash_value ;

my @outputData ;
my @outputDataSorted ;
my %outputGrouped ;

# get filename from cmd line
my $xml = shift||'';
if (!$xml)
{
	print STDERR $usage ;
	die () ;
}

# file present?
unless( -f $xml || $xml eq "-" ) {
	die("input file '$xml' not found\n");
}

my $html = shift||'';
if (!$html)
{
	print STDERR $usage ;
	die () ;
}

my $slippyout = shift||'';
if (!$slippyout)
{
	print STDERR $usage ;
	die () ;
}

$time0 = time() ;

# open input file
open(XML, "<$xml") or die("$!");

# parse data
while(my $line = <XML>) {

	# node data
	if($line =~ /^\s*\<node/) {
		# get all needed information
		my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/); # get node id
		my ($lon) = ($line =~ /^.+lon=[\'\"]([\d,\.]+)[\'\"]/);    # get position
		my ($lat) = ($line =~ /^.+lat=[\'\"]([\d,\.]+)[\'\"]/);    # get position

		unless ($id) { next; }
		unless ($lat) { next; }
		unless ($lon) { next; }

		# print "Node: ", $id, "\n" ;
		$node_count++ ;

		#store node
		$node_lon {$id} = $lon ;
		$node_lat {$id} = $lat ;

		# put in hash
		$hash_value = hashValue ($lon, $lat) ;
		#print $hash_value, "\n" ;
		push @{$node_hash {$hash_value}}, $id ;

		if ($line =~ /\//) {
		} 
		else {
			# print "processing tags...\n" ;
			#get tags
			$place_type_temp ="" ;
			$place_name_temp = "" ;
			while (not($line =~ /\/node/)) {
				$line = <XML> ;
				# print $line, "\n" ;
				my ($key)   = ($line =~ /^\s*\<tag k=[\'\"](\w+)[\'\"]/); # get key
				# my ($value)   = ($line =~ /^\s*v=[\'\"](\w+)[\'\"]/); # get value
				#my ($value) = ($line =~ /^.+v=[\'\"](\w+)[\'\"]/);       # get value // PROBLEM WITH UMLAUTE

				my ($value) = ($line =~ /v=[\'\"](.*)[\'\"]/) ; # NEW
				
				unless ($key) { next; }
				unless ($value) { next; }

				if ($key eq "place") {
					$place_type_temp = $value ;
				}			
				if ($key eq "name") {
					$place_name_temp = $value ;

				}	
				# print "K/v: ", $key, "/", $value, "\n" ;
			}
			if (($place_type_temp ne "") and ($place_name_temp ne "") and 
				( ($place_type_temp eq "city") or ($place_type_temp eq "town") or 
				($place_type_temp eq "village") or ($place_type_temp eq "suburb") ) ) {
				$place_name {$id} = $place_name_temp ;
				$place_type {$id} = $place_type_temp ;
				$place_lat {$id} = $lat ;
				$place_lon {$id} = $lon ;
				$place_nodes_inside {$id} = 0 ;
				$place_count = $place_count + 1 ;
				if ($place_type_temp eq "city") { $city_count++ } ;
				if ($place_type_temp eq "town") { $town_count++ } ;
				if ($place_type_temp eq "village") { $village_count++ } ;
				if ($place_type_temp eq "suburb") { $suburb_count++ } ;
				#print $place_type_temp, " ", $place_name_temp, " ", $lon, " ", $lat, "\n" ;
			}
		}		
	}
}

#foreach $placekey (keys %node_hash) {
#	print $placekey, " " ;
#	foreach (@{$node_hash{$placekey}}) {
#		print $_, " " ;
#	}
#	print "\n" ; 
#}

$time1 = time() ;

$i = 0 ;
foreach $placekey (keys %place_name) {
	$i++ ;
	$progress = int ($i / $place_count * 100) ;
	if ($i % 10 == 0) { print $xml, " ", $progress, "%\n" ; }

	#print "Name: ", $place_name{$placekey}, "\n" ;
	#print "Type: ", $place_type{$placekey}, "\n" ;
	#print "max dist: ", $dists{$place_type{$placekey}}, "\n" ;
	
	my $lo ;
	my $la ;
	for ($lo=$place_lon{$placekey}-0.1; $lo<=$place_lon{$placekey}+0.1; $lo=$lo+0.1) {
		for ($la=$place_lat{$placekey}-0.1; $la<=$place_lat{$placekey}+0.1; $la=$la+0.1) {
			#print $lo, " ", $la, "\n" ;
			$hash_value = hashValue ($lo, $la) ;
		
			#if (defined @{$node_hash{$hash_value}}) {
				early1: foreach $nodekey ( @{$node_hash{$hash_value}} ) {
					#print $nodekey, "\n" ;
					my $d_lat = ($node_lat{$nodekey} - $place_lat{$placekey}) * 111.11 ;
					my $d_lon = ($node_lon{$nodekey} - $place_lon{$placekey}) * cos ( $node_lat{$nodekey} / 360 * 3.14 * 2 ) * 111.11;
					$dist = sqrt ($d_lat*$d_lat+$d_lon*$d_lon);
					if ($dist < $dists{$place_type{$placekey}}) {
						$place_nodes_inside {$placekey} = $place_nodes_inside {$placekey} + 1 ;
					}
					if ($place_nodes_inside {$placekey} > $sparse{$place_type{$placekey}}) {
						last early1 ;
					}
				}
			#}
		}
	}
}


# count unmapped/sparsely mapped places

foreach $placekey (keys %place_name) {
	print "Name: ", $place_name{$placekey}, "\n" ;
	print "Type: ", $place_type{$placekey}, "\n" ;
	print "max dist: ", $dists{$place_type{$placekey}}, "\n" ;
	print "Nodes in: ", $place_nodes_inside {$placekey}, "\n" ;
	if ($place_nodes_inside {$placekey} < $sparse{$place_type{$placekey}}) {
		if ($place_nodes_inside {$placekey} < $unmapped{$place_type{$placekey}}) {
			$unmapped = $unmapped + 1 ;
		}
		else {
			$sparselyMapped ++ ;
		}
	}
	print "\n" ;
}

# find superior city/town
foreach $placekey1 (keys %place_name) {
	$place_superior {$placekey1} = "-" ;
	$place_superior_dist {$placekey1} = 9999 ;
	if (($place_type {$placekey1} eq "city") or ($place_type {$placekey1} eq "town")) {
		# do nothing
	} 
	else {
		# look for sup
		foreach $placekey2 (keys %place_name) {
			my $d_lat = ($place_lat{$placekey1} - $place_lat{$placekey2}) * 111.11;
			my $d_lon = ($place_lon{$placekey1} - $place_lon{$placekey2}) * cos ( $place_lat{$placekey2} / 360 * 3.14 * 2 ) * 111.11 ;
			$dist = sqrt($d_lat*$d_lat+$d_lon*$d_lon);
			if (  ($dist < $place_superior_dist {$placekey1}) 
				and ( 
					($place_type {$placekey2} eq "city") or 
					($place_type {$placekey2} eq "town"))   ) {
				$place_superior {$placekey1} = $place_name {$placekey2} ;
				$place_superior_dist {$placekey1} = $dist ;
			}
		}
	}
}

$time2 = time() ;
$timespent = $time2 - $time0 ;

# print output files

open ($html_file, , ">", $html) ; 
open ($slippy_file, ">", $slippyout) ; 

print_html_header() ;
print $slippy_file "lat\tlon\ttitle\tdescription\ticon\ticonSize\ticonOffset\n" ;

# print stats
print $html_file "<h1>Unmapped places for ", $xml, "</h1>\n" ;
print $html_file "<p>by Gary68</p>\n" ;
print $html_file "<p>Program Version: $version</p>\n" ;

print $html_file "<h2>Stats</h2>\n" ;
print $html_file "<p>File date: ", ctime(stat($xml)->mtime), "<br>\n" ;
print $html_file "Node count:      ", $node_count, "<br>\n" ;
print $html_file "place count:     ", $place_count, "<br>\n" ;
print $html_file "Cities:          ", $city_count, "<br>\n" ;
print $html_file "Towns:           ", $town_count, "<br>\n" ;
print $html_file "Suburbs:         ", $suburb_count, "<br>\n" ;
print $html_file "Villages:        ", $village_count, "<br>\n" ;
print $html_file "Unmapped places: ", $unmapped, "<br>\n" ;
print $html_file "Sparsely mapped: ", $sparselyMapped, "<br>\n" ;
printf $html_file "Unmapped:        %2d percent<br>\n", ($unmapped*100/$place_count) ;
printf $html_file "Sparsely mapped: %2d percent<br>\n", ($sparselyMapped*100/$place_count) ;
print $html_file "runtime1 =       ", $time1-$time0, "secs<br>\n" ;
print $html_file "<p>Time: ", ($timespent/(60*60))%99, " hours, ", ($timespent/60)%60, " minutes and ", $timespent%60, " seconds</p>" ;

print $html_file "<h2>Definitions</h2>\n" ;
print $html_file "<p>City: ",  $dists{'city'}, " km radius - sparse/unmapped thresholds ", $sparse{'city'}, "/", $unmapped{'city'}, "<br>\n" ;
print $html_file "Town: ",  $dists{'town'}, " km radius - sparse/unmapped thresholds ", $sparse{'town'}, "/", $unmapped{'town'}, "<br>\n" ;
print $html_file "Village: ",  $dists{'village'}, " km radius - sparse/unmapped thresholds ", $sparse{'village'}, "/", $unmapped{'village'}, "<br>\n" ;
print $html_file "Suburb: ",  $dists{'suburb'}, " km radius - sparse/unmapped thresholds ", $sparse{'suburb'}, "/", $unmapped{'suburb'}, "</p>\n" ;

print $html_file "<h2>Details</h2>\n" ; 
print $html_file "<p>Grouped list follows below!</p>\n" ;
foreach $placekey (keys %place_name) { 
        if ($place_nodes_inside {$placekey} < $sparse{$place_type{$placekey}}) { 
		my $temp ;

		$temp = "<!--" . $place_name {$placekey} . "-->" ;
		$temp = $temp . "<A HREF=\"http://www.openstreetmap.org/?lat=" . $place_lat {$placekey} . "&lon=" . $place_lon {$placekey} . "&zoom=13\">" ; 
                $temp = $temp . $place_name {$placekey} . "</A>" ; 
                $temp = $temp . " (" . $place_type {$placekey} . " near " . $place_superior {$placekey} . ", " . $place_nodes_inside{$placekey} . ")\n"  ; 
	        if ($place_nodes_inside {$placekey} < $unmapped{$place_type{$placekey}}) { 
			$temp = $temp . " - potentially unmapped<br>" ;
		}
		else {
			$temp = $temp . " - potentially sparsely mapped<br>" ;
		}
		$temp = $temp . "\n" ;

		push @outputData, $temp ;
		push @{$outputGrouped{$place_superior{$placekey}}}, $place_name {$placekey} ;

		print $slippy_file $place_lat {$placekey}, "\t" ;
		print $slippy_file $place_lon {$placekey}, "\t" ;
		print $slippy_file $place_name {$placekey}, "\t" ;
	        if ($place_nodes_inside {$placekey} < $unmapped{$place_type{$placekey}}) { 
			print $slippy_file "Potentially unmapped place", "\t" ;
			print $slippy_file "./", $iconRed, "\t" ;
		}
		else {
			print $slippy_file "Potentially sparsely mapped place", "\t" ;
			print $slippy_file "./", $iconBlue, "\t" ;
		}
		print $slippy_file "16,16", "\t" ;
		print $slippy_file "-8,-8", "\n" ;
        } 
} 


@outputDataSorted = sort @outputData ;

foreach (@outputDataSorted) {
	print $html_file $_ ;
}

print $html_file "<h2>Grouped</h2>\n" ;

foreach $key (sort keys %outputGrouped) {
	if ($key ne "-") {
		print $html_file "<h3>$key</h3>\n<p>" ;
		foreach $key2 (@{$outputGrouped{$key}}) {
			print $html_file $key2, "<br>\n" ;
		}
		print $html_file "</p>\n" ;
	}
}


print_html_foot() ;

close $html_file ;
close $slippy_file ;

print "\nTime: ", ($timespent/(60*60))%99, " hours, ", ($timespent/60)%60, " minutes and ", $timespent%60, " seconds\n" ;


######
# HTML
######

sub print_html_header {
	print $html_file "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"";
	print $html_file "  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
	print $html_file "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n";
	print $html_file "<head><title>unmapped by Gary68</title>\n";
	print $html_file "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n";
	print $html_file "</head>\n<body>\n";
}

sub print_html_foot {
	print $html_file "</body>\n</html>\n" ;
}


sub hashValue {
	my $lon = shift ;
	my $lat = shift ;

	my $lo = int ($lon*10) * 10000 ;
	my $la = int ($lat*10) ;

	return ($lo+$la) ;
}

