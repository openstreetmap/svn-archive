#!/usr/bin/perl

# script to convert an OSM file to a polygon file.
# the OSM file must follow certain conventions, namely
# each way must have a polygon_file and polygon_id tag,
# may have a note tag and must not have others.
#
# written by Frederik Ramm <frederik@remote.org>, public domain.

use strict;

my $poly_id;
my $poly_file;
my $polybuf;
my $outbuf;
my $nodes;
my $id=0;

while(<>) 
{
    if (/^\s*<node.*\sid=["']([0-9-]+)['"].*lat=["']([0-9.eE-]+)["'] lon=["']([0-9.eE-]+)["']/)
    {
        $nodes->{$1}=[$2,$3];
    }
    elsif (/^\s*<way /)
    {
        undef $poly_id;
        undef $poly_file;
        $polybuf = "";
    }
    elsif (defined($polybuf) && /k=["'](.*)["']\s*v=["'](.*?)["']/)
    {
        if ($1 eq "polygon_file") 
        {
            $poly_file=$2;
        }
        elsif ($1 eq "polygon_id")
        {
            $poly_id=$2;
        }
        elsif ($1 ne "note")
        {
            die("cannot process tag '$1'");
        }
    }
    elsif (/^\s*<nd ref=['"]([0-9-]+)["']/)
    {
        my $id=$1;
        die("dangling reference to node $id") unless defined($nodes->{$id});
        $polybuf .= sprintf("   %E   %E\n", $nodes->{$id}->[1], $nodes->{$id}->[0]);
    }
    elsif (/^\s*<\/way/) 
    {
        if (!defined($polybuf))
        {
            die("incomplete way definition");
        }
        $poly_id = ++$id unless defined($poly_id);
        $outbuf .= "$poly_id\n$polybuf"."END\n";
        undef $polybuf;
    }
}
$poly_file = "polygon" unless defined($poly_file);
print "$poly_file\n$outbuf"."END\n";

