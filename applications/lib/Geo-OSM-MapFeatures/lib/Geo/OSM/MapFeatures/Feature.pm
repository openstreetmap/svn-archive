package Geo::OSM::MapFeatures::Feature;

use warnings;
use strict;

use Geo::OSM::MapFeatures::Feature::Key;
use Geo::OSM::MapFeatures::Feature::Value;
use Geo::OSM::MapFeatures::Feature::Type;

use overload '""' => \&stringify;

use base qw(Class::Accessor);
__PACKAGE__->mk_accessors(qw(key values types description));

=head1 NAME

Geo::OSM::MapFeatures::Feature - Represents a feature on map features

=head1 VERSION

Version 0.01

=cut

our $VERSION = '0.02';


=head1 SYNOPSIS

A feature corresponds to one row on map features. It has a key, one or
more values, one or more feature types and a description.

=head1 FUNCTIONS

=head2 key

=head2 values

=head2 types

Feature type: area, way or node

=head2 description

=cut

sub new {
    my $pkg = shift;
    my $class = ref $pkg || $pkg;
    my $self = bless( {}, $class);

    my $key = shift;
    my $value = shift;
    my @types = @{shift()};
    my $description = shift;

    $self->key( new Geo::OSM::MapFeatures::Feature::Key($key) );

    # Value is single value or a list separated by "/", "|" or "or"
    # With the exception of 24/7, 
    my @values = split( m#\s*(?:(?<!24)/(?!7)|\'\'\'or\'\'\'|\bor\b|\|)\s*#, $value );
    foreach my $value ( @values ){
        #FIXME: use accessor instead
        push( @{$self->{values}}, new Geo::OSM::MapFeatures::Feature::Value($value) );
    }

    foreach my $type ( @types ){
        #FIXME: use accessor instead
        push( @{$self->{types}}, new Geo::OSM::MapFeatures::Feature::Type($type) );
    }

    $self->description($description);

    return $self;
}

sub stringify {
    my $self = shift;

    return sprintf("%s = %s", $self->{key}, join(' / ', @{ $self->{values} }));
}

=head1 AUTHOR

Knut Arne Bjørndal, C<< <bob at cakebox.net> >>

=head1 BUGS

Please report any bugs or feature requests to C<bug-geo-osm-mapfeatures-feature at rt.cpan.org>, or through
the web interface at L<http://rt.cpan.org/NoAuth/ReportBug.html?Queue=Geo-OSM-MapFeatures>.  I will be notified, and then you'll
automatically be notified of progress on your bug as I make changes.




=head1 SUPPORT

You can find documentation for this module with the perldoc command.

    perldoc Geo::OSM::MapFeatures


You can also look for information at:

=over 4

=item * RT: CPAN's request tracker

L<http://rt.cpan.org/NoAuth/Bugs.html?Dist=Geo-OSM-MapFeatures>

=item * AnnoCPAN: Annotated CPAN documentation

L<http://annocpan.org/dist/Geo-OSM-MapFeatures>

=item * CPAN Ratings

L<http://cpanratings.perl.org/d/Geo-OSM-MapFeatures>

=item * Search CPAN

L<http://search.cpan.org/dist/Geo-OSM-MapFeatures>

=back


=head1 ACKNOWLEDGEMENTS


=head1 COPYRIGHT & LICENSE

Copyright 2008 Knut Arne Bjørndal, all rights reserved.

This program is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.


=cut

1; # End of Geo::OSM::MapFeatures::Feature
