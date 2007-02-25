#!/usr/bin/perl
#-----------------------------------------------------------------------
# Tests HTTP connectivity to a load of servers, and displays the result
# on a wiki page.
#
# Usage: need to supply wiki username/password, and set the wiki page
# Check list of servers, then just run the program occasionally.
# See the results on wiki.openstreetmap.org recent changes
#
# OJW, 2007. GNU GPL v2 or later
#----------------------------------------------------------------------
use MediaWiki;
use LWP::UserAgent;
use strict;

TestServers("www","dev","wiki","tile","tilegen");


sub TestServers{
  # Given a list of OSM servers to check, put their current status on the wiki
  # TODO: cache results and only upload on a change in status
  
  # Wiki header
  my $Result = "==Server status==\n";  
  $Result .= "{| border=1 cellspacing=0\n";
  
  foreach my $Server(@_){  
  
    # Test connectivity to the server
    # TODO: consider more appropriate ways of testing the servers' responses
    my ($OK, $Status) = testServer("http://$Server.openstreetmap.org/");
    
    # Format the wikitext (red/green backgrounds etc.)
    my $Colour = $OK ? "#BFB":"#FBB";
    my $Style = "background-color:$Colour;";
    $Style .= "font-weight:bold;" if(!$OK);
    $Style = "style=\"$Style\"";
    
    # Link to dev stats
    my $Link = "http://openstreetmap.org/munin/openstreetmap/$Server.openstreetmap.html";
    
    # Add to the wikitable
    $Result .= "|-\n";
    foreach my $Data("[$Link $Server]", $OK ? "OK" : "DOWN", $Status){
      $Result .= "| $Style | $Data\n";
    }
  }
  
  # Wiki footer
  $Result .= "|}\n";
  
  # Debug: print result to screen
  print $Result; 
  
  # Optional: exit here to just test the program and not upload the results
  exit if(0);

  # Upload to the wiki  
  # Config: which page to upload to
  UploadToWiki("Sandbox/statusBotTest", $Result);
}

sub UploadToWiki{
  # Given a page name and some wikitext, update an OSM wiki page
  my ($Pagename, $Data) = @_;
  
  my $c = MediaWiki->new;
  
  # Config: Username, password
  my $is_ok = $c->setup({
          'bot' => { 'user' => 'WikiUsername', 'pass' => 'WikiPassword' },
          'wiki' => {
                  'host' => 'wiki.openstreetmap.org',
                  'path' => '/'
          }});

  # Check whether connected OK to wiki
  die("Can't connect to wiki - error: ". $c->{error} . "\n(see MediaWiki module on CPAN)\n") if(!$is_ok);
  printf "Status %s, user %s\n",  $is_ok, $c->user();
  
  # Update the wiki page
  print $c->text($Pagename, $Data);
}

sub testServer{
  # Given a webserver name, test whether it can be connected to
  my $Browser = LWP::UserAgent->new;
  
  # Browser options (see LWP pages on CPAN)
  $Browser->timeout(20);
  
  # Name to supply as the user-agent
  $Browser->agent("ServerStatusToWiki/0.7");
  
  # Try to fetch a page over HTTP
  my $Response = $Browser->get(shift());
  
  # Returns: boolean success, and the "200 OK"-style status line
  return($Response->is_success, $Response->status_line);
}
