#!/usr/bin/perl
#-------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact Deelkar on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2008, Dirk-Lueder Kreie and others
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

use strict;
use lib './lib';
use tahlib;
use TahConf;
use English '-no_match_vars';
use tahproject;


our $Mode = shift() || '';

my $Config;
my %EnvironmentInfo;

$Config = TahConf->getConfig();
%EnvironmentInfo = $Config->CheckConfig();

if ($Mode eq "getbbox")
{
    my $X = shift();
    my $Y = shift();
    my $Z = shift() || 12;

    my ($N,$E,$S,$W) = ProjectXY($Z, $X, $Y);

    my $N1 = $N + ($N - $S) * $Config->get("BorderNS");
    my $S1 = $S - ($N - $S) * $Config->get("BorderNS");
    my $E1 = $E + ($E - $W) * $Config->get("BorderWE");
    my $W1 = $W - ($E - $W) * $Config->get("BorderWE");

    print "\n";
    print "tilearea for $X,$Y zoom $Z is\n";
    print "  in left,bottom,right,top order: bbox=$W,$S,$E,$N \n";
    print "  coordinates: N=$N, S=$S, W=$W, E=$E \n\n";

    print "downloaded area for $X,$Y zoom $Z is\n";
    print "  in left,bottom,right,top order: bbox=$W1,$S1,$E1,$N1 \n";
    print "  coordinates: N=$N1, S=$S1, W=$W1, E=$E1)\n\n";
}
elsif ($Mode eq "center")
{
    my $X = shift();
    my $Y = shift();
    my $Z = shift() || 12;

    my ($N,$E,$S,$W) = ProjectXY($Z, $X, $Y);

    my $lat = ($W + $E) / 2;
    my $lon = ($N + $S) / 2;

    print "bbox center for tile $X,$Y zoom $Z is\n";
    print "  in lat,lon order: $lat,$lon \n\n";

    # lat stays the same
    (undef,$lon) = Project($Y*2, $Z+1);

    print "center of rendered (projected) tile is\n";
    print "  in lat,lon order: $lat,$lon \n\n";
}
elsif ($Mode eq "tile")
{
    my $lat = shift();
    my $lon = shift();
    my $Z = shift() || 12;
    
    my ($X,$Y) = getTileNumber($lat,$lon,$Z);

    print "\n";
    print "tilenumber for $lat,$lon at zoom $Z is\n";
    print " X=$X Y=$Y $Z,$X,$Y \n\n";

}
else
{
    usage();
}

sub usage
{
   print "perl debugHelper.pl <mode> <X> <Y> [Zoom]\n";
   print "where <mode> one of \"getbbox\", \"center\"\n";
   print " - or - ";
   print "perl debugHelper.pl \"tile\" <lat> <lon> [Zoom]\n";
   exit 1;
}

sub getTileNumber {
  my ($lat,$lon,$zoom) = @_;
  my $xtile = int( ($lon+180)/360 *2**$zoom ) ;
  my $ytile = int( (1 - log(tan($lat*pi/180) + sec($lat*pi/180))/pi)/2 *2**$zoom ) ;
  return(($xtile, $ytile));
}
