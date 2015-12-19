WMS/TMS service for Slovenia Landcover Import - RABA-KGZ
https://wiki.openstreetmap.org/wiki/Slovenia_Landcover_Import_-_RABA-KGZ

Reproject (from wgs84) to EPSG4326:
ogr2ogr -t_srs "EPSG:4326" RABA_20150331_EPSG4326 RABA_20150331_84 -nln RABA_20150331_EPSG4326

or directly from MKGP source using GeoCoordinateConverter (https://github.com/mrihtar/GeoCoordinateConverter ):
gk-shp -t 9 -dd RABA_20151130.shp RABA_20151130_EPSG4326.shp

Prepare the lightweight extract of built-up areas only for faster rendering:
ogr2ogr RABA3000_20150331_EPSG4326 RABA_20150331_EPSG4326 -where "RABA_ID=3000" -nln RABA3000_20150331_EPSG4326
ogr2ogr RABA3000_20151130_EPSG4326 RABA_20151130_EPSG4326 -where "RABA_ID=3000" -nln RABA3000_20151130_EPSG4326
