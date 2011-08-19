# 
# PERL mapweaver module by gary68
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


package OSM::gpx ; 

use strict ;
use warnings ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (	readGPXFile
		 ) ;

my $file ;
my $line ;

my $wptNr = 0 ;
my $trkNr = 0 ;
my $rteNr = 0 ;
my %wpt = () ;
my %trk = () ;
my %rte = () ;




sub readGPXFile {
	my $name = shift ;

	my $res = open ($file, "<", $name) ;

	if ($res) {

		$line = getLine() ;
		while (defined $line) {

			if ( grep /<wpt/i, $line) { readWpt() ; }
			if ( grep /<rte/i, $line) { readRte() ; }
			if ( grep /<trk/i, $line) { readTrk() ; }

			$line = getLine() ;
		}

		close ($file) ;

	}
	else {
		print "ERROR: can't open gpx file $name\n" ;
	}

	print "gpx file $name read. $wptNr waypoint(s), $trkNr track(s) and $rteNr route(s).\n" ;

	return (\%wpt, \%rte, \%trk) ;
}



sub getLine {
	$line = <$file> ;
	if (defined $line) {	
		$line =~ s/\r//g ; # remove dos/win char at line end
	}

	if (defined $line) {
		$line =~ s/^\s// ;
		$line =~ s/\s$// ;
	}

	while ( (defined $line) and  (length $line == 0) ) {
		$line = <$file> ;
	}
	return $line ;
}


sub readWpt {
	$wptNr++ ;
	# print "read wpt $wptNr\n" ;
	my ($lon) = ( $line =~ /lon=\"(.+?)\"/ ) ;
	my ($lat) = ( $line =~ /lat=\"(.+?)\"/ ) ;

	$wpt{$wptNr}{"lon"} = $lon ;
	$wpt{$wptNr}{"lat"} = $lat ;

	while ( ! grep /<\/wpt>/i, $line) {
		my ($ele) = ( $line =~ /<ele>(.+?)<\/ele>/ ) ;
		my ($name) = ( $line =~ /<name>(.+?)<\/name>/ ) ;
		if (defined $name) { $wpt{$wptNr}{"name"} = cleanName ($name) ; } 
		if (defined $ele) { $wpt{$wptNr}{"ele"} = $ele ; } 
		$line = getLine() ;
	}
}


sub readRte {
	$rteNr++ ;
	# print "read route $rteNr\n" ;
	my $rteWptNr = 0 ;

	$line = getLine() ;
	while ( ! grep /<\/rte>/i, $line) {

		if ( grep /<rtept/i, $line) { 
			$rteWptNr++ ;
			my ($lon) = ( $line =~ /lon=\"(.+?)\"/ ) ;
			my ($lat) = ( $line =~ /lat=\"(.+?)\"/ ) ;
			$rte{$rteNr}{$rteWptNr}{"lon"} = $lon ;
			$rte{$rteNr}{$rteWptNr}{"lat"} = $lat ;

			while ( ! grep /<\/rtept>/i, $line) {
				$line = getLine() ;
			}
		}

		my ($name) = ( $line =~ /<name>(.+?)<\/name>/ ) ;
		# if (defined $name) { $rte{$rteNr}{"name"} = cleanName ($name) ; } 

		$line = getLine() ;
	}
}



sub readTrk {
	$trkNr++ ;
	my $trkSegNr = 0 ;
	# print "read track $trkNr\n" ;

	$line = getLine() ;
	while ( ! grep /<\/trk>/i, $line) {

		if ( grep /<trkseg/i, $line) { 
			$trkSegNr++ ;
			# print "  read track segment $trkSegNr\n" ;
			my $wptNr = 0 ;

			while ( ! grep /<\/trkseg>/i, $line) {

				if ( grep /<trkpt/i, $line) {
					$wptNr++ ;					
					# print "    read track wpt $wptNr\n" ;
					my ($lon) = ( $line =~ /lon=\"(.+?)\"/ ) ;
					my ($lat) = ( $line =~ /lat=\"(.+?)\"/ ) ;
					$trk{$trkNr}{$trkSegNr}{$wptNr}{"lon"} = $lon ;
					$trk{$trkNr}{$trkSegNr}{$wptNr}{"lat"} = $lat ;

					while ( ! grep /<\/trkpt>/i, $line) {
						$line = getLine() ;
					}
				}

				$line = getLine() ;

			}

			# print "  track segment finished\n" ;
		}

		my ($name) = ( $line =~ /<name>(.+?)<\/name>/ ) ;
		# if (defined $name) { $trk{$trkNr}{"name"} = cleanName ($name) ; } 

		$line = getLine() ;
		# print "  track finished\n" ;
	}
	# print "readTrK finished\n" ;
}



sub cleanName {
	my $name = shift ;
	$name =~ s/\<!\[CDATA\[//i ; 
	$name =~ s/\]\]>//i ; 
	return $name ;
}


1 ;


