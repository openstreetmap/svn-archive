#!/usr/bin/python

import sys
from osgeo import gdal
from osgeo.gdalconst import *

argc=len(sys.argv)

if (argc != 2):
    sys.stderr.write("usage: %s file\n" % sys.argv[0])
    sys.exit(1)

dataset = gdal.Open( sys.argv[1], GA_ReadOnly )

tf=dataset.GetGeoTransform()

#print dataset.RasterXSize
#print dataset.RasterYSize
#print dataset.GetGeoTransform()

x1=tf[0]
y1=tf[3]

x2=tf[0] + tf[1] * dataset.RasterXSize + tf[2] * dataset.RasterYSize
y2=tf[3] + tf[4] * dataset.RasterXSize + tf[5] * dataset.RasterYSize

xmin=min(x1,x2)
xmax=max(x1,x2)
ymin=min(y1,y2)
ymax=max(y1,y2)

print '"wms_extent" "%f %f %f %f"' % (xmin,ymin,xmax,ymax)

