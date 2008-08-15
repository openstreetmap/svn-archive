from django import forms
from tah.requests.models import Request,Upload

CreateForm = forms.form_for_model(Request)
#Delete some fields unrequired fields
del CreateForm.base_fields['client']
del CreateForm.base_fields['ipaddress']
del CreateForm.base_fields['status']
del CreateForm.base_fields['clientping_time']
del CreateForm.base_fields['max_z']

UploadForm = forms.form_for_model(Upload)

class ClientAuthForm(forms.Form):
     user = forms.CharField()
     passwd = forms.CharField()

class TakeRequestForm(forms.Form):
     version = forms.CharField(required=False)
     layers = forms.CharField(required=False)
     layerspossible = forms.CharField(required=False)

