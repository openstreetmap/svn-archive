#!/usr/bin/perl
#
# This program counts the numbers of nodes, ways and relations per user in a 
# planet.osm file. Of course segments/parts can be used like germany or hessen.osm.
# No history data is used. Just the field user in xml.
# Output is first general stats, then lists of users with numbers of nodes, 
# ways and relations, unsorted.
# Then TOP lists are generated and displayed.
# 
# usage: statistics.pl <osm/xml file name>
#
# runtime 28 secs for hessen.osm (3.3 million lines) on 1.7GHz Windows Machine
#

use strict;
use warnings;


# get filename from cmd line
my $xml = shift||'';
if (!$xml)
{
    print STDERR "usage: statistics.pl <osm/xml file name> <user name>\n";
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
my $max_user = "" ;
my $max_number = 0 ;
my $top = 30 ;


my %user_relations ;
my %user_ways ;
my %user_nodes ;


# open input file
open(XML, "<$xml") or die("$!");

# parse data
while(my $line = <XML>) {
	$line_count++;

	# way data?
	if($line =~ /^\s*\<way/) {
		my ($id)   = ($line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/);
		my ($user) = ($line =~ /^.+user=[\'\"](\w+)[\'\"]/);
		unless ($id) { next; }
		unless ($user) { next; }
		$way_count++;
		if (exists ($user_ways {$user})) {
			$user_ways {$user} = ($user_ways {$user} + 1) ;
		} 
		else {
			$user_ways {$user} = 1 ;	
		} 

	}

	# node data
	if($line =~ /^\s*\<node/) {
		my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/);
		my ($user) = ($line =~ /^.+user=[\'\"](\w+)[\'\"]/);
		unless ($id) { next; }
		unless ($user) { next; }
		$node_count++;
		if (exists ($user_nodes {$user})) {
			$user_nodes {$user} = ($user_nodes {$user} + 1) ;
		} 
		else {
			$user_nodes {$user} = 1 ;	
		} 

	}

	# relation data
	if($line =~ /^\s*\<relation/) {
		my ($id)   = ($line =~ /^\s*\<relation id=[\'\"](\d+)[\'\"]/);
		my ($user) = ($line =~ /^.+user=[\'\"](\w+)[\'\"]/);
		unless ($id) { next; }
		unless ($user) { next; }
		$rel_count++;
		if (exists ($user_relations {$user})) {
			$user_relations {$user} = ($user_relations {$user} + 1) ;
		} 
		else {
			$user_relations {$user} = 1 ;	
		} 
	}

	# ignore the rest (for now)

}

print "\n\nStatistics for file: ", $xml, "\n";
print "Number rows: ", $line_count, "\n" ;
print "Total Ways: ", $way_count, "\n" ;
print "Total Nodes: ", $node_count, "\n" ;
print "Total Relations: ", $rel_count, "\n" ;
#...
print "\n" ;


print "\n\nNODES\n-----\n";
foreach $key ( sort keys %user_nodes){
	printf "%-50s %10i\n", $key, $user_nodes{$key} ;
} ;

print "\n\nWAYS\n----\n";
foreach $key ( sort keys %user_ways){
	printf "%-50s %10i\n", $key, $user_ways{$key} ;
} ;
print "\n\nRELATIONS\n---------\n";
foreach $key ( sort keys %user_relations){
	printf "%-50s %10i\n", $key, $user_relations{$key} ;
} ;

print "\nTOP NODES\n" ;
print "\n---------\n" ;
$temp = 1;
while ($temp <= $top) {
	$max_user = "" ;
	$max_number = 0 ;
	foreach $key (keys %user_nodes) {
		if ($user_nodes{$key} > $max_number) {
			$max_number = $user_nodes{$key} ;
			$max_user = $key ;
		} ;
	} ;
	printf "%-30s %10i\n", $max_user, $max_number ;
	$user_nodes{$max_user} = 0 ;
	$temp = $temp + 1 ;
} ;

print "\nTOP WAYS\n" ;
print "\n--------\n" ;
$temp = 1;
while ($temp <= $top) {
	$max_user = "" ;
	$max_number = 0 ;
	foreach $key (keys %user_ways) {
		if ($user_ways{$key} > $max_number) {
			$max_number = $user_ways{$key} ;
			$max_user = $key ;
		} ;
	} ;
	printf "%-30s %10i\n", $max_user, $max_number ;
	$user_ways{$max_user} = 0 ;
	$temp = $temp + 1 ;
} ;

print "\nTOP RELATIONS\n" ;
print "\n-------------\n" ;
$temp = 1;
while ($temp <= $top) {
	$max_user = "" ;
	$max_number = 0 ;
	foreach $key (keys %user_relations) {
		if ($user_relations{$key} > $max_number) {
			$max_number = $user_relations{$key} ;
			$max_user = $key ;
		} ;
	} ;
	printf "%-30s %10i\n", $max_user, $max_number ;
	$user_relations{$max_user} = 0 ;
	$temp = $temp + 1 ;
} ;
