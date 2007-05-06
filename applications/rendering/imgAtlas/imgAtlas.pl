#!/usr/bin/perl
#-----------------------------------------------------------------------------
# Document-creation system, for PDF output
# Supports the layout of:
#  * JPEG photos
#  * OpenStreetMap maps
#  * Simple text
#
#-----------------------------------------------------------------------------
# Copyright 2007
#  * Oliver White
#  * Hakan Tandogan
#
#    (Add your name here if you edit the program)
#-----------------------------------------------------------------------------
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
use strict;
use osm::getTileArea;
use PDF::API2;
use constant mm => 25.4 / 72;
my $Usage = "$0 [config file] [output pdf filename]\n";
my $Config = shift() || "config.txt";
my $Filename = shift() || "atlas.pdf";

open(COMMANDS, '<', $Config) || die($Usage);
my $PDF = PDF::API2->new();
my ($Page, @Attributions);
my $PageNum = 1;
# Default options
my %Option = (dpi=>150); 

newPage();
my $Font = $PDF->corefont('Helvetica');

  
while(my $Line = <COMMANDS>)
{
    $Line =~ s{     # replace:
	^           # at start of line
	\s*         #
	\#          # Comment characters
	.*          # ...
	$           # to end of line
    }{}x;           # with nothing

    #---------------------------------------------------------------------------
    #  New page
    #---------------------------------------------------------------------------
    if($Line =~ m{==page==}i)
    {
	$Page = $PDF->page;
	$PageNum++;
    }
    #---------------------------------------------------------------------------
    #  Change an option
    #---------------------------------------------------------------------------
    elsif($Line =~ m{
      option:         # command
	\s*           #
	(\w+)         # option name
	\s* = \s*     #  =
	(.*)          # value
    }x)
    {
	$Option{$1} = $2;
	print "Setting option \"$1\" to \"$2\"\n";

	if ($1 eq "tilesource")
	{
	    getTile::setTileSource($2);
	}
    }
    #---------------------------------------------------------------------------
    #  Add a line of text
    #---------------------------------------------------------------------------
    elsif($Line =~ m{
      text:                 # command
	\s*                 #
	(.*?)               # text
	\s*                 #
	\(                  # (
	(.*?), \s*          #  x
	(.*?), \s*          #  y
	(.*?)               #  size
	\)                  # )
    }x)
    {
	print "Adding \"$1\" at $2, $3, size $4\n";
	textAt($1,$2,$3,$4);
    }
    #---------------------------------------------------------------------------
    #  Add a list of images used within the document
    #---------------------------------------------------------------------------
    elsif($Line =~ m{
      attribution:          # command
	\s*                 #
	\(                  # (
	(.*?), \s*          #  x
	(.*?), \s*          #  y
	(.*?)               #  size
	\)                  # )
    }x)
    {
	printf "Adding attribution block\n";
	my $X = $1;
	my $Y = $2;
	my $Size = $3;

	# Add a title, just as an extra line of text at the beginning of the textblock
	unshift(@Attributions, "Image credits:");

	# Draw each line of text (TODO: make a generic "write array of text lines")
	foreach my $Line (@Attributions)
	{
	    textAt($Line, $X, $Y, $Size);
	    $Y -= $Size * 1.25;
	}
    }
    #---------------------------------------------------------------------------
    #  Add a JPEG image to the page
    #---------------------------------------------------------------------------
    elsif($Line =~ m{
      image:               # Command
	\s*                #
	(.*?)              # Filename
	\s*                #
	\(                 # (
	(\d+), \s*         #  left
	(\d+), \s*         #  bottom
	(\d+), \s*         #  width
	(\d+), \s*         #  height
	\"(.*?)\",\s*      #  author
	\"(.*?)\"          #  description
	\)                 # )
    }ix)
    {
	print "Adding $1 ($7) by $6 at $2, $3, $4, $5\n";
	my $PageGfx = $Page->gfx;
	my $Image = $PDF->image_jpeg($1) || print("Failed to load");
	$PageGfx->image($Image, $2/mm, $3/mm, $4/mm, $5/mm ); # from left, from bottom, width, height
    
	# Add to the list of images 
	# (this text needs to identify which image we're talking about, hence the description)
	push(@Attributions, "Page $PageNum: Image \"$7\", by $6");
    }
    #---------------------------------------------------------------------------
    #  Add a map to the page
    #---------------------------------------------------------------------------
    elsif($Line =~ m{
      map:                 # Command
	\s*                #
	\((.*?)\)          # Map options
	\s*                #
	at                 # at
	\s*                #
	\((.*?)\)          # Position options
    }ix)
    {
	my ($MapOptions, $PositionOptions) = ($1, $2);
	my ($Lat, $Long, $SizeKm, $Zoom) = split(/\s*,\s*/, $MapOptions);
	my ($X,$Y,$W,$H) = split(/\s*,\s*/, $PositionOptions);

	# Process those map options
	my $Filename = datafile('png');
	my $AspectRatio = $W / $H;
	my $ImgW = ($W / 25) * $Option{dpi}; # pixels = width in inches * DPI
	my $ImgH = $ImgW / $AspectRatio;
    
	if (!(defined($Zoom)))
	{
	    my $DesiredSizeOfTilePx = getTile::size();
	    # print "  .... auto-determiner: DesiredSizeOfTilePx = $DesiredSizeOfTilePx\n";

	    my $DesiredWidthInTiles = $ImgW / $DesiredSizeOfTilePx;
	    # print "  .... auto-determiner: ImgW = $ImgW\n";
	    # print "  .... auto-determiner: DesiredWidthInTiles = $DesiredWidthInTiles\n";

	    my $DesiredSizeOfTileKm = $SizeKm / $DesiredWidthInTiles;
	    # print "  .... auto-determiner: SizeKm = $SizeKm\n";
	    # print "  .... auto-determiner: DesiredSizeOfTileKm = $DesiredSizeOfTileKm\n";

	    my $DesiredZoom = 12 - log($DesiredSizeOfTileKm / 10) / log(2);		
	    # print "  .... auto-determiner: DesiredZoom = $DesiredZoom\n";

	    $Zoom = int($DesiredZoom + 0.5);
	    print "Auto-Determined Zoom: $Zoom\n";
	}
    
	my $SizeOfTileKm = 10 * (2 ** (12 - $Zoom));  # Z-12 is about 10km, and each additional zoom halves that
	my $WidthInTiles = $SizeKm / $SizeOfTileKm;   # How many tiles should fit across the map
	my $SizeOfTilePx = $ImgW / $WidthInTiles;     # How wide each tile should be made, in pixels
	print "$SizeKm km across, $SizeOfTileKm km per tile =  $WidthInTiles tiles, so $SizeOfTilePx px per tile\n";
	my %Area = (lat=>$Lat, long=>$Long, zoom=>$Zoom, width=>$ImgW, height=>$ImgH, size=>$SizeOfTilePx);
    
	# Create the map
	print "Creating map around $Lat, $Long, from zoom $Zoom tiles\nUsing image $ImgW x $ImgH\n";
	getTileArea::createArea(\%Area, $Filename);

	# Add map to page
	my $PageGfx = $Page->gfx;
	my $Image = $PDF->image_png($Filename) || print("Failed to load");
	$PageGfx->image($Image, $X/mm, $Y/mm, $W/mm, $H/mm ); # from left, from bottom, width, height
    }
    #---------------------------------------------------------------------------
    #  Unrecognized lines of input
    #---------------------------------------------------------------------------
    else
    {
	#print "Misunderstood $Line\n";
    }
}

print "Saving PDF to $Filename\n";
$PDF->saveas($Filename);

BEGIN
{
    my $NextFileNum = 1;
    sub datafile
    {
	my $Suffix = shift();
	return
	    sprintf("Data/%05d.$Suffix", $NextFileNum++);
    }
}

sub textAt
{
    my ($Text, $X, $Y, $Size) = @_;

    my $TextHandler = $Page->text;
    $TextHandler->font($Font, $Size/mm );
    $TextHandler->fillcolor('black');
    $TextHandler->translate($X/mm, $Y/mm);
    $TextHandler->text($Text);
}

sub newPage
{
    # A4 page
    $Page = $PDF->page;
    $Page->mediabox(210/mm, 297/mm);
    $Page->cropbox (10/mm, 10/mm, 200/mm, 287/mm);
}
