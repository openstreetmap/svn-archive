#-----------------------------------------------------------------------------
# Converts from lat/long to google-like tile coordinates
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
package tileCoords;
use strict;
use Math::Trig;

sub LLZ_to_XY{
  my ($Lat, $Long, $Zoom) = @_;
  my $PortionY = Lat2Y($Lat);
  my $PortionX = Long2X($Long);
  my $Size = 2 ** $Zoom;
  my $X = $PortionX * $Size;
  my $Y = $PortionY * $Size;
  return($X,$Y);
}

sub Long2X{
  return((shift() + 180) / 360);
}
sub Lat2Y{
  my $LimitY = 3.1415926;
  my $Y = ProjectF(shift());
  
  ($LimitY - $Y) / (2 * $LimitY);
}
sub ProjectF{
  my $Lat = deg2rad(shift());
  
  log(tan($Lat) + (1/cos($Lat)));
}
#sub deg2rad{
#  shift() * 0.0174532925;
#}

1