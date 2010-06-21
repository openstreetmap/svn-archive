#!/usr/bin/perl
#-------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact Deelkar on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2010, Dirk-Lueder Kreie and others
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

use warnings;
use strict;
use lib './lib';
use tahlib;
use TahConf;
use File::Temp qw(tempfile tempdir);
use File::Spec;
use File::Path qw(rmtree);
use GD 2 qw(:DEFAULT :cmp);


delete $ENV{LC_ALL};
delete $ENV{LC_NUMERIC};
delete $ENV{LANG};
$ENV{LANG} = 'C';

print "Reading Config\n";
my %EnvironmentInfo;
my $Config = TahConf->getConfig();

print "Testing Basic Config\n";

%EnvironmentInfo = $Config->CheckBasicConfig();

print "Testing Full Config\n";

%EnvironmentInfo = $Config->CheckConfig();

print "\nStart offline tests:\n";
my $Cmd;
my $success;
my $PID;

#$Cmd = "perl ./tilesGen.pl localFile tests/emptyfile_12_0_0.osm"; ## must fail
#$success = runCommand($Cmd,$PID);

##FIXME check result for failure

$Cmd = "perl ./tilesGen.pl --CreateTilesetFile=0 --Layers=tile localFile tests/fonttest_12_0_0.osm"; ##should create zips for font-comparison
$success = runCommand($Cmd,$PID); 

##FIXME check result for failure, otherwise graphically compare image
my $tempdir = tempdir ( DIR => $Config->get("WorkingDirectory") );
my $zipfile = File::Spec->join($Config->get("WorkingDirectory"),"uploadable","tile_12_0_0_*.zip");
$Cmd = sprintf("unzip -d %s -q %s",$tempdir,$zipfile);
$success = runCommand($Cmd,$PID);

if ($success) # remove all zips from the test, because they should not be uploaded to the server.
{ 
    my @files = glob($zipfile); 
    foreach my $zip (@files) 
    {
        print "removing $zip \n";
        unlink($zip) or die "cannot delete $zip";
    }
}

my @pngList = ("_12_0_0.png","_13_1_0.png","_14_1_1.png","_14_3_3.png"); # these tiles contain font samples
foreach my $pngSuffix (@pngList)
{
    my $fonttestRef = File::Spec->join("tests","fonttest".$pngSuffix);
    my $renderResult = File::Spec->join($tempdir,"tile".$pngSuffix);

    my $ReferenceImage = undef;
    eval { $ReferenceImage = GD::Image->newFromPng($fonttestRef); };
    die "$fonttestRef not found" if( not defined $ReferenceImage );
    
    my $Image = undef;
    eval { $Image = GD::Image->newFromPng($renderResult); };
    die "$renderResult not found" if( not defined $Image );
    
    # libGD comparison returns true if images are different. 
    die "Fonttest failed, check installed fonts. $renderResult $fonttestRef" if ($Image->compare($ReferenceImage) & GD_CMP_IMAGE)
}

rmtree($tempdir);
