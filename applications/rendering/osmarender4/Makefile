#
#  Osmarender
#
#  Makefile
#

all: osmarender.xsl symbol-catalogue.png symbol-catalogue.pdf

# Create osmarender.xsl XSL style sheet from its parts. You only need to
# do this if you changed anything in the src directory.
osmarender.xsl: src/*.xsl src/interactive.js
	xmllint --xinclude src/main.xsl >osmarender.xsl

# Create SVG file with symbols catalogue. This is for documentation only,
# it is not needed for the maps.
symbol-catalogue.svg: symbol-catalogue.xsl symbols/*
	-(cd symbols; xmlstarlet ls >../symbols.dir)
	./xsltrans symbols.dir symbol-catalogue.xsl >$@
	rm -f symbols.dir

# Create PNG file with symbols catalogue from SVG file
symbol-catalogue.png: symbol-catalogue.svg
	inkscape --export-png=$@ --export-area-drawing --export-background=white $<

symbol-catalogue.pdf: symbol-catalogue.svg
	inkscape --export-ps=symbol-catalogue.ps --export-area-canvas $<
	ps2pdf symbol-catalogue.ps
	rm symbol-catalogue.ps

