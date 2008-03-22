#!/usr/bin/perl

# or/p - Osmarender in Perl
# -------------------------
#
# Drawing Module
#
# (See orp.pl for details.)
#
# This module contains the routines that issue SVG drawing instructions
# object selection supported in <rule> elements.

use strict;
use warnings;
require "orp-bbox-area-center.pm";

our ($writer, $projection, $symbolScale, $textAttenuation, $debug);


# -------------------------------------------------------------------
# sub draw_lines($rulenode, $layer, $selection)
#
# for each selected object referenced by the $selection structure,
# draw a line.
#
# Parameters:
# $rulenode - the XML::XPath::Node object for the <line> instruction
#    in the rules file.
# $layer - if not undef, process only objects on this layer
# $selected - the list of currently selected objects
#
# Return value:
# none.
#
# Processes only ways from the selection.
# -------------------------------------------------------------------
sub draw_lines
{
    my ($linenode, $layer, $selected) = @_;
    # FIXME copy node svg: attributes
    my $smart_linecaps = ($linenode->getAttribute("smart-linecap") ne "no");
    my $class = $linenode->getAttribute("class");
    my $mask_class = $linenode->getAttribute("mask-class") || '';
    my $group_started = 0;

    foreach ($selected->members)
    {
        next unless (ref $_  eq 'way');
        next if defined($layer) and $_->{'layer'} != $layer;
        $writer->startTag("g", 
            "class" => $class,
            ($mask_class ne "") ? ("mask-class", $mask_class) : ()) unless $group_started;
        $group_started = 1;
        if ($smart_linecaps)
        {
            draw_way_with_smart_linecaps($linenode, $layer, $_, $class);
        }
        else
        {
            draw_path($linenode, "way_normal_".$_->{"id"});
        }
    }
    $writer->endTag("g") if ($group_started);
}

# The following comment is from the original osmarender.xsl and describes 
# how "smart linecaps" work:
#
# The first half of the first segment and the last half of the last segment 
# are treated differently from the main part of the way path.  The main part 
# is always rendered with a butt line-cap.  Each end fragement is rendered with
# either a round line-cap, if it connects to some other path, or with its 
# default line-cap if it is not connected to anything.  That way, cul-de-sacs 
# etc are terminated with round, square or butt as specified in the style for 
# the way.

sub draw_way_with_smart_linecaps
{
    my ($linenode, $layer, $way, $class) = @_;

    # convenience variables
    my $id = $way->{"id"};
    my $nodes = $way->{"nodes"};

    return unless(scalar(@$nodes));

    # first draw middle segment if we have more than 2 nodes
    draw_path($linenode, "way_mid_$id", 
        "osmarender-stroke-linecap-butt osmarender-no-marker-start osmarender-no-marker-end") 
        if (scalar(@$nodes)>2);

    # count connectors on first and last node
    my $first_node_connection_count = scalar(@{$nodes->[0]->{"ways"}});
    my $first_node_lower_layer_connection_count = $first_node_connection_count;
    if ($first_node_connection_count > 1)
    {
        # need to explicitly count lower layer connections.
        $first_node_lower_layer_connection_count = 0;
        foreach my $otherway(@{$nodes->[0]->{"ways"}})
        {
            $first_node_lower_layer_connection_count++ if ($otherway->{'layer'} < $way->{'layer'});
        }
    }

    my $last_node_connection_count = scalar(@{$nodes->[scalar(@$nodes)-1]->{"ways"}});
    my $last_node_lower_layer_connection_count = $last_node_connection_count;
    if ($last_node_connection_count > 1)
    {
        # need to explicitly count lower layer connections.
        $last_node_lower_layer_connection_count = 0;
        foreach my $otherway(@{$nodes->[scalar(@$nodes)-1]->{"ways"}})
        {
            $last_node_lower_layer_connection_count++ if ($otherway->{'layer'} < $way->{'layer'});
        }
    }

    if ($first_node_connection_count == 1)
    {
        draw_path($linenode, "way_start_$id", "osmarender-no-marker-end");
    } 
    elsif ($first_node_lower_layer_connection_count > 0)
    {
        draw_path($linenode, "way_start_$id", 
            "osmarender-stroke-linecap-butt osmarender-no-marker-end");
    }
    else
    {
        draw_path($linenode, "way_start_$id",
            "osmarender-stroke-linecap-round osmarender-no-marker-end");
    }

    if ($last_node_connection_count == 1)
    {
        draw_path($linenode, "way_end_$id", "osmarender-no-marker-start");
    } 
    elsif ($last_node_lower_layer_connection_count > 0)
    {
        draw_path($linenode, "way_end_$id", 
            "osmarender-stroke-linecap-butt osmarender-no-marker-start");
    }
    else
    {
        draw_path($linenode, "way_end_$id", 
            "osmarender-stroke-linecap-round osmarender-no-marker-start");
    }
}

# -------------------------------------------------------------------
# sub draw_areas($rulenode, $layer, $selection)
#
# for each selected object referenced by the $selection structure,
# create a closed path and draw an area.
#
# Parameters:
# $rulenode - the XML::XPath::Node object for the <area> instruction
#    in the rules file.
# $layer - if not undef, process only objects on this layer
# $selected - the list of currently selected objects
#
# Return value:
# none.
#
# Processes ways and nodes from the selection.
# -------------------------------------------------------------------
sub draw_areas
{
    my ($areanode, $layer, $selected) = @_;
    # FIXME copy node svg: attributes
    my $class = $areanode->getAttribute("class");

    $writer->startTag("g", "class" => $class);

OUTER:
    foreach ($selected->members)
    {
        next unless (ref $_ eq 'way');
        next if defined($layer) and $_->{'layer'} != $layer;

        my $points = [];
        foreach (@{$_->{"nodes"}})
        {
            push(@$points, [ $_->{"lat"}, $_->{"lon"} ]) if (defined($_->{"lat"}) && defined($_->{"lon"}));
        }
        my $path = make_path(@$points)."Z ";

        # find out if we're the "outer" or "inner" polygon of a "multipolygon" relation
        foreach my $relpair(@{$_->{"relations"}})
        {
            my ($role, $rel) = @$relpair;
            if ($rel->{"tags"}->{"type"} eq "multipolygon" && $role eq "outer")
            {
                # right, we are "outer" - find all "inner" ways of this relation 
                # and add them to our path
                foreach my $relmember(@{$rel->{"members"}})
                {
                    my ($role, $obj) = @$relmember;
                    if ($role eq "inner" && ref($obj) eq "way")
                    {
                        #debug(sprintf("collecting way %d as 'hole' in polygon %d",
                        #    $obj->{"id"}, $_->{"id"}));
                        $points = [];
                        foreach (@{$obj->{"nodes"}})
                        {
                            push(@$points, [ $_->{"lat"}, $_->{"lon"} ]) if (defined($_->{"lat"}) && defined($_->{"lon"}));
                        }
                        $path .= make_path(@$points)."Z";
                    }
                }
            }
            if ($rel->{"tags"}->{"type"} eq "multipolygon" && $role eq "inner")
            {
                # we are "inner" - if the corresponding "outer" poly is tagged 
                # the same as we are, then don't draw anything (legacy polygon
                # support). otherwise draw normally.
                foreach my $relmember(@{$rel->{"members"}})
                {
                    my ($role, $obj) = @$relmember;
                    if ($role eq "outer" && ref($obj) eq "way")
                    {
                        next OUTER if (tags_subset($_, $obj));
                        last;
                    }
                }
            }
        }
        $writer->emptyTag("path", "id" => "area_".$_->{"id"}, "d" => $path, "style" => "fill-rule:evenodd");
        $writer->emptyTag("use", 
            "xlink:href" => "#area_".$_->{"id"},
            "class" => $class);
    }
    $writer->endTag("g");
}

# returns true if the first has a subset of the second object's tags,
# with some tags being ignored
sub tags_subset
{
    my ($first, $second) = @_;
    foreach my $tag(%{$first->{"tags"}})
    {
        next if ($tag =~ /^(name|created_by|note)$/);
        return 0 unless defined($second->{'tags'}{$tag}) && $first->{'tags'}{$tag} eq $second->{'tags'}{$tag};
    }
    return 1;
}

# -------------------------------------------------------------------
# sub draw_text($rulenode, $layer, $selection)
#
# for each selected object referenced by the $selection structure,
# draw the specified text along the path or at the node.
#
# Parameters:
# $rulenode - the XML::XPath::Node object for the <text> instruction
#    in the rules file.
# $layer - if not undef, process only objects on this layer
# $selected - the list of currently selected objects
#
# Return value:
# none.
#
# Processes ways and nodes from the selection.
# -------------------------------------------------------------------
sub draw_text
{
    my ($textnode, $layer, $selected) = @_;

    # the text instruction has two different ways of accessing the text it is
    # going to write:
    # (a) <text k="name" ... />
    #     This will write the value of the "name" tag without further ado.
    # (b) <text>The name is <tag k="name"> and the ref is <tag k="ref"></text>
    #     This inserts the values of the named tags into the given text and 
    #     writes the result.
    # both are supported (through the substitute_text function)

    foreach($selected->members())
    {
        next if defined($layer) and $_->{'layer'} != $layer;
        if (ref $_ eq 'node')
        {
            my $text = substitute_text($textnode, $_);
            my $projected = project([$_->{'lat'}, $_->{'lon'}]);
            debug("draw node text '$text'") if ($debug->{"drawing"});
            $writer->startTag("text", 
                "x" => $projected->[0],
                "y" => $projected->[1],
                copy_attributes_not_in_list($textnode, 
                    [ "startOffset", "method", "spacing", 
                    "lengthAdjust","textLength" ]));
            $writer->characters($_->{'tags'}->{$textnode->getAttribute("k")});
            $writer->endTag("text");
        }
        elsif (ref $_ eq 'way')
        {
            draw_text_on_path($textnode, $_);
        }
    }
}

# -------------------------------------------------------------------
# sub draw_text_on_path($rulenode, $way)
#
# draws a text (usu. road name) onto an already defined path.
# Contains a very
# crude hack that tries to guess the way length and reduce the font
# size. This hack is present in Osmarender as well so we're compatible
# but it should really be replaced by something that does a proper
# calculation based on projected data and possibly font metrics, 
# rather than a crude approximation.
# 
# Parameters:
# $rulenode - the XML::XPath::Node object for the <text> instruction
#    in the rules file.
# $way - the way object on whose path the text should be drawn
#
# Return value:
# none.
# -------------------------------------------------------------------
sub draw_text_on_path
{
    my ($textnode, $way) = @_;

    my $text = substitute_text($textnode, $way);
    my $sumLon = 0;
    my $sumLat = 0;
    my $nodes = $way->{'nodes'};
    my $id = $way->{'id'};

    for (my $i=1; $i < scalar @$nodes; $i++)
    {
        $sumLat += abs($nodes->[$i]->{"lat"} - $nodes->[$i-1]->{"lat"});
        $sumLon += abs($nodes->[$i]->{"lon"} - $nodes->[$i-1]->{"lon"});
    }

    my $reverse = ($nodes->[scalar @$nodes - 1]->{"lon"} < $nodes->[0]->{"lon"});
    my $att = $textnode->getAttribute("textAttenuation") || '';
       $att = ($textAttenuation || '') if ($att eq '');
       $att = 99999999 if ($att eq '');

    my $pathLength = sqrt(($sumLon*1000*$att)**2 + 
       ($sumLat*1000*$att*$projection)**2);

    my $fontsize;
    my $textLength = length($text);
    return if ($textLength == 0);

    if ($pathLength > $textLength)
    {
        $fontsize = 100;
    }
    elsif ($pathLength > $textLength * .9)
    {
        $fontsize = 90;
    }
    elsif ($pathLength > $textLength * .8)
    {
        $fontsize = 80;
    }
    elsif ($pathLength > $textLength * .7)
    {
        $fontsize = 70;
    }

    if ($fontsize)
    {
        debug("draw text on path '$text'") if ($debug->{"drawing"});

        my $path = ($reverse) ?
            "#way_reverse_$id" : "#way_normal_$id";
        $writer->startTag("text", 
            copy_attributes_not_in_list($textnode, 
                [ "startOffset","method","spacing","lengthAdjust","textLength" ]));
        $writer->startTag("textPath",
            "xlink:href" => $path, 
            ($fontsize == 100) ? () : ("font-size", $fontsize."%"), 
            copy_attributes_in_list($textnode, 
                [ "font-size", "startOffset","method","spacing","lengthAdjust","textLength" ]));
        $writer->characters($text);
        $writer->endTag("textPath");
        $writer->endTag("text");
    }
    elsif ($debug->{"drawing"})
    {
        debug("do not draw text on path '$text' - no room");
    }
}

# -------------------------------------------------------------------
# sub substitute_text($rulenode, $object)
#
# returns the string to be drawn by the given text rule.
# 
# Supports simple text instructions that have no content and just
# a "k" attribute specifying the tag key whose value should be 
# printed, as well as the complex text instruction where the text
# instruction as abitrary fixed content interspresed with 
# "<tag k=.../>" elements that insert tag values in their place.
# 
# Parameters:
# $rulenode - the XML::XPath::Node object for the <text> instruction
#    in the rules file.
# $object - the object from which to read tag values
#
# Return value:
# the string to be drawn.
# -------------------------------------------------------------------
sub substitute_text
{
    my ($textnode, $object) = @_;
    my $text = '';
    my $k_attr = $textnode->getAttribute("k");

    if ($k_attr ne '')
    {
        # the simple case where the text is exactly one tag value
        $text = $object->{'tags'}{$k_attr} || '';
    }
    else
    {
        # need to examine the child nodes of the text node.
        foreach my $child($textnode->getChildNodes())
        {
            if ($child->getNodeType() == XML::XPath::Node::TEXT_NODE())
            {
                $text .= $child->string_value;
            }
            elsif ($child->getNodeType() == XML::XPath::Node::ELEMENT_NODE())
            {
                my $elname = $child->getName();
                if ($elname eq "tag")
                {
                    my $k = $child->getAttribute("k");
                    my $d = $child->getAttribute("default");
                    my $val;
                    if ($k =~ /^osm:(user|timestamp|id)$/)
                    {
                        $val = $object->{$1}
                    }
                    else
                    {
                        $val = $object->{'tags'}{$k};
                    }
                    $val = $d unless defined($val);
                    $text .= $val if defined($val);
                }
                else
                {
                    debug("ignoring <$elname> tag in text instruction");
                }
            }
            else
            {
                # error
                die "error parsing text instruction '".
                    $textnode->toString()."'";
            }
        }
    }
    return $text;
}
# -------------------------------------------------------------------
# sub draw_area_text($rulenode, $layer, $selection)
#
# for each selected object referenced by the $selection structure,
# draw the specified text inside the area.
#
# Parameters:
# $rulenode - the XML::XPath::Node object for the <areaText> instruction
#    in the rules file.
# $layer - if not undef, process only objects on this layer
# $selected - the list of currently selected objects
#
# Return value:
# none.
#
# Only ways are read from the selection; other objects are
# ignored.
# -------------------------------------------------------------------
sub draw_area_text
{
    my ($textnode, $layer, $selected) = @_;

    foreach($selected->members())
    {
        next if defined($layer) and $_->{'layer'} != $layer;
        next unless (ref $_ eq 'way');
        my $center = find_area_center($_);
        my $projected = project($center);
        $writer->startTag("text", 
            "x" => $projected->[0],
            "y" => $projected->[1],
            copy_attributes_not_in_list($textnode, 
                [ "startOffset", "method", "spacing", 
                "lengthAdjust","textLength" ]));
        $writer->characters($_->{'tags'}->{$textnode->getAttribute("k")});
        $writer->endTag("text");
    }
}

# -------------------------------------------------------------------
# sub draw_area_symbols($rulenode, $layer, $selection)
#
# for each selected object referenced by the $selection structure,
# draw a symbol inside the area.
#
# Parameters:
# $rulenode - the XML::XPath::Node object for the <areaSymbol> instruction
#    in the rules file.
# $layer - if not undef, process only objects on this layer
# $selected - the list of currently selected objects
#
# Return value:
# none.
#
# Only ways are read from the selection; other objects are
# ignored.
# -------------------------------------------------------------------
sub draw_area_symbols
{
    my ($symbolnode, $layer, $selected) = @_;

    foreach($selected->members())
    {
        next if defined($layer) and $_->{'layer'} != $layer;
        next unless (ref $_ eq 'way');
        my $center = find_area_center($_);
        my $projected = project($center);
        $writer->startTag("g", 
            "transform" => sprintf("translate(%f,%f) scale(%f)", 
                $projected->[0], $projected->[1], $symbolScale));
        my $ref = $symbolnode->getAttribute("ref");

        if ($ref ne "")
        {
            $writer->emptyTag("use", 
                "xlink:href" => "#symbol-".$ref,
                copy_attributes_not_in_list($symbolnode, [ "type", "ref", "scale", "smart-linecap" ]));
        }
        else
        {
            $writer->emptyTag("use", 
                copy_attributes_not_in_list($symbolnode, [ "type", "scale", "smart-linecap" ]));
        }
        $writer->endTag("g");
    }
}

# -------------------------------------------------------------------
# sub draw_circles($rulenode, $layer, $selection)
#
# for each selected object in $selection, draw a circle based 
# on the parameters specified by $rulenode.
#
# Parameters:
# $rulenode - the XML::XPath::Node object for the <circle> instruction
#    in the rules file.
# $layer - if not undef, process only objects on this layer
# $selection - Set::Object that contains selected objects
#
# Return value:
# none.
#
# Only nodes are read from the selection; other objects are
# ignored.
# -------------------------------------------------------------------
sub draw_circles
{
    my ($circlenode, $layer, $selected) = @_;

    foreach($selected->members())
    {
        next if (ref $_ eq 'way');
        next if defined($layer) and $_->{'layer'} != $layer;
        
        my $projected = project([$_->{'lat'}, $_->{'lon'}]);
        $writer->emptyTag('circle', 
            'cx' => $projected->[0], 
            'cy' => $projected->[1], 
            copy_attributes_not_in_list($circlenode, [ 'type', 'ref', 'scale', 'smart-linecap', 'cx', 'cy' ]));
    }
}

# -------------------------------------------------------------------
# sub draw_symbols($rulenode, $layer, $selection)
#
# for each selected object in $selection, draw a symbol based 
# on the parameters specified by $rulenode.
#
# Parameters:
# $rulenode - the XML::XPath::Node object for the <symbol> instruction
#    in the rules file.
# $layer - if not undef, process only objects on this layer
# $selection - Set::Object that contains selected objects
#
# Return value:
# none.
#
# Only nodes are read from the selection; other objects are
# ignored.
# -------------------------------------------------------------------
sub draw_symbols
{
    my ($symbolnode, $layer, $selected) = @_;

    foreach($selected->members())
    {
        next if (ref $_ eq 'way');
        next if defined($layer) and $_->{'layer'} != $layer;
        my $projected = project([$_->{'lat'}, $_->{'lon'}]);
        $writer->startTag("g", 
            "transform" => sprintf("translate(%f,%f) scale(%f)", 
                $projected->[0], $projected->[1], $symbolScale));
        my $ref = $symbolnode->getAttribute("ref");

        if ($ref ne "")
        {
            $writer->emptyTag("use", 
                "xlink:href" => "#symbol-".$ref,
                copy_attributes_not_in_list($symbolnode, 
                    [ "type", "ref", "scale", "smart-linecap" ]));
        }
        else
        {
            $writer->emptyTag("use", 
                copy_attributes_not_in_list($symbolnode, 
                    [ "type", "scale", "smart-linecap" ]));
        }
        $writer->endTag("g");
    }
}

# -------------------------------------------------------------------
# sub draw_way_markers($rulenode, $layer, $selection)
#
# for each selected object referenced by the $selection structure,
# draw a way marker based on the parameters specified by 
# $rulenode.
#
# From osmarender.xsl:
# "Draws a marker on a node that is perpendicular to a way that 
# passes through the node. If more than one way passes through the 
# node then the result is a bit unspecified."
#
# This implementation currently only looks at the first way using
# the specified node.
#
# Parameters:
# $rulenode - the XML::XPath::Node object for the <wayMarker> instruction
#    in the rules file.
# $layer - if not undef, process only objects on this layer
# $selected - the list of currently selected objects
#
# Return value:
# none.
#
# Only nodes are read from the selection; other objects are
# ignored.
# -------------------------------------------------------------------
sub draw_way_markers
{
    my ($markernode, $layer, $selected) = @_;

    foreach($selected->members())
    {
        next if (ref $_ eq 'way');
        next if defined($layer) and $_->{'layer'} != $layer;

        # find the (first) way using this node and use it to determine
        # previous and next nodes
        my $way = $_->{"ways"}->[0];
        next unless defined($way);
        my $previous;
        my $next;
        for (my $i=0; $i<scalar(@{$way->{'nodes'}}); $i++)
        {
            if ($way->{'nodes'}->[$i] eq $_)
            {
                # note: perl arrays are not bounds checked, undef is
                # returned for accesses outside bounds.
                $next = $way->{'nodes'}->[$i+1];
                $previous = $way->{'nodes'}->[$i-1];
                last;
            }
        }

        # build a three-element path from the nodes. osmarender always
        # makes sure there are three elements and substitutes the current
        # node if it has no previous or next node.
        my @nodes;
        push(@nodes, $previous || $_);
        push(@nodes, $_);
        push(@nodes, $next || $_);
        # make_path expects lat/lon array pairs not full nodes
        my $path = make_path(map { [$_->{'lat'}, $_->{'lon'}] } @nodes);

        $writer->startTag("g", 
            copy_attributes_not_in_list($markernode, []));
        $writer->emptyTag("path", 
            id => "nodePath_".$_->{"id"},
            d => $path);
        $writer->emptyTag("use", 
            "xlink:href" => "#nodePath_".$_->{"id"},
            copy_attributes_not_in_list($markernode, []));
        $writer->endTag("g");
    }
}

# -------------------------------------------------------------------
# sub draw_path($rulenode, $path_id, $class)
#
# draws an SVG path with the given path reference and style.
# -------------------------------------------------------------------
sub draw_path
{
    my ($rulenode, $path_id, $addclass) = @_;

    my $mask_class = $rulenode->getAttribute("mask-class");
    my $class = $rulenode->getAttribute("class");
    my $extra_attr = [];
    if ($mask_class ne "")
    {
        my $mask_id = "mask_".$path_id;
        $writer->startTag("mask", 
            "id" => $mask_id, 
            "maskUnits" => "userSpaceOnUse");
        $writer->emptyTag("use", 
            "xlink:href" => "#$path_id",
            "class" => "$mask_class osmarender-mask-black");

        # the following two seem to be required as a workaround for 
        # an inkscape bug.
        $writer->emptyTag("use", 
            "xlink:href" => "#$path_id",
            "class" => "$class osmarender-mask-white");
        $writer->emptyTag("use", 
            "xlink:href" => "#$path_id",
            "class" => "$mask_class osmarender-mask-black");

        $writer->endTag("mask");
        $extra_attr = [ "mask" => "url(#".$mask_id.")" ];
    }

    $writer->emptyTag("use", 
        "xlink:href" => "#$path_id",
        @$extra_attr,
        "class" => "$class $addclass");
}

1;
