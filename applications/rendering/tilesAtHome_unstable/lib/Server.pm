# A 'Server' encapsulates all server communications.
=pod

=head1 Server class for all communications of the t@h render

=head2 License and authors

 # Copyright 2008, by Matthias Julius and others
 # licensed under the GPL v2 or (at your option) any later version.

=head2 Overview

The I<Server> class encapsulates all server communications of the t@h client. It
is used to fetch a request from and return to the server as well as downloading
of data from the OSM API and uploading of rendered tilesets.

=cut

# See rest of the pd documentation is at the end of this file. Please keep 
# the description of public methods/attributes up to date.
package Server;

use warnings;
use strict;
use LWP::UserAgent;
use Error qw(:try);
use tahlib;
use lib::TahConf;
use Request;

#-------------------------------------------------------------------------------
# Server can be instantiated with ->new().
#-------------------------------------------------------------------------------
sub new 
{
    my $class = shift;
    my $self = {
        Config => TahConf->getConfig(),
        ua => undef,
    };
    bless $self, $class;

    my $ua = LWP::UserAgent->new(timeout => 240, protocols_allowed => ['http']);
    $ua->agent("tilesAtHome ($^O)");
    $ua->env_proxy();
    push @{ $ua->requests_redirectable }, 'POST';
    $self->{ua} = $ua;

    return $self;
}


#-------------------------------------------------------------------------------
# get a string from the server via HTTP
# getString($URL)
# $URL is the URL to fetch
# returns a raw string that describes the request
#-------------------------------------------------------------------------------
sub getString
{
    my $self = shift();
    my $URL = shift();

    my $res = $self->{ua}->post($URL, Content_Type => 'form-data',
                                Content => [ user => $self->{Config}->get("UploadUsername"),
                                             passwd => $self->{Config}->get("UploadPassword"),
                                             version => $self->{Config}->get("ClientVersion"),
                                             layerspossible => $self->{Config}->get("LayersCapability"),
                                             client_uuid => ::GetClientId() ]);

    (print "Request string from server: ", $res->content,"\n") if ($self->{Config}->get("Debug"));      

    if(!$res->is_success())
    {   # getting request string from server failed here
        throw ServerError "Unable to get string from server", "TempError";
    }

    # got a server reply here
    my $res_string = $res->content();
    chomp($res_string);
    return $res_string;  ## FIXME: check single line returned. grep?
}


#-------------------------------------------------------------------------------
# Get a new render request from the server that should be rendered
# returns a Request object
#-------------------------------------------------------------------------------
sub fetchRequest
{
    my $self = shift;
    my $Request;

    my $Requeststring = $self->getString($self->{Config}->get("RequestURL"));

    throw ServerError "Server returned no content", "TempError" unless $Requeststring;

    # $ValidFlag is 'OK' (got request) or 'XX' (error occurred, or no work for us)
    # $Version denotes the version of the request taking protocol the server speaks. It's currently at '5'
    my ($ValidFlag, $Version) = split(/\|/, $Requeststring);

    # First check that we understand the server protocol
    if ($Version < 5 or $Version > 5) {
        throw ServerError "Server is speaking a different version of the protocol to us ($Version).\n"
            . "Check to see whether a new version of this program was released!", "ProtocolVersionError";
    }

    if ($ValidFlag eq "OK") {
        # We got a valid request here!
        if ($Version == 5) {# this may seem nonsensical, but we'll need this once we introduce a new version
            my ($Z, $X, $Y, $Layers, $lastModified, $complexity);
            ($ValidFlag, $Version, $X, $Y, $Z, $Layers, $lastModified, $complexity) = split(/\|/, $Requeststring);
            $Request = Request->new($Z,$X,$Y);
            $Request->layers_str($Layers);
            $Request->lastModified($lastModified);
            $Request->complexity($complexity);
        }
    }
    elsif ($ValidFlag eq "XX") {
        ($ValidFlag, $Version, my $reason) = split(/\|/, $Requeststring);
        if ($reason =~ /Invalid username/) {
            throw ServerError "ERROR: Authentication failed - please check your username and password in 'authentication.conf'.\n\n"
                . "! If this worked just yesterday, you now need to put your osm account e-mail and password there.", "PermError";
        }
        elsif ($reason =~ /Invalid client version/) {
            throw ServerError "ERROR: This client version (" . $self->{Config}->get("ClientVersion")
                . ") was not accepted by the server.", "PermError";  ## this should never happen as long as auto-update works
        }
        elsif ($reason =~ /No requests in queue/) {
            throw ServerError "No Requests on server", "NoJobs";
        }
        elsif ($reason =~ /You have more than (\d+) active requests/) {
            throw ServerError "Is your client broken or have you just uploaded like crazy? \"$reason\"", "NoJobs";
        }
        else {
            throw ServerError "Unknown server response: $Requeststring", "TempError";
        }
    }
    else {
        # ValidFlag was neither 'OK' nor 'XX'. This should NEVER happen.
        throw ServerError "Unknown server response ($Requeststring), ValidFlag neither 'OK' nor 'XX'", "TempError";
    }
    return $Request;
}


#-----------------------------------------------------------------------------
# this is called when the client encounters errors in processing a tileset,
# it tells the server the tileset will not be returned by the client.
# Server->putRequestBack($Request, $Cause)
# $Request: a 'Request' object containing z,x,y of the current request
# $Cause:   a string describing the failure reason
#-----------------------------------------------------------------------------
sub putRequestBack 
{
    my ($self, $Request, $Cause) = @_;

    my $ua = $self->{ua};

    ::statusMessage(sprintf("Putting job (%d,%d,%d) back due to '%s'", $Request->Z, $Request->X, $Request->Y, $Cause), 1, 0);
    my $res = $ua->post($self->{Config}->get("ReRequestURL"),
                        Content_Type => 'form-data',
                        Content => [ x => $Request->X,
                                     y => $Request->Y,
                                     min_z => $Request->Z,
                                     user => $self->{Config}->get("UploadUsername"),
                                     passwd => $self->{Config}->get("UploadPassword"),
                                     version => $self->{Config}->get("ClientVersion"),
                                     cause => $Cause,
                                     client_uuid => ::GetClientId() ]);

    if(!$res->is_success()) {
        throw ServerError "Error reading response from server", "TempError";
    }
}


#-------------------------------------------------------------------------------
# Server will throw esceptions of class ServerError

package ServerError;
use base 'Error::Simple';

1;

=pod 

=head2 Public methods

=head3 Class methods

=over

=item C<< ->new() >>

A I<Server> can be instantiated with ->new().

e.g. my $server = new Server or my $server = Server->new()

=back

=head3 Instance methods

=over

=item C<< ->getString($URL) >>

Returns the string returned by the server using $URL as the URL.

=item C<< ->fetchRequest() >>

Returns a Request object.

=item C<< ->putRequestBack($Request, $Cause) >>

Puts a request back to the server, indicating the cause.

=over

=item C< $Request > is a Request object

=item C< $Cause > is a string

=back

=back

=cut
