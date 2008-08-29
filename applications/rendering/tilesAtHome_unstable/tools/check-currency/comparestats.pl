#!/usr/bin/perl

# This program reads two files (names hardcoded):
# lastmodtile.out  - output of the lastmodtile.pl script
# stats.txt -        current tiles@home tile list from 
#                    http://dev.openstreetmap.org/~ojw/Stats/
#
# It compares the sizes and last-modified timestamps for level-12
# tiles, and creates exactly one line of output for each level-12
# tile that is mentioned in either of the input files.
#
# The output is rather self-explanatory. Every tile falls into one
# of five groups:
#
# "spurious": Tiles which do exist in tiles@home and seem non-empty
#             (this is guessed from the PNG file size) but for which 
#             there is no OSM data. Such tiles may be leftovers of
#             deletions (i.e. generated at a time when OSM data was
#             indeed available), but they may also represent cases 
#             where rendering something on a neigbouring tile touches
#             a tile.
# "obsolete": Tiles which exist in tiles@home, seem empty, and have 
#             no data in the OSM planet file; they could probably be
#             deleted without harm.
# "current":  Tiles which exist in tiles@home and are newer than the
#             last change in the OSM data.
# "outdated": Tiles which exist in tiles@home but the corresponding
#             OSM data has changed since the tile was created.
# "missing":  Tiles which do not exist in tiles@home, but OSM data is
#             present and they could meaningfully be rendered.
#
# The script accepts the names of these five groups (spurious, obsolete,
# current, outdated, missing) on the command line; if one or more are
# specified, it will _only_ output the tile names for those tiles that
# fall into the specified categories. If the command line is empty, 
# a complete list will be genereated naming the status of each tile.
#
# Note that there may be cases where a tile is reported "missing", but
# in reality the tile area contains only data that is not rendered by
# osmarender. In that case, re-generation of the tile would not lead
# to a tile being uploaded, and the tile would still be missing the
# next time round.
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

# suck last-modified dates for tiles into memory.
my %lastmod;

open(LMT, "lastmodtile.out") or die "cannot open lastmodtile.out";
while(<LMT>)
{
    (/(\d+ \d+) (\d+)/) or die;
    $lastmod{$1}=$2;
}
close(LMT);

# read the stats file
open(STAT, "stats.txt") or die "cannot open stats.txt";
while(<STAT>)
{
    chomp;
    my ($x, $y, $z, $who, $size, $lmod) = split(/,/);
    next if ($z != 12); # skip anything that's not zoom level 12
    my $tile="$x $y";
    if (!defined($lastmod{$tile}))
    {
        if ($size >= 1000)
        {
            if ($commandline)
            {
                print "$tile\n" if ($spurious);
            }
            else
            {
                print "SPURIOUS: level-12 tile '$tile' is present with size ".
                    "$size (indicating non-empty content) but no OSM data to ".
                    "support it\n";
            }
        }
        else
        {
            if ($commandline)
            {
                print "$tile\n" if ($obsolete);
            }
            else
            {
                print "OBSOLETE: level-12 tile '$tile' is present with size ".
                    "$size (indicating empty content); remove it\n";
            }
        }
    }
    elsif ($lastmod{$tile} <= $lmod)
    {
        if ($commandline)
        {
            print "$tile\n" if ($current);
        }
        else
        {
            print "CURRENT: level-12 tile '$tile' was created later than ".
                "latest data modification\n";
        }
        delete $lastmod{$tile};
    }
    else
    {
        if ($commandline)
        {
            print "$tile\n" if ($outdated);
        }
        else
        {
            print "OUTDATED: level-12 tile '$tile' data has changed after ".
                "tile was rendered\n";
        }
        delete $lastmod{$tile};
    }
}
close(STAT);

exit if ($commandline and !$missing);

foreach my $tile(keys %lastmod)
{
    if ($commandline)
    {
        print "$tile\n" if ($missing);
    }
    else
    {
        print "MISSING: level-12 tile '$tile' is not present although there is data.\n";
    }
}
