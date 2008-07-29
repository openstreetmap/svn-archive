from django.utils.encoding import force_unicode
from django.shortcuts import render_to_response
from django.contrib.auth.models import User
import django.views.generic.list_detail
from django.http import HttpResponse, HttpResponseNotFound, HttpResponseForbidden
from tah.requests.models import Request,Upload
from tah.requests.forms import CreateForm,UploadForm,ClientAuthForm
from django.newforms import form_for_model,widgets
from datetime import datetime, timedelta
import urllib
import xml.dom.minidom
from django.contrib.auth import authenticate
#from tah.user.auth import OSMBackend
from tah.tah_intern.models import Layer
from tah.tah_intern.Tile import Tile
from django.views.decorators.cache import cache_control
from tah.tah_intern.models import Settings


def index(request):
  return render_to_response('base_requests.html');

#shortcut view to see all requests
def show_first_page(request):
  return show_requests(request,1)

@cache_control(must_revalidate=True, max_age=30)
def show_requests(request,page):
  if page: pagination=30 
  else: pagination=0
  return django.views.generic.list_detail.object_list(request, queryset=Request.objects.filter(status=0).order_by('-request_time'),template_name='requests_show.html',allow_empty=True,paginate_by=pagination,page=page,template_object_name='new_reqs',extra_context={'active_reqs_list':Request.objects.filter(status=1).order_by('-clientping_time')[:30]});


@cache_control(must_revalidate=True, max_age=30)
def show_uploads_page(request):
  return django.views.generic.list_detail.object_list(request, queryset=Request.objects.filter(status=2).order_by('-clientping_time')[:30],template_name='requests_show_uploads.html',allow_empty=True,template_object_name='reqs');


def saveCreateRequestForm(request, form):
    """ Returns (Request, reason), with Request being 'None' on failure and 
        the saved request object on success. 
        Reason is a string that describes the error in case of failure.
    """
    formdata = form.cleaned_data.copy()
    # delete entries that are not needed as default value or won't work
    del formdata['layers']
    del formdata['status']
    if not formdata['min_z'] in ['6','12']: formdata['min_z'] = 12
    if not formdata['max_z']: formdata['max_z'] = {0:5,6:11,12:17}[formdata['min_z']]
    if not formdata['priority'] or \
      formdata['priority']>4 or formdata['priority']<1: 
        formdata['priority'] = 3
    formdata['clientping_time'] = force_unicode(datetime.now().strftime('%Y-%m-%d %H:%M:%S'))

    # catch invalid x,y
    if not Tile(None,formdata['min_z'],formdata['x'],formdata['y']).is_valid():
      return (None, 'Invalid tile coordinates')

    # Deny request if the same is already out rendering
    if Request.objects.filter(status=1, min_z=formdata['min_z'], x=form.data['x'], y=form.data['y']).count():
      return (None, 'Request currently rendering')

    # Create a new request, or get the existing one
    newRequest, created_new = Request.objects.get_or_create(status=0,min_z=formdata['min_z'], x=form.data['x'], y=form.data['y'],defaults=formdata)

    newRequest.ipaddress = request.META['REMOTE_ADDR']
    if not created_new:
      #update existing request with new request data
      newRequest.max_z = max(formdata['max_z'],newRequest.max_z)
      newRequest.priority = min(formdata['priority'],newRequest.priority)

    # finally save the updated request
    newRequest.save()
    if form.data.has_key('layers'):
      # save the chosen layers
      layers = Layer.objects.filter(pk__in=form['layers'].data)
    else:
      # no layers selected -> save default layers
      layers = Layer.objects.filter(default=True)
    for l in layers: newRequest.layers.add(l)
    return (newRequest,'')

def create(request):
    html="XX|unknown error"
    CreateFormClass = CreateForm
    CreateFormClass.base_fields['ipaddress'].required = False
    CreateFormClass.base_fields['ipaddress'].widget = widgets.HiddenInput()
    CreateFormClass.base_fields['priority'].required = False
    CreateFormClass.base_fields['status'].required = False 
    CreateFormClass.base_fields['status'].widget = widgets.HiddenInput()
    CreateFormClass.base_fields['min_z'].required = False 
    CreateFormClass.base_fields['max_z'].required = False 
    CreateFormClass.base_fields['max_z'].widget = widgets.HiddenInput()
    CreateFormClass.base_fields['layers'].widget = widgets.CheckboxSelectMultiple(  
         choices=CreateFormClass.base_fields['layers'].choices)
    CreateFormClass.base_fields['layers'].required = False
    CreateFormClass.base_fields['clientping_time'].required = False
    CreateFormClass.base_fields['clientping_time'].widget = widgets.HiddenInput()
    CreateFormClass.base_fields['client'].required = False
    CreateFormClass.base_fields['client'].widget = widgets.HiddenInput()
    form = CreateFormClass()

    if request.method == 'POST':
      form = CreateForm(request.POST)
      if form.is_valid():
        req, reason = saveCreateRequestForm(request, form)
      else:
        html="form is not valid. "+str(form.errors)
    else:
      #Create request using GET"
      form = CreateForm(request.GET)
      if form.is_valid():
        req, reason = saveCreateRequestForm(request, form)
      else:
         # view the plain form webpage with default values filled in
         return render_to_response('requests_create.html', \
                {'createform': form, 'host':request.META['HTTP_HOST']})
    if req: html = "Render '%s' (%s,%s,%s)" % \
                    (req.layers_str,req.min_z,req.x,req.y)
    else:   html = "Request failed (%s,%s,%s): %s" \
                    % (form.cleaned_data['min_z'],form.cleaned_data['x'],form.cleaned_data['y'],reason)
    return HttpResponse(html)


def upload_request(request):
    html='XX|Unknown error.'
    UploadFormClass = UploadForm
    UploadFormClass.base_fields['ipaddress'].required = False
    UploadFormClass.base_fields['ipaddress'].widget = widgets.HiddenInput()
    UploadFormClass.base_fields['priority'].required = False
    UploadFormClass.base_fields['file'].required = False
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

@cache_control(max_age=30)
def upload_gonogo(request):
    # dummy, always return 1 for full speed ahead
    load = 1 - Upload.objects.all().count()/600.0
    return HttpResponse(str(load));

def take(request):
    html='XX|4|unknown error'
    if request.method == 'POST':
      form = ClientAuthForm(request.POST)
      if form.is_valid():
        name     = form.cleaned_data['user']
	passwd   = form.cleaned_data['passwd']
        user = authenticate(username=name, password=passwd)
        if user is not None:
            #"You provided a correct username and password!"
            try:
 	        req = Request.objects.filter(status=0).order_by('priority','request_time')[0]
 	        req.status=1
 	        req.client = user
 	        req.clientping_time=datetime.now()
                req.save()
	        html="OK|4|"+str(req)
                #req.delete()
            except IndexError:
                html ="XX|4|No requests in queue"
        else:
            html="XX|4|Invalid username. Your username and password were incorrect or the user has been disabled."
      else: #form was not valid
        html = "XX|4|Form invalid. "+str(form.errors)
      return HttpResponse(html);
    else: #request.method != POST, show the web form
      form = ClientAuthForm()
      return render_to_response('requests_take.html',{'clientauthform': form})
    return HttpResponse(html)

@cache_control(no_cache=True)
def request_changedTiles(request):
    setting = Settings()
    #check if we access this page from the whitelisted ip address
    allowed_ip = setting.getSetting('changed_tiles_allowed_IP')
    if not allowed_ip: allowed_ip = setting.setSetting('changed_tiles_allowed_IP',request.META['REMOTE_ADDR'])
    if allowed_ip != request.META['REMOTE_ADDR']:
      return HttpResponseForbidden('Access not allowed from this IP address.')
    #fetch the url that is called to retrieve the changed tiles
    url = setting.getSetting('changed_tiles_api_url')
    if not url: url = setting.setSetting('changed_tiles_api_url','http://www.openstreetmap.org/api/0.5/changes?hours=4')

    html="Requested tiles:\n"
    xml_dom = xml.dom.minidom.parse(urllib.urlopen(url))
    tiles = xml_dom.getElementsByTagName("tile")
    for tile in tiles:
      (z,x,y) = tile.getAttribute('z'),tile.getAttribute('x'),tile.getAttribute('y')
      CreateFormClass = CreateForm
      CreateFormClass.base_fields['max_z'].required = False 
      CreateFormClass.base_fields['layers'].required = False 
      CreateFormClass.base_fields['status'].required = False 
      form = CreateFormClass({'min_z': z, 'x': x, 'y': y, 'priority': 2})
      if form.is_valid():
        req, reason = saveCreateRequestForm(request, form)
        if req:
          html += "Render '%s' (%s,%s,%s)\n" % (req.layers_str,form.cleaned_data['min_z'],form.cleaned_data['x'],form.cleaned_data['y'])
        else: html +="Renderrequest failed (%s,%s,%s): %s\n" % (form.cleaned_data['min_z'],form.cleaned_data['x'],form.cleaned_data['y'], reason)
      else:
        html+="form is not valid. %s\n" % form.errors
    xml_dom.unlink()
    
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

    html="Reset %d requests to unfinished status.\nExpired %d old finished requests." % (len(rerequested),len(expired))
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
