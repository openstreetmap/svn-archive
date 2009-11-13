
pages :=	\
	pages/DestinationPage.nsh	\
	pages/DevicePage.nsh	\
	pages/devices.ini	\
	pages/LicensePage.txt	\
	pages/maps.ini	\
	pages/PreparationsPage.nsh	\
	pages/SourcePage.nsh	\
	pages/WelcomePage.nsh


all: gosmin.exe

clean: 
	rm -f gosmin.exe
	rm -f pages/devices.ini
	rm -f pages/maps.ini
	rm -f pages/LicensePage.txt

pages/devices.ini: pages/devices.ini.xsl devices.xml
	xsltproc pages/devices.ini.xsl devices.xml > pages/devices.ini

pages/maps.ini: pages/maps.ini.xsl maps.xml
	xsltproc pages/maps.ini.xsl maps.xml > pages/maps.ini

pages/LicensePage.txt: pages/License.txt 3rdparty/License.txt images/License.txt
	cat pages/License.txt > pages/LicensePage.txt
	cat 3rdparty/License.txt >> pages/LicensePage.txt
	cat images/License.txt >> pages/LicensePage.txt
    
gosmin.exe: ${pages} gosmin.nsi gosmin.ico
	/cygdrive/c/Programme/nsis/makensis gosmin.nsi

#	"/cygdrive/c/Program Files/nsis/makensis" gosmin.nsi

do:    

test: gosmin.exe do
	./gosmin.exe