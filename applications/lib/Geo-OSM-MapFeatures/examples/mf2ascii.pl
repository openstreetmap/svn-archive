#!/usr/bin/perl

=pod

=head1 DESCRIPTION

Simple test script that outputs a simple ascii output of features with descriptions

=cut

use Geo::OSM::MapFeatures;

my $language = $ARGV[0];

my $page;
if( $language ){
	$page = "$language:Map_Features";
} else {
	$page = "Map_Features";
}

my $mf = new Geo::OSM::MapFeatures({page => $page});
$mf->trace(1);

unless( $ENV{MAPFEATURESDEBUG} ){
	$mf->download();
} else {
	$mf->debug_download();
}

$mf->parse();

# To print a simple ascii representation:
binmode(STDOUT, ':utf8');
foreach my $category ( sort( $mf->categories() ) ){
	print "\n\n===== $category =====\n";
	foreach my $feature ( $mf->features($category) ){
		printf("%-35s %s\n", "$feature", $feature->description);
	}
}
