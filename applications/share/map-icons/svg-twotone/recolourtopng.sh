#!/bin/bash

# $1 input filename
# $2 background fill
# $3 background stroke
# $4 forground
# $5 png size
# $6 output filename

./recolour.sh $1 $2 $3 $4 | rsvg -f png -w ${5} -h ${5} /dev/stdin ${6}.png
