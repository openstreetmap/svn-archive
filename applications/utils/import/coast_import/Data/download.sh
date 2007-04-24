#! /bin/sh

for file in $(cat list.txt)
do
    wget -N -c ${file}
done

