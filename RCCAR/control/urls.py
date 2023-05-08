from django.urls import path
from .views import *
from django.views.generic import TemplateView #

app_name = 'control'

urlpatterns = [
    path('drive/', drive, name = "drive"),
]
