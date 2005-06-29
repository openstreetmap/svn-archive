#!/bin/bash
# Script to generate SRTM contour tiles within a certain area

starte=$1
ende=$2
startn=$3
endn=$4
scale=$5
mode=$6
step=$7

if [ $mode -le 7 ]; then
	hs='-r 1'
fi

n=$startn

while [ $n -le $endn ]; do
	n1=`printf %02d $n`
	e=$starte
	while [ $e -le $ende ]; do
		e1=`printf %02d $e`
		echo ${e1}0000 ${n1}0000 
		./srtm -e ${e1}0000 -n ${n1}0000 -s $scale -w 1000 -h 1000 ${hs} > data/tiles/mode${mode}/${e1}00${n1}00.png
		let e=e+${step}
	done
	let n=n+${step}
done
