use strict ;
use warnings ;

use OSM::osm 4.8 ;
use OSM::osmgraph 2.0 ;


my @test = qw (2x doppelt übereinander altglas altkleider recycling 3x dreifach bank beschriftung bridge tunnel briefkasten haltestelle cafe campingplatz restaurant parkplatz apotheke kindergarten feuerwehr schule sportplatz telefon hotel imbiss kapelle spielplatz parkhaus rathaus schwimmbad sparkasse stadthalle supermarkt tankstelle volksbank wegweiser zebrastreifen bäckerei bahnübergang brücke überlagert gaststätte parkplätze überwachungskamera) ;

@test = (@test, "2 mal", "brücke oder tunnel", "bridge or tunnel", "burger king", "mc donalds", "mc donald's", "bus stop", "post ") ; 

my $programName = "osbreport.pl" ;
my $usage = "osbreport.pl osb.gpx out.htm" ; 
my $version = "1.1" ;

my $count = 0 ;

my $gpxName ; 
my $htmlName ;

my $gpxFile ;
my $html ;

my $time0 ; my $time1 ;

# get parameter

$gpxName = shift||'';
if (!$gpxName)
{
	die (print $usage, "\n");
}

$htmlName = shift||'';
if (!$htmlName)
{
	die (print $usage, "\n");
}

print "\n$programName $version for file $gpxName\n" ;

$time0 = time() ;

print "texts to check: \n" ; 
foreach my $t (sort @test) { print "- $t\n" ; }
print "\n" ; 


open ($html, ">", $htmlName) or die ("can't open html output file\n") ;
printHTMLiFrameHeader ($html, "OSB report by Gary68") ;
print $html "<H1>OSB report by Gary68</H1>\n" ;
print $html "<p>Version ", $version, "</p>\n" ;
print $html "<H2>Info</H2>\n" ;
print $html "<p>", stringFileInfo ($gpxName), "<br>\n" ;
print $html "<H3>check texts:</H3>\n<p>" ;
foreach my $t (sort @test) { print $html "- $t<br>\n" ; }
print $html "</p>\n" ; 

print $html "<H2>OSB entries</H2>\n" ;
print $html "<p>At the given location a bug has been reported that potentially can be corrected remotely. Please correct errors and report back to OSB!</p>" ;
print $html "<table border=\"1\">\n";
print $html "<tr>\n" ;
print $html "<th>Line</th>\n" ;
print $html "<th>OSB link</th>\n" ;
print $html "<th>OSM link</th>\n" ;
print $html "<th>JOSM link</th>\n" ;
print $html "<th>Text</th>\n" ;
print $html "</tr>\n" ;

my $htmlLine = 0 ;



# PROCESS GPX FILE

my $success ;
$success = open ($gpxFile, "<", $gpxName) ;
my $line ;
if ($success) {
	print "\nprocessing file: $gpxName\n" ;
	while ($line = <$gpxFile>) {
		if (grep /<wpt/, $line) {
			my ($lon) = ($line =~ /^\.*\<wpt lon=[\'\"]([-\d,\.]+)[\'\"]/) ;
			my ($lat) = ($line =~ /^.*\lat=[\'\"]([-\d,\.]+)[\'\"]/) ;
			my ($desc) = ($line =~ /<desc>(.+)<\/desc>/) ;
			if ( (defined $lon) and (defined $lat) and (defined $desc) ) {
				$desc =~ s/<!\[CDATA\[// ;
				$desc =~ s/\]\]>// ;
				my $found = 0 ;
				foreach my $t (@test) {
					if ( grep /$t/i, $desc) { $found = 1 ; }
				}
				if ($found) {
					$count++ ;
					$htmlLine++ ;
					# print "$desc\n" ;
					print $html "<tr>\n" ;
					print $html "<td>", $htmlLine , "</td>\n" ;
					print $html "<td>", osmLink ($lon, $lat, 16) , "</td>\n" ;
					print $html "<td>", osbLink ($lon, $lat, 16) , "</td>\n" ;
					print $html "<td>", josmLinkDontSelect ($lon, $lat, 0.01), "</td>\n" ;
					print $html "<td>", $desc , "</td>\n" ;
					print $html "</tr>\n" ;
				}
			}
		}
	}
	close ($gpxFile) ;
}
else {
	print "\nNOT processing file: $gpxName\n" ;
}


print $html "</table>\n" ;
print $html "<p>$htmlLine lines total</p>\n" ;

$time1 = time() ;
print "$programName finished after ", stringTimeSpent ($time1-$time0), "\n\n" ;
print $html "<p>$programName finished after ", stringTimeSpent ($time1-$time0), "</p>\n" ;


printHTMLFoot ($html) ;

close ($html) ;


print "\n$count entries found.\n\n" ;



