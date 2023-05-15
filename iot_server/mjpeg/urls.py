from django.urls import path
from mjpeg.views import *

urlpatterns = [
    path('', CamView.as_view()),
    path('stream/', stream, name='stream'),
    path('upload/', upload, name='upload'),
    path('sec_file/', SecFileListView.as_view(), name='list'),
    path('sec_file/<int:pk>', SecFileDetailView.as_view(), name='detail')

]
