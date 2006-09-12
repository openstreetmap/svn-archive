#!/usr/bin/perl

BEGIN {
    unshift(@INC,"../perl");
    unshift(@INC,"~/svn.openstreetmap.org/utils/perl");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/utils/perl");
}

use strict;
use warnings;

use PDF::API2;
use Data::Dumper;
use constant mm => 25.4/72;
use Carp qw(cluck confess);
use Getopt::Long;

use Geo::GPX::File;
use Geo::Geometry;
use Geo::OSM::Planet;
use Geo::OSM::SegmentList;
use Geo::OSM::Tracks2OSM;
use Geo::OSM::Write;
use Geo::Tracks::GpsBabel;
use Geo::Tracks::Kismet;
use Geo::Tracks::NMEA;
use Geo::Tracks::Tools;
use Utils::Debug;
use Utils::File;
use Utils::Math;

sub CreateAtlas($); #{}
sub LoadOSM($$); #{}
sub AddTitlePage($$); #{}
sub TextPage($$); #{}
sub AddMetaInfo($$); #{}
sub LoadData($); #{}
sub ReadFile($); #{}
sub BBoxPages($); #{}
sub ContentsPage($$); #{}

my $ConfigFile = "Config/config.txt";
Getopt::Long::Configure('no_ignore_case');
print "Options: -".join(",",@ARGV)."-\n";
my $CommandLineOptions;
my $ResultDir=osm_dir().'/pdf-atlas/Results';

GetOptions ( 
	     'debug+'     => \$DEBUG,      
	     'd+'         => \$DEBUG,      
	     'c:s'        => \$ConfigFile,
	     'config:s'   => \$ConfigFile,
	     'Places:s'   => \$CommandLineOptions->{Places},
	     'ResultDir:s'=> \$ResultDir,
	     );

mkdir_if_needed( $ResultDir );

print "Rest Options: -".join(",",@ARGV)."-\n";
  
# Read the configuration file
my $Options = ReadFile($ConfigFile);
printf STDERR "CreateAtlas() Options: ".Dumper(\$Options)."\n" 
    if $DEBUG>12;

for my $k ( keys %{$CommandLineOptions} ) {
    my $value = $CommandLineOptions->{$k};
    $Options->{$k}=$value if defined $value;
    printf STDERR "CreateAtlas() Additional Option %-12s: $Options->{$k}\n",$k 
	if $DEBUG>2;      
}

if ( shift() ) {
    die("Usage: $0 [-d] [--config=<Config file>]\n");
};
    
CreateAtlas($Options);
exit;


#---------------------------------------------------------
# Create a PDF road atlas, based on options in a config file
#---------------------------------------------------------
sub CreateAtlas($){
  my $Options = shift;
  my $PDF = PDF::API2->new();


{ # Setting defaults
    my ($guess_name) = ( $Options->{Places}  =~ m/places-(.+).txt/ );
    unless (defined( $Options->{"Filename"} )) {
	die "Cannot guess name for defaults" unless $guess_name;
	$Options->{"Filename"} = "osm_atlas-$guess_name.pdf";
	}
    unless (defined( $Options->{"Title"} )) {
	die "Cannot guess name for defaults" unless $guess_name;
	$Options->{"Title"} = "OSM $guess_name Atlas";
    }
}

  # Title page
  AddTitlePage($PDF, $Options->{"Title"}) 
      if $Options->{"Title"};
  
BBoxPages( $Options);
  my $Data = LoadData($Options);

  MapPages($PDF, $Options, $Data);
  
  # License page
  TextPage($PDF, $Options->{"License"});
  
  # Save the PDF
  my $pdf_filename=$ResultDir."/".$Options->{"Filename"};
  printf STDERR "Saving %s\n", $pdf_filename;
  $PDF->saveas($pdf_filename);
}

#---------------------------------------------------------
# Adds a title page to the PDF document
#---------------------------------------------------------
sub AddTitlePage($$)
{
  my ($PDF, $Title) = @_;
  my $Font = $PDF->corefont('Helvetica');
  
  # Add file meta-informationshift()
  AddMetaInfo($PDF, $Title);
   
  # Create a new page for the title
  my $Page = $PDF->page;
  
  my $TextHandler = $Page->text;
  $Page->mediabox(210/mm, 297/mm);
  $Page->cropbox (10/mm, 10/mm, 200/mm, 287/mm);
  
  # Write the page title (TODO: read this from a text file)
  foreach my $Line(
    ("105, 200, 11, centre, black, $Title",
    "105, 60, 6, centre, black, Created from OpenStreetMap data",
    "105, 50, 6, centre, black, http://openstreetmap.org.uk/",
    "105, 40, 6, centre, black, Published under a Creative Commons license",
    ))
    {
    my ($X, $Y, $Size, $Pos, $Colour, $Text) = split(/,\s+/, $Line);
    
    $TextHandler->font($Font, $Size/mm );
    $TextHandler->fillcolor($Colour);
    $TextHandler->translate($X/mm, $Y/mm);
    
    $TextHandler->text($Text) if($Pos eq "left");
    $TextHandler->text_center($Text) if($Pos eq "centre");
    $TextHandler->text_right($Text) if($Pos eq "right");
    }
}

#---------------------------------------------------------
# Adds a page of preformatted text, from a text file
#---------------------------------------------------------
sub TextPage($$)
{
  my ($PDF, $Filename) = @_;
  my $Page = $PDF->page;
  $Filename =~ s/\~/$ENV{HOME}/;

  my $TextHandler = $Page->text;
  $TextHandler->font($PDF->corefont('Helvetica'), 6/mm );
  $TextHandler->fillcolor('black');
  printf STDERR "Reading TextPage $Filename\n";
  open(my $fp, "<", $Filename) 
      || die("Can't open $Filename ($!)\n");
  
  my ($x, $y) = (30, 250);
  foreach my $Line(<$fp>){
    chomp $Line;
    $TextHandler->translate($x/mm, $y/mm);
    $TextHandler->text($Line);
    $y -= 7;
  }
}
#---------------------------------------------------------
# Adds meta-information to a PDF
#---------------------------------------------------------
sub AddMetaInfo($$)
{
  my ($PDF, $Title) = @_;
  
  # Timestamp (using perl standard functions to make script easier to install)
  my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = gmtime(time);
  my $Timestamp = sprintf("D:%04d%02d%02d%02d%02d%02d+00;00", $year+1900, $mon+1, $mday, $hour, $min, $sec);
  
  $PDF->info(
        'Author'       => "OpenStreetMap community",
        'CreationDate' => $Timestamp,
        'ModDate'      => $Timestamp,
        'Creator'      => "OJW's script",
        'Producer'     => "PDF::API2",
        'Title'        => $Title,
        'Subject'      => "Cartography",
        'Keywords'     => "OpenStreetMap"
    );
}

sub LoadData($)
{
  my ($Options) = @_;
  my %Data;
  
  foreach my $Datatype("Styles","Equivalences","Filters")
  {
    my $Filename = $Options->{$Datatype};
    $Data{$Datatype} = ReadFile($Filename);
  }
 
  die "No Area defined for ".Dumper($Options)."\n" 
      unless defined $Options->{"Area"};
  print "Area: '$Options->{Area}'\n" if $DEBUG;
  my ($LatC, $LongC, $Size) = split(/\s*,\s*/,$Options->{"Area"});
  my $Margin = 1.5;
  my %Bounds = (
    "W" => $LongC - $Margin * $Size,
    "E" => $LongC + $Margin * $Size,
    "S" => $LatC - $Margin * $Size,
    "N" => $LatC + $Margin * $Size);

  print "Bounds: ".Dumper(\%Bounds) if $DEBUG>10;
  die "No Size defined for ".Dumper($Options)."\n" unless $Size;
  
  $Data{"Coast"} = LoadGSHHS($Options->{"Coast"}, \%Bounds) if($Options->{"Coast"});
    
  $Data{"Segments"} = LoadOSM($Options->{"Data"}, \%Bounds) if($Options->{"Data"});
  
  printf STDERR "Data loaded\n" if $DEBUG;
  return(\%Data);
}

sub LoadGSHHS()
{
  my ($Filename, $Bounds) = @_;
  my @Coasts;

  $Filename =~ s/\~/$ENV{HOME}/;

  if ( ! -s $Filename && -s osm_dir()."/$Filename" ) {
      $Filename=osm_dir()."/$Filename";
  }
  printf STDERR "Reading GSHHS file $Filename\n" if $DEBUG;
  # Open the file for reading, binary  
  open(my $fp, "<", $Filename)
      || die("Can't open GSHHS file $Filename ($!)\n");
  binmode($fp);
  printf "Loading coastlines from %s\n", $Filename;
  
    printf "GSHHS Area Bounds lat %f to %f, long %f to %f\n",
    $Bounds->{"S"},
    $Bounds->{"N"},
    $Bounds->{"W"},
    $Bounds->{"E"}; 

  # Continue reading headers until end of file
  while(read($fp, my $header, 8 * 4 + 2 * 2))
    {
    my ($Prev, $PLat, $PLon) = (0,0,0);
    my ($id, $numpoints, $level,
        $west, $east, $north, $south,
        $area, $crosses, $source) = unpack("NNNN4Nnn", $header);

    # Loop through vertices in this polygon
    foreach(1..$numpoints)
      {
      # Read from file
      die("GSHHS error\n") if(!read($fp, my $datapoint, 8));

      # Unpack binary data into local variables
      my($x, $y) = unpack("NN", $datapoint);

      my $Lat = coastcode($y);
      my $Lon = coastcode($x);
      $Lon -= 360 if($Lon > 180);

      if($Lat > $Bounds->{"S"} and 
        $Lat < $Bounds->{"N"} and 
        $Lon > $Bounds->{"W"} and 
        $Lon < $Bounds->{"E"}){
      
        push(@Coasts, "$PLat, $PLon, $Lat, $Lon") if($Prev);
      }
      ($PLat, $PLon, $Prev) = ($Lat, $Lon, 1);
      }
    }
  close($fp);
  printf STDERR "Reading GSHHS file $Filename complete\n";

  return(\@Coasts);
}

sub coastcode($){
  my $x = shift();
  if($x > 0x80000000){
    $x -= 0xFFFFFFFF; $x--;
  }
  return($x / 1E+6);
}

sub LoadOSM($$)
{
  my ($Filename, $Bounds) = @_;
  $Filename =~ s/\~/$ENV{HOME}/;

  printf STDERR "Reading OSM File: $Filename\n";
  open(my $fp, "<", $Filename) 
      or die("Can't open $Filename ($!)\n");
  printf "Loading streetmaps from %s\n", $Filename;
  my @Lines = <$fp>;
  chomp @Lines;
  close($fp);  

  return(\@Lines);
}


sub MapPages($$$)
{
  my ($PDF, $Options, $Data) = @_;
  
  my $Filename = $Options->{"Places"};
  $Filename =~ s/\~/$ENV{HOME}/;
  open(my $fp, "<", $Filename) 
      or die("Can't open $Filename ($!)\n");
  
  my @Maps;
  
  foreach my $Line(<$fp>){
      chomp $Line;
      $Line =~ s/[\r\a\n\s]*$//g;;
      next if $Line =~ m/^\s*\#/;# Comments starting with #
      next if $Line =~ m/^\s*$/; # Empty lines
      push(@Maps, $Line);
  }
  printf "Creating %d maps\n", scalar(@Maps);
  
  my ($LatC, $LongC, $Size) = split(/,/,$Options->{"Area"});
  MapContentsPage($PDF, \@Maps, $LatC, $LongC, $Size, $Data);
  
  
  foreach my $Map(@Maps)
  { 
      my ($Name, $More) = split(/:\s*/, $Map);
      my ($Lat, $Long, $Size, $Type) = split(/\s*,\s*/, $More);
      
      printf STDERR "Generating map for $Name at $Lat, $Long\n";      
      MapPage($PDF, $Lat, $Long, $Size, $Name, $Type, $Data);
  }
}

sub BBoxPages($)
{
  my ($Options) = @_;
  
  my $Filename = $Options->{"Places"};
  $Filename =~ s/\~/$ENV{HOME}/;
  open(my $fp, "<", $Filename) 
      or die("Can't open $Filename ($!)\n");
  
  my $lat_min =  90;
  my $lat_max = -90;
  my $lon_min =  180;
  my $lon_max = -180;

  foreach my $Line(<$fp>){
      chomp $Line;
      $Line =~ s/[\r\a\n\s]*$//g;;
      next if $Line =~ m/^\s*\#/;# Comments starting with #
      next if $Line =~ m/^\s*$/; # Empty lines
      $Line=~ s/^.+\:\s*//;
      my ($Lat, $Lon, $Size, $Type) = split(/\s*,\s*/, $Line);
      for my $size ( ($Size,-$Size)){
	  $lat_min  = $Lat+$size  if $lat_min > $Lat+$size;
	  $lat_max  = $Lat+$size  if $lat_max < $Lat+$size;
	  
	  $lon_min  = $Lon+$size  if $lon_min > $Lon+$size;
	  $lon_max  = $Lon+$size  if $lon_max < $Lon+$size;
      }
  }

  my $LatC=($lat_min+$lat_max)/2;
  my $LonC=($lon_min+$lon_max)/2;
  my $Size=max($lat_max-$lat_min,$lon_max-$lon_min)/2;
  printf "BBox   $Options->{Area} --> ($LatC,$LonC,$Size) for maps\n";
  $Options->{"Area"}="$LatC,$LonC,$Size";
  close $fp;

}

#---------------------------------------------------------
# Adds a simple "text-style" contents page
#---------------------------------------------------------
sub ContentsPage($$)
{
        my ($PDF, $Maps) = @_;
        my $Page = $PDF->page;
        my $TextSize = 4.5;

        my $PageNum = $PDF->pages + 1; # Assume first map is on page after the current one
        my $TextHandler = $Page->text;
        
        # Title
        $TextHandler->fillcolor('black');
        $TextHandler->font($PDF->corefont('Helvetica'), 7/mm );
	$TextHandler->translate(40/mm, 232/mm);
        $TextHandler->text("Contents:");

        # Setup size and position of contents items
        $TextHandler->font($PDF->corefont('Helvetica'), $TextSize/mm );  
	my $y = 220;
                
        foreach my $Map(@$Maps)
        {
	    my ($Name, $Misc) = split(/:/, $Map);
	    
	    # Name
            $TextHandler->translate(40/mm, $y/mm);
	    $TextHandler->text($Name);
	    
	    # Page num      
	    $TextHandler->translate(150/mm, $y/mm);
	    $TextHandler->text($PageNum++);
	    
            $y -= ($TextSize+1);                
        }
}
sub MapContentsPage(){
  my ($PDF, $Maps, $Lat, $Long, $Size, $Data) = @_;
  my $Proj = SetupProjection($Lat, $Long, $Size);

  my $Page = $PDF->page;
  $Page->mediabox(210/mm, 297/mm);
  
  my $gfx = $Page->gfx;
  my $text = $Page->text;
  $text->font($PDF->corefont('Helvetica'), 4/mm );
          
  DrawCoastline($Data->{"Coast"}, $Proj, $gfx);
  
  my $Count = $PDF->pages + 1;
  foreach my $Map(@$Maps)
  {
    my ($Name, $More) = split(/:\s*/, $Map);
    my ($LatS, $LongS, $SizeS, $Type) = split(/\s*,\s*/, $More);
    my $IsCity = $Type eq "city";
    my $Colour = $IsCity ? "#000000" : "#40C0FF";
    
    unless ( defined($LatS) && defined($LongS) && defined($SizeS) ) {
	confess "Map Definition has undefined Values:".Dumper(\$Map)."\n";
    }
    my $Proj2 = SetupProjection($LatS, $LongS, $SizeS);
    
    my ($x1, $y1) = Project($Proj, $Proj2->{"S"}, $Proj2->{"W"});
    my ($x2, $y2) = Project($Proj, $Proj2->{"N"}, $Proj2->{"E"});
    
    # Map border
    $gfx->strokecolor($Colour);
    $gfx->rect($x1/mm, $y1/mm, ($x2-$x1)/mm, ($y2-$y1)/mm); 
    $gfx->stroke();
    $gfx->endpath();
    
    # Text (inside area maps, outside city maps)
    if(!$IsCity){
      $text->fillcolor($Colour);
      $text->translate((($IsCity ? $x2 : $x1) + 1)/mm, ($y2 - 5)/mm);
      $text->text($Name);
    }
    
    $Count++;
  }
}

#---------------------------------------------------------
# Adds a page of maps
#---------------------------------------------------------
sub MapPage(){
  my ($PDF, $Lat, $Long, $Size, $Name, $Type, $Data) = @_;

  my $Proj = SetupProjection($Lat, $Long, $Size);

  my $Page = $PDF->page;
  $Page->mediabox(210/mm, 297/mm);
  
  my $gfx = $Page->gfx;
  my $Font = $PDF->corefont('Helvetica');

  # Draw the "simple" (A1 - G9) grid
  mapGrid($Page, 10, 287, 200, 10, $Font, $Proj);

  DrawCoastline($Data->{"Coast"}, $Proj, $gfx);

  DrawOSM($Data->{"Segments"}, $Proj, $gfx, $Data->{"Styles"});

  ScaleBar($PDF, $Page, $Proj);
  
  # White-out the edges of the page (stop maps spilling over)
  my $edges = $Page->gfx;
  $edges->rect(0/mm, 0/mm, 10/mm, 297/mm); # left
  $edges->rect(0/mm, 0/mm, 210/mm, 10/mm); # bottom
  $edges->rect(200/mm, 0/mm, 210/mm, 297/mm); # right
  $edges->rect(0/mm, 287/mm, 210/mm, 297/mm); # top
  $edges->fillcolor("#FFFFFF"); 
  $edges->fill; 
  $edges->endpath;
  
  # Map border
  $edges->strokecolor("#000080");
  $edges->rect(10/mm, 10/mm, 190/mm, 277/mm); 
  $edges->stroke();
  $edges->endpath();
  
  # Page number
  my $text = $Page->text;  
  $text->fillcolor("#000000"); 
  $text->font($Font, 6/mm );
  $text->translate( 10/mm, 289/mm );
  $text->text(sprintf("%d", $PDF->pages));
  
  # Page name
  $text->translate( 10/mm, 3/mm );
  $text->text($Name);
  
  # URL
  $text->translate( 200/mm, 3/mm );
  $text->text_right("http://openstreetmap.org/");
}

#---------------------------------------------------------
# Draw coastline segments       
#---------------------------------------------------------
sub DrawCoastline()
{
  my($Coast, $Proj, $gfx) = @_;

  if($Coast)
  {
    foreach my $Line(@$Coast)
    {
      my ($Lat1, $Long1, $Lat2, $Long2) = split(/,/, $Line);
      if(PossiblyOn($Proj, $Lat1, $Long1, $Lat2, $Long2))
      {
        my ($x1, $y1) = Project($Proj, $Lat1, $Long1);
        my ($x2, $y2) = Project($Proj, $Lat2, $Long2);
          
        my $Colour = "#0000FF";
        
        $gfx->strokecolor($Colour);
        $gfx->move($x1/mm,$y1/mm);
        $gfx->line($x2/mm,$y2/mm); 
        $gfx->stroke();
        $gfx->endpath();
      }
    }
  }     
}

#---------------------------------------------------------
# Draw OpenStreetMap segments
#---------------------------------------------------------
sub DrawOSM()
{
  my($Segments, $Proj, $gfx, $Styles) = @_;
  
  if($Segments)
  {
    foreach my $Line(@$Segments)
    {
      my($Lat1,$Long1,$Lat2,$Long2,$Class,$Name,$Highway) = split(/,/,$Line);
      unless ( $Lat1 && $Long1 && $Lat2 && $Long2 ) {
	  confess "$Line\nany of lat/lon is 0\n";
      }
      
      $Class = $Highway if(!$Class);
      

      $Class = lc($Class);  
      if(PossiblyOn($Proj, $Lat1, $Long1, $Lat2, $Long2))
      {
        my ($x1, $y1) = Project($Proj, $Lat1, $Long1);
        my ($x2, $y2) = Project($Proj, $Lat2, $Long2);
  
        my $Colour = exists($Styles->{$Class}) ? $Styles->{$Class}: "#A0A0A0";
  
        $gfx->strokecolor($Colour);
        $gfx->move($x1/mm,$y1/mm);
        $gfx->line($x2/mm,$y2/mm); 
        $gfx->stroke();
        $gfx->endpath();
      }
    }
  }
}
#---------------------------------------------------------
# Draw a basic (uncorrelated with anything) grid on a map
#---------------------------------------------------------
sub mapGrid()
{
  my ($Page, $x1, $y1, $x2, $y2, $Font, $Proj) = @_;
  my $grid = $Page->gfx;
  
  my $xLabels = "ABCDEFG";
  my $dx = ($x2 - $x1) / 7;
  my $dy = ($y2 - $y1) / 10;
  
  my $gridtext = $Page->text;  
  $gridtext->fillcolor("#C1E4FF"); 
  $gridtext->font($Font, 3/mm );
  
  my $count = 0;
  for(my $x = $x1; $x < $x2-1; $x += $dx)
  {
    $grid->move($x/mm,$y1/mm);
    $grid->line($x/mm,$y2/mm); 
    
    $gridtext->translate(($x + $dx - 4)/mm, ($y1 - 4)/mm );
    $gridtext->text(substr($xLabels, $count++, 1));
  }
  
  $count = 1;
  for(my $y = $y1; $y > $y2+1; $y += $dy)
  {
    $grid->move($x1/mm,$y/mm);
    $grid->line($x2/mm,$y/mm); 
    
    $gridtext->translate(($x1 + 2)/mm, ($y + $dy + 2)/mm );
    $gridtext->text($count++);
    
  }
  
  $grid->strokecolor("#C1E4FF");
  $grid->stroke();
  $grid->endpath();
}

#---------------------------------------------------------
# Draw a scalebar on a map
#---------------------------------------------------------
sub ScaleBar()
{
  my ($PDF, $Page, $Proj) = @_;
  my $EarthRadius = 6378;
  
  my $ScaleX = 20;
  my $ScaleY = 20;
  my $dx = 100;
  
  my $ScaleLenDeg = $Proj->{"dLong"} * $dx / $Proj->{"dx"};
  my $ScaleLenGuess = $EarthRadius * DegToRad($ScaleLenDeg) * $Proj->{"latlong_ratio"};
  
  my $ScaleLen = Round($ScaleLenGuess);
  $dx *= $ScaleLen / $ScaleLenGuess;
  
  my $scale = $Page->gfx;
  
  # Bordered rectangle, to get rid of any bits of map under the scalebar
  BorderRect($scale,
    ($ScaleX - 2)/mm, 
    ($ScaleY - 8)/mm, 
    ($dx + 4)/mm, 
    10/mm,
    "#FFFFFF", 
    "#CCCCFF");
  
  # Scalebar length
  $scale->move($ScaleX/mm, $ScaleY/mm);
  $scale->line(($ScaleX + $dx)/mm, $ScaleY/mm); 
  # and tickmarks
  foreach(0..10){
    my $Len = (($_ % 5) == 0) ? 2 : 1;
    my $X = $ScaleX + $dx * $_ / 10;
    $scale->move($X/mm, $ScaleY/mm);
    $scale->line($X/mm, ($ScaleY - $Len)/mm);
  }
  
  $scale->strokecolor("#000000");
  $scale->stroke();
  
  # Scalebar text
  my $text = $Page->text;  
  $text->fillcolor("#000000"); 
  $text->font($PDF->corefont('Helvetica'), 4/mm );
  $text->translate($ScaleX/mm, ($ScaleY - 6)/mm );
  $text->text(FormatNum($ScaleLen) . " km");
    
}

#---------------------------------------------------------
# Draw a rectangle with fill and border
#---------------------------------------------------------
sub BorderRect()
{
  my ($gfx, $x, $y, $w, $h, $fill, $line) = @_;
  $gfx->rect($x, $y, $w, $h);
  $gfx->fillcolor($fill); 
  $gfx->fill; 
  
  $gfx->rect($x, $y, $w, $h);
  $gfx->strokecolor($line);
  $gfx->stroke();
}

#---------------------------------------------------------
# Format a number without trailing zeros
#---------------------------------------------------------
sub FormatNum()
{
  my $Text = sprintf("%lf", shift());
  $Text =~ s/(\.\d*?)0+$/$1/;
  $Text =~ s/\.$//;
  return($Text);
}

#---------------------------------------------------------
# Utility: round a number to the nearest power of 10
#---------------------------------------------------------
sub Round()
{
  my $x = shift();
  my $Result = $x;
  foreach(0.1, 0.5, 1, 2, 5, 10, 50, 100){
    $Result = $_ if($x > $_);
    }
  return($Result);
}

#---------------------------------------------------------
# Tests whether a line can possibly intersect an area
#---------------------------------------------------------
sub PossiblyOn()
{
  my ($Proj, $Lat1, $Long1, $Lat2, $Long2) = @_;
  return(0) if($Long1 < $Proj->{"W"} && $Long2 < $Proj->{"W"});
  return(0) if($Long1 > $Proj->{"E"} && $Long2 > $Proj->{"E"});
  return(0) if($Lat1 < $Proj->{"S"} && $Lat2 < $Proj->{"S"});
  return(0) if($Lat1 > $Proj->{"N"} && $Lat2 > $Proj->{"N"});
  return(1);
}

#---------------------------------------------------------
# Project a lat/long onto x,y coordinates
#---------------------------------------------------------
sub Project($$$)
{
  my ($Proj, $Lat, $Long) = @_;


  if ( $Proj->{"dLong"} ==0  ) {
      cluck "Proj-dLong =0\n";
      return (0,0);
  }
  if ( $Proj->{"dLat"} ==0  ) {
      cluck "Proj-dat =0\n";
      return (0,0);
  }
  my $x = $Proj->{"x1"} + $Proj->{"dx"} * ($Long - $Proj->{"W"}) / $Proj->{"dLong"};
  my $y = $Proj->{"y1"} + $Proj->{"dy"} * ($Lat - $Proj->{"S"}) / $Proj->{"dLat"};
  
  return($x,$y);
}

#---------------------------------------------------------
# Initialise a map projection for later use by Project()
#---------------------------------------------------------
sub SetupProjection($$$)
{
  my ($Lat, $Long, $Size) = @_;
  my %Proj;
  
  # Scale to A4 page (note: A4 page sizes are hardcoded throughout at the moment)
  $Proj{"x1"} = 10;
  $Proj{"x2"} = 200;
  $Proj{"y1"} = 10;
  $Proj{"y2"} = 287;

  $Proj{"dx"} = $Proj{"x2"} - $Proj{"x1"};
  $Proj{"dy"} = $Proj{"y2"} - $Proj{"y1"};

  $Proj{"page_ratio"} = $Proj{"dy"} / $Proj{"dx"};

  # Size is used to define N-S limits
  $Proj{"N"} = $Lat + $Size;
  $Proj{"S"} = $Lat - $Size;
  $Proj{"dLat"} = $Proj{"N"} - $Proj{"S"};

  # Long/lat ratio is cos(lat) (very simple "projection")
  $Proj{"latlong_ratio"} = cos(DegToRad($Lat)); # ~0.5 for northern europe = long/lat

  confess("Projection D-Lat=0 for SetupProjection($Lat,$Long,$Size)\n")
	  unless $Proj{"dLat"};
  confess("Page ratio is =0 for SetupProjection($Lat,$Long,$Size)\n")
      unless $Proj{"page_ratio"};
  confess("latlong ratio is =0 for SetupProjection($Lat,$Long,$Size)\n")
      unless $Proj{"latlong_ratio"};
          
  $Proj{"dLong"} = $Proj{"dLat"} * $Proj{"page_ratio"} * $Proj{"latlong_ratio"};
  confess("Projection D-Lon =0 for SetupProjection($Lat,$Long,$Size)\n")
	  unless $Proj{"dLong"};

  $Proj{"W"} = $Long - 0.5 * $Proj{"dLong"};
  $Proj{"E"} = $Long + 0.5 * $Proj{"dLong"};
  
  return(\%Proj);
}
sub DegToRad(){
 return(3.1415926 * shift() / 180);
}
#---------------------------------------------------------
# Reads a file consisting of "Name: Value" pairs, and 
# returns them as a hash
#---------------------------------------------------------
sub ReadFile($){
  my $Filename = shift();

  $Filename =~ s/\~/$ENV{HOME}/;
  my %Options;
  printf STDERR "\n-----------------------------------\n" if $DEBUG>1;
  printf STDERR "  - Reading %s\n", $Filename;
  open(my $fp, $Filename) 
      || die("Can't open $Filename ($!)\n");
  
  foreach my $Line(<$fp>){
      chomp $Line;
      $Line =~ s/[\r\a\n\s]*$//g;;
      next if $Line =~ m/^\s*\#/;# Comments starting with #
      next if $Line =~ m/^\s*$/; # Empty lines
      my ($Key, $Value) = split(/:\s*/, $Line,2);
      chomp $Value;
      $Value =~ s/[\r\a\n\s]*$//g;;
      print STDERR "Key: $Key	Value:$Value\n" 
	  if $DEBUG>5;
      $Options{$Key} = $Value;
      unless ( $Key && defined($Value) && $Value ) { 
	  warn "Key:Value split not successfullt $Key:$Value"
	  };
  }
  close $fp;
  printf STDERR "Options($Filename):".Dumper(\%Options)."\n" if $DEBUG > 10;
  printf STDERR "File $Filename read complete\n" if $DEBUG >1;
  printf STDERR "+++++++++++++++++++++++++++++++++++++++++\n" if $DEBUG > 2;
  return(\%Options);
}
