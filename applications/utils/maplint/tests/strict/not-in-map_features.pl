#!/usr/bin/perl
# mapfeatures.pl - Rebuilds the mapfeatures.xml maplint test file
# Copyright (C) 2007 Knut Arne Bjørndal
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

# Usage:
# $ ./tests/strict/mapfeatures.pl > tests/strict/mapfeatures.xml.new
# $ diff -u tests/strict/mapfeatures.xml tests/strict/mapfeatures.xml.new 
# Make sure to look over the diff and check that not too much has changed.
# That could be a sign that somebody has radically changed the format of
# Map Features or something.
# If it looks fine, then do:
# $ mv tests/strict/mapfeatures.xml.new tests/strict/mapfeatures.xml
# And build maplint as usual

use strict;
use warnings;

use XML::Simple;
use LWP::UserAgent;
use Data::Dumper;

sub add_feature( $$$ );

# Get export of Map_Features
my $ua = LWP::UserAgent->new;
$ua->agent("maplint:mapfeatures.pl/0.1");

my $req = HTTP::Request->new(GET => "http://wiki.openstreetmap.org/index.php?title=Special:Export/Map_Features");
warn "Fetching Map_Features\n";
my $res = $ua->request($req);
if(! $res->is_success) {
    die "Couldn't fetch Map_Features: ".$res->status_line . "\n";
}

my $xml = XMLin($res->content);

&parse_map_features($xml);

# Add some additional features
add_feature('osmarender:nameDirection', 1, ['way']);
add_feature('osmarender:render', 'no', ['node', 'way', 'area']);
add_feature('osmarender:renderName', 'no', ['node', 'way', 'area']);
add_feature('osmarender:renderRef', 'no', ['node', 'way', 'area']);

sub parse_map_features( $ ){
    my $template;
    while( $xml->{page}->{revision}{text}{content} =~ /{{(.*?)}}/gs ){
        my $template = $1;
        next unless $template =~ /(Map_Features:\S+)/;
        my $page = $1;

        my $req = HTTP::Request->new(GET => "http://wiki.openstreetmap.org/api.php?action=expandtemplates&text={{$page}}&format=xml");
        warn "Fetching $page\n";
        my $res = $ua->request($req);
        if(! $res->is_success) {
            die "Couldn't fetch $page: ".$res->status_line . "\n";
        }
        
        my $xml = XMLin($res->content);
        
        &parse_featuretemplate($xml);
    }
}

sub parse_featuretemplate( $ ){
    my $xml = shift;

    # Read every table line and extract key, value and types

#    warn "XML: ".Dumper($xml);
#    $xml->{page}{revision}{text}{content} =~ /{\|(.*)\|}/s;
    $xml->{expandtemplates} =~ /{\|(.*)\|}/s;
    my $table = $1;
#    warn "Table: ".Dumper($table);

    my @lines = split(/\|-/, $table);
    foreach my $line (@lines){
        my @columns = split("\n", $line);

        # Ignore lines not starting with "| "
        @columns = grep {/^\| /} @columns;

        # Skip lines that doesn't have 6 columns
        next unless $#columns == 5;

        my $key = $columns[0];
        my $value = $columns[1];
        my $types = $columns[2];

        $key =~ s/^\| //;
        $value =~ s/^\| //;
        $types =~ s/^\| //;

        # Strip leading and trailing whitespace
        $key =~ s/^\s*//;
        $value =~ s/^\s*//;
        $key =~ s/\s*$//;
        $value =~ s/\s*$//;

        # Clean away wikilinks
        $key =~ s/\[\[(?:.*?\|\s*)?(.*?)\]\]/$1/g;
        $value =~ s/\[\[(?:.*?\|\s*)?(.*?)\]\]/$1/g;

        # Clean away hyperlinks
        $key =~ s/\[\S+:\/\/(?:[^\]]+\s+([^\]]+)|[^\]]+\/([^\]]+)(?:\.\w{2,5})?\s*)\]/$1 ? $1 : $2/ge;
        $value =~ s/\[\S+:\/\/(?:[^\]]+\s+([^\]]+)|[^\]]+\/([^\]]+)(?:\.\w{2,5})?\s*)\]/$1 ? $1 : $2/ge;

        # Clean away other markup
        $key =~ s/<[^>]+>//g;
        $value =~ s/<[^>]+>//g;

        # Get all types for the feature
        my @types;
        push(@types, 'node') if $types =~ /node/;
        push(@types, 'way') if $types =~ /way/;
        push(@types, 'area') if $types =~ /area/;

        # Is it a single value, or a slash-separated list?
        my @values = ();
        if( $value =~ /\/|\bor\b/ ){
            foreach my $subvalue ( split(/\s*(?:\/|or)\s*/, $value) ){
                add_feature($key, $subvalue, \@types);
            }
        } else {
            add_feature($key, $value, \@types);
        }
    }
}

# add_feature( $key, $value, \@types )
# Pushes the feature onto the global list after
# checking for special things that have to be specially handled
# in the rule output section below
sub add_feature( $$$ ){
    my $key = shift;
    my $value = shift;
    my @types = @{ shift() };

    my $special = 1; # This is special if it's a ref

    # name:''lg''
    if( $key =~ /(.*:)''lg''/i ){
	$key = "contains(\@k, '$1')";
    }

    # Arbitrarily defined value
    if( $value =~ /user defined|defined by editor/i ){
	$value = 'userdef';
    }
    # Numeric value
    elsif( lc($value) eq 'num' || lc($value) eq 'number' ){
	$special = {type => 'num'};
    }
    # Range value
    elsif( $value =~ /^(-?\d+)\s+to\s+(-?\d+)$/i ){
	$special = {type => 'range', from => $1, to => $2};
    }
    # Date value
    elsif( lc($value) eq 'date'  ){
        #TODO: if anybody ever decides on a date format then this should be {type => 'date'} and there should be a test against the format
        $value = 'userdef';
    }
    # Day of week
    elsif( lc($value) eq 'day of week' ){
        $special = { type => 'list',
                     list => ['monday', 'mon',
                              'tuesday', 'tue',
                              'wednesday', 'wed',
                              'thursday', 'thu',
                              'friday', 'fri',
                              'saturday', 'sat',
                              'sunday', 'sun'
                              ]};
    }
    # Time value
    elsif( lc($value) eq 'time' ){
        #TODO: if anybody ever decides on a time format then this should be {type => 'time'} and there should be a test against the format
        $value = 'userdef';
    }

    foreach my $type ( @types ){
	$type = lc($type);
        # Map features have the area type, but DB only has way
	if( $type eq 'area' ){ $type = 'way' }

	$main::keys{$type}{$key}{$value} = $special;
    }
}

#
# OUTPUT
#

#use Data::Dumper; die Dumper(\%main::keys); #DEBUG

# Header
print <<EOF;
<?xml version='1.0' encoding='iso-8859-1' ?>
<!--
IMPORTANT NOTICE:

This is an AUTOGENERATED file.
Do NOT edit this file manually, edit not-in-map_features.pl instead
-->
<maplint:test group="strict" id="not-in-map_features" version="1" severity="notice"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <maplint:desc xml:lang="en">
      Checks tags against map features. These are not errors since everybody
      can invent new tags, but this helps find both misspelled tags as well as
      tags one may want to propose.
      This list is autogenerated from the wiki page.
    </maplint:desc>

EOF

# Type
foreach my $type ( keys(%main::keys) ){
    print "<maplint:check data='$type' type='application/xsl+xml'>\n";
    print "<xsl:for-each select=\"tag\">\n";
    print "<xsl:choose>\n";

    # Special handling of tiger:*
    print "<xsl:when test=\"starts-with(\@k, 'tiger:')\">\n";
    print "</xsl:when>\n";

    # Special handling of AND_*
    print "<xsl:when test=\"starts-with(\@k, 'AND_')\">\n";
    print "</xsl:when>\n";
    print "<xsl:when test=\"starts-with(\@k, 'AND:')\">\n";
    print "</xsl:when>\n";

    # Special handling of gns:*
    print "<xsl:when test=\"starts-with(\@k, 'gns:')\">\n";
    print "</xsl:when>\n";

    # Special handdling of massgis:*
    print "<xsl:when test=\"starts-with(\@k, 'massgis:')\">\n";
    print "</xsl:when>\n";

    # Special handling of openGeoDB/opengeodb:*
    print "<xsl:when test=\"starts-with(\@k, 'openGeoDB:')\">\n";
    print "</xsl:when>\n";
    print "<xsl:when test=\"starts-with(\@k, 'opengeodb:')\">\n";
    print "</xsl:when>\n";

    # Key
    foreach my $key ( sort( keys( %{$main::keys{$type}} ) ) ){
	# Key must either be a simple string or a valid XSL expression
	my $test;
	if( $key =~ /^[\w\d_\- :]+$/ ){
	    $test = "\@k='$key'";
	} else {
	    $test = $key;
	}
	print "<xsl:when test=\"$test\">\n";

	# Values
	my @values = sort( keys( %{$main::keys{$type}{$key}} ) );
	@values = grep { $_ ne 'userdef' } @values;

	# Is there a list of values, or is it only user defined
	if( @values ){
	    print "<xsl:choose>\n";
	    foreach my $value ( @values ){
		# Is it a special value?
		if( ref( $main::keys{$type}{$key}{$value} ) ){
		    my $special = $main::keys{$type}{$key}{$value};
		    if( $special->{type} eq 'num' ){
			print "<xsl:when test=\"string(number(\@v)) != 'NaN'\" />\n";
		    } elsif( $special->{type} eq 'range' ){
			my $from = $special->{from}; my $to = $special->{to};
			print "<xsl:when test=\"\@v &gt; $from and \@v &lt; $to\" />\n";
                    } elsif( $special->{type} eq 'list' ){
                        foreach my $item ( @{$special->{list}} ){
                            print "<xsl:when test=\"\@v='$item'\" />\n";
                        }
		    } else {
			die "BUG: special ref but unknown type";
		    }
		}
		# Or normal value?
		else {
		    print "<xsl:when test=\"\@v='$value'\" />\n";
		}
	    }

	    # Can this value be user defined?
	    if( $main::keys{$type}{$key}{userdef} ){
		#print "<xsl:otherwise />\n";
		print "<!-- Uncomment to output notice about user defined value:\n";
		print "<xsl:otherwise>\n";
		print "<maplint:result><xsl:value-of select=\"concat('User defined value: ', \@k, '=', \@v)\" /></maplint:result>\n";
		print "</xsl:otherwise>\n";
		print " -->\n";
	    }
	    # If not everything but the above list is errors
	    else {
		print "<xsl:otherwise>\n";
		print "<maplint:result><xsl:value-of select=\"concat('Value not in map features: ', \@k, '=', \@v)\" /></maplint:result>\n";
		print "</xsl:otherwise>\n";
	    }
	    print "</xsl:choose>\n";
	}
	# Value can only be user defined:
	elsif( $main::keys{$type}{$key}{userdef} ){
	    print "<!-- Value: User defined -->\n";
	    #print "<maplint:result><xsl:value-of select=\"concat('User defined value: ', \@k, '=', \@v)\" /></maplint:result>\n";
	} else {
	    die "No possible values for key $key";
	}

	print "</xsl:when>\n";
    } # foreach key

    # Unknown key:
    print "<xsl:otherwise>\n";
    print "<maplint:result><xsl:value-of select=\"concat('Unknown key: ', \@k, '=', \@v)\" /></maplint:result>\n";
    print "</xsl:otherwise>\n";


    print "</xsl:choose>\n";
    print "</xsl:for-each>\n";
    print "</maplint:check>\n";
}
print "</maplint:test>\n";
