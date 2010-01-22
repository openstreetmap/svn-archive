#!/bin/bash -u

## poplargestfit function
# grabs the largest image that fits in a specified region and removes 
# it from the filelist
# NOTE: assumes that allicons list is sorted from tallest to shortest
function poplargestfit {
    # params: height width
    height=$1; width=$2

    # get row
    row=$(awk -F':' '$3 <= '$height' && $4 <= '$width' { print $0; exit }' \
	$allicons)
    if [ ! -z "$row" ]; then
        # remove row from filelist (if one found)
	id=$(echo $row | cut -d':' -f1)
	sed -i "/^$id/d" $allicons
        # echo row to stdout
    fi
    echo $row
}

## fillregion function
# recursively fills a specified region with images
function fillregion {
    # params: top left height width
    local top=$1; local left=$2; local height=$3; local width=$4

    # add the largest icon that will fit in the space in the top left
    local image=$(poplargestfit $height $width)
    local imfile=$(echo $image | cut -d':' -f2)
    local imheight=$(echo $image | cut -d':' -f3)
    local imwidth=$(echo $image | cut -d':' -f4)
    
    if [ ! -z "$imfile" ]; then
	echo ${imfile}:${left}:${top}:${imwidth}:${imheight}
	
        # fillregion the rest of the row (row height is imheight)
	fillregion $top $(($left+$imwidth)) $imheight $(($width-$imwidth))

        # fillregion the rest of the region (i.e. minus first row)
	fillregion $(($top+$imheight)) $left $(($height-$imheight)) $width
    fi

}

#collect the icons from here
ICONDIR="map-icons"

tmpdir=$(mktemp -d)
allicons=$tmpdir/all
sizeicons=$tmpdir/size
montagelist=$tmpdir/montages

name=icons

# Add PD Icons from openclipart.org
cp -a [^i0]*.png iconset.png $ICONDIR/classic.big
for n in [^i0]*.png iconset.png
do convert $n -resize 32x32\> $ICONDIR/classic.small/$n
done

# list all icons we want, sorting them by height then width
find $ICONDIR -iname '*.png' \
    -not -iwholename '*people*' \
    -not -iwholename '*wlan*' \
    -not -iwholename '*rendering*' \
    -not -iwholename '*svg*' \
    | xargs identify -format '%i:%h:%w\n' \
    | awk '{printf("%05d:%s\n",NR,$0)}' \
    | sort -r -t':' -k3 -n -k4 -n > $allicons

echo "Calculating icon distribution"

fillregion 0 0 1024 1024 > $name.csv

echo "Icon distribution list is in $name.csv ($(cat $allicons | wc -l) not included)"

echo "Creating $name.png"

convert -quality 0 -size 0x0 xc:black $name.png
for line in $(cat $name.csv); do
    convert $name.png \
	-page +$(echo $line | cut -d':' -f2)+$(echo $line | cut -d':' -f3) \
	"$(echo $line | cut -d':' -f1)" \
	-background None -mosaic -quality 0 $name.png
done
# The for loop can also be replaced with these 3 lines, which is slower for
# some reason:
#convert $name.png `
#  sed -e 's/\(.*\):\(.*\):\(.*\):.*:.*/ -page +\2+\3 \1/' <$name.csv` \
#  -background None -mosaic -quality 0 $name.png

echo "Flattening image (see $name-flatten.png)"
# convert to binary transparency through thresholding of alpha channel
convert $name.png -channel matte -separate +channel -negate \
    -threshold 25% -alpha off $tmpdir/mask.png
# add mask back to original image
composite -compose CopyOpacity $tmpdir/mask.png $name.png $tmpdir/binalpha.png
# set background for transparent regions and reduce colors
convert $tmpdir/binalpha.png -background black \
    -flatten -colors 256 -type palette \
    $name-flatten.png

echo "Converting to bmp and xpm formats"
# imagemagick's bmp conversion doesn't seem to work well for
# windows mobile resource files, so we will use netpbm here instead
# convert $name-flatten.png -type palette $name.bmp
pngtopnm $name-flatten.png | ppmtobmp > $name.bmp
pngtopnm $tmpdir/mask.png | pnminvert | ppmtobmp > $name-mask.bmp
# imagemagick works ok for xpm though
convert $tmpdir/binalpha.png $name.xpm

# Suppress the icons Ulf is using to highlight errors
echo 'map-icons/classic.big/misc/deprecated.png:0:0:1:1
map-icons/square.big/misc/deprecated.png:0:0:1:1
map-icons/classic.small/misc/deprecated.png:0:0:1:1
map-icons/square.small/misc/deprecated.png:0:0:1:1
map-icons/classic.big/misc/no_icon.png:0:0:1:1
map-icons/square.big/misc/no_icon.png:0:0:1:1
map-icons/classic.small/misc/no_icon.png:0:0:1:1
map-icons/square.small/misc/no_icon.png:0:0:1:1' >> $name.csv

rm -r $tmpdir


