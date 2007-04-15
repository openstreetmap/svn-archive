#!/usr/bin/perl
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home, status 
# Says a bit about what's happening
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

StatusOf("../requests/", "%d requests waiting to be downloaded");
StatusOf("../data/", "%d map files waiting to be transformed");
StatusOf("../SVG/", "%d SVG files waiting to be rendered");
StatusOf("../tiles/", "%d image files waiting to be split");
StatusOf("../tiles2/", "%d tiles waiting to be packed");
StatusOf("../uploadable/", "%d ZIP files ready to upload");

sub StatusOf(){
  my ($Dir, $Format) = @_;
  my $Count = CountFilesInDir($Dir);
  printf($Format."\n", $Count);
}