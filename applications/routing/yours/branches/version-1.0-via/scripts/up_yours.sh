#!/bin/bash
planet=/home/lambertus/planet.openstreetmap.org/planet-latest.osm.bz2
eurasia=/home/lambertus/planet.openstreetmap.org/eurasia.osm.gz
america=/home/lambertus/planet.openstreetmap.org/america.osm.gz

nice bzcat $planet | nice osmosis --rx file="-" enableDateParsing="no" --t outputCount=2 --bb left=-30 right=-168 idTrackerType="BitSet" --wx file=$eurasia --bb left=-168 right=-30 idTrackerType="BitSet" --wx file=$america 

echo "Normal: Eurasia"
cd normal/build/eurasia
nice gunzip -c $eurasia | nice ./gosmore rebuild
#nice cat $eurasia | nice ./gosmore rebuild
#touch ../../eurasia/.update
#cp gosmore ../../eurasia
#cp icons.csv ../../eurasia
#cp elemstyles.xml ../../eurasia
mv gosmore.pak ../../eurasia.pak
#rm ../../eurasia/.update
cd ../../../

echo "Normal: America"
cd normal/build/america
nice gunzip -c $america | nice ./gosmore rebuild
#nice cat $america | nice ./gosmore rebuild
#touch ../../america/.update
#cp gosmore ../../america
#cp icons.csv ../../america
#cp elemstyles.xml ../../america
mv gosmore.pak ../../america.pak
#rm ../../america/.update
cd ../../../

#echo "Cycle"
#cd cycle/build
#./build.sh
#cd ../../

stat -c %y $planet | cut -d' ' -f1 > /home/lambertus/yours/planet-date.txt

#echo "Test"
#cd test/build
#./build.sh
#cd ../../

