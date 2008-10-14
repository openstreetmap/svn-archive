# This class encapsulates access to the oceantiles database.
=pod

=head1 Oceantiles status data access

=head2 License and authors

 # Copyright 2008, by Dirk Stöcker
 # Copyright 2007, by Oliver White
 # licensed under the GPL v2 or (at your option) any later version.

=head2 Overview

The I<OceanTiles> object encapsulates the access to the tile status database.

=cut

#see rest of the pd documentation is at the end of this file. Please keep
# the description of public methofs/attributes up to date
package OceanTiles;

use warnings;
use strict;

#-----------------------------------------------------------------------------
# OceanTiles can be instantiated with ->new(filename) or ->new().
# e.g. my $r = new Request or my $r = Request->new("oceantiles_12.dat")
#-----------------------------------------------------------------------------
sub new
{
    my $class = shift;
    my $self = {
        zoom => 12,
    };
    my $filename = shift || "png2tileinfo/oceantiles_".$self->{zoom}.".dat";
    my $fh;
    return undef if !open($fh,"<",$filename);
    $self->{file} = $fh;
    bless $self, $class;
    return $self;
}

# pass x and y coordinate
sub getState
{
    my ($self, $X, $Y) = @_;

    my $tileoffset = ($Y * (2**$self->{zoom})) + $X;

    seek $self->{file}, int($tileoffset / 4), 0;
    my $buffer;
    read $self->{file}, $buffer, 1;
    $buffer = substr( $buffer."\0", 0, 1 );
    $buffer = unpack "B*", $buffer;
    my $str = substr( $buffer, 2*($tileoffset % 4), 2 );

    # $str eq "00" => unknown (not yet checked)
    # $str eq "01" => known land
    # $str eq "10" => known sea
    # $str eq "11" => known edge tile

    if ($str eq "10") { return "sea"; }
    elsif ($str eq "01") { return "land"; }
    elsif ($str eq "11") { return "mixed"; }
    return "unknown";
}

1;
