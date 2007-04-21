#!/usr/bin/perl 

open ASC, "$ARGV[0]";

for($i=0;$i<6;$i++){
    $head[$i]=<ASC>;
    chomp $head[$i];
    $head[$i]=~s/[a-zA-Z]//g;
}

$ncols = $head[0];
$nrows = $head[1];
$xllcorner = $head[2];
$yllcorner = $head[3];
$cellsize = $head[4];
$NULL = $head[5];
$l=$ARGV[1];

system("tail -n +7 $ARGV[0] > tmp");
print STDERR "Invoking gnuplot to generate contours at $l intervals\n";
open GNUPLOT, "|gnuplot";
print GNUPLOT "unset surface
set contour
set cntrparam level incremental 0, $l, 1000
set table 'tmp.cnt'
splot 'tmp' matrix w l
unset table
";

close GNUPLOT;
open CNT, "tmp.cnt";

$nid0 = 0;
$lastnode = $lastway = $lastseg = 10000; 
$prefix = "-";

print "<?xml version='1.0' encoding='UTF-8'?>
<osm version='0.3' generator='mkcntr'>
";

foreach (<CNT>){
    next if (/^\#/);
    if (/^[^0-9]*$/) {
	if($nid0 && $nid0!=$nidF){
	    #seg($nid0, $nidF);
	    way($z, @segs);
	    undef @segs;
	}
	$nid0 = 0;
	$nidF = 0;
	next;
    }
    chomp;
    ($i, $j, $z) = split;
#    next if(!defined($d));
    $nid1 = node($i, $j);
    if($nid0>0) {
	push @segs, seg($nid0, $nid1);
    } else {
	$nidF = $nid1;
    }
    $nid0=$nid1;
}

print "</osm>\n";

sub node {
    $lon = $xllcorner+$cellsize*$_[0];
    $lat = $yllcorner+$cellsize*($nrows-$_[1]);
    $id = $lastnode++;
    print "<node id='$prefix$id' lat='$lat' lon='$lon' />\n";
    return $id;
}

sub seg {
    $id = $lastseg++;
    print "<segment id='$prefix$id' from='$prefix$_[0]' to='$prefix$_[1]' />\n";
    return $id;
}

sub way {
    $id = $lastway++;
    $z = shift @_;
    print "<way id='$prefix$id'>\n";
    while (@_){
	$s = shift(@_);
	print "<seg id='$prefix$s' />\n";
    }
    print "<tag k='contour' v='$z' />\n";
    print "<tag k='created_by' v='mkcntr' />\n";
    print "</way>\n";
    return $id;
    
}
