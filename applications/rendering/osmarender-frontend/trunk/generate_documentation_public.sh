#!/bin/sh

echo "*** OSMARENDER FRONTEND *** Generating documentation"

echo "*** OSMARENDER FRONTEND *** Generating documentation for new CMYK library"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js cmyk -r -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/public/cmyk_new

echo "*** OSMARENDER FRONTEND *** Generating documentation for old CMYK library"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js cmyk.js -r -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/public/cmyk_old

echo "*** OSMARENDER FRONTEND *** Generating documentation for old Osmarender Frontend code"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js osmarender_frontend.js -r -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/public/osmarender_frontend_old

echo "*** OSMARENDER FRONTEND *** Generating documentation for new Osmarender Frontend code"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js osmarender_frontend -r -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/public/osmarender_frontend_new

echo "*** OSMARENDER FRONTEND *** Generating documentation for JUICE widget"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js juice -r -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/public/juice

echo "*** OSMARENDER FRONTEND *** documentation generated and can be found in /documentation directory"
