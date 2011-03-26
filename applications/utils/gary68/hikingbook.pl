



# todo
#
# LATER
# - negative ids for steps
# - print parameters
#

# history
# 0.91 autorotate corrected
# 0.92 bigger overview and detail maps; added names to pois in description; less steps
# 0.93 variable discs, taginfo -> pdf
# 0.94 role selection; title and outName defaults
# 0.95 sanitize
# 0.96 contour data
# 0.97 street directory
# 0.98 street atlas
# 

use strict ;
use warnings ;
use Getopt::Long ;
use OSM::osm ;
use OSM::QuadTree ;


my $programName = "hikingbook.pl" ;
my $version = "0.98" ;

my $inFileName = "hessen.osm" ;
my $outFileName = "hikingbook.pdf" ;
my $overviewStyleFileName = "hikingRules.csv" ;
my $detailStyleFileName = "hikingRules.csv" ;
my $poiFileName = "hikingbook.poi" ;
my $relationId = 0 ;
my $relationName = "" ;
my $relationRef = "" ;
my @relationTags ;
my $scale = 10000 ; # default detail scale 
my %pageHeight = () ;
$pageHeight{"A4"} = 27.7 ;
$pageHeight{"A5"} = 19 ;
my %pageWidth = () ;
$pageWidth{"A4"} = 19 ;
$pageWidth{"A5"} = 13.8 ;
my @overviewScales = (10000,25000,50000,75000,100000,200000,500000,1000000) ; # best fit will be taken
my $title = "Hiking book" ;
my $workDir = "" ;
my $logFileName = "hikingbooklog.txt" ;
my @tempFiles = () ; # to be deleted later

my %lon ; my %lat ;
my @ways ;
my @nodes ;
my %nodeDirCount ; 
my %wayNodesHash = () ;
my %wayNameHash = () ;
my $mapNumber = 0 ; # counts detail maps
my $scaleOverview = 0 ;
my $rectangles = "" ; # collect data for overview map

my $languageOpt = "EN" ;
my $noOutputOpt = 0 ;
my $verboseOpt = 0 ;
my $dirNumberOpt = 8 ; # number of different directions used (N,S,SW...)
my $pageSizeOpt = "A4" ;
my $overlapOpt = 5 ; # in percent
my $landscapeOpt = 0 ;
my $reverseOpt = 0 ;
my $roundtripOpt = 0 ;
my $noDeleteOpt = 0 ;
my $pnSizeOverview = 48 ;
my $pnSizeDetail = 64 ;
my $descName = "" ;
my $stepDescName = "" ;
my $autorotateOpt = 0 ;
my $lessStepsOpt = 0 ;
my $stepSizeOpt = 15 ;
my $stepColorOpt = "black" ;
my $stepFontSizeOpt = 35 ;
my $atlasOpt = "" ;

my $poiOpt = 0 ;
my $gridNumber = 5 ;
my %pois = () ;
my $streetOpt = 0 ;
my %streets = () ;

my $extraMapData1 = 0.1 ; # overlap for osm temp file 1 in degrees
my $extraMapData2 = 0.005 ; # for temp file 2s

my $mapgenCommandOverview = "perl mapgen.pl -pdf -declutter -legend=0 -scale -allowiconmove -pagenumbers=$pnSizeOverview,black,0" ;
my $mapgenCommandDetail = "perl mapgen.pl -pdf -declutter -legend=0 -scale -allowiconmove" ;

# relation bounding box. all data.
my $relLonMax = -999 ;
my $relLonMin = 999 ;
my $relLatMax = -999 ;
my $relLatMin = 999 ;

# detail boxes outer bounds 0.92
my $detailLonMax = -999 ;
my $detailLonMin = 999 ;
my $detailLatMax = -999 ;
my $detailLatMin = 999 ;

# initial file
my $fileLonMax = -999 ;
my $fileLonMin = 999 ;
my $fileLatMax = -999 ;
my $fileLatMin = 999 ;

# temp osm file 1 data
my $file1LonMax = -999 ;
my $file1LonMin = 999 ;
my $file1LatMax = -999 ;
my $file1LatMin = 999 ;

my $temp1FileName = "temp1.osm" ;
my $temp2FileName = "temp2.osm" ;

my %nodeName = () ;
my %nodeElevation = () ;
my %nodeInfo = () ;
my $segmentLength = 0 ;
my %stepInformation = () ;
my %contourData = () ;
my %contourWays = () ;
my $qt ;

my @poiList ;   # POIs actually found
my @pois = () ; # POIs to be used

my %translations = () ;
$translations{"DE"}{"station"} = "Bahnhof" ;
$translations{"DE"}{"bus_stop"} = "Bushaltestelle" ;
$translations{"DE"}{"viewpoint"} = "Aussichtspunkt" ;
$translations{"DE"}{"restaurant"} = "Restaurant" ;
$translations{"DE"}{"pharmacy"} = "Apotheke" ;
$translations{"DE"}{"hospital"} = "Krankenhaus" ;
$translations{"DE"}{"peak"} = "Gipfel" ;
$translations{"DE"}{"fuel"} = "Tankstelle" ;
$translations{"DE"}{"place_of_worship"} = "Kirche" ;
$translations{"DE"}{"bar"} = "Bar" ;
$translations{"DE"}{"hotel"} = "Hotel" ;
$translations{"DE"}{"attraction"} = "Attraktion" ;
$translations{"DE"}{"supermarket"} = "Supermarkt" ;
$translations{"DE"}{"fast food"} = "Fast Food" ;
$translations{"DE"}{"suburb"} = "Vorort" ;
$translations{"DE"}{"hamlet"} = "Weiler" ;
$translations{"DE"}{"city"} = "Großstadt" ;
$translations{"DE"}{"town"} = "Stadt" ;
$translations{"DE"}{"information"} = "Information" ;
$translations{"DE"}{"spring"} = "Quelle" ;
$translations{"DE"}{"parking"} = "Parkplatz" ;
$translations{"DE"}{"pub"} = "Kneipe" ;
$translations{"DE"}{"bank"} = "Bank" ;
$translations{"DE"}{"castle"} = "Burg" ;
$translations{"DE"}{"village"} = "Dorf" ;
$translations{"DE"}{"hostel"} = "Herberge" ;
# $translations{"DE"}{""} = "" ;

my %validLatex = () ;
my %replaceLatex = () ;

initializeLatex() ;
getProgramOptions () ;

if ($atlasOpt eq "") {
	print "HIKING/ROUTE MODE\n" ;
	getPoiFiledata () ;
	getRelationData() ;
	getStepInformation() ;
	createTemp1 () ;
	buildCompleteWay() ;
	compileElevationData () ;
	addPois() ;
	createDirections() ;
	createDetailMaps() ;
}
else {
	print "ATLAS MODE\n" ;

	if ($title eq "Hiking book") { $title = "Atlas" ; }

	# print "opt = $atlasOpt\n" ;
	($relLonMin, $relLatMin, $relLonMax, $relLatMax) = ( $atlasOpt =~ /([\-\d\.]+)\,([\-\d\.]+)\,([\-\d\.]+)\,([\-\d\.]+)/ ) ;
	# print "$relLonMin, $relLatMin, $relLonMax, $relLatMax\n" ;
	if ((! defined $relLonMin) or (! defined $relLatMin) or (! defined $relLonMax) or (! defined $relLatMax)) {
		die "ERROR in atlas coordinates: $atlasOpt\n" ;
	}

	openOsmFile ($inFileName) ;
	print "reading nodes of input file...\n" ;

	my $propRef ; my $tagsRef ;	
	($propRef, $tagsRef) = getNode3() ;
	while (defined $propRef) {
		# collect data of big file
		if ( $$propRef{"lon"} > $fileLonMax ) { $fileLonMax = $$propRef{"lon"} ; }
		if ( $$propRef{"lon"} < $fileLonMin ) { $fileLonMin = $$propRef{"lon"} ; }
		if ( $$propRef{"lat"} > $fileLatMax ) { $fileLatMax = $$propRef{"lat"} ; }
		if ( $$propRef{"lat"} < $fileLatMin ) { $fileLatMin = $$propRef{"lat"} ; }

		($propRef, $tagsRef) = getNode3() ;
	}
	closeOsmFile() ;
	print "done.\n" ;

	createTemp1 () ;
	createDetailMapsAtlas() ;
}

createOverviewMap() ;
createTitlePage () ;
createDirectory () ;
mergeAllFiles() ;

print "\ndeleting temp files (disable with -nodelete)...\n" ;
if ($noDeleteOpt == 0) {
	push @tempFiles, $temp2FileName ;
	foreach my $f (@tempFiles) {
		`rm $f` ;
	}
}
print "done.\n\n" ;


sub getRelationData {
	my %neededWays = () ;
	my %neededNodes = () ;
	my @relationMembers = () ;
	# my @relationTags = () ; # now GLOBAL

	# get relation data

	print "\nget data from file...\n" ;
	print "parsing relations...\n" ;
	openOsmFile ($inFileName) ;
	skipNodes() ;
	skipWays() ;

	my $propRef ; my $membersRef ; my $tagsRef ;
	($propRef, $membersRef, $tagsRef) = getRelation3() ;

	my $found = 0 ;
	my $name = "" ; my $ref = "" ;
	while ( (defined $propRef) and (! $found) ) {
		if ($$propRef{"id"} == $relationId) {
			$found = 1 ;
			@relationMembers = @$membersRef ;
			@relationTags = @$tagsRef ;
		}

		foreach my $t (@$tagsRef) {
			if ($t->[0] eq "name") { $name = $t->[1] ; }
			if ($t->[0] eq "ref") { $ref = $t->[1] ; }
		}

		if ( ($relationName ne "") and (grep /^$relationName$/i, $name) ) {
			$found = 1 ;
			$relationId = $$propRef{'id'} ;
			@relationMembers = @$membersRef ;
			@relationTags = @$tagsRef ;
		}
		if ( ($relationRef ne "") and (grep /^$relationRef$/i, $ref) ) {
			$found = 1 ;
			$relationId = $$propRef{'id'} ;
			@relationMembers = @$membersRef ;
			@relationTags = @$tagsRef ;
		}

		if ($found) {
			print "\nRelation $$propRef{'id'} found.\n" ;
			print "Name: $name\n" ;
			print "Ref: $ref\n\n" ;

		}

		($propRef, $membersRef, $tagsRef) = getRelation3() ;
	}
	closeOsmFile() ;

	if ( ($name ne "") and ($title eq "Hiking book") ) { $title = $name ; } # 0.94
	if ( ($name ne "") and ($outFileName eq "hikingbook.pdf") ) { $outFileName = $name . ".pdf" ; }

	if ($found == 0) {
		die ("relation not found!\n") ;
	}

	@ways = () ;

	my $wc = 0 ;
	my %types = () ;
	foreach my $m (@relationMembers) {
		if ($m->[0] eq "way") {
			$types{$m->[2]} = 1 ;
			my $validRole = 0 ; # 0.94
			foreach my $r (qw (none forward route) ) { 
				if ( $m->[2] eq $r ) { $validRole = 1 ; }
			}
			if ($validRole) {
				$wc++ ;
				$neededWays{$m->[1]} = 1 ;
				push @ways, $m->[1] ;
			}
			else {
				my $role = $m->[2] ;
				print "WARNING: invalid role $role detected.\n" ;
			}
		}
	}
	if ($verboseOpt eq "1") {
		print "relation contains $wc ways.\n" ;
		my @nw = keys %neededWays ;
		print "NEEDED WAYS: @nw\n" ;
	}

	if ($verboseOpt eq "1") {
		print "ROLES: " ;
		foreach my $r (keys %types) { print $r, " " ; }
		print "\n" ;
	}

	# get way data

	print "parsing ways...\n" ;
	openOsmFile ($inFileName) ;
	skipNodes() ;

	my $nodesRef ; 
	($propRef, $nodesRef, $tagsRef) = getWay3() ;
	while (defined $propRef) {
		if (defined $neededWays{$$propRef{"id"}}) {

			delete $neededWays{$$propRef{"id"}} ;

			if (scalar @$nodesRef < 2) { print "ERROR: needed way $$propRef{'id'} has less than 2 nodes!\n" ; } 

			foreach my $n (@$nodesRef) {
				$neededNodes{$n} = 1 ;
			}
			@{$wayNodesHash{ $$propRef{"id"}} } = @$nodesRef ;

			my $n = "" ; 
			my $r = "" ;
			foreach my $t (@$tagsRef) {
				if ($t->[0] eq "name") { $n = $t->[1] ; }
				if ($t->[0] eq "ref") { $r = $t->[1] ; }
			}

			my $name = "" ;
			if ($n ne "") { $name = $n ; }
			if ($r ne "") { $name = $r . " " . $name ; }
			$wayNameHash{$$propRef{"id"}} = $name ;
			# print "name: $name\n" ;

		}


		($propRef, $nodesRef, $tagsRef) = getWay3() ;
	}
	closeOsmFile() ;

	if (scalar keys %neededWays > 0) {
		foreach my $w (keys %neededWays) {
			print "ERROR: needed way $w missing.\n" ;
		}
		die ("ERROR: ways missing.\n") ;
	}

	# get node data

	openOsmFile ($inFileName) ;
	print "reading nodes...\n" ;

	($propRef, $tagsRef) = getNode3() ;
	while (defined $propRef) {
		if (defined $neededNodes{$$propRef{"id"}}) {
			delete $neededNodes{$$propRef{"id"}} ;

			$lon{ $$propRef{"id"} } = $$propRef{"lon"} ; 
			$lat{ $$propRef{"id"} } = $$propRef{"lat"} ; 

			if ( $$propRef{"lon"} > $relLonMax ) { $relLonMax = $$propRef{"lon"} ; }
			if ( $$propRef{"lon"} < $relLonMin ) { $relLonMin = $$propRef{"lon"} ; }
			if ( $$propRef{"lat"} > $relLatMax ) { $relLatMax = $$propRef{"lat"} ; }
			if ( $$propRef{"lat"} < $relLatMin ) { $relLatMin = $$propRef{"lat"} ; }

			my $ele = "" ;
			foreach my $t (@$tagsRef) {
				if ($t->[0] eq "ele") { $ele = $t->[1] ; }
			}
			$nodeElevation{$$propRef{"id"}} = $ele ;
	
		}

		my $extra = 0.000 ;
		$relLonMin = $relLonMin - $extra ;
		$relLonMax = $relLonMax + $extra ;
		$relLatMin = $relLatMin - $extra ;
		$relLatMax = $relLatMax + $extra ;


		# also collect data of big file
		if ( $$propRef{"lon"} > $fileLonMax ) { $fileLonMax = $$propRef{"lon"} ; }
		if ( $$propRef{"lon"} < $fileLonMin ) { $fileLonMin = $$propRef{"lon"} ; }
		if ( $$propRef{"lat"} > $fileLatMax ) { $fileLatMax = $$propRef{"lat"} ; }
		if ( $$propRef{"lat"} < $fileLatMin ) { $fileLatMin = $$propRef{"lat"} ; }

		($propRef, $tagsRef) = getNode3() ;
	}
	closeOsmFile() ;
	print "done.\n" ;

	if (scalar keys %neededNodes > 0) {
		foreach my $n (keys %neededNodes) {
			print "ERROR: needed node $n missing.\n" ;
		}
		die ("ERROR: nodes missing.\n") ;
	}


	if ($verboseOpt eq "1") {
		print "relation bounding box: $relLonMin, $relLatMin, $relLonMax, $relLatMax\n" ;
	}



}


sub createTemp1 {
	# this file is used to draw the overview and further osm files for details are generated from this one to save time.
	print "\ncreate temp file 1...\n" ;


	if ($noOutputOpt eq "0") {
		shrinkFile ($inFileName, $temp1FileName, $fileLonMin, $fileLatMin, $fileLonMax, $fileLatMax, $relLonMin, $relLatMin, $relLonMax, $relLatMax, $extraMapData1) ;

		push @tempFiles, $temp1FileName ;

		print "reading temp file...\n" ;

		my $contourCount = 0 ;
		my %neededNodes = () ;

		my $nodesRef ; my $propRef ; my $tagsRef ;

		if ($atlasOpt eq "") {

			# openOsmFile ($inFileName) ;
			openOsmFile ($temp1FileName) ; # 0.92
			skipNodes() ;

			($propRef, $nodesRef, $tagsRef) = getWay3() ;
			while (defined $propRef) {
				my $needed = 0 ;
				my $contourNeeded = 0 ;
				my $ele = -999 ;
				foreach my $t (@$tagsRef) {
					if ($t->[0] eq "highway") { $needed = 1 ; }
					if ($t->[0] eq "contour_ext") { $contourNeeded = 1 ; }
					if ($t->[0] eq "ele") { $ele = $t->[1] ; }
				}

				if ($needed) {
					@nodes = @$nodesRef ;
					for (my $i=0 ; $i <= $#nodes; $i++) {
						my $n = $nodes[$i] ;
						if ( ($i == 0) or ($i ==$#nodes) ) {
							$nodeDirCount{$n} += 1 ;
						}
						else {
							$nodeDirCount{$n} += 2 ;
						}
					}
				}

				if ( ($contourNeeded) and ($ele != -999) and (scalar @$nodesRef > 1) ) {
					$contourCount++ ;
					@{ $wayNodesHash{ $$propRef{"id"} } } = @$nodesRef ;
					$contourWays { $$propRef{"id"} } = $ele ;
					foreach my $n ( @$nodesRef ) {
						$neededNodes{$n} = 1 ;
					}
				}

				($propRef, $nodesRef, $tagsRef) = getWay3() ;
			}
			closeOsmFile() ;
			print "$contourCount contour ways found.\n" ;

		}

		# store min / max of temp file


		openOsmFile ($temp1FileName) ;
		($propRef, $tagsRef) = getNode3() ;
		while (defined $propRef) {

			if (defined $neededNodes { $$propRef{"id"} }) {
				$lon { $$propRef{"id"} } = $$propRef{"lon"} ;
				$lat { $$propRef{"id"} } = $$propRef{"lat"} ;
			}

			if ( $$propRef{"lon"} > $file1LonMax ) { $file1LonMax = $$propRef{"lon"} ; }
			if ( $$propRef{"lon"} < $file1LonMin ) { $file1LonMin = $$propRef{"lon"} ; }
			if ( $$propRef{"lat"} > $file1LatMax ) { $file1LatMax = $$propRef{"lat"} ; }
			if ( $$propRef{"lat"} < $file1LatMin ) { $file1LatMin = $$propRef{"lat"} ; }

			if ($atlasOpt eq "") {

				# poi check and data collection
				foreach my $t (@$tagsRef) {
					foreach my $p (@pois) {
						if ( ($t->[0] eq $p->[0]) and ($t->[1] eq $p->[1]) ) {
							my $name = "" ;
							foreach my $t1 (@$tagsRef) {
								if ($t1->[0] eq "name") { $name = $t1->[1] ; }
							}
							my $info ;
							if ($languageOpt eq "EN") { $info = $p->[3] ; }
							if ($languageOpt eq "DE") { $info = $p->[4] ; }
							$name = sanitizeLatexString ($name) ;
							push @poiList, [ $info, $name, $$propRef{"id"}, $p->[2] ] ;
							$lon{ $$propRef{"id"} } = $$propRef{"lon"} ;
							$lat{ $$propRef{"id"} } = $$propRef{"lat"} ;
						}
					}
				}

			}

			($propRef, $tagsRef) = getNode3() ;
		}
		closeOsmFile() ;

		# print "temp1: $file1LonMin, $file1LatMin, $file1LonMax, $file1LatMax\n" ;

		$qt = OSM::QuadTree->new (	-xmin => $file1LonMin, 
						-xmax => $file1LonMax, 
						-ymin => $file1LatMin, 
						-ymax => $file1LatMax, 
						-depth => 6) ;


	}
}

sub createOverviewMap {
	print "\ncreate overview map...\n" ;

	my $scale ;
	# my $distLon = distance ($relLonMin, $relLatMin, $relLonMax, $relLatMin) ; # in km
	# my $distLat = distance ($relLonMin, $relLatMin, $relLonMin, $relLatMax) ; # in km
	# 0.92
	my $distLon = distance ($detailLonMin, $detailLatMin, $detailLonMax, $detailLatMin) ; # in km
	my $distLat = distance ($detailLonMin, $detailLatMin, $detailLonMin, $detailLatMax) ; # in km

	$distLon = int ($distLon * 1000) / 1000 ;
	$distLat = int ($distLat * 1000) / 1000 ;

	if ($verboseOpt eq "1") {
		print "overview distances lon / lat in km: $distLon $distLat\n" ;
	}

	my $distWidth = $pageWidth{$pageSizeOpt} / 100 / 1000 ;
	my $distHeight = $pageHeight{$pageSizeOpt} / 100 / 1000 ;
	my $scaleWidth =  int ($distLon / $distWidth) ;
	my $scaleHeight =  int ($distLat / $distHeight) ;

	if ($verboseOpt eq "1") {
		print "overview scales W/H: $scaleWidth / $scaleHeight\n" ;
	}

	# select min scale
	$scale = $scaleWidth ;
	if ($scaleHeight > $scale) { $scale = $scaleHeight ; }

	# select fitting scale
	foreach my $s (@overviewScales) {
		if ($s > $scale) {
			$scale = $s ;
			last ;
		}
	}

	$scaleOverview = $scale ;

	if ($verboseOpt eq "1") {
		print "selected overview scale: $scale\n" ;
	}


	my $outName = $workDir . "overview.svg" ;
	if ($noOutputOpt eq "0") {

		my $pdfName = $outName ; $pdfName =~ s/\.svg/\.pdf/ ;
		my $ndlName = $outName ; $ndlName =~ s/\.svg/\_NotDrawnLabels\.txt/ ;
		push @tempFiles, $outName ;
		push @tempFiles, $pdfName ;
		push @tempFiles, $ndlName ;

		# allow for some more data than just the relation 0.92
		my $more = 0.005 ;
		my $ovLonMin = $detailLonMin - $more ;
		my $ovLonMax = $detailLonMax + $more ;
		my $ovLatMin = $detailLatMin - $more ;
		my $ovLatMax = $detailLatMax + $more ;

		# but check if data is present
		if ($ovLonMin < $file1LonMin) { $ovLonMin = $file1LonMin ; }
		if ($ovLonMax > $file1LonMax) { $ovLonMax = $file1LonMax ; }
		if ($ovLatMin < $file1LatMin) { $ovLatMin = $file1LatMin ; }
		if ($ovLatMax > $file1LatMax) { $ovLatMax = $file1LatMax ; }


		print "call mapgen and log to $logFileName...\n" ;

		`$mapgenCommandOverview -in=$temp1FileName -out=$outName -style=$overviewStyleFileName -scaleset=$scaleOverview -clipbbox=$ovLonMin,$ovLatMin,$ovLonMax,$ovLatMax -relid=$relationId $rectangles >> $logFileName 2>&1` ;

		print "done.\n" ;
	}
}



sub buildCompleteWay {
#
# this function tries to make sense of all the ways collected from the relation
#
	my %leftWays = () ;

	my @orderedWays ;

	print "\nbuilding complete way for relation...\n" ;

	my $firstWay = shift @ways ;
	@orderedWays = ($firstWay) ;

	if ($roundtripOpt eq "1") {
		# assure that first way can be expanded at the end
		my @n = @{$wayNodesHash{ $firstWay }} ;
		my $end = $n[-1] ;
		my $found = 0 ;
		foreach my $w (@ways) {
			my @tn = @{$wayNodesHash{ $w }} ;
			if ( ($tn[0] == $end) or ($tn[-1] == $end) ) { $found = 1 ; }
		}
		if ($found == 0) {
			@{$wayNodesHash{ $firstWay }} = reverse @{$wayNodesHash{ $firstWay }} ;
		}
	}


	@nodes = @{$wayNodesHash{ $firstWay }} ;

	foreach my $w (@ways) { $leftWays{ $w } = 1 ; }

	my $success = 1 ;
	while ( $success and ( scalar (keys %leftWays) > 0 ) ) {
		$success = 0 ;

		foreach my $w (keys %leftWays) {
			my @wayNodes = @{$wayNodesHash{$w}} ;

			if ( ( $nodes[0] == $wayNodes[0] ) and ($roundtripOpt == 0) ) {
			# if ( ( $nodes[0] == $wayNodes[0] ) ) {
				# reverse unshift
				@wayNodes = reverse @wayNodes ;
				pop @wayNodes ; # remove last element
				unshift @nodes, @wayNodes ;
				unshift @orderedWays, $w ;
				$success = 1 ;
				delete $leftWays{ $w } ;
				last ;
			}

			if ( ( $nodes[0] == $wayNodes[-1] ) and ($roundtripOpt == 0) ) {
			# if ( ( $nodes[0] == $wayNodes[-1] ) ) {
				# unshift
				pop @wayNodes ; # remove last element
				unshift @nodes, @wayNodes ;
				unshift @orderedWays, $w ;
				$success = 1 ;
				delete $leftWays{ $w } ;
				last ;
			}

			if ( $nodes[-1] == $wayNodes[0] ) {
				# push
				shift @wayNodes ; # remove first element
				push @nodes, @wayNodes ;
				push @orderedWays, $w ;
				$success = 1 ;
				delete $leftWays{ $w } ;
				last ;
			}

			if ( $nodes[-1] == $wayNodes[-1] ) {
				# push reverse
				@wayNodes = reverse @wayNodes ;
				shift @wayNodes ; # remove first element
				push @nodes, @wayNodes ;
				push @orderedWays, $w ;
				$success = 1 ;
				delete $leftWays{ $w } ;
				last ;
			}

		}
	}

	if ( $nodes[0] == $nodes[-1]) { print "found segment is closed.\n" ; }
	if ( scalar (keys %leftWays) > 0) { print "WARNING: relation consists of more than one segment. using only first one.\n" ; }

	my $nc = scalar @nodes ;

	if ($verboseOpt eq "1") {
		print "used segment consists of $nc nodes.\n" ;
	}

	@ways = @orderedWays ;

	if ( $lon{ $nodes[0] } > $lon{ $nodes[-1] } ) { 
		@nodes = reverse @nodes ; 
		@ways = reverse @ways ;
	} 

	if ($reverseOpt eq "1") {
		print "reversing ways and nodes.\n" ;
		@nodes = reverse @nodes ; 
		@ways = reverse @ways ;
	}
	
	# get names of ways to nodes for directions
	foreach my $w (@ways) {
		foreach my $n (@{$wayNodesHash{$w}}) {
			$nodeName{$n} = $wayNameHash{$w} ;
		}
	}

	# calc distances // nodeinfo is indexed by node number, not ID!
	my $dist = 0 ;
	$nodeInfo{0}{"distance"} = 0 ;
	for (my $i = 1;  $i<=$#nodes; $i++) {
		$dist += distance ($lon{$nodes[$i-1]}, $lat{$nodes[$i-1]}, $lon{$nodes[$i]}, $lat{$nodes[$i]}) ;
		$nodeInfo{$i}{"distance"} = int ($dist * 100) / 100 ;
	}
	$segmentLength = $dist ;
	$dist = int ($dist * 1000) / 1000 ;
	print "used route segment is $dist km long.\n" ;

	# nodeinfo is indexed by nodeNumber, NOT id!
	for (my $i = 0;  $i<$#nodes; $i++) {
		$nodeInfo{$i}{"direction"} = direction ($nodes[$i], $nodes[$i+1]) ;		
		$nodeInfo{$i}{"angle"} = angle ($lon{$nodes[$i]}, $lat{$nodes[$i]}, $lon{$nodes[$i+1]}, $lat{$nodes[$i+1]}) ;
		$nodeInfo{$i}{"name"} = sanitizeLatexString ( $nodeName{ $nodes[$i] } ) ;		
		$nodeInfo{$i}{"ele"} = $nodeElevation{ $nodes[$i] } ;		
		$nodeInfo{$i}{"dirs"} = $nodeDirCount{ $nodes[$i] } ;		
	}

	$nodeInfo{$#nodes}{"ele"} = $nodeElevation{ $nodes[ -1 ] } ;
	$nodeInfo{$#nodes}{"dirs"} = $nodeDirCount{ $nodes[ -1 ] } ;
	$nodeInfo{$#nodes}{"direction"} = "" ;
	$nodeInfo{$#nodes}{"angle"} = 0 ;
	$nodeInfo{$#nodes}{"name"} = "" ;

	my %info = () ;
	$info{"EN"}{"start"} = "Start" ;
	$info{"EN"}{"end"} = "End" ;
	$info{"DE"}{"start"} = "Start" ;
	$info{"DE"}{"end"} = "Ende" ;
	@{$nodeInfo{0}{"information"}} = ( $info{$languageOpt}{"start"} ) ;
	@{$nodeInfo{$#nodes}{"information"}} = ( $info{$languageOpt}{"end"} ) ;

	

	if ($verboseOpt eq "1") {
		print "ordered ways: @ways\n" ;
		print "\nordered nodes: @nodes\n\n" ;
	}
}



sub createDetailMaps {

	print "\ncreate detail maps...\n" ;

	my $first = 1 ;

	my $maxDistLon = $pageWidth{$pageSizeOpt} / 100 / 1000 * $scale ; # in km
	my $maxDistLat = $pageHeight{$pageSizeOpt} / 100 / 1000 * $scale ; # in km

	my $maxDistLonOverlap = $maxDistLon * ( 1 - 2 * $overlapOpt / 100) ;
	my $maxDistLatOverlap  = $maxDistLat * ( 1 - 2 * $overlapOpt / 100) ;

	# print "DETAIL:  page max dists $maxDistLon / $maxDistLat\n" ;

	my $mapName = "detail" ;
	my $start = 0 ;
	my $finished = 0 ;

	while ( ! $finished ) {
		
		my $actual = $start ;
		my $rotated = 0 ;
		my $lonMin = $lon{$nodes[$start]} ;
		my $lonMax = $lon{$nodes[$start]} ;
		my $latMin = $lat{$nodes[$start]} ;
		my $latMax = $lat{$nodes[$start]} ;
		
		my $busted = 0 ;
		while ( ( ! $busted ) and ($actual < $#nodes) ) {

			# print " $actual\n" ;
			my $tempLonMax = $lonMax ;
			my $tempLonMin = $lonMin ;
			my $tempLatMax = $latMax ;
			my $tempLatMin = $latMin ;

			# add point
			$actual++ ;
			if ( $lon{ $nodes[ $actual ] } > $lonMax) { $lonMax = $lon{ $nodes[ $actual ] } ; }
			if ( $lon{ $nodes[ $actual ] } < $lonMin) { $lonMin = $lon{ $nodes[ $actual ] } ; }
			if ( $lat{ $nodes[ $actual ] } > $latMax) { $latMax = $lat{ $nodes[ $actual ] } ; }
			if ( $lat{ $nodes[ $actual ] } < $latMin) { $latMin = $lat{ $nodes[ $actual ] } ; }

			my $distLon = distance ($lonMin, $latMin, $lonMax, $latMin) ;
			my $distLat = distance ($lonMin, $latMin, $lonMin, $latMax) ;

			# print "$actual:  $distLon / $distLat\n" ;

			# autorotate
			if ( ($autorotateOpt eq "1") and ( ! $rotated) ) {
				if 	( ( ($distLon > $maxDistLonOverlap) and ( $distLat < $maxDistLonOverlap) and ($maxDistLatOverlap > $maxDistLonOverlap) )
					or ( ($distLat > $maxDistLatOverlap) and ( $distLon < $maxDistLatOverlap)  and ($maxDistLatOverlap < $maxDistLonOverlap) )
					) { 
					if ($verboseOpt eq "1") { print "actual page automatically rotated.\n" ; }

					# print "rotated\n" ;

					$rotated = 1 ;
					$maxDistLon = $pageHeight{$pageSizeOpt} / 100 / 1000 * $scale ; # in km
					$maxDistLat = $pageWidth{$pageSizeOpt} / 100 / 1000 * $scale ; # in km

					$maxDistLonOverlap = $maxDistLon * ( 1 - 2 * $overlapOpt / 100) ;
					$maxDistLatOverlap  = $maxDistLat * ( 1 - 2 * $overlapOpt / 100) ;
				}
			}	

			if ( ($distLon > $maxDistLonOverlap) or ($distLat > $maxDistLatOverlap) ) { 
				# print "BUSTED\n" ;
				$busted = 1 ; 
				$actual-- ;
				# restore max and min
				$lonMax = $tempLonMax ;
				$lonMin = $tempLonMin ;
				$latMax = $tempLatMax ;
				$latMin = $tempLatMin;
			}
		}

		if ($actual == $#nodes) { $finished = 1 ; }

		# create map
		my $percent = int ($actual / $#nodes * 100) ;
		print "\ncreate map for nodes $start to $actual ($percent \%).\n" ;

		# print "DETAIL: initial bbox = $lonMin,$latMin,$lonMax,$latMax\n" ;
		# print "DETAIL: max dists = $maxDistLon $maxDistLat\n" ;

		my $distLon = distance ($lonMin, $latMin, $lonMax, $latMin) ;
		my $distLat = distance ($lonMin, $latMin, $lonMin, $latMax) ;
		# print "DETAIL: actual dists: $distLon, $distLat\n" ;


		my $missingLat = $maxDistLat - $distLat ;
		my $missingLatFactor = $missingLat / $distLat ;
		my $additionalLat = ($latMax-$latMin) * $missingLatFactor / 2 ;
		# print "DETAIL: LAT: $missingLat $missingLatFactor $additionalLat\n" ;
		
		$latMin = $latMin - $additionalLat ;
		$latMax = $latMax + $additionalLat ;

		my $missingLon = $maxDistLon - $distLon ;
		my $missingLonFactor = $missingLon / $distLon ;
		my $additionalLon = ($lonMax-$lonMin) * $missingLonFactor / 2 ;
		# print "DETAIL: LON: $missingLon $missingLonFactor $additionalLon\n" ;

		$lonMin = $lonMin - $additionalLon ;
		$lonMax = $lonMax + $additionalLon ;

		# print "DETAIL: bbox after factor = $lonMin,$latMin,$lonMax,$latMax\n" ;



		$mapNumber++ ;
		my $outName = $workDir . $mapName . $mapNumber . ".svg" ;
		my $actualScale = $scale ;
		if ($noOutputOpt eq "0") {


			shrinkFile ($temp1FileName, $temp2FileName, $file1LonMin, $file1LatMin, $file1LonMax, $file1LatMax, $lonMin, $latMin, $lonMax, $latMax, $extraMapData2) ;

			my ($file2LonMin, $file2LatMin, $file2LonMax, $file2LatMax) = getMinMax ($temp2FileName)  ;

			if ($lonMin < $file2LonMin) { $lonMin = $file2LonMin ; }
			if ($lonMax > $file2LonMax) { $lonMax = $file2LonMax ; }
			if ($latMin < $file2LatMin) { $latMin = $file2LatMin ; }
			if ($latMax > $file2LatMax) { $latMax = $file2LatMax ; }

			# maintain details outer box 0.92
			if ($lonMin < $detailLonMin) { $detailLonMin = $lonMin ; }
			if ($lonMax > $detailLonMax) { $detailLonMax = $lonMax ; }
			if ($latMin < $detailLatMin) { $detailLatMin = $latMin ; }
			if ($latMax > $detailLatMax) { $detailLatMax = $latMax ; }

			# print "DETAIL: bbox before mapgen = $lonMin,$latMin,$lonMax,$latMax\n" ;

			if ($first) {
				$first = 0 ;
				$rectangles = "-rectangles=" ;
			}
			else {
				$rectangles .= "#" ;
			}
			$rectangles .= "$lonMin,$latMin,$lonMax,$latMax" ;

			my $pdfName = $outName ; $pdfName =~ s/\.svg/\.pdf/ ;
			my $ndlName = $outName ; $ndlName =~ s/\.svg/\_NotDrawnLabels\.txt/ ;
			push @tempFiles, $outName ;
			push @tempFiles, $pdfName ;
			push @tempFiles, $ndlName ;

			my $poiParams = "" ;
			if ($poiOpt > 0) {
				$poiParams = "-poi -grid=$gridNumber" ;
			}

			my $streetParams = "" ;
			if ($streetOpt > 0) {
				$streetParams = "-dir -grid=$gridNumber" ;
			}

			print "call mapgen and log to $logFileName...\n" ;
			`$mapgenCommandDetail -in=$temp2FileName -out=$outName -style=$detailStyleFileName -scaleset=$actualScale -clipbbox=$lonMin,$latMin,$lonMax,$latMax -poifile=step.txt -relid=$relationId -pagenumbers=$pnSizeDetail,black,$mapNumber $poiParams $streetParams >> $logFileName 2>&1` ;
			print "done.\n" ;

			loadExternalFiles($outName) ;

		}

		# next
		$start = $actual ;

		# restore sizes (after rotate)
		$maxDistLon = $pageWidth{$pageSizeOpt} / 100 / 1000 * $scale ; # in km
		$maxDistLat = $pageHeight{$pageSizeOpt} / 100 / 1000 * $scale ; # in km

		$maxDistLonOverlap = $maxDistLon * ( 1 - 2 * $overlapOpt / 100) ;
		$maxDistLatOverlap  = $maxDistLat * ( 1 - 2 * $overlapOpt / 100) ;


	}

}

sub createDetailMapsAtlas {
	my $mapName = "detail" ;
	my $first = 1 ;

	# calc dists 

	my $maxDistLon = $pageWidth{$pageSizeOpt} / 100 / 1000 * $scale ; # in km
	my $maxDistLat = $pageHeight{$pageSizeOpt} / 100 / 1000 * $scale ; # in km
	my $sizeX = $maxDistLon / (111.11 * cos ($relLatMin / 360 * 3.14 * 2) ) ;
	my $sizeY = $maxDistLat / 111.11 ;
	if ($verboseOpt eq "1") {
		print "MAX dists: $maxDistLon, $maxDistLat (km)   $sizeX, $sizeY (degrees)\n" ;	
	}


	my @hor = () ;
	my @ver = () ;

	my $x = $relLonMin ; 
	my $x2 = $x + $sizeX ;
	push @hor, [$x, $x2] ;
	# print "$x $x2\n" ;
	while ($x2 < $relLonMax) {
		$x = $x2 - $sizeX * ($overlapOpt/100) ;
		$x2 = $x + $sizeX ;
		if ($x2 > $relLonMax) { $x2 = $relLonMax ; }
		push @hor, [$x, $x2] ;
		# print "$x $x2\n" ;
	}

	my $y = $relLatMax ; 
	my $y2 = $y - $sizeY ;
	push @ver, [$y2, $y] ;
	# print "$y $y2\n" ;
	while ($y2 > $relLatMin) {
		$y = $y2 + $sizeY * ($overlapOpt/100) ;
		$y2 = $y - $sizeY ;
		if ($y2 < $relLatMin) { $y2 = $relLatMin ; }
		push @ver, [$y2, $y] ;
		# print "$y $y2\n" ;
	}
	my $nx = scalar @hor ;
	my $ny = scalar @ver ;
	print "Dimensions: $nx * $ny (width * height)\n" ;
	my $ntot = $nx * $ny ;

	my $page ; my $py = 1 ; my $tot ;
	foreach my $v (@ver) {
		my $px = 1 ; 
		foreach my $h (@hor) {
			$tot = ($py-1) * (scalar @hor) + $px ;
			print "\n\ndetail maps progress: y=$py, x=$px  -  $h->[0] $v->[0] $h->[1] $v->[1]  -   page: $tot/$ntot\n" ;

			my $left ; my $right ; my $top ; my $bottom ;
			if ($px > 1) { $left = $px - 1 + ($py - 1) * (scalar @hor) ; } else { $left = 0 ; }
			if ($px < scalar @hor) { $right = $px + 1 + ($py - 1) * scalar @hor ; } else { $right = 0 ; }
			# print "TEST ", ($py - 2) * (scalar @hor) + $px, "\n" ;
			if ($py > 1) { $top = ($py - 2) * (scalar @hor) + $px ; } else { $top = 0 ; }
			if ($py < scalar @ver) { $bottom = ($py) * (scalar @hor) + $px ; } else { $bottom = 0 ; }
			if ($verboseOpt eq "1") {
				print "neighbour pages: $bottom $top // $left $right\n\n" ;
			}

			my $lonMin = $h->[0] ;
			my $lonMax = $h->[1] ;
			my $latMin = $v->[0] ;
			my $latMax = $v->[1] ;
	
			#
			# create map
			#

			$mapNumber++ ;
			my $outName = $workDir . $mapName . $mapNumber . ".svg" ;

			shrinkFile ($temp1FileName, $temp2FileName, $file1LonMin, $file1LatMin, $file1LonMax, $file1LatMax, $lonMin, $latMin, $lonMax, $latMax, $extraMapData2) ;

			my ($file2LonMin, $file2LatMin, $file2LonMax, $file2LatMax) = getMinMax ($temp2FileName)  ;

			if ($lonMin < $file2LonMin) { $lonMin = $file2LonMin ; }
			if ($lonMax > $file2LonMax) { $lonMax = $file2LonMax ; }
			if ($latMin < $file2LatMin) { $latMin = $file2LatMin ; }
			if ($latMax > $file2LatMax) { $latMax = $file2LatMax ; }

			# maintain details outer box
			if ($lonMin < $detailLonMin) { $detailLonMin = $lonMin ; }
			if ($lonMax > $detailLonMax) { $detailLonMax = $lonMax ; }
			if ($latMin < $detailLatMin) { $detailLatMin = $latMin ; }
			if ($latMax > $detailLatMax) { $detailLatMax = $latMax ; }

			# print "DETAIL: bbox before mapgen = $lonMin,$latMin,$lonMax,$latMax\n" ;

			if ($first) {
				$first = 0 ;
				$rectangles = "-rectangles=" ;
			}
			else {
				$rectangles .= "#" ;
			}
			$rectangles .= "$lonMin,$latMin,$lonMax,$latMax" ;

			my $pdfName = $outName ; $pdfName =~ s/\.svg/\.pdf/ ;
			my $ndlName = $outName ; $ndlName =~ s/\.svg/\_NotDrawnLabels\.txt/ ;
			push @tempFiles, $outName ;
			push @tempFiles, $pdfName ;
			push @tempFiles, $ndlName ;

			my $poiParams = "" ;
			if ($poiOpt > 0) {
				$poiParams = "-poi -grid=$gridNumber" ;
			}

			my $streetParams = "" ;
			if ($streetOpt > 0) {
				$streetParams = "-dir -grid=$gridNumber" ;
			}

			my $pageNumbers = "$pnSizeDetail,black,$mapNumber,$left,$bottom,$right,$top" ;

			print "call mapgen and log to $logFileName...\n" ;
			`$mapgenCommandDetail -in=$temp2FileName -out=$outName -style=$detailStyleFileName -scaleset=$scale -clipbbox=$lonMin,$latMin,$lonMax,$latMax -pagenumbers=$pageNumbers $poiParams $streetParams >> $logFileName 2>&1` ;
			print "done.\n" ;

			loadExternalFiles($outName) ;



			$px ++ ;
		}
		$py ++ ;
	}
}


sub shrinkFile {
	my ($inFileName, $outFileName, $inLonMin, $inLatMin, $inLonMax, $inLatMax, $outLonMin, $outLatMin, $outLonMax, $outLatMax, $pad) = @_ ;

	# print "SHRINK:\n" ;
	# print "$inFileName, $outFileName, $inLonMin, $inLatMin, $inLonMax, $inLatMax, $outLonMin, $outLatMin, $outLonMax, $outLatMax, $percentPad\n" ;

	# calc new bbox
	$outLonMax += $pad ; # 0.92
	$outLonMin -= $pad ;
	$outLatMax += $pad ;
	$outLatMin -= $pad ;
	
	# check against source bbox
	if ($outLonMin < $inLonMin) { $outLonMin = $inLonMin ; }
	if ($outLonMax > $inLonMax) { $outLonMax = $inLonMax ; }
	if ($outLatMin < $inLatMin) { $outLatMin = $inLatMin ; }
	if ($outLatMax > $inLatMax) { $outLatMax = $inLatMax ; }

	# print "bottom=$outLatMin top=$outLatMax left=$outLonMin right=$outLonMax\n" ;
	# print "SHRINK END\n" ;


	# call osmosis
	print "call osmosis and log to $logFileName...\n" ;

	# `osmosis --read-xml $inFileName  --bounding-box clipIncompleteEntities=true bottom=$outLatMin top=$outLatMax left=$outLonMin right=$outLonMax --write-xml $outFileName >> $logFileName 2>&1` ;

	`osmosis --read-xml $inFileName  --bounding-box completeWays=yes completeRelations=yes bottom=$outLatMin top=$outLatMax left=$outLonMin right=$outLonMax --write-xml $outFileName >> $logFileName 2>&1` ;

	print "osmosis done.\n" ;

}

sub direction {
	my ($node1, $node2) = @_ ;

	my %direction = () ;
	$direction{"EN"}{"N"} = "North" ;
	$direction{"EN"}{"S"} = "South" ;
	$direction{"EN"}{"W"} = "West" ;
	$direction{"EN"}{"E"} = "East" ;
	$direction{"EN"}{"NE"} = "North-East" ;
	$direction{"EN"}{"NW"} = "North-West" ;
	$direction{"EN"}{"SE"} = "South-East" ;
	$direction{"EN"}{"SW"} = "South-West" ;

	$direction{"DE"}{"N"} = "Nord" ;
	$direction{"DE"}{"S"} = "Süd" ;
	$direction{"DE"}{"W"} = "West" ;
	$direction{"DE"}{"E"} = "Ost" ;
	$direction{"DE"}{"NE"} = "Nordost" ;
	$direction{"DE"}{"NW"} = "Nordwest" ;
	$direction{"DE"}{"SE"} = "Südost" ;
	$direction{"DE"}{"SW"} = "Südwest" ;

	my $a = angle ($lon{$node1}, $lat{$node1}, $lon{$node2}, $lat{$node2}) ;	

	my $dir = "" ;

	if ($dirNumberOpt eq "8") {
		if ( ($a < 22.5) or ($a >= 337.5) ) { $dir = $direction {$languageOpt}{"N"} ; }
		if ( ($a >= 22.5) and ($a < 67.5) ) { $dir = $direction {$languageOpt}{"NE"} ; }
		if ( ($a >= 67.5) and ($a < 112.5) ) { $dir = $direction {$languageOpt}{"E"} ; }
		if ( ($a >= 112.5) and ($a < 167.5) ) { $dir = $direction {$languageOpt}{"SE"} ; }
		if ( ($a >= 167.5) and ($a < 202.5) ) { $dir = $direction {$languageOpt}{"S"} ; }
		if ( ($a >= 202.5) and ($a < 247.5) ) { $dir = $direction {$languageOpt}{"SW"} ; }
		if ( ($a >= 247.5) and ($a < 292.5) ) { $dir = $direction {$languageOpt}{"W"} ; }
		if ( ($a >= 292.5) and ($a < 337.5) ) { $dir = $direction {$languageOpt}{"NW"} ; }
	}
	else {
		if ( ($a < 45) or ($a >= 315) ) { $dir = $direction {$languageOpt}{"N"} ; }
		if ( ($a >= 45) and ($a < 135) ) { $dir = $direction {$languageOpt}{"E"} ; }
		if ( ($a >= 135) and ($a < 225) ) { $dir = $direction {$languageOpt}{"S"} ; }
		if ( ($a >= 225) and ($a < 315) ) { $dir = $direction {$languageOpt}{"W"} ; }
	}

	return $dir ;
}


sub mergeAllFiles {
	my $files ;

	$files .= $workDir . "title.pdf" ;
	$files .= " " . $workDir . "overview.pdf" ;

	if ($atlasOpt eq "") {
		$files .= " " . $workDir . "directions.pdf" ;
	}

	for (my $i = 1; $i <= $mapNumber; $i++) {
		my $name ;
		$name = " " . $workDir . "detail" . $i . ".pdf" ; 
		$files .= $name ;
	}

	if ( ($poiOpt > 0) or ($streetOpt > 0) ) {
		$files .= " " . $workDir . "directory.pdf" ; 
	}

	print "\nmerge pdfs ($files) into $outFileName and log to $logFileName\n" ;

	`pdfjoin --outfile \"$outFileName\" $files >> $logFileName 2>&1` ;

}


sub createDirections {
	print "\ncreating directions...\n" ;

	my $stepFileName = "step.txt" ;
	open (my $stepFile, ">", $stepFileName) or die ("can't open step output file") ;

	my $texFileName ; 
	my $ltxFileName ; 
	$texFileName = "directions.tex" ;
	$ltxFileName = "directionsltx.tex" ;
	push @tempFiles, $texFileName ;
	push @tempFiles, $stepFileName ;
	push @tempFiles, $ltxFileName ;
	my %label = () ;
	$label{"EN"}{"directions"} = "Directions" ;
	$label{"DE"}{"directions"} = "Wegbeschreibung" ;
	$label{"EN"}{"number"} = "Nr" ;
	$label{"DE"}{"number"} = "Nr" ;
	$label{"EN"}{"distance"} = "Distance" ;
	$label{"DE"}{"distance"} = "Entfernung" ;
	$label{"EN"}{"name"} = "Name" ;
	$label{"DE"}{"name"} = "Name" ;
	$label{"EN"}{"direction"} = "Direction" ;
	$label{"DE"}{"direction"} = "Richtung" ;
	$label{"EN"}{"elevation"} = "Elevation" ;
	$label{"DE"}{"elevation"} = "Höhe" ;
	$label{"EN"}{"info"} = "Information" ;
	$label{"DE"}{"info"} = "Informationen" ;
	$label{"EN"}{"eleprofile"} = "Elevation profile" ;
	$label{"DE"}{"eleprofile"} = "Höhenprofil" ;

	open (my $texFile, ">", $texFileName) or die ("can't open tex output file") ;
	open (my $ltxFile, ">", $ltxFileName) or die ("can't open tex ltx output file") ;
	if ($pageSizeOpt eq "A5") {
		print $texFile "\\documentclass[a5paper,12pt]{book}\n" ;
	}
	else {
		print $texFile "\\documentclass[a4paper,12pt]{book}\n" ;
	}
	print $texFile "\\usepackage[utf8]{inputenc}\n" ;
	print $texFile "\\usepackage{longtable}\n" ;
	print $texFile "\\usepackage{ltxtable}\n" ;
	print $texFile "\\columnsep7mm\n" ;
	print $texFile "\\begin{document}\n" ;
	print $texFile "\\section*{" .  $label{$languageOpt}{"directions"} . "}\n" ;
	print $texFile "\\LTXtable{\\textwidth}{directionsltx}\n" ;

	my $elevationDataPresent = 0 ;
	for (my $i=0; $i <= $#nodes; $i++) {
		if ($nodeInfo{$i}{'ele'} ne "") { $elevationDataPresent = 1 ; }
	}

	print $ltxFile "\\tiny\n" ;

	if ($elevationDataPresent) {
		print $ltxFile "\\begin{longtable}{|p{1cm}|p{2cm}|p{1.5cm}|p{3cm}|p{2cm}|p{3cm}|}\n" ;
	}
	else {
		print $ltxFile "\\begin{longtable}{|p{1cm}|p{2cm}|p{3cm}|p{2cm}|p{4.5cm}|}\n" ;
	}

	print $ltxFile "\\hline\n" ;

	if ($elevationDataPresent) {
		print $ltxFile "$label{$languageOpt}{'number'} & $label{$languageOpt}{'distance'} & $label{$languageOpt}{'elevation'} & $label{$languageOpt}{'name'} & $label{$languageOpt}{'direction'} & $label{$languageOpt}{'info'} \\\\ \n" ;
	}
	else {
		print $ltxFile "$label{$languageOpt}{'number'} & $label{$languageOpt}{'distance'} & $label{$languageOpt}{'name'} & $label{$languageOpt}{'direction'} & $label{$languageOpt}{'info'} \\\\ \n" ;
	}
	print $ltxFile "\\hline\n" ;


	my %toPrint = () ;
	$toPrint{0} = 1 ; 
	$toPrint{ $#nodes } = 1 ; 
	for (my $i=1; $i<$#nodes; $i++) {
		# if ($nodeInfo{$i-1}{"direction"} ne $nodeInfo{$i}{"direction"}) { $toPrint{$i} = 1 ; }

		# if ($nodeInfo{$i}{"dirs"} > 2) { $toPrint{$i} = 1 ; } #0.92
		if ( ($nodeInfo{$i}{"dirs"} > 2) and ( dirChange ($nodeInfo{$i-1}{"angle"}, $nodeInfo{$i}{"angle"}) > $lessStepsOpt) ) { $toPrint{$i} = 1 ; } #0.92

		if ($nodeInfo{$i-1}{"name"} ne $nodeInfo{$i}{"name"}) { $toPrint{$i} = 1 ; }
		if ($nodeInfo{$i}{"ele"} ne "") { $toPrint{$i} = 1 ; }
		if ( defined $nodeInfo{$i}{"information"} ) { $toPrint{$i} = 1 ; }
	}

	# prepare information
	my %info = () ;
	for (my $i=0; $i <= $#nodes; $i++) {
		$info{$i} = "" ;
		if (defined $nodeInfo{$i}{'information'} ) {
			my @a = () ; 
			@a = @{$nodeInfo{$i}{'information'}} ;
			my $t ; 
			my $first = 1 ;
			foreach my $e (@a) {
				if ($first) { $first = 0 ; } else { $t .= " * " ; }
				$t .= $e  ;
			}
			$info{$i} = $t ;
		}
	}

	my $lastDist = 0 ;
	my $line = 0 ;
	for (my $i=0; $i <= $#nodes; $i++) {
		if (defined $toPrint{$i}) {
			$line++ ;
			my $stepDist = $nodeInfo{$i}{"distance"} - $lastDist ;
			$stepDist = int ($stepDist * 100) / 100 ;

			if (defined $stepInformation{$line}) {
				if ($info{$i} eq "") {
					$info{$i} = $stepInformation{$line} ;
				}
				else {
					$info{$i} .= " * " . $stepInformation{$line} ;
				} 
			}


			if ($elevationDataPresent) {
				print $ltxFile "$line & " . $nodeInfo{$i}{"distance"} . " ($stepDist)" . " & " . $nodeInfo{$i}{"ele"} . " & " . $nodeInfo{$i}{"name"} . " & " . $nodeInfo{$i}{"direction"} . " & " . $info{$i} . "\\\\\n" ;
			}
			else {
				print $ltxFile "$line & " . $nodeInfo{$i}{"distance"} . " ($stepDist)" . " & " . $nodeInfo{$i}{"name"} . " & " . $nodeInfo{$i}{"direction"} . " & " . $info{$i} . "\\\\\n" ;
			}

			print $ltxFile "\\hline\n" ;
			$lastDist = $nodeInfo{$i}{"distance"} ;

			if ($stepSizeOpt > 0) {
				print $stepFile $lon{ $nodes[$i] } . " " ;
				print $stepFile $lat{ $nodes[$i] } . " " ;
				print $stepFile "$stepSizeOpt " ;
				print $stepFile "\"$stepColorOpt\" " ;
				print $stepFile "\"$line\" " ;
				print $stepFile "$stepFontSizeOpt" ;
				print $stepFile "\n" ;
			}
		}		
	}

	print $ltxFile "\\end{longtable}\n" ;


	my $countEle = 0 ; my $eleMin = 999 ; my $eleMax = -999 ;
	for (my $i=0; $i<=$#nodes; $i++) {
		if ($nodeInfo{$i}{'ele'} ne "") { 
			$countEle++ ; 
			if ($nodeInfo{$i}{'ele'} > $eleMax) { $eleMax = $nodeInfo{$i}{'ele'} ; }
			if ($nodeInfo{$i}{'ele'} < $eleMin) { $eleMin = $nodeInfo{$i}{'ele'} ; }
		}
	}

	foreach my $d (keys %contourData) {
		$countEle++ ; 
		if ($contourData{$d} > $eleMax) { $eleMax = $contourData{$d} ; }
		if ($contourData{$d} < $eleMin) { $eleMin = $contourData{$d} ; }

	}

	if ($countEle >= 2) {

		my $picWidth = 110 ;
		my $xOffset = 10 ;
		my $yOffset = 10 ;
		
		print $texFile "\\section*{" .  $label{$languageOpt}{"eleprofile"} . "}\n" ;
		print $texFile "\\setlength{\\unitlength}{1mm}\n" ;
		my $height = int ( ($eleMax - $eleMin) / 10 ) ;
		my $lh = $height + 10 ;
		print $texFile "\\begin{picture} ($picWidth,$lh)\n" ;

		my $width = 0 ;
		foreach my $d (1, 2, 3, 5, 7, 10, 15, 20, 25, 30, 35, 40, 45, 50, 75, 100, 150, 200, 300, 500, 750, 1000) {
			if ($d > $segmentLength) {
				$width = $d ;
				last ;
			} 
		}
		my $tx = $picWidth-$xOffset ;

		print $texFile "\\put($xOffset,$yOffset){\\line($picWidth,0){$tx}}\n" ;
		print $texFile "\\put($xOffset,$lh){\\line($picWidth,0){$tx}}\n" ;

		print $texFile "\\put($xOffset,$yOffset){\\line(0,$height){$height}}\n" ;
		print $texFile "\\put($picWidth,$yOffset){\\line(0,$height){$height}}\n" ;

		my $h1 = int ($eleMin) ;
		my $h2 = int ($eleMax) ;
		my $pos2 = $height + $yOffset ;
		my $ty = $yOffset + 2 ;
		print $texFile "\\put(0,$ty){\\tiny $h1 m}\n" ;
		print $texFile "\\put(0,$pos2){\\tiny $h2 m}\n" ;
		$tx = $picWidth-$xOffset ;
		print $texFile "\\put($tx,2){\\tiny $width km}\n" ;


		for (my $i=0; $i<=$#nodes; $i++) {
			if ($nodeInfo{$i}{"ele"} ne "") {
				my $x = int ($nodeInfo{$i}{"distance"} / $width * ($picWidth - $xOffset)) + $xOffset ;
				my $y = ($nodeInfo{$i}{"ele"} - $eleMin) / 10 + $yOffset ;
				print $texFile "\\put($x,$y){\\circle*{1}}\n" ;
			}
		}

		foreach my $d (keys %contourData) {
			my $x = int ($d / $width * ($picWidth - $xOffset)) + $xOffset ;
			my $y = ($contourData{$d} - $eleMin) / 10 + $yOffset ;
			print $texFile "\\put($x,$y){\\circle*{1}}\n" ;
		}

		print $texFile "\\end{picture}\n" ;
	}


	print $texFile "\\end{document}\n" ;
	close ($texFile) ;
	close ($ltxFile) ;

	close ($stepFile) ;

	my $dviFileName = $texFileName ;
	$dviFileName =~ s/.tex/.dvi/ ;
	my $psFileName = $texFileName ;
	$psFileName =~ s/.tex/.ps/ ;
	my $pdfFileName = "directions.pdf" ;

	push @tempFiles, $pdfFileName ;

	print "call latex, dvips and ps2pdf and log to $logFileName\n" ;
	`latex $texFileName >> $logFileName 2>&1` ;
	`latex $texFileName >> $logFileName 2>&1` ;
	`dvips -D600 $dviFileName -o >> $logFileName 2>&1` ;
	`ps2pdf $psFileName $pdfFileName >> $logFileName 2>&1` ;
	`rm *.dvi` ;
	# `rm *.tex` ;
	`rm *.ps` ;
	`rm *.aux` ;
	`rm *.log` ;
	my $target = $workDir . "directions.pdf" ;
	# `mv directions.pdf $target` ;
	print "done\n" ;
}

sub createTitlePage {

	my %heading = () ;
	$heading{"EN"} = "Disclaimer" ;
	$heading{"DE"} = "Haftungsauschluss" ;
	my %heading2 = () ;
	$heading2{"EN"} = "Tour description" ;
	$heading2{"DE"} = "Tourenbeschreibung" ;
	my %heading3 = () ;
	$heading3{"EN"} = "Basic route data" ;
	$heading3{"DE"} = "Basisroutendaten" ;
	my %disclaimer = () ;
	$disclaimer{"EN"} = << "END1" ;
The user of this book is always responsible for all his steps and actions.
The authors of this documentation are not responsible for the correctness and completeness of the data.
Data is derived from openstreetmap and is licensed by CC-BY-SA.
END1
	$disclaimer{"DE"} = << "END2" ;
Der Nutzer dieses Werkes ist stets selbst für seine Schritte und Aktionen ver\\-antwortlich.
Die Ersteller dieser Dokumentation übernehmen keine Garantie für Richtigkeit und Vollständigkeit selbiger.
Die Daten stammen von openstreetmap und stehen unter der CC-BY-SA Lizenz.
END2

	
	print "create title page...\n" ;
	my $texFileName ; 
	$texFileName = "title.tex" ;

	push @tempFiles, $texFileName ;

	open (my $texFile, ">", $texFileName) or die ("can't open tex output file") ;

	if ($pageSizeOpt eq "A5") {
		print $texFile "\\documentclass[a5paper,12pt]{book}\n" ;
	}
	else {
		print $texFile "\\documentclass[a4paper,12pt]{book}\n" ;
	}

	print $texFile "\\usepackage[utf8]{inputenc}\n" ;
	print $texFile "\\begin{document}\n" ;


	#
	# title
	#

	print $texFile "\\thispagestyle{empty}\n" ;
	print $texFile "\\vspace*{8cm}\n" ;
	print $texFile "\\Huge\n" ;
	$title = sanitizeLatexString ($title) ;
	print $texFile "$title \\par\n" ;
	# print $texFile "\\newline\n" ;
	print $texFile "\\vspace*{2cm}\n" ;
	print $texFile "\\normalsize\n" ;
	my %created = () ;
	$created{"EN"} = "created with hikingbook.pl" ;
	$created{"DE"} = "erstellt mit hikingbook.pl" ;
	print $texFile "$created{$languageOpt}\n" ;


	#
	# description
	#

	if ($descName ne "") {
		print $texFile "\\newpage\n" ;
		print "\nreading description file...\n" ;
		open (my $file, "<", $descName) or die ("ERROR: could not open description file $descName\n") ;
			print $texFile "\\section*{$heading2{$languageOpt}}\n" ;
			my $line = "" ; my $c = 0 ;
			while ($line = <$file>) {
				print $texFile $line ;
				$c++ ;
			}
		close ($file) ;	
		print "done. $c lines read.\n" ;
	}



	#
	# basic relation data
	#

	print $texFile "\\newpage\n" ;
	print $texFile "\\thispagestyle{empty}\n" ;

	if ($atlasOpt eq "") {
		print $texFile "\\section*{$heading3{$languageOpt}}\n" ;
		my %tags = () ;
		foreach my $t (@relationTags) {
			my $temp = $t->[1] ;
			$temp = sanitizeLatexString ($temp) ;
			$temp = convertHTML ($temp) ; 
			$tags{ $t->[0] } = $temp ; 
		}
		print $texFile "\\begin{tabular}{|p{4cm}|p{10cm}|}\n" ;
		print $texFile "\\hline\n" ;
		print $texFile "Key & Value \\\\ \n" ;
		print $texFile "\\hline\n" ;
		foreach my $t (sort keys %tags) {
			print $texFile "$t & $tags{$t} \\\\ \n" ;
			print $texFile "\\hline\n" ;
		}
		print $texFile "\\end{tabular}\n" ;
	}


	#
	# disclaimer
	#

	print $texFile "\\section*{$heading{$languageOpt}}\n" ;
	print $texFile "$disclaimer{$languageOpt}\n" ;
	

	print $texFile "\\end{document}\n" ;
	close ($texFile) ;

	my $dviFileName = $texFileName ;
	$dviFileName =~ s/.tex/.dvi/ ;
	my $psFileName = $texFileName ;
	$psFileName =~ s/.tex/.ps/ ;
	my $pdfFileName = "title.pdf" ;

	push @tempFiles, $pdfFileName ;

	print "call latex, dvips and ps2pdf and log to $logFileName\n" ;
	`latex $texFileName >> $logFileName 2>&1` ;
	`latex $texFileName >> $logFileName 2>&1` ;
	`dvips -D600 $dviFileName -o >> $logFileName 2>&1` ;
	`ps2pdf $psFileName $pdfFileName >> $logFileName 2>&1` ;
	`rm *.dvi` ;
	# `rm *.tex` ;
	`rm *.ps` ;
	`rm *.aux` ;
	`rm *.log` ;
	my $target = $workDir . "title.pdf" ;
	print "done.\n" ;
}


sub getProgramOptions {
	my $helpOpt = 0 ;
	my $optResult = GetOptions ( 	"in=s" 		=> \$inFileName,
					"out=s"		=> \$outFileName,		# output file
					"detailstyle=s"	=> \$detailStyleFileName,
					"overviewstyle=s"	=> \$overviewStyleFileName,
					"title=s"	=> \$title,
					"language=s"	=> \$languageOpt,
					"pagesize=s"	=> \$pageSizeOpt,
					"relation=i"	=> \$relationId,
					"atlas=s"	=> \$atlasOpt,
					"name=s"	=> \$relationName,
					"desc=s"	=> \$descName,
					"steps=s"	=> \$stepDescName,
					"ref=s"	=> \$relationRef,
					"overlap=i"	=> \$overlapOpt,
					"dirnumber=i"	=> \$dirNumberOpt,
					"lesssteps=i"	=> \$lessStepsOpt,
					"poi=i"		=> \$poiOpt,
					"street=i"		=> \$streetOpt,
					"scale=i"	=> \$scale,
					"stepsize=i"		=> \$stepSizeOpt,
					"stepcolor=s"		=> \$stepColorOpt,
					"stepfontsize=i"	=> \$stepFontSizeOpt,
					"pnsizeoverview=i"	=> \$pnSizeOverview,
					"pnsizedetail=i"	=> \$pnSizeDetail,
					"landscape"	=> \$landscapeOpt,
					"reverse"	=> \$reverseOpt,
					"autorotate"	=> \$autorotateOpt,
					"nodelete"	=> \$noDeleteOpt,
					"roundtrip"	=> \$roundtripOpt,
					"help"		=> \$helpOpt,
					"verbose" 	=> \$verboseOpt ) ;

	if ( (uc $languageOpt) eq "DE") { 
		$languageOpt = "DE" ;
	}
	else {
		$languageOpt = "EN" ;
	}
	if ( (uc $pageSizeOpt) eq "A5") { 
		$pageSizeOpt = "A5" ;
	}
	else {
		$pageSizeOpt = "A4" ;
	}

	if ($landscapeOpt eq "1") {
		foreach my $f (keys %pageHeight) {
			my $temp = $pageHeight{$f} ;
			$pageHeight{$f} = $pageWidth{$f} ;
			$pageWidth{$f} = $temp ;
		}
	}

	if ($helpOpt eq "1") {
		usage() ;
		die() ;
	}
}

sub usage {
	print "$programName version $version\n" ;
	print "-in=<infile.osm>\n" ;
	print "-out=<outfile.pdf>\n" ;
	print "-relation=<relation id>\n" ;
	print "-name=<relation name>\n" ;
	print "-ref=<relation ref>\n" ;
	print "-atlas=FLOAT,FLOAT,FLOAT,FLOAT (create street atlas for bounding box)\n" ;
	print "-detailstyle=<mapgen rules file for detail maps>\n" ;
	print "-overviewstyle=<mapgen rules file for overview maps>\n" ;
	print "-scale=<integer> (scale for detail maps); default = 10000\n\n" ;

	print "-language=EN|DE\n" ;
	print "-title=\"title text\" (for title page)\n" ;
	print "-desc=<tex file> (for long text after title page; tex format, max heading subsection\n" ;
	print "-steps=<step description file> (for descriptions of single steps or nodes; format: stepNr<space>TEXT)\n" ;
	print "-poi=<integer> (add poi directory to description, specify column number like 1,2,3)\n" ;
	print "-street=<integer> (add street directory to description, specify column number like 1,2,3)\n" ;
	print "-dirnumber=4|8 (4 or 8 different directions like N, S, E, W...); default=8\n" ;
	print "-lesssteps=INTEGER (reduce number of steps in description, step only if direction change > integer degrees)\n" ;
	print "-stepsize=INTEGER (size of step disc in map; 0 will suppress discs and labels)\n" ;
	print "-stepcolor=TEXT (color of step disc in map)\n" ;
	print "-stepfontsize=INTEGER (size of label for step disc in map)\n" ;
	print "\n" ;

	print "-pagesize=A4|A5\n" ;
	print "-overlap=<percent> (10 for 10\% overlap on each side; default=5)\n" ;
	print "-pnsizeoverview=INTEGER (size of page numbers in overview map)\n" ;
	print "-pnsizedetail=INTEGER (size of page numbers in detail maps)\n" ;
	print "-landscape\n" ;
	print "-autorotate (rotate paper format automatically)\n" ;
	print "\n" ;

	print "-reverse (reverse direction of relation/route)\n" ;
	print "-roundtrip (force route to begin/end with first way in relation)\n" ;
	print "\n" ;

	print "-verbose\n" ;
	print "-nodelete (temp files will not be deleted)\n" ;
	print "-help\n" ;
}


sub addPois {
	print "\nadding POIs to description...\n" ;
	foreach my $p (@poiList) {
		my $minDist = 999 ;
		my $nodeNumber = -1 ;
		my $dir = "" ;
		for (my $i = 0; $i <= $#nodes; $i++) {
			my $dist = distance ( $lon {$p->[2]}, $lat {$p->[2]}, $lon{ $nodes[$i] }, $lat{ $nodes[$i] }) ;
			if ($dist < $minDist) {
				$minDist = $dist ;
				$nodeNumber = $i ;
				$dir = direction ( $nodes[$i], $p->[2] ) ;
			}
		}
		if ($minDist < $p->[3]) {
			my $info = $p->[0] ; my $name = $p->[1] ;
			$minDist = int ($minDist * 100) / 100 ;
			push @{$nodeInfo{$nodeNumber}{"information"}}, "$info $name ($dir $minDist)" ;
		}
	}
}

sub getPoiFiledata {
	print "\nreading poi file...\n" ;
	my $result = open (my $file, "<", $poiFileName) ;
	if ($result) {
		my $line = "" ;
		my $count = 0 ;
		while ($line = <$file>) {
			my ($k, $v, $d, $en, $de) = ( $line =~ /(.+),(.+),(.+),(.+),(.+)/ ) ;
			if ( (defined $k) and (defined $v) and (defined $d) and (defined $en) and (defined $de) ) {
				$count++ ;
				push @pois, [$k, $v, $d, $en, $de] ;
				if ($verboseOpt eq "1") { print "POI $k=$v read from file.\n" ; }
			}
		}
		close ($file) ;
		print "$count pois read.\n" ;
	}
	else {
		print "WARNING: poi file $poiFileName could not be opened!\n" ;
	}
}

sub getStepInformation {
	if ($stepDescName ne "") {
		print "\nreading step information file...\n" ;
		open (my $file, "<", $stepDescName) or die ("ERROR: step information file $stepDescName could not be opened!\n") ;
		my $line = "" ; my $c = 0 ;
		while ($line = <$file>) {
			my ($nr, $text) = ($line =~ /([\d\-]+)\s(.+)/ ) ;
			if ( (defined $nr) and (defined $text) ) {
				$c++ ;
				$stepInformation{$nr} = $text ;
				if ($verboseOpt eq "1") {
					print "$nr $text\n" ;
				}
			}
			else {
				print "problem reading line from step information file:\n$line\n" ;
			}
		}
		close ($file) ;
		print "done. $c lines read.\n" ;
	}
}




sub createDirectory {

	if ( ($poiOpt > 0) or ($streetOpt > 0) ) {

		if ($poiOpt < 1) { $poiOpt = 1 ; } 
		if ($poiOpt > 3) { $poiOpt = 3 ; } 
		if ($streetOpt < 1) { $streetOpt = 1 ; } 
		if ($streetOpt > 3) { $streetOpt = 3 ; } 
		$poiOpt = int $poiOpt ;
		$streetOpt = int $streetOpt ;

		print "create directory...\n" ;
		my $texFileName ; 
		$texFileName = "directory.tex" ;

		push @tempFiles, $texFileName ;

		open (my $texFile, ">", $texFileName) or die ("can't open tex output file") ;

		if ($pageSizeOpt eq "A5") {
			print $texFile "\\documentclass[a5paper,12pt]{book}\n" ;
		}
		else {
			print $texFile "\\documentclass[a4paper,12pt]{book}\n" ;
		}

		print $texFile "\\usepackage[utf8]{inputenc}\n" ;
		print $texFile "\\usepackage{multicol}\n" ;
		print $texFile "\\begin{document}\n" ;

		if ($poiOpt > 0) {
			print $texFile "\\thispagestyle{empty}\n" ;

			my %heading3 = () ;
			$heading3{"EN"} = "Points of interest" ;
			$heading3{"DE"} = "Interessante Punkte" ;

			if ($poiOpt > 1) {
				print $texFile "\\begin{multicols}{$poiOpt}[\\section*{$heading3{$languageOpt}}]\n" ;
			}
			else {
				print $texFile "\\section*{$heading3{$languageOpt}}\n" ;
			}
			print $texFile "\\tiny\n" ;

			foreach my $poi (sort keys %pois) {
				my $sqText = "" ; my $first = 1 ;
				foreach my $sq ( @{$pois{$poi}} ) {
					if ($first) {
						$first = 0 ;
						$sqText .= $sq ;
					}
					else {
						$sqText .= " " ;
						$sqText .= $sq ;
					}
				}
				$poi = sanitizeLatexString ($poi) ;
				print $texFile $poi ;
				print $texFile " \\dotfill " ;
				print $texFile $sqText, "\\\\\n" ;
			}

			if ($poiOpt > 1) {
				print $texFile "\\end{multicols}\n" ;
			}
			print $texFile "\\normalsize\n" ;
		} # poiOpt > 0
	
		if ($streetOpt > 0) {
			print $texFile "\\thispagestyle{empty}\n" ;

			my %heading4 = () ;
			$heading4{"EN"} = "Street Directory" ;
			$heading4{"DE"} = "Straßenverzeichnis" ; 

			if ($streetOpt > 1) {
				print $texFile "\\begin{multicols}{$streetOpt}[\\section*{$heading4{$languageOpt}}]\n" ;
			}
			else {
				print $texFile "\\section*{$heading4{$languageOpt}}\n" ;
			}
			print $texFile "\\tiny\n" ;

			foreach my $street (sort keys %streets) {
				my $sqText = "" ; my $first = 1 ;
				foreach my $sq ( @{$streets{$street}} ) {
					if ($first) {
						$first = 0 ;
						$sqText .= $sq ;
					}
					else {
						$sqText .= " " ;
						$sqText .= $sq ;
					}
				}
				$street = sanitizeLatexString ($street) ;
				print $texFile $street ;
				print $texFile " \\dotfill " ;
				print $texFile $sqText, "\\\\\n" ;
			}

			if ($streetOpt > 1) {
				print $texFile "\\end{multicols}\n" ;
			}
			print $texFile "\\normalsize\n" ;
		} # streetOpt > 0
	

		print $texFile "\\end{document}\n" ;
		close ($texFile) ;

		my $dviFileName = $texFileName ;
		$dviFileName =~ s/.tex/.dvi/ ;
		my $psFileName = $texFileName ;
		$psFileName =~ s/.tex/.ps/ ;
		my $pdfFileName = "directory.pdf" ;

		push @tempFiles, $pdfFileName ;

		print "call latex, dvips and ps2pdf and log to $logFileName\n" ;
		`latex $texFileName >> $logFileName 2>&1` ;
		`latex $texFileName >> $logFileName 2>&1` ;
		`dvips -D600 $dviFileName -o >> $logFileName 2>&1` ;
		`ps2pdf $psFileName $pdfFileName >> $logFileName 2>&1` ;
		`rm *.dvi` ;
		`rm *.ps` ;
		`rm *.aux` ;
		`rm *.log` ;
		print "done.\n" ;

	}
}


sub convertHTML {
	my $line = shift ;

	($line) = ($line =~ /^(.*)$/ ) ;

	$line =~ s/\&apos;/\'/g ;
	$line =~ s/\&quot;/\'/g ;

	return $line ;
}

sub dirChange {
	my ($a, $b) = @_ ;

	my $c1 = abs ($a - $b) ;
	my $c2 = 360 - max ($a, $b) + min ($a, $b) ;

	my $c3 = min ($c1, $c2) ;
	return $c3 ;
}


sub min {
	my ($a, $b) = @_ ;
	if ($a < $b) {
		return $a ;
	}
	else {
		return $b ;
	}
}

sub max {
	my ($a, $b) = @_ ;
	if ($a > $b) {
		return $a ;
	}
	else {
		return $b ;
	}
}

sub initializeLatex {

	$replaceLatex{'_'} = '\\_' ;	
	$replaceLatex{'&'} = '\\&' ;	
	$replaceLatex{'$'} = '\\$' ;	
	$replaceLatex{'%'} = '\\%' ;	
	$replaceLatex{'{'} = '\\{' ;	
	$replaceLatex{'}'} = '\\}' ;	
	$replaceLatex{'#'} = '\\#' ;	

	foreach my $c (0..9) {
		$validLatex{$c} = 1 ;
	}
	foreach my $c ("a".."z") {
		$validLatex{$c} = 1 ;
	}
	foreach my $c ("A".."Z") {
		$validLatex{$c} = 1 ;
	}
	foreach my $c ( qw (! + * - : . ; § / = ?) ) {
		$validLatex{$c} = 1 ;
	}
	foreach my $c ( ' ', '"', "\'", '(', ')', '[', ']', ',' ) {
		$validLatex{$c} = 1 ;
	}
}

sub sanitizeLatexString {
	my $text = shift ;

	my $out = "" ;
	my @chars ;
	@chars = split //, $text ;

	for (my $i=0; $i<=$#chars; $i++) {
		my $c = $chars[$i] ;
		# print "$c ", ord ($c), "\n" ;
		if (defined $validLatex{$c}) {
			$out .= $c ;
		}
		elsif (defined $replaceLatex{$c}) {
			$out .= $replaceLatex{$c} ;
		}
		else {
			if ( ($i < $#chars) and ($c eq chr (195)) ) {
				my $found = 0 ;
				foreach my $c2 (164, 182, 188, 132, 150, 156, 159) {
					if ($chars[$i+1] eq chr($c2)) {
						$found = 1 ;
					}
				}
				if ($found) {
					$out .= $c . $chars[$i+1] ;
					$i++ ;
				}
			}
		}
	} 
	return $out ;
}


sub compileElevationData {
	print "evaluating contour data...\n" ;

	foreach my $wayId (keys %contourWays) {

		my ($x1, $x2, $y1, $y2) = getArea ( @{$wayNodesHash{$wayId}} );

		$qt->add ($wayId, $x1, $y1, $x2, $y2) ;
	}


	my $count = 0 ;
	for (my $a = 0; $a < $#nodes; $a++) { #way 1
		# print "a = $a\n" ;

		my $ref = $qt->getEnclosedObjects ( 	min( $lon{$nodes[$a]}, $lon{$nodes[$a+1]}),
							min( $lat{$nodes[$a]}, $lat{$nodes[$a+1]}),
							max( $lon{$nodes[$a]}, $lon{$nodes[$a+1]}),
							max( $lat{$nodes[$a]}, $lat{$nodes[$a+1]}) ) ;
		my @temp = @$ref ;

		foreach my $cw ( @temp ) { #way 2
		# foreach my $cw (keys %contourWays) { #way 2
			for ($b=0; $b<$#{$wayNodesHash{$cw}}; $b++) {
				# print "cw= $cw\n" ;
				# print "b = $b\n" ;
				my ($x, $y) = crossing ($lon{ $nodes[$a] }, 
						$lat{ $nodes[$a] }, 
						$lon{ $nodes[$a+1] }, 
						$lat{ $nodes[$a+1] }, 
						$lon{$wayNodesHash{$cw}[$b]}, 
						$lat{$wayNodesHash{$cw}[$b]}, 
						$lon{$wayNodesHash{$cw}[$b+1]}, 
						$lat{$wayNodesHash{$cw}[$b+1]}) ;
				if (($x != 0) and ($y != 0)) {
					$count ++ ;
					my $dist = $nodeInfo{$a}{"distance"} + distance ($lon{$nodes[$a]}, $lat{$nodes[$a]}, $x, $y) ;
					$contourData{$dist} = $contourWays { $cw } ;
					if ($verboseOpt eq "1") {
						print "contour intersection found: ($x, $y), $dist, $contourWays{$cw}\n" ;
					}
				}
			}
		}
	}
	print "done. $count intersections found.\n" ;
}


sub getArea {
	my @nodes = @_ ;

	my $minLon = 999 ;
	my $maxLon = -999 ;
	my $minLat = 999 ;
	my $maxLat = -999 ;


	foreach my $node (@nodes) {
		if ($lon{$node} > $maxLon) { $maxLon = $lon{$node} ; }
		if ($lon{$node} < $minLon) { $minLon = $lon{$node} ; }
		if ($lat{$node} > $maxLat) { $maxLat = $lat{$node} ; }
		if ($lat{$node} < $minLat) { $minLat = $lat{$node} ; }
	}	
	return ($minLon, $maxLon, $minLat, $maxLat) ;
}


sub getMinMax {
	my $fileName = shift ;
	# store min / max of temp file
	openOsmFile ($fileName) ;
	print "reading temp file nodes...\n" ;

	my $file2LonMax = -999 ;
	my $file2LonMin = 999 ;
	my $file2LatMax = -999 ;
	my $file2LatMin = 999 ;
	my $propRef ; my $tagsRef ;
	($propRef, $tagsRef) = getNode3() ;
	while (defined $propRef) {
		if ( $$propRef{"lon"} > $file2LonMax ) { $file2LonMax = $$propRef{"lon"} ; }

		if ( $$propRef{"lon"} < $file2LonMin ) { $file2LonMin = $$propRef{"lon"} ; }
		if ( $$propRef{"lat"} > $file2LatMax ) { $file2LatMax = $$propRef{"lat"} ; }
		if ( $$propRef{"lat"} < $file2LatMin ) { $file2LatMin = $$propRef{"lat"} ; }

		($propRef, $tagsRef) = getNode3() ;
	}
	closeOsmFile() ;
	print "done.\n" ;
	return ($file2LonMin, $file2LatMin, $file2LonMax, $file2LatMax) ;
}


sub loadExternalFiles {
	my $outName = shift ;
	my $poiName = $outName ;
	if ($poiOpt > 0) {
		$poiName =~ s/\.svg/\_pois\.txt/ ;
		open (my $file, "<", $poiName) or die ("ERRROR: could not open poi file name $poiName\n") ;

		my $line = "" ;
		while ($line = <$file>) {
			# print "POI line read : $line" ;
			my ($poi, $fields) = ( $line =~ /(.+)\t(.*)/ ) ;
			if ( (defined $poi) and (defined $fields) ) {
				my @f = split / /, $fields ;
				foreach my $sq (@f) {
					$poi = convertHTML ($poi) ;

					# translation German
					if ($languageOpt ne "EN") {
						foreach my $k (keys %{ $translations{$languageOpt} } ) {
							my $v = $translations{$languageOpt}{$k} ;
							$poi =~ s/\($k\)/\($v\)/gi ;
						}
					}

					$poi =~ s/\_/ /g ;
					push @{$pois{$poi}}, $mapNumber . "-" . $sq ;
				}
			}
		}
		close ($file) ;
		push @tempFiles, $poiName ;
	}

	my $streetName = $outName ;
	if ($streetOpt > 0) {
		$streetName =~ s/\.svg/\_streets\.txt/ ;
		open (my $file, "<", $streetName) or die ("ERRROR: could not open street file name $streetName\n") ;
		my $line = "" ;
		while ($line = <$file>) {
			# print "STREET line read : $line" ;
			my ($street, $fields) = ( $line =~ /(.+)\t(.*)/ ) ;
			if ( (defined $street) and (defined $fields) ) {
				my @f = split / /, $fields ;
				foreach my $sq (@f) {
					$street = convertHTML ($street) ;
					push @{$streets{$street}}, $mapNumber . "-" . $sq ;
				}
			}
		}
		close ($file) ;
		push @tempFiles, $streetName ;
	}

}
