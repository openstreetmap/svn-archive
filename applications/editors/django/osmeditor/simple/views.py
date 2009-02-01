import xml.dom.minidom as m
from django.conf import settings
from django import forms
from django.shortcuts import render_to_response
from django.http import HttpResponse, HttpResponseRedirect
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
    url = "%s/%s/%s/full" % (settings.OSM_API, type, id)
    if type == "node":
        url = url.replace('/full','') # required because the API 404s on node/x/full - wtf?
    u = urllib.urlopen(url)
    data = u.read()
    doc = m.parseString(data)
    return doc

def get_osm_obj(type, id, doc=None):
    obj = {}
    if not doc:
        doc = get_xml(type, id)
    obj['xml'] = doc.toxml() #expose this to make debugging easier

    obj['children'] = [] # we're giving everyone 0 or more children

    if type == "node":
        parent = doc.getElementsByTagName("node")[0]
        referencer = parent
        obj['lat'] = parent.getAttribute("lat")
        obj['lon'] = parent.getAttribute("lon")

    elif type == "way":
        parent = doc.firstChild
        referencer = doc.getElementsByTagName("way")[0]
        for node in parent.getElementsByTagName("node"):
            obj['children'].append( {'ref': node.getAttribute("id"),
                                  'type':'node',
                                  'tags': get_tags(node),
                                  'tag_count': node.getElementsByTagName("tag").length
                                 } )

    elif type == "relation":
        parent = doc.firstChild

        # this pre-loop seems the only robust way to select and classify child XML elements - need XPath
        memberElements = []
        for element in parent.getElementsByTagName("*"):
            if element.parentNode == parent:
                if element.getAttribute("id") == id:
                    referencer = element
                else:
                    memberElements.append(element)

        for member in memberElements:
            refs = [ref for ref in referencer.getElementsByTagName("member") if ref.getAttribute("ref") == member.getAttribute("id")]
            if len(refs):
                pointer = refs[0]
                obj['children'].append( { 'ref': member.getAttribute("id"),
                                       'type': member.nodeName, # could also use pointer.getAttribute("type") - maybe that's a safer bet?
                                       'role': pointer.getAttribute("role"),
                                       'tags': get_tags(member),
                                       'tag_count': member.getElementsByTagName("tag").length
                                    } )

    obj['child_count'] = len(obj['children'])
    obj['timestamp'] = referencer.getAttribute("timestamp")
    obj['id'] = int(id)
    obj['type'] = type
    obj['tags'] = get_tags(referencer)
    #hmm, not sure how the UI should manage the created_by tag
    #obj['tags']['created_by'] = "%s (%s)" % (settings.APP_NAME,settings.APP_URI)
    obj['user'] = { 'id': referencer.getAttribute("user") ,
                    'uri': settings.OSM_USER % referencer.getAttribute("user") 
                  }
    obj['uri'] = { 'browse': "%s/%s/%s" % (settings.OSM_BROWSE, type.lower(), id),
                   'xml_std': "%s/%s/%s" % (settings.OSM_API, type.lower(), id),
                   'xml_full': "%s/%s/%s/full" % (settings.OSM_API, type.lower(), id) # echh, this url is just wrong
                 }
    obj['foo'] = referencer.nodeName
    return obj 

def get_tags(parent):
    tagset = {}
    for tag in parent.getElementsByTagName("tag"):
        tagset[tag.getAttribute("k")] = tag.getAttribute("v")
    return tagset

def edit_osm_obj(type, id, post, session={}):
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
        parent = SubElement(osm, type, {'id':id })
        for nd in xml.getElementsByTagName("nd"):
            nd = SubElement(parent, "nd", {'ref': nd.getAttribute("ref")})
    
    elif type == "relation":
        parent = SubElement(osm, type, {'id':id, })
        for m in xml.getElementsByTagName("member"):
            member = SubElement(parent, "member", {'type': m.getAttribute("type"),
                                                   'id': m.getAttribute("id"),
                                                   'role': m.getAttribute("role")
                                                  }
                               )
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
  
    username = post.get('username', None) or session.get("username", None)
    password = post.get('password', None) or session.get("password", None)
    if not username or not password:
        raise Exception("Need username and password")
    xml = tostring(osm)
    change_obj(type, id, xml, username, password)

def change_obj(type, id, xml, username, password):    
    url = "%s/%s/%s" % (settings.OSM_API, type, id)
    http = httplib2.Http()   
    http.add_credentials(username, password)
    resp, content = http.request(url, "PUT", body=xml)    
    if int(resp.status) != 200:
        raise Exception("%s, %s \n%s" % (resp.status, url, content))

def load(request, type="node", id=0):
    if request.method == "POST":
        edit_osm_obj(type, id, request.POST, request.session)
        return HttpResponseRedirect("/%s/%s/" % (type, id))    
    obj = get_osm_obj(type, id)
    
    return render_to_response("obj.html",{'obj':obj, 
        'logged_in': ('username' in request.session)})

def home(request):
    return render_to_response("home.html", {'logged_in': 'username' in request.session})

class UserForm(forms.Form):
    email = forms.CharField(max_length=255)
    password = forms.CharField(widget=forms.PasswordInput)

def login(request):
    if request.method == "POST":
        form = UserForm(request.POST)
        if form.is_valid():
            username = form.cleaned_data['email']
            password = form.cleaned_data['password']
            http = httplib2.Http()   
            http.add_credentials(username, password)
            url = "%s/user/details" % (settings.OSM_API)
            resp, content = http.request(url, "GET")
            if int(resp.status) == 200:
                request.session['username'] = username 
                request.session['password'] = password 
                return HttpResponseRedirect("/")    
    else:
        form = UserForm()
    return render_to_response("login.html", {'form': form})    

def api_proxy(request, url):
    http = httplib2.Http()   
    url = "%s/%s" % (settings.OSM_API, url)
    resp, content = http.request(url, "GET")
    return HttpResponse(content, content_type="application/osm+xml")
