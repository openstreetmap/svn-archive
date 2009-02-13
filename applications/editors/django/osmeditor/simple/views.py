from django.conf import settings
from django import forms
from django.shortcuts import render_to_response
from django.http import HttpResponse, HttpResponseRedirect
from django.forms.util import ValidationError
import urllib

try:
    import httplib2
except:
    from third import httplib2

from lib import osmparser

def osmparser_obj(type, id, xml=None):
    if not xml:
        xml = get_xml_string(type, id)
    output = osmparser.parseString(xml, site_url=settings.OSM_SERVER)
    return output["%ss" % type][int(id)] 

def get_xml_string(type, id):
    url = "%s/%s/%s/full" % (settings.OSM_API, type, id)
    if type == "node":
        url = url.replace('/full','') # required because the API 404s on node/x/full - wtf?
    h = httplib2.Http()    
    (resp, content) = h.request(url)
    if int(resp.status) != 200:
        raise Exception("%s returned %s" % (url, resp.status))
    return content 

def edit_osm_obj(type, id, post, session={}):
    obj = osmparser_obj(type, id)
    if obj.timestamp != post['timestamp']:
        raise Exception("Object changed since you started editing it.")
    
    changed = False
    for key in filter(lambda x: x.startswith("delete_"), post.keys()):
        k = key.replace("delete_key_", "")
        if k in obj.tags:
            changed = True
            del obj.tags[k]
        else:
            raise Exception("Huh? %s is not in tags" % k)
    
    for key in filter(lambda x: x.startswith("key_"), post.keys()):
        k = key.replace("key_", "")
        if k in obj.tags and post[key] != obj.tags[k]:
            changed = True
            obj.tags[k] = post[key]
   
    for key in filter(lambda x: x.startswith("new_key_"), post.keys()):
        new_id = key.replace("new_key_", "")
        kname = "new_key_%s" % new_id
        vname = "new_value_%s" % new_id
        if kname in post and vname in post:
            k = post[kname]
            v = post[vname]
            if k and v:
                obj.tags[k] = v
                changed = True
            
    if not changed: return

    username = post.get('username', None) or session.get("username", None)
    password = post.get('password', None) or session.get("password", None)
    if not username or not password:
        raise Exception("Need username and password")
    obj.save(username, password)
    return obj

def tag_sorter(a, b):
    if ':' in a and not ':' in b:
        return 1
    elif ':' in b and not ':' in a:
        return -1
    return cmp(a,b)

def load(request, type="node", id=0):
    if request.method == "POST":
        obj = edit_osm_obj(type, id, request.POST, request.session)
        return HttpResponseRedirect(obj.local_url())   
    xml = get_xml_string(type, id)
    obj = osmparser_obj(type, id, xml=xml)
    
    tag_list = obj.tags.keys()
    tag_list.sort(tag_sorter)
    sorted_tags = []
    for k in tag_list:
        sorted_tags.append((k, obj.tags[k]))
    return render_to_response("obj.html",{'obj':obj, 'obj_xml': xml, 
        'sorted_tags': sorted_tags,
        'logged_in': ('username' in request.session)})

def home(request):
    return render_to_response("home.html", {'logged_in': 'username' in request.session})

class UserForm(forms.Form):
    email = forms.CharField(max_length=255)
    password = forms.CharField(widget=forms.PasswordInput)

    def clean(self):
        if self.cleaned_data.get('password') and self.cleaned_data.get('password'):
            http = httplib2.Http()   
            http.add_credentials(self.cleaned_data['email'], self.cleaned_data['password'])
            url = "%s/user/details" % (settings.OSM_API)
            resp, content = http.request(url, "GET")
            if int(resp.status) == 200:
                return self.cleaned_data
            else:
                raise ValidationError("Could not authenticate against OSM API") 
                        

def login(request):
    if request.method == "POST":
        form = UserForm(request.POST)
        if form.is_valid():
            request.session['username'] = form.cleaned_data['email']
            request.session['password'] = form.cleaned_data['password']
            return HttpResponseRedirect("/")
    else:
        form = UserForm()
    return render_to_response("login.html", {'form': form})    

def logout(request):
    request.session.flush()
    return HttpResponseRedirect("/")    

def help(request):
    return render_to_response("help.html")

def api_proxy(request, url):
    http = httplib2.Http()   
    if request.session['username']:
        http.add_credentials(request.session['username'], request.session['password'])
    url = "%s/%s?%s" % (settings.OSM_API, url, urllib.urlencode(request.GET))
    body = (request.raw_post_data or None)
    resp, content = http.request(url, request.method, body=body)
    return HttpResponse(content, content_type="application/osm+xml", status=resp.status)
