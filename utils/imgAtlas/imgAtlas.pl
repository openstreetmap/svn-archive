#!/usr/bin/perl
#-----------------------------------------------------------------------------
# Currently just generic image-to-PDF stuff
#
# Copyright 2007
#  * Oliver White
# 
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
use PDF::API2;
use constant mm => 25.4 / 72;
my $Usage = "$0 [config file] [output pdf filename]\n";
my $Config = shift() || "config.txt";
my $Filename = shift() || "atlas.pdf";

open(COMMANDS, '<', $Config) || die($Usage);
my $PDF = PDF::API2->new();
my ($Page, @Attributions);
my $PageNum = 1;

newPage();
my $Font = $PDF->corefont('Helvetica');

  
while(my $Line = <COMMANDS>){
  $Line =~ s{     # replace:
    ^             # at start of line
    \s*           #
    \#            # Comment characters
    .*            # ...
    $             # to end of line
    }{}x;         # with nothing

  if($Line =~ m{==page==}i){
    $Page = $PDF->page;
    $PageNum++;
  }
  elsif($Line =~ m{
    text:               # command
    \s*                 #
    (.*?)               # text
    \s*                 #
    \(                  # (
    (.*?), \s*          #  x
    (.*?), \s*          #  y
    (.*?)               #  size
    \)                  # )
    }x){
    print "Adding \"$1\" at $2, $3, size $4\n";
    textAt($1,$2,$3,$4);
  }
  if($Line =~ m{
    attribution:        # command
    \s*                 #
    \(                  # (
    (.*?), \s*          #  x
    (.*?), \s*          #  y
    (.*?)               #  size
    \)                  # )
    }x){
    printf "Adding attribution block\n";
    my $X = $1;
    my $Y = $2;
    my $Size = $3;
    
    # Add a title, just as an extra line of text at the beginning of the textblock
    unshift(@Attributions, "Image credits:");
    
    # Draw each line of text (TODO: make a generic "write array of text lines")
    foreach my $Line (@Attributions){
      textAt($Line, $X, $Y, $Size);
      $Y -= $Size * 1.25;
    }
  }
  elsif($Line =~ m{
    image:             # Command
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
  else{
    #print "Misunderstood $Line\n";
  }
  
  
}
$PDF->saveas($Filename);

sub textAt{
  my ($Text, $X, $Y, $Size) = @_;
  
  my $TextHandler = $Page->text;
  $TextHandler->font($Font, $Size/mm );
  $TextHandler->fillcolor('black');
  $TextHandler->translate($X/mm, $Y/mm);
  $TextHandler->text($Text);
  
}
sub newPage{
  # A4 page
  $Page = $PDF->page;
  $Page->mediabox(210/mm, 297/mm);
  $Page->cropbox (10/mm, 10/mm, 200/mm, 287/mm);
}