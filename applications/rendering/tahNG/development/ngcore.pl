#!/usr/bin/perl -w
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact Deelkar on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Dirk-Lueder "Deelkar" Kreie
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
use Image::Magick;
use LWP::UserAgent;
use Math::Trig;
use File::Copy;
use File::Temp qw(tempfile);
use AppConfig qw(:argcount);
use FindBin qw($Bin);
use English '-no_match_vars';
use tahconfig;
#use tahlib;
use tahproject;

my $Config = AppConfig->new({ 
            CREATE => 1,                      # Autocreate unknown variables
            GLOBAL => {
                    DEFAULT  => "<undef>",    # Create undefined Variables by default
                    ARGCOUNT => ARGCOUNT_ONE, # Simple Values (no arrays, no hashmaps)
                }
        });

$Config->define("help|usage!");
$Config->file("config.defaults", "authentication.conf", "tahng.conf");
$Config->args();
$Config->file("config.svn");
ApplyConfigLogic($Config);
my %EnvironmentInfo = CheckConfig($Config);

my $Version = '$Revision$';
$Version =~ s/\$Revision:\s*(\d+)\s*\$/$1/;
