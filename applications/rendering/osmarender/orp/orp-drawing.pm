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

our ($writer, $project, $projection, $symbolScale, $textAttenuation, $debug, $meter2pixel, $text_index, %symbols, $labelRelations);


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
    my $honor_width = ($linenode->getAttribute("honor-width") eq "yes");

    # explicit way specific style
    my $style="";

    foreach (@$selected)
    {
        next unless (ref $_ eq 'way'); # Draw lines doesn't care about multipolygons
        next if (scalar(@{$_->{"nodes"}}) < 2);

    # this is a special case for ways (e.g. rivers) where we honor a
    # width=something tag.
    # It is used to generate rivers of different width, depending on the
    # value of the width tag.
    # This is done by an explicit specification of a
    # style="stroke-width:..px" tag in the generated SVG output
    $style="";
    if ($honor_width) {
      if (defined($_->{"tags"}->{"width"})) {
        my $maxwidth = $linenode->getAttribute("maximum-width");
        if ($maxwidth eq "") {$maxwidth = 100}
        my $minwidth = $linenode->getAttribute("minimum-width");
        if ($minwidth eq "") {$minwidth = 0.1}
        my $scale = $linenode->getAttribute("width-scale-factor");
        if ($scale eq "") {$scale = 1}

        my $width = $_->{"tags"}->{"width"};
        # get rid of the meter unit
        $width =~ s/m$//;
        # some stupid german people use a komma as decimal separator
        $width =~ s/,/\./;
        
        my $w;
        # make sure, that width is a numeric value
        { no warnings; $w = $meter2pixel*$width if 0+$width;}

        if (defined($w)) {
          # make sure that width is inside the desired range
          my $maxw = $meter2pixel*$maxwidth;
          my $minw = $meter2pixel*$minwidth;
          if ($w > $maxw) {$w = $maxw;}
          if ($w < $minw) {$w = $minw;}
          $w *= $scale;
          $style = "stroke-width:${w}px";
        }
      }
    }

        $writer->startTag("g", 
            "class" => $class,
            ($mask_class ne "") ? ("mask-class", $mask_class) : ()) unless $group_started;
        $group_started = 1;
        if ($smart_linecaps)
        {
            draw_way_with_smart_linecaps($linenode, $layer, $_, $class, $style);
        }
        else
        {
            draw_path($linenode, $_->{"id"}, 'normal', $style);
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
    my ($linenode, $layer, $way, $class, $style) = @_;

    return if (ref $way eq 'multipolygon');

    # convenience variables
    my $id = $way->{"id"};
    my $nodes = $way->{"nodes"};

    return unless(scalar(@$nodes));

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

    my $extraClassFirst = '';
    my $extraClassLast = '';

    if ($first_node_connection_count != 1)
    {
        if ($first_node_lower_layer_connection_count > 0)
        {
            $extraClassFirst = 'osmarender-stroke-linecap-butt';
        }
        else
        {
            $extraClassFirst = 'osmarender-stroke-linecap-round'
        }
    }

    if ($last_node_connection_count != 1)
    {
        if ($last_node_lower_layer_connection_count > 0)
        {
            $extraClassLast = 'osmarender-stroke-linecap-butt';
        }
        else
        {
            $extraClassLast = 'osmarender-stroke-linecap-round';
        }
    }

    # If first and last is the same, draw only one way. Else divide way into way_start, way_mid and way_last
    if ($extraClassFirst eq $extraClassLast)
    {
        draw_path($linenode, $id, 'normal', $extraClassFirst, $style);
    } 
    else 
    {
        # first draw middle segment if we have more than 2 nodes
        draw_path($linenode, $id, 'mid', 
            "osmarender-stroke-linecap-butt osmarender-no-marker-start osmarender-no-marker-end", $style) 
            if (scalar(@$nodes)>2);
        draw_path($linenode, $id, 'start',
            "$extraClassFirst osmarender-no-marker-end", $style);
        draw_path($linenode, $id, 'end', 
            "$extraClassLast osmarender-no-marker-start", $style);
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
    foreach (@$selected)
    {
        next unless (ref $_ eq 'way' or ref $_ eq 'multipolygon');
        # Skip ways that are already rendered
        # because they are part of a multipolygon
        next if (ref $_ eq 'way' and defined $_->{"multipolygon"});

        my $ways;
        
        if (ref $_ eq 'way') {
            $ways = [$_];
        }
        if (ref $_ eq 'multipolygon') {
            $ways = [@{$_->{"outer"}}, @{$_->{"inner"}}];
        }
        
        my $path = '';
        foreach my $way (@$ways) {
            my $points = [];
            foreach (@{$way->{"nodes"}})
            {
                push(@$points, [ $_->{"lat"}, $_->{"lon"} ]) if (defined($_->{"lat"}) && defined($_->{"lon"}));
            }
            $path .= make_path(@$points)."Z ";
        }

        $writer->emptyTag("path", "d" => $path, "style" => "fill-rule:evenodd");
    }
    $writer->endTag("g");
}


# sub render_text($textnode, $text, $coordinates)
#
# render text at specified position
#
# Parameters:
# $textnode - the XML::XPath::Node object for the <text> instruction
# $text - caption text
# $coordinates - text position coordinates
sub render_text
{
    my ($textnode, $text, $coordinates) = @_;

    my $projected = $project->($coordinates);
    $writer->startTag("text", 
        "x" => $projected->[0],
        "y" => $projected->[1],
        copy_attributes_not_in_list($textnode, 
            [ "startOffset", "method", "spacing", 
              "lengthAdjust","textLength" ]));
    $writer->characters($text);
    $writer->endTag("text");
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

    foreach(@$selected)
    {
        my $text = substitute_text($textnode, $_);
        if ($text ne '')
        {
            # This function only works on pathes
            next if (ref $_ eq 'multipolygon');

            if (ref $_ eq 'node')
            {
                render_text($textnode, $text, [$_->{'lat'}, $_->{'lon'}]);
            }
            elsif (ref $_ eq 'way')
            {
                draw_text_on_path($textnode, $_, $text);
            }
            else
            {
                debug("Unhandled type in draw_text: ".ref($_)) if ($debug->{"drawing"});
            }
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
    my ($textnode, $way, $text) = @_;

    my $nodes = $way->{'nodes'};
    my $bucket;
    my $threshold = 500;

    if ($textnode->getAttribute("avoid-duplicates") =~ /^1|yes|true$/)
    {
        my $bucket1 = (int($nodes->[0]->{'lat'}*2)+180) * 720 + int($nodes->[0]->{'lon'}*2) + 360;
        my $bucket2 = (int($nodes->[scalar @$nodes -1]->{'lat'}*2)+180) * 720 + int($nodes->[scalar @$nodes -1]->{'lon'}*2) + 360;
        $bucket = ($bucket1 < $bucket2) ? $bucket1 : $bucket2;
        debug ("place '$text' in bucket $bucket") if ($debug->{"drawing"});
        foreach my $label (@{$text_index->{$bucket}})
        {
            if ($text eq $label->{"text"})
            {
                my $d1 = distance($nodes->[0], $label->{'n0'});
                my $d2 = distance($nodes->[scalar @$nodes -1], $label->{'n1'});
                debug ("   distance to other: $d1 $d2") if ($debug->{"drawing"});
                if ($d1<$threshold && $d2<$threshold)
                {
                    debug("ignore '$text'") if ($debug->{"drawing"});
                    return;
                }
                # same check for reversed way
                $d1 = distance($nodes->[0], $label->{'n1'});
                $d2 = distance($nodes->[scalar @$nodes -1], $label->{'n0'});
                debug ("   distance to other: $d1 $d2") if ($debug->{"drawing"});
                if ($d1<$threshold && $d2<$threshold)
                {
                    debug("ignore '$text'") if ($debug->{"drawing"});
                    return;
                }
            }
        }
    }
    my $sumLon = 0;
    my $sumLat = 0;
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

    my $overflow = ($textnode->getAttribute("overflow") =~ /^1|yes|true$/);

    my $pathLength = sqrt(($sumLon*1000*$att)**2 + 
       ($sumLat*1000*$att*$projection)**2);

    $pathLength *= 3 if ($overflow);

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
        push(@{$text_index->{$bucket}}, 
            { 'text' => $text, 
              'n0' => $nodes->[0], 
              'n1' => $nodes->[scalar @$nodes -1] }) if defined($bucket);
        my $extended = ($overflow) ? "_extended" : "";
        my $path = get_way_href($id,  ($reverse) ? 'reverse'.$extended : 'normal'.$extended);
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
                    debug("ignoring <$elname> tag in text instruction") if ($debug->{"drawing"});
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

    foreach(@$selected)
    {
        my $text = substitute_text($textnode, $_);
        next unless $text ne '';

        # Skip ways that are already rendered
        # because they are part of a multipolygon
        next if (ref $_ eq 'way' and defined $_->{"multipolygon"});

        if (ref $_ eq 'way' or ref $_ eq 'multipolygon')
        {
            #Area
            my $labelRelation = $labelRelations->{$_->{'id'}};
            if (defined($labelRelation))
            {
                # Draw text at users specifed position
                foreach my $ref (@{$labelRelation})
                {
                    render_text($textnode, $text, [$ref->{'lat'}, $ref->{'lon'}]);
                }
            }
            else
            {
                # Draw text at area center
                my $center = get_area_center($_);
                render_text($textnode, $text, $center);
            }
        }
        elsif (ref $_ eq 'node')
        {
            #Node
            render_text($textnode, $text, [$_->{'lat'}, $_->{'lon'}]);
        }
        else
        {
            debug("Unhandled type in draw_area_text: ".ref($_)) if ($debug->{"drawing"});
        }
    }
}

sub get_area_center
{
    my ($area) = @_;
    my $lat = $area->{"tags"}->{"osmarender:areaCenterLat"};
    my $lon = $area->{"tags"}->{"osmarender:areaCenterLon"};
    if (defined($lat) && defined($lon))
    {
        return [$lat, $lon];
    } else 
    {
        return find_area_center($area);
    }
}

# -------------------------------------------------------------------
# sub draw_symbols($rulenode, $layer, $selection)
#
# for each selected object referenced by the $selection structure,
# draw a symbol inside the area or at the node.
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
sub draw_symbols
{
    my ($symbolnode, $layer, $selected) = @_;
    foreach(@$selected)
    {
        # Skip ways that are already rendered
        # because they are part of a multipolygon
        next if (ref $_ eq 'way' and defined $_->{"multipolygon"});
        
        if (ref $_ eq 'way' or ref $_ eq 'multipolygon')
        {
            #Area
            my $labelRelation = $labelRelations->{$_->{'id'}};
            if (defined($labelRelation))
            {
                foreach my $ref (@{$labelRelation})
                {
                    draw_symbol($symbolnode, project[$ref->{'lat'}, $ref->{'lon'}]);
                }
            }
            else
            {
                # Draw icon at area center
                my $center = get_area_center($_);
                my $projected = $project->($center);
                draw_symbol($symbolnode, $projected);
            }
        }
        elsif (ref $_ eq 'node')
        {
            #Node
            my $projected = $project->([$_->{'lat'}, $_->{'lon'}]);
            draw_symbol($symbolnode, $projected);
        }
        else
        {
            debug("Unhandled type in draw_symbols: ".ref($_)) if ($debug->{"drawing"});
        }
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
    foreach(@$selected)
    {
        # Skip ways that are already rendered
        # because they are part of a multipolygon
        next if (ref $_ eq 'way' and defined $_->{"multipolygon"});
        
        if (ref $_ eq 'way' or ref $_ eq 'multipolygon')
        {
            #Area
            my $labelRelation = $labelRelations->{$_->{'id'}};
            if (defined($labelRelation))
            {
                foreach my $ref (@{$labelRelation})
                {
                    my $projected = $project->([$ref->{'lat'}, $ref->{'lon'}]);
                    $writer->emptyTag('circle',
                        'cx' => $projected->[0],
                        'cy' => $projected->[1],
                        copy_attributes_not_in_list($circlenode,
                            [ 'type', 'ref', 'scale', 'smart-linecap', 'cx', 'cy' ]));
                }
            }
            else
            {
                # Draw icon at area center
                my $center = get_area_center($_);
                my $projected = $project->($center);
                $writer->emptyTag('circle',
                    'cx' => $projected->[0],
                    'cy' => $projected->[1],
                    copy_attributes_not_in_list($circlenode,
                        [ 'type', 'ref', 'scale', 'smart-linecap', 'cx', 'cy' ]));
            }
        }
        elsif (ref $_ eq 'node')
        {
            #Node
            my $projected = $project->([$_->{'lat'}, $_->{'lon'}]);
            $writer->emptyTag('circle',
                'cx' => $projected->[0],
                'cy' => $projected->[1],
                copy_attributes_not_in_list($circlenode, [ 'type', 'ref', 'scale', 'smart-linecap', 'cx', 'cy' ]));
        }
        else
        {
            debug("Unhandled type in draw_circles: ".ref($_)) if ($debug->{"drawing"});
        }
    }
}

# -------------------------------------------------------------------
# sub draw_symbol($node, $coordinates)
#
# Draw one symbol
#
# Parameters:
# symbolNode - the XML::XPath::Node object for <use> instruction
# coordinates - array containing symbol coordinates
# -------------------------------------------------------------------
sub draw_symbol
{
    (my $symbolnode, my $coordinates) = @_;
    my $ref = $symbolnode->getAttribute("ref");

    if ($ref ne "")
    {
        my $id = 'symbol-'.$ref;
        my $width = $symbols{$id}->{'width'};
        my $height = $symbols{$id}->{'height'};
        $width = $symbolnode->getAttribute('width') unless $symbolnode->getAttribute('width') eq '';
        $height = $symbolnode->getAttribute('height') unless $symbolnode->getAttribute('height') eq '';

        my $shift = "";

        if ($symbolnode->getAttribute('position') eq 'center')
        {
            $shift = sprintf("translate(%f,%f)", - $width / 2, - $height / 2);
        }

        $writer->startTag("g", 
            "transform" => sprintf("translate(%f,%f) scale(%f) %s %s", 
                $coordinates->[0], $coordinates->[1], $symbolScale, $symbolnode->getAttribute("transform"), $shift));

        my %copiedAttributes = copy_attributes_not_in_list($symbolnode, 
                [ "type", "ref", "scale", "smart-linecap", 'position', 'transform' ]);
        my %attributes = ((
            "xlink:href" => "#$id",
            "width" => $width,
            "height" => $height,
        ), %copiedAttributes);
        $writer->emptyTag("use", %attributes);

        $writer->endTag("g");
    }
    else
    {
        $writer->startTag("g", 
            "transform" => sprintf("translate(%f,%f) scale(%f)", 
                $coordinates->[0], $coordinates->[1], $symbolScale));
        $writer->emptyTag("use", 
            copy_attributes_not_in_list($symbolnode, 
                [ "type", "scale", "smart-linecap" ]));
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
# the specified node that matches the given k= and v= attributes.
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

    foreach(@$selected)
    {
        next if (ref $_ eq 'way');

        # find the (first) way using this node and matching one of the keys
        # in k and one of the values in v (if set) and use it to determine
        # previous and next nodes. Multiple keys and values can be specified
        # separated by | chars. Earlier keys are more important then keys
        # later in the string. Within the same key earlier values are more
        # important. If two ways match the same key and value the result
        # is withever way is read first and therefore a bit random.
        my $way = undef;

        my @ka = split /\|/,$markernode->getAttribute('k');
        my @va = split /\|/,$markernode->getAttribute('v');
        # if no value is specified we assume that the user doesn't care and
        # only the key is important.
        push(@va, "*") if (scalar(@va) == 0);

        debug("looking for @ka and @va") if $debug->{'selectors'};

        WAYSEARCH: foreach my $k (@ka) {
            foreach my $v (@va) {
                foreach my $w (@{$_->{'ways'}}) {
                    next unless defined($w->{'tags'}->{$k});
                    # a * matches any value (like osmarender rules, NOT like perl regexp)
                    next unless ($v eq "*" or $w->{'tags'}{$k} eq $v);
                    debug("$w->{'id'} matches $k and $v") if $debug->{'selectors'};
                    $way = $w;
                    last WAYSEARCH;
                }
            }
        }

        next unless defined($way);
        my $previous;
        my $next;
        for (my $i=0; $i<scalar(@{$way->{'nodes'}}); $i++)
        {
            if ($way->{'nodes'}->[$i] eq $_)
            {
                $next = $way->{'nodes'}->[$i+1] 
                    if ($i<scalar(@{$way->{'nodes'}})-1);
                $previous = $way->{'nodes'}->[$i-1] 
                    if ($i>0);
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

        $writer->emptyTag("path", 
            d => $path,
            copy_attributes_not_in_list($markernode, []));
    }
}

# -------------------------------------------------------------------
# sub draw_path($rulenode, $path_id, $class, $style)
#
# draws an SVG path with the given path reference and style.
# -------------------------------------------------------------------
sub draw_path
{
    my ($rulenode, $way_id, $way_type, $addclass, $style) = @_;

    my $mask_class = $rulenode->getAttribute("mask-class");
    my $class = $rulenode->getAttribute("class");
    my $extra_attr = [];

    my $path_id = get_way_href($way_id, $way_type);
    if ($mask_class ne "")
    {
        my $mask_id = 'mask_'.$way_type.'_'.$way_id;
        $writer->startTag("mask", 
            "id" => $mask_id, 
            "maskUnits" => "userSpaceOnUse");
        $writer->emptyTag("use", 
            "xlink:href" => $path_id,
            "class" => "$mask_class osmarender-mask-black");

        # the following two seem to be required as a workaround for 
        # an inkscape bug.
        $writer->emptyTag("use", 
            "xlink:href" => $path_id,
            "class" => "$class osmarender-mask-white");
        $writer->emptyTag("use", 
            "xlink:href" => "$path_id",
            "class" => "$mask_class osmarender-mask-black");

        $writer->endTag("mask");
        $extra_attr = [ "mask" => "url(#".$mask_id.")" ];
    }
    if (defined($style) and $style ne "") {
      $writer->emptyTag("use", 
            "xlink:href" => "$path_id", "style" => "$style",
            @$extra_attr,
            "class" => defined($addclass) ? "$class $addclass" : $class);
    } else {
      $writer->emptyTag("use", 
            "xlink:href" => "$path_id",
            @$extra_attr,
            "class" => defined($addclass) ? "$class $addclass" : $class);
    }
}

1;
