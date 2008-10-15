#!/usr/bin/perl -w

# This program generates the "oceantiles_12.dat" file as used by
# lowzoom.pl and close-areas.pl.
#
# It takes a 4096x4096 pixel PNG file as input; the pixels in the 
# PNG file may have one of the four colors
#
# white - coastline intersects with this tile
# green - no coastline intersect, land tile
# blue -  no coastline intersect, sea tile
# black - unknown

# written by Martijn van Oosterhout <kleptog@gmail.com>
# with minor changes by Frederik Ramm <frederik@remote.org>

use GD;
use strict;
use bytes;

use constant TILETYPE_UNKNOWN => 0;
use constant TILETYPE_LAND => 1;
use constant TILETYPE_SEA => 2;
use constant TILETYPE_TILE => 3;
my @typenames = ('unknown', 'land', 'sea', 'mixed');
my $pngname = "oceantiles_12.png";
my $datname = "oceantiles_12.dat";


#
#
#
sub get_type($$$)
{
  my($image, $x, $y) = @_;

  my($r,$g,$b) = $image->rgb( $image->getPixel( $x,$y ) );

  if($r == 0)
  {
    if($g == 0)
    {
      return $b == 255 ? TILETYPE_SEA : TILETYPE_UNKNOWN;
    }
    elsif($g == 255 && $b == 0)
    {
      return TILETYPE_LAND;
    }
  }
  elsif($r == 255 && $g == 255 && $b == 255)
  {
    return TILETYPE_TILE;
  }

  die "Weird tiletype at [$x,$y]: ($r,$g,$b)\n";
}


#
#
#
sub set_type($$$$)
{
  my($image, $x, $y, $type) = @_;
  my $color;

  if($type == TILETYPE_SEA)
  { $color = $image->colorResolve(0,0,255); }
  elsif($type == TILETYPE_LAND)
  { $color = $image->colorResolve(0,255,0); }
  elsif($type == TILETYPE_TILE)
  { $color = $image->colorResolve(255,255,255); }
  $image->setPixel($x,$y, $color);
}


#
#
#
sub convertfile($$)
{
  my ($image,$dat) = @_;
  my $world_im = getimage($image);
  my $tileinfo_fh;

  print STDERR "Writing output to $dat\n";

  my $str;
  for my $y (0..4095)
  {
    my $tmp = 0;
    for my $x (0 .. 4095)
    {
      $tmp = ($tmp << 2) | get_type($world_im,$x,$y);

      if(($x&3) == 3)
      {
        $str .= chr $tmp;
        $tmp = 0;
      }
    }
  }
  open $tileinfo_fh, ">:raw",$dat or die;
  print $tileinfo_fh $str;
  close $tileinfo_fh;
}


#
#
#
sub getimage($)
{
  my $world_fh;
  my $name = shift @_;

  open $world_fh, "<:raw",$name or die;
  my $world_im = GD::Image->newFromPng( $world_fh, 1 );
  close $world_fh;

  return $world_im;
}


#
#
#
sub saveimage($$)
{
  my ($image, $name) = @_;
  my $world_fh;

  open $world_fh, ">:raw",$name or die;
  print $world_fh $image->png;
  close $world_fh;
}


#
#
#
sub printhelp
{
  print "Usage: png2tileinfo.pl check <x> <y>\n"
  .     "       png2tileinfo.pl set <x> <y> [land|sea|mixed] ...\n"
  .     "       png2tileinfo.pl diff oldfile.png newfile.png\n"
  .     "       png2tileinfo.pl svndiff\n"
  .     "       png2tileinfo.pl view\n"
  .     "       png2tileinfo.pl copydiff oldfile.png newfile.png targetfile.png\n";
  exit(0);
}


#
#
#
if ($#ARGV > -1)
{
  my $arg = shift @ARGV;
  if ($arg eq "check")
  {
    printhelp() if (@ARGV < 2);

    my ($x, $y) = ($ARGV[0], $ARGV[1]);

    my $png_val = get_type(getimage($pngname), $x, $y);
    print "$pngname($x, $y) = $png_val ($typenames[$png_val])\n";
  }
  elsif ($arg eq "set")
  {
    printhelp() if (@ARGV < 1);

    my $changed;
    my $world_im = getimage($pngname);

    while(@ARGV)
    {
      printhelp() if (@ARGV < 3);

      my ($x, $y, $nt) = splice(@ARGV,0,3);
      my $newtype;

      my $old_val = get_type($world_im, $x, $y);
      if($nt eq "land") {$newtype = TILETYPE_LAND;}
      elsif ($nt eq "sea") {$newtype = TILETYPE_SEA;}
      elsif ($nt eq "mixed") {$newtype = TILETYPE_TILE;}
      else {die "Unknown type $nt.\n";}

      if($old_val == $newtype)
      {
        print "$pngname($x, $y) = $newtype ($typenames[$newtype]) UNCHANGED\n";
      }
      else
      {
        set_type($world_im, $x, $y, $newtype);
        print "$pngname($x, $y) = $newtype ($typenames[$newtype]) WAS $old_val ($typenames[$old_val])\n";
        $changed = 1;
      }
    }

    saveimage($world_im, $pngname) if $changed;
  }
  elsif($arg eq "diff" || $arg eq "svndiff")
  {
    my $oldfile;
    my $newfile;

    if($arg eq "svndiff")
    {
      ($oldfile,$newfile) = (".svn/text-base/$pngname.svn-base", $pngname);
    }
    elsif (@ARGV < 2)
    {
      printhelp();
    }
    else
    {
      ($oldfile,$newfile) = @ARGV;
    }

    my $world_im = getimage($oldfile);
    my $newworld_im = getimage($newfile);

    for my $y (0 .. 4095)
    {
      for my $x (0 .. 4095)
      {
        my $type = get_type($world_im,$x,$y);
        my $ntype = get_type($newworld_im,$x,$y);
        if ($ntype != $type)
        {
          my $ntypen=$typenames[$ntype];
          my $typen=$typenames[$type];
          print "$pngname($x, $y) = $ntype ($ntypen) WAS $type ($typen)\n";
        }
      }
    }
  }
  elsif($arg eq "view")
  {
    my $file = getimage($pngname);
    for my $y (0 .. 4095)
    {
      for my $x (0 .. 4095)
      {
        my $type = get_type($file,$x,$y);
        print "$pngname($x, $y) = $type ($typenames[$type])\n";
      }
    }
  }
  elsif($arg eq "copydiff")
  {
    printhelp() if (@ARGV < 3);
    my ($oldfile,$newfile,$targetfile) = @ARGV;

    my $world_im = getimage($oldfile);
    my $newworld_im = getimage($newfile);
    my $target_im = getimage($targetfile);
    my $changed;

    for my $y (0 .. 4095)
    {
      for my $x (0 .. 4095)
      {
        my $type = get_type($world_im,$x,$y);
        my $ntype = get_type($newworld_im,$x,$y);
        if ($ntype != $type)
        {
          my $ttype = get_type($target_im,$x,$y);
          my $ntypen=$typenames[$ntype];
          my $typen=$typenames[$type];
          my $typet=$typenames[$type];
          if($ntype == $ttype)
          {
            print "$pngname($x, $y) = $ntype ($ntypen) UNCHANGED\n";
          }
          else
          {
            print "$pngname($x, $y) = $ntype ($ntypen) WAS $type ($typen)/$ttype ($typet)\n";
            set_type($target_im, $x, $y, $ntype);
            $changed = 1;
          }
        }
      }
    }
    saveimage($target_im, $targetfile) if $changed;
  }
  else
  {
    printhelp();
  }
}
else
{
  convertfile($pngname, $datname);
}
