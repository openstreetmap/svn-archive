
import xml.dom.minidom as m
from django.conf import settings
from django.shortcuts import render_to_response

try:
    import httplib2
except:
    from third import httplib2

try:
    from xml.etree.cElementTree import Element, SubElement, tostring 
except:
    from third.ElementTree import Element, SubElement, tostring

import urllib

def get_xml(type, id):
    url = "%s/%s/%s" % (settings.OSM_API, type, id)
    u = urllib.urlopen(url)
    data = u.read()
    doc = m.parseString(data)
    return doc

def get_osm_obj(type, id, doc=None):
    obj = {}
    if not doc:
        doc = get_xml(type, id)
    parent = doc.getElementsByTagName(type)[0]
    obj['timestamp'] = parent.getAttribute("timestamp")
    if type == "node":
        obj['lat'] = parent.getAttribute("lat")
        obj['lon'] = parent.getAttribute("lon")
    obj['id'] = int(id)
    obj['type'] = type
    obj['tags'] = {}
    for tag in parent.getElementsByTagName("tag"):
        obj['tags'][tag.getAttribute("k")] = tag.getAttribute("v")
    return obj 

def edit_osm_obj(type, id, post):
    xml = get_xml(type, id)
    obj = get_osm_obj(type, id, xml)
    if obj['timestamp'] != post['timestamp']:
        raise Exception("Object changed since you started editing it.")
    osm = Element('osm', {'version': '0.5'})
    if type == "node":
        parent = SubElement(osm, type, {'id':id, 
                                        'lat':obj['lat'],
                                        'lon':obj['lon']
                                        })
    elif type == "way":
        parent = SubElement(osm, type, {'id':id, })
        for nd in xml.getElementsByTagName("nd"):
            nd = SubElement(parent, "nd", {'ref': nd.getAttribute("ref")})
    
    else:
        raise Exception("%s not supported" % type)
    changed = False
    for key in filter(lambda x: x.startswith("delete_"), post.keys()):
        k = key.replace("delete_key_", "")
        if k in obj['tags']:
            changed = True
            del obj['tags'][k]
        else:
            raise Exception("Huh? %s is not in tags" % k)
    
    for key in filter(lambda x: x.startswith("key_"), post.keys()):
        k = key.replace("key_", "")
        if k in obj['tags'] and post[key] != obj['tags'][k]:
            changed = True
            obj['tags'][k] = post[key]
   
    for key in filter(lambda x: x.startswith("new_key_"), post.keys()):
        new_id = key.replace("new_key_", "")
        kname = "new_key_%s" % new_id
        vname = "new_value_%s" % new_id
        if kname in post and vname in post:
            k = post[kname]
            v = post[vname]
            if k and v:
                obj['tags'][k] = v
                changed = True
            
    if not changed: return

    for k, v in obj['tags'].items():
        SubElement(parent, "tag", {'k': k, 'v': v})
    
    if not 'username' in post or not 'password' in post:
        raise Exception("Need username and password")
    xml = tostring(osm)
    change_obj(type, id, xml, post['username'], post['password'])

def change_obj(type, id, xml, username, password):    
    url = "%s/%s/%s" % (settings.OSM_API, type, id)
    http = httplib2.Http()   
    http.add_credentials(username, password)
    resp, content = http.request(url, "PUT", body=xml)    
    if int(resp.status) != 200:
        raise Exception(resp.status)

def load(request, type="node", id=0):
    if request.method == "POST":
        edit_osm_obj(type, id, request.POST)
    obj = get_osm_obj(type, id)
    return render_to_response("obj.html",{'obj':obj})

def home(request):
    return render_to_response("home.html")
