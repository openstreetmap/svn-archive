#!/usr/bin/perl
#
# This scripts reads a tiles@home stats file (from 
# http://dev.openstreetmap.org/~ojw/Stats/) on stdin an attempts to
# determine the status of level-7 tiles based on the level-12 tile
# information.
#
# Command line and output similar to comparestats.pl which does the
# same to level-12 tiles (based on osm data). See comments there.
#
# Author Frederik Ramm <frederik@remote.org> / public domain

use strict;

my $commandline = 0;
my $spurious = 0;
my $obsolete = 0;
my $missing = 0;
my $current = 0;
my $outdated = 0;

foreach my $arg (@ARGV) 
{
    $commandline = 1;
    if (lc($arg) =~ /^(spurious|obsolete|missing|current|outdated)$/)
    {
        eval "\$$arg = 1";
    }
    else
    {
        die "invalid command line argument '$arg'";
    }
}

my $lastmod ={};

while(<STDIN>)
{
    chomp;
    my ($x, $y, $z, $who, $size, $when) = split(/,/);
    if ($z == 7)
    {
        my $coord = "$x $y";
        $lastmod->{$coord}->{"is"} = $when;
        $lastmod->{$coord}->{"size"} = $size;
    }
    elsif ($z == 12)
    {
        # skip empty l-12 tiles otherwise we generate half the world
        next if ($size < 1000);
        my $coord = sprintf("%d %d", $x/32, $y/32);
        $lastmod->{$coord}->{"should"} = $when 
            if ($when > $lastmod->{$coord}->{"should"});
    }
}

foreach my $coord(keys(%{$lastmod}))
{
    my $tlm = $lastmod->{$coord};
    if (!defined($tlm->{"is"}))
    {
        if ($commandline)
        {
            print "$coord\n" if ($missing);
        }
        else
        {
            print "MISSING: level-7 tile '$coord' is not present although there are level-12 tiles in its area.\n";
        }
    }
    elsif (!defined($tlm->{"should"}))
    {
        if ($tlm->{"size"} >= 1000)
        {
            if ($commandline)
            {
                print "$coord\n" if ($spurious);
            }
            else
            {
                print "SPURIOUS: level-7 tile '$coord' is present with size ".
                    $tlm->{"size"}.
                    " (indicating non-empty content) but no level-12 ".
                    "tiles to support it\n";
            }
        }
        else
        {
            if ($commandline)
            {
                print "$coord\n" if ($obsolete);
            }
            else
            {
                print "OBSOLETE: level-7 tile '$coord' is present with size ".
                    $tlm->{"size"}.
                    " (indicating empty content) and no level-12 tiles ".
                    "in its area; remove it\n";
            }
        }
    }
    elsif ($tlm->{"is"} >= $tlm->{"should"})
    {
        if ($commandline)
        {
            print "$coord\n" if ($current);
        }
        else
        {
            print "CURRENT: level-7 tile '$coord' was created later than ".
                "latest level-12 tile in its area\n";
        }
    }
    else
    {
        if ($commandline)
        {
            print "$coord\n" if ($outdated);
        }
        else
        {
            print "OUTDATED: level-7 tile '$coord' is based on level-12 ".
                "tiles that have changed after it was created\n";
        }
    }
}
