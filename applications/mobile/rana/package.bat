del /q /s rana
del rana.tar.gz
mkdir rana
mkdir rana\modules
copy modules\*.py rana\modules
rm rana\modules\mod_config.py
rm rana\modules\mod_mapData.py
rm rana\modules\mod_mapTiles.py
rm rana\modules\mod_route.py
move rana\modules\example_config.py rana\modules\mod_config.py
mkdir rana\icons
mkdir rana\icons\bitmap
copy icons\bitmap\*.* rana\icons\bitmap
mkdir rana\data
mkdir rana\data\tiledata
mkdir rana\data\tracklogs
mkdir rana\cache
mkdir rana\cache\images
copy data\search_menu.txt rana\data\
copy places.txt rana
copy COPYING.txt rana
copy rana.py rana
copy run.bat rana
C:\cygwin\bin\tar -cf rana.tar rana
C:\cygwin\bin\gzip rana.tar
"C:\Program Files\tools\SSH\pscp" rana.tar.gz "ojw@dev.openstreetmap.org:/home/ojw/public_html/rana/rana_pkg_latest.tar.gz"
@pause
