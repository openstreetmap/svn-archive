#----------------------------------------------------------------------------
# Given the "character", or flash code of a lighthouse or buoy, parse
# that into a perl structure.
#
# e.g.
#  Fl = flashing
#  QFl = quick flashing
#  QFl (3) = quick flashing 3 times
#  QFl (3) 10s = quick flashing 3 times, repeat after 10 seconds
#  QFl (3+2) = quick flashing, 3 times, then 2 times
#  QFl RGB = quick flashing, in sequence red, green, blue
#  Occ = occluded (duty cycle > 50%)
#  Iso = iso (duty cycle = 50%)
#  QFl (2) R + Fl (2) G = quick flash x 2 red, then flash x2 green
#
# Returns a structure containing any information we can parse out of it
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

sub parseFlashSequence
{
  my $Text = shift();
  my $Orig = $Text;
  my $Data;
  
  # Split into multiple independant parts
  #
  # e.g. "Fl (2) R + Fl (2) G" is split into red and green components
  # to be parsed separately.
  #
  # compare with "+" signs inside brackets which mean a different thing
  # e.g. "Fl (2+1) R" should not be split at this point
  # 
  # this should split only on + signs that are not inside brackets,
  # but currently makes the assumption (based on CIL [irish] data)
  # that there is whitespace around these + signs and not around the
  # bracketed ones
  my @Parts = split(/\s+\+\s*/, $Text);
  
  # return an array of these interpreted parts 
  # (which is often just 1 element long)
  foreach my $Part(@Parts)
  {
    push(@{$Data}, parseCharacter2($Part));
  }
  return($Data);
}

sub parseCharacter2
{
  my $Text = shift();
  
  my $Data;
  
  # Special-case: unlit
  if($Text =~ s{UNLIT}{}i)
  {
    $Data->{unlit} = 1;
    return($Data);
  }
  # Special-case: directional lights
  if($Text =~ s{^(Dir)\s*}{})
  {
    $Data->{dir} = 1;
  }
  # Special-case: morse-code flash sequence (gives you a letter)
  # e.g. "Mo (A)" = flash an "A" in morse code
  if($Text =~ s{Mo\s*\(\s*(\w+)\s*\)\s*}{}i)
  {
    $Data->{morse} = $1;
  }
  # Type of flashing:
  if($Text =~ s{^(L?Fl|V?Q|Iso|Occ?)}{})
  {
    my %Abbr = (
      'LFl' => 'Long flash',
      'Fl' => 'Flash',
      'Q' => 'Quick',
      'VQ' => 'Very quick',
      'Iso' => 'Isophase',
      'Occ' => 'Occulted');
    
    $Data->{abbr} = $1;
    $Data->{type} = $Abbr{$1};
  }
  # Repeat period
  # e.g. "30s" = repeat every 30 seconds
  if($Text =~ s{([0-9.]+)\s*s$}{}i)
  {
    $Data->{period} = $1;
  }
  # Repetitions of the flash within a period
  # e.g. "(2)" means flash twice
  # "(2+3)"  means ". .   . . ."
  if($Text =~ s{^\s*\(([0-9+]+)\s*\)}{})
  {
    @{$Data->{repeat}} = split(/\+/,$1);
  }
  # Colour of light
  if($Text =~ s{^\s*([WYRG]+)}{})
  {
    $Data->{colour_abbr} = $1;
  }
  else
  {
    $Data->{colour_abbr} = 'W';
  }
  # Interpret the colour letter
  my %Cols = (
    'W' => 'white',
    'Y' => 'yellow',
    'R' => 'red',
    'G' => 'green');
  foreach my $Letter(split(//,$Data->{colour_abbr}))
  {
    push(@{$Data->{colours}}, $Cols{$Letter});
  }
  
  # If any data remains that we couldn't interpret,
  # store that so that we can raise a warning if necessary
  $Data->{unconverted} = $Text if($Text =~ m{\S});
  
  return($Data);
}

1
