#!/usr/bin/perl -w

	#ÊPreprocess .osm file for cycling map
	# 1. changes highway and ref tags for easier processing
	# 2. calls osmcut to split into smaller .osm files
	# 3. calls mkgmap to create map

	# Richard Fairhurst 8-13.1.08, 6.4.08
	# released under the WTFPL

	# To use:
	# - download mkgmap
	# - download osmcut and put the .jar in the mkgmap/ directory
	# - put this file in the mkgmap/ directory
	# - put cycling-map-features.csv in mkgmap/resources
	# - perl preprocess.pl yourosmfile.osm
	# ...and the result is a gmapsupp.img file to copy to your Garmin

	# Still to do:
	# - rationalise 'proposed routes' code?
	# - cycle node handling

	use IO::Uncompress::Bunzip2 qw ($Bunzip2Error);
	use IO::File;

	# =====	Constants

	use constant NODE     => 1;
	use constant WAY      => 2;
	use constant RELATION => 3;
	
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
	
	# -----	Read in only the tags we use
	
	%usetag=('name',1, 'ref',1, 'route',1,
			 'ncn',1, 'ncn_ref',1,
			 'rcn',1, 'rcn_ref',1,
			 'lcn',1, 'lcn_ref',1);
	open (INFILE,"resources/noname-map-features.csv") or die "Can't find cycling styles: $!\n";
	while (<INFILE>) {
		chomp ($t=$_);
		if ($t=~/^\w+\|(\w+)/) { $usetag{$1}=1; }
	}
	close INFILE;
	
	# =====	Open file
	
	$fn=shift @ARGV;
	$ofn=Open_File($fn);
	open (OUTFILE,">$ofn") or die "Couldn't open output file: $!\n";
	
	# -----	Read relations

	print "Reading relations\n";
	# uncomment following line if you've saved relations into a separate file
	# $fh->close; $fh=new IO::File; $fh->open("relations_only.txt");
	%wayrefs=();
	%waytypes=();
	$in=0;
	while (<$fh>) {
		chomp ($t=$_);
		if ($t=~/^  <relation id="(\d+)".+>$/) {
			$c=$1; print "relation $c     \r";
			$in=RELATION; %tags=(); @members=();
			$tags{'type'}=''; $tags{'route'}='bicycle';
			$tags{'state'}=''; $tags{'network'}='';
		} elsif ($t =~/^    <member type="way" ref="(\d+)".*\/>$/) {
			push @members,$1;
		} elsif ($t =~/^    <tag k="(.+)" v="(.*)"\s*\/>$/ and $in==RELATION) {
			$tags{$1}=$2;
		} elsif ($t eq '  </relation>') {
			$in=0;
			next if ($tags{'type'} ne 'route');
			next if ($tags{'route'} ne 'bicycle');
			# What network are we in? (default LCN)
			$cycle=LCN; $prefix=''; $suffix='';
			if    ($tags{'network'} eq 'ncn') { $cycle=NCN; }
			elsif ($tags{'network'} eq 'rcn') { $cycle=RCN; $prefix='R'; }
			elsif ($tags{'network'} eq 'lcn') { $cycle=LCN; $prefix='L'; }
			if ($tags{'state'} eq 'proposed') { $cycle+=(NCN_PROPOSED)-(NCN); $suffix='*'; }
			# What's the ref? (fallback to name if none)
			$n='';
			if (exists $tags{'ref'}) { $n=$prefix.$tags{'ref'}.$suffix; }
			elsif (exists $tags{'name'}) { $n=$tags{'name'}.$suffix; }
			elsif (exists $tags{'network'}) { $n=$tags{'network'}.$suffix; }
			if ($n ne '') { $n.=' '; }
			# Set in all members
			foreach $m (@members) {
				if   (!exists $waytypes{$m}) { $waytypes{$m}=$cycle; }
				elsif ($waytypes{$m}>$cycle) { $waytypes{$m}=$cycle; }
				if (!exists $wayrefs{$m}) { $wayrefs{$m} =$n; }
									 else { $wayrefs{$m}.=$n; }
			}
		}
	}
	$fh->close;
	$in=0;
	
	# -----	Read whole file
	
	print "Reading tags and ways\n";
	Open_File($fn);
	while (<$fh>) {
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
			$refnum=''; $highway='';
			$cycle=NONE; if (exists $waytypes{$c}) { $cycle=$waytypes{$c}; }
			$cycleref=''; if (exists $wayrefs{$c}) { $cycleref=$wayrefs{$c}; }
			if (exists $tags{'ref'})		{ $refnum=$tags{'ref'}; delete $tags{'ref'}; }
			if (exists $tags{'highway'})	{ $highway=$tags{'highway'}; delete $tags{'highway'}; }
			if (exists $tags{'ncn'})		{ if ($tags{'ncn'} eq 'proposed') { Set_Cycle(NCN_PROPOSED); }
																	     else { Set_Cycle(NCN); } 
											  delete $tags{'ncn'}; }
			elsif (exists $tags{'rcn'})		{ if ($tags{'rcn'} eq 'proposed') { Set_Cycle(RCN_PROPOSED); }
																	     else { Set_Cycle(RCN); }
											  delete $tags{'rcn'}; }
			elsif (exists $tags{'lcn'})		{ if ($tags{'lcn'} eq 'proposed') { Set_Cycle(LCN_PROPOSED); }
																	     else { Set_Cycle(LCN); }
											  delete $tags{'lcn'}; }
			if (exists $tags{'route'})		{ if    ($tags{'route'} eq 'ncn'	) { Set_Cycle(NCN); }
											  elsif ($tags{'route'} eq 'rcn'	) { Set_Cycle(RCN); }
											  elsif ($tags{'route'} eq 'lcn'	) { Set_Cycle(LCN); }
											  delete $tags{'route'}; }

			#	munge ref tag
			if (exists $tags{'ncn_ref'})	{ if ($cycle!=NCN_PROPOSED) { Set_Cycle(NCN); }
											  $cycleref.=$tags{'ncn_ref'}; delete $tags{'ncn_ref'};
											  if ($cycle==NCN_PROPOSED) { $cycleref.='*'; } 
											  $cycleref.=' '; }
			if (exists $tags{'rcn_ref'})	{ if ($cycle!=RCN_PROPOSED) { Set_Cycle(RCN); }
											  $cycleref.='R'.$tags{'rcn_ref'}; delete $tags{'rcn_ref'};
											  if ($cycle==RCN_PROPOSED) { $cycleref.='*'; } 
											  $cycleref.=' '; }
			if (exists $tags{'lcn_ref'})	{ if ($cycle!=LCN_PROPOSED) { Set_Cycle(LCN); }
											  $cycleref.='L'.$tags{'lcn_ref'}; delete $tags{'lcn_ref'};
											  if ($cycle==LCN_PROPOSED) { $cycleref.='*'; }
											  $cycleref.=' '; }
			$refnum=$cycleref.$refnum; $refnum=~s/\s+$//;
			if ($refnum) { $tags{'ref'}=$refnum; }

			#	munge highway tag
			if (exists $roads{$highway}) { $hwp=$roads{$highway}; }
									else { $hwp='offroad'; }
			if (!(exists $roads{$highway} and not exists $tags{'name'})) { #don't munge highway tag if it needs the noname treatment...
				if    ($cycle==NCN         ) { $highway="ncn_$hwp"; }
				elsif ($cycle==NCN_PROPOSED) { $highway="ncn_$hwp"; }
				elsif ($cycle==RCN         ) { $highway="rcn_$hwp"; }
				elsif ($cycle==NCN_PROPOSED) { $highway="rcn_$hwp"; }
				elsif ($cycle==LCN         ) { $highway="lcn_$hwp"; }
				elsif ($cycle==LCN_PROPOSED) { $highway="lcn_$hwp"; }
			}
			if ($highway) { $tags{'highway'}=$highway; }

			#	fix annoying case where name=ref
			if (exists $tags{'ref'} and exists $tags{'name'}) {
				if ($tags{'ref'} eq $tags{'name'}) { delete $tags{'name'}; }
			}

                        # munge highway tag if it's a road with no name
                        if (exists $roads{$highway} and not exists $tags{'name'}) {
                                $tags{'highway'} = 'noname';
                        }


			#	write tags
			foreach $k (keys %tags) { print OUTFILE "    <tag k=\"$k\" v=\"$tags{$k}\" />\n"; }
#			if ($cycle!=NONE or $cycleref) { print "$refnum ($highway)\n"; }

		} elsif ($t =~/^    <tag k="(.+)" v="(.*)"\s*\/>$/) {
			# - read tag
			if ($usetag{$1}) { $tags{$1}=$2; }
			$t="";
		}
		if ($t) { $t=~s/^\s+//; print OUTFILE "$t\n"; }
	}

	$fh->close;
	close OUTFILE;

	# ===== Call osmcut to split into smaller files
	#		(probably save in temporary directory)

	$tempdir='temp';
	print "Splitting with osmcut\n";
	system ("mkdir $tempdir");
        system ("./osmcut --force -s 2 -d $tempdir $ofn");
        #system ("java -Xmx512M -jar osmcut.0.5.jar 0.5 $ofn $tempdir");
	system ("rm $ofn");
	
	# =====	Call mkgmap to create map
	#		(delete temporary directory afterwards)
	
	print "Creating Garmin map with mkgmap\n";
	system ("java -Xmx512M -jar mkgmap.jar --map-features=resources/noname-map-features.csv --gmapsupp $tempdir/*");
	print "Deleting temporary files\n";
	system ("rm 6*.img");
	system ("rm 6*.tdb");
	system ("rm -rf $tempdir");
	print "Finished!\n";


	# ===========================
	# General-purpose subroutines
	
	sub Set_Cycle { if ($_[0]<$cycle) { $cycle=$_[0]; } }

	sub Open_File {
		my $fn=$_[0];
		if ($fn=~/^(.+)\.bz2$/) {
			$fh=new IO::Uncompress::Bunzip2 $fn or die "Couldn't open bzipped input file: $Bunzip2Error\n";
			return "$1.processed";
		} else {
			$fh=new IO::File($fn) or die "Couldn't open input file: $!\n";
			return "$fn.processed";
		}
	}
