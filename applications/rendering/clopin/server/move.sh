blankTile="67"
mkdir $1
mkdir $1/$2
for ((x=$3;x<=$4;x++)) do
mkdir $1/$2/$x
for ((y=$5;y<=$6;y++)) do
echo $x $y
foo="${1}/clopin_$2_${x}_${y}.png"
echo $foo
fileSize=$(stat -c%s $foo)
echo $fileSize
if [ "$fileSize" = "$blankTile" ]; then
rm $foo
rm $1/$2/$x/$y.png
else
mv $foo $1/$2/$x/$y.png
fi
done
done
