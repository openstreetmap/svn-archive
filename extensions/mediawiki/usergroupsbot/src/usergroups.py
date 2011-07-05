# -*- coding: UTF8 -*-
'''
Created on 20.06.2011

@author: Matthias Meißer
'''

import pickle
import mykml
import os.path
import sys
import logging

def saveCache(groups):
    fcache=file("usergroups.cache","w")
    pickle.dump(groups,fcache)
    fcache.close()

def loadCache():
    groups={}
    try:
        fcache=file("usergroups.cache","r")
        groups=pickle.load(fcache)
        fcache.close()        
    except IOError:
        logging.log(logging.WARNING, "no cache file present!")
    return groups    

def getNewestUserGroups(currGroups):
    oldGroups=[]
    newGroups=[]
    #flatten lists
    for group in loadCache():
        oldGroups.append(group["name"])
    for group in currGroups:
        newGroups.append(group["name"])
    diff = list(set(newGroups)-set(oldGroups))
    diff=__appendNewestUserGroups(diff)
    return diff

def __appendNewestUserGroups(newgroups):
    try:
        fcache=file("lastgroups.cache","r")
        lastgroups=pickle.load(fcache)
        fcache.close()        
    except IOError:
        logging.log(logging.WARNING, "no lastgroups file present!")
        lastgroups=[]
    lastgroups=newgroups+lastgroups
    if len(lastgroups)>=5:
        lastgroups=lastgroups[0:4]
    fcache=file("lastgroups.cache","w")
    pickle.dump(lastgroups,fcache)
    fcache.close()
    return lastgroups
    
    
      

def exportUserGroups(groups,filename):
    #create a KML
    k=mykml.kml("OSM usergroups worldwide","Generated list of OpenStreetMap local user groups by UserGroupsBot")
    k.add_style("usergroup","localgroup.png")   
    #save groups
    for ugroup in groups:
        point=ugroup["lonlat"]
        name=ugroup["name"]
        rest=ugroup.copy();
        del rest["lonlat"]
        del rest["name"]   
        k.add_placemark(name,point,"usergroup",rest)
    k.save(filename)

def exportUserGroupsCountries(groups,langCodes,path):
    countries={}
    for ugroup in groups:
        try:
            countries[ugroup["country"]].append(ugroup)
        except KeyError:
            countries[ugroup["country"]]=[ugroup]
    for country in langCodes:
        k=mykml.kml("OSM usergroups "+country,"Generated list of OpenStreetMap local user groups by UserGroupsBot")
        k.add_style("usergroup","localgroup.png")
        try:
            for ugroup in countries[country]:
                point=ugroup["lonlat"]
                name=ugroup["name"]
                rest=ugroup.copy();
                del rest["lonlat"]
                del rest["name"]
                k.add_placemark(name,point,"usergroup",rest)
                filename="osm_user_groups-"+country+".kml"
                filename=os.path.join(path,filename)
                k.save(filename)
        except KeyError: sys.stderr.write("\nNo matches for country: "+country)   