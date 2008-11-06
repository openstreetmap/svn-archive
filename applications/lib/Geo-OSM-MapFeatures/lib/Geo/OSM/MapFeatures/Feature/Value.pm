package Geo::OSM::MapFeatures::Feature::Value;

use warnings;
use strict;

use Carp;

use base qw( Class::Factory Class::Accessor );
__PACKAGE__->mk_accessors(qw(value));

use overload '""' => \&stringify, '<=>' => \&compare, 'cmp' => \&compare;

=head1 NAME

Geo::OSM::MapFeatures::Feature::Value - Represents a feature value

=head1 VERSION

Version 0.01

=cut

our $VERSION = '0.01';


=head1 SYNOPSIS

This module represents a value on a feature from map features.

To handle all the special value types this class is a factory and returns
a subclass of itself if the value is special. Normal static strings are
handled in this module.

=cut

__PACKAGE__->add_factory_type( userdef => "Geo::OSM::MapFeatures::Feature::Value::Userdef" );
__PACKAGE__->add_factory_type( num => "Geo::OSM::MapFeatures::Feature::Value::Num" );
__PACKAGE__->add_factory_type( range => "Geo::OSM::MapFeatures::Feature::Value::Range" );
__PACKAGE__->add_factory_type( date => "Geo::OSM::MapFeatures::Feature::Value::Date" );
__PACKAGE__->add_factory_type( time => "Geo::OSM::MapFeatures::Feature::Value::Time" );
__PACKAGE__->add_factory_type( list => "Geo::OSM::MapFeatures::Feature::Value::List" );
__PACKAGE__->add_factory_type( numwithunit => "Geo::OSM::MapFeatures::Feature::Value::NumWithUnit" );

=head1 FUNCTIONS

=head2 value

This always contains the value as it was on map features (except any
wiki syntax)

=cut

sub new {
    my $pkg = shift;
    my $value = shift();

    my $class = ref($pkg) || $pkg;
    my %classargs;

    # Arbitrarily defined value
    if( $value =~ /user defined|defined by editor/i || $value =~ /^\s*\.\.\.\s*$/ ){
        #FIXME: Maybe defined by editor should be separate?
	$class = $pkg->get_factory_class('userdef');
    }
    # Numeric value
    elsif( lc($value) eq 'num' || lc($value) eq 'number' ){
        $class = $pkg->get_factory_class('num');
    }
    # Range value
    elsif( $value =~ /^(-?\d+)\s+to\s+(-?\d+)$/i ){
        $class = $pkg->get_factory_class('range');
        %classargs = (from => $1, to => $2);
    }
    # Date value
    elsif( lc($value) eq 'date'  ){
        $class = $pkg->get_factory_class('date')
    }
    # Day of week
    elsif( lc($value) eq 'day of week' ){
        $class = $pkg->get_factory_class('list');
        %classargs = (
            list => [
                'monday', 'mon',
                'tuesday', 'tue',
                'wednesday', 'wed',
                'thursday', 'thu',
                'friday', 'fri',
                'saturday', 'sat',
                'sunday', 'sun'
            ]);
    }
    # Time value
    elsif( lc($value) eq 'time' ){
        $class = $pkg->get_factory_class('time');
    }
    # Speed
    elsif( lc($value) eq 'speed' ){
        $class = $pkg->get_factory_class('numwithunit');
        %classargs = (
            units => [
                'kph', 'km/h',
                'mph', 'knots'
            ]
        );
    }
    # Lengths
    elsif( lc($value) eq 'height' || lc($value) eq 'width' ){
        $class = $pkg->get_factory_class('numwithunit');
        %classargs = (
            units => [
                'mm', 'cm', 'dm', 'm', 'km',
                'mil', # Scandinavian mil (10km)
                'inch', 'foot', 'yard', 'mile',
                'nm', # Nautical mile
                'furlong'
            ]
        );
    }

    my $self = bless( {value => $value}, $class);

    return $self->init($value, \%classargs, @_);
}

# Inheriting classes need to override this method if they get arguments
sub init {
    my $self = shift;

    confess "BUG: Subclass with arguments haven't overriden init. classargs=$_[1]"
        if %{$_[1]};

    return $self;
}

sub stringify {
    my $self = shift;

    return $self->value();
}

# Comparison operator
sub compare { 
    my ($s1, $s2, $inverted) = @_;
    return $inverted ? "$s2" cmp "$s1" : "$s1" cmp "$s2";
} 

=head1 AUTHOR

Knut Arne Bjørndal, C<< <bob at cakebox.net> >>

=head1 BUGS

Please report any bugs or feature requests to C<bug-geo-osm-mapfeatures-feature-value at rt.cpan.org>, or through
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

1; # End of Geo::OSM::MapFeatures::Feature::Value
