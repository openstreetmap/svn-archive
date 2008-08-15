#!/usr/bin/perl
#
# This program calculates the center of a user's activity in a given osm file/area.
# It uses the field user and builds the means of longitude and latitude for
# each user. This information is added to the original OSM file and can be fed to
# a renderer, i.e. KOSMOS. The file is written to STDOUT. Users are labeled light,
# medium and heavy according to contributed nodes. Limits can be edited in source file below.
#
# written by Gary68
#
# usage: centeruseractivity.pl input.osm > output.osm 
#
# runtime for hessen.osm (3.3 million lines xml) is 36 secs on 1.7GHz Windows machine
# 
# The lines inserted into the OSM file look like this and can be used by a renderer to display the
# center of a user's activity:
#
#  <node id="900000096" user="Gary68script" lat="49.9772216" lon="8.43589323333333">
#    <tag k="stat" v="usercenter"/>
#    <tag k="name" v="mdebets"/>
#    <tag k="contribution" v="heavy"/>
#  </node>
#
#
#
# A KOSMOS render rule to show the new information could look like this:
#
#  | UserCenterNode || {{IconNode}} || {{tag|stat|usercenter}} || Symbol (Type=Circle, MinZoom=10, Color=blue, BorderColor=red, BorderWidth=15%, Size=13:6;17:20)<br>Text (MinZoom=10, Color=black, TagToUse=name, FontName=Arial, FontStyle=bold, FontSize=10:8;15:12;17:16, TextLineOffset=-100%) ||
#
#
#
# Of course you could use child rules as well to distinguish between the different contribution attributes.
#
#  | UserCenterNode || {{IconNode}} || {{tag|stat|usercenter}} ||  ||
#  |-
#  | .heavy || {{IconNode}} || {{tag|contribution|heavy}} || Symbol (Type=Circle, MinZoom=10, Color=black, BorderColor=yellow, BorderWidth=15%, Size=10:8;17:25)<br>Text (MinZoom=10, Color=black, TagToUse=name, FontName=Arial, FontStyle=bold, FontSize=10:8;15:12;17:16, TextLineOffset=-100%) ||
#  |-
#  | .medium || {{IconNode}} || {{tag|contribution|medium}} || Symbol (Type=Circle, MinZoom=10, Color=blue, BorderColor=yellow, BorderWidth=15%, Size=10:6;17:20)<br>Text (MinZoom=10, Color=black, TagToUse=name, FontName=Arial, FontStyle=bold, FontSize=10:8;15:12;17:16, TextLineOffset=-100%) ||
#  |-
#  | .light || {{IconNode}} || {{tag|contribution|light}} || Symbol (Type=Circle, MinZoom=10, Color=green, BorderColor=yellow, BorderWidth=15%, Size=10:4;17:16)<br>Text (MinZoom=10, Color=black, TagToUse=name, FontName=Arial, FontStyle=bold, FontSize=10:8;15:12;17:16, TextLineOffset=-100%) ||
#
#

use strict;
use warnings;


# get filename from cmd line
my $xml = shift||'';
if (!$xml)
{
    print STDERR "centeruseractivity.pl input.osm > output.osm\n";
}

# file present?
unless( -f $xml || $xml eq "-" ) {
	die("input file '$xml' not found\n");
}

# variables
my $temp ;
my $user ;
my $firstway = 1 ;
my $nodeid = 900000000 ; # first id for the new nodes to be added
my $heavy = 10000 ;
my $medium = 1000 ;

my %user_lon ;
my %user_lat ;
my %user_nodes ;


# open input file
open(XML, "<$xml") or die("$!");

# parse data
while(my $line = <XML>) {

	# node data
	if($line =~ /^\s*\<node/) {
		# get all needed information
		my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/); # get node id
		my ($user) = ($line =~ /^.+user=[\'\"](\w+)[\'\"]/);       # get node user (last one)
		my ($lon) = ($line =~ /^.+lon=[\'\"]([\d,\.]+)[\'\"]/);    # get position
		my ($lat) = ($line =~ /^.+lat=[\'\"]([\d,\.]+)[\'\"]/);    # get position
		unless ($id) { print $line ; next; }
		unless ($user) { print $line ; next; }
		unless ($lat) { print $line ; next; }
		unless ($lon) { print $line ; next; }
		
		# store information
		if (exists ($user_nodes {$user})) {
			$user_nodes {$user} = ($user_nodes {$user} + 1) ;
			$user_lon {$user} = $user_lon {$user} + $lon ;
			$user_lat {$user} = $user_lat {$user} + $lat ;
		} 
		else {
			$user_nodes {$user} = 1 ;	
			$user_lon {$user} = $lon ;
			$user_lat {$user} = $lat ;
		} 

	}

	# way data
	if($line =~ /^\s*\<way/) {
		if ($firstway == 1) {
			$firstway = 0 ;

			# insert new nodes in output file
			foreach $user (keys %user_nodes) {

				# calc means
				my ($lat) = $user_lat {$user} / $user_nodes {$user} ;
				my ($lon) = $user_lon {$user} / $user_nodes {$user} ;

				# write new nodes with tags
				print "  <node id=\"$nodeid\" user=\"Gary68script\" lat=\"$lat\" lon=\"$lon\">\n" ; 
				print "    <tag k=\"stat\" v=\"usercenter\"/>\n" ;
				print "    <tag k=\"name\" v=\"$user\"/>\n" ;
				if ($user_nodes {$user} >= $heavy) {
					print "    <tag k=\"contribution\" v=\"heavy\"/>\n" ;
				}
				elsif ($user_nodes {$user} >= $medium) {
					print "    <tag k=\"contribution\" v=\"medium\"/>\n" ;
				}
				else {
					print "    <tag k=\"contribution\" v=\"light\"/>\n" ;
				}
				print "  </node>\n" ;
				$nodeid = $nodeid + 1 ;
				
			}

		}
	}

	# print all existing lines as they are
	print $line ; 
}

