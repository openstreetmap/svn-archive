#!/usr/bin/perl
#-----------------------------------------------------------------------------
# Currently just generic image-to-PDF stuff
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
use PDF::API2;
use constant mm => 25.4 / 72;
my $Filename = shift() || "atlas.pdf";
my $PDF = PDF::API2->new();

my $Page = $PDF->page;

my $PageGfx = $Page->gfx;
my $Image = $PDF->image_jpeg("some.jpg");
$PageGfx->image($Image, 10/mm, 10/mm, 190/mm, 270/mm ); # from left, from bottom, width, height


$PDF->saveas($Filename);
