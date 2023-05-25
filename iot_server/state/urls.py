from django.urls import path
from django.views.generic import TemplateView
from state.views import *


urlpatterns = [
    path('', StateView.as_view()),
]
