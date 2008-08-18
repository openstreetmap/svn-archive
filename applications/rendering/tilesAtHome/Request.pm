# A 'Request' encapsulates a render request.
#
# Copyright 2008, by Sebastian Spaeth
# licensed under the GPL v2 or (at your option) any later version.

## TODO: use proper perldoc format here
## public API:

## CREATION & COORDINATES:
## ->new(Z,X,Y) (set); ->ZXY(z,x,y) (set or get); ->X(x) (set or get)
## ->Y(y) (set or get), ->Z(z) (set or get)

## RETRIEVING AND PUTTING BACK REQUESTS WITH ERROR
## ->putBackToServer("cause")
## ->fetchFromServer()

package Request;

use strict;
use LWP::UserAgent;
use tahlib;

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
        layerstr => undef, #comma seperated string of requested layers
        lastModified => 0,  #unix timestamp of file on server
        complexity => 0,    #byte size of file on server
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
# Get a new render request from the server that should be rendered
# returns a tuple (success, reason) describing the result of the call
# success: is 1 on successfully retrieving a request, 0 otherwise 
# (even if the server simply had no work for us)
# reason: is a string describing the error condition.
# if success==1, the request will have set the z,x,y attributes as well as the
# last-modified and complexity attributes of the 'Request' object.
#-----------------------------------------------------------------------------
sub fetchFromServer
{
    my $self = shift;
    my $success;

    do
    {
        my $ValidFlag;
        my $Version;

        $success=0;
        my $Requeststring = $self->getRequestStringFromServer();

        return (0, "Error reading request from server") unless ($Requeststring);
        
        # $ValidFlag is 'OK' (got request) or 'XX' (error occurred, or no work for us)
        # $Version denotes the version of the request taking protocol the server speaks. It's currently at '5'
        ($ValidFlag,$Version) = split(/\|/, $Requeststring);

        # First check that we understand the server protocol
        if ($Version < 4 or $Version > 5)
        {
            #TODO use statusMessage here
            print STDERR "\n";
            print STDERR "Server is speaking a different version of the protocol to us.\n";
            print STDERR "Check to see whether a new version of this program was released!\n";
            cleanUpAndDie("ProcessRequestFromServer:Request API version mismatch, exiting \n".$Requeststring,"EXIT",1,$$);
            ## No need to return, we exit the program at this point
        }

        if ($ValidFlag eq "OK")
        {
            if ($Version == 5) # this may seem nonsensical, but we'll need this once we introduce a new version
            {
                my ($Z,$X,$Y,$Layers,$lastModified,$complexity);
                ($ValidFlag,$Version,$X,$Y,$Z,$Layers,$lastModified,$complexity) = split(/\|/, $Requeststring);
                $self->ZXY($Z,$X,$Y);
                # TODO implement getter/setter methods for layerstr, lastmodified and complexity
                $self->{'layerstr'} = $Layers;
                $self->{'lastModified'} = $lastModified;
                $self->{'complexity'} = $complexity;
                $success = 1;  # set to 1, so we could end the loop
            }
    
        }
        elsif ($ValidFlag eq "XX")
        {
            $ValidFlag, my $reason = split(/\|/, $Requeststring);
            if ($reason =~ /Invalid username/)
            {
                die "ERROR: Authentication failed - please check your username "
                        . "and password in 'authentication.conf'.\n\n"
                        . "! If this worked just yesterday, you now need to put your osm account e-mail and password there.";
            }
            elsif ($reason =~ /Invalid client version/)
            {
                die "ERROR: This client version (".$::Config->get("ClientVersion").") was not accepted by the server.";  ## this should never happen as long as auto-update works
            }
            else
            {
                return (0, "Unknown server response");
            }
        }
        else
        {   # ValidFlag was neither 'OK' nor 'XX'. This should NEVER happen.
              return (0, "Unknown server response, ValidFlag neither 'OK' nor 'XX'");
	}

        if ($self->is_unrenderable())
        {
            $success = 0;   # we need to loop yet again
            $self->putBackToServer("Unrenderable");
            # make sure we don't loop like crazy should we get another or the same unrenderable tile back over and over again
            ::talkInSleep("Ignoring unrenderable tile (".$self->Z.', '.$self->X.', '.$self->Y.')',20);
        }
    } while (!$success);

    # Information text to say what's happening
    ::statusMessage("Got work from the server", 0, 6);
    return (1, "");
}


#-----------------------------------------------------------------------------
# actually get a request string from the server via HTTP
# returns 0 on failure, or a raw string that describes the request otherwise 
#-----------------------------------------------------------------------------
sub getRequestStringFromServer
{
    my $Request;
    my $URL = $::Config->get("RequestURL");
    
    my $ua = LWP::UserAgent->new(timeout => 360, protocols_allowed => ['http'], agent =>"tilesAtHome");
    $ua->env_proxy();
    push @{ $ua->requests_redirectable }, 'POST';
    my $res = $ua->post($URL, Content_Type => 'form-data',
      Content => [ user => $::Config->get("UploadUsername"),
                   passwd => $::Config->get("UploadPassword"),
                   version => $::Config->get("ClientVersion"),
                   layerspossible => $::Config->get("LayersCapability"),
                   client_uuid => ::GetClientId() ]);

    (print "Request string from server: ", $res->content) if ($::Config->get("Debug"));      

    if(!$res->is_success())
    {   # getting request string from server failed here
        return 0;
    }
    else
    {   # got a server reply here
        $Request = $res->content;  ## FIXME: check single line returned. grep?
        chomp $Request;
    }

    return $Request;
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

1;
