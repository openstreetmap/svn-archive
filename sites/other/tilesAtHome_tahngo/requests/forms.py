from django import forms
from tah.requests.models import Request,Upload

CreateForm = forms.form_for_model(Request)
#Delete some fields unrequired fields
del CreateForm.base_fields['client']
del CreateForm.base_fields['ipaddress']
del CreateForm.base_fields['status']
del CreateForm.base_fields['clientping_time']
del CreateForm.base_fields['max_z']
del CreateForm.base_fields['client_uuid']
CreateForm.base_fields['src'].required = False
UploadForm = forms.form_for_model(Upload)

class ClientAuthForm(forms.Form):
     user = forms.CharField()
     passwd = forms.CharField()

class TakeRequestForm(forms.Form):
     version = forms.CharField(required=False,initial="Quickborn",widget=forms.widgets.HiddenInput())
     layerspossible = forms.CharField(required=False, initial="tile,maplint,captionless,caption")
     client_uuid = forms.IntegerField(min_value=0,max_value=65535,required=False,widget=forms.widgets.HiddenInput(),initial=0)
