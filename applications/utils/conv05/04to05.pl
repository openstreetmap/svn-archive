#!/usr/bin/env perl
# Copyright (C) 2007  Gabriel Ebner
#
# Permission is hereby granted, free of charge, to any person
# obtaining a copy of this software and associated documentation
# files (the "Software"), to deal in the Software without
# restriction, including without limitation the rights to use,
# copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following
# conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
# OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.

use XML::Parser;
use HTML::Entities;

sub esc { HTML::Entities::encode_numeric( $_[0] || $_ ) }

my $p = XML::Parser->new;

my $new_rel_id = -1;
my $new_way_id = -1;
my ( %segs, %used_segs );
our ( $id, $user, $timestamp, %tags, $lat, $lon, $from, $to, @segs );

sub obj_init($) {
    my %attr = %{ $_[0] };
    ( $id, $user, $timestamp, $visible ) = @attr{qw(id user timestamp visible)};
    $user      = '' unless defined $user;
    $visible   = 1  unless defined $visible;
    $timestamp = '' unless defined $timestamp;
    %tags = @segs = ();
}

sub fmt_std_tags {
    sprintf qq(id="%s" user="%s" visible="%s" timestamp="%s"), map esc, $id,
      $user, $visible, $timestamp;
}

sub write_tags {
    while ( my ( $k, $v ) = each %tags ) {
        printf qq(    <tag k="%s" v="%s"/>\n), map esc, $k, $v;
    }
}

$p->setHandlers(
    Start => sub {
        my ( $p, $name, %attr ) = @_;
        if ( $name eq 'tag' ) {
            $tags{ $attr{'k'} } = $attr{'v'};
        }
        elsif ( $name eq 'seg' ) {
            push @segs, $attr{'id'};
        }
        elsif ( $name eq 'node' ) {
            obj_init \%attr;
            ( $lat, $lon ) = @attr{qw(lat lon)};
        }
        elsif ( $name eq 'segment' ) {
            obj_init \%attr;
            ( $from, $to ) = @attr{qw(from to)};
        }
        elsif ( $name eq 'way' ) {
            obj_init \%attr;
        }
    },
    End => sub {
        my ( $p, $name ) = @_;
        if ( $name eq 'node' ) {
            printf qq(  <node %s lat="%s" lon="%s">\n), fmt_std_tags, map esc,
              $lat, $lon;
            write_tags;
            printf qq(  </node>\n);
        }
        elsif ( $name eq 'segment' ) {
            $segs{$id} =
              [ $id, $user, $timestamp, $visible, {%tags}, $from, $to ]
              if $visible;
        }
        elsif ( $name eq 'way' ) {
            $id = $new_way_id-- if $id < 0;
            my @pairs = map {
                $used_segs{$_} = 1;
                if ( defined $segs{$_} ) {
                    [ $segs{$_}->[5], $segs{$_}->[6] ];
                }
                else { () }
            } @segs;
            my @chunks;
            while (@pairs) {
                my @chunk = @{ shift @pairs };
                while (1) {
                    my $found = 0;
                    my @new_pairs;
                    for (@pairs) {
                        if ( $_->[0] == $chunk[ @chunk - 1 ] ) {
                            push @chunk, $_->[1];
                            $found = 1;
                        }
                        elsif ( $_->[1] == $chunk[0] ) {
                            unshift @chunk, $_->[0];
                            $found = 1;
                        }
                        else {
                            push @new_pairs, $_;
                        }
                    }
                    @pairs = @new_pairs;
                    last unless $found;
                }
                push @chunks, \@chunk;
            }

            my $orig_id_used = 0;
            my @ids;
            for my $chunk (@chunks) {
                my $way_id;
                unless ($orig_id_used) {
                    $way_id       = $id;
                    $orig_id_used = 1;
                }
                else {
                    $way_id = $new_way_id--;
                }
                push @ids, $way_id;
                local $id = $way_id;
                printf qq(  <way %s>\n), fmt_std_tags;
                printf qq(    <nd ref="%s"/>\n), esc $_ for @$chunk;
                write_tags;
                printf qq(  </way>\n);
            }

            if ( @ids > 1 ) {
                my $create_multipolygon =
                  ( defined $tags{'natural'}
                      and $tags{'natural'} ne 'coastline' )
                  || ( defined $tags{'waterway'}
                    and $tags{'waterway'} eq 'riverbank' )
                  || grep( defined $tags{$_},
                    qw(leisure landuse sport amenity tourism building) );
                if ($create_multipolygon) {
                    local $id = $new_rel_id--;
                    printf qq(  <relation %s>\n), fmt_std_tags;
                    printf qq(    <member type="way" ref="%s" role=""/>\n),
                      esc $_
                      for @ids;
                    print qq(    <tag k="type" v="multipolygon"/>\n);
                    print qq(  </relation>\n);
                }
            }
        }
    },
);

print qq(<?xml version="1.0"?>\n<osm version="0.5" generator="04to05.pl">\n);

if (@ARGV) {
    $p->parsefile($_) for @ARGV;
}
else {
    $p->parse( \*STDIN );
}

for my $seg ( values %segs ) {
    my $tags;
    ( $id, $user, $timestamp, $visible, $tags, $from, $to ) = @$seg;
    %tags = %$tags;
    next unless not $used_segs{$id} or grep( $_ ne 'created_by', keys %tags );

    local $id = $new_way_id--;
    printf qq(  <way %s>\n), fmt_std_tags;
    printf qq(    <nd ref="%s"/>\n), esc $_ for ( $from, $to );
    write_tags;
    printf qq(  </way>\n);
}

print qq(</osm>\n);
