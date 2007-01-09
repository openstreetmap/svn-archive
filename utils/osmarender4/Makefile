#
#  Osmarender
#
#  Makefile
#

# Create osmarender.xsl XSL style sheet from its parts. You only need to
# do this if you changed anything in the src directory.
osmarender.xsl: src/*.xsl src/interactive.js
	xmllint --xinclude src/main.xsl >osmarender.xsl

