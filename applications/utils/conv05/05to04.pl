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

my $new_seg_id = -1;
my %segs;
our ( $id, $user, $timestamp, %tags, $lat, $lon, @nodes, @members );

sub obj_init($) {
    my %attr = %{ $_[0] };
    ( $id, $user, $timestamp, $visible ) = @attr{qw(id user timestamp visible)};
    $user      = '' unless defined $user;
    $visible   = 1  unless defined $visible;
    $timestamp = '' unless defined $timestamp;
    %tags = @nodes = @members = ();
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
        elsif ( $name eq 'nd' ) {
            push @nodes, $attr{'ref'};
        }
        elsif ( $name eq 'node' ) {
            obj_init \%attr;
            ( $lat, $lon ) = @attr{qw(lat lon)};
        }
        elsif ( $name eq 'member' ) {
            push @members, [ @attr{qw(type ref role)} ];
        }
        elsif ( $name eq 'way' or $name eq 'relation' ) {
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
        elsif ( $name eq 'way' ) {
            my @segs;
            for my $i ( 0 .. ( @nodes - 2 ) ) {
                my ( $from, $to ) = @nodes[ $i, $i + 1 ];
                unless ( defined $segs{"$from,$to"} ) {
                    local $id = $new_seg_id--;
                    printf qq(  <segment %s from="%s" to="%s"/>\n),
                      fmt_std_tags, map esc, $from, $to;
                    $segs{"$from,$to"} = $id;
                }
                push @segs, $segs{"$from,$to"};
            }
            printf qq(  <way %s>\n), fmt_std_tags;
            printf qq(    <seg id="%s"/>\n), $_ for @segs;
            write_tags;
            printf qq(  </way>\n);
        }
        elsif ( $name eq 'relation' ) {

            # drop relations
        }
    },
);

print qq(<?xml version="1.0"?>\n<osm version="0.4" generator="05to04.pl">\n);
if (@ARGV) {
    $p->parsefile($_) for @ARGV;
}
else {
    $p->parse( \*STDIN );
}
print qq(</osm>\n);
