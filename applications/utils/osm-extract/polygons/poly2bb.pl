#!/usr/bin/perl

# script to extract a polygon file's bbox
# output is in a form suitable for the Osmosis "--bb" task.

# written by Frederik Ramm <frederik@remote.org>, public domain.

$maxx = -360;
$maxy = -360;
$minx = 360;
$miny = 360;

while(<>)
{
   if (/^\s+([0-9.E+-]+)\s+([0-9.E+-]+)\s*$/)
   {
       my ($x, $y) = ($1, $2);
       $maxx = $x if ($x>$maxx);
       $maxy = $y if ($y>$maxy);
       $minx = $x if ($x<$minx);
       $miny = $y if ($y<$miny);
   }
}

printf "left=%f right=%f top=%f bottom=%f\n",
   $minx, $maxx, $maxy, $miny;
