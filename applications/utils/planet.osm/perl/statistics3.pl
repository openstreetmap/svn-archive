#!/usr/bin/perl
#
# This program counts the numbers of nodes, ways and relations per user in a 
# planet.osm file. Of course segments/parts can be used like germany or hessen.osm.
# No history data is used. Just the field user in xml.
# Output is first general stats, then lists of users with numbers of nodes, 
# ways and relations, unsorted.
# Then TOP lists are generated and displayed.
# 
# usage: perl statistics2.pl -CIO < input.osm
#
# runtime 28 secs for hessen.osm (3.3 million lines) on 1.7GHz Windows Machine
#

use autodie;
use strict;
use warnings;
use YAML::XS qw(Dump);

# get filename from cmd line
my $xml = shift||'';
if (!$xml)
{
    print STDERR "usage: perl -CIO statistics3.pl < input.osm\n";
}

# file present?
unless( -f $xml || $xml eq "-" ) {
	die("input file '$xml' not found\n");
}

# variables
my $node_count = 0;
my $way_count = 0;
my $rel_count = 0 ;
my $line_count = 0;
my $key = "";
my $temp ;
my $num_keys;
my $max_user = "" ;
my $max_number = 0 ;
my $top = 30 ;


my %user_relations ;
my %user_ways ;
my %user_nodes ;


# open input file
open(my $XML, "<:encoding(UTF-8)", $xml);

# parse data
while(my $line = <$XML>) {
	$line_count++;

	# way data?
	if($line =~ /^\s*\<way/) {
		my ($id)   = ($line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/);
		my ($user) = ($line =~ /^.+user=[\'\"](.*?)[\'\"]/);
		unless ($id) { next; }
		unless ($user) { next; }
		$way_count++;
        $user_ways{$user}++;
	}

	# node data
	if($line =~ /^\s*\<node/) {
		my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/);
		my ($user) = ($line =~ /^.+user=[\'\"](.*?)[\'\"]/);
		unless ($id) { next; }
		unless ($user) { next; }
		$node_count++;
        $user_nodes{$user}++;
	}

	# relation data
	if($line =~ /^\s*\<relation/) {
		my ($id)   = ($line =~ /^\s*\<relation id=[\'\"](\d+)[\'\"]/);
		my ($user) = ($line =~ /^.+user=[\'\"](.*?)[\'\"]/);
		unless ($id) { next; }
		unless ($user) { next; }
		$rel_count++;
        $user_relations{$user}++;
	}

	# ignore the rest (for now)

}

my %stat;

%stat = (
    file => $xml,
    num_lines => $line_count,
    num_nodes => $node_count,
    num_ways => $way_count,
    num_relations => $rel_count,
);

foreach $key ( sort keys %user_nodes){
	$stat{node_users}->{$key} = $user_nodes{$key} if $user_nodes{$key} > 10;
}

foreach $key ( sort keys %user_ways){
	$stat{way_users}->{$key} = $user_ways{$key} if $user_ways{$key} > 5;
}

foreach $key ( sort keys %user_relations){
	$stat{relation_users}->{$key} = $user_relations{$key} if $user_relations{$key} > 2;
}

$temp = 1;
$num_keys = scalar keys %user_nodes;
while ($temp <= $top and $temp <= $num_keys) {
	$max_user = "" ;
	$max_number = 0 ;
	foreach $key (keys %user_nodes) {
		if ($user_nodes{$key} > $max_number) {
			$max_number = $user_nodes{$key} ;
			$max_user = $key ;
		} ;
	} ;
	$stat{top_nodes}->{$max_user} = $max_number if $max_number > 50;
	$user_nodes{$max_user} = 0 ;
	$temp = $temp + 1 ;
} ;

$temp = 1;
$num_keys = scalar keys %user_ways;
while ($temp <= $top and $temp <= $num_keys) {
	$max_user = "" ;
	$max_number = 0 ;
	foreach $key (keys %user_ways) {
		if ($user_ways{$key} > $max_number) {
			$max_number = $user_ways{$key} ;
			$max_user = $key ;
		} ;
	} ;
	$stat{top_ways}->{$max_user} = $max_number if $max_number > 50;
	$user_ways{$max_user} = 0 ;
	$temp = $temp + 1 ;
} ;

$temp = 1;
$num_keys = scalar keys %user_relations;
while ($temp <= $top and $temp <= $num_keys) {
	$max_user = "" ;
	$max_number = 0 ;
	foreach $key (keys %user_relations) {
		if ($user_relations{$key} > $max_number) {
			$max_number = $user_relations{$key} ;
			$max_user = $key ;
		} ;
	} ;
	$stat{top_relations}->{$max_user} = $max_number if $max_number > 50;
	$user_relations{$max_user} = 0 ;
	$temp = $temp + 1 ;
} ;

print Dump \%stat;
