#
# program bugs.pl by Gary68
#
# program takes definition file and queries openstreetbugs online for bugs in certain area.
# output is a gpx file which can be opened with JOSM or Mapsource i.e. and an html file for online viewing.
# Also data is provided as Garmin POI csv file.
# html includes links to JOSM, OSM, OSB.
#
# definition file in xml:
#
# <xml>
#  <k="top" v="50.2">
#  <k="bottom" v="50.0">
#  <k="left" v="9.0">
#  <k="right" v="9.2">
#  <k="step" v="0.1">       # can be omitted. default is 0.1 degrees.
#  <k="delay" v="0.6">      # can be omitted, default is 1.0 secs. should not be less than 0.5
#  <k="user" v="-">         # can be specified if only bugs of a special user are desired
#  <k="status" v="-">       # can be omitted. if only status OPEN or CLOSED is desired, specify so.
# </xml>
#
# openstreetbugs has some limitations: it will return at most 80 bugs per query where lots of bugs returned 
# can be outside the specified bounding box. therefore a hash is built to eliminate dupes. furthermore
# the infrastructure where OSB runs does not accept unlimited queries per second. to be precise according to my 
# information 2 queries per second should be ok but not more. to be sure I recommend 0.6 secs.
# 

use strict ;
use warnings ;
use LWP::Simple;

my $version = "1.0" ;
my $usage = "bugs.pl def.xml name.gpx name.htm poi.csv" ;

my $left   =  99 ;
my $right  =  99 ;
my $bottom =  99 ;
my $top    =  99 ;
my $josma = 0.004 ; 		# area calc for josm link
my $specialuser = "-" ;
my $specialstatus = "-" ;
my $step = 0.1 ;
my $wait = 1.0 ;

my $key ;
my $i ;
my $status_text ;
my $errors = 0 ;
my $gets = 0 ; 
my $url ;
my $a1 ;
my $b1 ;

my $tmp_file_name = "tmp.txt" ;
my $gpx_file ;
my $def_file ;
my $html_file ;
my $html ;
my $tmp_file ;
my $poi_file ;

my %lon ;
my %lat ;
my %text ;
my %open ;
my %user ;

#########################
# get cmd line parameters
#########################

my $def_name = shift||'';
if (!$def_name)
{
	die (print $usage, "\n");
}

my $gpx_name = shift||'';	# gpx output file name
if (!$gpx_name)
{
	die (print $usage, "\n");
}

my $html_name = shift||'';	# html output file name
if (!$html_name)
{
	die (print $usage, "\n");
}

my $poi_name = shift||'';	# poi output file name
if (!$poi_name)
{
	die (print $usage, "\n");
}

print "\nOSB bugs list by Gary68\n" ;
print "Definition file: $def_name\n" ;
print "GPX file: $gpx_name\n" ;
print "HTML file: $html_name\n\n" ;

##################
# read definitions
##################

print "Read definitions...\n" ;
open ($def_file, , "<", $def_name) or die "definition file not found" ;

while (my $line = <$def_file>) {
	#print "read line: ", $line, "\n" ;
	my ($k)   = ($line =~ /^\s*<k=[\'\"]([:\w\s\d]+)[\'\"]/); # get key
	my ($v) = ($line =~ /^.+v=[\'\"]([:\w\s\d\.\-]+)[\'\"]/);       # get value
	
	if ($k and $v) {
		#print "key: ", $k, "\n" ;
		#print "val: ", $v, "\n" ;
		if ($k eq "top") {
			$top = $v ;
		}
		if ($k eq "bottom") {
			$bottom = $v ;
		}
		if ($k eq "left") {
			$left = $v ;
		}
		if ($k eq "right") {
			$right = $v ;
		}
		if ($k eq "step") {
			$step = $v ;
		}
		if ($k eq "delay") {
			$wait = $v ;
		}
		if ($k eq "user") {
			$specialuser = $v ;
		}
		if ($k eq "status") {
			$specialstatus = $v ;
		}

	}
}
close $def_file ;

print "done.\n\n" ;
print "Left: $left\n" ;
print "Right: $right\n" ;
print "Bottom: $bottom\n" ;
print "Top: $top\n" ;
print "Step: $step\n" ;
print "Delay: $wait\n" ;
print "User: $specialuser\n" ;
print "Status: $specialstatus\n\n" ;

if (($left == 99) or ($right == 99) or ($bottom == 99) or ($top == 99)) {
	print STDERR "ERROR: at least one bounding parameter not specified\n" ;
	die () ;
}


################################
# send queries to openstreetbugs
# and append result to tmp file
################################

open ($tmp_file, , ">", $tmp_file_name) ;

for ($a = $left; $a <= $right; $a += $step) {
	for ($b = $bottom; $b <= $top; $b += $step) {
		$a1 = $a + $step ;
		$b1 = $b + $step ;

		print "get tile: ", $a, "/", $b, "\n" ;		

		$url = 'http://openstreetbugs.appspot.com/getBugs?b=' . $b . '&t=' . $b1 . '&l=' . $a . '&r=' . $a1 ;
		# print $url, "\n" ;
		$gets++ ;

		my $content = get $url;
		if (!defined $content) {
			print "ERROR: error receiving tile result\n" ;
			$errors ++ ;
		}
		print $tmp_file $content ;
		sleep $wait ;
	}
}
print $gets, " calls total\n" ;
print "Errors while getting tile informartion: ", $errors, "\n\n" ;
close $tmp_file ;


###############################
# get information from tmp file
###############################

open ($tmp_file, , "<", $tmp_file_name) ;
while(my $line = <$tmp_file>) {
	my ($id)   = ($line =~ /^putAJAXMarker\((\d+),/) ;
	my ($text)   = ($line =~ /^.*\"([-\w\W\d\D\s\']+)\"/) ;
	my ($user)   = ($line =~ /^.*\[([-\w\W\d\D\s\']+)\]/) ;
	my ($lon, $lat) = ($line =~ /,([-]?[\d]+\.[\d]+),([-]?[\d]+\.[\d]+)/);
	my ($open)   = ($line =~ /.*(\d)\);$/) ;

	if (!$user) { $user = "-" ; }

	#print $id, " - " ;
	#print $user, " - " ;
	#print $lon, " - " ;
	#print $lat, " - " ;
	#print $open, " - " ;
	#print $text, "\n" ;

	$text =~ s/<hr \/>/:::/g ;  # replace <HR /> horizontal rulers by ":::"
	$lon{$id} = $lon;
	$lat{$id} = $lat ;
	$text{$id} = $text ;
	$open{$id} = $open ;
	$user{$id} = $user ;
}
close $tmp_file ;


###################
# print gpx outfile
###################

open ($gpx_file, , ">", $gpx_name) ;
print $gpx_file "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" ;
print $gpx_file "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"Gary68script\" version=\"1.1\"\n" ;
print $gpx_file "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" ;
print $gpx_file "    xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" ;


foreach $key (keys %lon) {
	if ($open{$key} == 0 ) {
		$status_text = "OPEN" ;
	}
	else
	{
		$status_text = "CLOSED" ;
	}

	if ( ( ($user{$key} eq $specialuser) or ($specialuser eq "-") ) and ( ($status_text eq $specialstatus) or ($specialstatus eq "-") ) ) {
		#print $key, " ", $user{$key}, " ", $open{$key}, " ", $lon{$key}, " ", $lat{$key}, " ", $text{$key}, "\n" ;
		print $gpx_file "<wpt lat=\"", $lat{$key}, "\" lon=\"", $lon{$key}, "\">" ;
		print $gpx_file "<desc>", $status_text, " ", $text{$key}, "</desc></wpt>\n" ;
	}
}
print $gpx_file "</gpx>\n" ;
close $gpx_file ;


#################
# write html file
#################

open ($html, , ">", $html_name) ;
print $html "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"";
print $html "  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
print $html "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n";
print $html "<head><title>Openstreetbugs list</title>\n";
print $html "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n";
print $html "</head>\n<body>\n";

print $html "<H1>OSB list by Gary68</H1>\n" ;
print $html "<p>Program version: ", $version, "</p>\n" ;
print $html "<p>Definition file: ", $def_name, "</p>\n" ;
print $html "<H2>Parameters and error report</h2>\n" ;
print $html "<p>", $left, " - ", $right, " // ", $bottom, " - ", $top, "</p>\n" ;
print $html "<p>Special User: ", $specialuser, "</p>\n" ;
print $html "<p>Special Status: ", $specialstatus, "</p>\n" ;
print $html "<p><strong>Errors while getting tile informartion: ", $errors, "</strong></p>\n" ;

print $html "<H2>Data</H2>\n";
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>Bug Id</th>\n" ;
print $html "<th>User</th>\n" ;
print $html "<th>Status</th>\n" ;
print $html "<th>Text</th>\n" ;
print $html "<th>Links</th>\n" ;
print $html "</th>\n" ;

$i = 0 ;
foreach $key (keys %lon) {
	if ($open{$key} == 0 ) {
		$status_text = "OPEN" ;
	}
	else
	{
		$status_text = "CLOSED" ;
	}

	if ( ( ($user{$key} eq $specialuser) or ($specialuser eq "-") ) and ( ($status_text eq $specialstatus) or ($specialstatus eq "-") ) ) {
		$i += 1 ;
		print $html "<tr>\n" ;

		# line, id, user
		print $html "<td>", $i, "</td>\n" ;
		print $html "<td>", $key, "</td>\n" ;
		print $html "<td>", $user{$key}, "</td>\n" ;

		#status
		print $html "<td>", $status_text, "</td>\n" ;

		# text
		print $html "<td>", $text{$key}, "</td>\n" ;

		#osm osb josm
		print $html "<td>" ;
		print $html "<A HREF=\"http://www.openstreetmap.org/?mlat=", $lat{$key}, 
			"&mlon=", $lon{$key},"&zoom=17\">OSM</A> " ;
		print $html "<A HREF=\"http://openstreetbugs.appspot.com/?lon=", $lon{$key}, 
			"&lat=", $lat{$key},"&zoom=17\">OSB </A> " ;
		print $html "<A HREF=\"http://localhost:8111/load_and_zoom?" ;
		print $html "left=", $lon{$key} - $josma ;
		print $html "&right=", $lon{$key} + $josma ;
		print $html "&top=", $lat{$key} + $josma ;
		print $html "&bottom=", $lat{$key} - $josma ;
		print $html "\">JOSM</a></td>\n" ;
		print $html "</td>" ;

		print $html "</tr>\n" ;
	}
}
print $html "</table>\n" ;
print $html "<p>$i bugs listed</p>\n" ;
print $html "</body>\n</html>\n" ;
close $html ;


###################
# print poi outfile
###################

open ($poi_file, , ">", $poi_name) ;

foreach $key (keys %lon) {
	if ($open{$key} == 0 ) {
		$status_text = "OPEN" ;
	}
	else
	{
		$status_text = "CLOSED" ;
	}

	if ( ( ($user{$key} eq $specialuser) or ($specialuser eq "-") ) and ( ($status_text eq $specialstatus) or ($specialstatus eq "-") ) ) {
		#print $key, " ", $user{$key}, " ", $open{$key}, " ", $lon{$key}, " ", $lat{$key}, " ", $text{$key}, "\n" ;
		print $poi_file $lon{$key}, ",", $lat{$key}, ",\"", $text{$key}, "\"\n" ;
	}
}
close $poi_file ;
