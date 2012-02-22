#!/usr/bin/perl

# reads a changeset XML on stdin (e.g. changeset-latest.osm)
# and writes a shape file that contains the rectangle for each,
# together with some attributes.

# todo: add code that can also convert <tag> data to shape columns.
# add code to filter only certain changesets.

# written by Frederik Ramm <frederik@remote.org>, public domain.

use Geo::Shapelib qw/:all/;
use strict;

my $shapefile = new Geo::Shapelib { 
    Name => 'changesets',
    Shapetype => POLYGON,
    FieldNames => ['id','who','opened','closed','num_edits'],
    FieldTypes => ['Integer:8','String:50','String:20','String:20','Integer:5']
};

while (<>) {
    chomp;
    next unless (/<changeset.*\sid="(\d+)"/);
    my $id = $1;
    next unless (/\suser="([^"]+)"/);
    my $user = $1;
    next unless (/\screated_at="([^"]+)"/);
    my $cra = $1;
    next unless (/\sclosed_at="([^"]+)"/);
    my $cla = $1;
    next unless (/\snum_changes="(\d+)"/);
    my $nc = $1;
    my %bb;
    foreach my $attr(qw/min_lat min_lon max_lon max_lat/)
    {
        last unless (/\s$attr="([0-9.-]+)"/);
        $bb{$attr} = $1;
    }
    next unless defined($bb{'max_lat'});

    my $nc = $1;

    push @{$shapefile->{Shapes}},{ Vertices => [
        [$bb{min_lon}, $bb{min_lat}, 0, 0],
        [$bb{max_lon}, $bb{min_lat}, 0, 0],
        [$bb{max_lon}, $bb{max_lat}, 0, 0],
        [$bb{min_lon}, $bb{max_lat}, 0, 0],
        [$bb{min_lon}, $bb{min_lat}, 0, 0]
     ] };
    push @{$shapefile->{ShapeRecords}}, [$id, $user, $cra, $cla, $nc];
}

$shapefile->save();
