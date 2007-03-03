#!/usr/bin/perl
#-----------------------------------------------------------------------------
# Command-line program to generate a map image from OSM tiles
#
# Parameters:
# * lat  - WGS84 latitude of map centre
# * long - WGS84 longitude of map centre
# * zoom - zoom level of tiles to use (0-17)
# * width - image width to create, px
# * height - image height to create, px
# * size   - size of tiles in the output, pixels
# * filename - name of a PNG file to export
#
# Copyright 2007
#  * Oliver White
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
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
#-----------------------------------------------------------------------------
use strict;
use osm::getTileArea;

my %Area;
foreach my $Input qw(lat long zoom width height size){
  $Area{$Input} = shift();
}
my $Filename = shift();

getTileArea::createArea(\%Area, $Filename);

