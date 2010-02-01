#
# mapgen.pl
#
# Copyright (C) 2010, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#

# 0.03 enhanced legend, center label in areas
# 0.04 dash styles, shaping of ways, own perl module, 
#      only svg support, no more png (use i.e.inkscape for cmd line conversion
#      layers of ways; draw only really closed areas
#      getopt, embedded pdf and png creation
#
#

# TODO
# bbox? 5-10%?
# icons
# wiki page

# relations (scan, then convert to ways, preserve layers!)
# sub key/value for rules
# bg color

use strict ;
use warnings ;

use Getopt::Long ;
use OSM::osm ;
use OSM::mapgen ;

my $programName = "mapgen.pl" ;
my $usage = "mapgen.pl -in=file.osm -style=file.csv [-out=file.svg] [-size=INT] [-pdf] [-png] [-minlen=x.y] [-verbose]" ; 
my $version = "0.04" ;

# command line things
my $optResult ;
my $verbose = 0 ;
my $size = 1024 ; # default pic size longitude in pixels
my $osmName = "" ; 
my $csvName = "" ; 
my $svgName = "mapgen.svg" ; 
my $pdfOpt = 0 ;
my $pngOpt = 0 ;
my $labelMinLength = 0.1 ; # min length of street so that it will be labled / needs adjustment according to picture size


my @legend = () ;

# AREAS
my $areaIndexKey = 0 ;
my $areaIndexValue = 1 ;
my $areaIndexColor = 2 ;
my $areaIndexLegend = 3 ;
my @areas = () ;
# tag value color

# NODES
my $nodeIndexTag = 0 ;
my $nodeIndexValue = 1 ;
my $nodeIndexColor = 2 ;
my $nodeIndexThickness = 3 ;
my $nodeIndexLabel = 4 ;
my $nodeIndexLabelColor = 5 ;
my $nodeIndexLabelSize = 6 ;
my $nodeIndexLabelOffset = 7 ;
my $nodeIndexLegend = 8 ;
my @nodes = () ;
# tag value color thickness label label-color label-size label-offset


# WAYS and small AREAS
my $wayIndexTag = 0 ;
my $wayIndexValue = 1 ;
my $wayIndexColor = 2 ;
my $wayIndexThickness = 3 ;
my $wayIndexDash = 4 ;
my $wayIndexFilled = 5 ;
my $wayIndexLabel = 6 ;
my $wayIndexLabelColor = 7 ;
my $wayIndexLegend = 8 ;
my @ways = () ;
# key value color thickness fill label label-color



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

my %memNodeTags ;
my %memWayTags ;
my %memWayNodes ;
my %memRelationTags ;
my %memRelationMembers ;

my %lon ; my %lat ;

my $lonMin ; my $latMin ; my $lonMax ; my $latMax ;

my $time0 ; my $time1 ;

# get parameter

$optResult = GetOptions ( 	"in=s" 		=> \$osmName,		# the in file, mandatory
				"style=s" 	=> \$csvName,		# the style file, mandatory
				"out:s"		=> \$svgName,		# outfile name or default
				"size:i"	=> \$size,		# specifies pic size longitude in pixels
				"minlen:f"	=> \$labelMinLength,	# specifies min way len for labels
				"pdf"		=> \$pdfOpt,		# specifies if pdf will be created
				"png"		=> \$pngOpt,		# specifies if png will be created
				"verbose" 	=> \$verbose) ;		# turns twitter on


print "\n$programName $version for file $osmName\n" ;print "\n" ;
print "infile  = $osmName\n" ;
print "style   = $csvName\n" ;
print "outfile = $svgName\n" ;
print "size    = $size (pixels)\n" ;
print "minlen  = $labelMinLength (km)\n" ;
print "pdf     = $pdfOpt\n" ;
print "png     = $pngOpt\n" ;
print "verbose = $verbose\n\n" ;

# READ STYLE File
open (my $csvFile, "<", $csvName) or die ("ERROR: style file not found.") ;
my $line = <$csvFile> ;

#$line = <$csvFile> ;
#while (! grep /^\"SECTION/, $line) {
#	my ($color, $text) = ($line =~ /\"(.+)\" \"(.+)\"/ ) ;
#	# print "L $color $text\n" ; 
#	push @legend, $text ; push @legend, $color ;
#	$line = <$csvFile> ;
#}

$line = <$csvFile> ;
while (! grep /^\"SECTION/, $line) {
	my ($key, $value, $color, $legend) = ($line =~ /\"(.+)\" \"(.+)\" \"(.+)\" (\d)/ ) ;
	# print "A $key, $value, $color, $legend\n" ; 
	push @areas, [$key, $value, $color, $legend] ;
	$line = <$csvFile> ;
}
# tag value color thickness label label-color label-size label-offset
$line = <$csvFile> ;
while (! grep /^\"SECTION/, $line) {
	my ($key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelOffset, $legend) = 
		($line =~ /\"(.+)\" \"(.+)\" \"(.+)\" (\d+) \"(.+)\" \"(.+)\" (\d+) (\d+) (\d)/ ) ;
	# print "N $key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelOffset, $legend\n" ; 
	push @nodes, [$key, $value, $color, $thickness, $label, $labelColor, $labelSize, $labelOffset, $legend] ;
	$line = <$csvFile> ;
}
# key value color thickness fill label label-color
$line = <$csvFile> ;
while ( (! grep /^\"SECTION/, $line) and (defined $line) ) {
	my ($key, $value, $color, $thickness, $dash, $fill, $label, $labelColor, $legend) = 
		($line =~ /\"(.+)\" \"(.+)\" \"(.+)\" (\d+) (\d+) (\d+) \"(.+)\" \"(.+)\" (\d)/ ) ;
	# print "W $key, $value, $color, $thickness, $dash, $fill, $label, $labelColor, $legend\n" ; 
	push @ways, [$key, $value, $color, $thickness, $dash, $fill, $label, $labelColor, $legend] ;
	$line = <$csvFile> ;
}

close ($csvFile) ;

if ($verbose eq "1") {
	print "AREAS\n" ;
	foreach my $area (@areas) {
		printf "%-15s %-15s %-10s %-10s\n", $area->[0], $area->[1], $area->[2], $area->[3] ;
	}
	print "\n" ;
	print "WAYS\n" ;
	foreach my $way (@ways) {
		printf "%-20s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n", $way->[0], $way->[1], $way->[2], $way->[3], $way->[4], $way->[5], $way->[6], $way->[7], $way->[8] ;
	}
	print "\n" ;
	print "NODES\n" ;
	foreach my $node (@nodes) {
		printf "%-20s %-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n", $node->[0], $node->[1], $node->[2], $node->[3], $node->[4], $node->[5], $node->[6], $node->[7], $node->[8] ;
	}
	print "\n" ;
}

$time0 = time() ;


# STORE DATA
print "reading osm file...\n" ;

openOsmFile ($osmName) ;
($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
if ($nodeId != -1) {
	@nodeTags = @$aRef1 ;
}
while ($nodeId != -1) {

	$lon{$nodeId} = $nodeLon ;	
	$lat{$nodeId} = $nodeLat ;	
	@{$memNodeTags{$nodeId}} = @nodeTags ;

	($nodeId, $nodeLon, $nodeLat, $nodeUser, $aRef1) = getNode2 () ;
	if ($nodeId != -1) {
		@nodeTags = @$aRef1 ;
	}
}

($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
if ($wayId != -1) {
	@wayNodes = @$aRef1 ;
	@wayTags = @$aRef2 ;
}
while ($wayId != -1) {

	if (scalar (@wayNodes) > 1) {
		@{$memWayTags{$wayId}} = @wayTags ;
		@{$memWayNodes{$wayId}} = @wayNodes ;
	}
	
	($wayId, $wayUser, $aRef1, $aRef2) = getWay2 () ;
	if ($wayId != -1) {
		@wayNodes = @$aRef1 ;
		@wayTags = @$aRef2 ;
	}
}

closeOsmFile () ;



# calc area of pic and init

$lonMin = 999 ; $lonMax = -999 ; $latMin = 999 ; $latMax = -999 ;
my $key ;
foreach $key (keys %lon) {
	if ($lon{$key} > $lonMax) { $lonMax = $lon{$key} ; }
	if ($lon{$key} < $lonMin) { $lonMin = $lon{$key} ; }
	if ($lat{$key} > $latMax) { $latMax = $lat{$key} ; }
	if ($lat{$key} < $latMin) { $latMin = $lat{$key} ; }
}
initGraph ($size, $lonMin, $latMin, $lonMax, $latMax) ;


# BG AREAS

print "draw areas...\n" ;
foreach my $wayId (keys %memWayTags) {
	foreach $key (@{$memWayTags{$wayId}}) {
		foreach my $test (@areas) {
			if ( ($key->[0] eq $test->[$areaIndexKey]) and ($key->[1] eq $test->[$areaIndexValue]) ) {
				if ($memWayNodes{$wayId}[0] == $memWayNodes{$wayId}[-1]) {
					drawArea ($test->[$areaIndexColor], nodes2Coordinates( @{$memWayNodes{$wayId}} ) ) ;
				}
			}
		}
	}
}


# NODES

print "draw nodes...\n" ;
foreach my $nodeId (keys %memNodeTags) {
	foreach my $tag (@{$memNodeTags{$nodeId}} ) {
		foreach my $test (@nodes) {
			if ( ($tag->[0] eq $test->[$nodeIndexTag]) and ($tag->[1] eq $test->[$nodeIndexValue]) ) {
				if ($test->[$nodeIndexThickness] > 0) {
					drawNodeDot ($lon{$nodeId}, $lat{$nodeId}, $test->[$nodeIndexColor], $test->[$nodeIndexThickness]) ;
				}

				if ($test->[$nodeIndexLabel] ne "none") {
					my $name = "" ;
					# get name
					foreach my $tag2 (@{$memNodeTags{$nodeId}}) {
						if ($tag2->[0] eq $test->[$nodeIndexLabel]) {
							$name = $tag2->[1] ;
						}
					}
					if ($name ne "") {
						drawTextPos ($lon{$nodeId}, $lat{$nodeId}, 0, -$test->[$nodeIndexLabelOffset], 
							$name, $test->[$nodeIndexLabelColor], $test->[$nodeIndexLabelSize]) ;
					}
				} # draw label
			} # tag found
		} # test
	} # tags
} # nodes


# WAYS

print "draw ways...\n" ;
foreach my $wayId (keys %memWayTags) {
	#print "wayid: $wayId\n" ;
	my $text = "" ; 
	my $length = 0 ;

	for (my $i = 0; $i < scalar (@{$memWayNodes{$wayId}})-1   ; $i++) {
		$length += distance ($lon{ $memWayNodes{$wayId}[$i] }, $lat{ $memWayNodes{$wayId}[$i] }, 
			$lon{ $memWayNodes{$wayId}[$i+1] }, $lat{ $memWayNodes{$wayId}[$i+1] }) ;
	}

	# tunnels, bridges and layers
	my $tunnel = "no" ; my $bridge = "no" ; my $layer = 0 ;
	foreach my $tag (@{$memWayTags{$wayId}}) {
		if ($tag->[0] eq "tunnel") { $tunnel = $tag->[1] ; }
		if ($tag->[0] eq "bridge") { $bridge = $tag->[1] ; }
		if ($tag->[0] eq "layer") { $layer = $tag->[1] ; }
	}

	# test variables for correct content
	if ($tunnel ne "yes") { $tunnel = "no" ; }
	if ($bridge ne "yes") { $bridge = "no" ; }
	my $found = 0 ;
	foreach (-5,-4,-3,-2,-1,0,1,2,3,4,5) { if ($layer == $_) { $found = 1 ; } }
	if ($found == 0) { $layer = 0 ; }

	foreach my $tag (@{$memWayTags{$wayId}}) {
		#print "  $tag->[0] $tag->[1]\n" ;
		foreach my $test (@ways) {
			if ( ($tag->[0] eq $test->[$wayIndexTag]) and ($tag->[1] eq $test->[$wayIndexValue]) ) {
				#print "    tag match\n" ;
				if ($test->[$wayIndexFilled] eq "0") {
					#print "      drawing way $test->[$wayIndexColor], $test->[$wayIndexThickness] ...\n" ;
					if ($bridge eq "yes") {
						drawWayBridge ($layer-.4, "black", $test->[$wayIndexThickness]+4, 0, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
						drawWayBridge ($layer-.2, "white", $test->[$wayIndexThickness]+2, 0, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
					}
					if ($tunnel eq "yes") {
						drawWayBridge ($layer-.4, "black", $test->[$wayIndexThickness]+4, 11, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
						drawWayBridge ($layer-.2, "white", $test->[$wayIndexThickness]+2, 0, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
					}
					drawWay ($layer, $test->[$wayIndexColor], $test->[$wayIndexThickness], $test->[$wayIndexDash], nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
					if ($test->[$wayIndexLabel] ne "none") {
						foreach my $tag2 (@{$memWayTags{$wayId}}) {
							if ( ($tag2->[0] eq $test->[$wayIndexLabel]) and ($length >= $labelMinLength) ) {
								labelWay ($test->[$wayIndexLabelColor], 0, "", $tag2->[1], -2, nodes2Coordinates(@{$memWayNodes{$wayId}})) ;
							}
						}
					}
				} # not filled
				else {
					if (${$memWayNodes{$wayId}}[0] == ${$memWayNodes{$wayId}}[-1]) {
						drawArea ($test->[$wayIndexColor], nodes2Coordinates( @{$memWayNodes{$wayId}} ) ) ;
						if ($test->[$wayIndexLabel] ne "none") {
							foreach my $tag2 (@{$memWayTags{$wayId}}) {
								if ($tag2->[0] eq $test->[$wayIndexLabel]) {
									my ($x, $y) = (0, 0) ; my $count = 0 ;
									foreach my $node (@{$memWayNodes{$wayId}}) {
										$x += $lon{$node} ; $y += $lat{$node} ; $count++ ;
									}
									$x = $x / $count ; $y = $y / $count ;
									# drawTextPos ($lon{${$memWayNodes{$wayId}}[0]}, $lat{${$memWayNodes{$wayId}}[0]}, 0, 0, $tag2->[1], $test->[$wayIndexLabelColor], 2) ;
									drawTextPos ($x, $y, 0, 0, $tag2->[1], $test->[$wayIndexLabelColor], 2) ;
								}
							}
						} # draw label
					} #closed
				} # filled
			} # tag found
		} # $test
	} # $tag
} # ways



# draw other information

print "draw legend etc. and write files...\n" ;

# drawLegend (2, @legend) ;
createLegend() ;

drawRuler ("darkgray") ;
drawHead ("gary68's $programName $version", "black", 2) ;
drawFoot ("data by www.openstreetmap.org", "gray", 2) ;


writeSVG ($svgName) ;

if ($pdfOpt eq "1") {
	my ($pdfName) = $svgName ;
	$pdfName =~ s/\.svg/\.pdf/ ;
	print "creating pdf file $pdfName ...\n" ;
	`inkscape -A $pdfName $svgName` ;
}

if ($pngOpt eq "1") {
	my ($pngName) = $svgName ;
	$pngName =~ s/\.svg/\.png/ ;
	print "creating png file $pngName ...\n" ;
	`inkscape -e $pngName $svgName` ;
}


$time1 = time() ;
print "\n$programName finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;


sub nodes2Coordinates {
#
# transform list of nodeIds to list of lons/lats
#
	my @nodes = @_ ;
	my $i ;
	my @result = () ;

	#print "in @nodes\n" ;

	for ($i=0; $i<=$#nodes; $i++) {
		push @result, $lon{$nodes[$i]} ;
		push @result, $lat{$nodes[$i]} ;
	}
	return @result ;
}

sub createLegend {
	my $currentY = 50 ;
	my $step = 20 ;
	my $textX = 70 ;
	my $textOffset = -5 ;
	my $dotX = 40 ;
	my $areaSize = 8 ;
	my $wayStartX = 20 ;
	my $wayEndX = 60 ;
	my $areaStartX = 33 ;
	my $areaEndX = 47 ;
	my $count = 0 ;
	my $sizeLegend = 2 ;
	
	foreach (@areas) { if ($_->[$areaIndexLegend] == 1) { $count++ ; }  }
	foreach (@nodes) { if ($_->[$nodeIndexLegend] == 1) { $count++ ; }  }
	foreach (@ways) { if ($_->[$wayIndexLegend] == 1) { $count++ ; }  }

	# erase background
	drawAreaPix ("white", 0, 30,
			180,30,
			180, 30 + $count*20 + 10,
			0, 30 + $count*20 + 10,
			0, 30) ;
	
	foreach my $node (@nodes) { 
		if ($node->[$nodeIndexLegend] == 1) { 
			drawNodeDotPix ($dotX, $currentY, $node->[$nodeIndexColor], $node->[$nodeIndexThickness]) ;
			drawTextPix ($textX, $currentY+$textOffset, $node->[$nodeIndexValue], "black", $sizeLegend) ;
			$currentY += $step ;
		}  
	}

	foreach my $way (@ways) { 
		if ($way->[$wayIndexLegend] == 1) { 
			if ($way->[$wayIndexFilled] == 0) {
				drawWayPix ($way->[$wayIndexColor], $way->[$wayIndexThickness], $way->[$wayIndexDash], $wayStartX, $currentY, $wayEndX, $currentY) ;
			} 
			else {
				drawAreaPix ($way->[$wayIndexColor], $areaStartX, $currentY-$areaSize, 
					$areaEndX, $currentY-$areaSize,
					$areaEndX, $currentY+$areaSize,
					$areaStartX, $currentY+$areaSize,
					$areaStartX, $currentY-$areaSize) ;
			}
			drawTextPix ($textX, $currentY+$textOffset, $way->[$wayIndexValue], "black", $sizeLegend) ;
			$currentY += $step ;
		}  
	}

	foreach my $area (@areas) { 
		if ($area->[$areaIndexLegend] == 1) { 
			drawAreaPix ($area->[$areaIndexColor], $areaStartX, $currentY-$areaSize, 
				$areaEndX, $currentY-$areaSize,
				$areaEndX, $currentY+$areaSize,
				$areaStartX, $currentY+$areaSize,
				$areaStartX, $currentY-$areaSize) ;
			drawTextPix ($textX, $currentY+$textOffset, $area->[$areaIndexValue], "black", $sizeLegend) ;
			$currentY += $step ;
		}  
	}

}
