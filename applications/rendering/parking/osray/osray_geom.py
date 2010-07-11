# -*- coding: utf-8 -*-

#import string, cgi, time
#from os import curdir, sep
#from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
#import urlparse
#import osray
import math
import re

def avg(a,b): return (a+b)/2.0
def swap(tuple): return (tuple[1],tuple[0])

#
# string stuff to handle bbox strings
#
def bbox_string(xmin,ymin,xmax,ymax):
    return str(xmin)+" "+str(ymin)+","+str(xmax)+" "+str(ymax)

def bbox_format_3_to_1_comma(bbox):
    coords = bbox.split(',')
    if len(coords)!=4:
        raise 'wrong bbox format: '+bbox
    return coords[0]+" "+coords[1]+","+coords[2]+" "+coords[3]
    
def coords_from_bbox(bbox):
    bbox = bbox.replace(' ',',')
    coordslist = map(lambda coord: float(coord), old_bbox.split(','))
    return tuple(coordslist)

def scale_coords(xmin,ymin,xmax,ymax,scale):
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
    return (xmin,ymin,xmax,ymax)

def scale_bbox(bbox,scale):
    xmin,ymin,xmax,ymax = coords_from_bbox(bbox)
    xmin,ymin,xmax,ymax = scale_coords(xmin,ymin,xmax,ymax,scale)
    return bbox_string(xmin,ymin,xmax,ymax)

def num2deg(xtile, ytile, zoom):
    n = 2.0 ** zoom
    lon_deg = xtile / n * 360.0 - 180.0
    lat_rad = math.atan(math.sinh(math.pi * (1 - 2 * ytile / n)))
    lat_deg = math.degrees(lat_rad)
    return(lat_deg, lon_deg)

def num2bbox(xtile, ytile, zoom):
    bottomleft = swap(num2deg(xtile,ytile+1,zoom))
    topright = swap(num2deg(xtile+1,ytile,zoom))
    bbox = str(bottomleft)[1:-1].replace(',',' ')+","+str(topright)[1:-1].replace(',',' ')
    return bbox

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

def parse_int_safely(intstring,default):
    """ parses a intstring and returns an int - any error: returns default """
    if intstring==None:
        return default
    try:
        layer = int(intstring)
    except ValueError:
        print "### unknown integer '{0}'".format(intstring)
        layer = default
    return layer
