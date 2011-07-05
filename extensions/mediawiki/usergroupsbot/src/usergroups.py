# -*- coding: UTF8 -*-
'''
Created on 20.06.2011

@author: Matthias Meißer
'''

import pickle
import mykml
import os.path
import sys

def saveCache(groups):
    fcache=file("wiki_nodes.cache","w")
    pickle.dump(groups,fcache)
    fcache.close()

def loadCache():
    groups={}
    fcache=file("wiki_nodes.cache","r")
    groups=pickle.load(fcache)
    fcache.close()
    return groups

def getNewestUserGroups(currGroups):
    oldGroups=loadCache
    diff = set(currGroups)-set(oldGroups)
    return diff  

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