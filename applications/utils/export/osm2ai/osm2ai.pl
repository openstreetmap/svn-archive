#!/usr/bin/perl -w

	# osm2ai.pl
	# Create Adobe Illustrator 6 file from OpenStreetMap data
	
	# Richard Fairhurst/editions Systeme D 2007-8
	# distributed under the terms of the WTFPL
	# http://sam.zoy.org/wtfpl/COPYING
	
	# Options:
	# --xmin, --xmax, --ymin, --ymax	bounding box
	# --projection						mercator or osgb
	# --output							output filename
	# --filters							filter filename
	# --db, --user, --password			database connection (will use environment variables if present)

	# ----------------------------------------------------------------
	# To do (also see 'limitations'):
	# - option to generate grid (OSGB) or use fixed scale
	# - instead of bounding box, --place=Oxford, --radius=20
	# - optional join on the way_tags table to reduce number of ways returned

	# ================================================================
	# Initialise
	
	use DBI;
	use Math::Trig;
	use Getopt::Long;
	use Pod::Usage;
	use Geo::Coordinates::OSGB qw(ll_to_grid);

	# ================================================================
	# User-defined variables	

	$dbname='openstreetmap'; if (exists $ENV{'DBNAME'}) { $dbname=$ENV{'DBNAME'}; };
	$dbuser='openstreetmap'; if (exists $ENV{'DBUSER'}) { $dbname=$ENV{'DBUSER'}; };
	$dbpass='openstreetmap'; if (exists $ENV{'DBPASS'}) { $dbname=$ENV{'DBPASS'}; };

	$xmin=-3.7; $xmax= 1.9; $ymin=50.5 ; $ymax=54.3;	#	$xmin=-2.3; $xmax=-2.2; $ymin=52.15; $ymax=52.25;
	$proj='mercator';									#	$proj='osgb';
	$outfile='output.ai';
	$filters='';
	$help=$man=0;
	
	GetOptions('xmin=f'=>\$xmin, 'xmax=f'=>\$xmax,
			   'ymin=f'=>\$ymin, 'ymax=f'=>\$ymax,
			   'projection=s'=>\$proj, 'help|?' => \$help, man => \$man,
			   'output=s'=>\$outfile, 'filters=s'=>\$filters,
			   'password=s'=>\$dbpass, 'user=s'=>\$dbuser, 'db=s'=>\$dbname);
	pod2usage(1) if $help;
	pod2usage(-exitstatus => 0, -verbose => 2) if $man;
	$proj=lc $proj;

	# Set from options

	$dbh=DBI->connect("DBI:mysql:$dbname",$dbuser,$dbpass, { RaiseError =>1 } );
	if ($proj eq 'osgb') {
		($basex,$basey)=ll_to_grid($ymin,$xmin); $masterscale=1/250;
	} else {
		$baselong=$xmin; $basey=lat2y($ymin);
		$masterscale=500/($xmax-$xmin);
	}

	# Read filters
	
	if ($filters) {
		open INFILE,$filters or die "Can't open filters file: $!\n";
		@conditions=<INFILE>;
		close INFILE;
	} else {
		@conditions=('railway: railway=*',
					 'motorway: highway=motorway',
					 'trunk: highway=trunk',
					 'primary: highway=primary',
					 'secondary: highway=secondary',
					 'residential: highway=residential',
					 'unclassified: highway=unclassified',
					 'other highway: highway=*',
					 'other: =');
	}

	# ================================================================
	# Generate file

	# -----	Process conditions

	%layer=(); @layers=();
	foreach $condition (@conditions) {
		die unless $condition=~/^(.+):/;
		unless (exists $layer{$1}) { unshift @layers,$1; }
		$layer{$1}=[];
	}

	print "Getting list of ways\n";
	@waylist=Which_Ways();

	$i=0; $al=@waylist;
	foreach $way (@waylist) {
		$i++;
		print "Reading way $i of $al\r";
		($path,$attribute)=Get_Way($way);						# Read the way
		$paths{$way}=$path;
		$attributes{$way}=$attribute;

		CONDS: foreach $condition (@conditions) {				#ÊWhich conditions does it satisfy?
			next unless $condition=~/^(.+):\s*(.*)=(.*)$/;		#Ê |
			$l=$1; $k=$2; $v=$3;								#  |
			if (exists(${$attribute}{$k})) {					#  |
				if ($v eq '*' or ${$attribute}{$k} eq $v) {		#  |
					push @{$layer{$l}},$way; last CONDS;		#  |
				}												#  |
			}													#  |
			if ($k eq '') {										#  |
				push @{$layer{$l}},$way; last CONDS;			#  |
			}													#  |
		}
	}

	# -----	Write file

	open (OUTFILE, ">$outfile") or die "Can't open output file: $!";

	print "Writing file                               \n";
	Illustrator_Header();
	foreach $layername (@layers) {
		Illustrator_New_Layer($layername,"0 0 0 1");
		foreach $way (@{$layer{$layername}}) {
			$path=$paths{$way};
			New_Path($attributes{$way});
			foreach $row (@{$path}) {
				Output_Point($row->[0],$row->[1]);
			}
		}
		New_Path();
	}

	Illustrator_Footer();
	close OUTFILE;



	# ================================================================
	# OSM database routines

	# -----	Which_Ways
	#		returns array of ways

	sub Which_Ways {
		my $tilesql=sql_for_area($ymin,$xmin,$ymax,$xmax,'');
		$symin=$ymin*10000000; $symax=$ymax*10000000;
		$sxmin=$xmin*10000000; $sxmax=$xmax*10000000;
		my $sql=<<EOF;
SELECT DISTINCT current_way_nodes.id AS wayid 
  FROM current_way_nodes,current_nodes,current_ways 
 WHERE current_nodes.id=current_way_nodes.node_id 
   AND current_nodes.visible=1 
   AND current_ways.id=current_way_nodes.id 
   AND current_ways.visible=1 
   AND ($tilesql)
   AND (latitude  BETWEEN $symin AND $symax)
   AND (longitude BETWEEN $sxmin AND $sxmax)
 ORDER BY wayid
EOF
		my $query=$dbh->prepare($sql);
		my @ways=();
		$query->execute();
		while ($wayid=$query->fetchrow_array()) { push @ways,$wayid; }
		$query->finish();
		return @ways;
	}
	
	# -----	Get_Way(id)
	#		returns path array, attributes hash
	
	sub Get_Way {
		my $wayid=$_[0];
		my ($lat1,$long1,$id1,$lat2,$long2,$id2,$k,$v);
		my $sql=<<EOF;
SELECT latitude*0.0000001,longitude*0.0000001,current_nodes.id 
  FROM current_way_nodes,current_nodes 
 WHERE current_way_nodes.id=? 
   AND current_way_nodes.node_id=current_nodes.id 
   AND current_nodes.visible=1 
 ORDER BY sequence_id
EOF
		my $path=[];
		my $query=$dbh->prepare($sql);
		$query->execute($wayid);
		
		while (($lat,$long,$id)=$query->fetchrow_array()) {
			if ($proj eq 'mercator') { $xs=long2coord($long); $ys=lat2coord($lat); }
			elsif ($proj eq 'osgb')  { ($xs,$ys)=ll2osgb($long,$lat); }
			push @{$path},[$xs,$ys,$id];
		}
		$query->finish();
		
		$query=$dbh->prepare("SELECT k,v FROM current_way_tags WHERE id=?");
		$query->execute($wayid);
		my $attributes={};
		while (($k,$v)=$query->fetchrow_array()) { ${$attributes}{$k}=$v; }
		$query->finish();

		return ($path,$attributes);
	}

	# -----	Lat/long <-> coord conversion
	
	sub lat2coord 	{ return  (lat2y($_[0])-$basey)*$masterscale; }
	sub long2coord	{ return      ($_[0]-$baselong)*$masterscale; }
	sub lat2y	    { return 180/pi * log(Math::Trig::tan(pi/4+$_[0]*(pi/180)/2)); }

	sub ll2osgb		{ ($e,$n)=ll_to_grid($_[1],$_[0]);
					  $n=($n-$basey)*$masterscale;
					  $e=($e-$basex)*$masterscale;
					  return ($e,$n); }

	# ================================================================
	# Illustrator routines

	# -----	Write Illustrator header and footer

	sub Illustrator_Header {
		$start=1;
		print OUTFILE <<EOF;
%!PS-Adobe-3.0 
%%Creator: Adobe Illustrator(r) 6.0
%%For: (geowiki) (geowiki.com)
%%Title: (geowiki)
%%CreationDate: (29/9/02) (12:49 pm)
%%BoundingBox: -3999 -3893 4595 4685
%%HiResBoundingBox: -3998.05 -3892.05 4594.05 4684.05
%%DocumentProcessColors: Cyan Magenta Yellow Black
%%DocumentNeededResources: procset Adobe_level2_AI5 1.0 0
%%+ procset Adobe_Illustrator_AI6_vars Adobe_Illustrator_AI6
%%+ procset Adobe_Illustrator_AI5 1.0 0
%AI5_FileFormat 2.0
%AI3_ColorUsage: Color
%%AI6_ColorSeparationSet: 1 1 (AI6 Default Color Separation Set)
%%+ Options: 1 16 0 1 0 1 1 1 0 1 1 1 1 18 0 0 0 0 0 0 0 0 -1 -1
%%+ PPD: 1 21 0 0 60 45 2 2 1 0 0 1 0 0 0 0 0 0 0 0 0 0 ()
%AI3_TemplateBox: 306 396 306 396
%AI3_TileBox: 30 31 582 761
%AI3_DocumentPreview: None
%AI5_ArtSize: 612 792
%AI5_RulerUnits: 2
%AI5_ArtFlags: 1 0 0 1 0 0 1 1 0
%AI5_TargetResolution: 800
%AI5_NumLayers: 3
%AI5_OpenToView: -6702 3180 -16 826 581 58 0 1 2 40
%AI5_OpenViewLayers: 777
%%EndComments
%%BeginProlog
%%IncludeResource: procset Adobe_level2_AI5 1.0 0
%%IncludeResource: procset Adobe_Illustrator_AI6_vars Adobe_Illustrator_AI6
%%IncludeResource: procset Adobe_Illustrator_AI5 1.0 0
%%EndProlog
%%BeginSetup
Adobe_level2_AI5 /initialize get exec
Adobe_ColorImage_AI6 /initialize get exec
Adobe_Illustrator_AI5 /initialize get exec
%%EndSetup
%AI5_BeginLayer
1 1 1 1 0 0 0 79 128 255 Lb
(Layer 1) Ln
0 A
0 R
0 G
800 Ar
1 J 0 j 1 w 4 M []0 d
%AI3_Note:
0 D
0 XR
EOF
	}

	sub Illustrator_New_Layer {
		my $layername=$_[0];
		my $colour=$_[1];
		print OUTFILE <<EOF;
LB
%AI5_EndLayer--
%AI5_BeginLayer
1 1 1 1 0 0 1 255 79 79 Lb
($layername) Ln
0 A
0 R
$colour K
800 Ar
1 J 0 j 1 w 4 M []0 d
%AI3_Note:
0 D
0 XR
EOF
	}

	sub Illustrator_Footer {
		print OUTFILE <<EOF;
LB
%AI5_EndLayer--
%%PageTrailer
gsave annotatepage grestore showpage
%%Trailer
Adobe_Illustrator_AI5 /terminate get exec
Adobe_ColorImage_AI6 /terminate get exec
Adobe_level2_AI5 /terminate get exec
%%EOF
EOF
	}

	sub New_Path {
		$point='m';
		if ($start != 1) { print OUTFILE "S\n"; }
		$start=1;
		if ($_[0]) {
			$keystr="";
			foreach $k (keys %{$_[0]}) {
				unless ($k eq 'created_by' or $k=~/^osmarender/) { $keystr.="$k=".${$_[0]}{$k}."; "; }
			}
			$keystr=~s/; $//;
			$keystr=substr($keystr,0,240);
			print OUTFILE "\%AI3_Note:$keystr\n";
		}
	}


	sub Output_Point {
		print OUTFILE "$_[0] $_[1] $point\n";
		$point='l';
		$start=0;
	}
	

	
	# ================================================================
	# OSM quadtile routines
	# based on original Ruby code by Tom Hughes

	sub tile_for_point {
		my $lat=$_[0]; my $lon=$_[1];
		return tile_for_xy(round(($lon+180)*65535/360),round(($lat+90)*65535/180));
	}
	
	sub round {
		return int($_[0] + .5 * ($_[0] <=> 0));
	}
	
	sub tiles_for_area {
		my $minlat=$_[0]; my $minlon=$_[1];
		my $maxlat=$_[2]; my $maxlon=$_[3];
	
		$minx=round(($minlon + 180) * 65535 / 360);
		$maxx=round(($maxlon + 180) * 65535 / 360);
		$miny=round(($minlat + 90 ) * 65535 / 180);
		$maxy=round(($maxlat + 90 ) * 65535 / 180);
		@tiles=();
	
		for ($x=$minx; $x<=$maxx; $x++) {
			for ($y=$miny; $y<=$maxy; $y++) {
				push(@tiles,tile_for_xy($x,$y));
			}
		}
		return @tiles;
	}
	
	sub tile_for_xy {
		my $x=$_[0];
		my $y=$_[1];
		my $t=0;
		my $i;
		
		for ($i=0; $i<16; $i++) {
			$t=$t<<1;
			unless (($x & 0x8000)==0) { $t=$t | 1; }
			$x<<=1;
	
			$t=$t<< 1;
			unless (($y & 0x8000)==0) { $t=$t | 1; }
			$y<<=1;
		}
		return $t;
	}
	
	sub sql_for_area {
		my $minlat=$_[0]; my $minlon=$_[1];
		my $maxlat=$_[2]; my $maxlon=$_[3];
		my $prefix=$_[4];
		my @tiles=tiles_for_area($minlat,$minlon,$maxlat,$maxlon);
	
		my @singles=();
		my $sql='';
		my $tile;
		my $last=-2;
		my @run=();
		my $rl;
		
		foreach $tile (sort @tiles) {
			if ($tile==$last+1) {
				# part of a run, so keep going
				push (@run,$tile); 
			} else {
				# end of a run
				$rl=@run;
				if ($rl<3) { push (@singles,@run); }
					  else { $sql.="${prefix}tile BETWEEN ".$run[0].' AND '.$run[$rl-1]." OR "; }
				@run=();
				push (@run,$tile); 
			}
			$last=$tile;
		}
		$rl=@run;
		if ($rl<3) { push (@singles,@run); }
			  else { $sql.="${prefix}tile BETWEEN ".$run[0].' AND '.$run[$rl-1]." OR "; }
		if ($#singles>-1) { $sql.="${prefix}tile IN (".join(',',@singles).') '; }
		$sql=~s/ OR $//;
		return $sql;
	}

__END__

=head1 NAME

B<osm2ai.pl>

=head1 DESCRIPTION

osm2ai takes data from an OpenStreetMap-like MySQL database, 
and converts it to a file readable by Adobe Illustrator.

The data is wholly unstyled - the idea is that you make the 
cartographic decisions yourself. Data is grouped into layers 
to help you.

=head1 SYNOPSIS

osm2ai.pl --xmin -2.3 --xmax -2.2 --ymin 52.15 --ymax 52.25
          --projection osgb --output mymap.ai

=head1 OPTIONS

=over 2

=item B<--xmin> longitude
B<--xmax> longitude
B<--ymin> latitude
B<--ymax> latitude

The bounding box of the area you want to extract.

=item B<--projection> name

The projection for your map. Should be either B<osgb> 
(Ordnance Survey National Grid) or B<mercator> (spherical
Mercator).

=item B<--db> database_name
B<--user> database_user
B<--password> database_password

Connection details for the MySQL database which contains the
data. If you don't supply this, the DBNAME, DBUSER and DBPASS 
environment variables will be used. If they're not set, it'll 
default to openstreetmap, openstreetmap and openstreetmap.

=item B<--filters> filename

Specifies a file containing a list of 'filters'. These are 
used to put appropriately tagged ways in the right layers.

=item B<--output> filename

Specifies the output filename. Defaults to output.ai.

=item B<--man>

Output the full documentation.

=head1 FILTERS

Rather than just bunching everything into one layer, this
script can filter by tag. So you could put primary roads in 
one layer, secondary in another, and ignore canals 
completely.

Create a plain text file, and add lines like this:

B<motorway: highway=motorway>

Means "put ways tagged with highway=motorway in a 'motorway' 
layer".

B<railway: railway=*>

Means "put ways with any railway tag whatsoever in a 'railway' 
layer.

B<other: =>

Means "put anything else in an 'other' layer".

The tests are carried out in the order you give them. A way 
will only ever be put into one layer, even if it fulfils 
two conditions.

=head1 SETTING UP A DATABASE

Of course, before you run this, you'll need a database 
populated with OpenStreetMap data. This will typically 
involve downloading B<planet.osm> and then uploading it 
using a program such as B<planetosm-to-db.pl>.

For details, see http://wiki.openstreetmap.org/index.php/Planet.osm

=head1 OUTPUT

The resulting file is Illustrator v6 format (sometimes known as 
'legacy'), which can be opened in any version of Illustrator 
from then on.

For each way, the tags are saved in the 'Attributes' field. You 
can see this by clicking the way in Illustrator, then showing the 
Attributes window (Window->Attributes).

The ways aren't cropped to the bounding box.

=head1 PREREQUISITES

This script needs four modules which you almost certainly have 
(DBI, Math::Trig, Pod::Usage, Getopt::Long) and one which you 
might not (Geo::Coordinates::OSGB).

=head1 LIMITATIONS

It doesn't do POIs or relations, only tagged ways.

The quadtile stuff really ought to be in a library.

The whole caboodle should be on an OSM Export tab.

There should be a grid, or constant scale, or something, so you can mix and
match different maps.

=head1 COPYRIGHT

Written by Richard Fairhurst, 2007-2008.

Quadtile code adapted from Tom Hughes' Ruby OSM server code - 
thanks Tom!

This program really is free software. It's distributed under 
the terms of the WTFPL. You may do what the fuck you want to
with it. See http://sam.zoy.org/wtfpl/COPYING for details.

If you use it to extract data from OpenStreetMap which isn't 
yours, the output must of course only be published under 
the terms of whatever licence applies to the data.

=cut
