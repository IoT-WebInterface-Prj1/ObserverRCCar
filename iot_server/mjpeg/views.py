from django.views.generic import TemplateView
from django.http import HttpResponse, StreamingHttpResponse
from .picam import MJpegStreamCam

mjpegstream = MJpegStreamCam()

class CamView(TemplateView):
    template_name = "cam.html" # 랜더링할 템플릿 파일 경로

    def get_context_data(self):
        context = super().get_context_data()
        context["mode"] = self.request.GET.get("mode", "#")
        return context
    
def snapshot(request):
    image = mjpegstream.snapshot()
    return HttpResponse(image, content_type="image/jpeg")

def stream(request):
    return StreamingHttpResponse(mjpegstream, content_type='multipart/x-mixed-replace;boundary=--myboundary')