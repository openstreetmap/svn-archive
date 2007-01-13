##################################################################
package Geo::Filter::Area;
##################################################################

use strict;
use warnings;
use Carp;

use Data::Dumper;

# ------------------------------------------------------------------
my $AREA_DEFINITIONS = {
    #                     min    |    max  
    #               [  lat , lon ,  lat, lon ]
    uk         => [ [  49.7,-7.6,  58.8, 3.2 ], # Great Britain (GB)
	            [  49.9,-5.8,  54.0, 0.8 ], # NI
		    ],
    ni         => [ [  49.9,-5.8,  54.0, 0.8 ] ], # NI
    gb         => [ [  49.7,-7.6,  58.8, 3.2 ] ],, # Great Britain (GB)
		    
    iom        => [ [  49  , -11,   64,   3  ] ],
    france     => [ [  42.3,-1.7, 51.1, 8.2  ] ],
    germany    => [ [  47  ,   5, 55.1,  16  ] ],
    hamburg    => [ [  53.40133  ,   9.623,   53.7676,  10.36  ] ],
    switzerland => [ [ 45.5 , 6 ,   47.5 , 10.3 ] ],
    spain      => [ [  35.5,  -9,   44,   4  ] ],
    europe     => [ [  35  , -12,   75,  35  ],
		    [  62.2,-24.4,66.8,-12.2 ], # Iceland
		    ],
    Iceland    => [ [  62.2,-24.4,66.8,-12.2 ] ],
    australia  => [ [ -44  , -9,    112, 154 ] ],
    africa     => [ [ -45  , -20,   30,  55  ] ],
    # Those eat up all memory on normal machines
    world_east => [ [ -90  , -30,   90, 180  ] ], 
    world_west => [ [ -90  ,-180,   90, -30  ] ],
    world      => [ [ -90  ,-180,   90, 180  ] ],
};

# ------------------------------------
my $stripe_lon     = -180;
my $stripe_step    = 45;
my $stripe_overlap = 0.2;
while ( $stripe_lon < 180 ){
    my $stripe_lon1=$stripe_lon+$stripe_step+$stripe_overlap;
    $AREA_DEFINITIONS->{"stripe_${stripe_lon}_${stripe_lon1}"} =
	[ [ -90,$stripe_lon,   
	    90, $stripe_lon1] ];
    $stripe_lon=$stripe_lon+$stripe_step;
}

# ------------------------------------
sub new($;@){
    my $class = shift;
    if( scalar(@_) % 2 ) {
	Carp::confess("uneven amount of options\n");
	};
    my %options  = @_;

    my $self={};
    my $area;
    if ( defined $options{lat_min} &&
	 defined $options{lon_min} &&
	 defined $options{lat_max} &&
	 defined $options{lon_max} 
	 ) {
	$self->{area_filters}= [
				[$options{lat_min},$options{lon_min},
				 $options{lat_max},$options{lon_max}]
				];
	$self->{area_name} = "[$options{lat_min},$options{lon_min}".
	    " .. ".
	    "$options{lat_max},$options{lon_max}]";
	    
    } elsif ( defined $options{area} ) {
	$area = delete  $options{area};
	if ( ! defined ( $AREA_DEFINITIONS->{$area} ) ) {
	    die "unknown area $area.\n".
		"Allowed Areas:\n\t".
		join("\n\t",$class->allowed_areas())."\n";
	}
	$self->{area_name} = $area;
	$self->{area_filters} = $AREA_DEFINITIONS->{$area};
    }
    
    if( scalar(@_) % 2 ) {
	Carp::confess("uneven amount of options for area->new()\n");
	};
    bless ($self,$class);
    return $self;
}

sub list_areas($) {
    my $class = shift;
    return join("\n",$class->allowed_areas());
}

sub allowed_areas($) {
    my $class = shift;
    return sort keys %{$AREA_DEFINITIONS};
}

sub name($) {
    my $self = shift;
    return $self->{area_name};
}

# ------------------------------------------------------------------

sub inside($$){
    my $self = shift;
    my $obj  = shift;

    my $area_filters=$self->{area_filters};
    #print "in_area(".Dumper(\$obj).")";;
    #print Dumper(\$area_filters);
    for my $a ( @{$area_filters}  ) {
	if (
	    $obj->{lat} >= $a->[0] &&
	    $obj->{lon} >= $a->[1] &&
	    $obj->{lat} <= $a->[2] &&
	    $obj->{lon} <= $a->[3] ) {
	    return 1;
	}
    }
    return 0;
}

# ------------------------------------------------------------------

my $test_area = Geo::Filter::Area->new(area=>"uk");
if ( $test_area->inside({lat=>10,lon=>10})) {
    Carp::confess("Area filters are not working\n");
};

1;


__END__

=head1 NAME

Area.pm

=head1 COPYRIGHT

Copyright 2006, Jörg Ostertag

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=head1 AUTHOR

Jörg Ostertag (planet-count-for-openstreetmap@ostertag.name)

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
