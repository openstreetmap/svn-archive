#!/usr/bin/perl

# script to convert a GPX file to a polygon file.
#
# written by Rodolphe Quiédeville <rodolphe@quiedeville.org>, GPL.
# Most parts of code inspired from osm2poly.py by Frederik Ramm <frederik@remote.org>
# GPX Files for relations could be obtained from http://ra.osmsurround.org/analyze.jsp
#

use strict;

my $poly_id = -1;
my $poly_file;
my $polybuf;
my $outbuf;
my $id=0;

while(<>) 
{
    if (/^\s*<trkpt.*\slon=["']([0-9.eE-]+)["'] lat=["']([0-9.eE-]+)["']/)
    {
        $polybuf .= sprintf "\t%f\t%f\n", $1,$2;
    } 
    elsif (/^\s*<trk>/) 
    {
        $polybuf = "";
	$poly_id++;
    }
    elsif (/^\s*<\/trk>/) 
    {
        $outbuf .= "$poly_id\n$polybuf"."END\n";
    }
}

$poly_file = "polygon" unless defined($poly_file);
print "$poly_file\n$outbuf"."END\n";
