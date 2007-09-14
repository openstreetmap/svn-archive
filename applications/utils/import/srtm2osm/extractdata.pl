#!/usr/bin/perl 

use Pod::Usage;

if ( @ARGV < 5){
    printf STDERR "Need Filename and 4 Corners to operate\n";
    pod2usage(1);
    exit 1;
}

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

##################################################################
# Usage/manual

__END__

=head1 NAME

B<extractdata> Version 0.05

=head1 DESCRIPTION

Extracting columns firstcol - lastcol from rows  firstrow - lastrow
for srtm2osm

=head1 SYNOPSIS

B<Common usages:>

extractdata <filename> <y-ll-corner> <x-ll-corner> <y-tr-corner> <x-tr-corner>

=head1 COPYRIGHT

... please fill in

=head1 AUTHOR

... please fill in

=head1 SEE ALSO

http://www.openstreetmap.org/


=cut
