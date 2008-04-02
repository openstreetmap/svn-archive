#!/usr/bin/perl
use XML::Simple;
use Data::Dumper;
use strict;
$Data::Dumper::Terse = 1;

# Location of source data (unzip the KMZ file first to get doc.kml)
my $Data = XMLin("doc.kml");

# Dump some debugging output into this directory
my $Dir = "html";
mkdir $Dir if ! -d $Dir;

# Optional: just dump the entire XML payload to STDOUT
# print Dumper($Data);

# Start the OSM file
open OSM, ">lighthouses.osm";
print OSM "<?xml version='1.0' encoding='UTF-8'?>\n";
print OSM "<osm version='0.5' generator='OJWs lighthouse script'>\n";

my $TagUse;
my $NextId = -1;
my $Manmade = '';
my %Colours;

while(my($Set, $Items) = each(%{$Data->{Folder}->{Document}}))
{
  # Convert "Irish Lighthouses", "Irish Buoys" to "lighthouse","buoy"
  $Manmade = lc($Set);
  $Manmade =~ s{irish (.*)s}{\1};
  
  # Loop through locations
  while(my($Name, $Item) = each(%{$Items->{Folder}->{Placemark}}))
  {
    # Coordinates are stored as text, with comma between them
    my ($Lon,$Lat) = split(/,/,$Item->{Point}->{coordinates});
    
    # Pretty much all the data is in the description field
    my $Description = $Item->{description};
    
    # Parse that, using our special code
    my $Data = parse($Description);
    
    
    # Optional: just dump the list of locations
    # printf "%s: %f, %f\n", $Name, $Lat, $Long;
    
    # Save the description to an HTML file for easy viewing
    open(OUT, ">$Dir/$Name.html");
    print OUT $Description;
    close OUT;
    
    # Save the parsed data to a text file, so we can debug that
    open(OUT, ">$Dir/$Name.txt");
    print OUT Dumper($Data);
    close OUT;

    # Add this location to the main OSM file output
    print OSM toOsm($Data, $Lat, $Lon, $Name);
  }
}
print OSM "</osm>\n";

# Parse some description text from the irish lighthouses KML file
sub parse
{
  my $Data = {};
  my $Section = "";
  
  my $Html = shift();
  my $MiscLinks = 1;
  
  # Each line is enclosed in a <br> tag
  while($Html =~ m{<br>(.*?)</br>}g)
  {
    my $Line = $1;

    # Lines with bold text mark a new section
    if($Line =~ m{<b>\s*(.*?)\s*</b>\s*:?})
    {
      $Section = $1;
    }
    # Lines with "name:value" can be stored as data
    elsif($Line =~ m{\s*(.*?)\s*:\s*(.*)\s*})
    {
      if($1 eq 'Structure')
      {
        $Data->{description} = $2;
        $Data->{structure} = parseStructureDescription($2);
      }
      else
      {
        $Data->{$Section}->{$1} = $2;
      }
    }
    # Unformatted lines
    else
    {
      push(@{$Data->{FreeText}},$Line);
    }
    
    # If the line contains a hyperlink
    if($Line =~ m{^\s*(.*?)\s*<a href=\"(.*?)\">(.*?)</a>})
    {
      my $Pretext = $1;
      my $URL = $2;
      my $Title = $3;
      
      # Types of hyperlink in the irish lighthouses file
      if($Pretext =~ m{Operated by})
      {
        $Data->{Links}->{operator} = {URL=>$URL,title=>$Title};
      }
      elsif($Pretext =~ m{Details from})
      {
        $Data->{Links}->{source_data} = {URL=>$URL,title=>$Title};
      }
      elsif($Pretext eq "Picture at")
      {
        $Data->{Links}->{image} = {URL=>$URL,title=>$Title};
      }
      elsif($Pretext eq "History and picture at")
      {
        $Data->{Links}->{Website} = {URL=>$URL,title=>$Title};
      }
      else
      {
        # Unrecognised type of hyperlink
        #$Data->{Links}->{sprintf("website%d",$MiscLinks++)} = {URL=>$2,title=>$3};
        print "$1\n";
      }
    }
  }
  
  # Return a structure describing anything found in the supplied description
  return $Data;
}

sub parseStructureDescription
{
  my $Text = shift();
  my $Data;
  
  # Detect lanterns
  if($Text =~ s{(white|red|green) lantern}{})
  {
    $Data->{lantern} = $1;
  }
  elsif($Text =~ s{lantern}{})
  {
    $Data->{lantern} = 'default';
  }
  
  # Detect stripes
  if($Text =~ s{(\w+) (band|stripe)\b}{})
  {
    $Data->{band} = $1;
  }
  if($Text =~ s{(\w+) bands\b}{})
  {
    $Data->{bands} = $1;
  }
  
  # Detect detail of building
  if($Text =~ s{(?<!on)(.*)\s*(tower|pillar|hut|mast|building|platform|column|tower|house|structure)}{}i)
  {
    $Data->{main} = parseColoursEtc(lc($1));
    $Data->{main}->{type} = $2;
  }
  
  if($Text =~ s{on (.*)}{})
  {
    $Data->{atop} = $1;
  }
  
  if($Text =~ s{(.*) top}{})
  {
    $Data->{roof} = parseColoursEtc(lc($1));
  }
  
  return($Data);
}

sub parseColoursEtc
{
  my $Data;
  foreach my $Word(split(/\s+/, shift()))
  {
    $Colours{$Word}++;
  
    $Data->{shape} = $1 if($Word =~ m{(conical|square|round|8-sided)});
  
    $Data->{style} = $1 if($Word =~ m{(lattice|framework|pillar|tower)});
  
    $Data->{cladding} = $1 if($Word =~ m{(stone|concr?ete|granite)});
  
    $Data->{colour} = $1 if($Word =~ m{(black|grey|red|blue|white)});
  }
  return($Data);
}

# Convert a data structure to OSM XML
sub toOsm
{
  my ($Data, $Lat, $Lon, $Name) = @_;
  my $Text = "";
  
  # Start the node
  $Text .= sprintf "<node id='%d' lat='%f' lon='%f'>\n", $NextId--, $Lat, $Lon;
  
  # Add some standard fields
  $Text .= makeTag('name', $Name);
  $Text .= makeTag('source', 'Commissioners of Irish Lights');
  $Text .= makeTag('website:source', 'http://cil.ie/');
  $Text .= makeTag('description', $Data->{description});
  
  if($Name =~ m{Helicopter Landing Base})
    {
    # Helicopter landing bases aren't actually lighthouses!
    $Text .= makeTag('aeroway', 'heliport');
    }
  else
    {
    $Text .= makeTag('man_made', $Manmade);
    
    # Detect unlighted
    if($Name =~ m{Unlighted}i
      or 
      $Data->{'Technical Data'}->{'Character'} =~ m{unlit}i)
    {
      $Text .= makeTag('lighting', 'no');
    }
    
    $Text .= makeTag(
      'building', 
      $Data->{'structure'}->{'main'}->{type});
      
    $Text .= makeTag(
      'building:colour', 
      $Data->{'structure'}->{'main'}->{colour});
      
    $Text .= makeTag(
      'building:shape', 
      $Data->{'structure'}->{'main'}->{shape});
      
    $Text .= makeTag(
      'building:cladding', 
      $Data->{'structure'}->{'main'}->{cladding});
      
    $Text .= makeTag(
      'building:decoration:band', 
      $Data->{'structure'}->{band});
    
    $Text .= makeTag(
      'building:decoration:bands', 
      $Data->{'structure'}->{bands});
    
    $Text .= makeTag(
      'building:lantern:colour', 
      $Data->{'structure'}->{lantern});
       
    my $Ranges = $Data->{'Technical Data'}->{'Nominal Range(s)'};
    if(defined $Ranges)
    {
      my $Max = 0;
      foreach my $R(split(/\//, $Ranges))
      {
        $Max = $R if($R > $Max);
      }
      
      $Text .= makeTag("light:range", sprintf("%1.0f km", $Max * 1.852)) if($Max > 0);
      $Text .= makeTag("light:ranges", $Ranges . " nmi");
      undef $Data->{'Technical Data'}->{'Nominal Range(s)'};
    }
   
    # Take certain things out of "technical data" and into other named tags
    my %ConvertToOsm = (
      'Height of Tower/Structure'=>'building:height',
      'Height of Light Above MHWS'=>'ele:mhws', 
      'DGPS'=>'dgps',
      'Built'=>'start_date', 
      'AIS'=>'automatic_identification_system',
      'Character'=>'lighting:sequence');
    while(my($from,$to) = each(%ConvertToOsm))
    {
      $Text .= makeTag(
        $to, 
        $Data->{'Technical Data'}->{$from});
      
      undef($Data->{'Technical Data'}->{$from});
    }
    
    # List any other technical data
    while(my($k,$v) = each(%{$Data->{'Technical Data'}}))
    {
      $k =~ s{\s+}{_}g;
      $Text .= makeTag('navaid:sea:'.lc($k), $v);
    }
  } # end of "if a lighthouse"
  
  # List any URLs that were found
  while(my($Type,$Link) = each(%{$Data->{Links}}))
  {
    if($Type eq 'Website')
    {
      $Text .= makeTag("website", $Link->{URL});
      $Text .= makeTag("website_description", $Link->{title});
    }
    else
    {
      $Text .= makeTag("website:$Type", $Link->{URL});
      $Text .= makeTag("website_description:$Type", $Link->{title});
    }
  }

  $Text .= sprintf "</node>\n";
}

# Create an OSM XML tag
sub makeTag
{
  my($k, $v) = @_;
  
  # Don't write blank tags
  return if($v !~ /\S/);
  
  # Convert Yes/No to lowercase
  $v = lc($v) if($v =~ m{(Yes|No)});
  
  # Correct misspellings ;)
  $v =~ s{meters}{metres}g;
  
  # Store stats about the tags used
  $TagUse->{$k}->{$v} = 1;
  
  # Format as OSM XML
  return(sprintf("  <tag k=\"%s\" v=\"%s\" />\n", xmlSafe($k), xmlSafe($v)));
}

# Escape anything that's significant in an XML attribute
sub xmlSafe
{
  my $Text = shift();
  # Not using this, because none of our data happens to contain double-quotes
  #$Text =~ s{'}{''}g;
  
  $Text =~ s{\xB0}{&deg;}g; # degree characters
  
  return($Text);
}

