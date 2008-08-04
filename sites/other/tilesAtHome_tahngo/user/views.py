from django.shortcuts import render_to_response
import django.contrib.auth.views
from django.contrib.auth.models import User
from tah.user.models import TahUser

def index(request):
    return render_to_response("base_user.html");

def show_user(request):
    order = request.GET.get('order')
    if order == 'tiles': sortorder='-renderedTiles'
    elif order == 'upload':  sortorder='-kb_upload'
    else:                sortorder='-last_activity'
    u = TahUser.objects.filter(user__is_active=True).order_by(sortorder) # Get the first user in the system
    return render_to_response("user_show.html",{'user':u});

def show_single_user(request, username):
    u = TahUser.objects.get(user__is_active=True,user__username=username) # Get the user in the system
    return render_to_response("user_show_specific.html",{'user':u});

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