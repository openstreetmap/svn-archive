#----------------------------------------------------------------------------
# Given a string containing some colour changes representing a lighthouse
# or buoy flash sequence (e.g. "green,4|black,4" which represents 
# "Iso G 2s") convert that into an image showing the bands of colour
# horizontally across the image
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

sub flashSequenceToImage
{
  use GD;
  my ($Text, $Filename) = @_;
  
  # Make 3 copies of the incoming text, so we can see the repeat pattern
  # (TODO: should this be done by the calling function?)
  $Text = $Text x 3;
  
  # Option: Pixels per unit time
  my $dx = 4;
  
  # Find out how wide the image should be, based on the length 
  # of the sequence
  my $width = 0;
  foreach my $Block(split(/\|/,$Text))
  {
    my ($Colour,$len) =split(/,/, $Block);
    $width += $len * $dx;
  }
  return if($width == 0);
  
  # Option: Height of the image
  my $height = 20;
  
  # Create the image
  my $Image = new GD::Image( $width, $height );
  
  # List the colours that we support
  # (this should be equal to the colours that parseFlashSequence recognises)
  my %Colours;
  my %Palette = (
    'white'=>'225,225,225',
    'red'=>'255,127,127',
    'yellow'=>'255,255,127',
    'green'=>'127,255,127',
    'black'=>'0,0,0');
  
  while(my($Col,$RGB) = each(%Palette))
  {
    my($r,$g,$b)=split(/,/, $RGB);
    $Colours{$Col} = $Image->colorAllocate($r,$g,$b);
  }
  
  # For each block of colour, add it to the image
  my $x = 0;
  foreach my $Block(split(/\|/,$Text))
  {
    my ($Colour,$len) =split(/,/, $Block);
    $len *= $dx;
    $Image->filledRectangle($x,0,$x+$len,$height,$Colours{$Colour});
    $x+=$len;
  }
  
  # Store the output to a PNG file
  open(my $fp, ">$Filename") || return; 
  binmode $fp;
  print $fp $Image->png();
  close $fp;
}

1