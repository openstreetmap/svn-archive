#!/usr/bin/perl
#
# this script takes an osm file like planet.osm and runs some simple statistics
# it's my first PERL script and maybe later some more sophisticated ones will
# follow. Gary68
#
# usage: statistics.pl <osm/xml file name> <user name>
#
# output is node, way and relation ids from user and then statistics 
# line count and way/node/relation counts, total and for the special user
# the counts represent the number of nodes/ways/relations where user=<user name> (create/change) 
# no history data is used
#
# runtime for hessen.osm (3.3 million lines) on my 1.7GHz Windows is 25secs
# germany.osm takes 5 mins with 45 million lines
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

# get 2nd argument, the user name
my $user_name = shift||'';
if (!$user_name)
{
    print STDERR "usage: statistics.pl <osm/xml file name> <user name>\n";
}

# variables
my $node_count = 0;
my $way_count = 0;
my $rel_count = 0 ;
my $line_count = 0;
my $mine = 0;
my $mine_nodes = 0;
my $mine_relations = 0;


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

                if ($user eq $user_name) {
			print "Way: ", $id, "\n" ;
			$mine = $mine + 1;

		} 
		$way_count++;
	}

	# node data
	if($line =~ /^\s*\<node/) {
		my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/);
		my ($user) = ($line =~ /^.+user=[\'\"](\w+)[\'\"]/);
		unless ($id) { next; }
		unless ($user) { next; }

                if ($user eq $user_name) {
			print "Node: ", $id, "\n" ;
			$mine_nodes = $mine_nodes + 1;

		} 
		$node_count++;
	}

	# relation data
	if($line =~ /^\s*\<relation/) {
		my ($id)   = ($line =~ /^\s*\<relation id=[\'\"](\d+)[\'\"]/);
		my ($user) = ($line =~ /^.+user=[\'\"](\w+)[\'\"]/);
		unless ($id) { next; }
		unless ($user) { next; }

                if ($user eq $user_name) {
			print "Relation: ", $id, "\n" ;
			$mine_relations = $mine_relations + 1;

		} 
		$rel_count++;
	}

	# ignore the rest (for now)

}

print "\n\nStatistics for file: ", $xml, "\n";
print "Number rows: ", $line_count, "\n" ;
print "Total Ways: ", $way_count, "\n" ;
print "Total Nodes: ", $node_count, "\n" ;
print "Total Relations: ", $rel_count, "\n" ;
print "\nStatistics for user: ", $user_name, "\n" ;
print "User ways: ", $mine, "\n" ;
print "User nodes: ", $mine_nodes, "\n" ;
print "User relations: ", $mine_relations, "\n" ;
print "\n" ;
