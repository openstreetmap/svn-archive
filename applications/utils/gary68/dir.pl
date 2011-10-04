# 
# PERL by gary68
#
#
#
#
# Copyright (C) 2011, Gerhard Schwanz
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>
#


use strict ;
use warnings ;

my $version = "1.05" ;

my $streetFileName ;
my $poiFileName ;
my $pdfFileName ;
my $texFileName ;
my $titleText ;
my $numColumns ;

my $streetFile ;
my $poiFile ;
my $texFile ;


($streetFileName, $poiFileName, $titleText, $pdfFileName, $numColumns) = @ARGV ;


print "$streetFileName, $poiFileName, $titleText, $pdfFileName, $numColumns\n" ;

$texFileName = $pdfFileName ;
$texFileName =~ s/.pdf/.tex/ ;

open ($texFile, ">", $texFileName) or die ("can't open tex output file") ;
print $texFile "\\documentclass[a4paper,12pt]{book}\n" ;
print $texFile "\\usepackage{multicol}\n" ;
print $texFile "\\usepackage[utf8]{inputenc}\n" ;
print $texFile "\\usepackage[top=2.5cm,bottom=2cm,left=3cm,right=2cm]{geometry}\n" ;
print $texFile "\\columnsep7mm\n" ;
print $texFile "\\begin{document}\n" ;
print $texFile "\\section*{$titleText}\n" ;
print $texFile "\n" ;

print $texFile "\\tiny\n" ;
print $texFile "Data CC-BY-SA www.openstreetmap.org\n" ;
print $texFile "\\normalsize\n\n" ;

# streets
if ($streetFileName ne "none") {
	my $result = open ($streetFile, "<", $streetFileName) ;
	if ($result) {
		my $line ;
		print $texFile "\\begin{multicols}{$numColumns}[\\subsubsection*{Streets}]\n" ;
		print $texFile "\\tiny\n" ;
		while ($line = <$streetFile>) {
			$line = convert ($line) ;
			my (@entry) = split /\t/, $line ;
			print $texFile $entry[0] ;
			print $texFile " \\dotfill " ;
			print $texFile $entry[1], " \\\\\n" ;
		}
		close ($streetFile) ;
		print $texFile "\\normalsize\n" ;
		print $texFile "\\end{multicols}\n" ;
	}
	else {
		print "ERROR: street file $streetFile could not be opened." ;
	}
}




# POIs
if ($poiFileName ne "none") {
	my $result = open ($poiFile, "<", $poiFileName) ;
	if ($result) {
		my $line ;
		print $texFile "\\begin{multicols}{$numColumns}[\\subsubsection*{Points of interest}]\n" ;
		print $texFile "\\tiny\n" ;
		while ($line = <$poiFile>) {
			$line = convert ($line) ;
			my @entry = split /\t/, $line ;
			print $texFile $entry[0] ;
			print $texFile " \\dotfill " ;
			print $texFile $entry[1], "\\\\\n" ;
		}
		close ($poiFile) ;
		print $texFile "\\normalsize\n" ;
		print $texFile "\\end{multicols}\n" ;
	}
	else {
		print "ERROR: POI file $poiFile could not be opened." ;
	}
}




print $texFile "\\end{document}\n" ;
close ($texFile) ;
print "directory tex file created.\n" ;


my $dviFileName = $pdfFileName ;
$dviFileName =~ s/.pdf/.dvi/ ;
my $psFileName = $pdfFileName ;
$psFileName =~ s/.pdf/.ps/ ;


`latex $texFileName` ;
print "directory dvi file created.\n" ;
`dvips -D600 $dviFileName -o` ;
print "directory ps file created.\n" ;
`ps2pdf $psFileName $pdfFileName` ;
print "directory pdf file created.\n" ;
`rm *.dvi` ;
`rm *.tex` ;
`rm *.ps` ;
`rm *.aux` ;
`rm *.log` ;
print "directory FINISHED.\n" ;


sub convert {
	my $line = shift ;

	($line) = ($line =~ /^(.*)$/ ) ;

	$line =~ s/\&apos;/\'/g ;
	$line =~ s/\&quot;/\'/g ;
	$line =~ s/\_/ /g ;

	return $line ;
}


