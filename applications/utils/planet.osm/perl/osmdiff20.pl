#
# osmdiff.pl by Gary68
#
# osmdiff takes two .osm files and compares them. It will list changes of node and way data. It also draws a map
# of the area and highlights changes. Changes are distinguished between:
# - new nodes and ways
# - changed nodes and ways (position, length)
# - deleted nodes and ways
# - changes in tags
#
# size and thickness are optional and will default to 1024 pixels and 1 pixel // deleted ways will be drawn at a minimum of 2 pixels though
# size given is the size of longitude in pixels. If C is specified background will be colored lightly.
#
# Version 2
# - German Umlaute (DONE - OK)
# - history links for ways (DONE - OK)
# - history links for nodes (DONE - OK)
# - limit html table width to 100% (DONE - OK)
# - exclude "created_by" tags (DONE)
# - optional different background colors (DONE - OK)
# 



use strict ;
use warnings ;

use File::stat;
use Time::localtime;
use GD ;
use Encode ;

##########
#constants
##########
my $version = "V2.0" ;
my $usage   = "osmdiff.pl file1.osm file2.osm output.htm pic.png [size [thickness [C]]]" ;
my $delimiter = ":" ;	# for tag/value pair

##################
# OSM data storage
##################
my %node0_lon ;
my %node0_lat ;
my %node0_tags ;
my %node0_user ;
my %way0_nodes ;
my %way0_tags ;
my %way0_user ;
my %node1_lon ;
my %node1_lat ;
my %node1_tags ;
my %node1_user ;
my %way1_nodes ;
my %way1_tags ;
my %way1_user ;
my %place_name ;
my %place_lon ;
my %place_lat ;
my %user_lon ;
my %user_lat ;
my %user_num ;


##################
# difference lists
##################
my @nodes_new = () ;
my @nodes_deleted = () ;
my @nodes_changed_pos = () ;
my @nodes_changed_tags = () ;
my @ways_new = () ;
my @ways_deleted = () ;
my @ways_changed_num_nodes = () ;
my @ways_changed_tags = () ;
my @ways_changed_nodepos = () ;

my $nodetags0 = 0 ;
my $waytags0 = 0 ;
my $nodetags1 = 0 ;
my $waytags1 = 0 ;
my $waynodecount0 = 0 ;
my $waynodecount1 = 0 ;


######################
# definition variables
######################
my $bottom = "" ;
my $top = "" ;
my $left = "" ;
my $right = "" ;

################
# file variables
################
my $basefile_name = "" ;
my $file2_name = "" ;
my $html_name ;
my $html_file ;
my $pic_name ;
my $pic_file ;


##########
# graphics
##########
my $pic_size_x ; # lon
my $pic_size_y ; # lat
my $node_diameter = 4 ;
my $thickness ;
my $colors = "N" ;


#################
# other variables
#################
my $key ;
my $time0 ;
my $time1 ;
my $timespent ;
my $temp ;
my $i ;
my $j ;
my $n ;
my $c ;
my $d ;
my $e ;


$time0 = time() ;

#########################
# get cmd line parameters
#########################

$basefile_name = shift||'';
if (!$basefile_name)
{
	die (print $usage, "\n");
}

$file2_name = shift||'';
if (!$file2_name)
{
	die (print $usage, "\n");
}

$html_name = shift||'';
if (!$html_name)
{
	die (print $usage, "\n");
}

$pic_name = shift||'';
if (!$pic_name)
{
	die (print $usage, "\n");
}

$pic_size_x = shift||'';
if (!$pic_size_x)
{
	$pic_size_x = 1024 ;
}

$thickness = shift||'';
if (!$thickness)
{
	$thickness = 1 ;
}

$colors = shift||'';
if (!$colors)
{
	$colors = "N" ;
}

print "\n" ;

read_file ($basefile_name, 0) ;
read_file ($file2_name, 1) ;


####################
# calc maxs and mins
####################
$top = -99 ;
$left = 999 ;
$bottom = 99 ;
$right = -999 ;

print "calculating area...\n" ;
foreach $key (keys %node0_lat) {
	if ($node0_lat{$key} > $top ) { $top = $node0_lat{$key} ; }
	if ($node0_lat{$key} < $bottom ) { $bottom = $node0_lat{$key} ; }
	if ($node0_lon{$key} < $left ) { $left = $node0_lon{$key} ; }
	if ($node0_lon{$key} > $right ) { $right = $node0_lon{$key} ; }
}

foreach $key (keys %node1_lat) {
	if ($node1_lat{$key} > $top ) { $top = $node1_lat{$key} ; }
	if ($node1_lat{$key} < $bottom ) { $bottom = $node1_lat{$key} ; }
	if ($node1_lon{$key} < $left ) { $left = $node1_lon{$key} ; }
	if ($node1_lon{$key} > $right ) { $right = $node1_lon{$key} ; }
}

print "Left: $left " ;
print "Right: $right " ;
print "Bottom: $bottom " ;
print "Top: $top\n" ;

open ($html_file, ">", $html_name) ;

print_html_header () ;

#############
# comparisons
#############

print "start comparison and write html...\n" ;

#####################
# general information
#####################
print $html_file "<h1>osmdiff by Gary68</h1>\n" ;
print $html_file "<p>\n" ;
print $html_file "<a href=\"#General Information\">General Information</a><br>\n" ;
print $html_file "<a href=\"#User Information\">User Information</a><br>\n" ;
print $html_file "<a href=\"#Deleted ways\">Deleted ways</a><br>\n" ;
print $html_file "<a href=\"#New ways\">New ways</a><br>\n" ;
print $html_file "<a href=\"#Ways with altered node count\">Ways with altered node count</a><br>\n" ;
print $html_file "<a href=\"#Ways with at least one node with changed position\">Ways with at least one node with changed position</a><br>\n" ;
print $html_file "<a href=\"#Ways with changed tags\">Ways with changed tags</a><br>\n" ;
print $html_file "<a href=\"#Deleted nodes\">Deleted nodes</a><br>\n" ;
print $html_file "<a href=\"#New nodes\">New nodes</a><br>\n" ;
print $html_file "<a href=\"#Nodes with changed position\">Nodes with changed position</a><br>\n" ;
print $html_file "<a href=\"#Nodes with changed tags\">Nodes with changed tags</a><br>\n" ;
print $html_file "</p>\n" ;

print $html_file "<a name=\"General Information\">" ;
print $html_file "<h2>General information</h2>\n" ;
print $html_file "</a>\n" ;


print $html_file "<p>Bounding Box: $left - $right // $bottom - $top</p>\n" ;

print $html_file "<table border=\"1\" width=\"100%\">\n" ;
print $html_file " <tr>\n" ;
print $html_file "  <th></th>\n" ;
print $html_file "  <th>Old</th>\n" ;
print $html_file "  <th>New</th>\n" ;
print $html_file "  <th>Difference</th>\n" ;
print $html_file " </tr>\n" ;

print $html_file "<tr><td>Files</td>\n" ;
print $html_file "<td>".$basefile_name." date: ".ctime(stat($basefile_name)->mtime)."</td>\n" ;
print $html_file "<td>".$file2_name." date: ".ctime(stat($file2_name)->mtime)."</td>\n" ;
print $html_file "<td></td>\n" ;
print $html_file "</tr>\n" ;

$d = scalar (keys %node0_lon) ;
$e = scalar (keys %node1_lon) ;
$c = $e - $d ;
print $html_file "<tr><td>Number nodes</td>\n" ;
print $html_file "<td>$d</td>\n" ;
print $html_file "<td>$e</td>\n" ;
print $html_file "<td>$c</td>\n" ;
print $html_file "</tr>\n" ;

$d = scalar (keys %way0_nodes) ;
$e = scalar (keys %way1_nodes) ;
$c = $e - $d ;
print $html_file "<tr><td>Number ways</td>\n" ;
print $html_file "<td>$d</td>\n" ;
print $html_file "<td>$e</td>\n" ;
print $html_file "<td>$c</td>\n" ;
print $html_file "</tr>\n" ;

$d = $nodetags0 ;
$e = $nodetags1 ;
$c = $e - $d ;
print $html_file "<tr><td>Number node tags</td>\n" ;
print $html_file "<td>$d</td>\n" ;
print $html_file "<td>$e</td>\n" ;
print $html_file "<td>$c</td>\n" ;
print $html_file "</tr>\n" ;

$d = $waytags0 ;
$e = $waytags1 ;
$c = $e - $d ;
print $html_file "<tr><td>Number way tags</td>\n" ;
print $html_file "<td>$d</td>\n" ;
print $html_file "<td>$e</td>\n" ;
print $html_file "<td>$c</td>\n" ;
print $html_file "</tr>\n" ;

$d = $waynodecount0 ;
$e = $waynodecount1 ;
$c = $e - $d ;
print $html_file "<tr><td>Waynode count</td>\n" ;
print $html_file "<td>$d</td>\n" ;
print $html_file "<td>$e</td>\n" ;
print $html_file "<td>$c</td>\n" ;
print $html_file "</tr>\n" ;


print $html_file "</table>\n" ;


#######################
# user stats / new node
#######################
foreach $key (keys %node1_lon) {
	if (!exists $node0_lon{$key}) {
		# check if user is new to list! init.
		if (!exists $user_num{ $node1_user{$key} } ) {
			$user_lon{ $node1_user{$key} } = 0 ;
			$user_lat{ $node1_user{$key} } = 0 ;
			$user_num{ $node1_user{$key} } = 0 ;
		}
		$user_lon{ $node1_user{$key} } += $node1_lon{$key} ;
		$user_lat{ $node1_user{$key} } += $node1_lat{$key} ;
		$user_num{ $node1_user{$key} } ++ ;
	}
}

###############################
# user stats / changed pos node
###############################
foreach $key (keys %node0_lon) {
	if ( (exists $node1_lon{$key}) and 
	( ($node0_lon{$key} != $node1_lon{$key}) or 
	($node0_lat{$key} != $node1_lat{$key}) ) ) {
		# check if user is new to list! init.
		if (!exists $user_num{ $node1_user{$key} } ) {
			$user_lon{ $node1_user{$key} } = 0 ;
			$user_lat{ $node1_user{$key} } = 0 ;
			$user_num{ $node1_user{$key} } = 0 ;
		}
		$user_lon{ $node1_user{$key} } += $node1_lon{$key} ;
		$user_lat{ $node1_user{$key} } += $node1_lat{$key} ;
		$user_num{ $node1_user{$key} } ++ ;
	}
}


########################
# print user information
########################
print $html_file "<hr />\n" ;
print $html_file "<a name=\"User Information\">" ;
print $html_file "<h2>User statistics</h2>\n" ;
print $html_file "</a>\n" ;
print $html_file "<p>Number active users: ", scalar (keys %user_num), "</p>\n" ;
print $html_file "<h3>Number new and changed nodes per user</h3>\n" ;


print $html_file "<table border=\"1\" width=\"100%\">\n" ;
print $html_file " <tr>\n" ;
print $html_file "  <th>User</th>\n" ;
print $html_file "  <th>Nodes added/changed</th>\n" ;
print $html_file " </tr>\n" ;

foreach $key (sort { $user_num{$b} <=> $user_num{$a} } keys %user_num) {
	print $html_file "<tr><td>".$key."</td><td>".$user_num{$key}."</td></tr>\n" ;
}
print $html_file "</table>\n" ;


#################
#################
# Way information
#################
#################

print $html_file "<hr />\n" ;
print $html_file "<h2>Way information</h2>\n" ;

##############
# deleted ways
##############
print $html_file "<a name=\"Deleted ways\">" ;
print $html_file "<h3>Deleted ways</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<p><span style=\"color:red\">" ;
foreach $key (keys %way0_user) {
	if (!exists $way1_user{$key}) {
		print $html_file "<strong>", weblink("way", $key), "</strong> - ", $#{$way0_nodes{$key}}, " nodes. Tags: " ;
		if (defined $way0_tags{$key}[0]) {
			for ($n=0; $n <= $#{$way0_tags{$key}}; $n++) {              
				print $html_file $way0_tags{$key}[$n], "  " ;
			}
		}
		else {
			print $html_file "no tags" ;
		}
		print $html_file "<br>\n" ;
		push (@ways_deleted, $key) ;
	}
}
print $html_file "</span></p>\n" ;

##########
# new ways
##########
print $html_file "<a name=\"New ways\">" ;
print $html_file "<h3>New ways</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<p><span style=\"color:black\">" ;
foreach $key (keys %way1_user) {
	if (!exists $way0_user{$key}) {
		print $html_file "<strong>" ;
		print $html_file weblink("way", $key), " by user ", $way1_user{$key}, "</strong>: " ;
		if (defined $way1_tags{$key}[0]) {
			for ($n=0; $n <= $#{$way1_tags{$key}}; $n++) {              
				print $html_file $way1_tags{$key}[$n], "  " ;
			}
		}
		else {
			print $html_file "no tags" ;
		}
		print $html_file "<br>\n" ;
		push (@ways_new, $key) ;
	}
}
print $html_file "</span></p>\n" ;


##########################
# ways node number changed
##########################
print $html_file "<a name=\"Ways with altered node count\">" ;
print $html_file "<h3>Ways with altered number of nodes</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<p>Tags and user listed from new way.</p>\n" ;
print $html_file "<p><span style=\"color:blue\">" ;
foreach $key (keys %way0_user) {
	if (exists $way1_user{$key}) {
		if ($#{$way0_nodes{$key}} != $#{$way1_nodes{$key}}) {
			print $html_file "<strong>", weblink("way", $key), " by user ", $way1_user{$key}, "</strong> - " ;
			print $html_file " Nodes: ", $#{$way0_nodes{$key}}, "/", $#{$way1_nodes{$key}}, " Tags: " ;
			if (defined $way1_tags{$key}[0]) {
				for ($n=0; $n <= $#{$way1_tags{$key}}; $n++) {              
					print $html_file $way1_tags{$key}[$n], "  " ;
				}
			}
			else {
				print $html_file "no tags" ;
			}
			print $html_file "<br>\n" ;
			push (@ways_changed_num_nodes, $key) ;
		}
	}

}
print $html_file "</span></p>\n" ;

#######################
# ways node pos changed
#######################
print $html_file "<a name=\"Ways with at least one node with changed position\">" ;
print $html_file "<h3>Ways where at least one node changed position</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<p><span style=\"color:blue\">" ;
foreach $key (keys %way1_user) {
	if (exists $way0_user{$key}) {   # ONLY IF OLD WAY EXISTS !!!
		my $changed = 0 ;
		for ($i = 0; $i <= $#{$way1_nodes{$key}}; $i++) {
			if ( defined ( $node0_lon{$way1_nodes{$key}[$i]} ) ) {
				if ( ( $node0_lon{$way1_nodes{$key}[$i]} != $node1_lon{$way1_nodes{$key}[$i]} ) or   
					( $node0_lat{$way1_nodes{$key}[$i]} != $node1_lat{$way1_nodes{$key}[$i]} ) ) {
					$changed = 1 ;
				}
			}
			else {
				$changed = 1 ;
			}
		}
		if ($changed) {
			print $html_file "<strong>", weblink("way", $key), "</strong> - \n" ;
			if (defined $way1_tags{$key}[0]) {
				for ($n=0; $n <= $#{$way1_tags{$key}}; $n++) {              
					print $html_file $way1_tags{$key}[$n], "  " ;
				}
			}
			else {
				print $html_file "no tags" ;
			}
			print $html_file "<br>\n" ;
			push @ways_changed_nodepos, $key ;
		}
	}
}
print $html_file "</span></p>\n" ;
print $html_file "<p>Number ways: ", $#ways_changed_nodepos + 1, "</p>\n" ;


################################
# check for changed tags in ways
################################
print $html_file "<a name=\"Ways with changed tags\">" ;
print $html_file "<h3>Ways with changed tags</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<table border=\"1\" width=\"100%\">\n" ;
print $html_file " <tr>\n" ;
print $html_file "  <th>Way</th>\n" ;
print $html_file "  <th>Last user</th>\n" ;
print $html_file "  <th>Deleted tags</th>\n" ;
print $html_file "  <th>New tags</th>\n" ;
print $html_file "  <th>All tags (new file)</th>\n" ;
print $html_file " </tr>\n" ;

foreach $key (keys %way0_nodes) {
	if (defined $way1_nodes{$key}) {
		my @tags_new ;
		my @tags_deleted ;
		my $changed = 0 ;
		my $found ;
		if ( $#{$way0_tags{$key}} > 0 ) {
			if ( $#{$way1_tags{$key}} > 0 ) {

				# check each tag in way0
				for ( $i=0; $i<=$#{$way0_tags{$key}}; $i++ ) {
					$found = 0  ;
					for ( $j=0; $j<=$#{$way1_tags{$key}}; $j++) {
						if ( $way0_tags{$key}[$i] eq $way1_tags{$key}[$j] ) {
							$found = 1 ;
						}
					}
					if ( $found == 0 ) {
						push @tags_deleted, $way0_tags{$key}[$i] ;
						$changed = 1 ;
					}
				}				

				# check each tag in way0
				for ( $i=0; $i<=$#{$way1_tags{$key}}; $i++ ) {
					$found = 0  ;
					for ( $j=0; $j<=$#{$way0_tags{$key}}; $j++) {
						if ( $way1_tags{$key}[$i] eq $way0_tags{$key}[$j] ) {
							$found = 1 ;
						}
					}
					if ( $found == 0 ) {
						push @tags_new, $way1_tags{$key}[$i] ;
						$changed = 1 ;
					}
				}				

			}
			else { # way0 = 0

				# all tags from 0 deleted
				for ( $i=0; $i<= $#{$way0_tags{$key}}; $i++ ) {
					push @tags_deleted, $way0_tags{$key}[$i] ;
					$changed = 1 ;
				}
			}
		}
		else { # way0 = 0
			if ( $#{$way1_tags{$key}} > 0 ) {

				# all tags from way1 new
				for ( $i=0; $i<= $#{$way1_tags{$key}}; $i++ ) {
					push @tags_new, $way1_tags{$key}[$i] ;
					$changed = 1 ;
				}
			}
			else # way1 = 0
			{
				# no changes
			}
		}
		if ($changed) {

			push @ways_changed_tags, $key ;

			# print row to html
			print $html_file "<tr>\n" ;
			print $html_file "<td>" . weblink("way", $key) . "</td>\n" ;
			print $html_file "<td>$way1_user{$key}</td>\n" ;


			# print deleted tags
			print $html_file "<td><p>" ;
			foreach $temp (@tags_deleted) {
				print $html_file $temp, "<br>\n" ;
			}
			print $html_file "</p></td>\n" ;

			# print new tags
			print $html_file "<td><p>" ;
			foreach $temp (@tags_new) {
				print $html_file $temp, "<br>\n" ;
			}
			print $html_file "</p></td>\n" ;

			# all tags as in new way
			print $html_file "<td><p>" ;
			for ( $i = 0; $i <= $#{$way1_tags{$key}}; $i++ ) {
				print $html_file $way1_tags{$key}[$i], "<br>\n" ;
			}
			print $html_file "</p></td>\n" ;

			print $html_file "</tr>\n" ;
		}
	}
}
print $html_file "</table>\n" ;
$i = $#ways_changed_tags + 1 ;
print $html_file "<p>", $i, " ways with changed tags.</p>\n" ; 


##################
##################
# node information
##################
##################

print $html_file "<hr />\n" ;
print $html_file "<h2>Node information</h2>\n" ;

###############
# deleted nodes
###############
print $html_file "<a name=\"Deleted nodes\">" ;
print $html_file "<h3>Deleted nodes</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<p><span style=\"color:red\">" ;
foreach $key (keys %node0_lon) {
	if (!exists $node1_lon{$key}) {
		print $html_file "<strong>" ;
		print $html_file weblink("node", $key), "</strong>: " ;
		if (defined $node0_tags{$key}[0]) {
			for ($n=0; $n <= $#{$node0_tags{$key}}; $n++) {              
				print $html_file $node0_tags{$key}[$n], "  " ;
			}
		}
		else {
			print $html_file "no tags" ;
		}
		print $html_file "<br>\n" ;
		push (@nodes_deleted, $key) ;
	}
}
print $html_file "</span></p>\n" ;

###########
# new nodes
###########
print $html_file "<a name=\"New nodes\">" ;
print $html_file "<h3>New nodes</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<p><span style=\"color:black\">" ;
foreach $key (keys %node1_lon) {
	if (!exists $node0_lon{$key}) {
		print $html_file "<strong>" ;
		print $html_file weblink("node", $key), " by user ", $node1_user{$key}, "</strong>: " ;
		if (defined $node1_tags{$key}[0]) {
			for ($n=0; $n <= $#{$node1_tags{$key}}; $n++) {              
				print $html_file $node1_tags{$key}[$n], "  " ;
			}
		}
		else {
			print $html_file "no tags" ;
		}
		print $html_file "<br>\n" ;
		push (@nodes_new, $key) ;
	}
}
print $html_file "</span></p>\n" ;


#############################
# nodes with position changed
#############################
print $html_file "<a name=\"Nodes with changed position\">" ;
print $html_file "<h3>Nodes with new position</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<p><span style=\"color:blue\">" ;
foreach $key (keys %node0_lon) {
	if ( (exists $node1_lon{$key}) and 
	( ($node0_lon{$key} != $node1_lon{$key}) or 
	($node0_lat{$key} != $node1_lat{$key}) ) ) {
		my ($dist) = sqrt ( ($node0_lon{$key}-$node1_lon{$key})**2 + ($node0_lat{$key}-$node1_lat{$key})**2 ) * 111.11 * 1000 ;
		print $html_file "<strong>" ;
		printf $html_file "%s distance = ~%.1i (m)</strong> (tags from old data): ", weblink("node", $key), $dist ;
		if (defined $node0_tags{$key}[0]) {
			for ($n=0; $n <= $#{$node0_tags{$key}}; $n++) {              
				print $html_file $node0_tags{$key}[$n], "  " ;
			}
		}
		else {
			print $html_file "no tags" ;
		}
		print $html_file "<br>\n" ;
		push (@nodes_changed_pos, $key) ;
	}
}
print $html_file "</span></p>\n" ;


#################################
# check for changed tags in nodes
#################################
print $html_file "<a name=\"Nodes with changed tags\">" ;
print $html_file "<h3>Nodes with changed tags</h3>\n" ;
print $html_file "</a>\n" ;
print $html_file "<table border=\"1\" width=\"100%\">\n" ;
print $html_file " <tr>\n" ;
print $html_file "  <th>Node</th>\n" ;
print $html_file "  <th>Last user</th>\n" ;
print $html_file "  <th>Deleted tags</th>\n" ;
print $html_file "  <th>New tags</th>\n" ;
print $html_file "  <th>All tags (new file)</th>\n" ;
print $html_file " </tr>\n" ;

foreach $key (keys %node0_lon) {
	if (defined $node1_lon{$key}) {
		my @tags_new ;
		my @tags_deleted ;
		my $changed = 0 ;
		my $found ;
		if ( $#{$node0_tags{$key}} > 0 ) {
			if ( $#{$node1_tags{$key}} > 0 ) {

				# check each tag in node0
				for ( $i=0; $i<=$#{$node0_tags{$key}}; $i++ ) {
					$found = 0  ;
					for ( $j=0; $j<=$#{$node1_tags{$key}}; $j++) {
						if ( $node0_tags{$key}[$i] eq $node1_tags{$key}[$j] ) {
							$found = 1 ;
						}
					}
					if ( $found == 0 ) {
						push @tags_deleted, $node0_tags{$key}[$i] ;
						$changed = 1 ;
					}
				}				

				# check each tag in node1
				for ( $i=0; $i<=$#{$node1_tags{$key}}; $i++ ) {
					$found = 0  ;
					for ( $j=0; $j<=$#{$node0_tags{$key}}; $j++) {
						if ( $node1_tags{$key}[$i] eq $node0_tags{$key}[$j] ) {
							$found = 1 ;
						}
					}
					if ( $found == 0 ) {
						push @tags_new, $node1_tags{$key}[$i] ;
						$changed = 1 ;
					}
				}				

			}
			else { # node0 = 0

				# all tags from 0 deleted
				for ( $i=0; $i<= $#{$node0_tags{$key}}; $i++ ) {
					push @tags_deleted, $node0_tags{$key}[$i] ;
					$changed = 1 ;
				}
			}
		}
		else { # node0 = 0
			if ( $#{$node1_tags{$key}} > 0 ) {

				# all tags from node1 new
				for ( $i=0; $i<= $#{$node1_tags{$key}}; $i++ ) {
					push @tags_new, $node1_tags{$key}[$i] ;
					$changed = 1 ;
				}
			}
			else # node1 = 0
			{
				# no changes
			}
		}
		if ($changed) {

			push @nodes_changed_tags, $key ;

			# print row to html
			print $html_file "<tr>\n" ;
			print $html_file "<td>" . weblink("node", $key) . "</td>\n" ;
			print $html_file "<td>$node1_user{$key}</td>\n" ;

			# print deleted tags
			print $html_file "<td><p>" ;
			foreach $temp (@tags_deleted) {
				print $html_file $temp, "<br>\n" ;
			}
			print $html_file "</p></td>\n" ;

			# print new tags
			print $html_file "<td><p>" ;
			foreach $temp (@tags_new) {
				print $html_file $temp, "<br>\n" ;
			}
			print $html_file "</p></td>\n" ;

			# all tags as in new node
			print $html_file "<td><p>" ;
			for ( $i = 0; $i <= $#{$node1_tags{$key}}; $i++ ) {
				print $html_file $node1_tags{$key}[$i], "<br>\n" ;
			}
			print $html_file "</p></td>\n" ;

			print $html_file "</tr>\n" ;
		}
	}
}
print $html_file "</table>\n" ;
$i = $#nodes_changed_tags + 1 ;
print $html_file "<p>", $i, " nodes with changed tags.</p>\n" ; 





print_html_foot () ;
close $html_file ;

print "finished.\n\n" ;

draw_pic () ;

$time1 = time() ;
$timespent = ($time1 - $time0);
print "Time spent: ", ($timespent/(60*60))%99, " hours, ", ($timespent/60)%60, " minutes and ", $timespent%60, " seconds\n" ;




##########################################################################################################################
##########################################################################################################################
# sub routines 
##########################################################################################################################
##########################################################################################################################


############
# READ FILES
############

sub read_file {
	my ($name, $position) = @_;
	my $file ;

	print "reading file $name...\n" ;
    if ($name =~ /\.bz2$/) {
        require PerlIO::via::Bzip2;
        open ($file, "<:via(Bzip2)", $name) ;
    } else {
        open ($file, , "<", $name) ;
    }

	while(my $line = <$file>) {
		if($line =~ /^\s*\<way/) {
			my $n = 0 ; # way nodes count
			my $m = 0 ; # way k/v count
	
			# get all needed information
			my ($id)   = ($line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/); # get way id
			my ($u) = ($line =~ /^.+user=[\'\"](.*?)[\'\"]/);       # get value // REGEX???

			if (!$u) {
				$u = "unknown" ;
			}
			
			if (!$id) {
				print "ERROR reading osm file ", $name, ", line follows (expecting way id):\n", $line, "\n" ; 
			}
	
			unless ($id) { next; }

			if ($id and $u) {
				if ($position ==0) {
					${way0_nodes{$id}} = () ;
					$way0_user{$id} = $u ;
				}
				else {
					${way1_nodes{$id}} = () ;
					$way1_user{$id} = $u ;
				}
			}

			$line = <$file> ;
			while (not($line =~ /\/way/)) { # more way data

				#get nodes and type
				my ($node) = ($line =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/); # get node id
				my ($k)   = ($line =~ /^\s*\<tag k=[\'\"]([\w_:]+)[\'\"]/); # get key
				#my ($v) = ($line =~ /^.+v=[\'\"]([-\w\d\s\.\-]+)[\'\"]/);       # get value // REGEX???
				my ($v) = ($line =~ /v=[\'\"](.*)[\'\"]/) ;

				if (!(($node) or ($k and defined($v) ))) {
					print "WARNING reading osm file ", $name, ", line follows (expecting node or k/v for way):\n", $line, "\n" ; 
				}
			
				if ($node) {
					#add node to way data
					if ($position ==0) {
						$way0_nodes{$id}[$n] = $node ;
						$way0_user{$id} = $u ;
						$waynodecount0++ ;
					}
					else {
						$way1_nodes{$id}[$n] = $node ;
						$way1_user{$id} = $u ;
						$waynodecount1++ ;
					}
					$n++ ;
				}

				#get tags but not CREATED_BY
				if ($k and defined($v)) {
					if ($k ne "created_by") {
						#print "k/v; ", $k, " ", $v, "\n" ;
						if ($position == 0) {
							$way0_tags{$id}[$m] = $k . $delimiter . $v ;
							$waytags0++ ;
						}
						else {
							$way1_tags{$id}[$m] = $k . $delimiter . $v ;
							$waytags1++ ;
						}
						$m++ ;
					}
				}
				$line = <$file> ;
			}
		}

		if($line =~ /^\s*\<node/) {
			# get all needed information
			my ($id)   = ($line =~ /^\s*\<node id=[\'\"](\d+)[\'\"]/); # get node id
			my ($lon) = ($line =~ /^.+lon=[\'\"]([-\d,\.]+)[\'\"]/);    # get position
			my ($lat) = ($line =~ /^.+lat=[\'\"]([-\d,\.]+)[\'\"]/);    # get position
			my ($u) = ($line =~ /^.+user=[\'\"](.*?)[\'\"]/);       # get value // REGEX???

			my ($i) = 0 ;
			my $place = 0 ;
			my $name = "" ;

			if (!$u) {
				$u = "unknown" ;
			}

			if (!$id or !$lat or !$lon) {
				print "WARNING reading osm file ", $name, ", line follows (expecting id, lon, lat and user for node):\n", $line, "\n" ; 
			}

			unless ($id) { next; }
			unless ($lat) { next; }
			unless ($lon) { next; }
			#unless ($u) { next; }

			#store node
			if ($position == 0) {
				$node0_lon {$id} = $lon ;
				$node0_lat {$id} = $lat ;
				$node0_user {$id} = $u ;
			}
			else {
				$node1_lon {$id} = $lon ;
				$node1_lat {$id} = $lat ;
				$node1_user {$id} = $u ;
			}
			
 			if (grep (/">/, $line)) {                  # more lines, get tags
				$line = <$file> ;
				while (!grep(/<\/node>/, $line)) {
					my ($k) = ($line =~ /^\s*\<tag k=[\'\"]([\w_:]+)[\'\"]/);   # get key
					#my ($v) = ($line =~ /^.+v=[\'\"]([\/\w\d\s\.:,\(\)]+)[\'\"]/);   # get value REGEX???
					my ($v) = ($line =~ /v=[\'\"](.*)[\'\"]/) ;


					#get tags but not CREATED_BY
					if ($k and defined($v) ) {
						if ($k ne "created_by") {
							if ($position == 0) {
								$node0_tags{$id}[$i] = $k . $delimiter . $v ;
								$nodetags0++ ;
								if ( ($k eq "place") and (($v eq "city") or ($v eq "town") or ($v eq "suburb") or ($v eq "village")) ) {
									$place = 1 ;
								}
								if ($k eq "name") {

									# REPLACE UMLAUTE
									#print $v, " " ;
									#utf8::decode ($v) ;
									#$v =~ s/ä/ae/g ;
									#$v =~ s/\148/oe/g ;
									#$v =~ s/ü/ue/g ;
									#$v =~ s/ß/ss/g ;
									#$v =~ s/Ã¤/ae/g;
									#$v =~ s/Ã„/Ae/g;
									#$v =~ s/Ã¶/oe/g;
									#$v =~ s/Ã¼/ue/g;
									#$v =~ s/Ãœ/Ue/g;
									#$v =~ s/ÃŸ/ss/g;
									#$v =~ s/Ã/Oe/g;
									$name = $v ;
									#print $name, "\n" ;
								}
							}
							else {
								$node1_tags{$id}[$i] = $k . $delimiter . $v ;
								$nodetags1++ ;
							}
							$i++ ;
						}
					}
					else {
						print "WARNING reading osm file ", $name, ", line follows (expecting k/v for node):\n", $line, "\n" ; 
					}
					$line = <$file> ;
				}
			}
			if ( ($place == 1) and ($name ne "") ) {
				#print "place found: $id $name $lon $lat\n" ;
				$place_name{$id} = $name ;
				$place_lat{$id} = $lat ;
				$place_lon{$id} = $lon ;
			}
		}
	}
	close $file ;
	print "finished.\n" ;
} #sub


######
# HTML
######

sub print_html_header {
	print $html_file "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"";
	print $html_file "  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
	print $html_file "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n";
	print $html_file "<head><title>osmdiff by Gary68</title>\n";
	print $html_file "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n";
	print $html_file "</head>\n<body>\n";
}
sub print_html_foot {
	print $html_file "</body>\n</html>\n" ;
}


##############
##############
# draw picture
##############
##############

sub draw_pic {

	my $color ;

	##########
	# init pic
	##########
	$pic_size_y = $pic_size_x * ($top - $bottom) / ($right - $left) / cos ($top/360*3.14*2) ;
	my $image = new GD::Image($pic_size_x, $pic_size_y);
	$image->trueColor() ;
	print "drawing picture...\n" ;
	printf "pic size: %i by %i\n", $pic_size_x, $pic_size_y ;

	# create colors
	my $background = $image->colorAllocate(200, 200, 200) ;
	my $deleted = $image->colorAllocate(255, 0, 0) ;
	my $new = $image->colorAllocate(0, 0, 0) ; #black
	my $changed = $image->colorAllocate(0, 0, 255) ;
	my $changedtags = $image->colorAllocate(255, 165, 0) ; # orange
	my $black = $image->colorAllocate(0, 0, 0) ;
	my $white = $image->colorAllocate(255, 255, 255) ;
	my $bg_bab = $image->colorAllocate(24,116,205) ;
	my $bg_water = $image->colorAllocate(0,191,255) ;
	my $bg_primary = $image->colorAllocate(238,92,66) ;

	$image->filledRectangle(0,0,$pic_size_x-1,$pic_size_y-1,$white) ;

	###############
	# draw old ways
	###############
	foreach $key (keys %way0_nodes) {
		if ( $#{$way0_nodes{$key}} > 0 ) {
			$color = $background ;
			if ($colors eq "C") {
				if ( defined ($way0_tags{$key}) ) {
					for ($i=0; $i<= $#{$way0_tags{$key}}; $i++ ) {
						if ( grep (/trunk/, $way0_tags{$key}[$i]) ) { $color = $bg_bab ; } 
						if ( grep (/motorway/, $way0_tags{$key}[$i]) ) { $color = $bg_bab ; } 
						if ( grep (/waterway/, $way0_tags{$key}[$i]) ) { $color = $bg_water ; } 
						if ( grep (/primary/, $way0_tags{$key}[$i]) ) { $color = $bg_primary ; } 
					}
				}
			}
			for ($i=0 ; $i < $#{$way0_nodes{$key}}; $i++) {
				if (      (defined($node0_lon{$way0_nodes{$key}[$i]})) and (defined($node0_lon{ $way0_nodes{$key}[$i+1]}))   ) {
					#print "\n\nWAY: ", $key, " ", $i, "\n" ;
					my ($x1) = int( ($node0_lon{ $way0_nodes{$key}[$i]  } - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y1) = $pic_size_y - int( ($node0_lat{$way0_nodes{$key}[$i]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					my ($x2) = int( ($node0_lon{$way0_nodes{$key}[$i+1]} - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y2) = $pic_size_y - int( ($node0_lat{$way0_nodes{$key}[$i+1]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					#print $x1, " ", $y1, " ", $x2, " ", $y2, "\n" ;


					$image->line($x1,$y1,$x2,$y2,$color) ;
				}
				else
				{
					print "ERROR: basefile way $key, node $way0_nodes{$key}[$i] or $way0_nodes{$key}[$i+1] missing\n" ;
				}
			}
		}
	}
	print "background drawn.\n" ;

	$image->setThickness($thickness) ;

	###################
	# draw deleted ways
	###################
	if ($thickness >= 2) {
		$image->setThickness($thickness) ;
	}
	else
	{
		$image->setThickness(2) ;
	}
	foreach $key (@ways_deleted) {
		if ( $#{$way0_nodes{$key}} > 0 ) {
			for ($i=0 ; $i < $#{$way0_nodes{$key}}; $i++) {
				if (      (defined($node0_lon{$way0_nodes{$key}[$i]})) and (defined($node0_lon{ $way0_nodes{$key}[$i+1]}))   ) {
					#print "WAY: ", $key, " ", $i, "\n" ;
					my ($x1) = int( ($node0_lon{ $way0_nodes{$key}[$i]  } - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y1) = $pic_size_y - int( ($node0_lat{$way0_nodes{$key}[$i]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					my ($x2) = int( ($node0_lon{$way0_nodes{$key}[$i+1]} - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y2) = $pic_size_y - int( ($node0_lat{$way0_nodes{$key}[$i+1]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					#print $x1, " ", $y1, " ", $x2, " ", $y2, "\n" ;
					$image->line($x1,$y1,$x2,$y2,$deleted) ;
				}
				else
				{
					print "ERROR: basefile way $key, node $way0_nodes{$key}[$i] or $way0_nodes{$key}[$i+1] missing\n" ;
				}
			}
		}
	}
	$image->setThickness($thickness) ;
	print "deleted ways drawn.\n" ;

	###############
	# draw new ways
	###############
	foreach $key (@ways_new) {
		if ( $#{$way1_nodes{$key}} > 0 ) {
			for ($i=0 ; $i < $#{$way1_nodes{$key}}; $i++) {
				if (      (defined($node1_lon{$way1_nodes{$key}[$i]})) and (defined($node1_lon{ $way1_nodes{$key}[$i+1]}))   ) {
					#print "WAY: ", $key, " ", $i, "\n" ;
					my ($x1) = int( ($node1_lon{ $way1_nodes{$key}[$i]  } - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y1) = $pic_size_y - int( ($node1_lat{$way1_nodes{$key}[$i]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					my ($x2) = int( ($node1_lon{$way1_nodes{$key}[$i+1]} - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y2) = $pic_size_y - int( ($node1_lat{$way1_nodes{$key}[$i+1]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					#print $x1, " ", $y1, " ", $x2, " ", $y2, "\n" ;
					$image->line($x1,$y1,$x2,$y2,$new) ;
				}
				else
				{
					print "ERROR: file2 way $key, node $way1_nodes{$key}[$i] or $way1_nodes{$key}[$i+1] missing\n" ;
				}
			}
		}
	}
	print "new ways drawn.\n" ;


	##########################
	# draw changed ways / tags
	##########################
	foreach $key (@ways_changed_tags) {
		if ( $#{$way1_nodes{$key}} > 0 ) {
			for ($i=0 ; $i < $#{$way1_nodes{$key}}; $i++) {
				if (      (defined($node1_lon{$way1_nodes{$key}[$i]})) and (defined($node1_lon{ $way1_nodes{$key}[$i+1]}))   ) {
					#print "WAY: ", $key, " ", $i, "\n" ;
					my ($x1) = int( ($node1_lon{ $way1_nodes{$key}[$i]  } - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y1) = $pic_size_y - int( ($node1_lat{$way1_nodes{$key}[$i]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					my ($x2) = int( ($node1_lon{$way1_nodes{$key}[$i+1]} - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y2) = $pic_size_y - int( ($node1_lat{$way1_nodes{$key}[$i+1]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					#print $x1, " ", $y1, " ", $x2, " ", $y2, "\n" ;
					$image->line($x1,$y1,$x2,$y2,$changedtags) ;
				}
				else
				{
					print "ERROR: file2 way $key, node $way1_nodes{$key}[$i] or $way1_nodes{$key}[$i+1] missing\n" ;
				}
			}
		}
	}
	print "ways with changed tags drawn.\n" ;

	##################################
	# draw changed ways / number nodes
	##################################
	foreach $key (@ways_changed_num_nodes) {
		if ( $#{$way1_nodes{$key}} > 0 ) {
			for ($i=0 ; $i < $#{$way1_nodes{$key}}; $i++) {
				if (      (defined($node1_lon{$way1_nodes{$key}[$i]})) and (defined($node1_lon{ $way1_nodes{$key}[$i+1]}))   ) {
					#print "WAY: ", $key, " ", $i, "\n" ;
					my ($x1) = int( ($node1_lon{ $way1_nodes{$key}[$i]  } - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y1) = $pic_size_y - int( ($node1_lat{$way1_nodes{$key}[$i]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					my ($x2) = int( ($node1_lon{$way1_nodes{$key}[$i+1]} - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y2) = $pic_size_y - int( ($node1_lat{$way1_nodes{$key}[$i+1]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					#print $x1, " ", $y1, " ", $x2, " ", $y2, "\n" ;
					$image->line($x1,$y1,$x2,$y2,$changed) ;
				}
				else
				{
					print "ERROR: file2 way $key, node $way1_nodes{$key}[$i] or $way1_nodes{$key}[$i+1] missing\n" ;
				}
			}
		}
	}
	print "ways with different nodecount drawn.\n" ;

	###########################################
	# draw changed ways / position node changed
	###########################################
	foreach $key (@ways_changed_nodepos) {
		if ( $#{$way1_nodes{$key}} > 0 ) {
			for ($i=0 ; $i < $#{$way1_nodes{$key}}; $i++) {
				if (      (defined($node1_lon{$way1_nodes{$key}[$i]})) and (defined($node1_lon{ $way1_nodes{$key}[$i+1]}))   ) {
					#print "WAY: ", $key, " ", $i, "\n" ;
					my ($x1) = int( ($node1_lon{ $way1_nodes{$key}[$i]  } - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y1) = $pic_size_y - int( ($node1_lat{$way1_nodes{$key}[$i]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					my ($x2) = int( ($node1_lon{$way1_nodes{$key}[$i+1]} - $left) / ($right - $left) * $pic_size_x ) ;
					my ($y2) = $pic_size_y - int( ($node1_lat{$way1_nodes{$key}[$i+1]} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
					#print $x1, " ", $y1, " ", $x2, " ", $y2, "\n" ;
					$image->line($x1,$y1,$x2,$y2,$changed) ;
				}
				else
				{
					print "ERROR: file2 way $key, node $way1_nodes{$key}[$i] or $way1_nodes{$key}[$i+1] missing\n" ;
				}
			}
		}
	}
	print "ways with nodes with changed position drawn.\n" ;


	#######################################
	# draw new nodes (only those with tags)
	#######################################
	foreach $key (@nodes_new) {
		if ($#{$node1_tags{$key}} > 0) {
			my ($x1) = int( ($node1_lon{$key} - $left) / ($right - $left) * $pic_size_x ) ;
			my ($y1) = $pic_size_y - int( ($node1_lat{$key} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
			$image->filledEllipse($x1, $y1, $node_diameter, $node_diameter, $new) ;		
		}
	}
	print "new nodes drawn (only those with tags).\n" ;


	####################
	# draw deleted nodes
	#################### 
	foreach $key (@nodes_deleted) {
		my ($x1) = int( ($node0_lon{$key} - $left) / ($right - $left) * $pic_size_x ) ;
		my ($y1) = $pic_size_y - int( ($node0_lat{$key} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
		$image->filledEllipse($x1, $y1, $node_diameter, $node_diameter, $deleted) ;		
	}
	print "deleted nodes drawn.\n" ;

	########################
	# draw changed nodes pos
	########################
	foreach $key (@nodes_changed_pos) {
		my ($x1) = int( ($node0_lon{$key} - $left) / ($right - $left) * $pic_size_x ) ;
		my ($y1) = $pic_size_y - int( ($node0_lat{$key} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
		$image->filledEllipse($x1, $y1, $node_diameter, $node_diameter, $changed) ;		
	}
	print "nodes with changed position drawn.\n" ;

	#########################
	# draw changed nodes tags
	#########################
	foreach $key (@nodes_changed_tags) {
		my ($x1) = int( ($node0_lon{$key} - $left) / ($right - $left) * $pic_size_x ) ;
		my ($y1) = $pic_size_y - int( ($node0_lat{$key} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
		$image->ellipse($x1, $y1, $node_diameter+5, $node_diameter+5, $changedtags) ;		
	}
	print "nodes with changed tags drawn.\n" ;

	#############
	# draw places
	#############
	foreach $key (keys %place_name) {
		my ($x1) = int( ($place_lon{$key} - $left) / ($right - $left) * $pic_size_x ) ;
		my ($y1) = $pic_size_y - int( ($place_lat{$key} - $bottom) / ($top - $bottom) * $pic_size_y ) ;
		#$image->string(gdSmallFont, $x1, $y1, $place_name{$key}, $black) ;
		$image->string(gdSmallFont, $x1, $y1, encode("iso-8859-1", decode("utf8", $place_name{$key})), $black) ;
	}
	print "places drawn.\n" ;


	#####################
	# draw user positions
	#####################
	foreach $key (keys %user_lat) {
		my ($x) = $user_lon{$key} / $user_num{$key} ;
		my ($y) = $user_lat{$key} / $user_num{$key} ;
		my ($x1) = int( ($x - $left) / ($right - $left) * $pic_size_x ) ;
		my ($y1) = $pic_size_y - int( ($y - $bottom) / ($top - $bottom) * $pic_size_y ) ;
		#$image->string(gdSmallFont, $x1, $y1, $key, $changed) ;
		$image->string(gdSmallFont, $x1, $y1, encode("iso-8859-1", decode("utf8", $key)), $changed) ;
	}

	#############
	# draw legend
	#############
	$image->string(gdSmallFont, 20, 20, "data by www.openstreetmap.org", $black) ;
	$image->string(gdMediumBoldFont, 20, ($pic_size_y-100), "New", $new) ;
	$image->string(gdMediumBoldFont, 20, ($pic_size_y-80), "Deleted", $deleted) ;
	$image->string(gdMediumBoldFont, 20, ($pic_size_y-60), "Changed Position", $changed) ;
	$image->string(gdMediumBoldFont, 20, ($pic_size_y-40), "Changed Tags", $changedtags) ;
	$image->string(gdSmallFont, 20, ($pic_size_y-20), "Gary68's osmdiff "
	.$version." - ".$basefile_name." ".ctime(stat($basefile_name)->mtime)." // ".$file2_name." ".ctime(stat($file2_name)->mtime), $black) ;
	print "legend drawn.\n" ;

	############
	# draw ruler
	############
	my $B ;
	my $B2 ;
	my $L ;
	my $Lpix ;
	my $x ;
	my $text ;
	my $rx = $pic_size_x - 20 ;
	my $ry = 20 ;
	
	$B = $right - $left ; 				# in degrees
	$B2 = $B * cos ($top/360*3.14*2) * 111.1 ;	# in km
	#print $rx, " ", $ry, "\n" ;
	#print "B= ", $B, "\n" ;
	#print "B2= ", $B2, "\n" ;
	$text = "100m" ; $x = 0.1 ;			# default length ruler
	if ($B2 > 5) {$text = "500m" ; $x = 0.5 ; }	# enlarge ruler
	if ($B2 > 10) {$text = "1km" ; $x = 1 ; }
	if ($B2 > 50) {$text = "5km" ; $x = 5 ; }
	if ($B2 > 100) {$text = "10km" ; $x = 10 ; }
	#print "x / Text ", $x, " / ", $text, "\n" ;
	$L = $x / (cos ($top/360*3.14*2) * 111.1 ) ;	# length ruler in km
	#print "L= ", $L, "\n" ;
	$Lpix = $L / $B * $pic_size_x ;			# length ruler in pixels
	#print "Lpix= ", $Lpix, "\n" ;
	#print $rx-$Lpix, " ", $ry, " ", $rx, " ", $ry, "\n" ;
	$image->line($rx-$Lpix,$ry,$rx,$ry,$black) ;
	$image->line($rx-$Lpix,$ry,$rx-$Lpix,$ry+10,$black) ;
	$image->line($rx,$ry,$rx,$ry+10,$black) ;
	$image->line($rx-$Lpix/2,$ry,$rx-$Lpix/2,$ry+5,$black) ;
	$image->string(gdSmallFont, $rx-$Lpix, $ry+15, $text, $black) ;

	############
	# write file
	############
	open ($pic_file, ">", $pic_name) ;
	binmode $pic_file ;
	print $pic_file $image->png ; 
	close $pic_file ;
	print "finished.\n\n" ;
} # draw_pic


##############
# history link
##############

sub weblink {
	my ($type, $key) = @_;
	return "<a href=\"http://www.openstreetmap.org/browse/$type/$key/history\">$key</a>";
}
