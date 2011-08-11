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


package mwOccupy ; 

use strict ;
use warnings ;

use List::Util qw[min max] ;

use mwMap ;

use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw ( 	boxOccupyLines
		boxOccupyArea
		boxLinesOccupied
		boxAreaOccupied
		boxDrawOccupiedAreas
		 ) ;


my $boxSize = 5 ;

my %box = () ;


# -------------------------------------------------------------

sub boxOccupyLines {
	my ($refCoords, $buffer, $value) = @_ ;
	my @coordinates = @$refCoords ;
	my @lines = () ;

	for ( my $i = 0; $i < $#coordinates-2; $i += 2 ) {
		push @lines, [$coordinates[$i], $coordinates[$i+1], $coordinates[$i+2], $coordinates[$i+3]] ;
	}

	foreach my $line ( @lines ) {
		my $x1 = $line->[0] ;
		my $y1 = $line->[1] ;
		my $x2 = $line->[2] ;
		my $y2 = $line->[3] ;


		# print "$x1, $y1, $x2, $y2\n" ;

		if ( $x1 != $x2) {

			my $m = ($y2 - $y1) / ($x2 - $x1) ;
			my $b = $y1 - $m * $x1 ;

			if ( abs ( $x1 - $x2 ) > abs ( $y1 - $y2 ) ) {

				# calc points on x axis
				my $x = $x1 ;
				my $stepX = $boxSize ;
				if ( $x2 < $x1 ) { $stepX = - $boxSize ; }
				while ( ( $x >= min ($x1, $x2) ) and ( $x <= max ($x1, $x2) ) ) {

					my $y = $m * $x + $b ;

					# ACTUAL COORDINATE $x, $y
					my $ax1 = $x - $buffer ;
					my $ax2 = $x + $buffer ;
					my $ay1 = $y - $buffer ;
					my $ay2 = $y + $buffer ;
					boxOccupyArea ($ax1, $ay1, $ax2, $ay2, 0, $value) ;
					$x += $stepX ;	
				}

			}
			else {		

				# calc points on y axis
				my $y = $y1 ;
				my $stepY = $boxSize ;
				if ( $y2 < $y1 ) { $stepY = - $boxSize ; }
				while ( ( $y >= min ($y1, $y2) ) and ( $y <= max ($y1, $y2) ) ) {

					my $x = ($y - $b) / $m ;

					# ACTUAL COORDINATE $x, $y
					my $ax1 = $x - $buffer ;
					my $ax2 = $x + $buffer ;
					my $ay1 = $y - $buffer ;
					my $ay2 = $y + $buffer ;
					boxOccupyArea ($ax1, $ay1, $ax2, $ay2, 0, $value) ;

					$y += $stepY ;	
				}

			} # abs	

		}
		else {
			my $x = $x1 ;

			# calc points on y axis
			my $y = $y1 ;
			my $stepY = $boxSize ;
			if ( $y2 < $y1 ) { $stepY = - $boxSize ; }
			while ( ( $y >= min ($y1, $y2) ) and ( $y <= max ($y1, $y2) ) ) {

				# ACTUAL COORDINATE $x, $y
				my $ax1 = $x - $buffer ;
				my $ax2 = $x + $buffer ;
				my $ay1 = $y - $buffer ;
				my $ay2 = $y + $buffer ;
				boxOccupyArea ($ax1, $ay1, $ax2, $ay2, 0, $value) ;

				$y += $stepY ;	
			}
		}	

	}
}


sub boxLinesOccupied {
	my ($refCoords) = @_ ;

}


# -------------------------------------------------------------

sub boxOccupyArea {
	my ($x1, $y1, $x2, $y2, $buffer, $value) = @_ ;

	if ( $x2 < $x1) {
		my $tmp = $x1 ;
		$x1 = $x2 ;
		$x2 = $tmp ;
	}
	if ( $y2 < $y1) {
		my $tmp = $y1 ;
		$y1 = $y2 ;
		$y2 = $tmp ;
	}

	$x1 -= $buffer ;
	$x2 += $buffer ;
	$y1 -= $buffer ;
	$y2 += $buffer ;

	for ( my $x = $x1; $x <= $x2; $x += $boxSize) {
		for ( my $y = $y1; $y <= $y2; $y += $boxSize) {
			my $bx = int ( $x / $boxSize ) ;
			my $by = int ( $y / $boxSize ) ;
			$box{$bx}{$by} = $value ;
			# print "box $bx, $by occupied\n" ;
		}
	}

	return ;
}


sub boxAreaOccupied {
	my ($x1, $y1, $x2, $y2) = @_ ;
	my $result = 0 ;

	if ( $x2 < $x1) {
		my $tmp = $x1 ;
		$x1 = $x2 ;
		$x2 = $tmp ;
	}
	if ( $y2 < $y1) {
		my $tmp = $y1 ;
		$y1 = $y2 ;
		$y2 = $tmp ;
	}

	for ( my $x = $x1; $x <= $x2; $x += $boxSize) {
		my $bx = int ($x / $boxSize) ;
		for ( my $y = $y1; $y <= $y2; $y += $boxSize) {
			my $by = int ($y / $boxSize) ;
			# print "  $bx, $by\n" ;
			if ( defined $box{$bx}{$by} ) {
				if ( $box{$bx}{$by} > $result ) {
					# print "check box $bx, $by\n" ;
					$result = $box{$bx}{$by} ;
				}
			}
		}
	}
	return $result ;
}


# -------------------------------------------------------------


sub boxDrawOccupiedAreas {
	my $format1 = "fill=\"orange\" fill-opacity=\"0.5\" " ;
	my $format2 = "fill=\"blue\" fill-opacity=\"0.5\" " ;
	foreach my $bx ( sort {$a <=> $b} keys %box ) {
		foreach my $by ( sort {$a <=> $b} keys %{$box{$bx}} ) {
			my $x1 = $bx * $boxSize ;
			my $x2 = $x1 + $boxSize ;
			my $y1 = $by * $boxSize ;
			my $y2 = $y1 + $boxSize ;

			if ( $box{$bx}{$by} == 1) {
				drawRect ($x1, $y1, $x2, $y2, 0, $format1, "occupied") ;
			}
			else {
				drawRect ($x1, $y1, $x2, $y2, 0, $format2, "occupied") ;
			}
			# print "occupied $bx, $by\n" ;
		}
	}

}


1 ;


