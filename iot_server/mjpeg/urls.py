from django.urls import path
<<<<<<< HEAD
from mjpeg.views import *

urlpatterns = [
    path('', CamView.as_view()),
    path('snapshot/', snapshot, name='snapshot'),
    path('stream/', stream, name='stream'),
=======
from django.views.generic import TemplateView
from mjpeg.views import *


urlpatterns = [
    path('', CamView.as_view()),
    path('stream/', stream, name='stream'),
    path('upload/', upload, name='upload'),
    path('sec_file/', SecFileListView.as_view(), name='list'),
    path('sec_file/<int:pk>', SecFileDetailView.as_view(), name='detail')
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
]
