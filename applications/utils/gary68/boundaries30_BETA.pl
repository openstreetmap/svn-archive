#
#  $Revision$ by $Author$, $Date$
#
# boundaries.pl by gary68
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
# IN:  file.osm
#
# OUT: file.htm (list)
# OUT: file.csv (list)
# OUT: file.hirarchy.htm (list)
# OUT: file.hirarchy.csv (list)
# OUT: file.XXXXX.poly (borders original)
# OUT: file.Simplified.XXXXX.poly (borders simplified)
# OUT: file.Resized.XXXXX.poly (borders resized and potentially simplified)
# OUT: file.XXXXX.png (map of border)
#
# relation member roles used: outer and none
# 
# parameters and options see below
#
# Version 2
# - find hirarchies and report
# - use simplified polygons for hirarchy if specified
# - time calc 
# - error handling for relations caontaining themselves as member
# - check for max nesting level when parsing relation members being relations (prevent loops eating memory and terminate program)
# - resize option (-resize, -factor (float > 1.0)
# - pics with resized polygons
# - distinguish between invalid and selected
# - qualify invalid relation list with causes of errors
#
# Version 3
# - support multipolygons, multiple border segments
#
# TODO
# - command line error handling
# 

use strict ;
use warnings ;

use File::stat ;
use Time::localtime ; 
use Getopt::Long ;
use Math::Polygon ;

use OSM::osm 4.4 ;
use OSM::osmgraph ;

my $program = "boundaries.pl" ;
my $usage = $program . " see code GetOptions" ;
my $version = "3.0 BETA (008)" ;
my $maxNestingLevel = 10 ; # for relations

my $nodeId ;		# variables for reading nodes
my $nodeUser ;
my $nodeLat ;
my $nodeLon ;
my @nodeTags ;
my $aRef1 ;
my $aRef2 ;

my $wayId ;		# variables for reading ways
my $wayUser ;
my @wayNodes ;
my @wayTags ;

my $relationId ;	# variables for reading relations
my $relationUser ;
my @relationMembers ;
my @relationTags ;

my %lon = () ; my %lat = () ;	# all node positions

my %neededNodesHash = () ;	# will be used to load only needed data
my %neededWaysHash = () ;	# will be used to load only needed data

my %wayNodesHash = () ;	# nodes forming a way
my %relationWays = () ;	# ways contained (first directly, later also indirect by relation reference) in relation
my %relationRelations = () ;	# relations contained in relation (referenced)
my %validRelation = () ;	# checked and valid
my %selectedRelation = () ;	# can be used for evaluation, selected
my %relationName = () ;		# relation data
my %relationType = () ;		# relation data
my %relationBoundary = () ;	# relation data
my %relationLength = () ;	# relation data, original
my %relationAdminLevel = () ;	# relation data
my %relationOpenWays = () ;
my %relationClosedWays = () ;

my %relationPolygonsClosed = () ;	# relation polygons CLOSED
my %relationPolygonsOpen = () ;		# relation original polygons open
my %relationPolygonSimplified = () ;	# relation simplified polygon
my %relationPolygonResized = () ;	# relation resized polygon

my %relationIsIn = () ; 	# lists boundaries this relation is inside
my %relationSize = () ; 	# area as returned by math::polygon->area (no projection applied, so no real value! used only to sort is_ins)
my %relationSegments = () ;	# 
my %relationOpen = () ;	# 
my %relationWaysValid = () ;	# 

my $relationCount = 0 ;		# total
my $wayCount = 0 ; 
my $invalidWayCount = 0 ; 
my %invalidWays ;		# node count < 2, osmcut, osmosis, error...
my $adminInvalidCount = 0 ;	# how many relations are not used due to admin restriction

# command line things
my $optResult ;
my $verbose = "" ;
my $adminLevelOpt = "" ;
my $polyOpt = "" ;
my $hirarchyOpt = 0 ;
my $simplifyOpt = "" ;
my $debugOpt = "" ;
my $picOpt = "" ;
my $allPicsOpt = "" ;
my $picSize = 1024 ; # default pic size longitude in pixels
my $resizeOpt = "" ;
my $resizeFactor = 1.05 ; # 5% bigger default
my $osmName = "" ; 
my $htmlName = "" ; my $htmlFile ;
my $csvName = "" ; my $csvFile ;
my $polyBaseName = "" ;
my $polyName = "" ; my $polyFile ;

# defaults for simplify
my $simplifySlope = 0.001 ; # IN DEGREES, ~100m
my $simplifySame = 0.002 ; # IN DEGREES, ~200m
my $simplifyNpk = 2 ;    # max nodes per kilometer for simplified polygon


$optResult = GetOptions ( 	"in=s" 		=> \$osmName,		# the in file, mandatory
				"html=s"	=> \$htmlName,		# output file html, mandatory ([dir/]*.htm)
				"csv=s" 	=> \$csvName,		# output file csv, mandatory ([dir/]*.csv)
				"poly" 		=> \$polyOpt,		# option to create poly files, then give polyBaseName
				"polybase:s" 	=> \$polyBaseName,	# base filename for poly files. relId is appended. also used for pic names. [dir/]name 
				"simplify"	=> \$simplifyOpt,	# should simplified polygons be used?
				"slope:f" 	=> \$simplifySlope,	# simplify (Math::Polygon). distance in DEGREES. With three points X(n),X(n+1),X(n+2), the point X(n+1) will be removed if the length of the path over all three points is less than slope longer than the direct path between X(n) and X(n+2)
				"same:f" 	=> \$simplifySame,	# distance (IN DEGREES) for nodes to be considered the same
				"npk:f" 	=> \$simplifyNpk,	# max nodes per km when simplifying
				"debug"		=> \$debugOpt,		
				"pics" 		=> \$picOpt,		# specifies if pictures of polygons are drawn. polybasename must be given.
				"allpics"	=> \$allPicsOpt,	# also invalid and unselected will be drawn (don't use adminlevel selection then)
				"hirarchy" 	=> \$hirarchyOpt,	# specifies if hirarchies of boundaries are calculated. don't together use with adminlevel. can/should be used with -simplify, then simplified polygons are used for building the hirarchy - much faster
				"resize"	=> \$resizeOpt,	# specifies if new resized polygon will be produced (-polygon must be specified, maybe use -factor, if -simplify is given, simplified polygon will be resized)
				"factor:f"	=> \$resizeFactor,	# specifies how much bigger the resized polygon will be
				"picsize:i"	=> \$picSize,		# specifies pic size longitude in pixels
				"adminlevel:s"	=> \$adminLevelOpt,	# specifies which boundaries to look at
				"verbose" 	=> \$verbose) ;		# turns twitter on



my $time0 = time() ;
my $time1 ;

print "\n$program $version \nfor file $osmName\n\n" ;

#if ($optResult == 0) {
#	die ("usage...\n") ;
#}

#
# PARSING RELATIONS
# after this step rudimentary data of relations is present. 
# however relation members are not yet evaluated
#
print "parsing relations...\n" ;
openOsmFile ($osmName) ;
print "- skipping nodes...\n" ;
skipNodes() ;
print "- skipping ways...\n" ;
skipWays() ;
print "- checking...\n" ;

($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
if ($relationId != -1) {
	@relationMembers = @$aRef1 ;
	@relationTags = @$aRef2 ;
}

while ($relationId != -1) {

	my $name = "" ; 
	my $type = "" ; 
	my $boundary = "" ; 
	my $landArea = "" ; 
	my $adminLevel = "" ;

	my $i ;
	# process tags
	if (scalar (@relationTags) > 0) {
		for ($i=0; $i<scalar (@relationTags); $i++) {
			if ( ${$relationTags[$i]}[0] eq "name") { $name =  ${$relationTags[$i]}[1] ; }
			if ( ${$relationTags[$i]}[0] eq "type") { $type =  ${$relationTags[$i]}[1] ; }
			if ( ${$relationTags[$i]}[0] eq "boundary") { $boundary =  ${$relationTags[$i]}[1] ; }
			if ( ${$relationTags[$i]}[0] eq "admin_level") { $adminLevel =  ${$relationTags[$i]}[1] ; }
			if ( ${$relationTags[$i]}[0] eq "land_area") { $landArea =  ${$relationTags[$i]}[1] ; }
		}
	}
	# process interesting tags. evaluate relation at all?	
	my $eval = 0 ;
	if (  ( ($boundary ne "") or ($landArea ne "") or ($adminLevel ne "") ) and 
		( ($type eq "multipolygon") or ($type eq "boundary")  )  ) { 
		$eval = 1 ; 
	}
	# process members if relation is needed and has members
	if ( ($eval == 1) and (scalar (@relationMembers) > 0) ) {
		$relationName{$relationId} = $name ;
		$relationType{$relationId} = $type ;
		$relationBoundary{$relationId} = $boundary ;
		$relationAdminLevel{$relationId} = $adminLevel ;
		@{$relationWays{$relationId}} = () ;
		@{$relationRelations{$relationId}} = () ;
		@{$relationIsIn{$relationId}} = () ;
		$validRelation{$relationId} = 1 ;
		$selectedRelation{$relationId} = 1 ;
		$relationSegments{$relationId} = 0 ;
		$relationOpen{$relationId} = 0 ;
		$relationSize{$relationId} = 0 ;
		$relationWaysValid{$relationId} = 1 ;
		if ($verbose) { print "\nfound relation id=$relationId\nname=$name\ntype=$type\nboundary=$boundary\nadminLevel=$adminLevel\nlandArea=$landArea\n" ; }
		for ($i=0; $i<scalar (@relationMembers); $i++) {
			# way?
			if ( (${$relationMembers[$i]}[0] eq "way") and 
				((${$relationMembers[$i]}[2] eq "none") or (${$relationMembers[$i]}[2] eq "outer") or (${$relationMembers[$i]}[2] eq "exclave") ) ){ 
				$neededWaysHash{${$relationMembers[$i]}[1]} = 1 ;
				push @{$relationWays{$relationId}}, ${$relationMembers[$i]}[1] ; 
			}
			# relation?
			if ( (${$relationMembers[$i]}[0] eq "relation") and 
				((${$relationMembers[$i]}[2] eq "none") or (${$relationMembers[$i]}[2] eq "outer") or (${$relationMembers[$i]}[2] eq "exclave") ) ){ 
				if (${$relationMembers[$i]}[1] == $relationId) {
					print "ERROR: relation $relationId contains itself as a member. entry discarded.\n" ;
				}
				else {
					push @{$relationRelations{$relationId}}, ${$relationMembers[$i]}[1]  ;
				}
			}
		}
	}

	#next relation
	($relationId, $relationUser, $aRef1, $aRef2) = getRelation () ;
	if ($relationId != -1) {
		@relationMembers = @$aRef1 ;
		@relationTags = @$aRef2 ;
	}
}

closeOsmFile () ;

#
# GET MORE WAYS out of referenced relations (recursive)
#
my $rel ;
foreach $rel (keys %relationWays) {
	if (scalar (@{$relationRelations{$rel}}) > 1) {
		if ($verbose) { print "get relations for relation $rel\n" ; }
		my (@newWays) = getWays ($rel, 0, @{$relationRelations{$rel}}) ;
		push @{$relationWays{$rel}}, @newWays ;
	}
}
# now %relationWays contain all needed (recursive) ways of a boundary

#
# PARSE WAYS FOR NODES
#
print "\nparsing ways...\n" ;
openOsmFile ($osmName) ;
print "- skipping nodes...\n" ;
skipNodes() ;
print "- reading ways...\n" ;

($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {	
	if (scalar (@wayNodes) >= 2) {
		if (defined ($neededWaysHash{$wayId} ) ) {
			$wayCount++ ;
			@{$wayNodesHash{$wayId}} = @wayNodes ;
			foreach (@wayNodes) { $neededNodesHash{$_} = 1 ; }
			$invalidWays{$wayId} = 0 ;
		}
	}
	else {
		# an invalid way itself is no problem first. it will lead to a gap in a boundary if used however...
		#if ($verbose) { print "ERROR: invalid way (one node only): ", $wayId, "\n" ; }
		$invalidWayCount++ ;
		$invalidWays{$wayId} = 1 ;
	}

	# next way
	($wayId, $wayUser, $aRef1, $aRef2) = getWay () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;

if ($verbose) { print "\nthere are $invalidWayCount invalid ways\n\n" ; }

#
# PARSE NODES FOR POSITIONS
#
print "\nparsing nodes...\n" ;
openOsmFile ($osmName) ;

#foreach (@neededNodes) { $neededNodesHash{$_} = 1 ; }

($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}

while ($nodeId != -1) {
	if (defined ($neededNodesHash{$nodeId})) { 
		$lon{$nodeId} = $nodeLon ; 
		$lat{$nodeId} = $nodeLat ;
	}

	# next
	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}
print "done.\n" ;

#
# STATS
#
print "\n", scalar (keys %relationName), " relations read into memory.\n" ;
print scalar (keys %wayNodesHash), " ways read into memory.\n" ;
print scalar (keys %lon), " nodes read into memory.\n\n" ;

# 
# CHECK FOR VALID BOUNDARIES
#
print "check for valid boundaries...\n" ;
my $valid = 0 ; my $invalid = 0 ; 
foreach $rel (keys %relationWays) {

	my $way ;
	my $waysValid = 1 ;

	# if the relation ain't got a single way...
	if (scalar (@{$relationWays{$rel}}) == 0)  { 
		$waysValid = 0 ;
		$relationWaysValid{$rel} = 0 ;
		if ($verbose eq "1") { print "INVALID relation $rel due to no ways\n" ; }
	}

	# if the boundary contains an invalid way. chances for success are low :-)
	foreach $way (@{$relationWays{$rel}}) {
		if ($invalidWays{$way} == 1) { 
			$waysValid = 0 ; 
			$relationWaysValid{$rel} = 0 ;
			if ($verbose eq "1") { print "INVALID RELATION id=$rel, name=$relationName{$rel} due to invalid way $way\n" ; }
		}
	}

	# check for multiple usage of ways. checkSegments doesn't like that.
	my (@temp) = sort (@{$relationWays{$rel}}) ; my $i ;
	for ($i=0; $i<$#temp; $i++) {
		if ($temp[$i] == $temp[$i+1]) {
			print "ERROR RELATION id=$rel name=$relationName{$rel} contains way $temp[$i] twice\n" ;
			$waysValid = 0 ;
			$relationWaysValid{$rel} = 0 ;
		} 
	}

	# if we do have ways...
	if (scalar @{$relationWays{$rel}} > 0) {
		my $segments = 0 ; my $open = 0 ; my @waysClosed = () ; my @waysOpen = () ;
		if ($waysValid == 1) {
			if ($verbose) { print "call checksegments rel = $rel --- ways = @{$relationWays{$rel}}\n" ; } 
			# now let's see if we can build closed ways out of all these ways...
			my $refClosed ; my $refOpen ;
			($segments, $open, $refClosed, $refOpen) = checkSegments4 ( @{$relationWays{$rel}} ) ; 
			@{$relationOpenWays{$rel}} = @$refOpen ;
			@{$relationClosedWays{$rel}} = @$refClosed ;
		}

		if ( ($open == 0) and ($waysValid == 1) ) {
			$valid ++ ;
			$validRelation {$rel} = 1 ;
			$relationSegments{$rel} = $segments ;
			$relationOpen{$rel} = $open ;
			if ($verbose) { print "complete and all segments closed: relation $rel, name=$relationName{$rel}\n" ; }
		}
		else {
			$invalid ++ ;
			$validRelation {$rel} = 0 ; 
			$relationSegments{$rel} = $segments ;
			$relationOpen{$rel} = $open ;
			if ($verbose eq "1") { print "INVALID RELATION id=$rel, name=$relationName{$rel}, segments=$segments, open=$open, waysValid=$waysValid\n" ; }
		}
	}
	else {
		$invalid ++ ;
		$validRelation {$rel} = 0 ;
		print "INVALID RELATION id=$rel, no ways given.\n" ;
		$relationWaysValid{$rel} = 0 ;
	}

	# check for admin level given as option
	if (($adminLevelOpt ne "") and ($adminLevelOpt ne $relationAdminLevel{$rel})) {
		$selectedRelation {$rel} = 0 ;
		$adminInvalidCount++ ;
	}
}
print "done.\n" ;
print "\nTOTAL $valid valid relations, $invalid invalid relations.\n" ;
print "$adminInvalidCount relations selected by admin level.\n" ;
print "REMAINING for evaluation: ", $valid - $adminInvalidCount, " relations\n\n" ;

# 
# CHECK IF NEEDED NODES COULD BE FOUND
# 
print "checking if all needed nodes could be found in osm file...\n" ;
my $nodesMissing = 0 ; my $node ;
foreach $node (keys %neededNodesHash) {
	if ( (! (defined ($lon{$node}))) or (!(defined ($lat{$node}))) ) {
		print "ERROR: lon/lat for node $node missing. node not found or not valid in osm file.\n" ;
		$nodesMissing = 1 ; my $way ;
		foreach $way (keys %wayNodesHash) {
			my $n2 ;
			foreach $n2 (@{$wayNodesHash{$way}}) {
				if ($node == $n2) {
					print "       node used in way $way\n" ;
				}
			}
		}
	}
}

# 
# CHECK IF NEEDED WAYS COULD BE FOUND
# 
print "checking if all needed ways could be found in osm file...\n" ;
my $waysMissing = 0 ; my $way ;
foreach $way (keys %neededWaysHash) {
	if ( ! (defined ( @{$wayNodesHash{$way}} ) ) ) {
		if ($invalidWays{$way}) {
			print "WARNING way $way invalid in osm file.\n" ;
		}
		else {
			print "ERROR: nodes for way $way missing. way not found in osm file.\n" ;
			$waysMissing = 1 ;
			foreach $rel (keys %relationName) {
				my $w2 ;
				foreach $w2 (@{$relationWays{$rel}}) {
					if ($way == $w2) {
						print "       way used in relation $rel (directly or indirectly).\n" ;
					}
				}
			}
		}
	}
}

if ($nodesMissing == 1 ) { 
	print "ERROR: at least one needed node missing in osm file.\n" ; 
}
else {
	print "all needed nodes found.\n" ;
}
if ($waysMissing == 1 ) { 
	print "ERROR: at least one needed way missing in osm file.\n" ; 
}
else {
	print "all needed ways found.\n" ;
}
if ( ($nodesMissing == 1) or ($waysMissing == 1) )  {
	die ("ERROR: at least one needed node or way missing.\n")
}
print "done (node and way check).\n" ;

# 
# CALC LENGTH OF RELATIONS, 
# CALC SIMPLIFIED AND RESIZED WAY IF NEEDED
#
print "calc length, build polygons, (simplify, resize)...\n" ; 
foreach $rel (keys %relationWays) {
	if ( $selectedRelation{$rel} ) {	

		if ($debugOpt eq "1") { print "  - relId: $rel\n" ;}

		my $wayNodes ;
		my $length = 0 ;
		my $i ;
		my @way ;

		foreach $wayNodes (@{$relationOpenWays{$rel}}) {
			@way = @{$wayNodes} ;
			for ($i = 0; $i<$#way; $i++) {
				$length += distance ($lon{$way[$i]}, $lat{$way[$i]}, $lon{$way[$i+1]}, $lat{$way[$i+1]}) ;
			}
		}

		foreach $wayNodes (@{$relationClosedWays{$rel}}) {
			@way = @{$wayNodes} ;
			for ($i = 0; $i<$#way; $i++) {
				$length += distance ($lon{$way[$i]}, $lat{$way[$i]}, $lon{$way[$i+1]}, $lat{$way[$i+1]}) ;
			}
		}
		$relationLength{$rel} = int ($length * 100) / 100 ;

		if ($polyOpt eq "1" ) {

			foreach $way ( @{$relationClosedWays{$rel}} ) {
				my @poly = () ; my $node ;
				foreach $node ( @{$way} ) {
					push (@poly, [$lon{$node}, $lat{$node}]) ;
				}
				my ($p) = Math::Polygon->new(@poly) ;
				push @{$relationPolygonsClosed{$rel}}, $p ;
				$relationSize{$rel} += $p->area ;
			}

			if ($debugOpt eq "1") { print "    - create\n" ;}
			foreach $way ( @{$relationOpenWays{$rel}} ) {
				my @poly = () ; my $node ;
				foreach $node ( @{$way} ) {
					push (@poly, [$lon{$node}, $lat{$node}]) ;
				}
				my ($p) = Math::Polygon->new(@poly) ;
				push @{$relationPolygonsOpen{$rel}}, $p ;
			}

			if ($simplifyOpt eq "1") { 
				if ($debugOpt eq "1") { print "    - simplify\n" ;}
				foreach my $p ( @{$relationPolygonsClosed{$rel}} ) {

					# calc poly length $pl
					my $i ; my $pl = 0 ; 
					my (@coords) = $p->points ;
					for ($i=0; $i<$#coords; $i++) {
						$pl += distance ($coords[$i]->[0], $coords[$i]->[1], $coords[$i+1]->[0], $coords[$i+1]->[1]) ;
					}
					my ($maxNodes) = int ($pl * $simplifyNpk ) ; 
					if ($maxNodes < 10) { $maxNodes = 10 ; }
					if ($debugOpt eq "1") { print "      - max nodes allowed: $maxNodes\n" ;}
					if ($debugOpt eq "1") { print "      - number nodes p: ", $p->nrPoints, "\n" ;}
					my ($ps) = $p->simplify (max_points => $maxNodes, same => $simplifySame, slope => $simplifySlope ) ;
					push @{$relationPolygonSimplified{$rel}}, $ps ; 
					my ($percent) = int ($ps->nrPoints / $p->nrPoints * 100 ) ;
					if ($verbose) { print "relation $rel: new size of polygon=", $percent, "%\n" ; } 
				}
			} # simplify

			if ($resizeOpt eq "1") { 
				if ($debugOpt eq "1") { print "    - resize\n" ;}
				if ($simplifyOpt eq "1") { 
					foreach my $p ( @{$relationPolygonSimplified{$rel}} ) {
						my ($x, $y) = center( $p ) ;
						my ($pr) = $p->resize (center => [$x, $y], scale => $resizeFactor) ;
						push @{$relationPolygonResized{$rel}}, $pr ; 
					}
				}
				else {
					foreach my $p ( @{$relationPolygonsClosed{$rel}} ) {
						my ($x, $y) = center( $p ) ;
						my ($pr) = $p->resize (center => [$x, $y], scale => $resizeFactor) ;
						push @{$relationPolygonResized{$rel}}, $pr ; 
					}
				}
			} # resize

		}
	}
	else {
		$relationLength{$rel} = 0 ;
	}
}
print "done.\n" ; 

# 
# WRITE POLY FILES IF SPECIFIED
#
if ( ($polyBaseName ne "") and ($polyOpt eq "1") ) {
	print "write poly files...\n" ; 
	foreach $rel (keys %relationWays) {
		if ( ($selectedRelation{$rel}) and ($validRelation{$rel}) ) {

			my @way ; my $polyFileName = "" ; my @points = () ; my $text = "" ;
			if ($verbose) { print "write poly file for relation $rel $relationName{$rel} (", scalar (@points) , " nodes) ...\n" ; }

			if ($simplifyOpt eq "1") { 
				$polyFileName = $polyBaseName . ".Simplified." . $rel . ".poly" ;
				$text = " (SIMPLIFIED)" ;
				open ($polyFile, ">", $polyFileName) or die ("can't open poly output file") ;
				print $polyFile $relationName{$rel}, $text, "\n" ; # name
				my ($num) = 0 ;
				foreach my $p (@{$relationPolygonSimplified{$rel}}) {
					$num++ ;
					print $polyFile "$num\n" ;
					foreach my $pt ( $p->points ) {
						printf $polyFile "   %E   %E\n", $pt->[0], $pt->[1] ;
					}
					print $polyFile "END\n" ;
				}
				print $polyFile "END\n" ;
				close ($polyFile) ;
			}

			if ($resizeOpt eq "1") { 
				$polyFileName = $polyBaseName . ".Resized." . $rel . ".poly" ;
				$text = " (RESIZED)" ;
				if ($simplifyOpt eq "1") { $text = " (SIMPLIFIED/RESIZED)" ; }
				open ($polyFile, ">", $polyFileName) or die ("can't open poly output file") ;
				print $polyFile $relationName{$rel}, $text, "\n" ; # name
				my ($num) = 0 ;
				foreach my $p (@{$relationPolygonResized{$rel}}) {
					$num++ ;
					print $polyFile "$num\n" ;
					foreach my $pt ( $p->points ) {
						printf $polyFile "   %E   %E\n", $pt->[0], $pt->[1] ;
					}
					print $polyFile "END\n" ;
				}
				print $polyFile "END\n" ;
				close ($polyFile) ;
			}

			$polyFileName = $polyBaseName . "." . $rel . ".poly" ;

			open ($polyFile, ">", $polyFileName) or die ("can't open poly output file") ;
			print $polyFile $relationName{$rel}, "\n" ; # name
			my ($num) = 0 ;
			foreach my $p (@{$relationPolygonsClosed{$rel}}) {
				$num++ ;
				print $polyFile "$num\n" ;
				foreach my $pt ( $p->points ) {
					printf $polyFile "   %E   %E\n", $pt->[0], $pt->[1] ;
				}
				print $polyFile "END\n" ;
			}
			print $polyFile "END\n" ;
			close ($polyFile) ;
		}
	}
	print "done.\n" ; 
}

# 
# WRITE PICS IF SPECIFIED
#
if ( ($polyBaseName ne "") and ($picOpt eq "1") ) {
	print "write picture files...\n" ; 
	foreach $rel (keys %relationWays) {
		if ( ( ($validRelation{$rel}) or ($allPicsOpt eq "1") ) and ($selectedRelation{$rel} ) ) {
			drawPic ($rel) ;
		}
	}
	print "done.\n" ; 
}
#
# BUILD AND PRINT HIRARCHIES
#
if ($hirarchyOpt eq "1") {
	print "building hirarchies...\n" ;
	my $rel ; my $rel1 ; my $rel2 ; 
	my $count = 0 ; my ($max) = 0 ;
	# calc max number of checks
	foreach $rel1 (keys %relationName) {
		if ( ($validRelation{$rel1}) and ($selectedRelation{$rel1}) ) { $max++ ; }
	}
	$max = int ($max * $max / 2 ) ;

	foreach $rel1 (keys %relationName) {
		foreach $rel2 (keys %relationName) {
			if ( ($rel1 < $rel2) and ($validRelation{$rel1}) and ($validRelation{$rel2}) and ($selectedRelation{$rel1}) and ($selectedRelation{$rel2}) ) {
				$count++ ;
				if ( ($count % 100000) == 0 ) { 
					my ($percent) = int ($count / $max * 100) ;
					print "  $percent % is_in checks done...\n" ; 
				}
				my $res ;
				if ($simplifyOpt eq "1") {
					$res = isIn ( \@{$relationPolygonSimplified{$rel1}}, \@{$relationPolygonSimplified{$rel2}} ) ;
				}
				else {
					$res = isIn ( \@{$relationPolygonsClosed{$rel1}}, \@{$relationPolygonsClosed{$rel2}} ) ;
				}
				if ($res == 2) { push @{$relationIsIn{$rel2}}, $rel1 ; }
				if ($res == 1) { push @{$relationIsIn{$rel1}}, $rel2 ; }
			}
		}
	}
	print "\n$count is_in checks done.\n" ; 

	my ($csvNameHirarchy) = $csvName ;
	my ($htmlNameHirarchy) = $htmlName ;
	$csvNameHirarchy =~  s/.csv/.hirarchy.csv/ ;
	$htmlNameHirarchy =~ s/.htm/.hirarchy.htm/ ;

	open ($htmlFile, ">", $htmlNameHirarchy) or die ("can't open html output file") ;
	open ($csvFile, ">", $csvNameHirarchy) or die ("can't open csv output file") ;

	printHTMLHeader ($htmlFile, "boundaries by gary68 - hirarchy") ;
	print $csvFile "Line;RelationId;Name;Type;Boundary;AdminLevel;is_in\n" ;
	print $htmlFile "<h1>boundary.pl by gary68 - hirarchy</h1>" ;
	print $htmlFile "<p>Version ", $version, "</p>\n" ;
	print $htmlFile "<H2>Statistics</H2>\n" ;
	print $htmlFile "<p>", stringFileInfo ($osmName), "</p>\n" ;
	print $htmlFile "<h2>Data</h2>" ;
	printHTMLTableHead ($htmlFile) ;
	printHTMLTableHeadings ($htmlFile, ("Line", "RelationId", "Name", "Type", "Boundary", "AdminLevel", "is_in")) ;

	my $line = 0 ;

	foreach $rel (keys %relationName) {
		if ( ($validRelation{$rel}) and ($selectedRelation{$rel}) ) {
			
			my @is_in = () ;
			foreach my $r2 ( @{$relationIsIn{$rel}} ) {
				push @is_in, [ $r2, $relationSize{$r2} ] ;
			}
			@is_in = sort { $a->[1] <=> $b->[1] } (@is_in) ;
			
			$line++ ;
			print $csvFile $line, ";" ;
			print $csvFile $rel, ";" ;
			print $csvFile $relationName{$rel}, ";" ;
			print $csvFile $relationType{$rel}, ";" ;
			print $csvFile $relationBoundary{$rel}, ";" ;
			print $csvFile $relationAdminLevel{$rel}, ";" ;

			foreach my $r2 (@is_in) {
				print $csvFile $r2->[0], ";" ;
			}
			print $csvFile "\n" ;

			printHTMLRowStart ($htmlFile) ;
			printHTMLCellRight ($htmlFile, $line) ;
			printHTMLCellRight ($htmlFile, historyLink ("relation", $rel) . "(osm) " . analyzerLink($rel) . "(analyzer)" ) ;
			printHTMLCellLeft ($htmlFile, $relationName{$rel}) ;
			printHTMLCellLeft ($htmlFile, $relationType{$rel}) ;
			printHTMLCellLeft ($htmlFile, $relationBoundary{$rel}) ;
			printHTMLCellLeft ($htmlFile, $relationAdminLevel{$rel}) ;

			print $htmlFile "<td align=\"left\">\n" ;
			foreach my $r2 (@is_in) {
				print $htmlFile historyLink ("relation", $r2->[0]), "(osm) ", analyzerLink ($r2->[0]), "(analyzer) " ;
				print $htmlFile $relationName{$r2->[0]}, "<br>\n" ;
			}
			print $htmlFile "</td>\n" ;
			printHTMLRowEnd ($htmlFile) ;
		}
	}

	printHTMLTableFoot ($htmlFile) ;
	printHTMLFoot ($htmlFile) ;

	close ($htmlFile) ;
	close ($csvFile) ;
	print "done.\n" ;
} # hirarchy




# 
# WRITE OVERVIEW FILES, HTML and CSV
#
open ($htmlFile, ">", $htmlName) or die ("can't open html output file") ;
open ($csvFile, ">", $csvName) or die ("can't open csv output file") ;

printHTMLHeader ($htmlFile, "boundaries by gary68") ;
print $csvFile "Line;RelationId;Name;Type;Boundary;AdminLevel;Length;Nodes;NodesPerKm\n" ;
print $htmlFile "<h1>boundary.pl by gary68</h1>" ;
print $htmlFile "<p>Version ", $version, "</p>\n" ;
print $htmlFile "<H2>Statistics</H2>\n" ;
print $htmlFile "<p>", stringFileInfo ($osmName), "</p>\n" ;
print $htmlFile "<h2>Data</h2>" ;
printHTMLTableHead ($htmlFile) ;
printHTMLTableHeadings ($htmlFile, ("Line", "RelationId", "Name", "Type", "Boundary", "AdminLevel", "Length", "Nodes", "NodesPerKm")) ;

my $line = 0 ;
foreach $rel (keys %relationWays) {
	if ( ( $selectedRelation{$rel} ) and ($validRelation{$rel}) ) {

		$line++ ;
		my $pts = 0 ;
		foreach my $p ( @{$relationPolygonsClosed{$rel}}, @{$relationPolygonsOpen{$rel}}) {
			$pts += $p->nrPoints ;
		}

		my $nodesPerKm = int ( $pts / $relationLength{$rel} * 100 ) / 100 ;
		print $csvFile $line, ";" ;
		print $csvFile $rel, ";" ;
		print $csvFile "\"", $relationName{$rel}, "\";" ;
		print $csvFile $relationType{$rel}, ";" ;
		print $csvFile $relationBoundary{$rel}, ";" ;
		print $csvFile $relationAdminLevel{$rel}, ";" ;
		print $csvFile $relationLength{$rel}, ";" ;
		print $csvFile $pts, ";" ;
		print $csvFile $nodesPerKm, "\n" ;

		printHTMLRowStart ($htmlFile) ;
		printHTMLCellRight ($htmlFile, $line) ;
		printHTMLCellRight ($htmlFile, historyLink("relation", $rel) . "(osm) " .analyzerLink($rel) . "(analyzer)" ) ;
		printHTMLCellLeft ($htmlFile, $relationName{$rel}) ;
		printHTMLCellLeft ($htmlFile, $relationType{$rel}) ;
		printHTMLCellLeft ($htmlFile, $relationBoundary{$rel}) ;
		printHTMLCellRight ($htmlFile, $relationAdminLevel{$rel}) ;
		printHTMLCellRight ($htmlFile, $relationLength{$rel}) ;
		printHTMLCellRight ($htmlFile, $pts ) ;
		printHTMLCellRight ($htmlFile, $nodesPerKm) ;
		printHTMLRowEnd ($htmlFile) ;

	}
}
printHTMLTableFoot ($htmlFile) ;

print $htmlFile "<h2>Invalid Relations</h2>\n" ;
print $htmlFile "<p>List reflects the moment the *.osm file was created and a relation may be invalid because one or more ways were clipped in the process of creating the *.osm file.</p>\n" ;
printHTMLTableHead ($htmlFile) ;
printHTMLTableHeadings ($htmlFile, ("RelationId", "Name", "#segments", "#open segments", "ways valid")) ;
foreach $rel (keys %relationWays) {
	if (! $validRelation{$rel}) {
		printHTMLRowStart ($htmlFile) ;
		printHTMLCellRight ($htmlFile, historyLink("relation", $rel) . "(osm) " .analyzerLink($rel) . "(analyzer)" ) ;
		printHTMLCellLeft ($htmlFile, $relationName{$rel}) ;
		printHTMLCellRight ($htmlFile, $relationSegments{$rel} ) ;
		printHTMLCellRight ($htmlFile, $relationOpen{$rel} ) ;
		printHTMLCellRight ($htmlFile, $relationWaysValid{$rel} ) ;
		printHTMLRowEnd ($htmlFile) ;
	}
}
printHTMLTableFoot ($htmlFile) ;

printHTMLFoot ($htmlFile) ;
close ($htmlFile) ;
close ($csvFile) ;

#print "\n$program finished.\n\n";
print "\n", $program, " ", $osmName, " FINISHED after ", stringTimeSpent (time - $time0), "\n\n" ;





sub checkSegments4 {
	# sub builds segments for given set of ways. 
	# returns number of segments, number of open segments and closed and open segments as ways (array refs)
	my (@ways) = @_ ;
	my $way ; my $node ;
	my $segments = 0 ; my $openSegments = 0 ;
	my $found = 1 ;
	my $way1 ; my $way2 ;
	my $endNodeWay2 ;	my $startNodeWay2 ;
	my %starts = () ; my %ends = () ;
	my %wayStart = () ; my %wayEnd = () ;
	my %wayNodes = () ;

	#init
	foreach $way (@ways) {
		push @{$starts{$wayNodesHash{$way}[0]}}, $way ;
		push @{$ends{$wayNodesHash{$way}[-1]}}, $way ;
		$wayStart{$way} = $wayNodesHash{$way}[0] ;
		$wayEnd{$way} = $wayNodesHash{$way}[-1] ;
		@{$wayNodes{$way}} = @{$wayNodesHash{$way}} ; # complete...
		#print "    cs way = $way --- nodes = @{$wayNodesHash{$way}}\n" ;
	}

	while ($found == 1) {
		$found = 0 ;

		# check start/start
		loop1:
		foreach $node (keys %starts) {

			# if node with more than 1 connecting way...
			if (scalar (@{$starts{$node}}) > 1) {
				$way1 = ${$starts{$node}}[0] ; $way2 = ${$starts{$node}}[1] ;
				#print "merge start/start $way1 and $way2 at node $node\n" ;

				# complete
				@{$wayNodes{$way1}} = ( reverse ( @{$wayNodes{$way2}}[1..$#{$wayNodes{$way2}}] ), @{$wayNodes{$way1}} ) ;

				$endNodeWay2 = $wayEnd{$way2} ;
				#print "end node way2 = $endNodeWay2\n" ;

				# way1 gets new start: end way2
				push @{$starts{ $endNodeWay2 }}, $way1 ;
				$wayStart{$way1} = $endNodeWay2 ;

				# remove end way2
				if (scalar (@{$ends{$endNodeWay2}}) == 1) {
					delete $ends{$endNodeWay2} ;
					#print "$endNodeWay2 removed from end hash\n" ;
				}
				else {
					@{$ends{$endNodeWay2}} = removeElement ($way2, @{$ends{$endNodeWay2}}) ;
					#print "way $way2 removed from node $endNodeWay2 from end hash\n" ;
				}
				
				# remove way2
				delete $wayEnd{$way2} ;
				delete $wayStart{$way2} ;
				delete $wayNodes{$way2} ;

				# remove connecting starts
				if (scalar @{$starts{$node}} == 2) {
					delete $starts{$node} ;
					#print "$node removed from start hash\n" ;
				}
				else {
					@{$starts{$node}} = @{$starts{$node}}[2..$#{$starts{$node}}] ;
					#print "first two elements removed from start hash node = $node\n" ;
				}
				#print "\n" ;
				$found = 1 ; 
				last loop1 ;
			}
		}

		# check end/end
		if (!$found) {
			loop2:
			foreach $node (keys %ends) {

				# if node with more than 1 connecting way...
				if (scalar @{$ends{$node}} > 1) {
					$way1 = ${$ends{$node}}[0] ; $way2 = ${$ends{$node}}[1] ;
					#print "merge end/end $way1 and $way2 at node $node\n" ;
	
					# complete
					@{$wayNodes{$way1}} = ( @{$wayNodes{$way1}}, reverse ( @{$wayNodes{$way2}}[0..$#{$wayNodes{$way2}}-1] ) )  ;

					$startNodeWay2 = $wayStart{$way2} ;
					#print "start node way2 = $startNodeWay2\n" ;
	
					# way1 gets new end: start way2
					push @{$ends{ $startNodeWay2 }}, $way1 ;
					$wayEnd{$way1} = $startNodeWay2 ;
	
					# remove start way2
					if (scalar (@{$starts{$startNodeWay2}}) == 1) {
						delete $starts{$startNodeWay2} ;
						#print "$startNodeWay2 removed from start hash\n" ;
					}
					else {
						@{$starts{$startNodeWay2}} = removeElement ($way2, @{$starts{$startNodeWay2}}) ;
						#print "way $way2 removed from node $startNodeWay2 from start hash\n" ;
					}
				
					# remove way2
					delete $wayEnd{$way2} ;
					delete $wayStart{$way2} ;
					delete $wayNodes{$way2} ;

					# remove connecting ends
					if (scalar @{$ends{$node}} == 2) {
						delete $ends{$node} ;
						#print "$node removed from end hash\n" ;
					}
					else {
						@{$ends{$node}} = @{$ends{$node}}[2..$#{$ends{$node}}] ;
						#print "first two elements removed from end hash node = $node\n" ;
					}
					#print "\n" ;
					$found = 1 ; 
					last loop2 ;
				}
			}
		}


		# check start/end
		if (!$found) {
			my $wayFound = 0 ;
			loop3:
			foreach $node (keys %starts) {
				if (exists ($ends{$node})) {
					#look for different! ways
					my (@startingWays) = @{$starts{$node}} ;
					my (@endingWays) = @{$ends{$node}} ;
					my $w1 ; my $w2 ;
					loop4:
					foreach $w1 (@startingWays) {
						foreach $w2 (@endingWays) {
							if ($w1 != $w2) {
								$wayFound = 1 ;
								$way1 = $w1 ; 
								$way2 = $w2 ; # merge w1 and w2
								#print "start/end: merge ways $way1 and $way2 connected at node $node\n" ;
								last loop4 ;
							}
						}
					} # look for ways
					if ($wayFound) {
						#print "way $way1 start $wayStart{$way1} end $wayEnd{$way1}\n" ;
						#print "way $way2 start $wayStart{$way2} end $wayEnd{$way2}\n" ;

						# way1 gets new start: start way2
						$wayStart{$way1} = $wayStart{$way2} ;
						my ($way2StartNode) = $wayStart{$way2} ;

						# complete
						@{$wayNodes{$way1}} = ( @{$wayNodes{$way2}}[0..$#{$wayNodes{$way2}}-1], @{$wayNodes{$way1}} ) ;

						push @{$starts{$way2StartNode}}, $way1 ;
						#print "way $way1 added to starts for node $way2StartNode\n" ;

						# remove start way1
						if (scalar (@{$starts{$node}}) == 1) {
							delete $starts{$node} ;
							#print "$way1 removed from start hash for node $node\n" ;
						}
						else {
							@{$starts{$node}} = removeElement ($way1, @{$starts{$node}}) ;
							#print "$way1 removed from start hash for node $node\n" ;
						}

						#remove end way2
						if (scalar (@{$ends{$node}}) == 1) {
							delete $ends{$node} ;
							#print "$way2 removed from end hash for node $node\n" ;
						}
						else {
							@{$ends{$node}} = removeElement ($way2, @{$ends{$node}}) ;
							#print "$way2 removed from end hash for node $node\n" ;
						}
						#remove start way2
						if (scalar (@{$starts{$way2StartNode}}) == 1) {
							delete $starts{$way2StartNode} ;
							#print "$way2 removed from start hash for node $way2StartNode\n" ;
						}
						else {
							@{$starts{$way2StartNode}} = removeElement ($way2, @{$starts{$way2StartNode}}) ;
							#print "$way2 removed from start hash for node $way2StartNode\n" ;
						}

						# remove way2
						delete $wayEnd{$way2} ;
						delete $wayStart{$way2} ;
						delete $wayNodes{$way2} ;
						#print "way $way2 removed from waystart and wayend hashes\n" ;

						#print "\n" ;
						$found = 1 ; 
						last loop3 ;
					}
				}
			}
		}
	}

	# evaluation
	foreach $way (keys %wayStart) {
		#print "way $way start $wayStart{$way} end $wayEnd{$way}\n" ;
		if ($wayStart{$way} != $wayEnd{$way}) {
			$openSegments++ ;
		}
	}

	my @openWays = () ;
	my @closedWays = () ;
	foreach $way1 (keys %wayStart) {
		if ( ${$wayNodes{$way1}}[0] == ${$wayNodes{$way1}}[-1] ) {
			push @closedWays, [ @{$wayNodes{$way1}} ] ;
		}
		else {
			push @openWays, [ @{$wayNodes{$way1}} ] ;
		}
	}

	return (scalar (keys %wayStart), $openSegments, \@closedWays, \@openWays) ;
}

sub removeElement {
	# sub removes a single value (once) from an array
	my ($element, @array) = @_ ;
	my @arrayNew = () ;
	my $pos = -1 ; my $i ;
	for ($i=0; $i<=$#array; $i++) { if ($array[$i] == $element) { $pos = $i ; } }
	if ($pos != -1) {
		if ($pos == 0) {
			@arrayNew = @array[1..$#array] ;
		}
		if ($pos == $#array) {
			@arrayNew = @array[0..$#array-1] ;
		}
		if ( ($pos > 0) and ($pos < $#array) ) {
			@arrayNew = @array[0..$pos-1, $pos+1..$#array] ;
		}
	}
	return @arrayNew ;
}


sub drawPic {
	# draws simple picture of relation/boundary. original and possibly simplified/resized boundary.
	my ($rel) = shift ;
	my $buffer = 0.1 ;
	my $lonMin = 999 ; my $latMin = 999 ; my $lonMax = -999 ; my $latMax = -999 ; 
	my $node ;
	my $p ; my $pt ; 

	foreach $p ( @{$relationPolygonsClosed{$rel}}, @{$relationPolygonsOpen{$rel}} ) {
		foreach $pt ($p->points) {
			if ($pt->[0] > $lonMax) { $lonMax = $pt->[0] ; }
			if ($pt->[1] > $latMax) { $latMax = $pt->[1] ; }
			if ($pt->[0] < $lonMin) { $lonMin = $pt->[0] ; }
			if ($pt->[1] < $latMin) { $latMin = $pt->[1] ; }
		}
	}

	$lonMin = $lonMin - ($buffer * ($lonMax - $lonMin)) ;
	$latMin = $latMin - ($buffer * ($latMax - $latMin)) ;
	$lonMax = $lonMax + ($buffer * ($lonMax - $lonMin)) ;
	$latMax = $latMax + ($buffer * ($latMax - $latMin)) ;

	initGraph ($picSize, $lonMin, $latMin, $lonMax, $latMax) ;
	
	foreach $p (@{$relationPolygonsClosed{$rel}}) {
		my @coordinates = () ; 
		foreach $pt ($p->points) {
			push @coordinates, $pt->[0], $pt->[1] ;
		}
		drawWay ("green", 3, @coordinates) ;
	}
	foreach $p (@{$relationPolygonsOpen{$rel}}) {
		my @coordinates = () ; 
		foreach $pt ($p->points) {
			push @coordinates, $pt->[0], $pt->[1] ;
		}
		drawWay ("red", 3, @coordinates) ;
	}

	if ($simplifyOpt eq "1") {	
		foreach $p (@{$relationPolygonSimplified{$rel}}) {
			my @coordinates = () ; 
			foreach $pt ($p->points) {
				push @coordinates, $pt->[0], $pt->[1] ;
			}
			drawWay ("blue", 2, @coordinates) ;
		}
	}

	if ($resizeOpt eq "1") {	
		foreach $p (@{$relationPolygonResized{$rel}}) {
			my @coordinates = () ; 
			foreach $pt ($p->points) {
				push @coordinates, $pt->[0], $pt->[1] ;
			}
			drawWay ("black", 2, @coordinates) ;
		}
	}

	drawHead ($program . " ". $version . " by Gary68" . " RelId = " . $rel . " name = " . $relationName{$rel}, "black", 3) ;
	drawFoot ("data by openstreetmap.org" . " " . $osmName . " " .ctime(stat($osmName)->mtime), "gray", 3) ;
	drawLegend (3, "Resized", "black", "Open", "red", "Simplified", "blue", "Original", "green") ;
	drawRuler ("black") ;
	writeGraph ($polyBaseName . "." . $rel . ".png") ;
}

sub getWays {
	# sub gets all ways of given relation and all ways of referenced relations, recursive
	my ($startingRelation, $level, @relations) = @_ ;
	my @result = () ;

	if ($verbose) { print "getways called for starting relation $startingRelation with members: @relations\n" ; }
	if ($level > $maxNestingLevel) { 
		die ("ERROR: relations nested deeper than $maxNestingLevel levels, maybe a loop? starting relation is $startingRelation.\n") ;
	}

	my $rel ;	
	foreach $rel (@relations) {
		if (defined ($relationName{$rel})) {
			push @result, @{$relationWays{$rel}} ;
			if (scalar (@{$relationRelations{$rel}}) > 0) {
				my $rel2 ;
				foreach $rel2 (@{$relationRelations{$rel}}) { # could be done without loop, pass whole array...
					push @result, getWays ($startingRelation, $level+1, $rel2) ;
				}
			}
		}
		else {
			print "ERROR. Nested relation id=$rel not found or not tagged correctly.\n" ;
		}
	}
	if ($verbose) { print "  getways result: @result\n" ; }
	return @result ;
}

sub isIn {
	# check if polygon(s) 1 is(are) in polygon(s) 2 or vice versa
	# return 0 = neither
	#        1 = p1 is in p2
	#        2 = p2 is in p1
	my ($p1ref, $p2ref) = @_ ;

	my (@polygons1) = @$p1ref ;
	my (@polygons2) = @$p2ref ;
	my ($p1In2) = 0 ;
	my ($p2In1) = 0 ;

	# p1 in p2 ?

	second:
	foreach my $p2 (@polygons2) {
		my ($inside) = 1 ;
		first:
		foreach my $p1 (@polygons1) {
			foreach my $pt1 ($p1->points) {
				if ($p2->contains ($pt1) ) {
					# good
				}
				else {
					$inside = 0 ; last first ;
				}
			}
		}
		if ($inside == 1) {
			$p1In2 = 1 ; last second ;
		}
	}

	# p2 in p1 ?

	fourth:
	foreach my $p1 (@polygons1) {
		my ($inside) = 1 ;
		third:
		foreach my $p2 (@polygons2) {
			foreach my $pt2 ($p2->points) {
				if ($p1->contains ($pt2) ) {
					# good
				}
				else {
					$inside = 0 ; last third ;
				}
			}
		}
		if ($inside == 1) {
			$p2In1 = 1 ; last fourth ;
		}
	}


	if ($p1In2 == 1) {
		return 1 ;
	}
	elsif ($p2In1 == 1) {
		return 2 ;
	}
	else {
		return 0 ;
	}

}

sub center {
	my ($polygon) = shift ;
	my $lonSum = 0 ;
	my $latSum = 0 ;
	my $number = 0 ;

	foreach my $pt ($polygon->points) {
		$lonSum += $pt->[0] ;
		$latSum += $pt->[1] ;
		$number ++ ;
	}
	return ($lonSum/$number, $latSum/$number) ;
}
