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

use strict ;
use warnings ;

use OSM::osm 4.0 ;

my $programName = "routing.pl" ;
my $usage = $programName . " file.rou node1 node2" ; 
my $version = "1.0" ;

#
# ENTER INFORMATION IN THIS SECTION
# ---------------------------------
#

my $root ;
my $node1 ;
my $node2 ;

my $infinity = "inf";
my %dist ;
my %edge ;
my %prev ;
my @s ;
my %usedNodes ;
my @unsolved ;

my $rouName ; 

my $time0 ; my $time1 ;

# get parameter

$rouName = shift||'';
if (!$rouName)
{
	die (print $usage, "\n");
}

$node1 = shift||'';
if (!$node1)
{
	die (print $usage, "\n");
}

$node2 = shift||'';
if (!$node2)
{
	die (print $usage, "\n");
}


print "\n$programName $version for file $rouName\n" ;

$time0 = time() ;



$root = $node1 ;

my $rouFile ;
my $line ;
open ($rouFile, "<", $rouName) or die ("can't open input file!") ;
while ($line = <$rouFile>) {

	my ($n1, $n2, $d) = ($line =~ /^\s*(\d+)\s+(\d+)\s+([\d\.]+)/ ) ;
	if ((defined $n1) and (defined $n2) and (defined $d)) {
		# print "$n1, $n2, $d\n" ;
		$usedNodes{$n1} = 1 ;
		$usedNodes{$n2} = 1 ;
		$edge{$n1}{$n2} = $d ;
	}

}
close ($rouFile) ;


# DIJKSTRA
print "dijkstra running...\n" ;

# init unsolved nodes array
foreach my $node (keys %usedNodes) { push @unsolved, $node ; }

# alg
# all dist = infinity
foreach my $n (keys %usedNodes) { 
	$dist{$n} = $infinity ; 
	$prev{$n}=$n ; 
}

$dist{$root} = 0;

# loop while we have unsolved nodes

my $finished = 0 ;

while (@unsolved and !$finished) {
	print scalar (@unsolved), "\n" ;
	my ($n, $n2) ;
	@unsolved = sort byDistance @unsolved;
	push @s, $n = shift @unsolved;

	if ($n == $node2) { $finished = 1 ; }

	foreach $n2 (keys %{$edge{$n}}) {
		if (($dist{$n2} eq $infinity) ||
			($dist{$n2} > ($dist{$n} + $edge{$n}{$n2}) )) {
			$dist{$n2} = $dist{$n} + $edge{$n}{$n2} ;
			$prev{$n2} = $n;
		}
	}
}

if ($dist{$node2} ne $infinity) {
	print "distance to destination = ", $dist{$node2}, "\n" ;
	print "route to destination\n" ;
	my $act = $node2 ;
	while ($prev{$act} != $node1) {
		print $prev{$act}, " " ;
		$act = $prev{$act}
	}
}
else {
	print "DESTINATION UNREACHABLE\n" ;
}



$time1 = time() ;
print "\n$programName finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;



sub byDistance {
   $dist{$a} eq $infinity ? +1 :
   $dist{$b} eq $infinity ? -1 :
       $dist{$a} <=> $dist{$b};
}
