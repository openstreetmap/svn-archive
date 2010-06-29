# -*- coding: utf-8 -*-

#import string, cgi, time
#from os import curdir, sep
#from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
#import urlparse
#import osray
#import math

def avg(a,b): return (a+b)/2.0

def scale_bbox(self,old_bbox,scale):
    old_bbox = old_bbox.replace(' ',',')
    print "old_bbox ",old_bbox
    pointlist = map(lambda coord: float(coord), old_bbox.split(','))
    print "pointlist ",pointlist
    xmin = pointlist[0]
    ymin = pointlist[1]
    xmax = pointlist[2]
    ymax = pointlist[3]
    xmid = avg(xmin,xmax)
    ymid = avg(ymin,ymax)
    xradius = xmid-xmin
    yradius = ymid-ymin
    xradius *= scale
    yradius *= scale
    xmin = xmid-xradius
    xmax = xmid+xradius
    ymin = ymid-yradius
    ymax = ymid+yradius
    return str(xmin)+" "+str(ymin)+","+str(xmax)+" "+str(ymax)

def num2deg(xtile, ytile, zoom):
    n = 2.0 ** zoom
    lon_deg = xtile / n * 360.0 - 180.0
    lat_rad = math.atan(math.sinh(math.pi * (1 - 2 * ytile / n)))
    lat_deg = math.degrees(lat_rad)
    return(lat_deg, lon_deg)

def num2bbox(xtile, ytile, zoom):
    pass
    return 0

def shift_by_meters(lat, lon, brng, d):
    R = 6371000.0 # earth's radius in m
    lat=math.radians(lat)
    lon=math.radians(lon)
    lat2 = math.asin( math.sin(lat)*math.cos(d/R) + 
                      math.cos(lat)*math.sin(d/R)*math.cos(brng))
    lon2 = lon + math.atan2(math.sin(brng)*math.sin(d/R)*math.cos(lat), 
                             math.cos(d/R)-math.sin(lat)*math.sin(lat2))
    lat2=math.degrees(lat2)
    lon2=math.degrees(lon2)
    return [lat2,lon2]

def calc_bearing(x1,y1,x2,y2,side):
    Q = complex(x1,y1)
    R = complex(x2,y2)
    v = R-Q
    if side=='left':
        v=v*complex(0,1);
    elif side=='right':
        v=v*complex(0,-1);
    else:
        raise TypeError('side must be left or right')
    #v=v*(1/abs(v)) # normalize
    angl = angle(v) # angle (radians) (0°=right, and counterclockwise)
    bearing = math.pi/2.0-angl # (0°=up, and clockwise)
    return bearing

def parse_length_in_meters(length,default):
    units={
           'm':1.0, 'metres':1.0, 'meters':1.0, 'metre':1.0, 'meter':1.0,
           'km':1000.0,
           'ft':0.3,
           'yd':1.1,
           }
    parsed = re.split('(\d*[,.]?\d+)',length)
    if len(parsed)!=3:
        print "### unparsable length '{0}', parsed: {1}".format(length,parsed)
        return default
    prefix = parsed[0].strip()
    if prefix!='':
        print "### unknown prefix '{0}' in length '{1}'".format(prefix,length)
        return default
    unit = parsed[2].strip().lower()
    if unit=='': unit='m' # defaults to m
    factor = units.get(unit,0.0)
    if factor==0.0:
        print "### unknown unit '{0}' in length '{1}'".format(unit,length)
    meter = float(parsed[1])*factor
    return meter

