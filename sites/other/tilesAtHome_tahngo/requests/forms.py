from django import forms
from django.db import models
from tah.requests.models import Request,Upload

class CreateForm(forms.ModelForm):
  class Meta:
    model = Request
    exclude = ('client','ipaddress','status','request_time', 'clientping_time','max_z','client_uuid')
#-----------------------------------------------------------------
class UploadForm(forms.ModelForm):
  class Meta:
    model = Upload
    exclude = ('ipaddress', 'client_uuid', 'is_locked')
#-----------------------------------------------------------------
class ClientAuthForm(forms.Form):
     user = forms.CharField()
     passwd = forms.CharField()

class TakeRequestForm(forms.Form):
     version = forms.CharField(required=False,initial="Ulm",widget=forms.widgets.HiddenInput())
     layerspossible = forms.CharField(required=False, initial="tile,maplint,captionless,caption")
     client_uuid = forms.IntegerField(min_value=0,max_value=65535,required=False,widget=forms.widgets.HiddenInput(),initial=0)
