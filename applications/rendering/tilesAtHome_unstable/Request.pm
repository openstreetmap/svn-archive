# A 'Request' encapsulates a render request.
=pod

=head1 Request for a t@h render

=head2 License and authors

 # Copyright 2008, by Sebastian Spaeth
 # licensed under the GPL v2 or (at your option) any later version.

=head2 Overview

The I<Request> object encapsulates a render request from the t@h server. It is used to fetch a request from the server and contains all the contect information of the request that we have. It is then handed a render method that processes it. I<Request> can also return the request back to the server in case of an error. It encapsulates all the request-related communication with the server (except for uploading the resulting files).

=cut

#see rest of the pd documentation is at the end of this file. Please keep 
# the description of public methofs/attributes up to date
package Request;

use warnings;
use strict;
use LWP::UserAgent;
use Error qw(:try);
use tahlib;
use lib::TahConf;

#unrenderable is a class global hash that keeps unrenderable tilesets as ['z x y']=1
our %unrenderable = ();

#-----------------------------------------------------------------------------
# Request can be instantiated with ->new(Z,X,Y), alternatively set those with ->ZXY(z,x,y) later.
# e.g. my $r = new Request or my $r = Request->new()
#-----------------------------------------------------------------------------
sub new 
{
    my $class = shift;
    my $self = {
        MIN_Z => undef,
        X => undef,
        Y  => undef,
        lastModified => 0,  #unix timestamp of file on server
        complexity => 0,    #byte size of file on server
        layers => [],
        Config => TahConf->getConfig(),
    };
    bless $self, $class;
    $self->ZXY(@_);
    return $self;
}

#-----------------------------------------------------------------------------
# set and/or retrieve the z,x,y of a request
#-----------------------------------------------------------------------------
sub ZXY
{
    my $self = shift;
    my ($new_z, $new_x, $new_y) = @_;
    return ($self->Z($new_z),$self->X($new_x),$self->Y($new_y))
}

#-----------------------------------------------------------------------------
# retrieve (read-only) the z,x,y as string in form 'z,x,y'
#-----------------------------------------------------------------------------
sub ZXY_str
{
    my $self = shift;
    return $self->Z.','.$self->X.','.$self->Y;
}

#-----------------------------------------------------------------------------
# set and/or retrieve the z of a request
#-----------------------------------------------------------------------------
sub Z
{
    my $self = shift;
    my $new_z = shift;
    if (defined($new_z)) {$self->{MIN_Z} = $new_z;}
    return $self->{MIN_Z}
}

#-----------------------------------------------------------------------------
# set and/or retrieve the x of a request
#-----------------------------------------------------------------------------
sub X
{
    my $self = shift;
    my $new_x = shift;
    if (defined($new_x)) {$self->{X} = $new_x;}
    return $self->{X}
}

#-----------------------------------------------------------------------------
# set and/or retrieve the y of a request
#-----------------------------------------------------------------------------
sub Y
{
    my $self = shift;
    my $new_y = shift;
    if (defined($new_y)) {$self->{Y} = $new_y;}
    return $self->{Y}
}

#-----------------------------------------------------------------------------
# set and/or retrieve the required layers of a request
# it's handed an array of layers when setting, eg. 
# $r->layers('tile', 'maplint')
# returns an array of layernames when reading (empty array if unset)
#-----------------------------------------------------------------------------
sub layers
{
    my $self = shift;
    my @layers = @_;
    if (@layers) {
        @$self->{layers} =  @layers;
    }
    return @{$self->{layers}};
}


#-----------------------------------------------------------------------------
# Set or retrieve the required layers of a request
# usage, e.g. $r->layers_str([$layers_string])
# returns comma separated string of layers, eg. 'tile,maplint'
# returns empty string '' if unset.
#-----------------------------------------------------------------------------
sub layers_str
{
    my $self = shift;
    my $layers_str = shift;
    if (defined $layers_str) {
        @{$self->{layers}} =  split(/,/,$layers_str);
    }
    return join(",", @{$self->{layers}});
}


#-----------------------------------------------------------------------------
# set and/or retrieve the last modified timestamp of a request
#-----------------------------------------------------------------------------
sub lastModified
{
    my $self = shift;
    my $time = shift;
    if (defined($time)) {
        $self->{lastModified} = $time;
    }
    return $self->{lastModified}
}

#-----------------------------------------------------------------------------
# set and/or retrieve the complexity of a request
#-----------------------------------------------------------------------------
sub complexity
{
    my $self = shift;
    my $complexity = shift;
    if (defined($complexity)) {
        $self->{complexity} = $complexity;
    }
    return $self->{complexity}
}

#-----------------------------------------------------------------------------
# get/set the unrenderable status of a tileset
# takes 1 as parameter if setting as unrenderable
# without parrameters returns the unrenderable status
# currently does not check that ZXY are initialized.
#-----------------------------------------------------------------------------
sub is_unrenderable
{
    my $self = shift;
    # TODO check that XYZ are initialized
    my $zxy= $self->Z.' '.$self->X.' '.$self->Y;
    $unrenderable{$zxy} = 1 if @_;
    return $unrenderable{$zxy};
}

#-----------------------------------------------------------------------------
# this is called when the client encounters errors in processing a tileset,
# it tells the server the tileset will not be returned by the client.
# $req: a 'Request' object containing z,x,y of the current request
# $Cause: a string describing the failure reason
# returns: (success,reason)
# success=1 on success and 0 on failure,
# reason is a string describing the error
#-----------------------------------------------------------------------------
sub putBackToServer 
{
    my ($self, $Cause) = @_;

    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);

    $ua->protocols_allowed( ['http'] );
    $ua->agent("tilesAtHome");
    $ua->env_proxy();
    push @{ $ua->requests_redirectable }, 'POST';

    ::statusMessage(sprintf("Putting job (%d,%d,%d) back due to '%s'",$self->{MIN_Z},$self->{X},$self->{Y},$Cause),1,0);
    my $res = $ua->post($self->{Config}->get("ReRequestURL"),
              Content_Type => 'form-data',
              Content => [ x => $self->{X},
                           y => $self->{Y},
                           min_z => $self->{MIN_Z},
                           user => $self->{Config}->get("UploadUsername"),
                           passwd => $self->{Config}->get("UploadPassword"),
                           version => $self->{Config}->get("ClientVersion"),
                           cause => $Cause,
                           client_uuid => ::GetClientId() ]);

    if(!$res->is_success())
    {
        throw RequestError "Error reading response from server";
    }
    
    ::talkInSleep("Waiting before new tile is requested", 15);
}

package RequestError;
use base 'Error::Simple';

1;

=pod 

=head2 Public methods

=head3 CREATION & COORDINATES:

=over

=item C<< ->new(Z,X,Y) >> (set)

A I<Request> can be instantiated with ->new(Z,X,Y). Alternatively those coordinates can be set with ->ZXY(z,x,y) later. Of course you don't need to set those coordinates if you plan to retrieve a request from the server.

e.g. my $r = new Request or my $r = Request->new()

=item C<< ->ZXY(z,x,y) >> (set or get)

=item C<< ->ZXY_str >> (read-only) returns 'z,x,y' as string

=item C<< ->X(x) >> (set or get)

=item C<< ->Y(y) >> (set or get)

=item C<< ->Z(z) >> (set or get)

=item C<< ->layers(@layers) >> (set or get) 

Sets and returns array of layernames

=item C<< ->layers_str() >> (set or get)

Sets and returns comma-separated layer string

=item C<< ->lastModified([$timestamp]) >> (set or get)

Sets and returns unix timestamp of the tileset file on server. The server
responds with  0 if it doesn't have the tileset yet. Although nothing prevents
you technically from setting this attribute it only makes sense to use it for
reading.

=item C<< ->complexity([$complexity]) >> (set or get)

Sets and returns the byte size of the tileset file on the server. It will be set
to 0 if the server does not have the tileset yet.

=back

=head3 PUTTING BACK REQUESTS WITH ERROR

=over

=item ->putBackToServer("cause")

=back

=cut
