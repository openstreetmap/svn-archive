#!/usr/bin/env perl
use feature ':5.10';
use strict;
use warnings;

my ($arg, $max) = @ARGV;
if (-f $arg) {
    open my $fh, "<", $arg or die "Can't open file `$arg': $!";
    while (<$fh>)
    {
        chomp;
        say rgb_str($max ? ($_ / $max) : $_);
    }
} else {
    say rgb_str($max ? ($arg / $max) : $arg);
}

sub rgb_str
{
    "rgb(" . join(',', heatmap(warp_size_for_heatmap(shift))) . ")";
}

sub warp_size_for_heatmap
{
    my $size = shift;

    # FIXME: This results in a very lousy distribution, the map is all
    # blue
    my $n = log $size / log $max;
#    my $n = (log($size)/log(10)) / (log($max)/log(10));
    #my $n = (($size-$min+1) / $max)**0.2;
    #my $n = $size / (227879*32);

    return $n;
}

# From http://google.com/codesearch/p?hl=en#v85D9_xn8lk/viewcvs.py/gmod/graphbrowse/cgi/graphbrowse%3Frev%3D1.10&q=heat map rgb lang:perl
sub heatmap {
    my($v)=@_;

    die "Heatmap input out of range: $v" if $v < 0 || $v > 1;

    my @rgb;

    for my $offset (-0.25,0,0.25) {
        my $x = $v + $offset;
        my $y;
        if ($x <= .125) {
            $y = 0;
        } elsif ($x <= .375) {
            $y = ($x-.125)/.25;
        } elsif ($x <= .625) {
            $y = 1;
        } elsif ($x <= .875) {
            $y = (.875 - $x )/.25;
        } else {
            $y = 0;
        }
        push @rgb => $y * 255;
    }
    map { int } @rgb;
}
