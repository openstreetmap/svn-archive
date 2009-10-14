#!/usr/bin/perl -w

	use Geo::Proj4;
	use Math::Trig;
	use Imager;
	use Imager::CountColor;
	use POSIX qw(floor ceil);

	$google=Geo::Proj4->new(proj=>"merc", a=>6378137, b=>6378137,
						    lat_ts=>0.0, lon_0=>0.0, x_0=>0.0, y_0=>0,
						    k=>1.0, units=>"m", nadgrids=>'@null');
	$black=Imager::Color->new(0,0,0);
	$scale1=15;	$scale2=16; 	# Target zoom levels (min/max)
	$root="tiles/";

	# Combine images
	system "gdalwarp -s_srs EPSG:27700 -t_srs EPSG:900913 @ARGV combined.tiff";

	# Get bounds
	$info=`gdalinfo combined.tiff`;
	if ($info=~/Size is (\d+), (\d+)/i) { $width=$1; $height=$2; }
	if ($info=~/Upper Left\s*\(\s*([\d\-.]+),\s*([\d\-.]+)/i) { $xleft=$1; $ytop=$2; }
	if ($info=~/Lower Right\s*\(\s*([\d\-.]+),\s*([\d\-.]+)/i) { $xright=$1; $ybottom=$2; }
	if ($info=~/Pixel Size = \(-?([\d.]+)/i) { $pixelsize=$1; }
	
	($umaxlat,$uminlong)=$google->inverse($xleft ,$ytop   );
	($uminlat,$umaxlong)=$google->inverse($xright,$ybottom);
	print "Mercator: ($xleft, $ytop) - ($xright, $ybottom)\n";
	print "Lat/long: ($umaxlat, $uminlong) - ($uminlat, $umaxlong)\n";

	for ($scale=$scale1; $scale<=$scale2; $scale++) {
		print "\nScale $scale\n";

		# Get tiles
		$gx1=long2tile($uminlong)+1; $gx2=long2tile($umaxlong)-1;
		$gy1=lat2tile ($umaxlat )+1; $gy2=lat2tile ($uminlat )-1;
		$gwidth =$gx2-$gx1; $gwidthpx =$gwidth *256;
		$gheight=$gy2-$gy1; $gheightpx=$gheight*256;
	
		# Convert back to lat/long so we can crop
		$gminlong=tile2long($gx1); $gmaxlong=tile2long($gx2);
		$gmaxlat =tile2lat ($gy1); $gminlat =tile2lat ($gy2);
	
		print "\nGoogle crop range:\n";
		print "$gminlong, $gmaxlong\n";
		print "$gminlat, $gmaxlat\n";

		print "\nGoogle tile range:\n";
		print "X: $gx1 to $gx2 ($gwidth, $gwidthpx pixels)\n";
		print "Y: $gy1 to $gy2 ($gheight, $gheightpx pixels)\n";

		($gprojleft ,$gprojtop   )=$google->forward($gmaxlat,$gminlong);
		($gprojright,$gprojbottom)=$google->forward($gminlat,$gmaxlong);
		print "\nProjected Google values:\n";
		print "Mercator: ($gprojleft, $gprojtop) - ($gprojright, $gprojbottom)\n";

		$crop_left  =($gprojleft-$xleft)/$pixelsize;
		$crop_right =($xright-$gprojright)/$pixelsize;
		$crop_top   =($ytop-$gprojtop)/$pixelsize;
		$crop_bottom=($gprojbottom-$ybottom)/$pixelsize;
		print "crop: $crop_left, $crop_right, $crop_top, $crop_bottom\n";
	
		print "expected width: ".($width-$crop_left-$crop_right)."\n";
		print "expected height: ".($height-$crop_top-$crop_bottom)."\n";
	
		# ------------------------
		# Create tiles with Imager

		# Crop to relevant size
		# bbox of original is $uminlong->$umaxlong, $uminlat->$umaxlat
		# what we want is     $gminlong->$gmaxlong, $gminlat->$gmaxlat

		$pxsize=$width/($umaxlong-$uminlong);	# pixels per degree
		print "\nResize:\n";
		print "pxsize: $pxsize\n";

		$crop_left  =($gminlong-$uminlong)*$pxsize;
		$crop_right =($umaxlong-$gmaxlong)*$pxsize;
		$crop_top   =(lat2y($umaxlat)-lat2y($gmaxlat))*$pxsize;
		$crop_bottom=(lat2y($gminlat)-lat2y($uminlat))*$pxsize;

		print "\nProcessing file:\n";
		$pr=Imager->new();
		$pr->open(file=>"combined.tiff") or die "Open error: ".$pr->errstr;

		print "crop: $crop_left, $crop_right, $crop_top, $crop_bottom\n";
		$pr=$pr->crop(left  =>$crop_left,
					  right =>$width-$crop_right,
					  top   =>$crop_top,
					  bottom=>$height-$crop_bottom) or die "Crop error: ".$pr->errstr;
		print "scale: $gwidthpx, $gheightpx\n";
		$pr=$pr->scale(xpixels=>$gwidthpx,
					   ypixels=>$gheightpx,
					   type=>'nonprop') or die "Scale error: ".$pr->errstr;
		print "write\n";
		for ($x=$gx1; $x<$gx2; $x++) {
			for ($y=$gy1; $y<$gy2; $y++) {
				unless (-d "${root}$scale") { mkdir "${root}$scale"; }
				unless (-d "${root}$scale/$x") { mkdir "${root}$scale/$x"; }
				$tile=$pr->crop(left=>($x-$gx1)*256, width=>256,
								top =>($y-$gy1)*256, height=>256) or die "Tile crop error: ".$pr->errstr;
				$bcount=Imager::CountColor::count_color($tile,$black);
				if ($bcount<5000) {
					$tile->write(file=>"${root}$scale/$x/$y.jpg", jpegquality=>90) or die "Tile write error: ".$tile->errstr;
				}
			}
		}
	}
	unlink "combined.tiff";

	print chr(7)."Finished\n";

	# ==========
	# Projection

	sub long2tile { return (floor(($_[0]+180)/360*2**$scale)); }
	sub lat2tile { return (floor((1-log(tan($_[0]*pi/180) + 1/cos($_[0]*pi/180))/pi)/2 *2**$scale)); }
	sub tile2long { return ($_[0]/2**$scale*360-180); }
	sub tile2lat { my $n=pi-2*pi*$_[0]/2**$scale; return (180/pi*atan(0.5*(exp($n)-exp(-$n)))); }
	sub lat2y { return 180/pi*log(tan(pi/4+$_[0]*(pi/180)/2)); }
