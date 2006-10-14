#!/usr/bin/perl
# A script for rendering OSM data through osmarender, spitting out
#  tiles at various scales for it
#

use strict;

# Read in parameters
my $initial_dpi;
my @osmarender_scales;
my $filename;

while(my $arg = shift) {
	if($arg eq "-initial-dpi" || $arg eq "--initial-dpi" || $arg eq "-dpi") {
		$initial_dpi = shift;
	}
	elsif($arg eq "-osmarender-scales" || $arg eq "--osmarender-scales" || $arg eq "-scales") {
		@osmarender_scales = split(/,/, shift);
	} else {
		$filename = $arg;
	}
}

unless($initial_dpi && @osmarender_scales && $filename) {
	&help;
	exit;
}


sub help {
	print "Use:\n";
	print "   osmarender-tiles.pl -initial-dpi <dpi> -osmarender-scales <scale,scale,scale> <filename.osm>\n";
	print "\n";
	print "   initial-dpi        DPI setting of most zoomed out image\n";
	print "                       Select so as to give a ~500x500 image\n";
	print "   osmarender-scales  List of osmrender scales as we zoom in\n";
	print "                       eg 0.3,0.3,0.5,0.8,1\n";
	print "   filename.osm       The .osm file to render\n";
	exit;
}


# Do we have a copy of osmarender to hand?
my $osmarender_dir;
if(-d "osmarender") {
	$osmarender_dir = "osmarender";
} elsif(-f "osm-map-features.xml") {
	$osmarender_dir = "."; 
} else {
	# SVN checkout
	print "Fetching osmarender\n";
	`svn co http://svn.openstreetmap.org/utils/osmarender`;
	$osmarender_dir = "osmarender";
}


# Where are we putting things?
my ($dir) = ($filename =~ /^.*\/(.*?)\.osm$/);
unless($dir) { $dir = "output"; }
unless(-d $dir) { mkdir $dir; }
print "Outputting tiles to $dir\n";


# What are we going to use to make svgs?
my $xslt;
if(-f "/usr/share/java/xalan2.jar") {
	$xslt = "java -jar /usr/share/java/xalan2.jar";
} elsif(-f "/usr/share/java/xalan-j2.jar") {
	$xslt = "java -jar /usr/share/java/xalan-j2.jar";
} elsif(-f "/usr/bin/xsltproc") {
	$xslt = "/usr/bin/xsltproc";
} else {
	die("No supported xslt program found\n");
}


# Generate the svgs for each scale they wanted
my %svgs;
foreach my $scale (@osmarender_scales) {
	if($svgs{$scale}) { next; }
	my $ofn = "/tmp/output-$scale.svg";
	print "Generating SVG at scale $scale\n";

	# Copy stuff to tmp
	`cp $filename /tmp/data.osm`;
	`cp $osmarender_dir/*.xml /tmp/`;
	`cp $osmarender_dir/*.xsl /tmp/`;
	`cp $osmarender_dir/*.png /tmp/`;

	# Tweak osm-map-features
	tweakMapFeatures($scale);

	# Turn into svg
	`cd /tmp/ && $xslt -in osm-map-features.xml -out $ofn`;

	# Save the filename
	$svgs{$scale} = $ofn;
}


# For each scale, rasterize the image at the required DPI
my $dpi_factor = 0.5;
my @master_pngs;
foreach my $scale (@osmarender_scales) {
	# Next DPI
	$dpi_factor = $dpi_factor * 2;
	my $output_dpi = int($initial_dpi * $dpi_factor);

	# Rasterize the .svg
	my $png = "$dir/$output_dpi.png";
	print "Generating PNG for scale $scale at $output_dpi dpi\n";
	`unset DISPLAY && inkscape -D -d $output_dpi -e $png $svgs{$scale}`;
	print "Generated $png\n\n";
	push @master_pngs, $png;
}


# Output some javascript to help the display program
open(JS, "> $dir/information.js");
print JS "var scales = ".scalar(@osmarender_scales).";\n";
print JS "var tiles_in_dir = new Array();\n";
print JS "var tile_widths = new Array();\n";
print JS "var tile_heights = new Array();\n";


# Now, generate tiles from the master PNGs
#  (To zoom out, decrease the scale by 1, and halve the tile x+y)
#  (To zoom in, increase the scale by 1, and double the tile x+y)
# Start with 2x2 at the highest zoom
my $oscale = 0;
foreach my $scale (@osmarender_scales) {
	$oscale++;
	my $tscale = 2 ** ($oscale);

	my $odir = "$dir/scale-$oscale";
	unless(-d $odir) { mkdir $odir; }
	my $png = $master_pngs[($oscale-1)];

	# How many does the master png need to go into?
	my $tiles = $tscale * $tscale;

	# And how big is the image?
	my $identify = `identify $png`;
	my ($width,$height) = ($identify =~ /PNG (\d+)x(\d+) /);
	my $tilewidth = int($width / $tscale);
	my $tileheight = int($height / $tscale);
	my $cropwidth = $tilewidth * $tscale;
	my $cropheight = $tileheight * $tscale;
	unless($width && $height) {
		die("Couldn't make sense of identify output '$identify',\nso don't know how big things are\n");
	}

	print "\n";
	print "Generating tile images in $odir for scale $scale from $png\n";

	# Crop the master image so the tiles fit exactly into it
	unless($cropwidth == $width && $cropheight == $height) {
		print "Cropping image from ${width}x${height} to ${cropwidth}x${cropheight}\n";
		print `convert -crop ${cropwidth}x${cropheight}+0+0 $png /tmp/crop.png`;
		print `mv /tmp/crop.png $png`;
	}

	# Save the sizes for the javascript
	print JS "\n";
	print JS "tiles_in_dir[$oscale] = $tscale;\n";
	print JS "tile_widths[$oscale] = $tilewidth;\n";
	print JS "tile_heights[$oscale] = $tileheight;\n";

	# Now, have it split into handy tiles
	print "Splitting into $tiles tiles of ${tilewidth}x${tileheight}:\n";
	print `convert -crop ${tilewidth}x${tileheight}x0x0 $png $odir/tile.png`;

	# Remove the large image, we no longer need it
	unlink $png;

	# Finally, shuffle the tiles so they have the right names
	# We want them to be <updown>x<leftright>.png
	print "Renaming tiles\n";
	if($tscale == 1) {
		`mv $odir/tile.png $odir/tile-1x1.png`;
	} else {
		for(my $i=0; $i<$tscale; $i++) {
			for(my $j=0; $j<$tscale; $j++) {
				my $tile = $odir."/tile-".(($i*$tscale)+$j).".png";
				my $ntile = $odir."/tile-".($i+1)."x".($j+1).".png";
				`mv $tile $ntile`;
			}
		}
	}
	print "Finished generating $tiles tiles\n";
}


# Finish the javascript
close JS;

# Copy in the display html
`cp display.html $dir/index.html`;

print "\n\nTile generation complete\n";



##########################################################################


# To tweak map features to have the right settings
sub tweakMapFeatures {
	my $scale = shift;

	open(OSM, "</tmp/osm-map-features.xml");
	my $omf;
	while(<OSM>) { $omf .= $_; }
	close OSM;

	$omf =~ s/^\s*scale=".*?"/scale="$scale"/m;

	$omf =~ s/^\s*showScale=".*?"/showScale="no"/m;
	$omf =~ s/^\s*showGrid=".*?"/showGrid="no"/m;
	$omf =~ s/^\s*showBorder=".*?"/showBorder="no"/m;
	$omf =~ s/^\s*showAttribution=".*?"/showAttribution="no"/m;
	$omf =~ s/^\s*showLicense=".*?"/showLicense="no"/m;
	$omf =~ s/^\s*showZoomControls=".*?"/showZoomControls="no"/m;
	$omf =~ s/^\s*javaScript=".*?"/javaScript="no"/m;

	open(OSM, ">/tmp/osm-map-features.xml");
	print OSM $omf;
	close OSM;
}
