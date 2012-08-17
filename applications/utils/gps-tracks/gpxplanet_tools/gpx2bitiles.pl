#!/usr/bin/perl

# This script receives CSV file of "lat,lon" and makes a lot of bitiles.
# A bitile is a 8k bit array. Those should be converted to PNG images with bitiles2png.pl
# Written by Ilya Zverev, licensed WTFPL.

use strict;
use Math::Trig;
use File::Path 2.07 qw(make_path);
use File::Basename;
use Bit::Vector;
use Fcntl qw( SEEK_SET O_RDWR O_BINARY O_CREAT );
use Getopt::Long;

my $factor = 10**7; # number by which all coordinates in source CSV are multiplied

my $tile_folder;
my $zoom;
my $bbox_str = '-180,-85,180,85';

my $help;
my $verbose;
my $infile = '-';
my $thread_str;
my $state_file = 'gpx2bitiles.state';

GetOptions('h|help' => \$help,
           'o|output=s' => \$tile_folder,
           'z|zoom=i' => \$zoom,
           'v|verbose' => \$verbose,
           'i|input=s' => \$infile,
           'b|bbox=s' => \$bbox_str,
           't|thread=s' => \$thread_str,
           ) || usage();

if( $help ) {
  usage();
}

usage("Please specify output directory with -o") unless defined($tile_folder);
usage("Please specify zoom level with -z") unless defined($zoom);

my @bbox = split(",", $bbox_str);
die("badly formed bounding box - use four comma-separated values for ".
   "bottom left latitude, bottom left longitude, top right latitude, ".
   "top right longitude") unless $#bbox == 3;
die("max longitude is less than min longitude") if ($bbox[2] < $bbox[0]);
die("max latitude is less than min latitude") if ($bbox[3] < $bbox[1]);

my $zoom2 = 2**$zoom;
my $xmintile = 0;
my $xmaxtile = 2**18;
my $thread = '';

if( defined($thread_str) ) {
  die("Badly formed thread string: it must be THREAD,TOTAL") unless $thread_str =~ /^(\d+),(\d+)$/;
  $thread = $1;
  my $total = $2;
  die("Total number of threads must be no more than 16") if $total <1 || $total > 16;
  die("Thread number is higher than number of threads") if $thread > $total || $thread < 1;

  my $eps = 10**-8;
  my $bxmin = int(($bbox[0]+$eps+180)/360 * $zoom2);
  my $bxmax = int(($bbox[2]-$eps+180)/360 * $zoom2);
  my $bxwidth = $bxmax - $bxmin + 1;
  die("Total number of threads is higher than horizontal number of tiles") if $total > $bxwidth;
  my $width = int(($bxwidth + $total - 1) / $total);
  $xmintile = ($thread-1) * $width;
  $xmaxtile = $xmintile + $width - 1;
  # print STDERR "Thread $thread: width=$width (of $bxwidth, $bxmin..$bxmax), $xmintile..$xmaxtile\n"; exit;
}

my $count = 0;
if( open(STATE, "<$state_file$thread") ) {
  my $line = <STATE>;
  close STATE;
  $count = -$1 if $line =~ /^\d+/;
}

open CSV, "<$infile" or die "Cannot open $infile: $!\n";
while(<CSV>) {
  $count++;
  next if $count <= 0;
  if( $count % 10000 == 0 && open(STATE, ">$state_file$thread") ) {
    print STDERR '.' if $verbose;
    print STATE $count-1;
    close STATE;
  }

  next if !/^(-?\d+(?:\.\d+)?),\s*(-?\d+(?:\.\d+)?)/;
  my $lat = $1 / $factor;
  my $lon = $2 / $factor;
  next if $lat <= $bbox[1] || $lat >= $bbox[3] || $lon <= $bbox[0] || $lon >= $bbox[2];

  my $x = ($lon+180)/360 * $zoom2;
  my $y = (1 - log(tan(deg2rad($lat)) + sec(deg2rad($lat)))/pi)/2 * $zoom2;
  my $xtile = int($x);
  my $ytile = int($y);
  next if $xtile < $xmintile || $xtile > $xmaxtile;
  my $xpix = int(256 * ($x - $xtile));
  my $ypix = int(256 * ($y - $ytile));
  my $folder = "$tile_folder/$zoom/$xtile";
  make_path($folder);
  set_pixel("$folder/$ytile.bitile", $xpix, $ypix);
}
close CSV;

sub set_pixel {
  my($filename, $x, $y) = @_;
  my $vec = Bit::Vector->new(65536);
  my $read;

  print STDERR "Error opening tile $filename\n" unless sysopen(BITILE, $filename, O_RDWR | O_CREAT | O_BINARY);
  if( sysread(BITILE, $read, 8192) == 8192 ) {
      $vec->Block_Store($read);
  }

  if( !$vec->bit_test($y * 256 + $x) ) {
    $vec->Bit_On($y * 256 + $x);
    sysseek(BITILE, 0, SEEK_SET);
    syswrite(BITILE, $vec->Block_Read());
  }
  close BITILE;
}

sub usage {
    my ($msg) = @_;
    print STDERR "$msg\n\n" if defined($msg);

    my $prog = basename($0);
    print STDERR << "EOF";
This script receives CSV file of "lat,lon" and makes a lot of bitiles.
A bitile is a 8k bit array, which can be converted to regular PNG tile
with bitiles2png.pl

usage: $prog [-h] [-v] [-i file] -o target -z zoom [-b bbox]

 -h        : print ths help message and exit.
 -i file   : CSV points file to process (default is STDIN).
 -o target : directory to store bitiles.
 -z zoom   : generated tiles zoom level.
 -b bbox   : limit points by bounding box (four comma-separated
             numbers: minlon,minlat,maxlon,maxlat).
 -t n,t    : process Nth part of T jobs.
 -v        : print a dot every 10000 lines.

All coordinates in source CSV file should be multiplied by $factor
(you can change this number in the code).

EOF
    exit;
}