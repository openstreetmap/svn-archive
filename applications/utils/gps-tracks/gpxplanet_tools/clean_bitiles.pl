#!/usr/bin/perl

# Remove salt-n-pepper noise from bitiles.
# Written by Ilya Zverev, licensed WTFPL.

use strict;
use Bit::Vector;
use File::Path 2.07 qw(make_path);
use File::Basename;
use Getopt::Long;
use Fcntl qw( O_RDONLY O_RDWR O_CREAT O_BINARY );

my $source_dir;
my $dest_dir;
my $help;
my $verbose;
my $maxpixels = 64;
my $threshold;
my $thresholdpx;
my $threshold_m = 200;

GetOptions('h|help' => \$help,
           'v|verbose' => \$verbose,
           'i|input=s' => \$source_dir,
           'o|output=s' => \$dest_dir,
           'p|pixels=i' => \$maxpixels,
           't|threshold=i' => \$threshold_m,
           'x|thresholdpx=i' => \$thresholdpx,
           ) || usage();

if( $help ) {
  usage();
}

usage("Please specify input directory with -i") unless defined($source_dir);
die "Source directory not found" unless -d $source_dir;
# no default because this operation is irreversible
usage("Please specify output directory with -o") unless defined($dest_dir);

my $dh;
opendir($dh, "$source_dir") or die "Fail: cannot open $source_dir";
my @zlist = grep { /^\d+$/ && -d "$source_dir/$_" } readdir($dh);
closedir($dh);
die "No zoom level directories found in $source_dir" if $#zlist < 0;

for my $z (@zlist) {
  opendir($dh, "$source_dir/$z") or next;
  my @xlist = grep { /^\d+$/ && -d "$source_dir/$z/$_" } readdir($dh);
  closedir($dh);
  next if $#xlist < 0;
  
  if( $thresholdpx ) {
    $threshold = $thresholdpx;
  } else {
    my $resolution = 156543.04 * 0.5 / (2**$z); # meters/pixel at 60 degrees lat
    $threshold = int($threshold_m / $resolution + 0.5);
  }
  $threshold = 64 if $threshold > 64;
  $threshold = 2 if $threshold < 2;

  print STDERR "Processing zoom $z, threshold is $threshold pixels" if $verbose;

  for my $x (@xlist) {
    my $folder = "$source_dir/$z/$x";
    opendir($dh, $folder) or next;
    my @ylist = grep { /^\d+\.bitile$/ && -r "$folder/$_" } readdir($dh);
    closedir($dh);
    print STDERR '.' if $verbose;

    for my $filename (@ylist) {
      $filename =~ /^(\d+)/;
      clean_tile($z, $x, $1);
    }
  }
  print STDERR "Done\n" if $verbose;
}

sub clean_tile {
  my ($z, $x, $y) = @_;
  my $vec = read_bit_vector("$source_dir/$z/$x/$y.bitile");
  if( $vec->Norm() <= $maxpixels ) {
    my $vec_top = read_bit_vector("$source_dir/$z/$x/".($y-1).".bitile");
    my $vec_bottom = read_bit_vector("$source_dir/$z/$x/".($y+1).".bitile");
    my $vec_left = read_bit_vector("$source_dir/$z/".($x-1)."/$y.bitile");
    my $vec_right = read_bit_vector("$source_dir/$z/".($x+1)."/$y.bitile");
    my @pixels_to_remove = ();
    for my $pixel ($vec->Index_List_Read()) {
      my $px = $pixel % 256;
      my $py = int($pixel / 256);
      my $value = 0;
      TEST: for my $tx ($px-$threshold..$px+$threshold) {
        for my $ty ($py-$threshold..$py+$threshold) {
          next if $tx == $px && $ty == $py;
          if( $tx < 0 && $ty >= 0 && $ty < 256 ) {
            $value = $vec_left->bit_test($ty * 256 + $tx + 256);
          } elsif( $tx >= 0 && $tx < 256 ) {
            if( $ty < 0 ) {
              $value = $vec_top->bit_test(($ty + 256) * 256 + $tx);
            } elsif( $ty < 256 ) {
              $value = $vec->bit_test($ty * 256 + $tx);
            } else {
              $value = $vec_bottom->bit_test(($ty - 256) * 256 + $tx);
            }
          } elsif( $tx > 255 && $ty >= 0 && $ty < 256 ) {
            $value = $vec_right->bit_test($ty * 256 + $tx - 256);
          }
          last TEST if $value;
        }
      }
      push @pixels_to_remove, $pixel if !$value;
    }
    $vec->Index_List_Remove(@pixels_to_remove);
  }
  return if !$vec->Norm();
  make_path("$dest_dir/$z/$x");
  sysopen(BITILE, "$dest_dir/$z/$x/$y.bitile", O_RDWR | O_CREAT | O_BINARY);
  syswrite(BITILE, $vec->Block_Read());
  close BITILE;
}

sub read_bit_vector {
    my $filename = shift;
    my $vec = Bit::Vector->new(65536);
    if( sysopen(BITILE, $filename, O_RDONLY | O_BINARY) ) {
        if( sysread(BITILE, my $read, 8192) == 8192 ) {
            $vec->Block_Store($read);
        }
        close BITILE;
    }
    return $vec;
}

sub usage {
    my ($msg) = @_;
    print STDERR "$msg\n\n" if defined($msg);

    my $prog = basename($0);
    print STDERR << "EOF";
This script traverses input directory and cleans all bitiles in it
from salt-and-pepper noise.

usage: $prog [-h] [-v] -i source -o target [-p pixels] [-t threshold]

 -h           : print ths help message and exit.
 -i source    : directory with bitiles.
 -o target    : directory to store processed bitiles.
 -p pixels    : maximal number of pixels to start cleaning procedure ($maxpixels).
 -t threshold : radius of an empty area around a point to remove it
                (in meters at 60 degrees latitude, default is $threshold_m).
 -x threshold : the same radius, but in pixels.
 -v           : display progress.

EOF
    exit;
}
