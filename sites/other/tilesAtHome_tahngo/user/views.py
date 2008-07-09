from django.shortcuts import render_to_response
import django.contrib.auth.views

def index(request):
    return render_to_response("base_user.html");

def show_user(request):
    from django.contrib.auth.models import User
    #u = User.objects.filter(is_superuser=False) # Get the first user in the system
    u = User.objects.filter(is_active=True) # Get the first user in the system
    return render_to_response("user_show.html",{'user':u});

#u=User.objects.create_user(username, email, password)
#u.save()

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