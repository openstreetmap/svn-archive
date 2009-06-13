#!/usr/bin/perl
use warnings;
use strict;

use Geo::OSM::MapFeatures;
use XML::LibXML;

my $mf = new Geo::OSM::MapFeatures;
$mf->trace(1);
$mf->debug_download();
$mf->parse();

my $doc = XML::LibXML::Document->createDocument();
my $root = $doc->createElement('mapfeatures');
$doc->setDocumentElement( $root );

foreach my $section ( $mf->categories() ){
    my $sectionelem = $root->appendChild( $doc->createElement('section') );
    $sectionelem->setAttribute('name', $section);

    foreach my $feature ( $mf->features($section) ){
        my $featureelem = $sectionelem->appendChild( $doc->createElement('feature') );
        $featureelem->setAttribute('key', $feature->key());
        next unless $feature->values();
        foreach my $value ( @{$feature->values()} ){
            my $valueelem = $featureelem->appendChild( $doc->createElement('value') );
            $valueelem->setAttribute('name', "$value");
        }
    }
}

print $doc->serialize(1);
