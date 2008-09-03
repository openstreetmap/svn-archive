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
use tahlib;
use lib::TahConf;
use lib::TahExceptions;

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
# it's handed a comma separated string of layers when setting, eg. 
# $r->layers('tile,maplint')
# returns an array of layernames when reading (empty array if unset)
#-----------------------------------------------------------------------------
sub layers
{
    my $self = shift;
    my $layers_str = shift;
    if (defined $layers_str) { @{$self->{layers}} =  split(/,/,$layers_str);}
    return @{$self->{layers}};
}


#-----------------------------------------------------------------------------
# retrieve (read-only) the required layers of a request
# usage, e.g. $r->layers_str
# returns comma separated string of layers , eg. 'tile,maplint'
# returns empty string '' if unset.
#-----------------------------------------------------------------------------
sub layers_str
{
    my $self = shift;
    my $layers_str = join(",", @{$self->{layers}});
    return $layers_str;
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

        die TahError->new("ServerError", "Error reading request from server") unless ($Requeststring);
        
        # $ValidFlag is 'OK' (got request) or 'XX' (error occurred, or no work for us)
        # $Version denotes the version of the request taking protocol the server speaks. It's currently at '5'
        ($ValidFlag,$Version) = split(/\|/, $Requeststring);

        # First check that we understand the server protocol
        if ($Version < 5 or $Version > 5)
        {
            my $message = "Server is speaking a different version of the protocol to us ($Version).\n"
                . "Check to see whether a new version of this program was released!";
            die TahError->new("ServerError", $message);
        }

        if ($ValidFlag eq "OK")
        {
            if ($Version == 5) # this may seem nonsensical, but we'll need this once we introduce a new version
            {   # We got a valid request here!

                my ($Z,$X,$Y,$Layers,$lastModified,$complexity);
                ($ValidFlag,$Version,$X,$Y,$Z,$Layers,$lastModified,$complexity) = split(/\|/, $Requeststring);
                $self->ZXY($Z,$X,$Y);
                $self->layers($Layers);
                $self->{'lastModified'} = $lastModified;
                $self->{'complexity'} = $complexity;
                $success = 1;  # set to 1, so we could end the loop
                # got request, now check that it's not too complex
                if ($self->{Config}->get('MaxTilesetComplexity'))
                {   #the setting is enabled
                    if ($complexity > $self->{Config}->get('MaxTilesetComplexity'))
                    {   # too complex!
                        $success = 0;  # set to 0, need another loop
                        ::statusMessage("Ignoring too complex tile (".$self->ZXY_str.')',1,3);
                        # putbackToServer waits 15 secs before continuing, so we don't get the same time back 
                        eval {
                            $self->putBackToServer("TooComplex");
                        };
                    }
                }
            }
    
        }
        elsif ($ValidFlag eq "XX")
        {
            ($ValidFlag, $Version, my $reason) = split(/\|/, $Requeststring);
            if ($reason =~ /Invalid username/)
            {
                die TahError->new("AuthenticationError",
                                  "ERROR: Authentication failed - please check your username and password in 'authentication.conf'.\n\n"
                                  . "! If this worked just yesterday, you now need to put your osm account e-mail and password there.");
            }
            elsif ($reason =~ /Invalid client version/)
            {
                die TahError->new("ClientVersionError", "ERROR: This client version (" . $self->{Config}->get("ClientVersion")
                                  . ") was not accepted by the server.");  ## this should never happen as long as auto-update works
            }
            elsif ($reason =~ /No requests in queue/)
            {
                $success = 0; # set to 0, need another loop
                ::talkInSleep("No Requests on server",60);
            }
            elsif ($reason =~ /Check your client/)
            {
                die TahError->new("GeneralClientError", "ERROR: This client needs manual intervention. Server told us: \"$reason\"");
            }
            else
            {
                die TahError->new("ServerError", "Unknown server response: $Requeststring");
            }
        }
        else
        {   # ValidFlag was neither 'OK' nor 'XX'. This should NEVER happen.
              die TahError->new("ServerError", "Unknown server response ($Requeststring), ValidFlag neither 'OK' nor 'XX'");
	}

        if ($self->is_unrenderable())
        {
            $success = 0;   # we need to loop yet again
            ::statusMessage("Ignoring unrenderable tile (".$self->ZXY_str.')',1,3);
            # putbackToServer waits 15 secs before continuing, so we don't get the same time back 
            $self->putBackToServer("Unrenderable");
        }
    } while (!$success);

    # Information text to say what's happening
    ::statusMessage("Got work from the server: ".$self->layers_str.' ('.$self->ZXY_str.')', 0, 6);
}


#-----------------------------------------------------------------------------
# actually get a request string from the server via HTTP
# returns 0 on failure, or a raw string that describes the request otherwise 
#-----------------------------------------------------------------------------
sub getRequestStringFromServer
{
    my $self = shift();
    my $Request;
    my $URL = $self->{Config}->get("RequestURL");
    
    my $ua = LWP::UserAgent->new(timeout => 240, protocols_allowed => ['http']);
    $ua->agent("tilesAtHome ($^O)");
    $ua->env_proxy();
    push @{ $ua->requests_redirectable }, 'POST';
    my $res = $ua->post($URL, Content_Type => 'form-data',
      Content => [ user => $self->{Config}->get("UploadUsername"),
                   passwd => $self->{Config}->get("UploadPassword"),
                   version => $self->{Config}->get("ClientVersion"),
                   layerspossible => $self->{Config}->get("LayersCapability"),
                   client_uuid => ::GetClientId() ]);

    (print "Request string from server: ", $res->content,"\n") if ($self->{Config}->get("Debug"));      

    if(!$res->is_success())
    {   # getting request string from server failed here
        die TahError->new("ServerError", "Unable to get request string from server");
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
        die TahError->new("ServerError", "Error reading response from server");
    }
    
    ::talkInSleep("Waiting before new tile is requested", 15);
}

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

=item C<< ->layers('comma-separated-layerstring') >> (get or set) 

(get returns array of layernames)

=item C<< ->layers_str() >> (read only) returns comma-separated layer string)

=item C<< ->{lastModified} >> (read-only attribute)

is set to the unix timestamp of the tileset file on server. The server responds with  0 if it doesn't have the tileset yet. Although nothing prevents you technically from setting this attribute it only makes sense to use it for reading.

=item C<< ->{complexity} >> (read-only attribute)

is set to the byte size of the tileset file onthe server. It will be set to 0 if the server does not have the tileset yet.

=back

=head3 RETRIEVING AND PUTTING BACK REQUESTS WITH ERROR

=over

=item ->putBackToServer("cause")

=item ->fetchFromServer()

=back

=cut
