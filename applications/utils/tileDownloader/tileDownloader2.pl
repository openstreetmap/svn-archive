#!/usr/bin/perl
use LWP::UserAgent;
use File::Copy;
use FindBin qw($Bin);
use English '-no_match_vars';
use GD qw(:DEFAULT :cmp);
use utility_config;
use strict;
#-----------------------------------------------------------------------------
# OpenStreetMap tiles@home
#
# Contact OJW on the Openstreetmap wiki for help using this program
#-----------------------------------------------------------------------------
# Copyright 2006, Oliver White, Etienne Cherdlu, Dirk-Lueder Kreie,
# and others
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
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-----------------------------------------------------------------------------

# Get version number from version-control system, as integer

my $lastmsglen = 0;


# hash for MagicMkdir
my %madeDir;


# check GD
eval GD::Image->trueColor(1);
if ($@ ne '') {
  print STDERR "please update your libgd to version 2 for TrueColor support";
  exit(3);
}

# Setup GD options
# currently unused (GD 2 truecolor mode)
#
#   my $numcolors = 256; # 256 is maximum for paletted output and should be used
#   my $dither = 0; # dithering on or off.
#
# dithering off should try to find a good palette, looks ugly on 
# neighboring tiles with different map features as the "optimal" 
# palette is chosen differently for different tiles.

# Handle the command-line

my %Config;

  my  $arg = shift();
  if ($arg=="conf")
  {
     my $cfgfile= shift();
     
     %Config = ReadConfig($cfgfile);
     print "Using config file  $cfgfile\n";
  }
  else
  {

    $Config{"Layers"} = 'tiles,names';
    $Config{"Layer.tiles.URL"} = 'http://www.freemap.sk/layers/tiles/%d/%d/%d.png';
    $Config{"Layer.names.URL"} = 'http://www.freemap.sk/layers/names/%d/%d/%d.png';
    $Config{X} = $arg;
    $Config{Y} = shift();
    $Config{Zoom} = shift();
    $Config{Zoom2} = shift();
    $Config{W} = shift();
    $Config{H} = shift();

    $Config{R} = shift();

    if( defined $Config{R})
    {
		$Config{G} = shift();
		$Config{B} = shift();
    }
  }


my $totalTiles=0;
my $doneTiles=0;

my $k;

   for ($k=$Config{Zoom};$k<=$Config{Zoom2}; $k++)
   {
    $totalTiles += 4**($k-$Config{Zoom});
   }
    
    $totalTiles = $Config{W}*$Config{H}*$totalTiles;
    GenerateMap();


#-----------------------------------------------------------------------------
# Render a tile (and all subtiles, down to a certain depth)
#-----------------------------------------------------------------------------
sub GenerateMap
{
  my $i;
  my $j;
  
  my $URL;
  my $ImageFile;

  for ($i=0; $i<$Config{W}; $i++)
      {
      for ($j=0; $j<$Config{H}; $j++)
          {
           DownloadTile( $Config{X}+$i, $Config{Y}+$j, $Config{Zoom}, $Config{Zoom2});
          }
      }
  }

sub DownloadTile()
{
  my ($X, $Y, $Zoom, $Zoom2) = @_;

  $doneTiles++;
  my $Map = new GD::Image(256, 256);

  my $MapBg;
  if( defined $Config{R}  )
  {
	$MapBg = $Map->colorAllocate($Config{R},$Config{G},$Config{B});
  }
  else
  {
	$MapBg = $Map->colorAllocate(248,248,248);
	$Map->transparent($MapBg);
  }

  $Map->fill(127,127,$MapBg);

  my $URL;
  my $ImageFile;

      foreach my $layer(split(/,/, $Config{Layers}))
      {
          # stiahnut tile
          $URL = sprintf($Config{"Layer.$layer.URL"},$Zoom, $X, $Y);
          $ImageFile= sprintf("%d_%d_%d.%s", $Zoom, $X, $Y,$Config{"Layer.$layer.type"});

          DownloadFile($URL,$ImageFile, 0 );

          #pridat na spravne miesto

          if (-f $ImageFile)
          {
             if (-s $ImageFile >128)
             {

                my $SubImage;
                if ($Config{"Layer.$layer.type"} eq "png")
                {
                #printf($Config{"Layer.$layer.type"}."PNG\n");
                $SubImage = newFromPng GD::Image($ImageFile);
                }
                
                if ($Config{"Layer.$layer.type"} eq "jpg")
                {
                #printf($Config{"Layer.$layer.type"}."JPEG\n");
                $SubImage = newFromJpeg GD::Image($ImageFile);
                }

                #GD::Image::copy(destination, source, dstX, dstY, srcX, srcY, w, h)
                GD::Image::copy($Map, $SubImage, 0, 0, 0,0,256,256);
                undef $SubImage;
              }
           
              killafile($ImageFile);
           }
       }

       StoreTile ($X, $Y,$Zoom,$Map);
       if ($Zoom < $Zoom2)
       {
       DownloadTile( $X*2, $Y*2, $Zoom+1, $Zoom2);
       DownloadTile( $X*2, $Y*2+1, $Zoom+1, $Zoom2);
       DownloadTile( $X*2+1, $Y*2, $Zoom+1, $Zoom2);
       DownloadTile( $X*2+1, $Y*2+1, $Zoom+1, $Zoom2);
       }
       
       
}


sub StoreTile()
{
  my ($X, $Y, $Zoom, $Map) = @_;

  my $FinalFile;
  $FinalFile= sprintf("data/%d/%d/%d.png", $Zoom, $X, $Y);
  MagicMkdir ($FinalFile);
  
  
 if ($Config{PNGCrush}==1)
{
    my $FinalFile2= sprintf("data/%d/%d/%d-temp.png", $Zoom, $X, $Y);
    WriteImagePNG ($Map, $FinalFile2);

     my $Cmd = sprintf("pngcrush.exe -q %s %s > $PID.stderr",
      $FinalFile2,
      $FinalFile);

     my $retval = system($Cmd);
     $retval = ($retval<0) ? 0 : ($retval>>8) ? 0 : 1;

      if($retval)
      {
        unlink($FinalFile2);
        killafile("$PID.stderr");
        #print "crushed to $FinalFile\n";
      }
      else
      {
        rename($FinalFile2, $FinalFile);
      }

  }
  else
  {
  WriteImagePNG ($Map, $FinalFile);
  }

}

sub MagicMkdir
{
    my ($file) = @_;
    my @paths = split("/", $file);
    pop(@paths);
    my $dir = ".";
    foreach my $path(@paths)
    {
        $dir .= "/".$path;
        if (!defined($madeDir{$dir}))
        {
            mkdir $dir;
            $madeDir{$dir}=1;
        }
    }
}



#-----------------------------------------------------------------------------
# Delete a file if it exists
#-----------------------------------------------------------------------------
sub killafile{
  my $file = shift();
  if(-f $file)
  {
  unlink ($file);
  };

}



#-----------------------------------------------------------------------------
# 
#-----------------------------------------------------------------------------
sub DownloadFile 
{
  my ($URL, $File, $UseExisting) = @_;
  
  statusMessage ( "Downloading file $URL",0);
    my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 240);
    $ua->agent("FreemapTileDownloader");
    #$ua->env_proxy();

    if(!$UseExisting) 
  {
    killafile($File);
  }
  $ua->mirror($URL, $File);
  
}

sub statusMessage
{
    my ($msg, $newline) = @_;


    
    my $toprint = sprintf("[%3d%%]%s%s ", $doneTiles*100/$totalTiles , $msg, ($newline) ? "" : "...");
    my $curmsglen = length($toprint);
    print STDERR "\r$toprint";
    print STDERR " " x ($lastmsglen-$curmsglen);
    if ($newline)
    {
        $lastmsglen = 0;
        print STDERR "\n";
    }
    else
    {
        $lastmsglen = $curmsglen;
    }

}



#-----------------------------------------------------------------------------
# Write a GD image to disk
#-----------------------------------------------------------------------------
sub WriteImagePNG {
  my ($Image, $Filename) = @_;
  
  # Get the image as PNG data
  my $png_data = $Image->png;
  
  # Store it
  open (my $fp, ">$Filename") || die;
  binmode $fp;
  print $fp $png_data;
  close $fp;
}
