#!/usr/bin/python
# -*- coding: UTF8 -*-
'''
Created on 15.11.2010

@author: Matthias Meißer

Harvests all user groups and generates KML files for every country

http://wiki.openstreetmap.org/wiki/Template:User_group
http://wiki.openstreetmap.org/wiki/User:UserGroupsBot
'''

#TODO
#OL linken
#Zeit einbauen
import osmwiki
import usergroups
from time import * 
import getopt, sys
import urllib
import re
import mykml
import sys
import os.path

ugroup={} #the parsed dictionary of the template attributes
count=0


    

def writeStat(filename):
    count=len(groups)
    out = open(filename,'w+');
    out.write('var export_date="'+strftime("%Y-%m-%dT %H:%M:%S UTC",gmtime())+'";')
    out.write('var export_number="'+str(count)+'";')
    out.close()



if __name__ == '__main__':
    global site
    global k
    #parse args
    try:
        opts, args = getopt.getopt(sys.argv[1:], "u:p:", ["user=", "password="])
    except getopt.GetoptError, err:
        print str(err) # will print something like "option -a not recognized"
        print "Usage: -u username -p password"
        sys.exit(2)
    user = None
    password = None
    for o, a in opts:
        if o == "-u":
            user=a
        elif o == "-p":
            password=a
    groups=osmwiki.loadAllUserGroups(user, password)
    path,x=os.path.split(sys.argv[0])
    filename=os.path.join(path,"www","osm_user_groups.kml")
    usergroups.exportUserGroups(groups,filename)
    filename=os.path.join(path,"www")
    usergroups.exportUserGroupsCountries(groups, ["DE","UK"], filename)
    filename=os.path.join(path,"www","stat.js")
    writeStat(filename,groups)
        
    
