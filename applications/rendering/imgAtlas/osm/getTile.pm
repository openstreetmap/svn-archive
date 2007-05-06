#-----------------------------------------------------------------------------
# Fetches a map tile from OpenStreetMap
#
# Copyright 2007
#  * Oliver White
#  * Hakan Tandogan
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
package getTile;
use strict;
use LWP::Simple;

my $tilesource = "http://dev.openstreetmap.org/~ojw/Tiles/tile.php";

sub size
{
    # Tile size in pixels
    return(256);
}

sub tile
{
    my ($X, $Y, $Z) = @_;
    # Download a tile and return its contents as PNG data
    my $URL = URL($X,$Y,$Z);
    # print "   ...  Fetching $URL\n";
    my $Data = get($URL); 
    return $Data;
}

sub setTileSource
{
    my ($source) = @_;

    if ($source eq "default")
    {
	$tilesource = "http://dev.openstreetmap.org/~ojw/Tiles/tile.php";
    }
    elsif ($source eq "maplint")
    {
	$tilesource = "http://dev.openstreetmap.org/~ojw/Tiles/maplint.php";
    }
    elsif ($source eq "mapnik")
    {
	$tilesource = "http://tile.openstreetmap.org";
    }
    else
    {
	# Last chance... it might be a local tilserver...
	$tilesource = $source;
    }
}

sub URL
{
    my ($X,$Y,$Z) = @_;
    # Locate a tile's URL
    return
	sprintf
	$tilesource . "/%d/%d/%d.png",
	$Z, 
	$X, 
	$Y;
}

1
