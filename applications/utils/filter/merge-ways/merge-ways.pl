#!/usr/bin/perl

# A script that merges ways into one if they share certain characterstics.
#     
# Reads an OSM file on stdin, writes the merged file on stdout.
#
# May break referential integrity by dropping ways that are referenced by a relation!
#
# Writtern by Frederik Ramm <frederik@remote.org>, public domain.
# -----------------------------------------------------------------------------

# this is basically the configuration function: it controls which ways may be merged:

sub tags_compatible 
{
    my ($a, $b) = @_;

    return
        $a->{highway} eq $b->{highway} &&
        $a->{railway} eq $b->{railway} &&
        $a->{waterway} eq $b->{waterway} &&
        $a->{name} eq $b->{name};
}

# -----------------------------------------------------------------------------
# no user serviceable parts below.
#
use strict;

my $copy = 1;
my $w = {};
my $ways_beginning_at;

while(<>)
{
    if (/^\s*<(node|way|relation) (.*)/)
    {
        if ($1 eq "way") 
        {
            $copy = 0;
        }
        elsif ($1 eq "relation") 
        {
            print_ways();
            $copy = 1;
        }
    }
    if ($copy)
    {
        print;
        next;
    }
    if (/way id=['"](\d+)['"]/)
    {
        $w = {};
        $w->{attr} = $_;
        $w->{id} = $1;
    }
    elsif (/<nd ref=['"](\d+)['"]/)
    {
        if (defined($w->{firstnode}))
        {
            $w->{lastnode} = $1;
        }
        else
        {
            $w->{firstnode} = $1;
        }
        $w->{nodexml} .= $_;
    }
    elsif (/<tag k=(['"])(.*)\1 v=\1(.*)\1/)
    {
        $w->{tag}->{$2} = $3;
        $w->{tagxml} .= $_;
    }
    elsif (/<\/way>/)
    {
        push(@{$ways_beginning_at->{$w->{firstnode}}}, $w);
    }
}

sub print_ways
{
    foreach my $startnode(keys(%$ways_beginning_at))
    {
        foreach my $way(@{$ways_beginning_at->{$startnode}})
        {
            next if ($way->{drop});
            do
            {
                my $again = 0;
                next if ($way->{firstnode} == $way->{lastnode});
                foreach my $cont(@{$ways_beginning_at->{$way->{lastnode}}})
                {
                    next if ($cont->{drop});
                    next if ($cont->{firstnode} == $cont->{lastnode});
                    if (tags_compatible($way->{tag}, $cont->{tag}))
                    {
                        # merge
                        $cont->{drop} = 1;
                        $way->{lastnode} = $cont->{lastnode};
                        $way->{nodexml} .= $cont->{nodexml}; # fixme this duplicates one node in between
                        $again = 1;

                        printf STDERR "merge: %d (%s) and %d (%s)\n", 
                            $way->{id}, tostring($way->{tag}), $cont->{id}, tostring($cont->{tag});
                        last;
                    }
                }
                last unless $again;
            } 
        }
    }

    my $sorted_ways = [];
    foreach my $startnode(keys(%$ways_beginning_at))
    {
        foreach my $way(@{$ways_beginning_at->{$startnode}})
        {
            push @$sorted_ways, $way;
        }
        delete $ways_beginning_at->{$startnode};
    }
    undef $ways_beginning_at;

    foreach my $way (sort { $a->{id} <=> $b->{id} } @$sorted_ways)
    {
        next if ($way->{drop});
        print $way->{attr};
        print $way->{tagxml};
        print $way->{nodexml};
        print "</way>\n";
    }
}

sub tostring
{
    my $a = shift;
    my $tags = [];
    foreach my $key(keys %$a) { push(@$tags, "$key=".$a->{$key}); }
    return "no tags" unless scalar(@$tags);
    return join(",", @$tags);
}

