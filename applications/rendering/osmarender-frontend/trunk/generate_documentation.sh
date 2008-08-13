#!/bin/sh

echo "*** OSMARENDER FRONTEND *** Generating documentation"

java -jar jsdoctoolkit/jsrun.jar jsdoctoolkit/app/run.js cmyk -r -t=jsdoctoolkit/templates/jsdoc -d=cmyk_documentation

echo "*** OSMARENDER FRONTEND *** documentation generated and can be found in cmyk_documentation directory"
