#!/usr/bin/python
# -*- coding: UTF8 -*-
'''
Created on 15.11.2010

@author: Matthias Meißer

Harvests all user groups and generates a KML file immeadedly
'''

#TODO
#OL linken
#Zeit einbauen
from wikitools import wiki
from wikitools import api
from wikitools import pagelist
from time import * 
import getopt, sys
import urllib
import re
import mykml
import sys
import os.path

ugroup={} #the parsed dictionary of the template attributes
count=0

def nospaces(s):
    return (s.lstrip().rstrip()).replace('\n',"") 
    

def writeDate(filename):
    out = open(filename,'w+');
    out.write('var export_date="'+strftime("%Y-%m-%dT %H:%M:%S UTC",gmtime())+'";')
    out.write('var export_number="'+str(count)+'";')
    out.close()

def reformatLinks(source):    
    global site
    #converts hyperlinks in wiki syntax to HTML links
    if source.find("[http://") > -1 :
        start=source.find("[")
        mid=source.find(" ",start)
        end=source.find("]",mid)
        intro=source[0:start]
        url=source[start+1:mid]
        caption=source[mid+1:end]
        outro=source[end+1:]
        return intro+" <a href='"+url+"'>"+caption+"</a> "+outro
    else:
        #convert wikilinks to hyperlinks
        if source.find("[[") > -1 :
            start=source.find("[[")
            end=source.find("]]")
            url=source[start+2:end]
            return " <a href='"+str(site.domain)+"/" +url+"'>"+url+"</a> "
        else:
            #add simple weblinks
            if (source.find("http://") > -1) or (source.find("www.") > -1) :
                return " <a href='"+source+"'>"+source+"</a> "
                
            
            return source #no wiki in there
    
def genGermanLegacy():
    global count
    global k
    #get all templates 
    allUserGroups = {'action':'query', 'list':'embeddedin',"eititle":"Template:Lokale_Gruppe","eilimit":"500"}
    request = api.APIRequest(site, allUserGroups)
    result = request.query()
     #for group in result["query"]["embeddedin"]: print(urllib.unquote(group["title"]))
    list=pagelist.listFromQuery(site,result["query"]["embeddedin"])
    for page in list:
        count=count+1
#        print page.title,
        if page.getWikiText(False).find("{{Lokale Gruppe")>-1 : #some embedded the template within other templates so we receive fakes 
            source=page.getWikiText(False);
            source=urllib.unquote(source).decode("utf-8")
            #extract template and it's attributes
            start=source.find("{{Lokale Gruppe")+len("{{Lokale Gruppe")+1
            end=source.find("}}",start)
             #print source[start:end]
            for attr in source[start:end].split("|"):
                #print attr
                items=attr.split("=")
                if len(items)==2 : # some formatings have otherwise strange effects
                    ugroup[str(items[0]).replace("\n","")]=items[1] #remove leading linebreaks
#            for x in ugroup.keys():
#                print x
#                print ugroup[x]
            #add to KML after reformating
            point=nospaces(ugroup["lon"]),nospaces(ugroup["lat"])
            name=nospaces(ugroup["name"])
            where=nospaces(reformatLinks(ugroup["meets_where"]))
            when=nospaces(ugroup["meets_when"])
            mail=nospaces(ugroup["mailing_list_url"])
            wikipage=nospaces("http://wiki.openstreetmap.org/wiki/"+page.title)
            k.add_placemark(name,point,"usergroup",{"where":where,"when":when,"mail":mail,"wiki":wikipage})
#            print " ok"
        else:
            pass
#            print " skipped"
    
def genAll():
    global count
    global k
    #get all templates 
    allUserGroups = {'action':'query', 'list':'embeddedin',"eititle":"Template:user_group","eilimit":"500"}
    request = api.APIRequest(site, allUserGroups)
    result = request.query()
     #for group in result["query"]["embeddedin"]: print(urllib.unquote(group["title"]))
    list=pagelist.listFromQuery(site,result["query"]["embeddedin"])
    for page in list:
        count=count+1
#        print page.title,
        if page.getWikiText(False).find("{{user group")>-1 : #some embedded the template within other templates so we receive fakes 
            source=page.getWikiText(False).decode("utf-8") #API uses UTF-8
            source=urllib.unquote(source).replace('\n',"")
            #extract template and it's attributes
            start=source.find("{{user group")+len("{{user group")+1
            end=source.find("}}",start)
            #print source[start:end]
            ugroup.clear()
            for attr in source[start:end].split("|"):
                #print attr
                items=attr.split("=",1)                
                if len(items)==2 : # some formatings have otherwise strange effects
                    ugroup[nospaces(str(items[0]))]=nospaces(items[1]) #remove leading linebreaks
#            for x in ugroup.keys():
#                print x
#                print ugroup[x]
            #add to KML after reformating
            name=where=when=url=mail=wikipage=photo=""
            point=(ugroup["lon"]),nospaces(ugroup["lat"])
            name=ugroup.get("name","")
            where=reformatLinks(ugroup.get("meets_where",""))
            when=ugroup.get("meets_when","")
            url=ugroup.get("url","")
            mail=ugroup.get("mailing_list_url","")
            wikipage="http://wiki.openstreetmap.org/wiki/"+page.title
            photo=ugroup.get("photo","")
            if not photo.isspace() :
                if len(photo)>1 :
                    photo=getImageURL(photo) #the fotos need additional API magic
            k.add_placemark(name,point,"usergroup",{"where":where,"when":when,"url":url,"wiki":wikipage,"mail":mail,"photo":photo})
#            print " ok"
        else:
             pass
#            print " skipped"
    


def getImageURL(name):
    global site
    if not name.isspace():
        imageURL = {'action':'query', 'prop':'imageinfo',"iiprop":"url","titles":"Image:"+name}
        request = api.APIRequest(site, imageURL)
        result = request.query()
        return  result["query"]["pages"].values()[0]["imageinfo"][0]["url"]


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
    #create a KML
    k=mykml.kml("user_groups","Generated list of OpenStreetMaps local user groups")
    k.add_style("usergroup","localgroup.png")   
    #connect to OSM wiki
    site = wiki.Wiki("http://wiki.openstreetmap.org/w/api.php")
    site.login(user,password)
    site.setUserAgent("UserGroupsBot 0.1")
    genGermanLegacy() #old stuff till replaced by new one
    genAll()
#    print "finished "+str(count)
    path,x=os.path.split(sys.argv[0])
    filename=os.path.join(path,"www","osm_user_groups.kml")
    k.save(filename)
    filename=os.path.join(path,"www","date.js")
    writeDate(filename)
        
    
