#----------------------------------------------------------------------------
# Given a string containing some colour changes representing a lighthouse
# or buoy flash sequence (e.g. "green,4|black,4" which represents 
# "Iso G 2s") convert that into an animated image showing the light flashing
#
#----------------------------------------------------------------------------
# Copyright 2008, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#----------------------------------------------------------------------------
use GD;
use strict;
use parseFlashSequence;
use renderFlashSequence;

sub flashSequenceToGifAnim
{
  my($Seq, $GifFile) = @_;
  
  # List the colours that we support
  # (this should be equal to the colours that parseFlashSequence recognises)
  my %Palette = (
          white=>[255,255,255],
          black=>[0,0,0],
          red=>[255,128,128],
          yellow=>[255,255,64],
          green=>[64,255,64],
          blue=>[64,64,255]);
  
  # Create an image of the appropriate size
  my $im = new GD::Image(40,40);
  
  # Assign the colours to the palette (and actually use each colour,
  # so that its guaranteed to be available in the animation)
  my %Colours;
  while(my($col,$rgb) = each(%Palette))
  {
    $Colours{$col} = $im->colorAllocate($rgb->[0],$rgb->[1],$rgb->[2]);
    $im->filledRectangle(0,0,39,39,$Colours{$col});
  }
  
  # Start the GIF animation
  my $gif = $im->gifanimbegin(1,0);
  
  # Background colour
  $im->filledRectangle(0,0,39,39,$Colours{black});
  
  # For each light colour
  foreach my $Part(split(/\|/, $Seq))
  {
    my ($col, $len) = split(/,/, $Part);
    
    # Display this light around the middle of the image
    $im->filledRectangle(10,10,30,30,$Colours{$col});
    
    # Add this frame to the animation
    $gif .= $im->gifanimadd(1,0,0,int(20 * $len));
  }
  
  # End and store the GIF file
  $gif .= $im->gifanimend();
  
  open OUT, ">$GifFile";
  binmode OUT;
  print OUT $gif;
  close OUT;
}
1
