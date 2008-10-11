#!/usr/bin/perl

# or/p - Osmarender in Perl
# -------------------------
#
# OSM data parsing module
#
# (See orp.pl for details.)
#
# This module contains a SAX handler code that can be used in conjunction
# with XML::Parser::PerlSAX to parse OSM XML files.

package SAXOsmHandler;

use strict;

my $current_element;

my $node_storage;
my $way_storage;
my $relation_storage;

sub new 
{
    my ($type, $ns, $ws, $rs) = @_;
    $node_storage = $ns;
    $way_storage = $ws;
    $relation_storage = $rs;
    return bless {}, $type;
}

sub start_element 
{
    my ($self, $element) = @_;

    if ($element->{Name} eq 'node') 
    {
        undef $current_element;
        return if ($element->{'Attributes'}->{'action'} eq 'delete');
        my $id = $element->{'Attributes'}->{'id'};
        $node_storage->{$id} = $current_element = { 'id' => $id, 'layer' => 0, 
            'lat' => $element->{'Attributes'}->{'lat'}, 
            'lon' => $element->{'Attributes'}->{'lon'}, 
            'ways' => [], 'relations' => [] };
        bless($current_element, 'node');
    }
    elsif ($element->{Name} eq 'way')
    {
        undef $current_element;
        return if ($element->{'Attributes'}->{'action'} eq 'delete');
        my $id = $element->{'Attributes'}->{'id'};
        $way_storage->{$id} = $current_element = { 'id' => $id, 'layer' => 0, 
            'nodes' => [], 'relations' => [] };
        bless($current_element, 'way');
        
    }
    elsif ($element->{Name} eq 'relation')
    {
        undef $current_element;
        return if ($element->{'Attributes'}->{'action'} eq 'delete');
        my $id = $element->{'Attributes'}->{'id'};
        $relation_storage->{$id} = $current_element = { 'id' => $id, 
            'members' => [], 'relations' => [] };
        bless($current_element, 'relation');
    }
    elsif (($element->{Name} eq 'nd') and (ref($current_element) eq 'way'))
    {
        push(@{$current_element->{'nodes'}}, $node_storage->{$element->{'Attributes'}->{'ref'}});
    }
    elsif (($element->{Name} eq 'member') and (ref($current_element) eq 'relation'))
    {
        # relation members are temporarily stored as symbolic references (e.g. a
        # string that contains "way:1234") and only later replaced by proper 
        # references.
        push(@{$current_element->{'members'}}, 
            [ $element->{'Attributes'}->{'role'}, 
              $element->{'Attributes'}->{'type'}.":".
              $element->{'Attributes'}->{'ref'} ]);
    }
    elsif ($element->{Name} eq 'tag')
    {
        # store the tag in the current element's hash table.
        # also extract layer information into a direct hash member for ease of access.
        $current_element->{"tags"}->{$element->{"Attributes"}->{"k"}} = $element->{"Attributes"}->{"v"};
        $current_element->{"layer"} = $element->{"Attributes"}->{"v"} if ($element->{"Attributes"}->{"k"} eq "layer");
    }
    else
    {
        # ignore for now
    }
}

sub characters 
{
    # osm data format has no plain character data
}

sub end_element 
{
    # no
}

sub start_document {
    my ($self) = @_;
    print "Starting SAX OSM parser\n";
}

sub end_document {
    my ($self) = @_;
    print "SAX OSM parser finished\n";
}

1; 

