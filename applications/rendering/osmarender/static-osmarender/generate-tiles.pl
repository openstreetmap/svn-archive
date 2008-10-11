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
	}
	elsif($arg = "-h") {
		print `cat README.txt`;
		exit;
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
unless($dir) { 
	if(!($filename =~ /\//) && $filename =~ /\.osm$/) {
		($dir) = ($filename =~ /^(.*?)\.osm$/);
	} else {
		$dir = "output"; 
	}
}
unless(-d $dir) { mkdir $dir; }
print "Outputting tiles to $dir\n";

# What are we going to use to make svgs?
my $xslt;
my $xslt_out;
if(-f "/usr/share/java/xalan2.jar") {
	$xslt = "java -jar /usr/share/java/xalan2.jar -in";
	$xslt_out = "-out";
} elsif(-f "/usr/share/java/xalan-j2.jar") {
	$xslt_out = "-out";
	$xslt = "java -jar /usr/share/java/xalan-j2.jar -in";
} elsif(-f "/usr/bin/xsltproc") {
	$xslt = "/usr/bin/xsltproc";
	$xslt_out = "-o";
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
	`cd /tmp/ && $xslt osm-map-features.xml $xslt_out $ofn`;

	# Save the filename
	$svgs{$scale} = $ofn;
}


# For each scale, rasterize the image at the required DPI
my $dpi_scale = 0.5;
my @master_pngs;
foreach my $scale (@osmarender_scales) {
	# Move to the next DPI
	$dpi_scale = $dpi_scale * 2;
	my $output_dpi = int($initial_dpi * $dpi_scale);

	# Do we need to tweak the DPI, to stop the tiles being too big?
	# (Caused by osmarender scaling effects)
	# (The 0.65 is an empirical scale factor, should give ~ same widths)
	$output_dpi = $output_dpi / (($scale / $osmarender_scales[0]) ** 0.65);
	
	# Round the output dpi
	$output_dpi = int($output_dpi);

	# Rasterize the .svg, at the required DPI
	my $png = "$dir/$output_dpi.png";
	print "Generating PNG for scale $scale at $output_dpi dpi\n";
	print `inkscape -D -z -d $output_dpi -e $png $svgs{$scale} 2>&1`;
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

	# Save the sizes for the javascript
	print JS "\n";
	print JS "tiles_in_dir[$oscale] = $tscale;\n";
	print JS "tile_widths[$oscale] = $tilewidth;\n";
	print JS "tile_heights[$oscale] = $tileheight;\n";

	# Now, have it split into handy tiles
	# Makes one call to vips per tile, which isn't ideal, but at least
	#  it doesn't die / use lots of memory / break like "convert -crop" does
	#	
	# We want the tiles to be to be <updown>x<leftright>.png
	print "Splitting into $tiles tiles of ${tilewidth}x${tileheight}:\n";
	if($tscale == 1) {
		`cp $png $odir/tile-1x1.png`;
	} else {
		for(my $i=0; $i<$tscale; $i++) {
			for(my $j=0; $j<$tscale; $j++) {
				my $tileid = ($i+1)."x".($j+1);
				my $otile = $odir."/tile-".$tileid.".png";

				my $toffset = $i * $tileheight;
				my $loffset = $j * $tilewidth;

				print "  Producing $tileid of ${tscale}x${tscale} - offset is ${loffset}x${toffset}\n";
				print `vips im_extract_area $png $otile $loffset $toffset $tilewidth $tileheight`;
			}
		}
	}

	# Remove the large image, we no longer need it
	unlink $png;

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

__END__

=head1 NAME

B<generate-tiles.pl>

=head1 DESCRIPTION

Please Fill in

=head1 SYNOPSIS

Please Fill in

=head1 OPTIONS

Please Fill in
=head1 COPYRIGHT

Please Fill in

=head1 AUTHOR

Please Fill in

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
