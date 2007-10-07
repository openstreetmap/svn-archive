#-----------------------------------------------------------------
# Downloads photos showing a real-life example of each OpenStreetmap
# tag
#-----------------------------------------------------------------
# Usage: perl getPhotos.pl
# Will create an ./html/photos directory and fill it with JPEG images
# Uses input from http://wiki.openstreetmap.org/index.php/Tagwatch/Photos
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
use MediaWiki;
use LWP::Simple;
use GD;

my $c = MediaWiki->new;
$c->setup({'wiki' => {
            'host' => 'wiki.openstreetmap.org',
            'path' => ''}});

my $PDir = "photos";
mkdir $PDir if ! -d $PDir;
foreach my $Line(split(/\n/, $c->text("Tagwatch/Photos"))){
  if($Line =~ m{^\*\s*(\w+)=(\w+)\s+(.*?)\s*$}){
    my $Filename = "$PDir/$1_$2.jpg";
    print "Getting $3\n";
    my $Data = get($3);
    
    my $Image1 = GD::Image->newFromJpegData($Data);

    my $WO = $Image1->width;
    my $W = 200;
    my $Ratio = $W / $WO;
    my $HO = $Image1->height;
    my $H = $HO * $Ratio;
    print "Creating $W x $H\nFrom $WO x $HO\n";
    my $Image2 = new GD::Image($W,$H);
    $Image2->copyResampled($Image1,0,0,0,0,$W,$H,$WO,$HO);
    open(IMOUT, ">$Filename") || die;
    binmode IMOUT;
    print IMOUT $Image2->jpeg();
    close IMOUT;

    print "$Filename\n";
  }
}
