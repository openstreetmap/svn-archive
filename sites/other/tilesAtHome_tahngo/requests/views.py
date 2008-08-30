import logging, os
from django.utils.encoding import force_unicode
from django.shortcuts import render_to_response
import django.views.generic.list_detail
from django.http import HttpResponse, HttpResponseNotFound, HttpResponseForbidden
from tah.requests.models import Request,Upload
from tah.requests.forms import *
from django.conf import settings
from django.forms import widgets
from datetime import datetime, timedelta
import urllib
import xml.dom.minidom
from django.contrib.auth import authenticate
from tah.tah_intern.models import Layer
from tah.tah_intern.Tile import Tile
from tah.tah_intern.Tileset import Tileset
from django.views.decorators.cache import cache_control
from tah.tah_intern.models import Settings

###############################################
# requests.views.py does all the action with regard to creating requests,
# taking error feedback and receiving uploads
###############################################

#-------------------------------------------------------
# Show the base request homepage

def index(request):
  return render_to_response('base_requests.html');

#-------------------------------------------------------
# Show the recent uploads in flashy ajax

def show_ajax(request):
  return render_to_response('requests_show_ajax.html');

#-------------------------------------------------------
# shortcut to see the first page of the show_request page

def show_first_page(request):
  return show_requests(request,1)

#-------------------------------------------------------
# show the list of recently created requests and the list
# of recently taken requests

@cache_control(max_age=10)
def show_requests(request,page):
  if page: pagination=30 
  else: pagination=0
  return django.views.generic.list_detail.object_list(request, \
    queryset=Request.objects.filter(status=0).order_by('-request_time'), \
    template_name='requests_show.html', allow_empty=True, paginate_by=pagination, \
    page=page, template_object_name='new_reqs', extra_context = {
    'active_reqs_list':Request.objects.filter(status=1).order_by('-clientping_time')[:30]
    });

#-------------------------------------------------------
# Show a list of recently uploaded requests

@cache_control(max_age = 10)
def show_uploads_page(request):
  return django.views.generic.list_detail.object_list(request, \
        queryset=Request.objects.filter(status=2).order_by('-clientping_time')[:30], \
        template_name='requests_show_uploads.html',allow_empty=True, \
        template_object_name='reqs');

#-------------------------------------------------------
# not a public view, but a helper function that creates a new request based on data in
# 'form' and performs sanity checks etc. The form must have been validated previously.

def saveCreateRequestForm(request, form):
    """ Returns (Request, reason), with Request being 'None' on failure and 
        the saved request object on success. 
        Reason is a string that describes the error in case of failure.
    """
    formdata = form.cleaned_data.copy()

    # delete layer here. We use the layers from ^'form' later.
    del formdata['layers']

    #sanity changes and default values on some attributes
    if not formdata['min_z'] in ['6','12']: formdata['min_z'] = 12
    if not formdata['priority'] or \
      formdata['priority'] > 4 or formdata['priority'] < 1: 
          formdata['priority'] = 3
    formdata['clientping_time'] = force_unicode(datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
    if not formdata['src']: formdata['src'] = '' # requester did not supply a 'src' string

    # catch invalid x,y and return if found
    if not Tile(None,formdata['min_z'],formdata['x'],formdata['y']).is_valid():
        return (None, 'Invalid tile coordinates')

    # Deny request if the same is already out rendering
    if Request.objects.filter(status=1, min_z=formdata['min_z'], x=form.data['x'], \
      y=form.data['y']).count():
          return (None, 'Request currently rendering')

    # Create a new request, or get the existing one
    newRequest, created_new = Request.objects.get_or_create(status=0, \
        min_z=formdata['min_z'], x=form.data['x'], y=form.data['y'],defaults=formdata)

    # set more values in the new request item
    newRequest.ipaddress = request.META['REMOTE_ADDR']
    newRequest.max_z = {0:5,6:11,12:17}[formdata['min_z']]

    if not created_new:
        # increase priority if we update existing request (if needed)
        newRequest.priority = min(formdata['priority'], newRequest.priority)

    ##check if the IP has already lot's of high priority requests going and auto-bump down
    if formdata['priority'] == 1:
        ip_requested = Request.objects.filter(status__lt= 2, \
          ipaddress= request.META['REMOTE_ADDR']).count()

        if ip_requested > 15:
            # auto bump down to a minimum of 2
            newRequest.priority = max(2,newRequest.priority)

    # finally save the updated request, the required layers are still missing then.
    newRequest.save()

    # add layers to the request
    if form.data.has_key('layers'):
        # save the chosen layers
        layers = Layer.objects.filter(pk__in=form['layers'].data)
    else:
        # no layers selected -> save default layers
        layers = Layer.objects.filter(default=True)
    for l in layers: newRequest.layers.add(l)

    # Finally return the request. Success!
    return (newRequest,'')


#-------------------------------------------------------
# view that creates a new request

def create(request):
    html="XX|unknown error"
    req = None
    CreateFormClass = CreateForm
    CreateFormClass.base_fields['priority'].required = False
    CreateFormClass.base_fields['min_z'].required = False 
    CreateFormClass.base_fields['layers'].widget = widgets.CheckboxSelectMultiple(  
         choices=CreateFormClass.base_fields['layers'].choices)
    CreateFormClass.base_fields['layers'].required = False
    form = CreateFormClass()

    if request.method == 'POST':
      form = CreateForm(request.POST)
      if form.is_valid():
        req, reason = saveCreateRequestForm(request, form)
      else:
        html="form is not valid. "+str(form.errors)
        return HttpResponse(html)
    else:
      #Create request using GET"
      form = CreateForm(request.GET)
      if form.is_valid():
        req, reason = saveCreateRequestForm(request, form)
      else:
         # view the plain form webpage with default values filled in
         return render_to_response('requests_create.html', \
                {'createform': form, 'host':request.META['HTTP_HOST']})
    if req: html = "Render '%s' (%s,%s,%s) at priority %d" % \
                    (req.layers_str,req.min_z,req.x,req.y,req.priority)
    else:   html = "Request failed (%s,%s,%s): %s" \
                    % (form.cleaned_data['min_z'],form.cleaned_data['x'],form.cleaned_data['y'],reason)
    return HttpResponse(html)

def feedback(request):
    html="XX|unknown error"
    CreateFormClass = CreateForm
    CreateFormClass.base_fields['priority'].required = False
    CreateFormClass.base_fields['layers'].widget = widgets.CheckboxSelectMultiple(  
         choices=CreateFormClass.base_fields['layers'].choices)
    CreateFormClass.base_fields['layers'].required = False
    form = CreateFormClass()
    if request.method == 'POST':
      authform = ClientAuthForm(request.POST)
      if authform.is_valid():
        name     = authform.cleaned_data['user']
	passwd   = authform.cleaned_data['passwd']
        user = authenticate(username=name, password=passwd)
        if user is not None:
          #"You provided a correct username and password!"

          form = CreateForm(request.POST)
          if form.is_valid():
            formdata = form.cleaned_data
            reqs = Request.objects.filter(status=1,x = formdata['x'],y=formdata['y'],min_z = formdata['min_z'])
            num = reqs.count()
            reqs.update(status=0)
            html = "Reset %d tileset (%d,%d,%d)" % \
                      (num,formdata['min_z'],formdata['x'],formdata['y'])
            logging.info("%s by user %s (uuid: %s). Cause: %s" %(html,user,request.POST.get('client_uuid','0'),request.POST.get('cause','unknown')))
      	  else:
            html="XX|4|form is not valid. "+str(form.errors)
      else:
        # authentication failed here, or authform failed to validate
        html="XX|4|Invalid username. Your username and password were incorrect or the user has been disabled."
    elif request.method == 'GET':
         # view the plain form webpage with default values filled in
         return render_to_response('requests_create.html', \
                {'createform': form, 'host':request.META['HTTP_HOST']})
    return HttpResponse(html)


#-------------------------------------------------------
# Upload a finished tileset

def upload_request(request):
    html='XX|Unknown error.'
    UploadFormClass = UploadForm
    UploadFormClass.base_fields['ipaddress'].required = False
    UploadFormClass.base_fields['ipaddress'].widget = widgets.HiddenInput()
    UploadFormClass.base_fields['priority'].required = False
    UploadFormClass.base_fields['file'].required = False
    UploadFormClass.base_fields['client_uuid'].required = False
    UploadFormClass.base_fields['client_uuid'].widget = widgets.HiddenInput()
    UploadFormClass.base_fields['is_locked'].required = False
    UploadFormClass.base_fields['is_locked'].widget = widgets.HiddenInput()
    UploadFormClass.base_fields['user_id'].required = False
    UploadFormClass.base_fields['user_id'].widget = widgets.HiddenInput()
    form = UploadFormClass()

    if request.method == 'POST':
      authform = ClientAuthForm(request.POST)
      if authform.is_valid():
        name     = authform.cleaned_data['user']
	passwd   = authform.cleaned_data['passwd']
        user = authenticate(username=name, password=passwd)
        if user is not None:
          #"You provided a correct username and password!"
          formdata = request.POST.copy()
          formdata['user_id'] = user.id # set the user to the correct value
          # we had huge client_uuids in the beginnning, shorten if necessary
          if formdata.has_key('client_uuid') and int(formdata['client_uuid']) > 65535:
            formdata['client_uuid'] = formdata['client_uuid'][-4:]
          #t@h client send layername, rather than layer number,
          #find the right one if that is the case
          if formdata.has_key('layer'):
            try: int(formdata['layer'])
            except ValueError:
              # look up the layer id
              try: formdata['layer'] = Layer.objects.get(name=formdata['layer']).id
              except Layer.DoesNotExist: del formdata['layer']
          form = UploadFormClass(formdata, request.FILES)

          if form.is_valid():
            file = request.FILES['file']
            try:
              newUpload= form.save(commit=False)
              #             #   filetype = file['content-type']   
              newUpload.ipaddress = request.META['REMOTE_ADDR']
              # low priority upload by default
              if not newUpload.priority: newUpload.priority = 3
              if not newUpload.client_uuid: newUpload.client_uuid = 0
              newUpload.is_locked = False
              #newUpload.save_file_file(file['filename'],file['content'])
              newUpload.save()
	      html="OK|4|"
            except:
              #TODO: delete possibly uploaded file, in case of exception
	      import sys
              html ="XX| Exception thrown."+str(user.id)+str(sys.exc_info()[1])
      	  else:
            html="XX|4|form is not valid. "+str(form.errors)
        else:
          # authentication failed here, or authform failed to validate.
          html="XX|4|Invalid username. Your username and password were incorrect or the user has been disabled."
    else:
      # request.method==GET here. View the plain form webpage with default values filled in
      authform = ClientAuthForm()
      return render_to_response('requests_upload.html',{'uploadform': form, 'authform': authform, 'host':request.META['HTTP_HOST']})
    return HttpResponse(html);


#-------------------------------------------------------
# return upload queue load

def upload_gonogo(request):
    # return 'fullness' of the server queue between [0,1]
    load = min(Upload.objects.all().count()/1500.0, 1)
    return HttpResponse(str(load));

#-------------------------------------------------------
# retrieve a new request from the server

def take(request):
    html='XX|5|unknown error'
    if request.method == 'POST':
      # cleanup, we had huge client_uuids in the beginnning, shorten if necessary
      if request.POST.has_key('client_uuid') and int(request.POST['client_uuid']) > 65535:
        request.POST['client_uuid'] = request.POST['client_uuid'][-4:]

      authform = ClientAuthForm(request.POST)
      form     = TakeRequestForm(request.POST)
      form.is_valid() #clean data
      if authform.is_valid():
        name     = authform.cleaned_data['user']
	passwd   = authform.cleaned_data['passwd']
        user = authenticate(username=name, password=passwd)
        if user is not None:
          #"You provided a correct username and password!"
          # next, check for a valid client version
          if form.cleaned_data['version'] in ['Rapperswil', 'Saurimo']:
            try:  
                #next 2 lines are for limiting max #of active requests per usr
                active_user_reqs = Request.objects.filter(status=1,client=user.id).count()
                if active_user_reqs <= 50:
                  req = Request.objects.filter(status=0).order_by('priority','request_time')[0]
 	          req.status=1
 	          req.client = user
 	          req.client_uuid = form.cleaned_data.get('client_uuid', 0)
 	          req.clientping_time=datetime.now()
                  req.save()
                  # find out tileset filesize and age
                  # (always 'tile' layer for now. need to find something better)
                  tilelayer = Layer.objects.get(name='tile')
                  (tilepath, tilefile) = Tileset(tilelayer, req.min_z, req.x, req.y).get_filename(settings.TILES_ROOT)
                  tilefile = os.path.join(tilepath, tilefile)
                  try: 
                    fstat = os.stat(tilefile)
                    (fsize,mtime) = (fstat[6], fstat[8])
                  except OSError: 
                    (fsize,mtime) = 0,0
	          html="OK|5|%s|%d|%d" % (req,mtime,fsize)
                else:
                  html ="XX|5|You have more than 50 active requests. Check your client."
            except IndexError:
                html ="XX|5|No requests in queue"
          else:
            # client version no in whitelist
            logging.info("User %s connects with disallowed client '%s'." %(user,form.cleaned_data['version']))
            html="XX|5|Invalid client version."
        else:
            # user is None, auth failed
            html="XX|5|Invalid username. Your username and password were incorrect or the user has been disabled."
      else: #form was not valid
        html = "XX|5|Form invalid. "+str(form.errors)
      return HttpResponse(html);
    else: #request.method != POST, show the web form
      authform, form = ClientAuthForm(), TakeRequestForm()
      return render_to_response('requests_take.html',{'clientauthform': authform, 'takeform': form})
    return HttpResponse(html)

@cache_control(no_cache=True)
def request_changedTiles(request):
    #http://www.openstreetmap.org/api/0.5/changes?start=2008-08-18-18:40&end=now
    #<osm version="0.5" generator="OpenStreetMap server">
    #<changes starttime="2008-08-18T18:40:00+01:00" endtime="2008-08-18T20:50:29+01:00">
    setting = Settings()
    #check if we access this page from the whitelisted ip address
    allowed_ip = setting.getSetting('changed_tiles_allowed_IP')
    if not allowed_ip: allowed_ip = setting.setSetting('changed_tiles_allowed_IP',request.META['REMOTE_ADDR'])
    if allowed_ip != request.META['REMOTE_ADDR']:
      return HttpResponseForbidden('Access not allowed from this IP address.')
    #fetch the url that is called to retrieve the changed tiles
    url = setting.getSetting('changed_tiles_api_url')
    if not url: url = setting.setSetting('changed_tiles_api_url','http://www.openstreetmap.org/api/0.5/changes?hours=6')

    html="Requested tiles:\n"
    xml_dom = xml.dom.minidom.parse(urllib.urlopen(url))
    tiles = xml_dom.getElementsByTagName("tile")
    for tile in tiles:
      (z,x,y) = tile.getAttribute('z'),tile.getAttribute('x'),tile.getAttribute('y')
      CreateFormClass = CreateForm
      CreateFormClass.base_fields['layers'].required = False 
      form = CreateFormClass({'min_z': z, 'x': x, 'y': y, 'priority': 2, 'src':'ChangedTileAutoRequest'})
      if form.is_valid():
        req, reason = saveCreateRequestForm(request, form)
        if req:
          html += "Render '%s' (%s,%s,%s)\n" % (req.layers_str,form.cleaned_data['min_z'],form.cleaned_data['x'],form.cleaned_data['y'])
        else: html +="Renderrequest failed (%s,%s,%s): %s\n" % (form.cleaned_data['min_z'],form.cleaned_data['x'],form.cleaned_data['y'], reason)
      else:
        html+="form is not valid. %s\n" % form.errors
    xml_dom.unlink()
    logging.info("Requested %d changes Tilesets." % len(tiles))
    return HttpResponse(html,mimetype='text/plain')

@cache_control(no_cache=True)
def expire_tiles(request):
    """ Rerequest old active request that haven't been pinged by the client
        for a while. Also delete old finished requests that we don't need 
        anymore.
    """
    rerequested = Request.objects.filter(status=1,clientping_time__lt=datetime.now()-timedelta(0,0,0,0,0,6))
    for r in rerequested:
      #next django version will be able to just update() this
      r.status=0
      r.save()

    expired = Request.objects.filter(status=2, clientping_time__lt=datetime.now()-timedelta(2,0,0,0,0,0))
    expired.delete()

    html="Reset %d requests to unfinished status. Expired %d old finished requests." % (len(rerequested),len(expired))
    logging.debug(html)
    return HttpResponse(html,mimetype='text/plain')

@cache_control(no_cache=True)
def stats_munin_requests(request,status):
    reply=''
    states={'pending':0,'active':1,'done':2}
    if states.has_key(status): state = states[status]
    else: return HttpResponseNotFound('Unknown request state')
    reqs = Request.objects.filter(status=state)
    if state < 2:
      #output low/medium/high requests for unfinished ones
      for val, state in enumerate(['high','medium','low']):
        c = reqs.filter(priority=val+1).count()
        reply += "%s.value %d\n" % (state,c)
    else:
      #output requests finished per last hour and 48 moving avg
      reply += "_req_processed_last_hour.value %d\n" %  (reqs.filter(clientping_time__gt=datetime.now()-timedelta(0,0,0,0,0,1)).count())
      reply += 'done.value %d' % (reqs.filter(clientping_time__gt=datetime.now()-timedelta(0,0,0,0,0,48)).count() // 48)
    return HttpResponse(reply,mimetype='text/plain')

@cache_control(must_revalidate=True, max_age=3600)
def show_latest_client_version(request):
    html = Settings().getSetting(name='latest_client_version')
    return HttpResponse(html,mimetype='text/plain')
