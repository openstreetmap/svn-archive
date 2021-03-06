#-----------------------------------------------------------------
# Usage: perl parse.pl < data.osm
# 
# Output: 
#  - creates 2 files:
#    - nodes.txt (list of interesting nodes)
#    - ways.txt (list of interesting ways)
#  - Both files are semicolon-separated list of tags
#  - 'Interesting' means it's got tags that aren't in the ignore list
#  - See bottom of file for the ignore list
#  - special tags:
#    - 'polyline' is a list of lat,long pairs (comma-separated)
#    - 'lat' is latitude of a node
#    - 'lon' is longitude of a node
#  - limitations
#    - semicolons in tags are silently converted to commas
#    - tags in the ignore list can't be used in OSM file (e.g. 'lat')
#    - there may be blank lines in the output ways file
# 
# Copying:
#  Copyright 2007, Oliver White, streetmap@blibbleblobble.co.uk
#  Licensed under GNU GPL v2 or later
#  No warranty etc.
#---------------------------------------------------------------
use strict;
my %Nodes;                       # List of all nodes
my %Segments;                    # List of all segments
my %Tags;                        # List of tags in the current object
my @Segments;                    # List of segments in the current way
my %IgnoreTags = IgnoreTags();   # List of tag keys to ignore
my $Tagtype = '-';               # What object the parser is in

# File outputs
open(NODES, '>nodes.txt') || die();
open(WAYS, '>ways.txt') || die();

while(my $Line = <>){
  if($Line =~ m{<node (.*)}){
    # Beginning of a node
    %Tags = getAttributes($1);
    $Tagtype = 'n';
  }
  elsif($Line =~ m{<tag k="(.*?)" v="(.*?)"\s*/>}){
    # Tag within an object
    my ($Name, $Value) = ($1, $2);
    if($Value ne ''){
      if(!$IgnoreTags{$Name}){      # Ignored tags
        if(!$IgnoreTags{$Tagtype.':'.$Name.'='.$Value}){ # Ignored name=tag combos
          $Tags{$Name} = $Value;
        }
      }
    }
  }
  elsif($Line =~ m{</node}){
    # End of a node
    my $ID = $Tags{id};
    $Nodes{$ID.'_lat'} = $Tags{lat};
    $Nodes{$ID.'_lon'} = $Tags{lon};
    writeNode();
    $Tagtype = '-';
  }
  elsif($Line =~ m{<segment (.*)}){
    # Beginning of a segment
    %Tags = getAttributes($1);
    $Tagtype = 's';
  }  
  elsif($Line =~ m{<way (.*)}){
    # Beginning of a way
    %Tags = getAttributes($1);
    $Tagtype = 'w';
    @Segments = ();
  }  
  elsif($Line =~ m{<seg id="(\d+)"/>}){
    # Segment within a way
    push(@Segments, $1);
  }
  elsif($Line =~ m{</segment}){
    # End of a segment
    my $ID = $Tags{id};
    $Segments{$ID.'_from_lat'} = $Nodes{$Tags{from} . '_lat'};
    $Segments{$ID.'_from_lon'} = $Nodes{$Tags{from} . '_lon'};
    $Segments{$ID.'_to_lat'} = $Nodes{$Tags{to} . '_lat'};
    $Segments{$ID.'_to_lon'} = $Nodes{$Tags{to} . '_lon'};
    $Tagtype = '-';
  }
  elsif($Line =~ m{</way}){
    # End of a way
    writeWay();
    $Tagtype = '-';
  }
}

# Decide if a way is interesting, and write it to disk
# Split the way if its discontinuous
sub writeWay{
  my($LastLat,$LastLon) = (0,0);
  my $TagList = tagList();
  
  return if(!$TagList);
  
  while(my $S = shift(@Segments)){
    my $FromLat = $Segments{$S.'_from_lat'};
    my $FromLon = $Segments{$S.'_from_lon'};
    my $ToLat = $Segments{$S.'_to_lat'}; 
    my $ToLon = $Segments{$S.'_to_lon'};

    if($FromLat != $LastLat || $FromLon != $LastLon){
      printf WAYS "\n%s;polyline=%f,%f,",
        $TagList,
        $FromLat,
        $FromLon;
    }
    printf WAYS "%f,%f,", 
      $ToLat,
      $ToLon;

    $LastLat = $ToLat;
    $LastLon = $ToLon;
  }
  print WAYS "\n";
  
}

# Decide if a node is interesting, and write it to disk
sub writeNode{
  my $TagList = tagList();
  if($TagList){
    printf NODES "lat=%f;lon=%f;%s\n",$Tags{lat},$Tags{lon}, $TagList;
  }
}

# Get the global tags list, as a semicolon-separated string
sub tagList{
  my @Stuff;
  while(my($k,$v) = each(%Tags)){
    if(!$IgnoreTags{$k}){
      $k =~ s/;/,/g;
      $v =~ s/;/,/g;
      push(@Stuff, "$k=$v");
    }
  }
  return(join(';',@Stuff));
}

# Parse an XML attributes string, return hash
sub getAttributes{
  my $Text = shift();
  my %A;
  while($Text =~ m{(\w+)=\"(.*?)\"}g){
    $A{$1} = $2;
  }
  return(%A);
}

# Create a list of tags to ignore
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
    'n:natural=coastline',    # coastline nodes
    'n:natural=water',        # coastline nodes
    'n:highway=primary',      # not needed for nodes 
    'n:highway=secondary',    # not needed for nodes 
    'n:highway=minor',        # not needed for nodes 
    'n:highway=unclassified', # not needed for nodes 
    'n:highway=residential',  # not needed for nodes 
    'n:highway=trunk',        # not needed for nodes 
    'n:highway=service',      # not needed for nodes 
    'n:highway=cycleway',     # not needed for nodes 
    'n:highway=bridleway',    # not needed for nodes 
    'n:highway=footway',      # not needed for nodes 
    'n:oneway=yes',           # not relevant for nodes 
    'n:oneway=true',          # not relevant for nodes 
    ){
    $Ignore{$Tag} = 1;
  }
  return(%Ignore);
}