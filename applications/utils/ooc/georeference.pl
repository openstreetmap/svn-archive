#!/usr/bin/perl -w

	# Convert raw TIFFs to properly squared-up GeoTIFFs

	foreach $fn (@ARGV) {
		$fn=~s/\..+$//;
		print "Processing $fn\n";
		$/="\n";
#		$/="\r\n";

		$cmd='';
		$min_e=9999999; $max_e=0;
		$min_n=9999999; $max_n=0;
		open (INFILE, "$fn.csv") or die "Couldn't open $fn.csv: $!\n";
		while (<INFILE>) {
			$inline=$_; chomp $inline;
			($e,$a,$x,$n,$a,$y)=split(/,/,$inline);
			if ($e!~/\d+/) { next; }
			$cmd.="-gcp $x $y $e $n ";
			if ($e<$min_e) { $min_e=$e; }
			if ($n<$min_n) { $min_n=$n; }
			if ($e>$max_e) { $max_e=$e; }
			if ($n>$max_n) { $max_n=$n; }
		}
		close INFILE;

		if (-e "${fn}_r.tiff") { unlink "${fn}_r.tiff"; }
		system "gdal_translate $cmd -a_srs EPSG:27700 $fn.tif temp1.tiff";
		system "gdalwarp -s_srs EPSG:27700 -t_srs EPSG:27700 -te $min_e $min_n $max_e $max_n temp1.tiff ${fn}_r.tiff";
		unlink "temp1.tiff";
	}
