./generate_xml.py osm-parking-src.xml osm-parking.xml --host localhost --user `whoami` --dbname gis --symbols ./symbols/ --world_boundaries ./world_boundaries/ --port 5432 --password 'kay' 
./generate_xml.py osm-parking-bw-src.xml osm-parking-bw.xml --host localhost --user `whoami` --dbname gis --symbols ./symbols/ --world_boundaries ./world_boundaries/ --port 5432 --password 'kay' 
./generate_xml.py osm-parktrans-src.xml osm-parktrans.xml --host localhost --user `whoami` --dbname gis --symbols ./symbols/ --world_boundaries ./world_boundaries/ --port 5432 --password 'kay' 
./generate_xml.py osm-parkerr-src.xml osm-parkerr.xml --host localhost --user `whoami` --dbname gis --symbols ./symbols/ --world_boundaries ./world_boundaries/ --port 5432 --password 'kay' 
