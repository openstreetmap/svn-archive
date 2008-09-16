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
print GNUPLOT "set terminal table
unset surface
set contour
set cntrparam level incremental -500, $l, 9000
set output 'tmp.cnt'
splot 'tmp' matrix w l
";

close GNUPLOT;
open CNT, "tmp.cnt";

$nid0 = 0;
$lastnode = 1000050000;
$lastway = 1000050000;
$prefix = "";

print "<?xml version='1.0' encoding='UTF-8'?>
<osm version='0.5' generator='mkcntr'>
<bound box='-90,-180,90,180' origin='mkcntr'/>
";

foreach (<CNT>){
    next if (/^\#/);
    if (/^[^0-9]*$/) {
	if($nid0 && $nid0!=$nidF){
	    way($z, @nodes);
	}
        undef @nodes;
	$nid0 = 0;
	$nidF = 0;
	next;
    }
    chomp;
    ($i, $j, $z) = split;
    $nid1 = node($i, $j);
    if($nid0>0) {
        push @nodes, $nid1;
    } else {
        push @nodes, $nid1;
	$nidF = $nid1;
    }
    $nid0=$nid1;
}

print "</osm>\n";

sub node {
    $lon = $xllcorner+$cellsize*$_[0];
    $lat = $yllcorner+$cellsize*($nrows-$_[1]);
    $id = $lastnode++;
    print "<node id='$prefix$id' timestamp='0001-01-01T00:00:00' lat='$lat' lon='$lon' />\n";
    return $id;
}

sub way {
    $id = $lastway++;
    $z = shift @_;
    print "<way id='$prefix$id' timestamp='0001-01-01T00:00:00'>\n";
    while (@_){
	$s = shift(@_);
	print "<nd ref='$prefix$s' />\n";
    }
    print "<tag k='contour' v='elevation' />\n";

    if ($ARGV[2] && $ARGV[3]) {
        if ( $z % $ARGV[3] == 0 ) {
            print "<tag k='contour_ext' v='elevation_major'\n />";
        } else {
            if ( $z % $ARGV[2] == 0 ) {
                print "<tag k='contour_ext' v='elevation_medium'\n />";
            } else {
                print "<tag k='contour_ext' v='elevation_minor'\n />";
            }
        }
    }

    print "<tag k='ele' v='$z' />\n";
    print "<tag k='created_by' v='mkcntr' />\n";
    print "</way>\n";
    return $id;
    
}
