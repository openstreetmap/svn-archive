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

$Cmd = "perl ./tilesGen.pl localFile tests/emtpyfile_0_0_12.osm";
$success = runCommand($Cmd,$PID);

$Cmd = "perl ./tilesGen.pl localFile tests/fonttest_0_0_12.osm";
$success = runCommand($Cmd,$PID);

