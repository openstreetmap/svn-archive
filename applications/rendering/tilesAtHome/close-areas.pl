#!/usr/bin/perl

# A program to form closed areas from incomplete sets of bordering 
# segments, by making an assumption about which side of the segment
# the enclosed area lies on.
#
# Currently only supports "coastline" ways but may be extended in
# the future.
#
# A detailed discussion should be in the Wiki:
# http://wiki.openstreetmap.org/index.php/Tiles%40home/Dev/Interim_Coastline_Support
#
# Written by Frederik Ramm <frederik@remote.org> - public domain

use Math::Trig;
use Math::Complex;
use strict;

my $segcount = 0;
my $nodecount = 0;
my $waycount = 0;
my $halfpi = pi()/2;
my $twopi = pi()*2;

my $LimitY = pi();
my $LimitY2 = -pi();
my $RangeY = $twopi;
my $debug=0;
my $make_open_ways=0;
my $segments;
my $nodes;

# The direction in which areas are closed - cw (clockwise), or ccw
# (counter-clockwise). For OSM we normally use cw, which means that
# area boundaries are drawn such that the area is "to the right" of 
# the segment (i.e. coastline segments have water on their right).
#
# "ccw" is unused but supported in case somebody needs it.
my $direction="cw";

my @coastline_segments;

my $tilex;
my $tiley; 
my $zoom;
my $minlat;
my $minlon;
my $maxlat;
my $maxlon;
my $border_crossed;
my $emulate_frollo=1; # unused for 0.5
# hash containing information about segments that start a subpath
my $subpath_start;

if (scalar(@ARGV) == 2 or scalar(@ARGV) == 3)
{
    ($tilex, $tiley, $zoom) = @ARGV;
    if (!$zoom) {$zoom = 12;}
    ($maxlat, $minlat) = Project($tiley, $zoom);
    ($minlon, $maxlon) = ProjectL($tilex, $zoom);
}
elsif (scalar(@ARGV) == 4)
{
    ($minlat, $minlon, $maxlat, $maxlon) = @ARGV;
}
else
{
    die "need either 'tilex tiley [zoom]' (defaults to zoom=12) or 'minlat minlon maxlat maxlon' on cmdline";
}

my $sigma = 0.01;

my $helpernodes =
    { 0 => [ $maxlat + $sigma, $maxlon + $sigma], 
      1 => [ $maxlat + $sigma, $minlon - $sigma ], 
      2 => [ $minlat - $sigma, $minlon - $sigma ], 
      3 => [ $minlat - $sigma, $maxlon + $sigma ] };

my $copy = 1;
my $waybuf;
my $is_coastline;
my @seglist;
my $version;
my $last_node_ref; # for 0.5
my $segcount = 0;  # for 0.5

while(<STDIN>)
{
    while(/(<[^'"<>]*((["'])[^\3]*?\3[^<>"']*)*>)/og)

    {
        my $xmltag = $1;
        if ($xmltag =~ /^\s*<node.*id=["'](\d+)['"].*lat=["']([0-9.Ee-]+)["'].*lon=["']([0-9.Ee-]+)["']/)
        {
            $nodes->{$1} = { "lat" => $2, "lon" => $3 };
        }
        elsif ($xmltag =~ /^\s*<segment.*id=["'](\d+)['"].*from=["']([0-9.-]+)["'].*to=["']([0-9.-]+)["']/)
        {
            $segments->{$1} = { "from" => $2, "to" => $3 };
        }
        elsif ($xmltag =~ /^\s*<way /)
        {
            $copy = 0;
            undef $waybuf;
            undef @seglist;
            undef $is_coastline;
            undef $last_node_ref;
        }
        elsif($xmltag =~ /^\s*<\/osm/)
        {
            last;
        }
        elsif($xmltag =~ /^\s*<(osm.*)>/)
        {
            # If frollo encounters an empty file, it outputs <osm foo />. Detect this and exit
            if (substr($1, -1) eq "/" )
            { print $xmltag; exit }
            
            if (/version=['"](.*?)['"]/)
            {
                if ($1 eq "0.4") 
                {
                    $version = "04";
                }
                elsif ($1 eq "0.5")
                {
                    $version = "05";
                }
                else
                {   
                    die ("close-areas.pl does not support version $1");
                }
            }
        }

        if ($copy)
        {
            print $xmltag."\n";
        }
        elsif ($xmltag =~ /^\s*<tag k=['"]natural["'].*v=["']coastline['"]/)
        {
            $is_coastline = 1;  
        }
        elsif ($version eq "04" && ($xmltag =~ /^\s*<seg id=['"](\d+)["']/))
        {
            $waybuf .= $xmltag . "\n";
            push(@seglist, $1);
        }
        elsif ($version eq "05" && ($xmltag =~ /^\s*<nd ref=['"](\d+)["']/))
        {
            $waybuf .= $xmltag . "\n";
            if (defined($last_node_ref))
            {
                $segments->{$segcount} = { "from" => $last_node_ref, "to" => $1 };
                push(@seglist, $segcount++);
            }
            $last_node_ref = $1;
        }
        elsif ($xmltag =~ /^\s*<\/way/)
        {
            $copy = 1;
            # non-coastal ways are written and forgotten
            if (!$is_coastline)
            {
                print $waybuf;
                print $xmltag . "\n";
            }
            # coastal ways are forgotten too, but they pass on their "coastal"
            # information to the segments.
            else
            {
                foreach my $seg(@seglist)
                {
                    $segments->{$seg}->{"coast"} = 1 if (defined($segments->{$seg}));
                }
                        
            }
        }
        else
        {
            $waybuf .= $xmltag . "\n";
        }
    }
}

# file fully read. delete non-coastal segments (we have printed them 
# all, so no need for us to keep them), and check coastal segments for
# intersection with the bounding box.

foreach my $seg(keys(%$segments))
{
    if ($segments->{$seg}->{"coast"})
    {
        my $fromnode = $nodes->{$segments->{$seg}->{"from"}};
        my $tonode = $nodes->{$segments->{$seg}->{"to"}};

        if (!(defined($fromnode) && defined($tonode)))
        {
            delete $segments->{$seg};
            print "delete segment - incomplete $seg\n" if ($debug);
            next;
        }

        # returns 0, 1, or 2 points.
        # (may probably return 3 or 4 points in freak cases)
        my $intersect = compute_bbox_intersections($fromnode, $tonode);
        printf "intersections for $seg: %d\n", scalar(@$intersect) if ($debug);

        if (!scalar(@$intersect))
        {
            # this segment has no intersections with the bounding box.
            if (!node_is_inside($fromnode))
            {
                # this segment is fully outside the bounding box, and of 
                # no concern to us. 
                delete $segments->{$seg};
                print "delete $seg fully out\n" if ($debug);
            }
        }
        elsif (scalar(@$intersect) == 1)
        {
            # this segments enters OR exits the bounding box. find out which,
            # and tag accordingly.
            if (node_is_inside($fromnode))
            {
                $segments->{$seg}->{"exit_intersect"} = $intersect->[0];
                $segments->{$seg}->{"exit_intersect_angle"} =
                    compute_angle_from_bbox_center($intersect->[0]);
            }
            else
            {
                $segments->{$seg}->{"entry_intersect"} = $intersect->[0];
                $segments->{$seg}->{"entry_intersect_angle"} =
                    compute_angle_from_bbox_center($intersect->[0]);
            }
        }
        else
        {
            # this segment enters AND exits the bounding box. as intersection
            # points are ordered by distance from the segment's origin, we 
            # assume that the very first intersection is the entry point and
            # the very last is the exit point.
            #   
            # FIXME: segments like this - probably a very long one cutting right
            # through the box or a short one cutting diagonally at one edge - 
            # are very little tested.
            $segments->{$seg}->{"entry_intersect_angle"} =
                compute_angle_from_bbox_center($intersect->[0]);
            $segments->{$seg}->{"exit_intersect_angle"} =
                compute_angle_from_bbox_center($intersect->[scalar(@$intersect)-1]);
            $segments->{$seg}->{"entry_intersect"} = $intersect->[0];
            $segments->{$seg}->{"exit_intersect"} = $intersect->[scalar(@$intersect)-1];
        }
    }
    else
    {
        print "delete 2 $seg\n" if ($debug);
        delete $segments->{$seg};
    }
}

# we do not need the nodes any longer. they have been printed already.
undef $nodes;

# if no coastline segments are present, switch over to 
# special handler to decide whether to draw a blue tile.

if (!scalar(%$segments))
{
    ## instead of filling the tile here, we do it at the end...
    $border_crossed = 0;
    goto ENDOSM;
}

# now start building artificial ways for coastline segments.
#
# strategy:
# 1. grab any one segment. if none available, we're done.
# 2. find segment that starts where previous segment ends, 
#    repeat until none found OR back where we started.
# 3. if back where we started, write the circular list of segments,
#    delete them, and repeat from 1.
# 4. if none found AND last position is inside drawing area,
#    remove processed segments without writing, and repeat from 1.
# 5. if none found AND last position is outside drawing area,
#    search clockwise outside of drawing area for another
#    segment that begins here, create articifial segment joining
#    latest position and segment's start node, and continue with 
#    step 2. 
#
# Special rule for creating artificial segments: artificial segments
# must never intersect with drawing area. use artificial nodes outside
# the four corners of the drawing area to route segments if necessary.

# copy list of segment ids
my $available_segs;
grep { $available_segs->{$_} = 1 } keys(%$segments);

while (scalar(%$available_segs))
{
    my @seglist;
    # grab any segment as the first one
    my @tmp = keys(%$available_segs);
    my $currentseg = shift(@tmp);
    printf "GRAB %d (from %d to %d)\n", $currentseg, $segments->{$currentseg}->{"from"}, $segments->{$currentseg}->{"to"} if ($debug);
    delete $available_segs->{$currentseg};
    #printf "REMAINING: %d = %s\n", scalar(keys(%$available_segs)), join(",", keys(%$available_segs)) if ($debug);
    my $currentnode = $segments->{$currentseg}->{"to"};
    push (@seglist, $currentseg);

STRING:
    # find a segment that begins where the previous one ended
    while ($currentseg)
    {
        printf "SEARCH begin at %d\n", $currentnode if ($debug);
        undef $currentseg;
        foreach my $seg(keys(%$available_segs))
        {
            #printf "  TEST seg %d begin at %d\n", $seg, $segments->{$seg}->{"from"} if ($debug);
            if ($segments->{$seg}->{"from"} == $currentnode)
            {
                printf "  FOUND seg %d begin at %d\n", $seg, $segments->{$seg}->{"from"} if ($debug);
                push (@seglist, $seg);
                $currentseg = $seg;
                $currentnode = $segments->{$currentseg}->{"to"};
                delete $available_segs->{$seg};
                #printf "REMAINING: %d = %s\n", scalar(keys(%$available_segs)), join(",", keys(%$available_segs)) if ($debug);
                last;
            }
        }
    }

    # no more segments found. do we have a loop?
    if ($currentnode == $segments->{$seglist[0]}->{"from"})
    {
        printf("LOOP\n") if ($debug);
        # loop detected. store the segment list for later output,
        # and move on trying to connect the rest.
        push(@coastline_segments, @seglist);
        $subpath_start->{$seglist[0]} = 1;
        next;
    }

    my $lastseg = @seglist[scalar(@seglist)-1];
    my $exit_angle = $segments->{$lastseg}->{"exit_intersect_angle"};

    # are we inside the drawable area? (we are if the last segment did not
    # exit the drawable area.)
    # if yes, give up as we obviously have an imcomplete piece of coastline.
    if (!defined($exit_angle))
    {
        printf("NOT OUTSIDE\n") if ($debug);
        # this is a debug option that allows one to detect the incomplete
        # way by looking at the output file with JOSM etc.; it is not intended
        # for production use!
        if ($make_open_ways)
        {
            make_way(\@seglist, 1);
        }
        next;
    }

    # "else" case: we are outside the drawable area and want
    # to find another segment that begins outside the drawable
    # area and enters it.
    
    my $segs_to_check;
    $segs_to_check = [];
    foreach my $seg(keys(%$available_segs))
    {
        push(@$segs_to_check, $seg) if defined($segments->{$seg}->{"entry_intersect"});
    }

    # we will also accept the first segment of the current way
    # if it is outside. this is a special case as, being used
    # already, it isn't in the $segments list any
    # more
    
    push(@$segs_to_check, $seglist[0]) 
        if defined($segments->{$seglist[0]}->{"entry_intersect"});

    # sort all segments entering the drawable area by angle from area 
    # centrepoint to the point where they enter the area.
    my @sorted_segs_to_check;
    @sorted_segs_to_check = sort
        { $segments->{$a}->{"entry_intersect_angle"} <=> 
          $segments->{$b}->{"entry_intersect_angle"} } @$segs_to_check;

    @sorted_segs_to_check = reverse @sorted_segs_to_check if ($direction eq "cw");

    # find the nearest entering segment.
    my $found;
    $found = 0;
    foreach my $seg(@sorted_segs_to_check)
    {
        if ($direction eq "cw")
        {
            next if ($segments->{$seg}->{"entry_intersect_angle"}) > $exit_angle;
        }
        else
        {
            next if ($segments->{$seg}->{"entry_intersect_angle"}) < $exit_angle;
        }
        printf("use seg %d angle %f\n", $seg, $segments->{$seg}->{"entry_intersect_angle"}) if ($debug);
        $found = $seg;
        last;
    }
    if (!$found)
    {
        foreach my $seg(@sorted_segs_to_check)
        {
            printf("use (2) seg %d angle %f\n", $seg, $segments->{$seg}->{"entry_intersect_angle"}) if ($debug);
            $found = $seg;
            last;
        }
    }

    # if no segment remains outside, give up
    if (!$found)
    {
        printf("NO SEG OUTSIDE\n") if ($debug);
        # this is a debug option that allows one to detect the incomplete
        # way by looking at the output file with JOSM etc.; it is not intended
        # for production use!
        if ($make_open_ways)
        {
            make_way(\@seglist, 1);
        }
        next;
    }
    
    # at this point, we have a list of segments that ends with a segment 
    # leaving the drawable area, and we also have the next segment where
    # the coastline comes back into the drawable area. we need to connect
    # them with an artifical segment - or more than one.

    # If a coastline leaves the visible area at the top (side 1) and comes
    # back in from the right (side 0), then we must not make a direct connection
    # between the points for fear of clipping our viewport; instead, we must
    # "hop" over the top right corner. Same for other sides. The "helpernodes"
    # hash contains nodes to be used for hopping from one side to the next.
    #
    # (an extreme case of this is coastline leaving and entering at the same
    # side but leaving south of where it enters - need to go around all four
    # corners then, for a total of 5 artificial segments.)

    $border_crossed = 1;
    my $height = $maxlat-$minlat;
    my $width = $maxlon-$minlon;

    # exit_angle is already defined
    my $entry_angle = $segments->{$found}->{"entry_intersect_angle"};

    my $exit_side = $segments->{$lastseg}->{"exit_intersect"}->{"side"};
    my $entry_side = $segments->{$found}->{"entry_intersect"}->{"side"};

    printf("exit angle %s entry angle %s\n", $exit_angle, $entry_angle) if $debug;
    printf("exit side %d entry side %d\n", $exit_side, $entry_side) if $debug;

    # the following two blocks (similar but not identical for clockwise/
    # counter-clockwise) will find out whether we need to "go around corners"
    # and add 0-4 segments if needed.
    if ($direction eq "ccw")
    {
        # $min_once is for the special case where entry and exit side are 
        # identical but we still need to go all the way around the box.
	my $min_once;
	if ($exit_side == 0 and $entry_side == 0) 
	{
	    # Take into account that the angle flips from 2*pi() to zero at this side
	    $min_once = (($exit_angle > $entry_angle and ($exit_angle - $entry_angle) < pi()) ||
			 ($exit_angle < pi() and $entry_angle > pi()));
	}
	else 
	{
	    $min_once = ($exit_angle > $entry_angle); 
	}
        printf("min_once=%d\n", $min_once) if $debug;
        for (my $i = $exit_side;; $i++)
        {
            $i=0 if ($i==4);
            last if ($i==$entry_side && !$min_once);
            $min_once = 0;
            my $newnode = make_node($helpernodes->{$i}->[0], $helpernodes->{$i}->[1]);
            my $newseg = make_seg($currentnode, $newnode);
            $currentnode = $newnode;
            push(@seglist, $newseg);
        }
    }
    else
    {
	my $min_once;
	if ($exit_side == 0 and $entry_side == 0)
	{
	    $min_once = (($exit_angle < $entry_angle and ($entry_angle - $exit_angle) < pi()) ||
			 ($exit_angle > pi() and $entry_angle < pi()));
	}
	else
	{
	    $min_once = ($exit_angle < $entry_angle);
	}
        printf("min_once=%d\n", $min_once) if $debug;
        for (my $i = $exit_side;; $i--)
        {
            $i=3 if ($i==-1);
            last if ($i==$entry_side && !$min_once);
            $min_once = 0;
            my $helper = $i-1;
            $helper = 3 if ($helper == -1);
            my $newnode = make_node($helpernodes->{$helper}->[0], $helpernodes->{$helper}->[1]);
            my $newseg = make_seg($currentnode, $newnode);
            $currentnode = $newnode;
            push(@seglist, $newseg);
        }
    }

    my $newseg = make_seg($currentnode, $segments->{$found}->{"from"});
    push(@seglist, $newseg);

    # if the segment we have found is our start segment (which we added
    # as a special case earlier!), we have a closed way.
    if ($found == $seglist[0])
    {
        printf("CLOSED\n") if ($debug);
        push(@coastline_segments, @seglist);
        $subpath_start->{$seglist[0]} = 1;
        next;
    }

    # else we just plod on.
    push (@seglist, $found);
    $currentseg = $found;
    $currentnode = $segments->{$found}->{"to"};
    delete $available_segs->{$found};
    #nprintf "REMAINING: %d = %s\n", scalar(keys(%$available_segs)), join(",", keys(%$available_segs)) if ($debug);
    goto STRING;
}

ENDOSM:

# if we had no coastline intersection with the bounding box, but coastline
# was present, we have an island situation and need to add a blue background.
# FIXME what if we have a "little lake" situation?

unless( $border_crossed )
{
  my $state = lookup_handler($helpernodes, $tilex, $tiley, $zoom);
  if( $state eq "10" )
  {
    # sea
    addBlueRectangle($helpernodes);
  }
  elsif ( $state eq "01" )
  {
    #land
  }
  else
  {  
    my %temp = ("00"=>0, "10"=>0, "01"=>0, "11"=>0);;
    $temp{lookup_handler($helpernodes, $tilex-1, $tiley, $zoom)}++;
    $temp{lookup_handler($helpernodes, $tilex+1, $tiley, $zoom)}++;
    $temp{lookup_handler($helpernodes, $tilex, $tiley-1, $zoom)}++;
    $temp{lookup_handler($helpernodes, $tilex, $tiley+1, $zoom)}++;

    if( $temp{"10"} > $temp{"01"} )
    {
      addBlueRectangle($helpernodes);
    }
    elsif ( ($state eq "11") and ( $temp{"01"} == 0 ) ) 
    # if the tile is marked coast but no land near, assume it's a group of islands instead of lakes.
    {
      # coast
      addBlueRectangle($helpernodes);
    }
    else
    {
      #land
    }
  }
}
make_way(\@coastline_segments);
print "</osm>\n";

sub make_node
{
    my ($lat, $lon) = @_;
    my $id = --$nodecount;
    print "<node id='$id' lat='$lat' lon='$lon' />\n";
    return $id;
}

sub make_seg
{
    my ($from, $to) = @_;
    my $id = --$segcount;
    print "<segment id='$id' from='$from' to='$to' />\n" if ($version eq "04");
    $segments->{$id} = { "from" => $from, "to" => $to } if ($version eq "05");
    return $id;
}

sub make_way
{
    my ($seglist, $open) = @_;
    my $id = --$waycount;
    print "<way id='$id'>\n";
    print "  <tag k='natural' v='coastline' />\n";
    print "  <tag k='created-with' v='close-areas.pl' />\n";
    print "  <tag k='close-areas.pl:debug' v='open way' />\n" if ($open);

    if ($version eq "04")
    {
        foreach my $seg(@$seglist) 
        { 
            print "  <seg id='$seg' ";
            if ($emulate_frollo)
            {
                print "osma:fromCount='1' osma:toCount='1' ";
                print "osma:sub-path='1' " if ($subpath_start->{$seg}) ;
            }
            print " />\n";
        }
    }
    else # 0.5
    {
        my $first = 1;
        foreach my $seg(@$seglist) 
        { 
            printf "  <nd ref='%s' />\n", $segments->{$seg}->{"from"} if ($first);
            printf "  <nd ref='%s' />\n", $segments->{$seg}->{"to"};
            $first = 0;
        }
    }
    print "</way>\n";
    return $id;
}

# index lookup by Martijn van Oosterhout
sub lookup_handler
{
    my ($helpernodes, $x, $y, $zoom) = @_;
    # make it use z12 x,y coordinates
    # this looks up the most upper left z12 tile in zoom<12. This probably
    # needs to be made smarter for islands etc.
    my $tilex = $x*(2**(12-$zoom));
    my $tiley = $y*(2**(12-$zoom));
    my $tileoffset = ($tiley * (2**12)) + $tilex;
    my $fh;
    open($fh, "<", "oceantiles_12.dat") or die;
    seek $fh, int($tileoffset / 4), 0;  
    my $buffer;
    read $fh, $buffer, 1;
    $buffer = substr( $buffer."\0", 0, 1 );
    $buffer = unpack "B*", $buffer;
    my $str = substr( $buffer, 2*($tileoffset % 4), 2 );
    close($fh);

    print("lookup handler finds: $str\n") if $debug;

    # $str eq "00" => unknown (not yet checked)
    # $str eq "01" => known land
    # $str eq "10" => known sea
    # $str eq "11" => known edge tile

    return $str; 
}

sub addBlueRectangle
{
    my $helpernodes = shift;
    my @n;
    my @s;
    for (my $i=0; $i<4; $i++)
    {
        $n[$i] = make_node($helpernodes->{$i}->[0], 
                         $helpernodes->{$i}->[1]);
    }
    for (my $i=0; $i<4; $i++)
    {
        if ($direction eq "ccw")
        {
            $s[$i] = make_seg($n[$i], $n[($i+1)%4]);
        }
        else
        {
            $s[3-$i] = make_seg($n[($i+3)%4], $n[($i+2)%4]);
        }
    }
    push(@coastline_segments, @s);
    $subpath_start->{$s[0]} = 1;
}

# this takes two points (hashes with lat/lon keys) as arguments and returns 
# an array reference to an array containing up to four points (hashes with
# lat/lon keys and an added "side" key, where 0=right 1=top 2=left 3=bottom) 
# denoting the intersections of the line formed by the input points with the 
# bounding box.
#
# 0 results - segment is fully inside or fully outside bounding box
# 1 result  - segment begins inside, ends outside or vice versa
# 2 results - segment begins outside and ends outside, but cuts through
#             bounding box
# 3 results - can't think how this can happen
# 4 results - as case 2 but segment is exactly at the bounding box diagonal,
#             thus intersecting with all four edges (freak case)
#
# If there is more than one result, results are sorted by distance from the
# first point.
sub compute_bbox_intersections
{
    my ($from, $to) = @_;
    my $result = [];
    my $point;

    # div by zero FIXME
    my $latd = ($to->{"lat"} - $from->{"lat"});
    my $lond = ($to->{"lon"} - $from->{"lon"});

    # segment's bbox
    my $s_minlon = $from->{"lon"};
    my $s_minlat = $from->{"lat"};
    $s_minlon = $to->{"lon"} if ($to->{"lon"} < $s_minlon);
    $s_minlat = $to->{"lat"} if ($to->{"lat"} < $s_minlat);
    my $s_maxlon = $from->{"lon"};
    my $s_maxlat = $from->{"lat"};
    $s_maxlon = $to->{"lon"} if ($to->{"lon"} > $s_maxlon);
    $s_maxlat = $to->{"lat"} if ($to->{"lat"} > $s_maxlat);

    printf "BBOX:\n minlat %f\n minlon %f\n maxlat %f\n maxlon %f\n",
        $minlat, $minlon, $maxlat, $maxlon if ($debug);

    printf "SBBOX:\n minlat %f\n minlon %f\n maxlat %f\n maxlon %f\n",
        $s_minlat, $s_minlon, $s_maxlat, $s_maxlon if ($debug);

    # only if the segment is not horizontal
    if ($latd != 0)
    {
        # intersection with top of bounding box
        $point = { 
            "side" => "1",
            "lat" => $maxlat, 
            "lon" => $from->{"lon"} + ($maxlat - $from->{"lat"}) * $lond / $latd 
        };
        push (@$result, $point) 
            if ($point->{"lat"} >= $s_minlat && $point->{"lat"} <= $s_maxlat) &&
               ($point->{"lon"} >= $s_minlon && $point->{"lon"} <= $s_maxlon) &&
               ($point->{"lon"} >= $minlon && $point->{"lon"} <= $maxlon);


        # intersection with bottom of bounding box
        $point = { 
            "side" => "3",
            "lat" => $minlat, 
            "lon" => $from->{"lon"} + ($minlat - $from->{"lat"}) * $lond / $latd 
        };
        push (@$result, $point) 
            if ($point->{"lat"} >= $s_minlat && $point->{"lat"} <= $s_maxlat) &&
               ($point->{"lon"} >= $s_minlon && $point->{"lon"} <= $s_maxlon) &&
               ($point->{"lon"} >= $minlon && $point->{"lon"} <= $maxlon);
    }

    # only if the segment is not vertical
    if ($lond != 0)
    {
        # intersection with left of bounding box
        $point = { 
            "side" => "2",
            "lat" => $from->{"lat"} + $latd / $lond * ($minlon - $from->{"lon"}),
            "lon" => $minlon
        };
        push (@$result, $point) 
            if ($point->{"lat"} >= $s_minlat && $point->{"lat"} <= $s_maxlat) &&
               ($point->{"lon"} >= $s_minlon && $point->{"lon"} <= $s_maxlon) &&
               ($point->{"lat"} >= $minlat && $point->{"lat"} <= $maxlat);

        # intersection with right of bounding box
        $point = { 
            "side" => "0",
            "lat" => $from->{"lat"} + $latd / $lond * ($maxlon - $from->{"lon"}),
            "lon" => $maxlon
        };
        push (@$result, $point) 
            if ($point->{"lat"} >= $s_minlat && $point->{"lat"} <= $s_maxlat) &&
               ($point->{"lon"} >= $s_minlon && $point->{"lon"} <= $s_maxlon) &&
               ($point->{"lat"} >= $minlat && $point->{"lat"} <= $maxlat);
    }

    # if more than 1 result, sort by distance from origin of segment
    # (strictly speaking this sorts by distance squared but why waste
    # a sqrt call)
    if (scalar(@$result) > 1)
    {
        my @tmp = sort 
            { (($a->{"lat"} - $from->{"lat"})**2 + ($a->{"lon"} - $from->{"lon"})**2) <=> 
              (($b->{"lat"} - $from->{"lat"})**2 + ($b->{"lon"} - $from->{"lon"})**2) } @$result;
        $result = \@tmp;
    }

    #printf "intersections for segment %f,%f - %f,%f:\n", $from->{"lat"}, $from->{"lon"}, $to->{"lat"}, $to->{"lon"};
    #print Dumper($result);

    return $result;
}

sub node_is_inside
{
    my $point = shift;
    return 0 if ($point->{"lat"} > $maxlat);
    return 0 if ($point->{"lat"} < $minlat);
    return 0 if ($point->{"lon"} > $maxlon);
    return 0 if ($point->{"lon"} < $minlon);
    return 1;
}

# - - - - - - - - - rest copied verbatim from tilesGen.pl - - - - - - - - - 

#-----------------------------------------------------------------------------
# Project X to longitude bounds
#-----------------------------------------------------------------------------
sub ProjectL {
  my ($X, $Zoom) = @_;
  
  my $Unit = 360 / (2 ** $Zoom);
  my $Long1 = -180 + $X * $Unit;
  return(($Long1, $Long1 + $Unit));  
}
#-----------------------------------------------------------------------------
# Project Y to latitude bounds
#-----------------------------------------------------------------------------
sub Project {
  my ($Y, $Zoom) = @_;
  
  my $Unit = 1 / (2 ** $Zoom);
  my $relY1 = $Y * $Unit;
  my $relY2 = $relY1 + $Unit;
  
  $relY1 = $LimitY - $RangeY * $relY1;
  $relY2 = $LimitY - $RangeY * $relY2;
    
  my $Lat1 = ProjectMercToLat($relY1);
  my $Lat2 = ProjectMercToLat($relY2);
  return(($Lat1, $Lat2));  
}

#-----------------------------------------------------------------------------
# Convert Y units in mercator projection to latitudes in degrees
#-----------------------------------------------------------------------------
sub ProjectMercToLat($){
  my $MercY = shift();
  return(RadToDeg(atan(sinh($MercY))));
}

#-----------------------------------------------------------------------------
# Angle unit-conversions
#-----------------------------------------------------------------------------
sub DegToRad($){return pi * shift() / 180;}
sub RadToDeg($){return 180 * shift() / pi;}


sub compute_angle_from_bbox_center
{
    my ($node) = @_;
    my $opposite_leg = ($node->{"lat"}-($maxlat-$minlat)/2-$minlat);
    my $adjacent_leg = ($node->{"lon"}-($maxlon-$minlon)/2-$minlon);
    my $z = cplx($adjacent_leg, $opposite_leg);
    return (arg($z) < 0) ? arg($z) + 2*pi : arg($z);
}

