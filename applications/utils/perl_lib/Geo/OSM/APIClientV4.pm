##################################################################
## APIClientV4.pm - General Perl client for the API             ##
## By Martijn van Oosterhout <kleptog@svana.org>                ##
##                                                              ##
## Currently only supports uploading. Note the package actually ##
## creates a package named Geo::OSM::APIClient so upgrades to   ##
## later versions will be easier.                               ##
## Licence: LGPL                                                ##
##################################################################

use LWP::UserAgent;
use strict;

package Geo::OSM::APIClient;
use MIME::Base64;
use HTTP::Request;
use Carp;

sub new
{
  my( $class, %attr ) = @_;
  
  my $obj = bless {}, $class;

  my $url = $attr{api};  
  if( not defined $url )
  {
    croak "Did not specify aip url";
  }

  $url =~ s,/$,,;   # Strip trailing slash
  $obj->{url} = $url;
  $obj->{client} = new LWP::UserAgent(agent => 'Geo::OSM::APIClientV4');
  
  if( defined $attr{username} and defined $attr{password} )
  {
    if( $obj->{url} =~ m,http://([\w.]+)/, )
    {
      $obj->{client}->credentials( "$1:80", "Web Password",  $attr{username}, $attr{password} );
    }
    my $encoded = MIME::Base64::encode_base64("$attr{username}:$attr{password}","");
    $obj->{client}->default_header( "Authorization", "Basic $encoded" );
  }
  
  return $obj;
}

sub last_error_code
{
  return shift->{last_error}->code;
}

sub last_error_message
{
  return shift->{last_error}->message;
}

sub create
{
  my( $self, $ent ) = @_;
  my $oldid = $ent->id;
  $ent->set_id(0);
  my $content = $ent->full_xml;
  $ent->set_id($oldid);
  my $req = new HTTP::Request PUT => $self->{url}."/".$ent->type()."/create";
  $req->content($content);
  
#  print $req->as_string;
  
  my $res = $self->{client}->request($req);
  
#  print $res->as_string;

  if( $res->code == 200 )
  {
    return $res->content
  }
    
  $self->{last_error} = $res;
  return undef;
}

sub modify
{
  my( $self, $ent ) = @_;
  my $content = $ent->full_xml;
  my $req = new HTTP::Request PUT => $self->{url}."/".$ent->type()."/".$ent->id();
  $req->content($content);
  
#  print $req->as_string;
  
  my $res = $self->{client}->request($req);
  
  return $ent->id() if $res->code == 200;
  $self->{last_error} = $res;
  return undef;
}

sub delete
{
  my( $self, $ent ) = @_;
  my $content = $ent->full_xml;
  my $req = new HTTP::Request DELETE => $self->{url}."/".$ent->type()."/".$ent->id();
#  $req->content($content);
  
#  print $req->as_string;
  
  my $res = $self->{client}->request($req);
  
  return $ent->id() if $res->code == 200;
  $self->{last_error} = $res;
  return undef;
}

1;
