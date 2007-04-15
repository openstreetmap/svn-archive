#!/usr/bin/perl -w
#-----------------------------------------------------------------------------
#
#  lines2curves.pl
#
#  This script post-processes Osmarender output to change lines in ways
#  into smooth bezier curves.
#
#  This is what it does for each 'way' in the svg:
#     For each pair of lines that make up the way it will replace the two
#     straight lines with an appropriate bezier curve through the point where
#     the lines meet. Appropriate means that the curve is more localised the
#     sharper the angle, and if the angle is less than 90 degrees it will not
#     introduce a bezier curve.
#
#     If the point where the lines meet has other ways that meet it (at a 'T'
#     junction for example), it leaves those segments untouched (i.e. it
#     doesn't introduce curves for those segments).
#
#  Call it as follows:
#
#    lines2curves.pl YourSVGFile.svg >SVGFileWithCurves.svg
#
#-----------------------------------------------------------------------------
#
#  Copyright 2007 by Barry Crabtree
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
#
#-----------------------------------------------------------------------------

use strict;
use Carp;
use Math::Vec qw(:terse);

my $min_angle = 0.5;
my $min_scale = 0;
#
# transform linear paths to curves
#
# Globals...
my $line_position = 0; # current line position in the svg file
my @svg_lines = ();    # the lines from the svg file
my %point_is_in;       # lookup for 'what ways is this point in?'
my %to_transform;      # ways that need transforming

# first pass, read in the svg lines, build the %point_is_in @svg_lines &
# %to_transform structures.
while (<>) {
    my $line = $_;
    $svg_lines[$line_position] = $line;
    if ( $line =~ m{(<path \s id=\"(?:way|area)_\d+\" \s d=\") # the prefix of the path
                    ([^\"Z]+)                          # the core path itself
                    (.*/>)$                           # the rest
                   }x ) { # found a path
        my $path_prefix    = $1;
        my $path_statement = $2;
        my $path_suffix    = $3;
        my $path_points_ref = path_points($path_statement);
        foreach my $point (@$path_points_ref) {
            my $point_index = $point->[0].$point->[1];
            my $way_list_ref = $point_is_in{$point_index};
            push @$way_list_ref, $path_prefix;
            $point_is_in{$point_index} = $way_list_ref;
        }

        $to_transform{$path_prefix}
            = [$line_position, $path_statement, $path_suffix, $path_points_ref];
        
    }
    $line_position++;
}

# Second pass, create the bezier curve versions of all the paths that have
# been found.
foreach my $path (keys %to_transform) {
    # transform path_statment to curves
    my $path_info_ref  = $to_transform{$path};
    my $line_index     = $path_info_ref->[0];
    my $path_statement = $path_info_ref->[1];
    my $path_suffix    = $path_info_ref->[2];

    my $transformed_path = curvify_path($path_statement, $path);
    $svg_lines[$line_index] = $path.$transformed_path.$path_suffix."\n";
}

# print out the transformed svg file.
foreach my $line (@svg_lines) {
    print $line;
}


# Get all the points in a path, removing duplicates along the way...
sub path_points {
    my $path_string = shift;
    my $path_points_ref;

    # there may be multiple moves in a path so get each one
    my @move_segments = split /M\s*/, $path_string;

    foreach my $move_segment (@move_segments) {
        next if !$move_segment; # there is no pre-seg if there is only 1 'M'

        # get all the points in the path
        my $tmp_points_ref = [map [split /(?:\s|,)/, $_], split('L', $move_segment)];
        # stop those occasional divide by zero errors...
        push @$path_points_ref, @$tmp_points_ref; 
    }

    my $clean_points_ref = remove_duplicate_points($path_points_ref);
    return $clean_points_ref;
}

sub remove_duplicate_points {
    my $points_ref = shift;

    my $clean_points_ref = [$points_ref->[0]];
    shift @$points_ref;

    foreach my $point_ref (@$points_ref) {
        if ($point_ref->[0].$point_ref->[1] eq
                $clean_points_ref->[-1][0].$clean_points_ref->[-1][1]) {
                next;
        }

        push @$clean_points_ref, $point_ref;
    }
    if (scalar(@$points_ref)+1 != scalar(@$clean_points_ref)) {
    } 
    return $clean_points_ref;
}

sub remove_spur_points {
    my $points_ref = shift;

# spur points are when you get a way that goes A->B->A->C. The point 'B' is a
# spur point & we don't like 'em.

    my $clean_points_ref = [$points_ref->[0]];

    shift @$points_ref;

    for(my $i=0; $i < scalar(@$points_ref)-1; $i++) {
        if ($clean_points_ref->[-1][0].$clean_points_ref->[-1][1] ne 
            $points_ref->[$i+1][0].$points_ref->[$i+1][1]
           ) {
           push @$clean_points_ref, $points_ref->[$i];
        }
    }
    push @$clean_points_ref, $points_ref->[-1];

    return $clean_points_ref;
}

sub dup_points {
    my $points_ref = shift;

    foreach my $p (@$points_ref) {
        my $count = 0;
        foreach my $q (@$points_ref) {
            if (join('', @$p) eq join('', @$q)) {
                $count++;
            }
        }
        if ($count > 1) {
            return 1;
        }
    }
    return;
}

# splits up the path string and calls 'from_lines_to_curves' appropriately to
# build up a modified path string.
sub curvify_path {
    my $path_string = shift;
    my $way_id      = shift;

    my $tmp_string = $path_string;
    $tmp_string =~ s/[^L]//g;

    # 
    if (length($tmp_string) < 1) { # cant do much with a single line segment
        return $path_string;
    }

    my $bezier_path_string = q{};

    # there may be multiple moves in a path so get each one
    my @move_segments = split /M\s*/, $path_string;

    foreach my $move_segment (@move_segments) {
        next if !$move_segment; # there is no pre-seg if there is only 1 'M'

        # get all the points in the path
        my @path_points = map [split /(?:\s|,)/, $_], split('L', $move_segment);

        if ($way_id =~ /way_/ && dup_points(\@path_points)) {
            $bezier_path_string .= "M$path_string"; 
        } else {
            $bezier_path_string 
                .= 'M'.from_lines_to_curves(\@path_points, $way_id);
        }
    }

    return $bezier_path_string;
}

# When two ways meet at their ends, we need the second point in the 'other'
# way to get good control points in the bezier curve. This just returns the
# second point.
sub get_second_point {
    my ($start_point, $way_id) = @_;

# uncomment the line below if you don't want the last segment in a way to be
# curved when it meets another way.
# 
#    return undef;
    my $ways_ref = $point_is_in{$start_point};

    if ($way_id =~ /area_/) { # areas are easier...
        my $way_points = $to_transform{$way_id}->[3];
        if ($way_points->[0][0].$way_points->[0][1] eq $start_point) {
            return $way_points->[-1];
        } else {
            return $way_points->[0];
        }
    }

    # now do normal ways...
    # more than two ways meet - dont curve these.
    return undef if @$ways_ref != 2;

    my $otherway = ($ways_ref->[0] eq $way_id && $ways_ref->[1] ne $way_id)? $ways_ref->[1]: $ways_ref->[0];

    # maybe there wasn't another way.
    return undef if !$otherway;
    # maybe this way has a loop in it.
#    return undef if $otherway eq $way_id;

    my $way_points = $to_transform{$otherway}->[3];
    
    if ($start_point eq $way_points->[0][0].$way_points->[0][1]) {
        return $way_points->[1];
    } elsif ($start_point eq $way_points->[-1][0].$way_points->[-1][1]) {
        return $way_points->[-2];
    }

    return undef;
}

# heavy work here. Takes the set of points in a path and generates a bezier
# version where appropriate.
sub from_lines_to_curves {
    my $points_ref = shift;
    my $way_id     = shift;

    my $cp_range = 0.5;
    my $incremental_string = q{};

    # Add a point at either end of the set of points to make it easy to
    # generate the correct control points. If this way is standalone, then the
    # ends of the way are extended straight back from the start/end
    # points. If it joins another way, then use the second point in the
    # other way as the end point. This will make ways that join connect
    # smoothly - a point that Jochn Topf noticed.
    #
    my $start_point = $points_ref->[0][0].$points_ref->[0][1];
    my $end_point   = $points_ref->[-1][0].$points_ref->[-1][1];

    my $second_point_ref;
    
    if ($start_point eq $end_point && $way_id =~ /area_/) {
        $second_point_ref = $points_ref->[-2];
    } else {
        $second_point_ref = get_second_point($start_point, $way_id);
    }
    if ($second_point_ref && $second_point_ref->[0].$second_point_ref->[1] ne
    $points_ref->[1][0].$points_ref->[1][1]) {
        unshift @$points_ref, $second_point_ref;
    } else { # make a dummy point
        unshift @$points_ref, [ $points_ref->[0][0]
                               -$points_ref->[1][0] + $points_ref->[0][0],
                                $points_ref->[0][1]
                               -$points_ref->[1][1] + $points_ref->[0][1] ];
    }

    if ($start_point eq $end_point && $way_id =~ /area_/) {
        $second_point_ref = $points_ref->[1];
    } else {
        $second_point_ref = get_second_point($end_point, $way_id);
    }
    if ($second_point_ref && $second_point_ref->[0].$second_point_ref->[1] ne
    $points_ref->[-2][0].$points_ref->[-2][1]) {
        push @$points_ref, $second_point_ref;
    } else { # make a dummy point
        push @$points_ref, [ $points_ref->[-1][0]
                            +$points_ref->[-1][0] - $points_ref->[-2][0],
                             $points_ref->[-1][1]
                            +$points_ref->[-1][1] - $points_ref->[-2][1] ];
    }

    $points_ref = remove_duplicate_points(remove_spur_points($points_ref));
    my $points_in_line     = scalar(@$points_ref);
    my $current_point      = 0;
    my $path_start_ref     = shift @$points_ref;
    my $path_mid_ref       = $points_ref->[0];
    my $path_end_ref       = $points_ref->[1];

    my ($start_v,  $mid_v,  $end_v, $mid_start_v, $mid_end_v );
    my ($start_mid_nv, $mid_start_nv, $mid_end_nv);
    my $control_v;
    my $control_scale;

    my $pl;

    foreach my $p (@$points_ref) {
        $pl .= "\n\t".join(':', @$p);
    }
#    print "$way_id: $pl\n";

    # go round each set of 3 points to generate the bezier points
    while (@$points_ref >= 3) {
        if (!$incremental_string) {
            $incremental_string = q{ }.$path_mid_ref->[0].q{ }.$path_mid_ref->[1];
        }

        # decide to use real control points or not. We only use real control
        # points if this node is only referenced in one 'way' or we are at the
        # beginning/end of a way that only joins with one other way. This makes ways
        # that 'T' sharp on the join.
        if (   $way_id =~ /area_/
            || @{$point_is_in{$path_mid_ref->[0].$path_mid_ref->[1]}} == 1
            || (   $current_point == 0 
                && @{$point_is_in{$path_mid_ref->[0].$path_mid_ref->[1]}} == 2)
            || (   $current_point == $points_in_line-3 
                && @{$point_is_in{$path_mid_ref->[0].$path_mid_ref->[1]}} == 2)
            ) {

#            print "\n\t->".join(':', @$path_start_ref)." ".join(':',
#            @$path_mid_ref)." ".join(':',@$path_end_ref)."\n";
            $incremental_string .= ' C ';
            # work out control point 1 from $path_start, $path_mid & $path_end
            $start_v = V($path_start_ref->[0], $path_start_ref->[1]);
            $mid_v   = V($path_mid_ref->[0], $path_mid_ref->[1]);
            $end_v   = V($path_end_ref->[0], $path_end_ref->[1]);
            $start_mid_nv = U( ($mid_v-$start_v) + ($end_v-$mid_v) );
            $mid_start_v = V($start_v->[0]-$mid_v->[0], $start_v->[1]-$mid_v->[1]);
            $mid_end_v   = V($end_v->[0]-$mid_v->[0], $end_v->[1]-$mid_v->[1]);
            $control_scale = normalise_cp($mid_start_v, $mid_end_v);
            $control_v = $mid_v + V($start_mid_nv->ScalarMult($control_scale*abs($end_v-$mid_v)*$cp_range));

            $incremental_string .= $control_v->[0].','.$control_v->[1].q{ };
            
            # move on a segment
            $path_start_ref = $path_mid_ref;
            $path_mid_ref   = $path_end_ref;
            $path_end_ref   = $points_ref->[2];
            shift @$points_ref;
    
            # work out control point 2 from new $path_start, $path_mid & path_end
            $start_v = V($path_start_ref->[0], $path_start_ref->[1]);
            $mid_v   = V($path_mid_ref->[0], $path_mid_ref->[1]);
            $end_v   = V($path_end_ref->[0], $path_end_ref->[1]);
            $start_mid_nv = U( ($start_v-$mid_v) + ($mid_v-$end_v) );
            $mid_start_v = V($start_v->[0]-$mid_v->[0], $start_v->[1]-$mid_v->[1]);
            $mid_end_v   = V($end_v->[0]-$mid_v->[0], $end_v->[1]-$mid_v->[1]);
                $control_scale = normalise_cp($mid_start_v, $mid_end_v);
            $control_v = $mid_v + V($start_mid_nv->ScalarMult($control_scale * abs($mid_v-$start_v)*$cp_range));
    
            $incremental_string .= $control_v->[0].','.$control_v->[1].q{ };
    
            $incremental_string .= $path_mid_ref->[0].q{ }.$path_mid_ref->[1].q{ };
        } else { # make a straight line segment
            $incremental_string .= 'L'.$path_end_ref->[0].','.$path_end_ref->[1].q{};
            # move on a segment
            $path_start_ref = $path_mid_ref;
            $path_mid_ref   = $path_end_ref;
            $path_end_ref   = $points_ref->[2];
            shift @$points_ref; 
        }
    }
    
    return $incremental_string;
}


# if the angle of the control point is less than 90 degrees return 0
# between 90 & 180 degrees return a number between 0 & 1.
sub normalise_cp {
    my ($start_v, $end_v) = @_;
    my $PI = 3.1415926;
    my $max_angle = $PI/2; # 180degrees
    my $angle = $start_v->InnerAngle($end_v);
    
    if ($angle < $PI*$min_angle) { # too small, so 
        return 0;
    }

    # angle is between $PI/4 and $PI/2
    $angle = $angle - $PI*$min_angle;
    return $angle / ($PI*$min_angle);
}
