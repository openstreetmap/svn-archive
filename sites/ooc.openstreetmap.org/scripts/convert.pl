#!/usr/bin/perl

use strict;
use warnings;

use File::Path;
use Imager;

our $SrcDir = "/tmp/npe";
our $DstDir = "/home/npe";
our @Tiles;

foreach my $x (30 .. 32)
{
    foreach my $y (18 .. 21)
    {
        process_tile(6, $x, $y);
    }
}

exit 0;

sub process_tile
{
    my $z = shift;
    my $x = shift;
    my $y = shift;

    if ($z < 14)
    {
        $Tiles[$z] = Imager->new(xsize => 256, ysize => 256);

        process_tile($z + 1, $x * 2 + 0, $y * 2 + 0);
        process_tile($z + 1, $x * 2 + 1, $y * 2 + 0);
        process_tile($z + 1, $x * 2 + 0, $y * 2 + 1);
        process_tile($z + 1, $x * 2 + 1, $y * 2 + 1);

        save_tile($Tiles[$z], $z, $x, $y);
    }
    elsif (-f "${SrcDir}/${z}/${x}/${y}.jpg")
    {
        my $tile = Imager->new;

        $tile->read(file => "${SrcDir}/${z}/${x}/${y}.jpg") || die $tile->errstr;

	for (my $dz = 1; $Tiles[$z - $dz]; $dz++)
        {
            my $f = 2 ** $dz;
            my $s = 256 / $f;

            $Tiles[$z - $dz]->paste(left => $s * ($x % $f),
                                    top => $s * ($y % $f),
                                    src => $tile->scale(xpixels => $s)) || die $Tiles[$z - $dz]->errstr;
        }

        foreach my $dz (1 .. 1)
        {
            my $f = 2 ** $dz;
            my $s = 256 / $f;

            foreach my $ix (0 .. $f - 1)
            {
                foreach my $iy (0 .. $f - 1)
                {
                    my $cropped = $tile->crop(left => $s * $ix,
                                              top => $s * $iy,
                                              width => $s, height => $s) || die $tile->errstr;
                    my $scaled = $cropped->scale(xpixels => 256) || die $cropped->errstr;

                    save_tile($scaled, $z + $dz, $x * $f + $ix, $y * $f + $iy);
                }
            }
        }

        save_tile($tile, $z, $x, $y);
    }

    return;
}

sub save_tile
{
    my $tile = shift;
    my $z = shift;
    my $x = shift;
    my $y = shift;

    mkpath("${DstDir}/${z}/${x}");

    print "Writing ${DstDir}/${z}/${x}/${y}.png\n";

    my $paletted = $tile->to_paletted({ max_colors => 256 }) || die $tile->errstr;

    open(OUT, "| pnmtopng > ${DstDir}/${z}/${x}/${y}.png") || die "$!";

    $paletted->write(fh => \*OUT, type => "pnm") || die $tile->errstr;

    close(OUT);

    return;
}
