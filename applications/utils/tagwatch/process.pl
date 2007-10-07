#-----------------------------------------------------------------
# Parses an OpenStreetMap XML file looking for tags, and counting
# how often each one is used
#-----------------------------------------------------------------
# Usage: perl process.pl < data.osm
# Will create an ./Output/ directory and fill it with text files
# describing the tags used in data.osm
#-----------------------------------------------------------------
# This file is part of Tagwatch
# Tagwatch is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# Tagwatch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
#---------------------------------------------------------------
use strict;
my %IgnoreTags = IgnoreTags();   # List of tag keys to ignore
my $Tagtype = '-';               # What object the parser is in
my %Tags;
my %Values;
my %Usage;

while(my $Line = <>){
  if($Line =~ m{<tag k=["'](.*?)["'] v=["'](.*?)["']\s*/>}){
    # Tag within an object
    my ($Name, $Value) = ($1, $2);
    if($Value ne ''){
      if(!$IgnoreTags{$Name}){      # Ignored tags
	$Tags{$Name}++;
	$Values{$Name}->{$Value}++;
	$Usage{$Name}->{$Value}->{$Tagtype}++;
        #print STDERR "$Name = $Value\n";
      }
    }
  }
  elsif($Line =~ m{<(node|segment|way) (.*)}){
    # Beginning of an object
    $Tagtype = substr($1,0,1);
  }  
  elsif($Line =~ m{<seg id=["'](\d+)["']\s*/>}){
    # Segment within a way
  }
  elsif($Line =~ m{</(node|segment|way)}){
    # End of an item
    $Tagtype = '-';
  }
}

my $Dir = "Output";
mkdir $Dir if(!-d $Dir);
open(OUT, ">$Dir/tags.txt");
foreach my $Tag(keys %Tags){
  printf OUT "%d %s\n", $Tags{$Tag}, $Tag;

  open(TAG, ">$Dir/tag_$Tag.txt");
  open(USAGE, ">$Dir/usage_$Tag.txt");
  
  foreach my $Value(keys(%{$Values{$Tag}})){
    printf TAG "%d %s\n", $Values{$Tag}->{$Value}, $Value;
    printf USAGE "%s %d %d\n", $Value, $Usage{$Tag}->{$Value}->{'n'}, $Usage{$Tag}->{$Value}->{'w'};
  }

  close TAG; 
  close USAGE;
}
close OUT;

# Create a list of tags to ignore
# TODO: put this on a wiki page?
sub IgnoreTags{
  my %Ignore;
  foreach my $Tag(
    'lat','lon','tagtype','id',  # Reserved words (all objects)
    'created_by', # Not relevant for rendering
    'ele',        # GPS metadata
    '',           # Tags without a name
    'from',       # Reserved word (segment)
    'to',         # Reserved word (segment)
    'visible',    # OSM internal metadata
    'timestamp',  # OSM internal metadata
    'user',       # OSM internal metadata
    'source',     # Not relevant for rendering
    'polyline',   # Reserved word (way)
    'time',       # GPS metadata?
    'editor',     # Not relevant for rendering
    'author',     # Not relevant for rendering
    'hdop',       # GPS metadata
    'pdop',       # GPS metadata
    'sat',        # GPS metadata
    'speed',      # GPS metadata
    'fix',        # GPS metadata
    'course',     # GPS metadata
    'class',      # depreciated
    'converted_by', # Some program
    ){
    $Ignore{$Tag} = 1;
  }
  return(%Ignore);
}