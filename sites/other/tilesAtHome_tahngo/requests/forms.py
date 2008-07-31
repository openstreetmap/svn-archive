from django import forms
from tah.requests.models import Request,Upload

CreateForm = forms.form_for_model(Request)
UploadForm = forms.form_for_model(Upload)

class ClientAuthForm(forms.Form):
     user = forms.CharField()
     passwd = forms.CharField()
     #capability = forms.CharField(required=False,widget=forms.HiddenInput)

