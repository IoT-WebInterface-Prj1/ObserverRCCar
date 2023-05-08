from django.urls import path
from .views import *
from django.views.generic import TemplateView #https://github.com/yannJu/Gateway/blob/master/Server/NodeMCU_MQTT/templates/iot/mqtt.html

app_name = 'control'

urlpatterns = [
    path('drive/', TemplateView.as_view(template_name='iot/mqtt.html')),
]
