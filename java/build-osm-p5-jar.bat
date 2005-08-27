@rem a lazy windows batch file to get just the util files required for the 
@rem processing applet into a jar file, to copy into the OSMApplet code 
@rem folder in the Processing development environment
@rem it's not elegant, but it works
mkdir dist
javac -cp lib/core.jar;lib/commons-codec-1.3.jar;lib/xmlrpc-2.0-beta.jar -d dist -source 1.3 -target 1.3 src/org/openstreetmap/processing/*.java src/org/openstreetmap/processing/util/*.java
pause
jar cvf dist/OSMApplet.jar -C dist org/openstreetmap/processing/*.class -C dist org/openstreetmap/processing/util/*.class -C lib core.jar -C lib commons-codec-1.3.jar -C lib xmlrpc-2.0-beta.jar
pause
del src\org\openstreetmap\processing\*.class
del src\org\openstreetmap\processing\util\*.class
