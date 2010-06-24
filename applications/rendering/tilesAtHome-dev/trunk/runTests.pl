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

print "- Reading Config ... ";
my %EnvironmentInfo;
my $Config = TahConf->getConfig();
print "OK\n";

print "- Testing Basic Config ... ";
%EnvironmentInfo = $Config->CheckBasicConfig();
print "OK\n";


print "- Testing Full Config ... ";
%EnvironmentInfo = $Config->CheckConfig();
print "OK\n";

print "- Start offline tests:\n";
my $Cmd;
my $success;
my $PID;

#$Cmd = "perl ./tilesGen.pl localFile tests/emptyfile_12_0_0.osm"; ## must fail
#$success = runCommand($Cmd,$PID);

##FIXME check result for failure

print " * fonttest (will take some time) ... ";
$Cmd = "perl ./tilesGen.pl --tile_MaxZoom=14 --Verbose=3 --CreateTilesetFile=0 localFile tests/fonttest_12_0_0.osm tile"; ##should create zips for font-comparison
my $retval = system($Cmd);
##FIXME check result for failure, otherwise graphically compare image
print STDERR "\n\n";

my $tempdir = tempdir ( DIR => $Config->get("WorkingDirectory") );
my $comparedir = tempdir ( DIR => $Config->get("WorkingDirectory") );
my $zipfile = File::Spec->join($Config->get("WorkingDirectory"),"uploadable","tile_12_0_0_*.zip");
$Cmd = sprintf("unzip -d %s -q %s",$tempdir,$zipfile);
$success = runCommand($Cmd,$PID);

if ($success) # remove all zips from the test, because they should not be uploaded to the server.
{ 
    my @files = glob($zipfile); 
    foreach my $zip (@files) 
    {
        print STDERR "removing $zip \n";
        unlink($zip) or die "cannot delete $zip";
    }
}

my @pngList = ("_14_0_2.png","_14_0_3.png","_14_1_1.png","_14_2_2.png","_14_3_3.png"); # these tiles contain font samples"
my @failedImages;
foreach my $pngSuffix (@pngList)
{
    print STDERR "testing tests/fonttest".$pngSuffix."\n";

    rename(File::Spec->join($tempdir,"tile".$pngSuffix),File::Spec->join($comparedir,"tile".$pngSuffix)); #move interesting images to $comparedir

    my $renderResult = File::Spec->join($comparedir,"tile".$pngSuffix);
    my $Image = undef;
    eval { $Image = GD::Image->newFromPng($renderResult); };
    die "$renderResult not found" if( not defined $Image );

    my @fonttestRef = undef;
    my @ReferenceImage = undef;
    my $loopmax = 15;
    my $I = 0;
    for (; $I <= $loopmax;)
    {
        $fonttestRef[$I] = File::Spec->join("tests","fonttest".$I.$pngSuffix);
        $ReferenceImage[$I] = undef;
        eval { $ReferenceImage[$I] = GD::Image->newFromPng($fonttestRef[$I]); };
        if (not defined $ReferenceImage[$I]) # this means we ran out of tests, without finding a match.
        {
            pop(@fonttestRef);
            print STDERR "\nFonttest failed, check installed fonts. $renderResult doesn't match any of ";
            print STDERR join(", ",@fonttestRef);
            print STDERR "\n";
            push(@failedImages, $renderResult);
            die $fonttestRef[$I]." not found" if($I == 0);
            last; 
        }
         
        # libGD comparison returns true if images are different. 
        if (not $Image->compare($ReferenceImage[$I]) & GD_CMP_IMAGE)
        {
            last; #we found a match, so the client is OK.
        }
        else
        {
            $I++
        }
    }
}

rmtree($tempdir); #clean up all remaining images not useful for comparison

if (scalar(@failedImages))
{
    print STDERR "Please e-mail the following failed images to tah\@deelkar.net:\n";
    print STDERR "Failed images reference number: 2010062402\n";
    print STDERR join("\n",@failedImages);
    print STDERR "\n";
    exit(7);
}
else
{
    rmtree($comparedir);
    print "OK\n";
}


print "- done testing.\n";
