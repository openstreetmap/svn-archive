#!/usr/bin/perl
use LWP::Simple;
use LWP::UserAgent;
use Math::Trig;
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, requests module
# Downloads requests for tiles from the server, and stores them in a queue
#
# Contact OJW on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Oliver White
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-----------------------------------------------------------------------------
require("lib.pl");
require("../secret.pl");
my $Dir = "../requests";

# Use the -f lag to run the program continuously
DownloadContinuously($Dir, 10) if(shift() eq "-f");

DownloadRequest("../requests");

#-----------------------------------------------------------------------------
# Continuously download requests, to keep a "queue" of N files in the requests
# directory
#-----------------------------------------------------------------------------
sub DownloadContinuously(){
  my ($Dir, $QueueLength) = @_;
  while(1){
    # If the queue of requsts isn't full, then download one
    if(CountFilesInDir($Dir) < $QueueLength){
      DownloadRequest($Dir);
    }
    sleep(5);
  }
}

#-----------------------------------------------------------------------------
# Download a request for while tileset to render next
#-----------------------------------------------------------------------------
sub DownloadRequest(){
  my ($TempDir) = @_;
  
  my $LocalFilename = "$Dir/request_$$.txt"; 
  # $$ is the process ID, to make this file unique to us
  
  # ----------------------------------
  # Download the request, process it, and delete it
  # Note: to find out exactly what this server is telling you, 
  # add ?help to the end of the URL and view it in a browser.
  # It will give you details of other help pages available,
  # such as the list of fields that it's sending out in requests
  # ----------------------------------
  getstore("http://osmathome.bandnet.org/Requests/", $LocalFilename);
  ProcessRequest($LocalFilename, $Dir);  
  unlink $LocalFilename;
}

#-----------------------------------------------------------------------------
# Once a request has been downloaded as a textfile, have a look at it and
# convert the result to something the other programs can use
#-----------------------------------------------------------------------------
sub ProcessRequest(){
  my ($LocalFilename, $TempDir) = @_;
  
  if(! -f $LocalFilename){
    print "Couldn't get request from server";
    return;
  }

  # Read into memory
  open(my $fp, "<", $LocalFilename) || return;
  my $Request = <$fp>;
  chomp $Request;
  close $fp;
  
  # Parse the request
  my ($ValidFlag,$Version,$X,$Y,$Z,$ModuleName) = split(/\|/, $Request);
  
  # First field is always "OK" if the server has actually sent a request
  if($ValidFlag != "OK"){
    print "Invalid request $Request\n";
    return;
  }
  
  # Check what format the results were in
  # If you get this message, please do check for a new version, rather than
  # commenting-out the test - it means the field order has changed and this
  # program no longer makes sense!
  if($Version != 3){
    print "Server is speaking a different version of the protocol to us\n";
    print "Check to see whether a new version of this program was released\n";
    exit;
  }
  
  # Information text to say what's happening
  print "Zoom level $Z, location $X, $Y from $ModuleName\n";
  
  # Store the request (by creating empty file whose name indicates request)
  my $RequestFile = sprintf("$TempDir/%d_%d_%d",$Z,$X,$Y);
  open(my $fp, ">", $RequestFile);
  close $fp if $fp;
  
}

