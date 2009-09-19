all: dist

dist/Setup.exe: garmin-mapsource-installer.au3 
	wine autoit/Aut2Exe/Aut2Exe.exe /in garmin-mapsource-installer.au3 /out dist/Setup.exe /icon logo.ico

dist/garmin-mapsource-installer.ini:
	iconv -f UTF-8 -t ISO-8859-1 --output dist/garmin-mapsource-installer.ini garmin-mapsource-installer.ini

dist-directory:
	if [ ! -d dist ]; then mkdir dist; fi

dist: dist-directory dist/garmin-mapsource-installer.ini dist/Setup.exe

clean:
	rm -rfv dist
