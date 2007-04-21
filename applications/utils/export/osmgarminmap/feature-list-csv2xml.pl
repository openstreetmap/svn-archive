#!/usr/bin/perl
#
#  garmin-features-csv2xml.pl
#

use XML::Generator;

use strict;

my $xml = XML::Generator->new(':pretty');

my %features;

<>; # read first line and discard it

while(<>) {
    chomp;
 
    my ($topo, $type, $subtype, $topo_id, $type_id, $subtype_id, $name) = split(/\|/);

    next if ($type eq '');
    next if ($subtype eq '') && ($topo eq 'point');

    my %attributes = ( 'type' => $type, 'garmin_id' => $type_id );
    $attributes{'subtype'} = $subtype if $subtype;
    $attributes{'garmin_subid'} = $subtype_id if $subtype_id;

    push(@{$features{$topo}}, $xml->feature( \%attributes ));
}

print $xml->xmldecl( 'version' => '1.0', 'encoding' => 'UTF-8' );
print "<?xml-stylesheet type='text/xsl' href='../mpx2mp.xsl'?>\n";
print $xml->xmlcmnt('This file was generated from a CSV file with the script garmin-features-csv2xml.pl. Do not change!'), "\n";
print $xml->defs( { 'data' => 'map.mpx' },
    $xml->point(    { 'id' => 'RGN10' }, @{$features{'point'}}),
    $xml->city(     { 'id' => 'RGN20' }, @{$features{'city'}}),
    $xml->polyline( { 'id' => 'RGN40' }, @{$features{'polyline'}}),
    $xml->polygon(  { 'id' => 'RGN80' }, @{$features{'polygon'}}),
), "\n";

