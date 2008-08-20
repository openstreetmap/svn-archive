#!/bin/sh

echo "*** OSMARENDER FRONTEND *** Generating documentation"

echo "*** OSMARENDER FRONTEND *** Generating documentation for new CMYK library"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js cmyk -r -p -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/private/cmyk_new

echo "*** OSMARENDER FRONTEND *** Generating documentation for old CMYK library"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js cmyk.js -r -p -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/private/cmyk_old

echo "*** OSMARENDER FRONTEND *** Generating documentation for old Osmarender Frontend code"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js osmarender_frontend.js -r -p -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/private/osmarender_frontend_old

echo "*** OSMARENDER FRONTEND *** Generating documentation for new Osmarender Frontend code"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js osmarender_frontend -r -p -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/private/osmarender_frontend_new

echo "*** OSMARENDER FRONTEND *** Generating documentation for JUICE widget"
java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js juice -r -p -a -t=jsdoctoolkit/templates/jsdoc -d=documentation/private/juice

echo "*** OSMARENDER FRONTEND *** documentation generated and can be found in /documentation directory"
