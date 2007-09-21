level=$2
maxLevel=17
startx=$3
endx=$4
starty=$5
endy=$6
sh move.sh $1 $level $startx $endx $starty $endy
if [ "$level" = "$maxLevel" ]; then
echo done
else
newlevel=$[${level}+1]
newstartx=$[${startx} * 2]
newstarty=$[${starty}*2]
newendx=$[(${endx}*2)+1]
newendy=$[(${endy}*2)+1]
echo $1 $newlevel $newstartx $newendx $newstarty $newendy
sh moveall.sh $1 $newlevel $newstartx $newendx $newstarty $newendy
fi
