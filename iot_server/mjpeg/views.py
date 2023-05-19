from django.views.generic import TemplateView
from django.http import HttpResponse, StreamingHttpResponse, JsonResponse
from .picam import MJpegStreamCam
from django.views import generic
from django.views.decorators.csrf import csrf_exempt
from .models import SecFile
from django.urls import path


mjpegstream = MJpegStreamCam()

class CamView(TemplateView):
    template_name = "cam.html" # 랜더링할 템플릿 파일 경로

    def get_context_data(self):
        context = super().get_context_data()
        context["mode"] = self.request.GET.get("mode", "#")
        return context
    

def stream(request):
    return StreamingHttpResponse(mjpegstream, content_type='multipart/x-mixed-replace;boundary=--myboundary')

##########################################################################################

@csrf_exempt
def upload(request):
    if request.method == 'POST' :
        file_name = request.POST['file_name']
        sec_file = request.FILES['sec_file']
        model = SecFile(file_name = file_name, sec_file=sec_file)
        model.save()
        print('upload file', file_name, sec_file)
        msg = { 'result' : 'success' }
    else:
        msg = { 'result' : 'fail' }
    return JsonResponse(msg)

class SecFileListView(generic.ListView):
    model = SecFile
    template_name = 'mjpeg/sec_file_list.html'
    context_object_name = 'sec_files'

class SecFileDetailView(generic.DetailView):
    model = SecFile
    template_name = 'mjpeg/sec_file_detail.html'
    context_object_name = 'vfile'