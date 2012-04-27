#!/usr/bin/perl

use GD;
use strict;
my $WIDTH=800;
my $HEIGHT=600;
my $MARGIN_TOP=25;
my $MARGIN_BOT=25;
my $MARGIN_LEFT=340;
my $MARGIN_RIGHT=30;
my $OUTPUT_PATH="out/";
my $LINE_HEIGHT=36;
my $SIZE=20;
my $FONT="/usr/share/fonts/dejavu/DejaVuSans.ttf";


open(L,"list") or die;

my $count = 1;
while(<L>)
{
    /^(.*?):(.*)/;
    print;
    my ($file, $desc) = ($1,$2);
    my $source = GD::Image->new($file) or next;
    my $dest = GD::Image->newTrueColor($WIDTH,$HEIGHT);
    my $white = $dest->colorAllocate(168,168,168);
    my $black= $dest->colorAllocate(0,0,0);
    $dest->filledRectangle(0,0,$MARGIN_LEFT-$MARGIN_RIGHT,$HEIGHT,$white);
    my ($sw, $sh) = $source->getBounds();

    my $avw = $WIDTH-$MARGIN_LEFT-$MARGIN_RIGHT;
    my $avh = $HEIGHT-$MARGIN_TOP-$MARGIN_BOT;
    my $scw = $avw / $sw;
    my $sch = $avh / $sh;
    my $sc = $scw; 
    $sc = $sch if ($sch < $sc);
    my $neww = $sw * $sc;
    my $newh = $sh * $sc;
    my $x = $MARGIN_LEFT + ($avw-$neww)/2;
    my $y = $MARGIN_TOP + ($avh-$newh)/2;
    $dest->copyResampled($source,$x, $y, 0, 0, $neww, $newh, $sw, $sh);
    
    my $textx = $MARGIN_RIGHT;
    my $texty = $MARGIN_TOP + $LINE_HEIGHT;
    my $text = $desc;
    my $avw=$MARGIN_LEFT-2.5*$MARGIN_RIGHT;
    while(length($text))
    {
        my $to=length($text);
        my $firstspc = index($text," ");
        my $firstdash = index($text," ");
        my $firstsep = $firstspc; 
        $firstsep = $firstdash if ($firstsep>$firstdash);
        while(1)
        {
            my @bounds = GD::Image->stringFT($black,$FONT,$SIZE,0,100,100,substr($text,0,$to));
            my $tw = $bounds[2]-$bounds[0];
            if ($tw > $avw)
            {
                my $newto = int($to*($avw/$tw)*1.2);
                $newto = $to-1 if ($newto>=$to);
                while($newto>0 && substr($text,$newto,1) !~ /[ -]/)
                {
                    $newto--;
                }
                $to = $newto;
                if ($newto==0)
                {
                    $to=$firstsep;
                    last;
                }
            }
            else
            {
                last;
            }
        }
        #last if ($to==0);
        $to++;
        my @bounds = $dest->stringFT($black,$FONT,$SIZE,0,$textx,$texty,substr($text,0,$to));
        $text=substr($text,$to);
        $texty+=$LINE_HEIGHT;
    }

    open(O,sprintf(">%s/%03d.png", $OUTPUT_PATH, $count++)) or die;
    print O $dest->png;
    close(O);
}
close(L);

