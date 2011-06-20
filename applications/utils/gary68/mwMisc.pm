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


package mwMisc ; 

use strict ;
use warnings ;


use vars qw($VERSION @ISA @EXPORT @EXPORT_OK);

require Exporter ;

@ISA = qw ( Exporter AutoLoader ) ;

@EXPORT = qw (	getValue
		createLabel
		 ) ;



sub getValue {
	my ($key, $aRef) = @_ ;
	my $value = undef ;
	foreach my $kv (@$aRef) {
		if ($kv->[0] eq $key) { $value = $kv->[1]; }
	}
	return $value ;
}

sub createLabel {
#
# takes @tags and labelKey(s) from style file and creates labelTextTotal and array of labels for directory
# takes more keys in one string - using a separator. 
#
# § all listed keys will be searched for and values be concatenated
# # first of found keys will be used to select value
# "name§ref" will return all values if given
# "name#ref" will return name, if given. if no name is given, ref will be used. none given, no text
#
	my ($ref1, $styleLabelText, $lon, $lat) = @_ ;
	my @tags = @$ref1 ;
	my @keys ;
	my @labels = () ;
	my $labelTextTotal = "" ; 

	if (grep /!/, $styleLabelText) { # AND
		@keys = split ( /!/, $styleLabelText) ;
		# print "par found: $styleLabelText; @keys\n" ;
		for (my $i=0; $i<=$#keys; $i++) {
			if ($keys[$i] eq "_lat") { push @labels, $lat ; } 
			if ($keys[$i] eq "_lon") { push @labels, $lon ; } 
			foreach my $tag (@tags) {
				if ($tag->[0] eq $keys[$i]) {
					push @labels, $tag->[1] ;
				}
			}
		}
		$labelTextTotal = "" ;
		foreach my $label (@labels) { $labelTextTotal .= $label . " " ; }
	}
	else { # PRIO
		@keys = split ( /#/, $styleLabelText) ;
		my $i = 0 ; my $found = 0 ;
		while ( ($i<=$#keys) and ($found == 0) ) {
			if ($keys[$i] eq "_lat") { push @labels, $lat ; $found = 1 ; $labelTextTotal = $lat ; } 
			if ($keys[$i] eq "_lon") { push @labels, $lon ; $found = 1 ; $labelTextTotal = $lon ; } 
			foreach my $tag (@tags) {
				if ($tag->[0] eq $keys[$i]) {
					push @labels, $tag->[1] ;
					$labelTextTotal = $tag->[1] ;
					$found = 1 ;
				}
			}
			$i++ ;
		}		
	}
	return ( $labelTextTotal, \@labels) ;
}


1 ;


