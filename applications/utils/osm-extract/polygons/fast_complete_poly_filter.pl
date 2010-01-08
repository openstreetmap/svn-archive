#!/usr/bin/perl

# script that achieves the same as
# 
# osmosis --rx infile.osm 
#  --bp file=file.poly completeWays=yes completeRelations=yes 
#  --wx outfile.osm
#
# just faster, by adding a "--bb" step just before the --bp that
# cuts out the area of the polygon plus a safety buffer. this provides
# a big speedup if the polygon cuts out a relatively small area of the
# input file, because it saves osmosis a lot of temporary storage.
#
# requires osmosis to be in the $PATH.

if (scalar(@ARGV) != 3)
{
    print "usage: $0 infile.osm polyfile.poly outfile.osm\n";
    exit;
}

my ($infile, $polyfile, $outfile) = @ARGV;

if (!-r $infile)
{
    print "cannot open $infile for reading\n";
    exit;
}

if (-s $infile > 20000000000)
{
    $idt = "idTrackerType=BitSet";
}

if (!-r $polyfile)
{
    print "cannot open $polyfile for reading\n";
    exit;
}

if (!open(X, ">$outfile"))
{
    print "cannot open $outfile for writing\n";
    exit;
}
close(X);

open (POLY, $polyfile);

my $bottom = 90;
my $top = -90;
my $left = 180;
my $right = -180;

while($line = <POLY>)
{
    my ($dummy, $x, $y) = split(/\s+/, $line);
    if ($x =~ /^[0-9-+.eE]+$/ && $y =~ /^[0-9-+.eE]+$/)
    {
        if ($x<$left) { $left=$x } elsif ($x>$right) { $right=$x; }
        if ($y<$bottom) { $bottom=$y } elsif ($y>$top) { $top=$y; }
    }
}
close(POLY);

$left--; $right++;
$bottom--; $top++;

$command =  "osmosis --rx $infile --bb left=$left right=$right bottom=$bottom top=$top $idt --bp file=$polyfile completeWays=true completeRelations=true --wx $outfile";
print "$command\n";
exec $command;
