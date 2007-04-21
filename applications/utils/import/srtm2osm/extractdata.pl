#!/usr/bin/perl 

open ASC, "$ARGV[0]";
#open OUT, ">$ARGV[0]";

for($i=0;$i<6;$i++){
    $head[$i]=<ASC>;
    chomp $head[$i];
    $head[$i]=~s/[a-zA-Z_]//g;
}

$ncols = $head[0];
$nrows = $head[1];
$xllcorner = $head[2];
$yllcorner = $head[3];
$cellsize = $head[4];
$NULL = $head[5];

#$newxllcorner = -2.4146;
#$newyllcorner = 51.3526;
#$newxtrcorner = -2.3171;
#$newytrcorner = 51.4089;

$newxllcorner = $ARGV[2];
$newyllcorner = $ARGV[1];
$newxtrcorner = $ARGV[4];
$newytrcorner = $ARGV[3];

$firstcol = int(($newxllcorner-$xllcorner)/$cellsize);
$lastcol = int(($newxtrcorner-$xllcorner)/$cellsize);
$firstrow = $nrows - int(($newytrcorner-$yllcorner)/$cellsize);
$lastrow = $nrows - int(($newyllcorner-$yllcorner)/$cellsize);
$newncols = $lastcol - $firstcol;
$newnrows = $lastrow - $firstrow;
$newxllcorner = $xllcorner + $firstcol * $cellsize;
$newyllcorner = $yllcorner + ($nrows-$lastrow) * $cellsize;

print STDERR "Extracting columns $firstcol-$lastcol from rows $firstrow-$lastrow\n";

print 
"ncols\t$newncols
nrows\t$newnrows
xllcorner\t$newxllcorner
yllcorner\t$newyllcorner
cellsise$cellsize
NODATA_value$NULL
";

for($i=0;$i<=$lastrow;$i++){
    $line=<ASC>;
    if($i>=$firstrow){
	chomp $line;
	@d = split(" ", $line);
	for($j=$firstcol;$j<$lastcol;$j++){
	    print "$d[$j] ";
	}
	print "\n";
    }
}

sub coord {
    $lon = $xllcorner+$cellsize*$_[1];
    $lat = $yllcorner+$cellsize*($nrows-$_[0]);
    return "$lat, $lon\n";
}
