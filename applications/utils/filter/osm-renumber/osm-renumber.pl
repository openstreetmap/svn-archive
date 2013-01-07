#!/usr/bin/perl

use strict;

# this utility renumbers an OSM file, giving each object an ID starting 
# with the given $base and counting upwards from there. references are
# adjusted accordingly. 

# limited effort is made to drop stuff marked as "action=delete" by JOSM.

my $base =  { 'node' => 1, 'way' => 1, 'relation' => 1 };
my $idmap = { 'node' => {}, 'way' => {}, 'relation' => {} };

my $what;
my $del;

while(<>)
{
    if (/<(node|way|relation).* id=['"](-?\d+)['"]/)
    {
        my ($w, $i) = ($1, $2);
        if (/action=.delete/)
        {
            $del = 1;
            $what = $w;
        }
        else
        {
            $what = $w;
            my $newid = getid($what, $i);
            s/ id=['"]$i['"]/ id="$newid"/;
            print;
        }
        if (/\/>$/)
        {
            undef $del;
            undef $what;
        }
    }
    elsif (/<\/$what>/)
    {
        print unless $del;
        undef $del;
    }
    elsif (!$del)
    {
        if (/member type=['"](node|way|relation)['"].*ref=['"](-?\d+)['"]/)
        {
            my $newid = getid($1, $2);
            s/ ref=['"]$2['"]/ ref="$newid"/;
        }
        elsif (/nd ref=['"](-?\d+)['"]/)
        {
            my $newid = getid("node", $1);
            s/ ref=['"]$1['"]/ ref="$newid"/;
        }
        print;
    }
}

sub getid
{
    my ($what, $old) = @_;
    my $map = $idmap->{$what};
    return $map->{$old} if (defined($map->{$old}));
    $map->{$old} = $base->{$what}++;
    return $map->{$old};
}

