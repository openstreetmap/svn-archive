#!/usr/bin/perl

# Program that finds and fixes unordered ways.
#
# Takes an osm file on standard input; writes a modified osm file to
# standard output.
#
# The output file contains everything that was in the input file; some
# segments and ways may be augmented by an "action=modfiy" attribute
# that will cause JOSM to upload the change if the file is loaded in 
# JOSM.
#
# This script puts ways in one of four categories:
#
# 1. Ordered correctly, no action required
# 2. Not ordered, but can be fixed by simply re-arranging segments
# 3. Not ordered, but can be fixed by reversing some segments and 
#    then re-arranging
# 4. Not ordered, and cannot be fixed (often this will be a way
#    with branches, but see Caveats).
#
# Types 2 and 3 will be fixed and augmented by "action=modify".
# Types 2 to 4 will have an XML comment in the output file telling
# you which type it is.
#
# CAVEATS:
#
# * Will shamelessly reverse segments in anything that doesn't have
#   "oneway=true", even rivers. And even "oneway=true" is only checked
#   for the current way context, i.e. if two ways share a segment, 
#   and one of them has oneway=true, then the segment may still be
#   reversed while dealing with the other way.
#
# * May not fix everything that can be fixed by a human being, because
#   a segment already reversed is never touched again by the script,
#   even if this may make a later way sharing the same segment unfixable.
#   Some times in such situations it might be possible to go back to
#   the first way using the segment and reverse all segments in that way,
#   allowing to fix the current one.
#
# * Uses regular expressions to parse the planet file, not a proper XML
#   parser.
#
# * Memory consumption and speed: takes about 45 seconds and 400 MB of
#   RAM on a decent machine to process the whole of Germany (which amounts
#   to roughly 10% of the planet file). However, if you intend to upload
#   the results, you can only handle file sizes that can comfortably be
#   loaded into JOSM. 
#
# * May contain traces of bugs ;-)
#
# Author Frederik Ramm <frederik@remote.org> / public domain

use strict;

my $tempfile = "/tmp/$$.tmp";
my $segments = {};
my $current_segment;

open(TEMP, ">$tempfile") or die "Cannot open $tempfile for writing";
while(<>) 
{
    if (/^  <segment id=["'](\d+)["'].* from=["'](\d+)["'] to=["'](\d+)["']/)
    {
        $segments->{$1} = [$2, $3];
        print TEMP;
    }
    elsif(/^  <way/)
    {
        my $raw = $_;
        my $segs = [];
        my $current = 0;
        my $segdelim;
        while(<>) 
        {
            last if (/^  <\/way>/);
            if (/^    <seg id=(["'])(\d+)/) 
            {
                my $segid = $2;
                $segdelim = $1;
                if (($current == 0) or ($current == $segments->{$segid}->[0]))
                {
                    $current = $segments->{$segid}->[1];
                }
                else
                {
                    $current = -1;
                }
                push(@{$segs}, $segid);
            }
            else
            {
                $raw .= $_;
            }
        }
        if ($current == -1)
        {
            $current = -2;
            # re-order segments, using the same algorithm that JOSM uses
            # (cf. ReorderAction.sortSegments() in JOSM)
            my $unordered = {};
            foreach my $s (@{$segs}) 
            {
                $unordered->{$s} = 1;
            }
            $segs = [];

            while (scalar(keys(%{$unordered})))
            {
                my ($first, $rest) = keys(%{$unordered});
                delete $unordered->{$first};
                my @pivot = ( $first );
                my $found = 1;
                while ($found)
                {
                    $found = 0;
                    foreach my $seg(keys(%{$unordered}))
                    {
                        if ($segments->{$seg}->[0] == $segments->{$pivot[$#pivot]}->[1])
                        {
                            push @pivot, $seg;
                            delete $unordered->{$seg};
                            $found = 1;
                            next; 
                        }
                        elsif ($segments->{$seg}->[1] == $segments->{$pivot[0]}->[0])
                        {
                            unshift(@pivot, $seg);
                            delete $unordered->{$seg};
                            $found = 1;
                            next; 
                        }
                    }
                }
                $current = -1 if (scalar(@{$segs}));
                push(@{$segs}, @pivot);
            }
        }
        if (($current == -1) && ($raw !~ /k=["']oneway["'] v=["'](yes|true)/i))
        {
            $current = -3;
            # re-order segments, using the same algorithm that JOSM uses
            # (cf. ReorderAction.sortSegments() in JOSM)
            # but this time allow the reversal of segments that haven't been used
            # yet
            my $unordered = {};
            my @reversed;
            my $origsegs = $segs;
            foreach my $s (@{$segs}) 
            {
                $unordered->{$s} = 1;
            }
            $segs = [];

            while (scalar(keys(%{$unordered})))
            {
                my ($first, $rest) = keys(%{$unordered});
                delete $unordered->{$first};
                my @pivot = ( $first );
                my $found = 1;
                while ($found)
                {
                    $found = 0;
                    foreach my $seg(keys(%{$unordered}))
                    {
                        if ($segments->{$seg}->[0] == $segments->{$pivot[$#pivot]}->[1])
                        {
                            push @pivot, $seg;
                            delete $unordered->{$seg};
                            $found = 1;
                            next; 
                        }
                        elsif ($segments->{$seg}->[1] == $segments->{$pivot[0]}->[0])
                        {
                            unshift(@pivot, $seg);
                            delete $unordered->{$seg};
                            $found = 1;
                            next; 
                        } 
                        elsif (($segments->{$seg}->[1] == $segments->{$pivot[$#pivot]}->[1]) && !$segments->{$seg}->[2])
                        {
                            my $t = $segments->{$seg}->[0];
                            $segments->{$seg}->[0] = $segments->{$seg}->[1];
                            $segments->{$seg}->[1] = $t;
                            $segments->{$seg}->[3] = 1;
                            push @pivot, $seg;
                            delete $unordered->{$seg};
                            $found = 1;
                            next; 
                        }
                        elsif (($segments->{$seg}->[0] == $segments->{$pivot[0]}->[0]) && !$segments->{$seg}->[2])
                        {
                            my $t = $segments->{$seg}->[0];
                            $segments->{$seg}->[0] = $segments->{$seg}->[1];
                            $segments->{$seg}->[1] = $t;
                            $segments->{$seg}->[3] = 1;
                            unshift(@pivot, $seg);
                            delete $unordered->{$seg};
                            $found = 1;
                            next; 
                        }
                    }
                }
                if (scalar(@{$segs}))
                {
                    # it didn't work, we still have a "complicated" way
                    $current = -1;
                    last;
                }
                push(@{$segs}, @pivot);
            }
            if ($current == -1)
            {
                # undo segment reversal
                foreach my $seg(@reversed)
                {
                    my $t = $segments->{$seg}->[0];
                    $segments->{$seg}->[0] = $segments->{$seg}->[1];
                    $segments->{$seg}->[1] = $t;
                    $segments->{$seg}->[3] = 0;
                }
                $segs = $origsegs;
            }
        }
        foreach my $s (@{$segs}) 
        {
            $segments->{$s}->[2]=1;
            # debug version:
            #$raw .= sprintf("    <seg id=%s%d%s /> <!-- from %d to %d %s-->\n", 
            #    $segdelim, $s, $segdelim, 
            #    $segments->{$s}->[0], $segments->{$s}->[1],
            #    $segments->{$s}->[3] ? "(reversed) " : "");
            # less chatty version:
            $raw .= sprintf("    <seg id=%s%d%s>\n", $segdelim, $s, $segdelim);
        }
        $raw .= "  </way>\n";

        print TEMP "<!-- the following is un-ordered and could not be ordered properly -->\n" if ($current == -1);
        print TEMP "<!-- the following has been re-ordered -->\n" if ($current == -2);
        print TEMP "<!-- the following has been re-ordered with segment reversal -->\n" if ($current == -3);
        $raw =~ s/<way /<way action='modify' / if ($current < -1);
        print TEMP $raw;
    }
    else
    {
        print TEMP;
    }
}
close TEMP;

open(TEMP, "$tempfile") or die "Cannot open $tempfile for reading";
while(<TEMP>)
{
    if (/^  <segment id=["'](\d+)["'](.*) from=["'](\d+)["'](.*) to=["'](\d+)(["'])(.*)/)
    {
        my ($id, $r1, $f, $r2, $t, $d, $r3) = ($1, $2, $3, $4, $5, $6, $7);
        if ($segments->{$id}->[3])
        {
            printf "  <segment id=%s%d%s action=%smodify%s from=%s%d%s to=%s%d%s%s%s%s\n",
                $d, $id, $d, $d, $d, $d, $t, $d, $d, $f, $d, $r1, $r2, $r3;
        }
        else
        {
            print;
        }
    }
    else
    {
        print;
    }
}
close(TEMP);
unlink($tempfile);
