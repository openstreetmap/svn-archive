#!/usr/bin/perl -w

	#ÊPreprocess .osm file for cycling map
	# 1. changes highway and ref tags for easier processing
	# 2. calls osmcut to split into smaller .osm files
	# 3. calls mkgmap to create map

	# Richard Fairhurst 8-13.1.08
	# released under the WTFPL

	# To use:
	# - download mkgmap
	# - download osmcut and put the .jar in the mkgmap/ directory
	# - put this file in the mkgmap/ directory
	# - put cycling-map-features.csv in mkgmap/resources
	# - perl preprocess.pl yourosmfile.osm
	# ...and the result is a gmapsupp.img file to copy to your Garmin

	# Still to do:
	# - read bzipped file directly?
	# - rationalise 'proposed routes' code?
	# - cycle node handling
	
	# =====	Constants

	use constant NODE => 1;
	use constant WAY  => 2;
	
	use constant NCN  		  => 1;
	use constant RCN		  => 2;
	use constant LCN		  => 3;
	use constant NCN_PROPOSED => 4;
	use constant RCN_PROPOSED => 5;
	use constant LCN_PROPOSED => 6;
	use constant NONE		  => 9;

	# -----	Map OSM highway tags to ncn_/rcn_/lcn_ suffixes
	#		so a local route on a trunk road would become lcn_major
	
	%roads=('motorway','major',
			'motorway_link','major',
			'trunk','major',
			'trunk_link','major',
			'primary','major',
			'primary_link','major',
			'secondary','minor',
			'tertiary','minor',
			'unclassified','minor',
			'residential','minor',
			'service','minor',
			'living_street','minor');
	
	# -----	Other routes to highlight (using route= tag)
	
	%routes=('National Byway',NCN,
			 'Four Castles Cycle Route',RCN);
	
	# -----	Read in only the tags we use
	
	%usetag=('name',1, 'ref',1, 'route',1,
			 'ncn',1, 'ncn_ref',1,
			 'rcn',1, 'rcn_ref',1,
			 'lcn',1, 'lcn_ref',1);
	open (INFILE,"resources/cycling-map-features.csv") or die "Can't find cycling styles: $!\n";
	while (<INFILE>) {
		chomp ($t=$_);
		if ($t=~/^\w+\|(\w+)/) { $usetag{$1}=1; }
	}
	close INFILE;
	
	# =====	Process file
	
	$fn=shift @ARGV;
	open (INFILE,$fn) or die "Couldn't open input file: $!\n";
	open (OUTFILE,">$fn.processed") or die "Couldn't open output file: $!\n";
	print "Processing $fn\n";
	
	while (<INFILE>) {
		chomp ($t=$_);
		$t=~s/ timestamp="[^"]+"//;
		if ($t=~/^  <node id="(\d+)".+">$/) {
			# - Start of node element
			$c=$1; if ($c=~/[05]0$/) { print "node $c    \r"; }
			$in=NODE; %tags=();
		} elsif ($t=~/^  <way id="(\d+)".*>$/) {
			# - Start of way element
			$c=$1; if ($c=~/[05]0$/) { print "way $c    \r"; }
			$in=WAY; %tags=();
		} elsif ($t eq '  </node>') {
			# -	End of node element, process tags
			$in=0;
			if (exists $tags{'created_by'}) { delete $tags{'created_by'}; }
			foreach $k (keys %tags) { print OUTFILE "    <tag k=\"$k\" v=\"$tags{$k}\" />\n"; }
		} elsif ($t eq '  </way>') {
			# - End of way element, process tags
			$in=0;
			$refnum=''; $highway=''; $cycle=NONE; $cycleref='';
			if (exists $tags{'ref'})		{ $refnum=$tags{'ref'}; delete $tags{'ref'}; }
			if (exists $tags{'highway'})	{ $highway=$tags{'highway'}; delete $tags{'highway'}; }
			if (exists $tags{'route'})		{ if ($tags{'route'} eq 'ncn'	) { $cycle=NCN; }
											  elsif ($tags{'route'} eq 'rcn'	) { $cycle=RCN; }
											  elsif ($tags{'route'} eq 'lcn'	) { $cycle=LCN; }
											  elsif (exists $routes{$tags{'route'}}) { $cycle=$routes{$tags{'route'}}; 
											  										   $cycleref.=$tags{'route'}.' '; }
											  delete $tags{'route'}; }
			if (exists $tags{'ncn'})		{ if ($tags{'ncn'} eq 'proposed') { $cycle=NCN_PROPOSED; }
																	     else { $cycle=NCN; } 
											  delete $tags{'ncn'}; }
			elsif (exists $tags{'rcn'})		{ if ($tags{'rcn'} eq 'proposed') { $cycle=NCN_PROPOSED; }
																	     else { $cycle=RCN; }
											  delete $tags{'rcn'}; }
			elsif (exists $tags{'lcn'})		{ if ($tags{'lcn'} eq 'proposed') { $cycle=NCN_PROPOSED; }
																	     else { $cycle=LCN; }
											  delete $tags{'lcn'}; }

			#	munge ref tag
			if (exists $tags{'ncn_ref'})	{ if ($cycle>NCN and $cycle!=NCN_PROPOSED) { $cycle=NCN; }
											  $cycleref.=$tags{'ncn_ref'}; delete $tags{'ncn_ref'};
											  if ($cycle==NCN_PROPOSED) { $cycleref.='*'; } 
											  $cycleref.=' '; }
			if (exists $tags{'rcn_ref'})	{ if ($cycle>RCN and $cycle!=RCN_PROPOSED) { $cycle=RCN; }
											  $cycleref.='R'.$tags{'rcn_ref'}; delete $tags{'rcn_ref'};
											  if ($cycle==RCN_PROPOSED) { $cycleref.='*'; } 
											  $cycleref.=' '; }
			if (exists $tags{'lcn_ref'})	{ if ($cycle>LCN and $cycle!=LCN_PROPOSED) { $cycle=LCN; }
											  $cycleref.='L'.$tags{'lcn_ref'}; delete $tags{'lcn_ref'};
											  if ($cycle==LCN_PROPOSED) { $cycleref.='*'; }
											  $cycleref.=' '; }
			$refnum=$cycleref.$refnum; $refnum=~s/\s+$//;
			if ($refnum) { $tags{'ref'}=$refnum; }

			#	munge highway tag
			if (exists $roads{$highway}) { $hwp=$roads{$highway}; }
									else { $hwp='offroad'; }
			if    ($cycle==NCN         ) { $highway="ncn_$hwp"; }
			elsif ($cycle==NCN_PROPOSED) { $highway="ncn_$hwp"; }
			elsif ($cycle==RCN         ) { $highway="rcn_$hwp"; }
			elsif ($cycle==NCN_PROPOSED) { $highway="rcn_$hwp"; }
			elsif ($cycle==LCN         ) { $highway="lcn_$hwp"; }
			elsif ($cycle==LCN_PROPOSED) { $highway="lcn_$hwp"; }
			if ($highway) { $tags{'highway'}=$highway; }

			#	fix annoying case where name=ref
			if (exists $tags{'ref'} and exists $tags{'name'}) {
				if ($tags{'ref'} eq $tags{'name'}) { delete $tags{'name'}; }
			}

			#	write tags
			foreach $k (keys %tags) { print OUTFILE "    <tag k=\"$k\" v=\"$tags{$k}\" />\n"; }
#			if ($cycle!=NONE or $cycleref) { print "$refnum ($highway)\n"; }

		} elsif ($t =~/^    <tag k="(.+)" v="(.*)" \/>$/) {
			# - read tag
			if ($usetag{$1}) { $tags{$1}=$2; }
			$t="";
		}
		if ($t) { $t=~s/^\s+//; print OUTFILE "$t\n"; }
	}

	close INFILE;
	close OUTFILE;

	# ===== Call osmcut to split into smaller files
	#		(probably save in temporary directory)

	$tempdir='temp'.localtime;
	print "Splitting with osmcut\n";
	system ("mkdir $tempdir");
	system ("java -Xmx512M -jar osmcut.0.5.jar 0.5 $fn.processed $tempdir");
	system ("rm $fn.processed");
	
	# =====	Call mkgmap to create map
	#		(delete temporary directory afterwards)
	
	print "Creating Garmin map with mkgmap\n";
	system ("java -Xmx512M -jar mkgmap.jar --map-features=resources/cycling-map-features.csv --gmapsupp $tempdir/*");
	system ("rm -rf $tempdir");
	system ("rm 6*.img");
	system ("rm 6*.tdb");
	print "Finished!\n";
