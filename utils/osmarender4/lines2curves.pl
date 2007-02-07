#!/usr/bin/perl -w
#-----------------------------------------------------------------------------
#
#  lines2curves.pl
#
#  This script post-processes Osmarender output to change lines in ways
#  into smooth bezier curves.
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
use Math::Vec qw(:terse);

#
# transform linear paths to curves
#

while (<>) {
    my $line = $_;
    if ( $line =~ m{(<path \s id=\"way_\d+\" \s d=\") # the prefix of the path
                    ([^\"]+)                          # the core path itself
                    (.*/>)$                           # the rest
                   }x ) { # found a path
        my $path_prefix    = $1;
        my $path_statement = $2;
        my $path_suffix    = $3;
 
        # transform path_statment to curves
        my $transformed_path = curvify_path($path_statement);
        print $path_prefix, $transformed_path, $path_suffix, "\n";
    }
    else {
        print $line;
    }
}

sub curvify_path {
    my $path_string = shift;

    my $tmp_string = $path_string;
    $tmp_string =~ s/[^L]//g;

    if (length($tmp_string) < 2) { # cant do much with a single line segment
        return $path_string;
    }

    my $bezier_path_string = q{};

    # there may be multiple moves in a path so get each one
    my @move_segments = split 'M', $path_string;

    foreach my $move_segment (@move_segments) {
        next if !$move_segment; # there is no pre-seg if there is only 1 'M'

        # get all the points in the path
        my @path_points = map [split q{ }, $_], split('L', $move_segment);

        $bezier_path_string .= 'M' . from_lines_to_curves(\@path_points);
    }

    return $bezier_path_string;
}

sub from_lines_to_curves {
    my $points_ref = shift;

    my $incremental_string = q{};

    # add dummy points to the start and end of the array
    # to make the looping simpler
    unshift @$points_ref, [ $points_ref->[0][0]
                           -$points_ref->[1][0] + $points_ref->[0][0],
                            $points_ref->[0][1]
                           -$points_ref->[1][1] + $points_ref->[0][1] ];
    push @$points_ref, [ $points_ref->[-1][0]
                        +$points_ref->[-1][0] - $points_ref->[-2][0],
                         $points_ref->[-1][1]
                        +$points_ref->[-1][1] - $points_ref->[-2][1] ];

    my $path_start_ref     = shift @$points_ref;
    my $path_mid_ref       = $points_ref->[0];
    my $path_end_ref       = $points_ref->[1];

    my ($start_v,  $mid_v,  $end_v, $mid_start_v, $mid_end_v );
    my ($start_mid_nv, $mid_start_nv, $mid_end_nv);
    my $control_v;
    my $control_scale;

    while (@$points_ref >= 3) {
        if (!$incremental_string) {
            $incremental_string = q{ }.$path_mid_ref->[0].q{ }.$path_mid_ref->[1].' C ';
        }
        else {
            $incremental_string .= ' C ';
        }

        # work out control point 1 from $path_start, $path_mid & $path_end
        $start_v = V($path_start_ref->[0], $path_start_ref->[1]);
        $mid_v   = V($path_mid_ref->[0], $path_mid_ref->[1]);
        $end_v   = V($path_end_ref->[0], $path_end_ref->[1]);
        $start_mid_nv = U( ($mid_v-$start_v) + ($end_v-$mid_v) );
        $mid_start_v = V($start_v->[0]-$mid_v->[0], $start_v->[1]-$mid_v->[1]);
        $mid_end_v   = V($end_v->[0]-$mid_v->[0], $end_v->[1]-$mid_v->[1]);
        $control_scale = normalise_cp($mid_start_v, $mid_end_v);
        $control_v = $mid_v + V($start_mid_nv->ScalarMult($control_scale*abs($end_v-$mid_v)/2));

        $incremental_string .= $control_v->[0].','.$control_v->[1].q{ };
        
        # move on a segment:0
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
        $control_v = $mid_v + V($start_mid_nv->ScalarMult($control_scale * abs($mid_v-$start_v)/2));

        $incremental_string .= $control_v->[0].','.$control_v->[1].q{ };

        $incremental_string .= $path_mid_ref->[0].q{ }.$path_mid_ref->[1].q{ };
    }
    
    return $incremental_string;
}


sub normalise_cp {
my $PI = 3.1415926;
    my ($start_v, $end_v) = @_;

    my $angle = $start_v->InnerAngle($end_v);
    
    if ($angle < $PI*5/8) { # too small, so 
        return 0;
    }

    # angle is between $PI/4 and $PI/2
    $angle = $angle - $PI*5/8;
    return $angle / ($PI*5/8);
}
