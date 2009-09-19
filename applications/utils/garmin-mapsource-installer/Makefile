all: dist/Setup.exe

dist/Setup.exe: dist/garmin-mapsource-installer.ini garmin-mapsource-installer.au3 
	wine autoit/Aut2Exe/Aut2Exe.exe /in garmin-mapsource-installer.au3 /out dist/Setup.exe /icon logo.ico

dist/garmin-mapsource-installer.ini: dist
	iconv -f UTF-8 -t ISO-8859-1 --output dist/garmin-mapsource-installer.ini garmin-mapsource-installer.ini

dist-directory:
	mkdir dist

dist: dist-directory dist/garmin-mapsource-installer.ini dist/Setup.exe

clean:
	rm -rfv dist
