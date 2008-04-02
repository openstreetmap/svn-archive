use XML::Simple;
use Data::Dumper;
use strict;
$Data::Dumper::Terse = 1;
#$Data::Dumper::Indent = 0;

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

# Index files
open INDEX, ">$Dir/index.html";
open MISC, ">$Dir/misc.txt";
open LIGHTS, ">Img/index.htm"; 
open SUMMARY, ">summary.htm"; 

print SUMMARY <<HEAD;
<html><head><title>Lighthouse data</title><style>
.t1{  background-color:#FFD  }
.t2{  background-color:#FDD  }
.t3{  background-color:#DFD  }
.t4{  background-color:#DDF  }
.t5{  background-color:#EEE  }

.t1, .t2, .t3, .t4, .t5{
padding:1em;
border:1em;
}
</style></head><body>
<p>This is a quick summary of the CIL lighthouse data, along with some of
the information being parsed out of each one.</p>
HEAD
print LIGHTS "<body style='background-color:black;color:white'>";

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
    
    # Add links to both files, from the index
    print INDEX "<li>$Name - <a href='$Name.html'>html</a>, <a href='$Name.txt'>data</a></li>\n";
    
    printf SUMMARY "<h1><a href='http://informationfreeway.org/?lat=%f&amp;lon=%f&amp;zoom=15&amp;layers=0000F0B0F'>%s</a></h1>\n", $Lat, $Lon, $Name;
    
    printf SUMMARY "<p><a href='http://en.wikipedia.org/wiki/%s'>Wikipedia</a></p>\n", $Name;

    printf SUMMARY "<pre class=t1><b>Tech =</b> %s</pre>\n", Dumper($Data->{'Technical Data'});
    printf SUMMARY "<pre class=t2><b>Light sequence =</b>%s</pre>\n", Dumper($Data->{light_sequence});
    
    printf SUMMARY "<p><b>Description = %s</b></p>\n", $Data->{description};
    
    printf SUMMARY "<pre class=t3><b>Building =</b>%s</pre>\n",  Dumper($Data->{structure});
    
    printf SUMMARY "<pre class=t4><b>Links =</b>%s</pre>\n", Dumper($Data->{Links});
    
    printf SUMMARY "<div class=t5><b>Original description =</b>%s</div>\n", $Description;
    
    printf SUMMARY "<hr />\n";
    
    
    # Add this location to the main OSM file output
    print OSM toOsm($Data, $Lat, $Lon, $Name);
  }
}
print OSM "</osm>\n";
close;

# Optional: dump the list of all tag/value combinations that were used (to help with documentation of the tagging scheme)
if(1)
{
  open TAGS, ">tags.txt";
  while(my($k,$vs) = each(%{$TagUse}))
  {
    print TAGS "* $k\n";
    printf TAGS "** %s\n", join(" | ", keys(%{$vs}));
    while(my($v,$one) = each(%{$vs}))
    {
      print TAGS "** $v\n";
    }
  }
}

while(my ($c, $num) = each(%Colours))
{
print MISC "$c\n";
}
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
      if($1 eq 'Character')
      {
        $Data->{light_sequence} = parseCharacter($2);
      }      
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
      #print MISC "$Line\n";
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
    
    # Store the original text of all the lines
    #push(@{$Data->{Lines}},$Line);
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
  
  #print MISC Dumper($Data), "\n\n";
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
  $Text .= makeTag('man_made', $Manmade);
  $Text .= makeTag('source', 'Commissioners of Irish Lights');
  $Text .= makeTag('website:source', 'http://cil.ie/');
  $Text .= makeTag('description', $Data->{description});
  
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
    'building:decoration:single_band', 
    $Data->{'structure'}->{band});
  
  $Text .= makeTag(
    'building:decoration:multiple_bands', 
    $Data->{'structure'}->{bands});
  
  $Text .= makeTag(
    'lighting:colour', 
    $Data->{'structure'}->{lantern});
    
  
  # Take certain things out of "technical data" and into other named tags
  my %ConvertToOsm = (
    'Height of Tower/Structure'=>'building:height',
    'Fog Signal'=>'audio:as_text', 
    'Height of Light Above MHWS'=>'ele:mhws', 
    'DGPS'=>'dgps',
    'Built'=>'start_date', 
    'AIS'=>'automatic_identification_system');
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
    $Text .= makeTag('navaid:sea:'.$k, $v);
  }
    
  $Text .= makeTag(
    'start_date', 
    $Data->{'Technical Data'}->{Built});
  
  makeTag('signal_light', $Data->{structure}->{lantern});

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
  #$v =~ s{meters}{metres};
  
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
  $Text =~ s{°}{&deg;};
  return($Text);
}


sub parseCharacter
{
  my $Text = shift();
  #print STDERR "$Text -->\n";
  my $Orig = $Text;
  my $Data;
  my @Parts = split(/\s+\+\s*/, $Text);
  
  #printf( "%s -> %s\n", $Text, join(":::", @Parts)) if(scalar(@Parts) > 1); return;
  
  foreach my $Part(@Parts)
  {
    #push(@{$Data}, $Part);
    push(@{$Data}, parseCharacter2($Part));
  }
  
  printf LIGHTS "<h1>%s</h1>\n", $Orig;
  my ($Debug, $Html) = recreateLight($Data);
  
  my $Filename = "Img/$Orig.png";
  textToImage($Html,$Filename);
  
  printf LIGHTS "<p><img src='$Orig.png' /></p>\n", $Filename;
  #printf LIGHTS "<p>%s</p>\n", $Html x 3;
  printf LIGHTS "<pre><b>%s</b></pre>\n", Dumper($Data);
  #printf LIGHTS "<pre style='color:blue'><b>%s</b></pre>\n", $Debug;
  
  return($Data);
}

sub parseCharacter2
{
  my $Text = shift();
  
  my $Data;
  
  if($Text =~ s{UNLIT}{}i)
  {
    $Data->{unlit} = 1;
    return($Data);
  }
  if($Text =~ s{^(Dir)\s*}{})
  {
    $Data->{dir} = 1;
  }
  if($Text =~ s{Mo\s*\(\s*(\w+)\s*\)\s*}{}i)
  {
    $Data->{morse} = $1;
  }
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
  if($Text =~ s{([0-9.]+)\s*s$}{}i)
  {
    $Data->{period} = $1;
  }
  if($Text =~ s{^\s*\(([0-9+]+)\s*\)}{})
  {
    @{$Data->{repeat}} = split(/\+/,$1);
  }
  if($Text =~ s{^\s*([WYRG]+)}{})
  {
    $Data->{colour_abbr} = $1;
  }
  else
  {
    $Data->{colour_abbr} = 'W';
  }
  my %Cols = (
    'W' => 'white',
    'Y' => 'yellow',
    'R' => 'red',
    'G' => 'green');
  foreach my $Letter(split(//,$Data->{colour_abbr}))
  {
    push(@{$Data->{colours}}, $Cols{$Letter});
  }
  
  $Data->{unconverted} = $Text if($Text =~ m{\S});
  
  #print STDERR "*** $Text from \"$Data->{part}\"\n" if($Text =~ m{\S});
  return($Data);
}

sub recreateLight
{
  my $Data = shift();
  my $Html = '';
  my $Debug = '';
  
  foreach my $Part(@{$Data})
  {
    my ($D,$H) = recreateLightPart($Part);
    $Html .= $H;
    $Debug .= $D;
  }
  return($Debug, $Html);
}

sub recreateLightPart
{
  my $Data = shift();
  my $Html = '';
  my $Debug = '';
  return('',formatFlash(10,"black")) if($Data->{unlit});
  
  my $Len = LengthOfFlashType($Data->{abbr});
  my $Period = $Data->{period};
  
  my @Reps;
  if( exists($Data->{repeat}))
  {
    @Reps = @{$Data->{repeat}};
    $Debug = "normal repeat";
  }
  elsif($Data->{abbr} eq 'Iso')
  {
    @Reps = (1);
    $Len = $Period / (2.0 * scalar(@{$Data->{colours}}));
    $Debug = "Iso";
  }
  elsif($Data->{abbr} eq 'Oc')
  {
    $Html .= formatFlash(1, "black");
    $Html .= formatFlash($Period - 1, $Data->{colours}[0]);
    return('Occluded, assuming 1s dark', $Html);
  }
  elsif(defined $Data->{morse})
  {
    # Morse code is special case, gets handled separately
    my $Code = morse($Data->{morse});
    my $Time = 0;
    foreach my $DotDash(split(//,$Code))
    {
      my $Pulse = $DotDash eq '-' ? 1.5 : 0.5;
      $Html .= formatFlash($Pulse, $Data->{colours}[0]);
      $Html .= formatFlash(0.5, "black");
      $Time += $Pulse + 0.5;
    }
    if(defined($Period) and ($Time < $Period))
      {
      $Html .= htmlFlash($Period - $Time, "black");
      }
    return('Morse',$Html);
  }
  elsif(! defined $Len)
  {
    return('No length',formatFlash(5,"blue"));
  }
  elsif(defined $Period and defined $Len)
  {
    #$Debug = "Reps from Period";
    #@Reps = ($Period / (2.0 * $Len));
    @Reps = (1);
  }
  else
  {
    $Debug = "Len but not period";
    @Reps = (4);
    $Period = scalar(@Reps) * 2.0 * $Len;
  }
  
  my @Colours = @{$Data->{colours}};
  
  my $Time = 0;
  my $MultiReps = scalar(@Reps) > 1;
  foreach my $Num(@Reps)
  {
    foreach(1..int($Num))
    {
      foreach my $Colour(@Colours)
        {
        $Html .= formatFlash($Len, $Colour);
        $Html .= formatFlash($Len, "black");
        $Time += 2 * $Len;
        }
    }
    if($MultiReps)
    {
        $Html .= formatFlash($Len, "black");
        $Time += $Len;
    }
  }
  if($Time < $Period)
    {
    $Html .= formatFlash($Period - $Time, "black");
    }
    
  return($Debug,$Html);
}

sub textToImage
{
  use GD;
  my ($Text, $Filename) = @_;
  
  $Text = $Text x 3;
  my $dx = 4;
  
  my $width = 0;
  foreach my $Block(split(/\|/,$Text))
  {
    my ($Colour,$len) =split(/,/, $Block);
    $width += $len * $dx;
  }
  return if($width == 0);
  
  my $height = 20;
  
  my $Image = new GD::Image( $width, $height );
  
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
  
  
  my $x = 0;
  foreach my $Block(split(/\|/,$Text))
  {
    my ($Colour,$len) =split(/,/, $Block);
    $len *= $dx;
    $Image->filledRectangle($x,0,$x+$len,$height,$Colours{$Colour});
    $x+=$len;
  }
  
  open(my $fp, ">$Filename") || return; 
  binmode $fp;
  print $fp $Image->png();
  close $fp;
}

sub formatFlash
{
  #return(imgFlash(@_));
  return(htmlFlash(@_));
}

sub imgFlash
{
  my ($Len, $Colour) = @_;
  
  return sprintf("%s,%s|", $Colour, int($Len * 4));
}

sub htmlFlash
{
  my ($Len, $Colour) = @_;
  
  my %ColourMatch = (
    'white'=>'#CCC',
    'red'=>'#F88',
    'green'=>'#AFA');
  
  $Colour = $ColourMatch{$Colour} if(defined($ColourMatch{$Colour}));
  
  return sprintf(
    "<span style='background-color:%s'>%s</span>", 
      $Colour,
      "&nbsp;" x int($Len * 4));
}
sub LengthOfFlashType
{
  my %Options = (
      'LFl' => 2,
      'Fl' => 1,
      'Q' => 0.5,
      'VQ' => 0.25,
      'Iso' => 5,
      'Occ' => 1
      );
  return($Options{shift()});
}

sub morse
{
  my %Morse = (
  'A'=>'.-',      'G'=>'',      'L'=>'',      'Q'=>'',      'V'=>'',      
  'B'=>'-...',      'H'=>'',      'M'=>'',      'R'=>'',      'W'=>'',      
  'C'=>'-.-.',      'I'=>'',      'N'=>'',      'S'=>'',      'X'=>'',      
  'D'=>'-..',      'J'=>'',      'O'=>'',      'T'=>'',      'Y'=>'',      
  'E'=>'.',      'K'=>'',      'P'=>'',      'U'=>'',      'Z'=>'',      
  'F'=>''); # TODO
  return($Morse{shift()});
}