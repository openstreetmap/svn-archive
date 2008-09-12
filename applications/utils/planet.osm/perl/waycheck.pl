#!/usr/bin/perl
#
#
# written by Gary68
#
# modes: A = end check, print all ways with one or two open ends
#        B = end check, print only ways with two open ends
#        X = crossing check
# 
#
# Definition File Format Example
#
#<XML>
#  <k="mode" v="X">				# mode in capital letter(s)
#  <k="dist" v="10">				# max distance (in km) of start nodes of ways to be checked
#  <k="check" v="highway:motorway">
#  <k="check" v="highway:motorway_link">
#  <k="check" v="highway:trunk">
#  <k="check" v="highway:trunk_link">
#  <k="against" v="highway:primary">
#  <k="against" v="highway:primary_link">
#  <k="against" v="highway:secondary">
#  <k="against" v="highway:tertiary">
#  <k="against" v="junction:roundabout">
#</XML>
#
#
# v?
# - implementation of different modes of operation
# - implementation of parameters for "check" and "check against"
# - layer, bridge, tunnel info added to parsed data
# - mode X implemented, dups removed, first data checks
# - file timestamp added
# - mode X progress added
# - def in xml
# - error hadling
#

use strict;
use warnings;
use List::Util qw[min max];

use File::stat;
use Time::localtime;





###########
# variables
###########

my $usage = "waycheck <def.xml> <file.osm> <out.htm>" ;
my $version = "1.00" ;

my $mode = "-" ;
my $d_lat ;
my $d_lon ;
my $time0 ;			# start
my $time1 ;			# end parsing
my $time2 ;			# end mode A and B
my $time3 ;			# end mode X
my $key ; my $key1 ; my $key2 ; 
my $key3 ; my $key4 ;
my $text ="" ;
my $i ; my $j ;
my $line1 ;
my $progress = 0 ; my $progress2 = 0 ;
my $sc ; my $ec ;
my $out_file ;			# html out file handle
my $def_file ;			# def file handle
my $double = 0 ; my $single = 0 ;
my $checked = 0 ;
my $waycount = 0 ;		# ways in XML
my $neededwaycount = 0 ;	# ways cat 1 and 2	
my $highwaycount = 0 ;		# ways with highway tag
my $checkwaycount = 0 ;		# ways cat 1
my $unconnected_single = 0 ; 
my $unconnected_double = 0 ;
my $node_count = 0 ;
my $josm = 0.001 ; 		# area calc for josm link
my $teststr ;
my $numbercheck ; 		# counter for grep1
my $numberagainst ; 		# counter for grep2
my $dist_max = "-" ;

my @cross1 ; 			# result function crossing
my @check =   () ;
my @against = () ; 

				
my %node_lon ;			# lons of nodes
my %node_lat ;			# lats of nodes

my %temp ;
my %waynodes ;
my %waytype ;
my %waylayer ;
my %waytunnel ;
my %waybridge ;
my %waycategory ;
my %waystartconnected ;
my %wayendconnected ;
my %wayname ;




# get definitions filename from cmd line
my $def = shift||'';
if (!$def)
{
    print STDERR $usage, "\n" ;
}


my $xml = shift||'';		# get filename from cmd line
if (!$xml)
{
	die (print $usage, "\n");
}

unless( -f $xml || $xml eq "-" ) {	# file present?
	die("input file '$xml' not found\n");
}


my $html_name = shift||'';	# output file name
if (!$html_name)
{
	die (print $usage, "\n");
}

print "\nWayCheck...\n" ;
print "def : ", $def, "\n" ;
print "xml : ", $xml, "\n" ;
print "out : ", $html_name, "\n" ;


##################
# read definitions
##################

print "Read definitions...\n" ;
open ($def_file, , "<", $def) or die "definition file not found" ;

$i = 0 ;
$j = 0 ;
while (my $line = <$def_file>) {
	#print "read line: ", $line, "\n" ;
	my ($k)   = ($line =~ /^\s*<k=[\'\"]([:\w\s\d]+)[\'\"]/); # get key
	my ($v) = ($line =~ /^.+v=[\'\"]([:\w\s\d]+)[\'\"]/);       # get value
	
	if ($k and $v) {
		#print "key: ", $k, "\n" ;
		#print "val: ", $v, "\n" ;
		if ($k eq "mode") {
			$mode = $v ;
		}

		if ($k eq "dist") {
			$dist_max = $v ;
		}

		if ($k eq "check") {
			$check[$i] = $v ;
			$i++;
		}

		if ($k eq "against") {
			$against[$j] = $v ;
			$j++;
		}
	}
}

print "mode: ", $mode, "\n" ;
print "dist: ", $dist_max, " km\n" ;

print "\nCheck:\n" ;
foreach (@check) {print $_, "\n" ;}
print "\nAgainst:\n" ;
foreach (@against) {print $_, "\n" ;}
print "\n" ;
close $def_file ;

if ($mode eq "-") {
	print "no mode provided in definition file\n" ;
	die () ;
}

if ($dist_max eq "-") {
	print "no max dist provided in definition file\n" ;
	die () ;
}

if ($i == 0) {
	print "no check way provided in definition file\n" ;
	die () ;
}

if ($j == 0) {
	print "no against way provided in definition file\n" ;
	die () ;
}



$time0 = time() ;		# get start time in seconds

##########################
# OPEN AND READ INPUT FILE
##########################

print "Read XML file $xml ...\n " ;
# open input file
open(XML, "<$xml") or die("$!");

############
# parse data
############

while(my $line = <XML>) {
	if($line =~ /^\s*\<way/) {
		my $n = 0 ; # way nodes count

		# get all needed information
		my ($id)   = ($line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/); # get way id
		unless ($id) { next; }
		$waycount++ ;
		$waytype{$id} = "unknown" ;
		$waylayer{$id} = 0 ;
		$waytunnel{$id} = "-" ;
		$waybridge{$id} = "-" ;
		$waycategory{$id} = 0 ;
		$wayname{$id} = "&nbsp;" ;
		$waystartconnected{$id} = 0 ;
		$wayendconnected{$id} = 0 ;
		${waynodes{$id}} = () ;
		#print "found way ", $id, " " ;

		$line = <XML> ;
		while (not($line =~ /\/way/)) { # more way data

			#get nodes and type
			my ($node) = ($line =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/); # get node id
			my ($k)   = ($line =~ /^\s*\<tag k=[\'\"](\w+)[\'\"]/); # get key
			my ($v) = ($line =~ /^.+v=[\'\"]([-\w\d\s]+)[\'\"]/);       # get value // SPACE ADDED
			if ($node) {
				#add node to way data
				$waynodes{$id}[$n] = $node ;
				$n++ ;
				#print $node, " " ;
			}

			#get type and other information
			if ($k and $v) {
				#print "k/v; ", $k, " ", $v, "\n" ;

				$numbercheck = 0 ;
				$numberagainst = 0 ;

				my ($teststr) = $k.":".$v ;
				#$numbercheck   = grep (/$teststr/, @check ) ; 
				#$numberagainst = grep (/$teststr/, @against ) ;
				$numbercheck   = grep (/^$teststr$/, @check ) ; 	# exact match
				$numberagainst = grep (/^$teststr$/, @against ) ;	# exact match
				

				if ($numbercheck > 0) {
					$waycategory{$id} = 1 ;
					#print "cat 1\n" ;
				}

				if (($numberagainst > 0) and ($numbercheck == 0)) {
					$waycategory{$id} = 2 ;
					#print "cat 2\n" ;
				}

				#print $id, " ", $numbercheck, " ", $numberagainst, " ", $waycategory{$id}, "\n" ;

				if (($k eq "highway") or ($k eq "waterway") ) {
					$waytype {$id} = $v ;
					#$highwaycount++ ;
				}

				if ($k eq "name") {
					$wayname{$id} = $wayname{$id} . "name=" . $v . " " ;
				}

				if ($k eq "ref") {
					$wayname{$id} = $wayname{$id} . "ref=" . $v . " " ;

				}

				if ($k eq "layer") {
					$waylayer{$id} = $v ;
				}

				if ($k eq "bridge") {
					$waybridge{$id} = $v ;
				}

				if ($k eq "tunnel") {
					$waytunnel{$id} = $v ;
				}
			}

			$line = <XML> ;
		}
		#print " ", $waytype{$id}, "\n" ;
	}

	if($line =~ /^\s*\<node/) {
		# get all needed information
		my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/); # get node id
		my ($lon) = ($line =~ /^.+lon=[\'\"]([-\d,\.]+)[\'\"]/);    # get position
		my ($lat) = ($line =~ /^.+lat=[\'\"]([-\d,\.]+)[\'\"]/);    # get position

		unless ($id) { next; }
		unless ($lat) { next; }
		unless ($lon) { next; }

		# print "Node: ", $id, "\n" ;
		$node_count++ ;

		#store node
		$node_lon {$id} = $lon ;
		$node_lat {$id} = $lat ;
	}
}
close $xml ;

print "finished\n" ;


######################
# REMOVE UNNEEDED WAYS
######################

print "Remove unneeded ways ...\n " ;


$neededwaycount = $waycount ;
%temp = %waytype ;
foreach $key1 (keys %temp) {
	if ($waycategory{$key1} == 0 ) {
		delete $waynodes{$key1} ;
		delete $waytype{$key1} ;
		delete $waylayer{$key1} ;
		delete $waytunnel{$key1} ;
		delete $waybridge{$key1} ;
		delete $waycategory{$key1} ;
		delete $waystartconnected{$key1} ;
		delete $wayendconnected{$key1} ;
		delete $wayname{$key1} ;
		$neededwaycount-- ;
	}
}

print "finished\n" ;


$time1 = time() ; # end parse data



##############
# PRINT HEADER
##############

open ($out_file, , ">", $html_name) ;

print $out_file "<HTML>\n" ;
print $out_file "<H1>Waycheck by Gary68</H1>\n" ;
print $out_file "<H2>file: ", $xml, "</h2>\n" ;
print $out_file "File date: ", ctime(stat($xml)->mtime), "<br>\n" ;
print $out_file "Program version: ", $version, "<br>\n" ;
print $out_file "Definition file: ", $def, "<br>\n" ;
print $out_file "<h3>Checked ways</h2>\n" ;
print $out_file "<b>Waytypes to check:</b> " ;
foreach (@check) { print $out_file $_, " -  " } 
print $out_file "<br><br>\n" ;
print $out_file "<b>Waytypes to check against:</b> " ;
foreach (@against) { print $out_file $_, " -  " } 
print $out_file "<br>\n" ;


###################
# CALC MODE A AND B
###################

if ( (grep (/A/, $mode)) or (grep (/B/, $mode))) {
	print "Start calc mode A/B ...\n " ;
	# find connections
	foreach $key1 (keys %waynodes) {
		$progress++ ;
		if ($progress % 1000 == 0) {
			my ($percent) = $progress / $neededwaycount * 100 ;
			my ($time_spent) = (time() - $time1) / 3600 ;
			my ($tot_time) = $time_spent / $progress * $neededwaycount ; 
			my ($to_go) = $tot_time - $time_spent ;
			printf "end check - file: %s %d/100 Ttot=%2.1fhrs Ttogo=%2.1fhrs\n", $xml, $percent, $tot_time, $to_go ; 
		}

		if ($waycategory{$key1} == 1) {
			$checkwaycount++ ;
			$checked = 0 ;
			early: foreach $key2 (keys %waytype) {
				my ($d1) = $node_lat{$waynodes{$key1}[0]} - $node_lat{$waynodes{$key2}[0]} ;
				my ($d2) = $node_lon{$waynodes{$key1}[0]} - $node_lon{$waynodes{$key2}[0]} ;
				my ($dist) = sqrt ($d1*$d1+$d2*$d2) * 111.11 ;
				if ( ($waycategory{$key2} > 0)  and ($key1 != $key2) and ($dist < $dist_max) ) {
					$checked++ ;
					# print "\n c ", $waytype{$key2}, " ", $key2, " nc ", $#{$waynodes{$key2}}, "\n" ;
					for ($i=0; $i <= $#{$waynodes{$key2}}; $i++ ) {                 #nodes in way 2
						# print $i, " ", $waynodes{$key2}[$i], " " ;
						if ($waynodes{$key2}[$i] == $waynodes{$key1}[0]) {
							$waystartconnected{$key1} = 1 ;
							#print " found start " ;
						}
						if ($waynodes{$key2}[$i] == $waynodes{$key1}[$#{$waynodes{key1}}]) {
							$wayendconnected{$key1} = 1 ;	
							#print " found end   " ;
						}
					}
				}
				if (($wayendconnected{$key1} == 1) and ($waystartconnected{$key1} == 1)) {
					last early ;
				}
			}
			# print "\n***", $key1, " checked: ", $checked, "ways\n" ;
		}
	}
	print "finished\n" ;
}



#foreach $key1 (keys %waytype) {
#		print $waytype{$key1}, " ", $key1, " ", $waystartconnected{$key1}, " ", $wayendconnected{$key1}, "\n" ; 
#		print "nodes: ", $#{$waynodes{$key1}}, " startid: ", $waynodes{$key1}[0], " endid: ", $waynodes{$key1}[$#{$waynodes{$key1}}], "\n" ;
#}

$time2 = time() ; # end calc A and B
$checkwaycount = 0 ;

foreach $key1 (keys %waytype) {
	if ($waycategory{$key1} == 1) {
		$checkwaycount++ ;
	}
}


#######################
# CALC AND PRINT MODE X
#######################

if  (grep (/X/, $mode) )  {
	print "Start calc mode X ...\n " ;

	print $out_file "<h3>Mode X - file: ", $xml, "</h3>\n";

	print $out_file "Number check ways: ", $checkwaycount, "<br>\n" ;
	print $out_file "Number \"against\" ways: ", $neededwaycount, "<br>\n" ;

	print $out_file "<table border=1>\n";
	print $out_file "<tr>\n" ;
	print $out_file "<th>Line</th>\n" ;
	print $out_file "<th>Way1</th>\n" ;
	print $out_file "<th>Way2</th>\n" ;
	print $out_file "<th>Type1</th>\n" ;
	print $out_file "<th>Type2</th>\n" ;
	print $out_file "<th>Name1</th>\n" ;
	print $out_file "<th>Name2</th>\n" ;
	print $out_file "<th>Seg1</th>\n" ;
	print $out_file "<th>Seg2</th>\n" ;
	print $out_file "<th>Pos</th>\n" ;
	print $out_file "<th bgcolor=\"yellow\">Lay1</th>\n" ;
	print $out_file "<th bgcolor=\"yellow\">Lay2</th>\n" ;
	print $out_file "<th>Tun1</th>\n" ;
	print $out_file "<th>Tun2</th>\n" ;
	print $out_file "<th>Bri1</th>\n" ;
	print $out_file "<th>Bri2</th>\n" ;
	print $out_file "<th>OSM</th>\n" ;
	print $out_file "<th>OSB</th>\n" ;
	print $out_file "</th>\n" ;



	$i = 0 ;
	# find crossings
	foreach $key3 (keys %waynodes) {
		$progress2++ ;
		if ($progress2 % 100 == 0) {
			my ($percent) = $progress2 / $neededwaycount * 100 ;
			my ($time_spent) = (time() - $time2) / 3600 ;
			my ($tot_time) = $time_spent / $progress2 * $neededwaycount ;
			my ($to_go) = $tot_time - $time_spent ;
			printf "Mode X - file: %s %d/100 Ttot=%2.1fhrs Ttogo=%2.1fhrs\n", $xml, $percent, $tot_time, $to_go ; 
		}

		#print $progress2/$neededwaycount, "\n" ; 

		if ($waycategory{$key3} == 1) {
			#print "way 1: ", $key3, " ", $#{$waynodes{$key3}}, " ", $waytype{$key3}, "\n" ;
			#$checkwaycount++ ;
			#$checked = 0 ;
			foreach $key4 (keys %waytype) {
				my ($di1) = $node_lat{$waynodes{$key3}[0]} - $node_lat{$waynodes{$key4}[0]} ;
				my ($di2) = $node_lon{$waynodes{$key3}[0]} - $node_lon{$waynodes{$key4}[0]} ;
				my ($dist2) = sqrt ($di1*$di1+$di2*$di2) * 111.11 ;
				if ( ( ($waycategory{$key4} > 0)  and ($key3 != $key4) and ($dist2 < $dist_max) )
					and not (($waycategory{$key3} == $waycategory{$key4}) and ($key3<$key4)) ) {
				#if ( ($waycategory{$key4} > 0)  and ($key3 != $key4) and ($dist2 < $dist_max) ) {

					# ONLY IF NEEDED. IF cat1=cat2 and key3<key4
			
					#print "    way 2: ", $key4, " ", $#{$waynodes{$key4}}, " ", $waytype{$key4}, "\n" ;
					# check these ways
					my ($a) ; my ($b) ;
					for ($a=0; $a<$#{$waynodes{$key3}}; $a++) {
						#print "\na=", $a, " " ;
						for ($b=0; $b<$#{$waynodes{$key4}}; $b++) {
							#print "b=", $b, " " ;
							@cross1 = crossing ($node_lon{$waynodes{$key3}[$a]}, 
									$node_lat{$waynodes{$key3}[$a]}, 
									$node_lon{$waynodes{$key3}[$a+1]}, 
									$node_lat{$waynodes{$key3}[$a+1]}, 
									$node_lon{$waynodes{$key4}[$b]}, 
									$node_lat{$waynodes{$key4}[$b]}, 
									$node_lon{$waynodes{$key4}[$b+1]}, 
									$node_lat{$waynodes{$key4}[$b+1]}) ;
							#print @cross1, "\n" ;
							#if (($cross1[0] != 0) and ($cross1[1] != 0) ) {
							if (($cross1[0] != 0) and ($cross1[1] != 0) and ($waylayer{$key3} == $waylayer{$key4}) ) {

								$i++ ;

								print $out_file "<tr>\n" ;	

								#Line
								print $out_file "<td>", $i, "</td>\n" ;

								#Way1
								print $out_file "<td><A HREF=\"http://www.openstreetmap.org/browse/way/", $key3, "\">", $key3, "</A></td>\n" ;
								#Way2
								print $out_file "<td><A HREF=\"http://www.openstreetmap.org/browse/way/", $key4, "\">", $key4, "</A></td>\n" ; 

								#Type1
								print $out_file "<td>$waytype{$key3}</td>\n" ; 
								#Type2
								print $out_file "<td>$waytype{$key4}</td>\n" ; 

								#Name1
								print $out_file "<td>$wayname{$key3}</td>\n" ; 
								#Name2
								print $out_file "<td>$wayname{$key4}</td>\n" ; 

								#Seg1
								print $out_file "<td>$a</td>\n" ; 
								#Seg2
								print $out_file "<td>$b</td>\n" ; 

								#Position
								printf $out_file " <td>%i/%i</td>\n", $cross1[0], $cross1[1] ;


								#Lay1
								print $out_file "<td bgcolor=\"yellow\">", $waylayer{$key3}, "</td>\n" ; 
								#Lay2
								print $out_file "<td bgcolor=\"yellow\">", $waylayer{$key4}, "</td>\n" ; 

								#Tun1
								print $out_file "<td>", $waytunnel{$key3}, "</td>\n" ; 
								#Tun2
								print $out_file "<td>", $waytunnel{$key4}, "</td>\n" ; 

								#Bri1
								print $out_file "<td>", $waybridge{$key3}, "</td>\n" ; 		#Bri2
								print $out_file "<td>", $waybridge{$key4}, "</td>\n" ; 

								#OSM
								print $out_file "<td><A HREF=\"http://www.openstreetmap.org/?mlat=", $cross1[1], 
									"&mlon=", $cross1[0],"&zoom=16&layers=B00FTF\">OSM</td>\n" ;

								#OSB
								print $out_file "<td><A HREF=\"http://openstreetbugs.appspot.com/?lon=", $cross1[0], 
									"&lat=", $cross1[1],"&zoom=16\">OSB</td>\n" ;

								print $out_file "</tr>\n" ;	

							}
						}
					}
				}
			}	
		}
	}
	print $out_file "</table>\n" ;

	$time3 = time() ; # end calc and print X
	print $out_file "<br>Crossings found: ", $i, "<br>\n" ;
	print $out_file "<br>Time for mode X: ", $time3-$time2, "secs<br>\n" ;

	print "finished\n" ;
}





####################
# PRINT MODE A AND B
####################

if ( (grep (/A/, $mode)) or (grep (/B/, $mode))) {
	print "Print mode A/B ...\n " ;

	print $out_file "<h3>Mode A/B file", $xml, "</h3>\n" ;
	print $out_file "file: ", $xml, "<br>\n" ;
	print $out_file "total nodes: ", $node_count, "<br>\n" ;
	print $out_file "total ways: ", $waycount, "<br>\n" ;
	print $out_file "total neededways: ", $neededwaycount, "<br>\n" ;
	print $out_file "total checkways: ", $checkwaycount, "<br>\n" ;
	print $out_file "time1: ", $time1-$time0, "secs<br>\n" ;
	print $out_file "time2: ", $time2-$time0, "secs total<br>\n" ;

	print $out_file "<table border=1>\n" ;
	print $out_file " <tr>\n" ;
	print $out_file "  <th>Way(s)</th>\n" ;
	print $out_file "  <th>Name/Ref</th>\n" ;
	print $out_file "  <th>ID</th>\n" ;
	print $out_file "  <th>Issues</th>\n" ;
	print $out_file "  <th>Position</th>\n" ;
	print $out_file "  <th>Start</th>\n" ;
	print $out_file "  <th>End</th>\n" ;
	print $out_file "  <th>History</th>\n" ;
	print $out_file "  <th>JOSM</th>\n" ;
	print $out_file "  <th>OSB Start</th>\n" ;
	print $out_file "  <th>OSB End</th>\n" ;
	print $out_file " </tr>\n" ;

	foreach $key1 (keys %waytype) {
		if ($waycategory{$key1} == 1) {
			$sc = $waystartconnected{$key1} ;
			$ec = $wayendconnected{$key1} ;
	
			my ($lon1) = $node_lon{$waynodes{$key1}[0]} ;
			my ($lat1) = $node_lat{$waynodes{$key1}[0]} ;
			my ($lon2) = $node_lon{$waynodes{$key1}[$#{$waynodes{$key1}}]} ;
			my ($lat2) = $node_lat{$waynodes{$key1}[$#{$waynodes{$key1}}]} ;

			if (($sc == 0) and ($ec == 0)) {
				$text = "Start and end unconnected" ;
				$unconnected_double++ ;
			}		
			else {
				if ($sc == 0) {
					$text = "Start unconnected" ;
					$unconnected_single++ ;
				}
				if ($ec == 0) {
					$text = "End unconnected" ;
					$unconnected_single++ ;
				}
			}

			if (   (($sc == 0) or ($ec == 0))    and (grep (/A/, $mode)) or
				  (($sc == 0) and ($ec == 0))    and (grep (/B/, $mode))) {
				print $out_file " <tr>\n" ;
				print $out_file "  <td>$waytype{$key1}</td>\n" ;
				print $out_file "  <td>$wayname{$key1}</td>\n" ;
				print $out_file "  <td>", $key1, "</td>\n" ;
				print $out_file "  <td>", $text, "</td>\n" ;
				printf $out_file " <td>%i/%i</td>\n", $lat1, $lon1 ;

				print $out_file "  <td><A HREF=\"http://www.openstreetmap.org/?mlat=" ; 
				print $out_file $lat1, "&mlon=", $lon1 ;
				print $out_file "&zoom=15&layers=B00FTF\">Osm Start</A></td>\n" ;

				print $out_file "  <td><A HREF=\"http://www.openstreetmap.org/?mlat=" ; 
				print $out_file $lat2, "&mlon=", $lon2 ;
				print $out_file "&zoom=15&layers=B00FTF\">Osm End</A></td>\n" ;

				printf $out_file "  <td><A HREF=\"http://www.openstreetmap.org/browse/way/%i/history\">History</a></td>\n", $key1 ;

				print $out_file "  <td><A HREF=\"http://localhost:8111/load_and_zoom?" ;
				print $out_file "left=", min ($lon1, $lon2) - $josm ;
				print $out_file "&right=", max ($lon1, $lon2) + $josm ;
				print $out_file "&top=", max ($lat1, $lat2) + $josm ;
				print $out_file "&bottom=", min ($lat1, $lat2) - $josm ;
				print $out_file "&select=way", $key1 ;
				print $out_file "\">Local JOSM</a></td>\n" ;

				print $out_file "  <td><A HREF=\"http://openstreetbugs.appspot.com/?lon=" ; 
				print $out_file $lon1, "&lat=", $lat1 ;
				print $out_file "&zoom=15\">OSB Start</A></td>\n" ;

				print $out_file "  <td><A HREF=\"http://openstreetbugs.appspot.com/?lon=" ; 
				print $out_file $lon2, "&lat=", $lat2 ;
				print $out_file "&zoom=15\">OSB End</A></td>\n" ;

	
				print $out_file " </tr>\n" ;

			}

		}
	}
	print $out_file "</table>\n" ;

	print $out_file "<br>Ways unconnected: ", $unconnected_double, "<br>\n" ;
	print $out_file "Ways one open end: ", $unconnected_single, "<br>\n" ;
	print "finished\n" ;
}


print $out_file "</HTML>\n" ;


#die("\n") ;


#############
# subroutines
#############

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
	# nur geraden prüfen, wenn node ids ungleich !!!

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

	
	# wenn punkt auf gerade, dann 1 - DELTA Prüfung !!! delta?


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

}

