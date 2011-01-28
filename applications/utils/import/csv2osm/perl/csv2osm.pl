#!/usr/bin/perl
# CSV to OSM converter - see README.txt for details

eval {
	require 'config';
};
if($!) {
	die("You must copy config.sample to config, and customise\n");
}

use HTTP::Request;
use LWP::UserAgent;
my $ua = LWP::UserAgent->new;

# URLs we'll use to do things
my $new_node_url = 'http://www.openstreetmap.org/api/0.3/node/0';
my $map_url = 'http://www.openstreetmap.org/api/0.3/map?bbox='; # bllong,bllat,trlong,trlat

# Open the files we'll need
open(CSV, "<$input_csv") or die ("Could not load csv '$input_csv'\n");
open(WORKED, "> worked.osm");
open(PROBLEM, "> problem.osm");

# Output the headers
my $xml_header = "<?xml version='1.0'?>\n";
my $xml_osm_header = "$xml_header<osm version='0.3' generator='CSV2OSM'>\n";
my $xml_osm_footer = "</osm>\n";
print WORKED $xml_osm_header;
print PROBLEM $xml_osm_header;

# Process the file
while(my $line = <CSV>) {
	chomp $line;
	unless($line) { next; }
	my @tmp = split(/,/,$line);
	my $csv_ref = \@tmp;	

	# Let the custom processer tweak it
	&csv_tweaker($csv_ref);

	# Do substitutions in the output
	my %data;
	for(my $i=0; $i<@output_mapping; $i++) {
		my $val = $output_mapping[$i];
		while($val =~ /\$COL(\d+)/) {
			my $index = $1;
			my $subs = $csv_ref->[($index-1)];
			$val =~ s/\$COL$index/$subs/;
		}
		$val =~ /^(.*?)=(.*)$/;
		$data{$1} = $2;
	}

	# Convert lat+long into useful formats
	($data{'latitude'},$data{'longitude'}) = 
		format_latlong($data{'latitude'},$data{'longitude'});

	# Build the XML
	my $xml = "<node id='0' lat='".$data{'latitude'}."' lon='".$data{'longitude'}."'>\n";
	foreach my $key (sort keys %data) {
		unless($key eq "latitude" || $key eq "longitude") {
			$xml .= "  <tag k='$key' v='".$data{$key}."' />\n";
		}
	}
	$xml .= "</node>\n";

	print "\n\nPotential new node is:\n";
	print "$xml\n";

	# Do a fetch for the surrounding area
	my ($min_lat,$min_long,$max_lat,$max_long) = 
		build_search_latlong($data{'latitude'},$data{'longitude'});
	# map?bbox=bllon,bllat,trlon,trlat
	my $url = $map_url."$min_long,$min_lat,$max_long,$max_lat";
	print "Doing search:\n  ";
	print "$url\n";

	my $resp = $ua->get( build_url($url) );
	unless($resp->is_success) {
		warn("Error fetching: ".$resp->status_line."\n");
		print PROBLEM $xml;
		next;
	}
	my $data = $resp->content;

	# Check to see if we had a matching node or not
	my @nodes = ($data =~ /(<node .*?<\/node>)/gs);
	print "Found ".(scalar @nodes)." nodes in the search area.\n";

	my $match = 0;
	foreach my $node (@nodes) {
		foreach my $attr (@search_attrs) {
			my ($key,$value) = split(/=/, $attr);
			if($node =~ /<tag k=['"]${key}['"] v=['"]${value}['"]/) {
				# Match
				warn("Found possible match for new node, not adding.\n");
				print "Match is:\n".$node."\n";
				$match = 1;
			}
		}
	}
	if($match) {
		print PROBLEM $xml;
		next;
	}
	print "No nodes found with matching attributes, adding\n";
	
	# Add
	my $upload_xml = $xml_osm_header.$xml.$xml_osm_footer;
	my $request = HTTP::Request->new(
					"PUT", build_url($new_node_url), undef, $upload_xml
	);
	$resp = $ua->request($request);
	unless($resp->is_success) {
		warn("Error uploading: ".$resp->status_line."\n");
		print PROBLEM $xml;
		next;
	}
	# Grab the ID
	my $id = $resp->content;
	chomp $id;

	# Save 
	$xml =~ s/id='0'/id='$id'/;
	print WORKED $xml;
	print "\nAdded node, new id is $id\n";
}

# Close down
print WORKED $xml_osm_footer;
print PROBLEM $xml_osm_footer;
close WORKED;
close PROBLEM;


sub format_latlong($,$) {
	my @data = @_;
	my @out;
	foreach my $val (@data) {
		my $sign = 1;
		if($val =~ /([NE])$/i) {
			chop $val;
		}
		if($val =~ /([SW])$/i) {
			chop $val;
			$sign = -1;
		}

		if($val =~ /^(\-?\d+)[-:](\d+)[-:](\d+)$/) {
			my ($h,$m,$s) = (int($1),int($2),int($3));
			$val = $h + ($m/60) + ($s/60/60);	
			$val *= $sign;
		} elsif($val =~ /^(\-?\d+\.\d+)$/) {
			# In right format already
		} else {
			die("Unknown lat/long format '$val'\n");
		}
		push @out, $val;
	}

	return @out;	
}

sub build_search_latlong($,$) {
	my ($lat,$long) = @_;

	my $pi = atan2(1,1) * 4;

	# The earth's radius, in meters, at the equator (should be close enough)
	my $earth_radius_m = 6335.437 * 1000;

	# What's the earth's radius at this latitude?
	my $erl = cos($lat*$pi/360) * $earth_radius_m;

	# Cheat a bit, this delta is only appropriate for one of lat or long 
	#  (can't remember which), but it'll do for short distances
	my $delta = $search_distance / $erl * 360;
	my $delta_lat = $delta;
	my $delta_long = $delta;

	return ($lat-$delta_lat,$long-$delta_long,$lat+$delta_lat,$long+$delta_long);
}

sub build_url($) {
	my $url = shift;
	$url =~ s/^http:\/\///;
	$username =~ s/\@/\%40/g;

	return "http://".$username.":".$password."@".$url;
}
