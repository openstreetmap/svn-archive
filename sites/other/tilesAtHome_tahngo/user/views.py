from django.http import Http404
from django.shortcuts import render_to_response
import django.contrib.auth.views
#from django.contrib.auth.models import User
from tah.user.models import TahUser
from tah.requests.models import Request

def index(request):
    return render_to_response("base_user.html");

def show_user(request):
    order = request.GET.get('order')
    if order == 'tiles': sortorder='-renderedTiles'
    elif order == 'upload':  sortorder='-kb_upload'
    else:                sortorder='-last_activity'
    u = TahUser.objects.filter(user__is_active=True).order_by(sortorder) # Get the first user in the system
    return render_to_response("user_show.html",{'user':u});


def show_single_user_byname(request, searchstring, by):
    return show_single_user(request, searchstring, by);

def show_single_user(request, searchstring, by):
    """ display detail page on a single user. 'by' can be 'pk' (primary key)
        and 'username' and specifies whether we
        look for a username or id number.
    """
    if by == 'pk':
      try: u = TahUser.objects.get(user__pk=searchstring) # Get the user in the system
      except TahUser.DoesNotExist: 
        raise Http404
    else: 
      try: u = TahUser.objects.get(user__username=searchstring) # Get the user in the system
      except TahUser.DoesNotExist:
        raise Http404

    # if we want the user id, need to look at TahUser.user.pk, not TahUser.pk as we use this for ident
    active = Request.objects.filter(status=1, client= u.user.pk)
    finished = Request.objects.filter(status=2, client= u.user.pk).order_by('-clientping_time')[:20]

    return render_to_response("user_show_specific.html",{'user':u, 'active_reqs': active, 'finished_reqs': finished});

#from django.contrib.auth import authenticate
#user = authenticate(username='john', password='secret')
#if user is not None:
#    if user.is_active:
#        print "You provided a correct username and password!"
#        login(request, user)
#    else:
#        print "Your account has been disabled!"
#else:
#    print "Your username and password were incorrect."

def login(request):
  return django.contrib.auth.views.login(request,template_name='user_login.html')