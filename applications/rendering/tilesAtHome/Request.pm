# A 'Request' encapsulates a render request.
#
# Copyright 2006, by Sebastian Spaeth
# licensed under the GPL v2 or (at your option) any later version.
package Request;

use LWP::UserAgent;
use tahlib;

#""" Request can be instantiated with (Z,X,Y), alternatively set those with ->ZXY(z,x,y) later."""
# my $r = new Request or my $r = Request->new()
sub new 
{
    my $class = shift;
    my $self = {
        MIN_Z => shift,
        X => shift,
        Y  => shift,
    };
    bless $self, $class;
    return $self;
}

# set and/or retrieve the z,x,y of a request
sub ZXY
{
    my $self = shift;
    my ($new_z, $new_x, $new_y) = @_;
    return ($self->Z($new_z),$self->X($new_x),$self->($new_y))
}

# set and/or retrieve the z of a request
sub Z
{
    my $self = shift;
    my $new_z = shift;
    if (defined($new_z)) {$self->{MIN_Z} = $new_z;}
    return $self->{MIN_Z}
}

# set and/or retrieve the x of a request
sub X
{
    my $self = shift;
    my $new_x = shift;
    if (defined($new_x)) {$self->{X} = $new_x;}
    return $self->{X}
}

# set and/or retrieve the y of a request
sub Y
{
    my $self = shift;
    my $new_y = shift;
    if (defined($new_y)) {$self->{Y} = $new_y;}
    return $self->{Y}
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

    # do not do this if called in xy mode!
    return if($::Mode eq "xy");
    
    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 360);

    $ua->protocols_allowed( ['http'] );
    $ua->agent("tilesAtHome");
    $ua->env_proxy();
    push @{ $ua->requests_redirectable }, 'POST';

    ::statusMessage(sprintf("Putting job (%d,%d,%d) back due to '%s'",$self->{MIN_Z},$self->{X},$self->{Y},$Cause),1,0);
    my $res = $ua->post($::Config->get("ReRequestURL"),
              Content_Type => 'form-data',
              Content => [ x => $self->{X},
                           y => $self->{Y},
                           min_z => $self->{MIN_Z},
                           user => $::Config->get("UploadUsername"),
                           passwd => $::Config->get("UploadPassword"),
                           version => $::Config->get("ClientVersion"),
                           cause => $Cause,
                           client_uuid => ::GetClientId() ]);

    if(!$res->is_success())
    {
        return (0, "Error reading response from server");
    }
    
    ::talkInSleep("Waiting before new tile is requested", 10);
    return (1,"OK")
}

true;
