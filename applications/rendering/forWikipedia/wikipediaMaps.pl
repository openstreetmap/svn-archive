#!/usr/bin/perl
#-----------------------------------------------------------------
# Creates maps for wikipedia articles
#-----------------------------------------------------------------
# Copyright, 2008, Oliver White
# 
# This program is free software: you can redistribute it and/or modify
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
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#---------------------------------------------------------------
# TODO:
#  * Check the map image page, and only create a map if it doesn't
#    already exist, or if the existing copy is more than 3 months
#    old
#  * Is there any way to get "last modified" date for OSM data
#    in a certain region?  Don't update wikipedia maps unless the 
#    OSM data has changed
#---------------------------------------------------------------
use strict;
use MediaWiki;
use Data::Dumper;
use LWP::Simple;
use File::Slurp;

use myWikiCredentials;
my ($WikiUser, $WikiPass) = getWikiUserPass();

# Create a connection to wikipedia
my $Wikipedia = MediaWiki->new;
my $OK1 = $Wikipedia->setup({
        'bot' => { 'user' => $WikiUser, 'pass' => $WikiPass },
        'wiki' => {
            'host' => 'en.wikipedia.org',
            'path' => 'w'}});

die("Couldn't login to wikipedia") if(!$OK1);
printf "Logged into wikipedia as \"%s\"\n", $Wikipedia->user(); 

# Create a connection to wikimedia commons
my $Commons = MediaWiki->new;
my $OK2 = $Commons->setup({
        'bot' => { 'user' => $WikiUser, 'pass' => $WikiPass },
        'wiki' => {'host' => 'commons.wikimedia.org',
                'path' => 'w'}});

die("Couldn't login to commons") if(!$OK2);
printf "Logged into commons as \"%s\"\n", $Commons->user(); 

# Temporary 
my $PicDir = "Pics";
mkdir $PicDir if ! -d $PicDir;

# Start by deciding what to do.  Ask this wiki page, which contains
# a list of categories which link to towns we want to render
my $Categories = GetCategories("Wikipedia:WikiProject OpenStreetMap/OJW list");

# Look through the categories
my $Count = 0;
foreach my $Category(@{$Categories})
{
  # Find the actual pages in that category
  my $Pages = PagesInCategory($Category);
  
  foreach my $Page(@{$Pages})
  {
    # Try to find a geotag on the wiki page
    my $Location = PageGeoLocation($Page);

    next if(!defined($Location->{lat}) or !defined($Location->{lon}));
    
    # Download an OSM map of the area
    my $Name = SuggestImageName($Page);
    my $Filename = "$PicDir/$Name";
    
    print "Downloading $Name\n";
    GetMapImage($Location, $Filename, 15, 1200);
    
    # Upload that map to wikimedia commons
    my $ImageName = "$Name";
    my $ImageData = read_file($Filename);
    my $ImageDescription = GetImageDescription($Page, $Location);
    
    printf "Uploading %s (%d bytes)\n", $ImageName, length($ImageData);
    $Commons->upload($ImageName, $ImageData, $ImageDescription, 1);
    
    # Add a description to the image page
    $Commons->{summary} = "Map metadata";
    $Commons->text("Image:$ImageName", $ImageDescription);
    
    # Put a message on the wikipedia talk page for that location,
    # saying that a map is available
    MessageToWikipedia($Page);
    
    exit if(++$Count > 3);
  }
}

exit;

sub MessageToWikipedia
{
  my $Page = shift();
  my $TalkPage = "Talk:$Page";
  
  my $ExistingTalkPage = $Wikipedia->text($TalkPage);
  
  if($ExistingTalkPage =~ m{open \s* street \s* map}ix)
  {
    print "$TalkPage already has an OSM discussion\n";
    return;
  }
  
  my $NewTalkPage = $ExistingTalkPage . "\n\n{{OpenStreetMap_render_available}}\n\n~~~~\n";
  
  $Wikipedia->{summary} = "Message about a free map of $Page becoming available";
  $Wikipedia->text($TalkPage, $NewTalkPage);
}

sub GetImageDescription
{
  my ($Page, $Location) = @_;
  return sprintf
    "{{openstreetmap_render|name=%s|lat=%f|lon=%f}}\n",
    $Page,
    $Location->{lat},
    $Location->{lon};
}

sub GetMapImage
{
  my ($Location, $Filename, $Zoom, $Width) = @_;

  my $URL = sprintf("http://%s/MapOf/?lat=%f&long=%f&z=%d&w=%d&h=%d&format=%s",
    "tah.openstreetmap.org",
    $Location->{lat},
    $Location->{lon},
    $Zoom,
    $Width,
    $Width,
    "png");
  
  getstore($URL, $Filename);
}

sub SuggestImageName
{
  my $Page = shift();
  $Page = "OpenStreetMap_render_$Page.png";
  return($Page);
}

sub PageGeoLocation
{
  my $Page = shift();
  my $Text = $Wikipedia->text($Page);
  
  my $Attr = {};
  while($Text =~ m{
    (latitude|longitude)
    \s*
    =
    \s*
    (
      [-+]?
      [0-9]*
      \.
      [0-9]+
    |
      [0-9]+
    )
    }xg)
    {
    $Attr->{substr($1,0,3)} = $2;
    }

  return $Attr;
}

sub PagesInCategory
{
  my $Category = shift();
  my ($Pages, $Categories) = $Wikipedia->readcat($Category);
  
  my $X;
  foreach my $Page(@{$Pages})
  {
    # Note: the readcat function returns a *lot* of junk.
    # Try to filter-out anything that obviously isn't an article
    # about a town or city
    if($Page =~ m{^(Category|Special):}){}
    elsif($Page =~ m{Wiki[pm]edia}i){}
    elsif($Page =~ m{accesskey}){}
    elsif($Page =~ m{(organization|501|Permanent link|Support us|Find background|About the)}){}
    else
    {
    push(@{$X}, $Page);
    }
  }
  return($X);
}

sub GetCategories
{
  my $Page = shift();
  my $Cat = [];
  foreach my $Line(split(/\n/, $Wikipedia->text($Page)))
  {
    # The "stuff to do" page just contains lines of the form:
    # * [[:Category:Some list of towns]]
    if($Line =~ m{
      \*
      \s*
      \[\[
      \:
      Category
      \:
      (.*)
      \]\]
      }xi)
      {
      push(@{$Cat}, $1);
      }
  }
  return($Cat);
}

