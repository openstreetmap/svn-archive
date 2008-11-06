package Geo::OSM::MapFeatures::Feature::Value::NumWithUnit;

use warnings;
use strict;

use overload '""' => \&stringify;

use base qw(Geo::OSM::MapFeatures::Feature::Value);

=head1 NAME

Geo::OSM::MapFeatures::Feature::Value::NumWithUnit - Numeric value with unit

=head1 VERSION

Version 0.01

=cut

our $VERSION = '0.01';


=head1 SYNOPSIS

=head1 FUNCTIONS

=head2 units

Returns a reference to a list of units

=cut

__PACKAGE__->mk_accessors(qw(units));

=pod

=cut

sub init {
    my $self = shift;
    my $value = shift;
    my %classargs = %{ shift() };

    $self->units($classargs{list});

    return $self;
}

sub stringify {
    my $self = shift;
    my @units = @{ $self->units() };

    return sprintf("Number with unit: %s", join(', ', @units));
}

=head1 AUTHOR

Knut Arne Bjørndal, C<< <bob at cakebox.net> >>

=head1 BUGS

Please report any bugs or feature requests to C<bug-geo-osm-mapfeatures-feature-value-numwithunit at rt.cpan.org>, or through
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

1; # End of Geo::OSM::MapFeatures::Feature::Value::NumWithUnit
