#
#  Osmarender
#
#  Makefile
#

all: osmarender.xsl symbols.svg symbol-catalogue.png symbol-catalogue.pdf

# Create osmarender.xsl XSL style sheet from its parts. You only need to
# do this if you changed anything in the src directory.
osmarender.xsl: src/*.xsl src/interactive.js
	xmllint --xinclude src/main.xsl >osmarender.xsl

# Join all the symbols in the symbols directory into one file.
# This file can later be included into map files.
symbols.svg: symbols/*
	echo '<?xml version="1.0" encoding="utf-8"?>' >$@
	echo '<!-- This file is created automatically from contents of the' >>$@
	echo '     symbols directory. DO NOT CHANGE THIS FILE! -->' >>$@
	echo '<svg xmlns="http://www.w3.org/2000/svg">' >>$@
	echo '    <defs>' >>$@
	cat symbols/*svg >>$@
	echo '    </defs>' >>$@
	echo '</svg>' >>$@

# Create SVG file with symbols catalogue. This is for documentation only,
# it is not needed for the maps.
symbol-catalogue.svg: symbol-catalogue.xsl symbols.svg
	./xsltrans symbols.svg symbol-catalogue.xsl >$@

# Create PNG file with symbols catalogue from SVG file
symbol-catalogue.png: symbol-catalogue.svg
	inkscape --export-png=$@ --export-area-drawing --export-background=white $<

symbol-catalogue.pdf: symbol-catalogue.svg
	inkscape --export-ps=symbol-catalogue.ps --export-area-canvas $<
	ps2pdf symbol-catalogue.ps
	rm symbol-catalogue.ps

