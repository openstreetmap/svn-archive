#!/usr/bin/perl -w

# or/p - Osmarender in Perl
# -------------------------
#
# Main Program
#
# This is a re-implementation of Osmarender in Perl.
#
# Usage:
# perl orp.pl -r rule.xml data.osm
#
# creates a file named data.svg
#
# BUGS AND LIMITATIONS
# --------------------
#
# Known bugs:
# - something seems to be wrong with my implementation of bobkare's area
#   center algorithm; it works "mostly" but sometimes it is a bit off. It
#   doesn't support relations (polygons with holes) yet but even for those
#   without holes it is not always right. I've switched to the old primitive
#   "center of bbox" algorithm for the time being.
#
# Osmarender features not yet supported:
# - "s" attribute on rules is unsupported in some esoteric cases
#
# Possible optimisations:
# - generate more concise SVG output by naming things differently
#   (not way_reverse_45363 but wr_123; possibly also renumber them
#   1..n)
# - include lines2curves
# - simplify paths that have lots of nodes (specify something like
#   an "output dpi" and then just round every position to the nearest
#   possible output position - this will ultimately allow us to render
#   complex level-12 or even larger tiles
# - pre-process rules file to determine what needs to be read and 
#   ignore other data on input
# - process multiple rules files into multiple out files (saves parsing
#   time)
# - use Proj.4 projection (will break compatibility)
# - do proper clipping, i.e. suppress generating SVG instructions for stuff
#   that is invisible anyway, thus enabling us to use one big OSM file and
#   make several SVG "tiles" from it
# - loads more
#
# Stuff supported by or/p but not by Osmarender/XSLT:
# - gridSpacing variable (default 1000, grid spacing in metres)
#
# DATA STRUCTURE
# --------------
#
# NODES:
#   * represented as hashes with keys "id", "lat", "lon", "tags",
#     "layer", and "ways"
#     where "tags" is a hash ref and "ways" is an array ref,
#     containing way references (not way ids)
#   * stored in global hash $node_storage (key: node id)
# WAYS:
#   * represented as hashes with "id", "tags", "nodes"
#     where "tags" is a hash ref and "nodes" is an array ref, 
#     containing node references (not node ids)
#   * stored in global hash $way_storage (key: way id)
# RELATIONS:
#   * represented as hashes with "id", "tags", "members"
#     where "tags" is a hash ref and "members" is an array ref, 
#     each element again an array reference with two elements
#     (role and object reference)
#   * stored in global hash $relation_storage (key: relation id)
#
# INDEXES:
#   * $index_node_keys is a hash with one key for each tag key
#     present in nodes, the value is an array of node references
#   * $index_way_keys the same for way tag keys
#   * each object has a "relations" hash element whose value is
#     an array of ($role,$relation_ref) pairs
#
# SELECTION LISTS:
#   * $selection is an array that contains references to 
#     all currently selected elements on various levels of rule
#     recursion.
#     element #0 has pointers to ALL elements.
#     element #1 has pointers to all elements selected by the
#       top-most rule on the current recursion stack.
#     element #2 has pointers to the subset of #1 selected
#       by the second rule on the current recursion stack
#     etc.
#   * each element in $selection is a Set::Object because
#     we want the selection lists to be unique.
#
#
# LICENSE
# -------
#
# Written by Frederik Ramm <frederik@remote.org>, as a complete re-write
# of osmarender.xsl.
#
# osmarender.xsl is Copyright (C) 2006-2007  Etienne Cherdlu, Jochen Topf
# and released under GPL v2 or later.
#
# This program does not contain code from the original osmarender.xsl 
# but since the logic has been copied from Osmarender, it is safe to 
# assume that this triggers the viral element of the GPL, making orp.pl
# GPL v2 or later also. (It would have been Public Domain otherwise.)
#
# -----------------------------------------------------------------------------
use strict;
use warnings;
use bytes;

use XML::Parser::PerlSAX ();
use XML::XPath ();
use XML::XPath::XMLParser ();
use Math::Trig qw(great_circle_distance deg2rad pi);
use Set::Object ();
use Getopt::Long qw(GetOptions);
use XML::Writer ();
use IO::File ();
use FindBin qw($Bin);
use lib $Bin;
use SAXOsmHandler ();
use Math::Trig;

require "orp-select.pm";
require "orp-drawing.pm";

# available debug flags:
our $debug = { 
    "general" => 0,  # general status messages
    "rules" => 0,    # print all rules and how many matches
    "indexes" => 0,  # print messages about the use of indexes
    "drawing" => 0,  # print out all drawing instructions executed
    "multipolygon" => 0, # print debugging messages for multipolygons
    "selectors" => 0, # print debug messages from selectors
};

our $node_storage = {};
our $way_storage = {};
our $relation_storage = {};
my %data_bounds = ();
our $text_index = {};
our $meter2pixel = {};
our %symbols = ();
our $labelRelations = {};

my $handler = SAXOsmHandler->new($node_storage, $way_storage, $relation_storage, \%data_bounds);
my $parser = XML::Parser::PerlSAX->new(Handler => $handler);
my $rule_file = "rule.xml";
my $debug_opts = '';
my $output_file;
my %opt_bounds;
my $bbox;
my %referenced_ways;

# List of drawing commands which will make the map
# Represented as an array. Array item is hash with members:
# instruction
# array of elements
my $drawing_commands;

# Informations about drawing instructions. It will contain default layer and maybe some
# other info in future.
my %instructions = (
  'line' => {'func' => \&draw_lines},
  'area' => {'func' => \&draw_areas},
  'circle' => {'func' => \&draw_circles},
  'symbol' => {'func' => \&draw_symbols},
  'wayMarker' => {'func' => \&draw_way_markers},
  'caption' => {'func' => \&draw_area_text},
  'pathText' => {'func' => \&draw_text},
  # The following names are kept for compatibility
  'text' => {'func' => \&draw_text},
  'areaText' => {'func' => \&draw_area_text},
  'areaSymbol' => {'func' => \&draw_symbols});

GetOptions("rule=s"    => \$rule_file, 
           "debug=s"   => \$debug_opts,
           "outfile=s" => \$output_file,
           "minlat=f"  => \$opt_bounds{minlat},
           "minlon=f"  => \$opt_bounds{minlon},
           "maxlat=f"  => \$opt_bounds{maxlat},
           "maxlon=f"  => \$opt_bounds{maxlon},
           "bbox=s"    => \$bbox) or usage('');

for my $key(split(/,/, $debug_opts))
{
    if (!defined($debug->{"$key"}))
    {
        usage("unknown debug option '$key'");
    }
    $debug->{$key} = 1;
}

if ($bbox)
{
    usage("Error: Both bbox and one of minlat/minlon/maxlat/maxlon given")
        if @opt_bounds{qw(minlat minlon maxlat maxlon)};
    ($opt_bounds{minlat}, $opt_bounds{minlon}, $opt_bounds{maxlat}, $opt_bounds{maxlon}) = split(/,/, $bbox);
}

my $rules = XML::XPath->new(filename => $rule_file); 
my $data = get_variable("data", "");

# if data file given in rule file, prepend rule file's path
if ($rule_file =~ m!(.*[/\\])(.*)! && defined($data))
{
    $data = $1.$data;
}

usage ("data file must be specified in rule or on command line")
    if (($data eq "") && (scalar(@ARGV) == 0));

my $input_file = (defined $ARGV[0]) ? $ARGV[0] : $data;

if (!defined($output_file))
{
    if ($input_file =~ /^(.*)\.osm$/)
    {
        $output_file = $1.".svg";
    }
    else
    {
        $output_file = "output.svg";
    }
}

our $index_node_tags = {};
our $index_way_tags = {};

# parse the OSM input file and store data in $node_storage,
# $way_storage, $relation_storage.
my %parser_args = (Source => {SystemId => $input_file});
$parser->parse(%parser_args);

# initialise the "ways" element of every node with the list of
# ways it belongs to (creating a back reference)
# FIXME: it's not entirely clear if this should also be done for
# multipolygon objects. I think it should be done, but it needs
# further checking.
foreach (values(%$way_storage))
{
    foreach my $node(@{$_->{"nodes"}})
    {
        push(@{$node->{"ways"}}, $_);
    }
}

# initialise the relation member lists (after parsing, these only
# contain symbolic references of the form "way:1234" instead of
# proper perl references - this is because relations may refer 
# to other relations that haven't been read yet); also add relations 
# to the "relations" element of every member (creating a back 
# reference)
foreach (values(%$relation_storage))
{
    foreach my $member(@{$_->{"members"}})
    {
        my ($type, $id) = split(/:/, $member->[1]);
        my $deref = 
            ($type eq 'node') ? $node_storage->{$id} : 
            ($type eq 'way') ? $way_storage->{$id} : 
            ($type eq 'relation') ? $relation_storage->{$id} : 
            undef;
        $member->[1] = $deref;

        if (defined($deref))
        {
            push(@{$deref->{'relations'}}, [ $member->[0], $_ ]);
        }
    }
}

my $multipolygon_wayid = 0;

sub assemble_closed_ways
{
    my ($inputways, $relation) = @_;
    return [] if not defined $inputways;
    my $outputways = [];
    while (@$inputways > 0)
    {
        # Start with the first item in the list
        my $way = shift @$inputways;
        my $nodes = [@{$way->{'nodes'}}];
        my $tags = {};
        my $relations = [@{$way->{'relations'}}]; # TODO: Make sure no duplicate entries are present
        while (my ($key, $val) = each(%{$way->{'tags'}}))
        {
            $tags->{$key}=$val;
        }
        $multipolygon_wayid += 1;
        my $wayobj = {
            'layer' => $way->{'layer'},
            'timestamp' => $way->{'timestamp'},
            'user' => $way->{'user'},
            'nodes' => $nodes,
            'relations' => $relations,
            'id' => "multipolygon$multipolygon_wayid",
            'tags' => $tags,
        };
        bless($wayobj, 'way');

        # $found stores information if new node where found in
        # the last iteration though the nodelist
        # if no new nodes are found but the list is still not
        # empty there are 2 or more disjunct areas
        # 1 = nodes were found in the last iteration or this 
        #       is the first iteration
        # 0 = no nodes were found, start a new way
        my $found = 1;
        while (@$inputways > 0 && $found)
        {
            $found = 0;
            foreach my $index (0 .. $#{$inputways})
            {
                my $nodelist = @$inputways[$index]->{'nodes'};
                my @sorted;
                # Check if the way's direction is reversed
                if (defined $nodes->[-1] && defined $nodelist->[-1] && 
                    ($nodes->[-1] eq $nodelist->[-1]))
                {
                    @sorted = reverse @$nodelist;
                }
                else
                {
                    @sorted = @$nodelist;
                }

                # Check if the way matches
                if (defined $nodes->[-1] && defined $sorted[0] && 
                    ($nodes->[-1] eq $sorted[0]))
                {
                    # Add way segement
                    $found = 1;
                    # Add tags to taglist
                    while (my ($key, $val) = each(%{@$inputways[$index]->{'tags'}}))
                    {
                        $tags->{$key}=$val;
                    }
                    push(@$relations, @{@$inputways[$index]->{'relations'}});
                    # Remove first node which is identical to
                    # the last node of the old way
                    shift @sorted;
                    push(@$nodes, @sorted);
                    # Remove segment from the list of available segements
                    splice(@$inputways, $index, 1);
                    last;
                }
            }
        }
        push(@$outputways, $wayobj);
        debug("Unclosed way in multipolygon $relation->{'id'}") if (defined @$nodes[0]
            and @$nodes[0] ne @$nodes[-1] and $debug->{'multipolygon'});
    }
    return $outputways;
}

# returns true if the first has a subset of the second object's tags,
# with some tags being ignored
sub tags_subset
{
    my ($first, $second) = @_;
    foreach my $tag(keys %{$first->{"tags"}})
    {
        next if ($tag =~ /^(name|created_by|note|layer|osmarender:areaCenterLat|osmarender:areaCenterLon|osmarender:areaSize)$/);
        return 0 unless defined($second->{'tags'}{$tag}) && $first->{'tags'}{$tag} eq $second->{'tags'}{$tag};
    }
    return 1;
}


# return true if the relation has no keys that affect the rendering
# TODO: add more keys that didn't affect the rendering
sub multipolygon_has_tags
{
    my ($multipolygon) = @_;
    foreach my $key(keys %{$multipolygon->{'tags'}})
    {
        if ($key !~ m/^(type|created_by|fixme|name.*|note.*|source.*|osmarender.*)$/)
        {
            return 1;
        }
    }
    return 0;
}


#Multipolygon:
# "outer": [way1, way2, ...]
#   Combined to form full ways
# "inner": [way1, way2, ...]
#   Combined to form full ways, for each inner way a new way-object with
#   the sum of all tags is created and added to $way_storage
# "tags":
#   The sum of all tags, from both the relation and all the outer ways.
# The original ways get a special tag, so they aren't processed anymore
# in function that are able to handle multipolygons.
#
# In theory each way could be represented as a multipolygon with the
# following properties:
# - One outer way
# - No inner ways
# - Tags from the original way
#
# This might be a good future enhancement but requires big changes to
# existing code.
#
# TODO:
# - Label relation


foreach my $rel (values(%$relation_storage))
{
    next unless defined $rel->{'tags'}->{'type'} and $rel->{'tags'}->{'type'} eq 'multipolygon';
    my $outerways = ();
    my $innerways = ();
    $multipolygon_wayid += 1;
    my $multipolygon = {
        'multipolgyon_relation' => $rel,
        'relations' => [],
        'tags' => {},
        'id' => "multipolygon$multipolygon_wayid"
    };
    # Copy tags from relation
    while (my ($key, $val) = each(%{$rel->{"tags"}}))
    {
        $multipolygon->{"tags"}->{$key}=$val;
    }
    foreach my $relmember(@{$rel->{"members"}})
    {
        my ($role, $obj) = @$relmember;
        next unless (defined $role && defined $obj && 
            ref($obj) eq "way" && defined $obj->{"nodes"});
        if ($role eq "outer")
        {
            push(@$outerways, $obj);
            push(@{$multipolygon->{'relations'}}, @{$obj->{'relations'}});
        }
        elsif ($role eq 'inner')
        {
            push(@$innerways, $obj);
        }
        else
        {
            debug("Unknown role \"$role\" in multipolygon relation $rel->{'id'}!") if ($debug->{'multipolygon'});
        }
        $obj->{'multipolygon'} = 1; # Mark object as beeing part of a multipolygon
    }

    if (defined(@$outerways))
    {
        if (!multipolygon_has_tags($multipolygon))
        {
            # multipolygon relation without tags on itself
            # using deprecated multipolygon relation tagging rules
            # to figure out what to do - compatibility mode
            my %relation_tags_count;
            my %relation_tags_tag;
            my %relation_tags_value;
            for (my $i = 0; $i < scalar @$outerways; $i++)
            {
                my $obj = \%{@$outerways[$i]};
                while (my ($key, $val) = each(%{$obj->{'tags'}}))
                {
                    $relation_tags_count{"$key\0$val"}++;
                    $relation_tags_tag{"$key\0$val"} = $key;
                    $relation_tags_value{"$key\0$val"} = $val;
                }
            }

            foreach my $keyval(keys %relation_tags_count)
            {
                # copy only tags to the multipolygon relation wich are
                # shared by each outer way
                if ($relation_tags_count{$keyval} == scalar @$outerways)
                {
                    my $key = $relation_tags_tag{$keyval};
                    my $val = $relation_tags_value{$keyval};
                    $multipolygon->{'tags'}->{$key} = $val;
                }
            }

            if (!multipolygon_has_tags($multipolygon))
            {
                # multipolygon has still no tags, do it like earlier in orp.pl
                # just copy everything from the outer ways to the multipolygon
                # relation
                #
                # pure guessing - results aren't predictable and mostly wrong
                for (my $i = 0; $i < scalar @$outerways; $i++)
                {
                    my $obj = \%{@$outerways[$i]};
                    while (my ($key, $val) = each(%{$obj->{'tags'}}))
                    {
                        next if defined($multipolygon->{'tags'}->{$key});
                        $multipolygon->{'tags'}->{$key}=$val;
                    }
                }
                debug("No common tags on outer ways in multipolygon relation $rel->{'id'}!") if $debug->{'multipolygon'};
            }
            else
            {
                # just a warning
                debug("Copied common tags from outer way(s) into multipolygon relation $rel->{'id'}!") if $debug->{'multipolygon'};
            }
        }
    }

    # A list of all outer and inner nodes is assembled, now sort them
    $multipolygon->{'outer'} = assemble_closed_ways($outerways, $rel);
    $multipolygon->{'inner'} = assemble_closed_ways($innerways, $rel);
    bless($multipolygon, 'multipolygon');

    # Add inner ways to the global list of ways
    WAY:
    foreach my $way (@{$multipolygon->{'inner'}})
    {
        # Handle multipolygon in multipolygon
        foreach (@{$way->{'relations'}})
        {
            my ($role, $wayrelation) = @{$_};
            next WAY if ($role eq 'outer' and defined $wayrelation->{"tags"}->{"type"}
                and $wayrelation->{"tags"}->{"type"} eq "multipolygon")
        }
        # Handle old-style multipolygons
        next if tags_subset($way, $multipolygon);
        $way_storage->{$way->{'id'}} = $way;
    }
    # Add multipolygon object to the global list of ways
    $way_storage->{$multipolygon->{'id'}} = $multipolygon;
}

# initialise level-0 selection list with all available objects.
# (relations are only there for specific reference; you cannot
# have rules that match relations. if you want that, then add
# relations to the initial selection here.)
our $selection = [];
$selection->[0] = Set::Object->new();
$selection->[0]->insert(values(%$way_storage));
$selection->[0]->insert(values(%$node_storage));

# initialise the tag indexes. These will help us to quickly
# find objects that have a given tag key.
foreach (values(%$way_storage))
{
    foreach my $key(keys(%{$_->{"tags"}}))
    {
        push(@{$index_way_tags->{$key}}, $_);
    }
}
foreach (values(%$node_storage))
{
    foreach my $key(keys(%{$_->{"tags"}}))
    {
        push(@{$index_node_tags->{$key}}, $_);
    }
}

my $count = $selection->[0]->size();
debug("$count objects in level-0 selection") if ($debug->{"general"});

my $title = get_variable("title", "");
my $showBorder = get_variable("showBorder", "no");
my $showScale = get_variable("showScale", "no");
my $showLicense = get_variable("showLicense", "no");
our $textAttenuation = get_variable("textAttenuation");

# the following conversion factor is required to support width tags in meters
$meter2pixel = get_variable("meter2pixel", "0.1375");

# extra height for marginalia
my $marginaliaTopHeight = ($title ne "") ? 40 : 
    ($showBorder eq "yes") ? 1.5 : 0;
my $marginaliaBottomHeight = 
    ($showScale eq "yes" or $showLicense eq "yes") ? 45 : 
    ($showBorder eq "yes") ? 1.5 : 0;

# extra width and height for border
my $extraWidth = ($showBorder eq "yes") ? 3 : 0;
my $extraHeight = ($title eq "" and $showBorder eq "yes") ? 3 : 0;

# Find out what bounds to render

our ($minlat, $minlon, $maxlat, $maxlon);

if (scalar(grep defined, values %opt_bounds) == 4) # Given on command line
{
    $minlat = $opt_bounds{minlat};
    $minlon = $opt_bounds{minlon};
    $maxlat = $opt_bounds{maxlat};
    $maxlon = $opt_bounds{maxlon};
}
elsif ($rules->find('//rules/bounds')) # Given in rules file
{
    $minlat = get_variable('bounds/minlat');
    $minlon = get_variable('bounds/minlon');
    $maxlat = get_variable('bounds/maxlat');
    $maxlon = get_variable('bounds/maxlon');
}
elsif (%data_bounds) # Given as <bounds> element(s) in data file
{
    $minlat = $data_bounds{minlat};
    $minlon = $data_bounds{minlon};
    $maxlat = $data_bounds{maxlat};
    $maxlon = $data_bounds{maxlon};
}
else # Fallback to calculating based on nodes in data
{
    foreach (values(%$node_storage))
    {
        $maxlon = $_->{"lon"} if (!defined($maxlon) || $_->{"lon"} > $maxlon);
        $maxlat = $_->{"lat"} if (!defined($maxlat) || $_->{"lat"} > $maxlat);
        $minlon = $_->{"lon"} if (!defined($minlon) || $_->{"lon"} < $minlon);
        $minlat = $_->{"lat"} if (!defined($minlat) || $_->{"lat"} < $minlat);
    }
}

our $scale = get_variable("scale", 1);
our $symbolScale = get_variable("symbolScale", 1);
our $projection = 1 / cos(($maxlat + $minlat) / 360 * pi);
our $km = 0.0089928*$scale*10000*$projection;
our $dataWidth = ($maxlon - $minlon) * 10000 * $scale;
# original osmarender: our $dataHeight = ($maxlat - $minlat) * 10000 * $scale * $projection;
our $dataHeight = (ProjectF($maxlat) - ProjectF($minlat)) * 180 / pi * 10000 * $scale; 
our $minimumMapWidth = get_variable("minimumMapWidth", undef);
our $minimumMapHeight = get_variable("minimumMapHeight", undef);
our $documentWidth = ($dataWidth > $minimumMapWidth * $km) ? $dataWidth : $minimumMapWidth * $km;
our $documentHeight = ($dataHeight > $minimumMapHeight * $km) ? $dataHeight : $minimumMapHeight * $km;

# FIXME: what's the logic behind the following?
our $width = ($documentWidth + $dataWidth) / 2;
our $height = ($documentHeight + $dataHeight) / 2;

# Approximate projected bounds
our ($minx, $miny, $maxx, $maxy);

our $project;

# Project all nodes into document space
if (get_variable("projection")) {
    require "project.pm";
} else {
    $project = \&project;
}


my $output = new IO::File(">$output_file");
our $writer = new XML::Writer(OUTPUT => $output, UNSAFE => 1, 
    DATA_MODE => 1, DATA_INDENT => 3, ENCODING => "utf-8");

# Add xml-stylesheet processing instruction if given
my $style = get_variable("xml-stylesheet", undef);
if( $style ){
    $writer->pi('xml-stylesheet', "href=\"$style\" type=\"text/css\"");
}

my $svgWidth = $documentWidth + $extraWidth;
my $svgHeight = $documentHeight + $marginaliaBottomHeight + $marginaliaTopHeight;

# start the SVG document
$writer->startTag("svg",
    "xmlns" => "http://www.w3.org/2000/svg",
    "xmlns:svg" => "http://www.w3.org/2000/svg",
    "xmlns:xlink" => "http://www.w3.org/1999/xlink",
    "xmlns:xi" => "http://www.w3.org/2001/XInclude",
    "xmlns:inkscape" => "http://www.inkscape.org/namespaces/inkscape",
    "xmlns:cc" => "http://web.resource.org/cc/",
    "xmlns:rdf" => "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "id" => "main", 
    "version" => 1.1, 
    "baseProfile" => get_variable("svgBaseProfile"), 
    "width" => "${svgWidth}px", 
    "height" => "${svgHeight}px", 
    "preserveAspectRatio" => "none", 
    "viewBox" => sprintf("%f %f %f %f", 
       -$extraWidth/2, -$extraHeight/2, $svgWidth, $svgHeight));
# FIXME add interactive stuff
# fixme add metadata

# copy definitions from rule file
$writer->startTag("defs", "id" => "defs-rulefile");
$writer->raw($rules->findnodes_as_string("//rules/defs/*[local-name() != 'svg' and local-name() != 'symbol']"));
$writer->endTag("defs");

# copy symbols
sub registerSymbol
{
    (my $node, my $id, my $width, my $height) = @_;
    $id = $node->getAttribute('id') unless defined $id;
    $width = $node->getAttribute('width') unless defined $width;
    $height = $node->getAttribute('height') unless defined $height;

    $symbols{$id}{'width'} = $width ne ""?$width:0;
    $symbols{$id}{'height'} = $height ne ""?$height:0;
}

$writer->startTag("defs", "id" => "defs-symbols");
# ... from stylesheet, convert svg to symbol if necessary
foreach my $node ($rules->find('//rules/defs/svg:symbol')->get_nodelist)
{
    $writer->raw($node->toString);
    registerSymbol($node);
}
foreach my $node ($rules->find('//rules/defs/svg:svg')->get_nodelist)
{
    my $id = $node->getAttribute('id');
    my %attributes = map {$_->getName => $_->getNodeValue} $node->getAttributes;
    $writer->startTag("symbol", %attributes);
    $writer->raw($rules->findnodes_as_string("//rules/defs/svg:svg[\@id='$id']/*"));
    $writer->endTag("symbol");
    registerSymbol($node);
}
# ... from symbols dir
my $symbolsDir = get_variable("symbolsDir");
if (defined($symbolsDir))
{
    # allow overriding symbolsDir with local directory - only recurse to global dir if local
    # not found
    $symbolsDir = File::Spec->catdir($Bin, '../stylesheets/', $symbolsDir) unless (-d $symbolsDir);

    # get refs, then convert to hash so we can get only unique values
    my %refs = map {$_, 1} map {$_->getNodeValue} $rules->find('/rules//symbol/@ref | /rules//areaSymbol/@ref')->get_nodelist;
    foreach my $file (keys %refs) 
    {
        if (not exists $symbols{'symbol-'.$file})
        {
	    my $symbolFile = XML::XPath->new(filename => $symbolsDir . "/" . $file . ".svg"); 
            $symbolFile->set_namespace('svg', 'http://www.w3.org/2000/svg');
	    my $symbol = $symbolFile->find('/svg:svg/svg:defs/svg:symbol');
            if ($symbol->size()==1)
            {
                $writer->raw($symbol->get_node(1)->toString);
                registerSymbol($symbol->get_node(1));
            } else
            {
                my $svgNode = $symbolFile->find("/svg:svg")->get_node(1);
                my %namespaces = map {"xmlns:".$_->getPrefix => $_->getExpanded} $svgNode->getNamespaces;
                $namespaces{'xmlns'} = $namespaces{'xmlns:#default'};
                delete $namespaces{'xmlns:#default'};
                my %attributes = map {$_->getName => $_->getValue} $svgNode->getAttributes;
                $attributes{'id'} = "symbol-".$file; 
                $writer->startTag("symbol", %attributes, %namespaces);
                $writer->raw($symbolFile->findnodes_as_string("/svg:svg/*"));
                $writer->endTag("symbol");
                registerSymbol($svgNode, $attributes{'id'});
            }
        }
    }
}
$writer->endTag("defs");

#include referenced defs
$writer->startTag("defs", "id" => "defs-included");
foreach my $include ($rules->find("/rules//include")->get_nodelist)
{
    my $includeFile = XML::XPath->new(filename => File::Spec->catdir($Bin, '../stylesheets/', $include->getAttribute("ref")));
    $includeFile->set_namespace('svg', 'http://www.w3.org/2000/svg');
    $writer->raw($includeFile->findnodes_as_string("/svg:svg/*"));
}
$writer->endTag("defs");

# load label relations
foreach my $relation (values(%$relation_storage))
{
    my $type = $relation->{'tags'}->{'type'};
    next unless defined($type) && $type eq 'label';

    my $labelRelationInfo = [];

    # make list of labels
    foreach my $relpair (@{$relation->{"members"}})
    {
        my ($role, $ref) = @$relpair;
        if ($role eq 'label' && ref $ref eq 'node')
        {
            push @$labelRelationInfo, $ref;
        }
    }

    # assing labels to first object, other object will be empty
    my $first = 1;
    foreach my $relpair (@{$relation->{"members"}})
    {
        my ($role, $ref) = @$relpair;

        if ($role eq 'object')
        {
            if ($first)
            {
                $labelRelations->{$ref->{'id'}} = $labelRelationInfo;
                $first = 0;
            }
            else
            {
                $labelRelations->{$ref->{'id'}} = [];
            }
        }
    }
}

# Clipping rectangle for map

$writer->startTag("clipPath", "id" => "map-clipping");
$writer->emptyTag("rect", "id" => "map-clipping-rect", "x" => "0px", "y" => "0px", 
    "height" => $documentHeight."px", "width" => $documentWidth."px");
$writer->endTag("clipPath");

# Start of main drawing

$writer->startTag("g", "id" => "map", "clip-path"=> "url(#map-clipping)", 
    "inkscape:groupmode" => "layer", "inkscape:label" => "Map", 
    "transform" => "translate(0,$marginaliaTopHeight)");

# Draw a nice background layer

$writer->emptyTag("rect", "id" => "background", "x" => "0px", "y" => "0px", 
    "height" => $documentHeight."px", "width" => $documentWidth."px",
    "class" => "map-background");

# Process all the rules drawing all map features 

# If the global var withOSMLayers is 'no', we don't care about layers and 
# draw everything in one go. This is faster and is sometimes useful. For 
# normal maps you want withOSMLayers to be 'yes', which is the default.

my $rulelist = $rules->find('//rules/rule');

if (get_variable("withOSMLayers", "yes") eq "no")
{
    # we have all elements in selection0, process rules for all of them.
    process_rule($_, 0) foreach ($rulelist->get_nodelist());
}
else
{
    # process all layers
    process_rule($_, 0) foreach ($rulelist->get_nodelist());

    # render z-mode=bottom
    render_layer('bottom', [grep({($_->{'instruction'}->getAttribute('z-mode') || 'normal') eq 'bottom'} @$drawing_commands)]);

    # prepare z-mode=normal
    my %normalInstructions;
    foreach my $command (@$drawing_commands)
    {
        my $instruction = $command->{'instruction'};
        next unless ($instruction->getAttribute('z-mode') || 'normal') eq 'normal';

        my $layer = $instruction->getAttribute('layer');
        
        foreach my $element (@{$command->{'elements'}})
        {
            my $elementLayer = $layer || $element->{'tags'}->{'layer'} || 0;
            
            if (!defined($normalInstructions{$elementLayer}))
            {
                $normalInstructions{$elementLayer} = [{'instruction'=>$instruction}];
            }

            if ($normalInstructions{$elementLayer}[-1]->{'instruction'} != $instruction)
            {
                push @{$normalInstructions{$elementLayer}}, {'instruction'=>$instruction};
            }

            push @{$normalInstructions{$elementLayer}[-1]->{'elements'}}, $element;
        }
    }

    # render z-mode=normal
    foreach my $layer(sort { $a <=> $b } keys %normalInstructions)
    {
        render_layer($layer, $normalInstructions{$layer});
    }

    # render z-mode=top
    render_layer('top', [grep(($_->{'instruction'}->getAttribute('z-mode') || 'normal') eq 'top', @$drawing_commands)]);
}

$writer->endTag('g');

draw_map_decoration();
draw_marginalia() if ($title ne "" || $showScale eq "yes" || $showLicense eq "yes");

# Generate named path definitions for referenced ways

generate_paths();

# FIXME zoom controls from Osmarender.xsl

$writer->endTag('svg');
$writer->end();
$output->close();

exit;


sub render_layer
{
    (my $id, my $commands) = @_;

    return unless defined($commands);

    $writer->startTag('g',
       'inkscape:groupmode' => 'layer',
       'id' => "layer$id",
       'inkscape:label' => "Layer $id");

    foreach my $command (
        sort {($a->{'instruction'}->getAttribute('z-index') || 0) <=> ($b->{'instruction'}->getAttribute('z-index') || 0)} @$commands)
    {
        $instructions{$command->{'instruction'}->getName()}->{'func'}->($command->{'instruction'}, undef, $command->{'elements'});
    }

    $writer->endTag();
}

sub get_way_href
{
    my ($id, $type) = @_;

    $referenced_ways{$id}->{$type} = 1;
    return '#way_'.$type.'_'.$id;
}

# sub generate_paths()
# --------------------
#
# Creates path definitions for all ways in the source.
#
sub generate_paths
{
    $writer->startTag("defs", "id" => "defs-ways");

    foreach my $way_id (keys %referenced_ways)
    {
        # extract data into variables for convenience. the "points"
        # array contains lat/lon pairs of the nodes.
        my $way = $way_storage->{$way_id};
        debug('ERROR: Multipolygon in generate_paths!') if ref $way eq
            'multipolygon' and $debug->{'multipolygon'};
        my $types = $referenced_ways{$way_id};
        my $tags = $way->{"tags"};
        my $points = [];
        foreach (@{$way->{"nodes"}})
        {
            push(@$points, [ $_->{"lat"}, $_->{"lon"} ]) if (defined($_->{"lat"}) && defined($_->{"lon"}));
        }

        my $n = scalar(@$points) -1;
        next if ($n < 1);

        # generate a normal way path
        if ($types->{'normal'})
        {
            $writer->emptyTag("path", "id" => "way_normal_$way_id", "d" => make_path(@$points));
        }

        # generate reverse path if needed
        if ($types->{'reverse'})
        {
            $writer->emptyTag("path", "id" => "way_reverse_$way_id", 
                "d" => make_path(reverse @$points));
        }

        # this generates extended paths. extended paths have one extra node at the beginning 
        # and one at the end, each "shooting out" of the way in a straight line, and each
        # having a distance from the original end node equivalent to the total length of the
        # way. the path is thus exactly three times as long as it was originally.
        # the idea is that by drawing text on such extended paths instead of normal paths, 
        # text may "spill" over the end of the road which is not always beautiful but can give
        # better results. 
        #
        # extended paths are requested by the draw_text_on_path function when it encounters
        # text drawing instructions with the "overflow" attribute set to "true". it is not
        # suggested to use this feature for tiles@home.
        if ($types->{'normal_extended'} or $types->{'reverse_extended'})
        {
            # compute total length
            my $wl = 0;
            for (my $i = 1; $i < scalar(@$points); $i++)
            {
                my $pwl = sqrt(($points->[$i]->[0] - $points->[$i-1]->[0])**2 + ($points->[$i]->[1] - $points->[$i-1]->[1])**2);
                $wl += $pwl;
            }

            # extend before first node
            my $a = $points->[1]->[0] - $points->[0]->[0];
            my $b = $points->[1]->[1] - $points->[0]->[1];
            my $bb;
            my $aa;
            if ($b == 0)
            {
                $bb = 0;
                $aa = $wl;
            }
            else
            {
                $bb = sqrt($wl**2 / ($a**2/$b**2 + 1));
                $bb *= -1 if ($b<0);
                $aa = $bb/$b*$a;
            }

            my $startnode = [ $points->[0]->[0] - $aa, $points->[0]->[1] - $bb ];

            # extend after last node
            $a = $points->[$n]->[0] - $points->[$n-1]->[0];
            $b = $points->[$n]->[1] - $points->[$n-1]->[1];
            if ($b == 0)
            {
                $bb = 0;
                $aa = $wl;
            }
            else
            {
                $bb = sqrt($wl**2 / ($a**2/$b**2 + 1));
                $bb *= -1 if ($b<0);
                $aa = $bb/$b*$a;
            }
            my $endnode = [ $points->[$n]->[0] + $aa, $points->[$n]->[1] + $bb ];
            
            foreach my $t("normal_extended", "reverse_extended")
            {
                $writer->emptyTag("path", "id" => "way_${t}_$way_id", "d" => 
                    ($t eq "normal_extended") ? 
                        make_path($startnode, @$points, $endnode) : make_path($endnode, reverse(@$points), $startnode));
            }
        }

        # generate the start, middle and end paths needed for "smart linecaps".
        # The first and last way segment are split in the middle.
        my $midpoint_head = [ ($points->[0]->[0]+$points->[1]->[0])/2,
                             ($points->[0]->[1]+$points->[1]->[1])/2 ];
        my $midpoint_tail = [ ($points->[$n]->[0]+$points->[$n-1]->[0])/2,
                             ($points->[$n]->[1]+$points->[$n-1]->[1])/2 ];
        my $firstnode = shift @$points;
        my $lastnode = pop @$points;

        if ($types->{'start'})
        {
            $writer->emptyTag("path", "id" => "way_start_$way_id", 
                "d" => make_path($firstnode, $midpoint_head));
        }
        if ($types->{'end'})
        {
            $writer->emptyTag("path", "id" => "way_end_$way_id", 
                "d" => make_path($midpoint_tail, $lastnode));
        }
        if ($types->{'mid'})
        {
            $writer->emptyTag("path", "id" => "way_mid_$way_id", 
                "d" => make_path($midpoint_head, @$points, $midpoint_tail)) if scalar(@$points);
        }
    };
    $writer->endTag("defs");
}


# sub draw_map_decoration()
# -------------------------
#
# Draws grids and stuff.
#
sub draw_map_decoration
{
    $writer->startTag('g', 
        'inkscape:groupmode' => 'layer',
        'inkscape:label' => 'Map decoration',
        'transform' => "translate(0,$marginaliaTopHeight)");

    # draw a grid if required
    if (get_variable("showGrid") eq "yes")
    {
        # grid spacing in metres.
        my $gridSpacing = get_variable("gridSpacing", 1000);
        my $gridSpacingPx = $km / 1000 * $gridSpacing;
        $writer->startTag('g', 
            'inkscape:groupmode' => 'layer',
            'inkscape:label' => 'Grid');
        for (my $i=1; $i<$documentHeight / $gridSpacingPx; $i++)
        {
            $writer->emptyTag('line',
                'id' => 'grid-hori-'.$i,
                'x1' => '0px', 'y1' => sprintf('%fpx', $i * $gridSpacingPx),
                'x2' => $documentWidth.'px', 'y2' => sprintf('%fpx', $i * $gridSpacingPx),
                'class' => 'map-grid-line');
        }
        for (my $i=1; $i<$documentWidth / $gridSpacingPx; $i++)
        {
            $writer->emptyTag('line',
                'id' => 'grid-vert-'.$i,
                'x1' => sprintf('%fpx', $i * $gridSpacingPx), 'y1' => 0, 
                'x2' => sprintf('%fpx', $i * $gridSpacingPx), 'y2' => $documentHeight.'px',
                'class' => 'map-grid-line');
        }
        $writer->endTag('g');
    }

    # draw a border if required
    if (get_variable("showBorder") eq "yes")
    {
        $writer->startTag('g', 
            'id' => 'border',
            'inkscape:groupmode' => 'layer',
            'inkscape:label' => 'Map Border');
        foreach my $type('casing', 'core')
        {
            $writer->emptyTag('line',
                'id' => 'border-left-'.$type,
                'x1' => 0, 'y1' => 0, x2 => 0, y2 => $documentHeight, 
                'class' => 'map-border-'.$type,
                'stroke-dasharray' => sprintf("%f,1", $km/10-1));
            $writer->emptyTag('line',
                'id' => 'border-top-'.$type,
                'x1' => 0, 'y1' => 0, x2 => $documentWidth, y2 => 0,
                'class' => 'map-border-'.$type,
                'stroke-dasharray' => sprintf("%f,1", $km/10-1));
            $writer->emptyTag('line',
                'id' => 'border-bottom-'.$type,
                'x1' => 0, 'y1' => $documentHeight, x2 => $documentWidth, y2 => $documentHeight,
                'class' => 'map-border-'.$type,
                'stroke-dasharray' => sprintf("%f,1", $km/10-1));
            $writer->emptyTag('line',
                'id' => 'border-right-'.$type,
                'x1' => $documentWidth, 'y1' => 0, x2 => $documentWidth, y2 => $documentHeight, 
                'class' => 'map-border-'.$type,
                'stroke-dasharray' => sprintf("%f,1", $km/10-1));
        }
        $writer->endTag('g');
    }
    $writer->endTag('g');
}

# sub draw_map_decoration()
# -------------------------
#
# Draws license and stuff.
#
sub draw_marginalia
{
    $writer->startTag('g', 
        'id' => 'marginalia',
        'inkscape:groupmode' => 'layer',
        'inkscape:label' => 'Marginalia');
    if ($title ne "")
    {
        $writer->startTag('g',
            'inkscape:groupmode' => 'layer',
            'inkscape:label' => 'Title');
        $writer->emptyTag('rect',
            'id' => 'marginalia-title-background', 
            'class' => 'map-title-background', 
            'x' => '0px', y => '0px', 
            'width' => $documentWidth.'px', 'height' => sprintf('%fpx', $marginaliaTopHeight - 5));
        $writer->dataElement('text', $title, 
            'id' => 'marginalia-title-text', 
            'class' => 'map-title',
            'x' => $documentWidth/2, 'y' => 30);
        $writer->endTag('g');
    }
    if ($showScale eq "yes" || $showLicense eq "yes")
    {
        $writer->startTag('g', 
            'id' => 'marginalia-bottom',
            'inkscape:groupmode' => 'layer',
            'inkscape:label' => 'Marginalia (Bottom)');
        $writer->emptyTag('rect',
            'id' => 'marginalia-background',
            'x' => '0px', y => sprintf('%fpx', $documentHeight + 5),
            'height' => '40px', width => $documentWidth.'px',
            'class' => 'map-marginalia-background');
        if ($showScale eq 'yes')
        {
            my $x1 = 14;
            my $y = int (28.5 + $documentHeight);
            my $x2 = $x1 + $km;
            $writer->startTag('g',
                'id' => 'marginalia-scale',
                'inkscape:groupmode' => 'layer',
                'inkscape:label' => 'Scale');
            $writer->emptyTag('line',
                'class' => 'map-scale-casing',
                'x1' => $x1, 'y1' => $y, 'x2' => $x2, 'y2' => $y);
            $writer->emptyTag('line',
                'class' => 'map-scale-core',
                'stroke-dasharray' => $km/10,
                'x1' => $x1, 'y1' => $y, 'x2' => $x2, 'y2' => $y);
            $writer->emptyTag('line',
                'class' => 'map-scale-bookend',
                'x1' => $x1, 'y1' => $y+2, 'x2' => $x1, 'y2' => $y-10);
            $writer->emptyTag('line',
                'class' => 'map-scale-bookend',
                'x1' => $x2, 'y1' => $y+2, 'x2' => $x2, 'y2' => $y-10);
            $writer->dataElement('text', '0',
                'class' => 'map-scale-caption', 
                'x' => $x1, 'y' => $y-10);
            $writer->dataElement('text', '1km',
                'class' => 'map-scale-caption', 
                'x' => $x2, 'y' => $y-10);
            $writer->endTag('g');
        }
        if ($showLicense eq 'yes')
        {
            $writer->startTag('g',
                'inkscape:groupmode' => 'layer',
                'inkscape:label' => 'Copyright',
                'transform' => sprintf('translate(%f,%f)', $documentWidth, $documentHeight));
            open(CCLOGO, "cclogo.svg");
            local $/;
            $_ = <CCLOGO>;
            $writer->raw($_);
            close(CCLOGO);
            $writer->endTag('g');
        }
        $writer->endTag('g');
    }
    $writer->endTag('g');
}
        

# -------------------------------------------------------------------
# sub process_layer()
#
# Used for layer instructions.
#
# -------------------------------------------------------------------
sub process_layer
{

    my ($layernode, $depth, $layer) = @_;


    my $lname = $layernode->getAttribute("name");
    my $opacity = $layernode->getAttribute("opacity");

    debug("layer: $lname") if ($debug->{'rules'});
    
    $writer->startTag("g", "name" => "Layer-$lname", $opacity eq ""?"":"opacity" => $opacity );

    $selection->[$depth+1] = $selection->[$depth];

    
    foreach ($layernode->getChildNodes())
    {
        my $name = $_->getName() || "";

        if($name eq "rule")
        {
            process_rule($_, $depth+1, $layer);
        }
        elsif ($name ne "")
        {
            debug("'$name' id not allowed layer instruction '$lname' ignored");
        }
    }
    $writer->endTag("g");

}


# -------------------------------------------------------------------
# sub process_rule()
#
# The main workhorse. 
#
# This is called recursively if you have nested rule elements.
#
# Parameters:
# $rulenode - the XML::XPath node for the <rule> or <else> element
#   being processed.
# $depth -    the recursion depth.
# $layer -    the OSM layer being processed (undef for no layer restriction)
# $previous - the XML::XPath node for the previous <rule> of the 
#   same depth; used only for debug messages.
# -------------------------------------------------------------------
sub process_rule
{
    my ($rulenode, $depth, $layer, $previous) = @_;

    # normally, we pass on the given layer attribute unchanged, and it
    # will the be honoured in the various drawing instruction handlers.
    # However if the rule itself has a layer attribute, this means that 
    # - if we are on that layer, let that rule process ALL objects
    #   (i.e. lift the "only objects on layer X" restriction)
    # - if we are on another layer, ignore the rule.
    
    my $rule_layer = $rulenode->getAttribute('layer') || '';
    if (($rule_layer ne '') && defined($layer))
    {
        if ($rule_layer != $layer)
        {
            debug("rule has layer '$rule_layer', ignored on layer '$layer': ".$rulenode->toString(1))
                if ($debug->{"rules"});
            return;
        }
        undef $layer;
    }

    # ----------------------------------------------------
    # Part 1 of process_rule:
    # create the new selection by applying the rule to  
    # the current selection.
    # ----------------------------------------------------

    if ($rulenode->getName() eq "rule")
    {
        # normal selection 
        $selection->[$depth+1] = make_selection($rulenode, $selection->[$depth]);
        if ($debug->{'rules'})
        {
            debug('rule "'.$rulenode->toString(1).'" matches '.
                $selection->[$depth+1]->size().' elements');
        }
    }
    elsif ($rulenode->getName() eq "else")
    {
        # "else" selection - a selection for our level of 
        # recursion already exists (from the previous rule) and 
        # we need to select all objects that are present in the
        # selection one level up and not in the previous rule's
        # selection (which is on our level of recursion).
        $selection->[$depth+1] = $selection->[$depth] - $selection->[$depth+1];
        if ($debug->{'rules'})
        {
            debug('"else" branch of rule "'.$previous->toString(1).
                '" matches '.$selection->[$depth+1]->size().' elements');
        }
    }
    else 
    {
        die("internal error, process_rule must not be called with '".
            $rulenode->getName()."' node");
    }

    my $selected = $selection->[$depth+1];

    # if no rows were inserted, we can leave now. 
    if ($selected->size() == 0)
    {
        return;
    }

    # ----------------------------------------------------
    # Part 2 of process_rule:
    # the selection is complete; iterate over child nodes
    # of the rule and either do recursive rule processing, 
    # or execute drawing instructions.
    # ----------------------------------------------------

    my $previous_child;
    foreach my $instruction ($rulenode->getChildNodes())
    {
        next unless ref $instruction eq 'XML::XPath::Node::Element';
        my $name = $instruction->getName() || '';

        if ($name eq "layer")
        {
              process_layer($instruction, $depth+1, $layer);
        }
        elsif ($name eq "rule")
        {
            # a nested rule; make recursive call.
            process_rule($instruction, $depth+1, $layer);
        }
        elsif ($name eq "else")
        {
            # an "else" element. 
            if (!defined($previous_child) || $previous_child->getName() ne "rule")
            {
                debug("<else> not following <rule>, ignored: ".substr($instruction->toString(0), 0, 60)."...");
            }
            else
            {
                # make recursive call
                process_rule($instruction, $depth+1, $layer, $previous_child);
            }
        }
        elsif ($instructions{$name})
        {
            my $command = {'instruction' => $instruction, 'elements' => [$selected->members()]};
            push @$drawing_commands, $command
        }
        elsif ($name ne "")
        {
            debug("unknown drawing instruction '$name' ignored");
        }
        $previous_child = $_ unless ($name eq "");
    }
}

# -------------------------------------------------------------------
# sub make_selection()
#
# Applies a rule to a selection, and returns the new (reduced)
# selection.
#
# Parameters:
#    $rulenode - the Xml::XPath node for the rule
#    $oldsel - the Set::Object reference for the current selection
#
# Returns:
#    a new Set::Object with the reduced selection.
# -------------------------------------------------------------------
sub make_selection
{
    my ($rulenode, $oldsel) = @_;

    my $k = $rulenode->getAttribute("k");
    my $v = $rulenode->getAttribute("v");

    # read the "e" attribute of the rule (type of element)
    # and execute the selection for these types. "e" is assumed
    # to be either "node", "way", or "node|way".
    
    my $e = $rulenode->getAttribute("e");
    my $s = $rulenode->getAttribute("s");
    my $rows_affected;

    # make sure $e is either "way" or "node" or undefined (=selects both)
    my $e_pieces = {};
    $e_pieces->{$_}=1 foreach(split('\|', $e));
    if ($e_pieces->{'way'} && $e_pieces->{'node'})
    {
        undef $e;
    }
    elsif ($e_pieces->{'way'})
    {
        $e = 'way';
    }
    else
    {
        $e = 'node';
    }
    foreach(keys(%$e_pieces))
    {
        warn('ignored invalid value "'.$_.'" for e attribute in rule '.$rulenode->toString(1))
            unless($_ eq "way" or $_ eq "node");
    }

    my $interim;

    if ($k eq '*' or !defined($k))
    {
        # rules that apply to any key. these don't occur often
        # but are in theory supported by osmarender.

        if ($v eq "~")
        {
            # k=* v=~ means elements without tags.
            # FIXME "s"
           $interim = select_elements_without_tags($oldsel, $e);
        }
        elsif ($v eq "*")
        {
            # k=* v=* means elements with any tag.
            # FIXME "s"
            $interim = select_elements_with_any_tag($oldsel, $e);
        }
        else
        {
            # k=* v=something means elements with a tag that has the
            # value "something". "something" may be a pipe-separated
            # list of values. The "~" symbol is not supported in the
            # list.
            # FIXME "s"
            $interim = select_elements_with_given_tag_value($oldsel, $e, $v);
        }
    }
    else
    {
        # rules that apply to the specifc key given in $k. This may
        # be a pipe-separated list of values.
        
        if ($v eq "*")
        {
            # objects that have the given key(s), with any value.
            # FIXME "s"
            $interim = select_elements_with_given_tag_key($oldsel, $e, $k);
        }
        elsif ($v eq "~")
        {
            # objects that don't have the key(s)
            # FIXME "s"
            $interim = select_elements_without_given_tag_key($oldsel, $e, $k);
        }
        elsif ($s eq "" and index($v, '~') == -1)
        {
            # objects that have the given keys and values, where none of the
            # values is "~"
            $interim = select_elements_with_given_tag_key_and_value_fast($oldsel, $e, $k, $v);
        }
        elsif ($s eq "way" and index($v, '~') == -1)
        {
            # nodes that belong to a way that has the given keys and values,
            # where none of the values is "~"
            $interim = select_nodes_with_given_tag_key_and_value_for_way_fast($oldsel, $k, $v);
        }
        else
        {
            # the code that can handle "~" in values (i.e. rules like "the 
            # 'highway' tag must be 'bridleway' or not present at all)
            # is slower since it cannot use indexes.
            $interim = select_elements_with_given_tag_key_and_value_slow($oldsel, $e, $k, $v, $s);
        }
    }

    # make assertion to help programmers who break the above
    die ("something is wrong") unless defined($interim);

    # post-process the selection according to proximity filter, if set.

    # the following control the proximity filter. horizontal and vertical proximity
    # control the size of the imaginary box drawn around the point where the label is
    # placed. proximityClass is a storage class; if it is shared by multiple rules,
    # then all these rules compete for the same space. If no class is set then the
    # filter only works on objects selected by this rule.
    # FIXME: proximity filtering does not take text length into account, and boxes
    # are currently based on lat/lon values to remain compatible to Osmarender,
    # yielding reduced spacings the closer you get to the poles.

    my $hp = $rulenode->getAttribute("horizontalProximity");
    my $vp = $rulenode->getAttribute("verticalProximity");
    my $pc = $rulenode->getAttribute("proximityClass");
    if ($hp ne "" && $vp ne "")
    {
        #debug("activating proximity filter for rule");
        $interim = select_proximity($interim, $hp, $vp, $pc);
    }


    # post-process with minSize filter, if set
    my $minsize = $rulenode->getAttribute("minSize");
    if ($minsize ne "")
    {
        $interim = select_minsize($interim, $minsize);
    }

    # post-process with notConnectedSameTag filter, if set
    my $notConnectedSameTag = $rulenode->getAttribute("notConnectedSameTag");
    if ($notConnectedSameTag ne '')
    {
        $interim = select_not_connected_same_tag($interim, $notConnectedSameTag);
    }

    # Filter for closed or unclosed ways if closed= is set
    my $closed = $rulenode->getAttribute("closed");
    if ($closed ne '')
    {
        if ($closed ne 'yes' && $closed ne 'no')
        {
            die "Error in stylesheet: <rule closed= must be 'yes' or 'no'\n";
        }

        select_closed($interim, $closed);
    }

    return $interim;
}

# -------------------------------------------------------------------
# sub make_path(@nodelist)
#
# returns an SVG path string for the given list of points.
# -------------------------------------------------------------------
sub make_path
{
    my $firstpoint = shift;
    my $path = sprintf("M".project_string($firstpoint));
    $path .= "L".project_string($_) foreach @_;
    return $path;
}

# -------------------------------------------------------------------
# sub project_string($latlon)
#
# takes an array reference with a "lat" and a "lon" element
# and returns a string consisting of two space-separated floats
# for usage in SVG paths.
# -------------------------------------------------------------------
sub project_string
{
    my $latlon = shift;
    my $projected = $project->($latlon);
    return sprintf("%f %f", $projected->[0], $projected->[1]);
}

# -------------------------------------------------------------------
# sub project($latlon)
#
# takes an array reference with a "lat" and a "lon" element
# and returns an array reference with "x" and "y" elements.
#
# SUPER BIG FIXME: switch to Proj.4 library to allow arbitrary
# (correct) projections instead of the current kludge. Also, 
# possibly project stuff directly in the data base.
# -------------------------------------------------------------------
sub project
{
    my $latlon = shift;
    return [
        $width - ($maxlon-$latlon->[1])*10000*$scale, 
        # original osmarender (unused)
        # $height + ($minlat-$latlon->[0])*10000*$scale*$projection
        # new (proper merc.)
        $height + (ProjectF($minlat) - ProjectF($latlon->[0])) * 180/pi * 10000 * $scale 
    ];
}

sub distance
{
    my ($p1, $p2) = @_;
    return great_circle_distance(deg2rad($p1->{"lon"}),
        deg2rad(90-$p1->{"lat"}), 
        deg2rad($p2->{"lon"}), 
        deg2rad(90-$p2->{"lat"}), 6378135);
}


# -------------------------------------------------------------------
# sub get_variable($name)
#
# helper that reads a variable value from the rule file.
# $name is a slash separated path, and the last element of
# $name is taken to be an attribute name.
# 
# if the variable is not found, the given $default is returned.
# -------------------------------------------------------------------
sub get_variable
{
    my ($name, $default) = @_;
    my $fullname = "//rules/$name";
    $fullname =~ m!(.*)/(.*)!;
    my $find = "$1/\@$2";
    my $obj = $rules->findvalue($find);
    return $default unless $obj;
    return sprintf("%s", $obj);
}

# -------------------------------------------------------------------
# sub copy_attributes_in_list($node, $list)
#
# returns an array that contains altarnating keys and values of each
# of $node's attributes, where the key is mentioned in the $list
# array reference.
#
# used to supply attributes to XML::Writer methods.
# -------------------------------------------------------------------
sub copy_attributes_in_list
{
    my ($node, $list) = @_;
    my $result = [];
    foreach my $key(@$list)
    {
        my $attr = $node->getAttribute($key);
        if ($attr ne "")
        {
            push @$result, $key, $attr;
        }
    }
    return @$result;
}

# -------------------------------------------------------------------
# sub copy_attributes_not_in_list($node, $list)
#
# returns an array that contains altarnating keys and values of each
# of $node's attributes, where the key is not in the $list
# array reference.
#
# used to supply attributes to XML::Writer methods.
# -------------------------------------------------------------------
sub copy_attributes_not_in_list
{
    my ($node, $list) = @_;
    my $result = [];
    foreach my $attr($node->getAttributes())
    {
        my $k = $attr->getName();
        foreach(@$list)
        {
            if ($_ eq $k)
            {
                undef $attr;
                last;
            }
        }
        if ($attr)
        {
            push @$result, $k, $attr->getValue();
        }
    }
    return @$result;
}


# -------------------------------------------------------------------
# sub debug($msg)
#
# prints $msg to stdout.
# -------------------------------------------------------------------
sub debug
{
    my $msg = shift;
    print $msg."\n";
}

# -------------------------------------------------------------------
# sub usage($msg)
#
# prints $msg and usage info, and exits.
# -------------------------------------------------------------------
sub usage
{
    my $msg = shift;
    print <<EOF;
$msg

Usage:

perl orp.pl [options] data.osm

Options:
   -d|--debug=list      specify list of debug options
   -o|--outfile=name    specify output file (default: same as input, with .svg)
   -r|--rule=file.xml   specify the rule file to use (default: rule.xml)
   --minlat		Minimum latitude to render
   --minlon		Minimum longitude to render
   --maxlat		Maximum latitude to render
   --maxlon		Maximum longitude to render
   -b|--bbox=minLat,minLon,maxLat,maxLon specify bounding box (as alternative to the specific options)

EOF
    exit(1);
}

# from tahproject.pm
sub ProjectF
{
    my $Lat = shift() / 180 * pi;
    my $Y = log(tan($Lat) + sec($Lat));
    return($Y);
}

